package com.campusoverflow.discovery.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusoverflow.discovery.domain.notification.Notification;
import com.campusoverflow.discovery.domain.notification.NotificationRepository;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.api.QaQueryApi;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Role;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** NotificationProjector 幂等测试：同一事件重复投递不产生重复通知。 */
class NotificationProjectorTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private InMemoryNotifications notifications;
    private NotificationProjector projector;

    @BeforeEach
    void setUp() {
        notifications = new InMemoryNotifications();
        IdentityApi identity = new StubIdentity();
        QaQueryApi qa = new StubQa();
        projector = new NotificationProjector(notifications, identity, qa);
    }

    @Test
    @DisplayName("回答提交：通知问题作者，自己回答自己不通知，重复投递不重复")
    void answerSubmittedIdempotent() {
        // 小红(2)回答小明(1)的问题
        AnswerSubmittedEvent e = new AnswerSubmittedEvent("evt-ans-1", NOW, 10L, 100L, 2L, 1L, "红黑树为什么旋转");
        projector.on(e);
        projector.on(e); // 重放

        List<Notification> toAuthor = notifications.findByRecipient(1L, new PageRequest(1, 50)).items();
        assertThat(toAuthor).hasSize(1); // 只通知一次
        assertThat(toAuthor.get(0).title()).contains("回答了你的问题");

        // 自己回答自己（answerAuthorId == questionAuthorId）→ 不通知
        AnswerSubmittedEvent self = new AnswerSubmittedEvent("evt-self", NOW, 11L, 100L, 1L, 1L, "自答");
        projector.on(self);
        assertThat(notifications.findByRecipient(1L, new PageRequest(1, 50)).items()).hasSize(1);
    }

    @Test
    @DisplayName("回答被采纳：通知回答者，重复投递不重复")
    void answerAcceptedIdempotent() {
        AnswerAcceptedEvent e = new AnswerAcceptedEvent("evt-accept-1", NOW, 10L, 100L, 2L, 1L, 1L, "红黑树");
        projector.on(e);
        projector.on(e); // 重放

        List<Notification> toAnswerer = notifications.findByRecipient(2L, new PageRequest(1, 50)).items();
        assertThat(toAnswerer).hasSize(1);
        assertThat(toAnswerer.get(0).title()).contains("被采纳");
    }

    // 内存替身

    static class InMemoryNotifications implements NotificationRepository {
        private final List<Notification> store = new ArrayList<>();
        private final Set<String> keys = new HashSet<>();

        @Override
        public boolean exists(String sourceEventId, long recipientId) {
            return keys.contains(sourceEventId + ":" + recipientId);
        }

        @Override
        public Notification save(Notification notification) {
            keys.add(notification.sourceEventId() + ":" + notification.recipientId());
            store.add(notification);
            return notification;
        }

        @Override
        public Optional<Notification> findById(long id) {
            return store.stream().filter(n -> n.id() != null && n.id() == id).findFirst();
        }

        @Override
        public PageResult<Notification> findByRecipient(long recipientId, PageRequest page) {
            List<Notification> list = store.stream().filter(n -> n.recipientId() == recipientId).toList();
            return new PageResult<>(list, list.size(), page.page(), page.size());
        }

        @Override
        public long countUnread(long recipientId) {
            return store.stream().filter(n -> n.recipientId() == recipientId && !n.read()).count();
        }

        @Override
        public int markAllRead(long recipientId) {
            return 0;
        }
    }

    static class StubIdentity implements IdentityApi {
        private final Map<Long, UserSummary> users = new HashMap<>();

        StubIdentity() {
            users.put(1L, new UserSummary(1L, "小明", Role.STUDENT, true, "计算机学院"));
            users.put(2L, new UserSummary(2L, "小红", Role.STUDENT, true, "计算机学院"));
        }

        @Override
        public Optional<UserSummary> findUser(long userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public Map<Long, UserSummary> findUsers(java.util.Collection<Long> userIds) {
            return new HashMap<>();
        }

        @Override
        public Optional<UserSummary> findByDisplayName(String displayName) {
            return Optional.empty();
        }

        @Override
        public Optional<com.campusoverflow.identity.api.CourseSummary> findCourse(long courseId) {
            return Optional.empty();
        }

        @Override
        public boolean canManageCourse(long userId, Long courseId) {
            return false;
        }
    }

    static class StubQa implements QaQueryApi {
        @Override
        public Optional<com.campusoverflow.qa.api.QuestionRef> findQuestion(long questionId) {
            return Optional.empty();
        }
    }
}
