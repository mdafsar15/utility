package com.ndmc.ndmc_record;

import com.ndmc.ndmc_record.config.Constants;
import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.blockchainGatway.ClientAppGovt;
import com.ndmc.ndmc_record.blockchainGatway.EnrollAdminGovt;
import com.ndmc.ndmc_record.blockchainGatway.RegisterUserGovt;
import com.ndmc.ndmc_record.property.FileStorageProperties;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties({ FileStorageProperties.class })
public class    NdmcRecordApplication {

	@Autowired
	BlockchainGatway blockchainGatway;

	public static void main(String[] args) {
		SpringApplication.run(NdmcRecordApplication.class, args);

		try {
			System.out.println("setting EnrollAdminGovt.main..."+   Constants.CONTRACT_NAME);
			EnrollAdminGovt.main(args);
			
			RegisterUserGovt.main(args);
			ClientAppGovt.main(args);


		} catch (Exception e) {	
			e.printStackTrace();
		}
		// UserRoleModel admin = new UserRoleModel("ADMIN");
		// UserRoleModel creator = new UserRoleModel("CREATOR");
		// UserRoleModel approver = new UserRoleModel("APPROVER");

		// use.saveAll(List.of(admin, creator, approver));
		// userRoleRepository.saveAll(List.of(admin, creator,approver));

	}



}
