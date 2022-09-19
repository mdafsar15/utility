package com.ndmc.ndmc_record.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@ToString
@Getter
@Setter
@Table(name = "blockchain_preush_summary")
public class BlockchainRePushSummary implements Serializable {
    @Id
    private String id;
    private Long idTable;
    private String tableName;
    private String blcStatus;
    private String blcAction;

}