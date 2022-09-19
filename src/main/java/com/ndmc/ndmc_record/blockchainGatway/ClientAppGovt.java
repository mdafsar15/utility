/*
SPDX-License-Identifier: Apache-2.0
*/

package com.ndmc.ndmc_record.blockchainGatway;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.serviceImpl.AuthServiceImpl;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAppGovt {

	private static final Logger logger = LoggerFactory.getLogger(ClientAppGovt.class);
	static {
		System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "false");
	}

	public static void main(String[] args) throws Exception {
		// Load a file system based wallet for managing identities.

		String BASE_PATH = Constants.BASE_PATH;
		String CONNECTION_PROFILE = Constants.CONNECTION_PROFILE;
		Path walletPath = Paths.get(BASE_PATH + "wallet/govt");
		Wallet wallet = Wallets.newFileSystemWallet(walletPath);
		// load a CCP
		Path networkConfigPath = Paths.get(BASE_PATH + CONNECTION_PROFILE, "govt-connection.json");

		Gateway.Builder builder = Gateway.createBuilder();
		logger.debug("Constants.NDMC_GOVT_USER == >>>"+Constants.NDMC_GOVT_USER);
		builder.identity(wallet, Constants.NDMC_GOVT_USER).networkConfig(networkConfigPath).discovery(true);

		// create a gateway connection
		try (Gateway gateway = builder.connect()) {

			// get the network and contract
			Network network = gateway.getNetwork("ndmcgovtchannel");
			Contract contract = network.getContract(Constants.CONTRACT_NAME);

			byte[] result;

			//result = contract.submitTransaction("createBirthCertificate","{\"applicationnumber\":\"00100\",\"registrationnumber\":2,\"divisioncode\":1, \"fathername\" :\"abc\", \"mothername\":\"pqr\",\"permanentaddress\": \"new delhi\", \"fatheroccupation\": 1, \"fatherreligion\":3, \"eventplace\":\"Home\", \"gendercode\" : \"M\", \"idndmcresident\" : \"No\", \"informantaddress\": \"new delhi\", \"informantname\": \"aml\"}");
			//System.out.println(new String(result));


			result = contract.evaluateTransaction("queryBirthCertificate","00100");
			System.out.println(new String(result));
//
//			result = contract.submitTransaction("birthCertificateApprove","0014");
//			System.out.println(new String(result));
//
//			result = contract.evaluateTransaction("queryBirthCertificate","0014");
//			System.out.println(new String(result));
//
//			result = contract.submitTransaction("birthCertificateReject","0014");
//			System.out.println(new String(result));

//			result = contract.submitTransaction("createDeathCertificate","{\"applicationnumber\":\"0010\",\"registrationnumber\":2,\"divisioncode\":1, \"mothername\" : \"pqr\", \"fathername\" : \"abc\", \"causeofdeath\":3, \"nationalityofdeceased\":\"Indian\", \"permanentaddress\":\"new delhi\", \"ageofdeceased\":60, \"educationofdeceased\":\"high school\", \"eventdate\":\"16356353\", \"eventplace\":\"Home\", \"genercode\" : \"M\"}");
//			System.out.println(new String(result));
//
//			result = contract.evaluateTransaction("queryDeathCertificate","0014");
//			System.out.println(new String(result));
//
//			result = contract.submitTransaction("deathCertificateReject","0014");
//			System.out.println(new String(result));

//
//			result = contract.submitTransaction("deathCertificateApprove","0014");
//			System.out.println(new String(result));

		}
	}

}
