package com.campusoverflow.discovery.application;

import com.campusoverflow.discovery.domain.notification.MentionParser;
import com.campusoverflow.discovery.domain.notification.Notification;
import com.campusoverflow.discovery.domain.notification.NotificationRepository;
import com.campusoverflow.discovery.domain.notification.NotificationType;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.api.QaQueryApi;
import com.campusoverflow.qa.api.QuestionRef;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerEndorsedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.CommentPostedEvent;
import com.campusoverflow.reputation.api.event.BadgeAwardedEvent;
import com.campusoverflow.reputation.api.event.BountyAwardedEvent;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 通知中心：把上游事件转换为站内通知。幂等键 (sourceEventId, recipientId)。 */
@Component
public class NotificationProjector {

    private final NotificationRepository notifications;
    private final IdentityApi identity;
    private final QaQueryApi qa;

    public NotificationProjector(NotificationRepository notifications, IdentityApi identity, QaQueryApi qa) {
        this.notifications = notifications;
        this.identity = identity;
        this.qa = qa;
    }

    @EventListener
    @Transactional
    public void on(AnswerSubmittedEvent e) {
        if (e.answerAuthorId() == e.questionAuthorId()) {
            return;
        }
        String title = name(e.answerAuthorId()) + " 回答了你的问题：" + e.questionTitle();
        notify(e.questionAuthorId(), NotificationType.ANSWER_RECEIVED, title,
                "/questions/" + e.questionId(), e.eventId(), e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(AnswerAcceptedEvent e) {
        if (e.answerAuthorId() == e.questionAuthorId()) {
            return;
        }
        String title = "你的回答被采纳：" + e.questionTitle();
        notify(e.answerAuthorId(), NotificationType.ANSWER_ACCEPTED, title,
                "/questions/" + e.questionId(), e.eventId(), e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(AnswerEndorsedEvent e) {
        String title = name(e.endorserId()) + " 认证了你的回答：" + e.questionTitle();
        notify(e.answerAuthorId(), NotificationType.ANSWER_ENDORSED, title,
                "/questions/" + e.questionId(), e.eventId(), e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(CommentPostedEvent e) {
        Set<String> mentionedNames = MentionParser.extract(e.body());
        if (mentionedNames.isEmpty()) {
            return;
        }
        for (String name : mentionedNames) {
            identity.findByDisplayName(name).ifPresent(user -> {
                if (user.id() != e.authorId()) {
                    String title = name(e.authorId()) + " 在评论中提到了你";
                    notify(user.id(), NotificationType.MENTIONED, title,
                            "/questions/" + e.questionId(), e.eventId() + ":" + user.id(), e.occurredAt());
                }
            });
        }
    }

    @EventListener
    @Transactional
    public void on(BountyAwardedEvent e) {
        String title = "你赢得了 " + e.points() + " 积分悬赏";
        notify(e.winnerId(), NotificationType.BOUNTY_AWARDED, title,
                "/questions/" + e.questionId(), e.eventId(), e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(BadgeAwardedEvent e) {
        String title = "恭喜获得徽章：" + e.badgeType();
        notify(e.userId(), NotificationType.BADGE_AWARDED, title,
                "/profile/" + e.userId(), e.eventId(), e.occurredAt());
    }

    private void notify(long recipientId, NotificationType type, String title, String link, String eventId,
                        java.time.Instant at) {
        if (notifications.exists(eventId, recipientId)) {
            return;
        }
        notifications.save(Notification.create(recipientId, type, title, link, eventId, at));
    }

    private String name(long userId) {
        return identity.findUser(userId).map(UserSummary::displayName).orElse("匿名用户");
    }
}
