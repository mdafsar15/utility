package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.model.UserModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthRepository extends JpaRepository<UserModel, Long> {

    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    UserModel findByUserName(String username);
    String findDataByUserName(String userName);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByContactNo(String contactNo);


    List<UserModel> findByFirstName(String firstName);

    List<UserModel> findByLastName(String lastName);

    List<UserModel> findByContactNo(String mobileNo);

    List<UserModel> findByOrganizationId(String orgId);

    List<UserModel> findByFirstNameAndLastName(String firstName, String lastName);

    List<UserModel> findAll(Specification<UserModel> userModelSpecification);

    @Query(value="select * from users where contact_no =:mobileNoOrEmail", nativeQuery = true)
    UserModel findByMobileNo(String mobileNoOrEmail);


    UserModel findByEmail(String mobileNoOrEmail);

    @Query(value="select * from users as u, user_role as r where u.organization_id =:organizationId and (r.role_id =:approver or r.role_id =:cfcApprover)", nativeQuery = true)
    List<UserModel> findUserByOrganizationId(String organizationId, Long approver, Long cfcApprover);

    @Query(value="select * from users as u where u.user_name LIKE %:username% order by user_id desc limit 0,1", nativeQuery = true)
    UserModel findByUserNameLike(String username);

    UserModel findByUserNameStartsWith(String username);
}
