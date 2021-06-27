package com.github.rntrp.springcontent.nio2.spring;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {TestEntityRepo.class, TestContentStore.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TestSpringIntegration {
    @Autowired
    TestEntityRepo testEntityRepo;
    @Autowired
    TestContentStore testContentStore;

    @BeforeEach
    void cleanStore() throws IOException {
        testContentStore.clean();
    }

    @Test
    void testDataJpaBasics() {
        TestEntity e1 = new TestEntity();
        e1.setField("field1");
        testEntityRepo.save(e1);
        int id1 = e1.getId();
        assertEquals(1, id1);
        assertEquals("field1", testEntityRepo.findById(id1).map(TestEntity::getField).orElse(null));

        TestEntity e2 = new TestEntity();
        e2.setField("field2");
        testEntityRepo.save(e2);
        int id2 = e2.getId();
        assertEquals(2, id2);
        assertEquals("field2", testEntityRepo.findById(id2).map(TestEntity::getField).orElse(null));
    }

    @Test
    void testContentStore() throws IOException {
        TestEntity e = new TestEntity();
        e.setField("field1");
        e.setContent(42);
        try (InputStream in = new ByteArrayInputStream("foo".getBytes(UTF_8))) {
            testContentStore.setContent(e, in);
        }
        testEntityRepo.save(e);

        assertEquals(1, e.getId());
        assertEquals("field1", testEntityRepo.findById(e.getId()).map(TestEntity::getField).orElse(null));

        try (InputStream in = testContentStore.getContent(e)) {
            assertEquals("foo", IOUtils.toString(in, UTF_8));
        }
    }
}
