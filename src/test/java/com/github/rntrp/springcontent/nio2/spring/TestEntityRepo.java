package com.github.rntrp.springcontent.nio2.spring;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestEntityRepo extends CrudRepository<TestEntity, Integer> {
}
