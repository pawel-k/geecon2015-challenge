package pl.allegro.promo.geecon2015.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import pl.allegro.promo.geecon2015.domain.stats.FinancialStatisticsRepository;
import pl.allegro.promo.geecon2015.domain.stats.FinancialStats;
import pl.allegro.promo.geecon2015.domain.transaction.TransactionRepository;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransaction;
import pl.allegro.promo.geecon2015.domain.user.UserRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ReportGenerator {

    private final FinancialStatisticsRepository financialStatisticsRepository;

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    @Autowired
    public ReportGenerator(FinancialStatisticsRepository financialStatisticsRepository,
                           UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.financialStatisticsRepository = financialStatisticsRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public Report generate(ReportRequest request) {
        Report report = new Report();
        FinancialStats financialStats = financialStatisticsRepository.listUsersWithMinimalIncome(request.getMinimalIncome(), request.getUsersToCheck());
        financialStats.forEach(userId -> report.add(new ReportedUser(userId,
                getName(userId),
                getTransactionsAmount(userId))));
        return report;
    }

    private BigDecimal getTransactionsAmount(UUID userId) {
        try {
            return transactionRepository.transactionsOf(userId).getTransactions()
                    .stream()
                    .map(UserTransaction::getAmount)
                    .reduce(BigDecimal::add)
                    .get();
        } catch (HttpServerErrorException e) {
            return null;
        }
    }

    private String getName(UUID userId) {
        try {
            return userRepository.detailsOf(userId).getName();
        } catch (HttpServerErrorException e) {
            return "<failed>";
        }
    }

}
