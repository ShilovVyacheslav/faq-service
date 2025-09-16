package io.knowledgebase.demo.repository;

import io.knowledgebase.demo.document.FaqDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqDocRepository extends MongoRepository<FaqDoc, Long> {}
