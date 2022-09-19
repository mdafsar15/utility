package com.ndmc.ndmc_record.blockchainGatway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ndmc.ndmc_record.exception.NullPointerException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.domain.RecordDetails;
import com.ndmc.ndmc_record.dto.BirthDto;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.serviceImpl.BirthServiceImpl;
import com.ndmc.ndmc_record.serviceImpl.BlockchainService;
import com.ndmc.ndmc_record.serviceImpl.PdfHash;
//import org.apache.commons.io.FileUtils;
import com.ndmc.ndmc_record.utils.JsonUtil;

import org.hyperledger.fabric.gateway.*;
//import org.json.JSONObject;
import org.json.JSONObject;
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import com.ndmc.ndmc_record.domain.ChildDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BlockchainGatway {

	private final Logger logger = LoggerFactory.getLogger(BlockchainGatway.class);

	static {
		System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
	}

	@Autowired
	BlockchainService blockchainService;

	public String insertRecord(RecordDetails details) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork("mychannel");
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		result = contract.submitTransaction("createRecord", details.getApplicationNumber(), details.getName(),
				details.getDivisionCode(), details.getDateOfEvent(), details.getActivityCode(),
				details.getDateOfRegistration(), details.getPlaceOfEvent(), details.getGender(),
				details.getFatherName(), details.getFatherLiteracy(), details.getFatherOccupation(),
				details.getFatherNationality(), details.getFatherReligion(), details.getMotherName(),
				details.getMotherNationality(), details.getPermanentAddress(), details.getCreatedAt().toString(),
				details.getCreatedBy());

		String s = new String(result);
		System.out.println(s);
		return s;
	}

	public String insertRecord(ChildDetails details) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork("mychannel");
		Contract contract = network.getContract(Constants.CONTRACT_NAME);

		result = contract.submitTransaction("createRecord", details.getRegistrationNumber(), details.getChildName(),
				details.getGender().toString(), details.getDateOfBirth(), details.getDateOfTime(),
				details.getBirthCityName(), details.getBirthCountryName(), details.getChildAadharNo(),
				details.getMotherName(), details.getFatherName(), details.getMotherAadharNo(),
				details.getFatherAadharNo(), details.getGurdianAadharNo(), details.getAddress(), details.getCity(),
				details.getState(), details.getCountry(), details.getPostalCode(), details.getReligion(),
				details.getModifiedBy(), details.getModifiedDate().toString());

		String s = new String(result);
		System.out.println(s);
		return s;
	}

	public String modifyRecord(ChildDetails details) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork("mychannel");
		Contract contract = network.getContract(Constants.CONTRACT_NAME);

		result = contract.submitTransaction("modifyRecord", details.getRegistrationNumber(), details.getChildName(),
				details.getGender().toString(), details.getDateOfBirth(), details.getDateOfTime(),
				details.getBirthCityName(), details.getBirthCountryName(), details.getChildAadharNo(),
				details.getMotherName(), details.getFatherName(), details.getMotherAadharNo(),
				details.getFatherAadharNo(), details.getGurdianAadharNo(), details.getAddress(), details.getCity(),
				details.getState(), details.getCountry(), details.getPostalCode(), details.getReligion(),
				details.getModifiedBy(), details.getModifiedDate().toString());

		String s = new String(result);
		System.out.println(s);
		return s;
	}

	public String modifyRecord(RecordDetails details) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork("mychannel");
		Contract contract = network.getContract(Constants.CONTRACT_NAME);

		result = contract.submitTransaction("modifyRecord", details.getApplicationNumber(), details.getName(),
				details.getDivisionCode(), details.getDateOfEvent(), details.getActivityCode(),
				details.getDateOfRegistration(), details.getPlaceOfEvent(), details.getGender(),
				details.getFatherName(), details.getFatherLiteracy(), details.getFatherOccupation(),
				details.getFatherNationality(), details.getFatherReligion(), details.getMotherName(),
				details.getMotherNationality(), details.getPermanentAddress(), details.getModifiedBy(),
				details.getModifiedAt().toString());

		String s = new String(result);
		System.out.println(s);
		return s;
	}

	public String approveRecord(String registrationNumber, String filePath) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork("mychannel");
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		PdfHash pdfHash = new PdfHash();
		MessageDigest md = MessageDigest.getInstance("SHA-256"); // SHA, MD2, MD5, SHA-256, SHA-384...
		String dochash = pdfHash.checksum(filePath, md);
		result = contract.submitTransaction("approveRecord", registrationNumber, dochash);

		String s = new String(result);
		System.out.println(s);
		return s;
	}

	public String queryRecordByRegNumber(String registrationNumber) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork("mychannel");
		Contract contract = network.getContract(Constants.CONTRACT_NAME);

		result = contract.evaluateTransaction("queryRecordByRegNumber", registrationNumber);

		String s = new String(result);
		System.out.println(s);
		return s;
	}

	public String queryAllRecords() throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork("mychannel");
		Contract contract = network.getContract(Constants.CONTRACT_NAME);

		result = contract.evaluateTransaction("queryAllRecords");

		String s = new String(result);
		System.out.println(s);
		return s;
	}

	public String getRecordHistory(String registrationNumber) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork("mychannel");
		Contract contract = network.getContract(Constants.CONTRACT_NAME);

		result = contract.evaluateTransaction("getRecordHistory", registrationNumber);

		String s = new String(result);
		System.out.println(s);
		return s;
	}

	// Birth record addition

	public BlockchainBirthResponse insertBirthRecord(BirthModel details, String privateHospitalChannel)
			throws Exception {

		String jsonPayload = JsonUtil.getJsonString(details);
		logger.info("===== BLOCKCHAIN PAYLOAD inside gateway ===" + jsonPayload);
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();

		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.submitTransaction("createBirthCertificate", jsonPayload);
		String blockchainResponse = new String(result);
		logger.info("CreateBirthCertificate =====Result " + blockchainResponse);
		BlockchainBirthResponse blockchainBirthResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainBirthResponse.class);
		return blockchainBirthResponse;

	}

	public BlockchainStillBirthResponse insertStillBirthRecord(SBirthModel details, String privateHospitalChannel)
			throws Exception {

		logger.info(" Still birth  payload inside Blockchain gatway ===" + JsonUtil.getJsonString(details));
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}

		result = contract.submitTransaction("createStillBirthCertificate", JsonUtil.getJsonString(details));

		String blockchainResponse = new String(result);
		logger.info("CreateStillBirthCertificate =====Result " + blockchainResponse);
		BlockchainStillBirthResponse blockchainBirthResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainStillBirthResponse.class);
		return blockchainBirthResponse;
	}

	public BlockchainDeathResponse insertDeathRecord(DeathModel details, String privateHospitalChannel)
			throws Exception {

		logger.info(" Death payload inside Blockchain gatway ===" + JsonUtil.getJsonString(details));
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}

		result = contract.submitTransaction("createDeathCertificate", JsonUtil.getJsonString(details));
		String blockchainResponse = new String(result);
		logger.info("CreateDeathCertificate =====Result " + blockchainResponse);
		BlockchainDeathResponse blockchainDeathResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainDeathResponse.class);
		return blockchainDeathResponse;
	}

	// Birth record update
	public BlockchainUpdateBirthResponse updateBirthRecord(BirthModel details, String privateHospitalChannel)
			throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("contract is null");
		}
		String requestJson = JsonUtil.getJsonString(details);
		logger.info("birthCertificateModify:" + requestJson);
		result = contract.submitTransaction("birthCertificateModify", requestJson);

		String blockchainResponse = new String(result);
		logger.info("BirthCertificateModify ===== Result " + blockchainResponse);
		BlockchainUpdateBirthResponse blockchainBirthResponse = JsonUtil.getObjectFromJson(blockchainResponse,
		BlockchainUpdateBirthResponse.class);
		return blockchainBirthResponse;
	}

	// Still Birth record update
	public BlockchainUpdateSBirthResponse updateStillBirthRecord(SBirthModel details, String privateHospitalChannel)
			throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		String requestJson = JsonUtil.getJsonString(details);
		logger.info("stillBirthCertificateModify:" + requestJson);
		result = contract.submitTransaction("stillBirthCertificateModify", requestJson);

		String blockchainResponse = new String(result);
		logger.info("StillBirthCertificateModify ===== Result " + blockchainResponse);
		BlockchainUpdateSBirthResponse blockchainBirthResponse = JsonUtil.getObjectFromJson(blockchainResponse,
		BlockchainUpdateSBirthResponse.class);
		return blockchainBirthResponse;
	}

	public BlockchainUpdateDeathResponse updateDeathRecord(DeathModel details, String privateHospitalChannel)
			throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		Contract contract = network.getContract(Constants.CONTRACT_NAME);

		String requestJson = JsonUtil.getJsonString(details);
		logger.info("deathCertificateModify:" + requestJson);
		result = contract.submitTransaction("deathCertificateModify", requestJson);

		String blockchainResponse = new String(result);
		logger.info("deathCertificateModify ===== Result " + blockchainResponse);
		BlockchainUpdateDeathResponse blockchainBirthResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainUpdateDeathResponse.class);
		return blockchainBirthResponse;
	}

	// Birth record Rejection
	public BlockchainRejectBirthResponse rejectBirthRecord(String birthId, String userName, String dateTime,
			String remarks, String privateHospitalChannel) throws Exception {
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		// {"function":"deathCertificateReject","Args":["11","CM","NDMC Office","I want
		// to reject this death certificate"]}'
		// "Id, by, at, rejection comment" birthId, rejectedBy, rejectedAt,
		// rejectionRemark
		result = contract.submitTransaction("birthCertificateReject", birthId,  userName, dateTime,  remarks);
		String blockchainResponse = new String(result);
		logger.info("BirthCertificateReject ===== Result " + blockchainResponse);
		BlockchainRejectBirthResponse blockchainBirthRejectResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainRejectBirthResponse.class);
		return blockchainBirthRejectResponse;
	}

	public BlockchainRejectBirthResponse rejectStillBirthRecord(String birthId, String userName, String dateTime,
			String remarks, String privateHospitalChannel) throws Exception {
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		logger.info("stillBirthCertificateReject ===== birthId = " + birthId
				+ " , userName = " +userName
				+ " , dateTime = " +dateTime
				+ " , remarks = " +remarks);

		result = contract.submitTransaction("stillBirthCertificateReject", birthId,  userName, dateTime, remarks);
		String blockchainResponse = new String(result);
		logger.info("StillBirthCertificateReject ===== Result " + blockchainResponse);
		BlockchainRejectBirthResponse blockchainSBirthRejectResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainRejectBirthResponse.class);
		return blockchainSBirthRejectResponse;
	}

	public BlockchainRejectDeathResponse rejectDeathRecord(String deathId, String userName, String dateTime,
			String remarks, String privateHospitalChannel) throws Exception {
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		logger.info("deathCertificateReject ===== deathId = " + deathId
				+ " , userName = " +userName
				+ " , dateTime = " +dateTime
				+ " , remarks = " +remarks);
		result = contract.submitTransaction("deathCertificateReject", deathId, dateTime, userName,  remarks);
		String blockchainResponse = new String(result);
		logger.info("DeathCertificateReject ===== Result " + blockchainResponse);
		BlockchainRejectDeathResponse blockchainDeathRejectResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainRejectDeathResponse.class);
		return blockchainDeathRejectResponse;
	}

	// Birth record Approval
	public BlockchainApproveBirthResponse approveBirthRecord(String birthId, String userName, String dateTime,
			String privateHospitalChannel) throws Exception {
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		logger.info("birthCertificateApprove ===== deathId = " + birthId
				+ " , userName = " +userName
				+ " , dateTime = " +dateTime);
		result = contract.submitTransaction("birthCertificateApprove", birthId, userName, dateTime);
		String blockchainResponse = new String(result);
		logger.info("BirthCertificateApprove ===== Result " + blockchainResponse);
		BlockchainApproveBirthResponse blockchainApproveBirthResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainApproveBirthResponse.class);
		return blockchainApproveBirthResponse;
	}

	public BlockchainApproveBirthResponse approveStillBirthRecord(String birthId, String userName, String dateTime,
			String privateHospitalChannel) throws Exception {
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		logger.info("stillBirthCertificateApprove ===== deathId = " + birthId
				+ " , userName = " +userName
				+ " , dateTime = " +dateTime);
		result = contract.submitTransaction("stillBirthCertificateApprove", birthId, userName, dateTime);
		String blockchainResponse = new String(result);
		logger.info("StillBirthCertificateApprove ===== Result " + blockchainResponse);
		BlockchainApproveBirthResponse blockchainApproveBirthResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainApproveBirthResponse.class);
		return blockchainApproveBirthResponse;
	}

	public BlockchainApproveDeathResponse approveDeathRecord(String deathId, String userName, String dateTime,
			String privateHospitalChannel) throws Exception {
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.submitTransaction("deathCertificateApprove", deathId, userName, dateTime);
		String blockchainResponse = new String(result);
		logger.info("DeathCertificateApprove ===== Result " + blockchainResponse);
		BlockchainApproveDeathResponse blockchainApproveDeathResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainApproveDeathResponse.class);
		return blockchainApproveDeathResponse;
	}

	// Get Records from Application Number

