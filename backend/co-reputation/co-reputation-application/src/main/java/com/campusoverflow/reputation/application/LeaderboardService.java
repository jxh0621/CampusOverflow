package com.campusoverflow.reputation.application;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.reputation.api.event.ReputationChangedEvent;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.shared.event.ProcessedEventStore;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 排行榜：周/月/学期榜来自 Redis ZSET，总榜直接读账户表。 */
@Service
public class LeaderboardService {

    private final LeaderboardStore store;
    private final ReputationAccountRepository accounts;
    private final IdentityApi identity;
    private final ProcessedEventStore processed;
    private final Clock clock;

    public LeaderboardService(LeaderboardStore store, ReputationAccountRepository accounts, IdentityApi identity,
                              ProcessedEventStore processed, Clock clock) {
        this.store = store;
        this.accounts = accounts;
        this.identity = identity;
        this.processed = processed;
        this.clock = clock;
    }

    @EventListener
    @Transactional
    public void on(ReputationChangedEvent e) {
        if (!processed.markProcessed(e.eventId(), "leaderboard")) {
            return;
        }
        store.increment(e.userId(), e.delta(), e.occurredAt());
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryView> top(LeaderboardPeriod period, int limit) {
        if (period == LeaderboardPeriod.ALL) {
            List<ReputationAccount> topAccounts = accounts.topByReputation(limit);
            List<LeaderboardEntryView> result = new ArrayList<>();
            int rank = 1;
            for (ReputationAccount account : topAccounts) {
                String name = identity.findUser(account.userId())
                        .map(UserSummary::displayName).orElse("未知用户");
                boolean verified = identity.findUser(account.userId())
                        .map(UserSummary::verified).orElse(false);
                result.add(new LeaderboardEntryView(rank++, account.userId(), name, verified,
                        account.reputation()));
            }
            return result;
        }
        List<LeaderboardStore.Entry> entries = store.top(period, clock.instant(), limit);
        List<LeaderboardEntryView> result = new ArrayList<>();
        int rank = 1;
        for (LeaderboardStore.Entry entry : entries) {
            String name = identity.findUser(entry.userId())
                    .map(UserSummary::displayName).orElse("未知用户");
            boolean verified = identity.findUser(entry.userId())
                    .map(UserSummary::verified).orElse(false);
            result.add(new LeaderboardEntryView(rank++, entry.userId(), name, verified, entry.score()));
        }
        return result;
    }
}
