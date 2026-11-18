package com.rica.cqrs.modelo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaRepository extends MongoRepository<FichaInvestigador, String> {
}
