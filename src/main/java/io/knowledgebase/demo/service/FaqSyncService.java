package io.knowledgebase.demo.service;

import io.knowledgebase.demo.entity.Faq;

import java.util.List;

public interface FaqSyncService {

    void syncFaqToMongo(Faq faq);

    List<Faq> findUnsyncedFaqs();

    void syncAllUnsyncedFaqs();

}
