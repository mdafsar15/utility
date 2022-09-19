package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class TestDto {

    private Long id;
    private String name;
//    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime createdAt;

    public TestDto() {
    }

    public TestDto(Long id, String name) {
        this.id = id;
        this.name = name;
        //this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }

    @Override
    public String toString() {
        return "TestDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
               // ", createdAt=" + createdAt +
                '}';
    }
}
