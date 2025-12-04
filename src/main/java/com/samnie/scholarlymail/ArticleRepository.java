package com.samnie.scholarlymail;

import org.springframework.stereotype.Repository;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

@Repository
public interface ArticleRepository extends CouchbaseRepository<Article, String> {}
