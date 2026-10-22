package com.campusoverflow.reputation.application;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.reputation.domain.BadgeAward;
import com.campusoverflow.reputation.domain.BadgeRepository;
import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.reputation.domain.LedgerEntry;
import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReputationQueryService {

    private final ReputationAccountRepository accounts;
    private final LedgerRepository ledger;
    private final BadgeRepository badges;
    private final IdentityApi identity;

    public ReputationQueryService(ReputationAccountRepository accounts, LedgerRepository ledger,
                                  BadgeRepository badges, IdentityApi identity) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.badges = badges;
        this.identity = identity;
    }

    public ReputationProfileView profile(long userId, Actor viewerOrNull) {
        UserSummary user = identity.findUser(userId)
                .orElseThrow(() -> new NotFoundException("REP-5001", "用户不存在: " + userId));
        ReputationAccount account = accounts.findByUserId(userId)
                .orElseGet(() -> ReputationAccount.open(userId, 0));
        boolean isSelf = viewerOrNull != null && viewerOrNull.userId() == userId;
        boolean isAdmin = viewerOrNull != null && viewerOrNull.role() == Role.ADMIN;
        Integer availablePoints = (isSelf || isAdmin) ? account.availablePoints() : null;
        Integer frozenPoints = (isSelf || isAdmin) ? account.frozenPoints() : null;
        List<ReputationProfileView.BadgeView> badgeViews = badges.findByUser(userId).stream()
                .map(this::toBadgeView).toList();
        return new ReputationProfileView(userId, user.displayName(), account.reputation(),
                availablePoints, frozenPoints, badgeViews);
    }

    /** 声誉流水属于个人数据：仅本人与管理员可查看。 */
    public PageResult<LedgerEntryView> ledger(long userId, Actor viewer, PageRequest page) {
        if (viewer.userId() != userId && viewer.role() != Role.ADMIN) {
            throw new ForbiddenException("REP-5002", "无权查看他人的声誉流水");
        }
        PageResult<LedgerEntry> entries = ledger.findByUser(userId, page);
        List<LedgerEntryView> views = entries.items().stream().map(this::toLedgerView).toList();
        return new PageResult<>(views, entries.total(), entries.page(), entries.size());
    }

    private ReputationProfileView.BadgeView toBadgeView(BadgeAward award) {
        BadgeType type = award.type();
        return new ReputationProfileView.BadgeView(type.name(), type.displayName(), type.description(),
                award.awardedAt());
    }

    private LedgerEntryView toLedgerView(LedgerEntry entry) {
        return new LedgerEntryView(entry.id(), entry.reason().name(), entry.reason().label(),
                entry.delta(), entry.refType(), entry.refId(), entry.createdAt());
    }
}
