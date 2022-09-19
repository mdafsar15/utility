package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.config.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import org.springframework.boot.autoconfigure.web.WebProperties;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity()
@Table(name = "online_appointments")
public class OnlineAppointment {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "apt_id")
    private Long aptId;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime appointmentDateTime;

    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    private String createdBy;

    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime modifiedAt;

    private String modifiedBy;
    private Long slaId;
    private String status; // OPEN | CLOSED
}
