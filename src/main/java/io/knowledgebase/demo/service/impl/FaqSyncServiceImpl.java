package io.knowledgebase.demo.service.impl;

import io.knowledgebase.demo.entity.Faq;
import io.knowledgebase.demo.repository.FaqRepository;
import io.knowledgebase.demo.service.FaqDocService;
import io.knowledgebase.demo.service.FaqSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class FaqSyncServiceImpl implements FaqSyncService {

    private final FaqDocService faqDocService;
    private final FaqRepository faqRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void syncFaqToMongo(Faq faq) {
        transactionTemplate.execute(status -> {
            try {
                faqDocService.moveFaqToMongo(faq);
                log.info("Successfully sync FAQ ID: {} to MongoDB", faq.getId());
                return faq.getId();
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Sync failed for FAQ ID: " + faq.getId(), e);
            }
        });
    }

    @Override
    public List<Faq> findUnsyncedFaqs() {
        return transactionTemplate.execute(status -> {
            status.isReadOnly();
            return faqRepository.findByInMongoFalse();
        });
    }

    @Override
    public void syncAllUnsyncedFaqs() {
        List<Faq> unsyncedFaqs = findUnsyncedFaqs();
        if (unsyncedFaqs.isEmpty()) {
            return;
        }
        log.info("Found {} unsynced FAQs ", unsyncedFaqs.size());
        int successCount = 0;
        for (Faq faq : unsyncedFaqs) {
            try {
                syncFaqToMongo(faq);
                ++successCount;
            } catch (Exception e) {
                log.error("Failed to sync FAQ ID: {} to MongoDB", faq.getId(), e);
            }
        }
        log.info("Successfully synced {} out of {} FAQs", successCount, unsyncedFaqs.size());
    }

}
