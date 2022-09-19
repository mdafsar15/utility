/*
SPDX-License-Identifier: Apache-2.0
*/

package com.ndmc.ndmc_record.blockchainGatway;

import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Properties;
import java.util.Set;

import com.ndmc.ndmc_record.config.Constants;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

public class RegisterUserGovt {

	static {
		System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "false");
	}

	public static void main(String[] args) throws Exception {

		String BASE_PATH = Constants.BASE_PATH;
		// Create a CA client for interacting with the CA.
		Properties props = new Properties();
		props.put("pemFile", BASE_PATH + "tls-ca-certs/govt/tls-ca-govt-ndmc-com-7054.pem");
		props.put("allowAllHostNames", "true");
		HFCAClient caClient = HFCAClient.createNewInstance("https://ca.govt.ndmc.com:7054", props);
		CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
		caClient.setCryptoSuite(cryptoSuite);

		// Create a wallet for managing identities
		Wallet wallet = Wallets.newFileSystemWallet(Paths.get(BASE_PATH + "wallet/govt"));

		// Check to see if we've already enrolled the user.
		if (wallet.get(Constants.NDMC_GOVT_USER) != null) {
			System.out.println("An identity for the user \""+ Constants.NDMC_GOVT_USER +"\" already exists in the wallet");
			return;
		}

		X509Identity adminIdentity = (X509Identity)wallet.get("admin");
		if (adminIdentity == null) {
			System.out.println("\"admin\" needs to be enrolled and added to the wallet first");
			return;
		}
		User admin = new User() {

			@Override
			public String getName() {
				return "admin";
			}

			@Override
			public Set<String> getRoles() {
				return null;
			}

			@Override
			public String getAccount() {
				return null;
			}

			@Override
			public String getAffiliation() {
				return "org1.department1";
			}

			@Override
			public Enrollment getEnrollment() {
				return new Enrollment() {

					@Override
					public PrivateKey getKey() {
						return adminIdentity.getPrivateKey();
					}

					@Override
					public String getCert() {
						return Identities.toPemString(adminIdentity.getCertificate());
					}
				};
			}

			@Override
			public String getMspId() {
				return "GOVTMSP";
			}

		};

		// Register the user, enroll the user, and import the new identity into the wallet.
		RegistrationRequest registrationRequest = new RegistrationRequest(Constants.NDMC_GOVT_USER);
		registrationRequest.setAffiliation("org1.department1");
		registrationRequest.setEnrollmentID(Constants.NDMC_GOVT_USER);
		String enrollmentSecret = caClient.register(registrationRequest, admin);
		Enrollment enrollment = caClient.enroll(Constants.NDMC_GOVT_USER, enrollmentSecret);
		Identity user = Identities.newX509Identity("GOVTMSP", adminIdentity.getCertificate(), adminIdentity.getPrivateKey());
		wallet.put(Constants.NDMC_GOVT_USER, user);
		System.out.println("Successfully enrolled user \""+Constants.NDMC_GOVT_USER+"\" and imported it into the wallet");
	}

}

