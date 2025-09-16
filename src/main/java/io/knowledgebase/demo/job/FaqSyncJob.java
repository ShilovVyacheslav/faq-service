package io.knowledgebase.demo.job;

import io.knowledgebase.demo.service.FaqSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "scheduler.faq-sync.enabled", havingValue = "true")
public class FaqSyncJob {

    private final FaqSyncService faqSyncService;

    @Scheduled(cron = "${scheduler.faq-sync.cron}")
    @SchedulerLock(
            name = "faqSyncJob",
            lockAtLeastFor = "${scheduler.faq-sync.lockAtLeastFor}",
            lockAtMostFor = "${scheduler.faq-sync.lockAtMostFor}"
    )
    public void syncFaqsJob() {
        try {
            faqSyncService.syncAllUnsyncedFaqs();
        } catch (Exception e) {
            log.error("FAQ sync job failed", e);
        }
    }

}