//    public String  getRecordFromApplNo(String applNumber, String channel) throws Exception {
//
//        // create a gateway connection
//        byte result[];
//        Gateway gateway = blockchainService.getGateway();
//            // get the network and contract
//            Network network = gateway.getNetwork(channel);
//            Contract contract = network.getContract(Constants.CONTRACT_NAME);
//            result = contract.evaluateTransaction("queryBirthCertificate", applNumber);
//
//        String s = new String(result);
//        logger.info("queryBirthCertificate =====Result "+s);
//        return s;
//    }

	// New changes from applicationNumber to id

	public String getBirthRecord(Long birthId, String channel) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(channel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.evaluateTransaction("queryBirthCertificate", birthId.toString());

		String s = new String(result);
		logger.info("queryBirthCertificate =====Result " + s);
		return s;
	}

	public String getDeathRecord(String deathId, String channel) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(channel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.evaluateTransaction("queryDeathCertificate", deathId);

		String s = new String(result);
		logger.info("queryDeathCertificate =====Result " + s);
		return s;
	}

	public String getStillBirthRecord(String sbirthId, String channel) throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(channel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.evaluateTransaction("queryStillBirthCertificate", sbirthId);

		String s = new String(result);
		logger.info("queryStillBirthCertificate =====Result " + s);
		return s;
	}

	public List<BlockchainBirthHistoryResponse> getBirthHistoryByBirthId(String birthId, String channel)
			throws Exception {

		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(channel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.submitTransaction("getHistoryForBirthCertificate", birthId);
		String blockchainResponse = new String(result);
		blockchainResponse = blockchainResponse.replaceAll("mdifiedBy", "modifiedBy");
		logger.info("getHistoryForBirthCertificate ===== Result " + blockchainResponse);

		// JsonNode accounts = given().when().expect().statusCode(expectedResponseCode;
		// List<BlockchainBirthHistoryResponse> blcResponse = new
		// ArrayList<BlockchainBirthHistoryResponse>();
		JsonNode nodeblockchainBirthHistoryResponse = JsonUtil.getObjectFromJson(blockchainResponse, JsonNode.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(
				new SimpleModule().addSerializer(new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE))
						.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));
		objectMapper.registerModule(new SimpleModule()
				.addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE))
				.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));

		logger.info("===Converting Json node to Class " + nodeblockchainBirthHistoryResponse);
		List<BlockchainBirthHistoryResponse> blockchainBirthHistoryResponse = objectMapper.convertValue(
				nodeblockchainBirthHistoryResponse, new TypeReference<List<BlockchainBirthHistoryResponse>>() {
				});
		return blockchainBirthHistoryResponse;
	}

	public List<BlockchainStillBirthHistoryResponse> getStillBirthHistoryBySBirthId(String birthId, String channel)
			throws Exception {

		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(channel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.submitTransaction("getHistoryForStillBirthCertificate", birthId);
		String blockchainResponse = new String(result);
		// blockchainResponse = blockchainResponse.replaceAll("mdifiedBy",
		// "modifiedBy");
		logger.info("getHistoryForBirthCertificate ===== Result " + blockchainResponse);

		// JsonNode accounts = given().when().expect().statusCode(expectedResponseCode;
		// List<BlockchainBirthHistoryResponse> blcResponse = new
		// ArrayList<BlockchainBirthHistoryResponse>();
		JsonNode nodeblockchainBirthHistoryResponse = JsonUtil.getObjectFromJson(blockchainResponse, JsonNode.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(
				new SimpleModule().addSerializer(new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE))
						.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));
		objectMapper.registerModule(new SimpleModule()
				.addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE))
				.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));

		List<BlockchainStillBirthHistoryResponse> blockchainStillBirthHistoryResponse = objectMapper.convertValue(
				nodeblockchainBirthHistoryResponse, new TypeReference<List<BlockchainStillBirthHistoryResponse>>() {
				});
		return blockchainStillBirthHistoryResponse;
	}

	public List<BlockchainDeathHistoryResponse> getDeathHistoryByDeathId(String deathId, String channel)
			throws Exception {

		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(channel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.submitTransaction("getHistoryForDeathCertificate", deathId);
		String blockchainResponse = new String(result);
		// blockchainResponse = blockchainResponse.replaceAll("mdifiedBy",
		// "modifiedBy");
		logger.info("getHistoryForDeathCertificate ===== Result " + blockchainResponse);

		// JsonNode accounts = given().when().expect().statusCode(expectedResponseCode;
		// List<BlockchainBirthHistoryResponse> blcResponse = new
		// ArrayList<BlockchainBirthHistoryResponse>();
		JsonNode nodeblockchainBirthHistoryResponse = JsonUtil.getObjectFromJson(blockchainResponse, JsonNode.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(
				new SimpleModule().addSerializer(new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE))
						.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));
		objectMapper.registerModule(new SimpleModule()
				.addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE))
				.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));

		List<BlockchainDeathHistoryResponse> blockchainDeathHistoryResponse = objectMapper.convertValue(
				nodeblockchainBirthHistoryResponse, new TypeReference<List<BlockchainDeathHistoryResponse>>() {
				});
		return blockchainDeathHistoryResponse;
	}

	// SlaDetails record addition

	public BlockchainSlaDetailsResponse insertSlaDetails(SlaDetailsModel details, String privateHospitalChannel)
			throws Exception {

		String jsonPayload = JsonUtil.getJsonString(details);
		logger.info("insertSlaDetails   =====  BLOCKCHAIN PAYLOAD inside gateway ===" + jsonPayload);
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();

		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.submitTransaction("createSlaDetails", jsonPayload);
		String blockchainResponse = new String(result);
		logger.info("createSlaDetails =====Result " + blockchainResponse);
		BlockchainSlaDetailsResponse blockchainSlaDetailsResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainSlaDetailsResponse.class);
		return blockchainSlaDetailsResponse;

	}
	// SlaDetails record addition

	public BlockchainAttachmentResponse insertAttachmentDetails(AttachmentModel details, String privateHospitalChannel)
			throws Exception {

		String jsonPayload = JsonUtil.getJsonString(details);
		logger.info("===== BLOCKCHAIN PAYLOAD inside gateway ===" + jsonPayload);
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();

		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("Contract is null");
		}
		result = contract.submitTransaction("createAttachment", jsonPayload);
		String blockchainResponse = new String(result);
		logger.info("createAttachment =====Result " + blockchainResponse);
		BlockchainAttachmentResponse blockchainSlaDetailsResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainAttachmentResponse.class);
		return blockchainSlaDetailsResponse;

	}

	// SLA record update
	public BlockchainSlaDetailsResponse updateSlaRecord(SlaDetailsModel details, String privateHospitalChannel)
			throws Exception {

		logger.info(" updateSlaRecord Payload ===" + JsonUtil.getJsonString(details));
		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("contract is null");
		}
		String requestJson = JsonUtil.getJsonString(details);
		logger.info("slaDetailsModify:" + requestJson);
		result = contract.submitTransaction("slaDetailsModify", requestJson);

		String blockchainResponse = new String(result);
		logger.info("slaDetailsModify ===== Result " + blockchainResponse);
		BlockchainSlaDetailsResponse blockchainSlaDetailsResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainSlaDetailsResponse.class);
		return blockchainSlaDetailsResponse;
	}


	// Attachment record update
	public BlockchainAttachmentResponse updateAttachmentRecord(AttachmentModel details, String privateHospitalChannel)
			throws Exception {

		// create a gateway connection
		byte result[];
		Gateway gateway = blockchainService.getGateway();
		if (gateway == null) {
			throw new NullPointerException("Gateway is null");
		}
		// get the network and contract
		Network network = gateway.getNetwork(privateHospitalChannel);
		if (network == null) {
			throw new NullPointerException("Network is null");
		}
		Contract contract = network.getContract(Constants.CONTRACT_NAME);
		if (contract == null) {
			throw new NullPointerException("contract is null");
		}
		String requestJson = JsonUtil.getJsonString(details);
		logger.info("attachmentModify:" + requestJson);
		result = contract.submitTransaction("attachmentModify", requestJson);

		String blockchainResponse = new String(result);
		logger.info("attachmentModify  ===== Result " + blockchainResponse);
		BlockchainAttachmentResponse blockchainAttachmentResponse = JsonUtil.getObjectFromJson(blockchainResponse,
				BlockchainAttachmentResponse.class);
		return blockchainAttachmentResponse;
	}

}
