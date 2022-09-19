package com.ndmc.ndmc_record.model;

import lombok.ToString;

import javax.persistence.*;

@Entity
@ToString
@Table(
        name="application_number_counter",
        uniqueConstraints={
        @UniqueConstraint(columnNames={"org_code", "year", "registration_type"}, name = "UKAppCounterOrgYearWise")}
)
public class ApplicationNumberCounter {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    long id;

    @Column(name = "org_code")
    private String orgCode;
    private Long orgId;
    private int year;
    @Column(name = "registration_type")
    private String registrationType;

    int count;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // orgId
    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
