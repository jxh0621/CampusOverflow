package com.campusoverflow.reputation.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusoverflow.identity.api.event.UserRegisteredEvent;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.reputation.domain.BadgeAward;
import com.campusoverflow.reputation.domain.BadgeRepository;
import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.reputation.domain.LedgerEntry;
import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.reputation.domain.ReputationReason;
import com.campusoverflow.reputation.domain.ReputationRulebook;
import com.campusoverflow.shared.event.IntegrationEvent;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Role;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Reputation 应用层测试：重点验证事件幂等重放——同一事件投递两次不得重复记账、重复开户、重复授予徽章。
 */
class ReputationEventHandlersTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    // 内存替身
    private final InMemoryAccounts accounts = new InMemoryAccounts();
    private final InMemoryLedger ledger = new InMemoryLedger();
    private final InMemoryBadges badges = new InMemoryBadges();
    private final RecordingEvents events = new RecordingEvents();

    private ReputationLedgerService ledgerService;
    private BadgeService badgeService;
    private ReputationEventHandlers handlers;

    @BeforeEach
    void setUp() {
        badgeService = new BadgeService(badges, events, clock);
        ledgerService = new ReputationLedgerService(accounts, ledger, badgeService, events, clock);
        // 用空的 BountyRepository，settleOnAccept 时 findOpenByQuestion 返回 empty 直接跳过
        InMemoryBountyRepository bountyRepo = new InMemoryBountyRepository();
        BountyService bountyService = new BountyService(bountyRepo, accounts, ledgerService, null, null, events, null, clock);
        handlers = new ReputationEventHandlers(accounts, ledger, ledgerService, badgeService, bountyService, clock);
    }

    @Test
    @DisplayName("用户注册：开户幂等，重复事件不重复创建账户")
    void userRegisteredIdempotent() {
        UserRegisteredEvent e = new UserRegisteredEvent("evt-1", NOW, 1L, "小明", Role.STUDENT);
        handlers.on(e);
        handlers.on(e); // 重放

        assertThat(accounts.findByUserId(1L)).isPresent();
        assertThat(accounts.findByUserId(1L).get().availablePoints())
                .isEqualTo(ReputationRulebook.initialPoints(Role.STUDENT));
        assertThat(ledger.findByUser(2L, new PageRequest(1, 50)).items()).isEmpty(); // 开户不写流水
    }

    @Test
    @DisplayName("回答被采纳：重复投递不重复+15声誉")
    void answerAcceptedIdempotent() {
        // 先给回答者开户
        accounts.save(ReputationAccount.open(2L, 0));

        AnswerAcceptedEvent e = new AnswerAcceptedEvent("evt-accept-1", NOW, 10L, 100L,
                2L, 1L, 1L, "红黑树为什么旋转");
        handlers.on(e);
        handlers.on(e); // 重放

        // 只记了一次账
        assertThat(ledger.findByUser(2L, new PageRequest(1, 50)).items()).hasSize(1);
        ReputationAccount acc = accounts.findByUserId(2L).orElseThrow();
        assertThat(acc.reputation()).isEqualTo(15); // 采纳+15，只加一次
    }

    @Test
    @DisplayName("投票事件：重复投递不重复计分")
    void voteCastIdempotent() {
        accounts.save(ReputationAccount.open(2L, 0));

        VoteCastEvent e = new VoteCastEvent("evt-vote-1", NOW, "ANSWER", 10L,
                2L, 1L, 100L, 0, 1); // 从0到+1
        handlers.on(e);
        handlers.on(e); // 重放

        assertThat(ledger.findByUser(2L, new PageRequest(1, 50)).items()).hasSize(1);
        ReputationAccount acc = accounts.findByUserId(2L).orElseThrow();
        assertThat(acc.reputation()).isEqualTo(10); // 赞+10，只加一次
    }

    @Test
    @DisplayName("回答提交：授予 FIRST_ANSWER 徽章，重复提交不重复授予")
    void firstAnswerBadgeIdempotent() {
        AnswerSubmittedEvent e = new AnswerSubmittedEvent("evt-ans-1", NOW, 10L, 100L,
                2L, 1L, "红黑树为什么旋转");
        handlers.on(e);
        handlers.on(e); // 重放

        assertThat(badges.has(2L, BadgeType.FIRST_ANSWER)).isTrue();
        assertThat(badges.findByUser(2L)).hasSize(1);
    }

    @Test
    @DisplayName("每日上限：当日正向投票声誉超过200的部分不计")
    void dailyCap() {
        accounts.save(ReputationAccount.open(2L, 0));
        // 先手动记一笔 200 的正向投票
        ledgerService.record(2L, "evt-prior", ReputationReason.ANSWER_VOTE, 200, "ANSWER", 99L);

        VoteCastEvent e = new VoteCastEvent("evt-vote-2", NOW, "ANSWER", 10L,
                2L, 1L, 100L, 0, 1); // 再投+10
        handlers.on(e);

        // 已用200，上限200，新增10被截断为0
        ReputationAccount acc = accounts.findByUserId(2L).orElseThrow();
        assertThat(acc.reputation()).isEqualTo(200); // 不增加
    }

    // ============ 内存替身 ============

    static class InMemoryAccounts implements ReputationAccountRepository {
        private final Map<Long, ReputationAccount> store = new HashMap<>();

        @Override
        public Optional<ReputationAccount> findByUserId(long userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public List<ReputationAccount> findAllByUserId(Collection<Long> userIds) {
            return userIds.stream().map(store::get).filter(a -> a != null).toList();
        }

        @Override
        public List<ReputationAccount> topByReputation(int limit) {
            return store.values().stream()
                    .sorted((a, b) -> Integer.compare(b.reputation(), a.reputation()))
                    .limit(limit).toList();
        }

        @Override
        public List<ReputationAccount> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public ReputationAccount save(ReputationAccount account) {
            store.put(account.userId(), account);
            return account;
        }
    }

    static class InMemoryLedger implements LedgerRepository {
        private final List<LedgerEntry> entries = new ArrayList<>();

        @Override
        public boolean exists(String eventId, long userId, ReputationReason reason) {
            return entries.stream().anyMatch(e -> e.eventId().equals(eventId)
                    && e.userId() == userId && e.reason() == reason);
        }

        @Override
        public void append(LedgerEntry entry) {
            entries.add(entry);
        }

        @Override
        public PageResult<LedgerEntry> findByUser(long userId, PageRequest page) {
            List<LedgerEntry> list = entries.stream().filter(e -> e.userId() == userId).toList();
            return new PageResult<>(list, list.size(), page.page(), page.size());
        }

        @Override
        public long countByUserAndReason(long userId, ReputationReason reason) {
            return entries.stream().filter(e -> e.userId() == userId && e.reason() == reason).count();
        }

        @Override
        public int sumPositiveVoteDeltaSince(long userId, Instant since) {
            return entries.stream()
                    .filter(e -> e.userId() == userId && e.delta() > 0 && e.reason().isVote())
                    .mapToInt(LedgerEntry::delta).sum();
        }

        @Override
        public Map<Long, Long> sumByUser() {
            Map<Long, Long> result = new HashMap<>();
            entries.forEach(e -> result.merge(e.userId(), (long) e.delta(), Long::sum));
            return result;
        }
    }

    static class InMemoryBadges implements BadgeRepository {
        private final Set<String> awarded = new HashSet<>();
        private final List<BadgeAward> all = new ArrayList<>();

        @Override
        public boolean has(long userId, BadgeType type) {
            return awarded.contains(userId + ":" + type);
        }

        @Override
        public void award(BadgeAward award) {
            awarded.add(award.userId() + ":" + award.type());
            all.add(award);
        }

        @Override
        public List<BadgeAward> findByUser(long userId) {
            return all.stream().filter(a -> a.userId() == userId).toList();
        }
    }

    static class InMemoryBountyRepository implements com.campusoverflow.reputation.domain.BountyRepository {
        private final List<com.campusoverflow.reputation.domain.Bounty> store = new ArrayList<>();

        @Override
        public Optional<com.campusoverflow.reputation.domain.Bounty> findOpenByQuestion(long questionId) {
            return Optional.empty(); // 测试中无悬赏
        }

        @Override
        public Optional<com.campusoverflow.reputation.domain.Bounty> findLatestByQuestion(long questionId) {
            return store.stream().filter(b -> b.questionId() == questionId).findFirst();
        }

        @Override
        public List<com.campusoverflow.reputation.domain.Bounty> findDue(Instant now, int limit) {
            return List.of();
        }

        @Override
        public com.campusoverflow.reputation.domain.Bounty save(com.campusoverflow.reputation.domain.Bounty bounty) {
            store.add(bounty);
            return bounty;
        }
    }

    static class RecordingEvents implements IntegrationEventPublisher {
        final List<IntegrationEvent> published = new ArrayList<>();

        @Override
        public void publish(IntegrationEvent event) {
            published.add(event);
        }

        @SuppressWarnings("unchecked")
        <T extends IntegrationEvent> List<T> ofType(Class<T> type) {
            return (List<T>) (List<?>) published.stream().filter(type::isInstance).toList();
        }
    }
}
