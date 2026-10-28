package com.campusoverflow.qa.application.command;

import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.AnswerRepository;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Vote;
import com.campusoverflow.qa.domain.VoteRepository;
import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用例：投票 / 改投 / 撤销（value ∈ {-1, 0, 1}，0 表示撤销）。
 * 发布的事件同时携带旧值与新值，下游据此计算声誉增量。
 */
@Service
public class VoteService {

    private final VoteRepository votes;
    private final QuestionRepository questions;
    private final AnswerRepository answers;
    private final IntegrationEventPublisher events;
    private final Clock clock;

    public VoteService(VoteRepository votes, QuestionRepository questions, AnswerRepository answers,
                       IntegrationEventPublisher events, Clock clock) {
        this.votes = votes;
        this.questions = questions;
        this.answers = answers;
        this.events = events;
        this.clock = clock;
    }

    public record VoteResult(int myVote, int scoreDelta) {
    }

    private record Target(long authorId, long questionId) {
    }

    @Transactional
    public VoteResult cast(Actor actor, TargetType type, long targetId, int value) {
        if (value != -1 && value != 0 && value != 1) {
            throw new BusinessRuleException("QA-1014", "投票值只能是 -1、0 或 1");
        }
        Target target = resolve(type, targetId);
        if (actor.userId() == target.authorId) {
            throw new BusinessRuleException("QA-1015", "不能给自己的内容投票");
        }

        Optional<Vote> existing = votes.find(actor.userId(), type, targetId);
        int oldValue = 0;
        int newValue = value;

        if (existing.isPresent()) {
            Vote vote = existing.get();
            oldValue = vote.value();
            if (newValue == 0) {
                votes.delete(vote);
            } else if (newValue == oldValue) {
                // 幂等：重复投相同值，不做任何变更
                return new VoteResult(oldValue, 0);
            } else {
                vote.change(newValue, clock.instant());
                votes.save(vote);
            }
        } else {
            if (newValue != 0) {
                Vote vote = Vote.cast(actor.userId(), target.authorId, type, targetId, newValue, clock.instant());
                votes.save(vote);
            }
        }

        int scoreDelta = newValue - oldValue;
        if (scoreDelta != 0) {
            if (type == TargetType.QUESTION) {
                questions.adjustScore(targetId, scoreDelta);
            } else {
                answers.adjustScore(targetId, scoreDelta);
            }
        }

        events.publish(new VoteCastEvent(EventIds.next(), clock.instant(), type.name(), targetId,
                target.authorId, actor.userId(), target.questionId, oldValue, newValue));

        return new VoteResult(newValue, scoreDelta);
    }

    private Target resolve(TargetType type, long targetId) {
        if (type == TargetType.QUESTION) {
            Question q = questions.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("QA-2103", "问题不存在: " + targetId));
            return new Target(q.authorId(), q.id());
        } else {
            Answer a = answers.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("QA-2104", "回答不存在: " + targetId));
            return new Target(a.authorId(), a.questionId());
        }
    }
}
