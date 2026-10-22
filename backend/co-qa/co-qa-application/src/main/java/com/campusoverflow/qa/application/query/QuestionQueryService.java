package com.campusoverflow.qa.application.query;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.application.port.ContentRenderer;
import com.campusoverflow.qa.application.port.QaReadModel;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.application.port.ViewCounter;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 问答查询用例（读路径）。 */
@Service
@Transactional(readOnly = true)
public class QuestionQueryService {

    private static final int EXCERPT_LENGTH = 160;

    private final QaReadModel readModel;
    private final IdentityApi identity;
    private final ContentRenderer renderer;
    private final ViewCounter viewCounter;
    private final QuestionListCache cache;

    public QuestionQueryService(QaReadModel readModel, IdentityApi identity, ContentRenderer renderer,
                                ViewCounter viewCounter, QuestionListCache cache) {
        this.readModel = readModel;
        this.identity = identity;
        this.renderer = renderer;
        this.viewCounter = viewCounter;
        this.cache = cache;
    }

    public PageResult<QuestionSummaryView> list(Long courseId, PageRequest page) {
        return cache.get(courseId, page).orElseGet(() -> {
            PageResult<QuestionRow> rows = readModel.listQuestions(courseId, page);
            Set<Long> authorIds = rows.items().stream().map(QuestionRow::authorId).collect(Collectors.toSet());
            Map<Long, UserSummary> users = identity.findUsers(authorIds);
            List<QuestionSummaryView> views = rows.items().stream().map(row -> {
                String excerpt = renderer.toPlainText(row.body(), EXCERPT_LENGTH);
                AuthorView author = AuthorView.of(row.authorId(), users.get(row.authorId()));
                return new QuestionSummaryView(row.id(), row.title(), excerpt, row.tags(), row.courseId(),
                        author, row.score(), row.answerCount(), row.viewCount(), row.acceptedAnswerId() != null,
                        row.createdAt());
            }).toList();
            PageResult<QuestionSummaryView> result = new PageResult<>(views, rows.total(), page.page(), page.size());
            cache.put(courseId, page, result);
            return result;
        });
    }

    public QuestionDetailView detail(long questionId, Actor actorOrNull) {
        QuestionRow row = readModel.findQuestion(questionId)
                .orElseThrow(() -> new NotFoundException("QA-1201", "问题不存在: " + questionId));
        viewCounter.increment(questionId);

        List<AnswerRow> answerRows = readModel.findAnswers(questionId);
        List<CommentRow> commentRows = readModel.findComments(questionId);

        Set<Long> userIds = new HashSet<>();
        userIds.add(row.authorId());
        answerRows.forEach(a -> userIds.add(a.authorId()));
        commentRows.forEach(c -> userIds.add(c.authorId()));
        answerRows.forEach(a -> { if (a.endorsedBy() != null) userIds.add(a.endorsedBy()); });
        Map<Long, UserSummary> users = identity.findUsers(userIds);

        Map<String, Integer> myVotes = Map.of();
        if (actorOrNull != null) {
            List<Long> answerIds = answerRows.stream().map(AnswerRow::id).toList();
            myVotes = readModel.findMyVotes(actorOrNull.userId(), questionId, answerIds);
        }

        AuthorView questionAuthor = AuthorView.of(row.authorId(), users.get(row.authorId()));
        CourseRefView course = row.courseId() != null
                ? identity.findCourse(row.courseId()).map(c -> new CourseRefView(c.id(), c.code(), c.name()))
                        .orElse(null)
                : null;

        PermissionsView permissions = buildPermissions(row, actorOrNull);
        int myQuestionVote = myVotes.getOrDefault("QUESTION:" + questionId, 0);

        List<CommentView> questionComments = buildComments(commentRows.stream()
                .filter(c -> "QUESTION".equals(c.targetType()) && c.targetId() == questionId).toList(), users);

        List<AnswerView> answerViews = answerRows.stream().map(ar -> {
            AuthorView answerAuthor = AuthorView.of(ar.authorId(), users.get(ar.authorId()));
            AuthorView endorsed = ar.endorsedBy() != null
                    ? AuthorView.of(ar.endorsedBy(), users.get(ar.endorsedBy())) : null;
            int myAnswerVote = myVotes.getOrDefault("ANSWER:" + ar.id(), 0);
            boolean canEditAnswer = actorOrNull != null
                    && (actorOrNull.userId() == ar.authorId() || isManager(actorOrNull, row.courseId()));
            List<CommentView> answerComments = buildComments(commentRows.stream()
                    .filter(c -> "ANSWER".equals(c.targetType()) && c.targetId() == ar.id()).toList(), users);
            return new AnswerView(ar.id(), ar.body(), renderer.toSafeHtml(ar.body()), answerAuthor, ar.score(),
                    row.acceptedAnswerId() != null && row.acceptedAnswerId() == ar.id(), endorsed, myAnswerVote,
                    canEditAnswer, ar.createdAt(), ar.updatedAt(), answerComments);
        }).sorted(Comparator.comparing(AnswerView::accepted).reversed()
                .thenComparing(AnswerView::score).reversed()).toList();

        return new QuestionDetailView(row.id(), row.title(), row.body(), renderer.toSafeHtml(row.body()),
                row.tags(), course, questionAuthor, row.score(), row.answerCount(), row.viewCount() + 1,
                row.acceptedAnswerId(), myQuestionVote, row.createdAt(), row.updatedAt(), permissions,
                questionComments, answerViews);
    }

    public List<TagView> popularTags(int limit) {
        return readModel.popularTags(limit);
    }

    private PermissionsView buildPermissions(QuestionRow row, Actor actor) {
        if (actor == null) {
            return PermissionsView.NONE;
        }
        boolean manager = isManager(actor, row.courseId());
        boolean isAuthor = actor.userId() == row.authorId();
        boolean canEdit = isAuthor || manager;
        boolean canDelete = (isAuthor && row.answerCount() == 0) || manager;
        boolean canAccept = isAuthor || manager;
        return new PermissionsView(canEdit, canDelete, canAccept, manager);
    }

    private boolean isManager(Actor actor, Long courseId) {
        return actor.isTeacherOrAdmin() || identity.canManageCourse(actor.userId(), courseId);
    }

    private List<CommentView> buildComments(List<CommentRow> rows, Map<Long, UserSummary> users) {
        return rows.stream().map(cr -> new CommentView(cr.id(),
                AuthorView.of(cr.authorId(), users.get(cr.authorId())), cr.parentId(), cr.body(), cr.createdAt()))
                .sorted(Comparator.comparing(CommentView::createdAt)).toList();
    }
}
