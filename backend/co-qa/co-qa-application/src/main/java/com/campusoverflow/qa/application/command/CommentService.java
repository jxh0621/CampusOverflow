package com.campusoverflow.qa.application.command;

import com.campusoverflow.qa.api.event.CommentPostedEvent;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.AnswerRepository;
import com.campusoverflow.qa.domain.Comment;
import com.campusoverflow.qa.domain.CommentRepository;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：评论与二级回复。评论中的 @昵称 由 Discovery 解析并发送提醒。 */
@Service
public class CommentService {

    private final CommentRepository comments;
    private final QuestionRepository questions;
    private final AnswerRepository answers;
    private final IntegrationEventPublisher events;
    private final Clock clock;

    public CommentService(CommentRepository comments, QuestionRepository questions, AnswerRepository answers,
                          IntegrationEventPublisher events, Clock clock) {
        this.comments = comments;
        this.questions = questions;
        this.answers = answers;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public long post(Actor actor, PostCommentCommand cmd) {
        long questionId;
        if (cmd.targetType() == TargetType.QUESTION) {
            Question q = questions.findById(cmd.targetId())
                    .orElseThrow(() -> new NotFoundException("QA-3101", "问题不存在: " + cmd.targetId()));
            questionId = q.id();
        } else {
            Answer a = answers.findById(cmd.targetId())
                    .orElseThrow(() -> new NotFoundException("QA-3102", "回答不存在: " + cmd.targetId()));
            questionId = a.questionId();
        }
        Comment parent = null;
        if (cmd.parentId() != null) {
            parent = comments.findById(cmd.parentId())
                    .orElseThrow(() -> new NotFoundException("QA-3103", "父评论不存在: " + cmd.parentId()));
        }
        Comment comment = Comment.post(cmd.targetType(), cmd.targetId(), questionId, actor.userId(), parent,
                cmd.body(), clock.instant());
        comment = comments.save(comment);
        events.publish(new CommentPostedEvent(EventIds.next(), clock.instant(), comment.id(), questionId,
                actor.userId(), cmd.body()));
        return comment.id();
    }
}
