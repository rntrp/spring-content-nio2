package com.github.rntrp.springcontent.nio2.spring;

import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.annotations.ContentLength;

import javax.persistence.*;

@Entity
@Table(name = "test_entity_multiple_content")
public class TestEntityMultiContent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @ContentId
    private Integer content1;

    @ContentLength
    private long content1Length;

    @ContentId
    private Integer content2;

    @ContentLength
    private long content2Length;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getContent1() {
        return content1;
    }

    public void setContent1(Integer content1) {
        this.content1 = content1;
    }

    public long getContent1Length() {
        return content1Length;
    }

    public void setContent1Length(long content1Length) {
        this.content1Length = content1Length;
    }

    public Integer getContent2() {
        return content2;
    }

    public void setContent2(Integer content2) {
        this.content2 = content2;
    }

    public long getContent2Length() {
        return content2Length;
    }

    public void setContent2Length(long content2Length) {
        this.content2Length = content2Length;
    }
}
