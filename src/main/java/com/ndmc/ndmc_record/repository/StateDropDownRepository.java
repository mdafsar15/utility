package com.ndmc.ndmc_record.repository;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.StateMasterModel;

@Repository
public interface StateDropDownRepository extends JpaRepository<StateMasterModel, Long> {
	
	List<StateMasterModel> findByPincode(String pincode);

	List<StateMasterModel> findByStateNameNotNull();

	List<StateMasterModel> findByStateNameAndAreaNotNull(String stateName);

	List<StateMasterModel> findByStateNameAndAreaAndDistrictNameNotNull(String stateName, String area);

	List<StateMasterModel> findByStateNameAndAreaAndDistrictNameAndPincodeNotNull(String stateName, String area,
			String districtName);

	List<StateMasterModel>  findByStateNameAndAreaAndDistrictNameAndDeleteFlagAndPincodeNotNull(String stateName, String area, String districtName, String n);

	List<StateMasterModel> findByDeleteFlagAndStateNameNotNull(String status);

	List<StateMasterModel> findByStateNameAndDeleteFlagAndAreaNotNull(String stateName, String status);

	List<StateMasterModel> findByStateNameAndAreaAndDeleteFlagAndDistrictNameNotNull(String stateName, String area, String status);

	List<StateMasterModel>  findByStateNameAndDeleteFlagAndDistrictNameNotNull(String stateName, String n);

	List<StateMasterModel> findByStateNameAndDistrictNameAndDeleteFlagAndAreaNotNull(String stateName, String districtName, String n);
}
