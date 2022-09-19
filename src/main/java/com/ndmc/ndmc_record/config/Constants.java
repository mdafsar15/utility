package com.ndmc.ndmc_record.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {
    public static final String BLC_STATUS_FALSE = "false";
    public static final String ORG_NDMC = "NDMC";
    public static final String ORG_NOT_SAME_MESSAGE = "Your Organization is not same";
    public static final String SDM_LETTER_BLANK = "SDM Letter field is empty";
    public static final String CRON = "CRON";
    public static final String PRINT_TYPE_CFC = "C";
    public static final String PRINT_TYPE_HOSPITAL = "H";
    public static final String YES_PRINTED = "YES";
    public static final String NAME_INCLUSION = "Name Inclusion";
    public static final String RECORD_ADDED = "Record Addition";
    public static final String RECORD_UPDATED = "Record Updation";
    public static final String RECORD_REJECTED = "Record Rejection";
    public static final String RECORD_DELETE = "Record Deletion";
    public static final String RECORD_APPROVED = "Record Approval";
    public static final String HISTORY_REJECTION_TEXT = "Record rejected with remark <br/>";
    public static final String HISTORY_APPROVED_TEXT = "Record Approved";


    public static final String BIRTH_HISTORY_DESCRIPTION_HEADER = "" +
            "RegistrationDatetime~Date Of Registration|ApplicationNumber~Application Number|" +
            "EventDate~Date Of Birth|GenderCode~Gender|Name~Child Name|PermanentAddress~Permanent Address" +
            "|FatherName~Father Name|SdmLetterNo~SDM Letter Number|PrintedBy~Printed By|TransactionType~Transaction Type" +
            "|RejectionRemark~Rejection Remark|FatherNationality~Father Nationality|MotherNationality~Mother Nationality|" +
            "PinCode~PinCode|RejectedAt~Rejected At|RejectedBy~Rejected By|ApprovedAt~Approved At|ApprovedBy~Approved By|" +
            "ContactNumber~Contact Number|Status~Status|OrganizationCode~Organisation Code|MotherAdharNumber~Mother Aadhar Number|" +
            "FatherAdharNumber~Father Aadhar Number|ModifiedBy~Modified By|ModifiedAt~ModifiedAt|LateFee~Late Fee|" +
            "CrNumber~CR Number|ActivityCode~Activity Code|InformantAddress~Informant Address|InformantName~Informant Name|" +
            "MethodOfDelivery~Method Of Delivery|MotherAgeAtMarriage~Mother Age At Marriage|AddressAtBirth~Address At Birth|" +
            "IsNdmcResident~Is NDMC Resident|IsDelhiResident~Is Delhi Resident|EventPlaceFlag~Event Place Flag|" +
            "StateName~State Name|DistrictName~District Name|NameVillageTown~Name Of Village or Town|" +
            "UserId~User Id|DeliveryAttentionCode~Delivery Attention Code|BirthOrder~Birth Order|IllegitimateBirth~Illegitimate Birth|" +
            "NumberWeekPregnancy~Number Week Pregnancy|ChildWeight~Child Weight|MotherAge~MotherAge|" +
            "MotherReligion~Mother Religion|MotherOccupation~Mother Occupation|MotherLiteracy~Mother Literacy|" +
            "MotherName~Mother Name|FatherReligion~Father Religion|FatherOccupation~Father Occupation|" +
            "FatherLiteracy~Father Literacy|FatherName~Father Name|PermanentAddress~Permanent Address|Name~Name|" +
            "GenderCode~Gender Code|EventPlace~Event Place|DivisionCode~Division Code";

    public static final String DEATH_HISTORY_DESCRIPTION_HEADER = "" +
            "RegistrationDatetime~Date Of Registration|ApplicationNumber~Application Number|" +
            "EventDate~Date Of Birth|GenderCode~Gender|PermanentAddress~Permanent Address" +
            "|FatherName~Father Name|SdmLetterNo~SDM Letter Number|PrintedBy~Printed By|TransactionType~Transaction Type" +
            "|RejectionRemark~Rejection Remark|FatherNationality~Father Nationality|MotherNationality~Mother Nationality|" +
            "PinCode~PinCode|RejectedAt~Rejected At|RejectedBy~Rejected By|ApprovedAt~Approved At|ApprovedBy~Approved By|" +
            "ContactNumber~Contact Number|Status~Status|OrganizationCode~Organisation Code|MotherAdharNumber~Mother Aadhar Number|" +
            "FatherAdharNumber~Father Aadhar Number|modifiedBy~Modified By|ModifiedAt~ModifiedAt|LateFee~Late Fee|" +
            "CrNumber~CR Number|ActivityCode~Activity Code|InformantAddress~Informant Address|InformantName~Informant Name|" +
            "IsNdmcResident~Is NDMC Resident|IsDelhiResident~Is Delhi Resident|EventPlaceFlag~Event Place Flag|"+
            "StateName~State Name|DistrictName~District Name|NameVillageTown~Name Of Village or Town|" +
            "userId~User Id|" +
            "FatherLiteracy~Father Literacy|FatherName~Father Name|PermanentAddress~Permanent Address|Name~Name|" +
            "GenderCode~Gender Code|EventPlace~Event Place|DivisionCode~Division Code|YearsSmoking~Smoking Year|YearsDrinking~Drinking Year|" +
            "YearsTobacco~Year of consuming Tobacco|IsPregnant~Is Pregnant|CauseOfDeath~Cause Of Death|MaritalStatusCode~Marital Status Code|" +
            "MedicalAttentionCode~Medical Attention Code|DeceasedReligion~Deceased Religion|DeceasedAge~Age of Deceased|" +
            "CauseOfDeathDetails~Cause Of Death Details|DeceasedAdharNumber~Deceased Adahar Number|EducationCode~Education Code|" +
            "ReligionCode~Religion Code|Nationality~Nationality|OccupationCode~Occupation Code";
    public static final long APPROVAL_TIME_HOUR = 72;
    public static final String RECORD_CREATED = "Record Created";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String ACTION_ALREADY_APPLIED = "Action already Applied";
    public static final String CHILD_NAME_NOT_FOUND = "Child name not found";
    public static final String SBIRTH_ID = "sbirthId";
    public static final String BIRTH_ID = "birthId";
    public static final String FILTER_LEGAL_CORRECTION_SEARCH = "legal-correction-search";
    public static final String FILTER_INCLUSION_SEARCH = "inclusion-search";
    public static final String FILTER_PRINT_SEARCH = "print-search";
    public static final String DEFAULT_PASSWORD = "welcome";
    public static final String USERNAME_NOT_AVAILABLE = "This user name is already registered with us";
    public static final String EMAIL_NOT_AVAILABLE = "This email id already registered with us";
    public static final String EMPCODE_NOT_AVAILABLE = "This Employee Code or Username already registered with us";
    public static final String MOBILENO_NOT_AVAILABLE ="This Mobile No already registered with us" ;
    public static final String USER_ADDED = "User Added Successfully";
    public static final String USER_DEFAULT_STATUS = "ENABLED";
    public static final String EMPCODE_MANDATORY = "Employee code is mandatory";
    public static final String MOBILENO_MANDATORY = "Contact number is mandatory";
    public static final String USERID_NOT_AVAILABLE = "USER ID Field is missing";
    public static final String USER_UPDATED = "USER Updated Successfully";
    public static final String USER_FNAME = "firstName";
    public static final String USER_LNAME = "lastName";
    public static final String USER_ORGID = "organizationId";
    public static final String USER_MOBILENO = "contactNo";
    public static final String PASSWORD_NOT_MATCHED = "You have entered wrong current password";
    public static final String CONFIRM_PASSWORD_NOT_MATCHED = "New Password and Confirm Password not matched";
    public static final String CURRENT_PWD_BLANK = "Current Password field is Mandatory";
    public static final String NEW_PWD_BLANK = "Password and Confirm Password field is mandatory";
    public static final String PASSWORD_UPDATED = "Password is updated successfully!";
    public static final String MANDATORY_FIELD = "Field is mandatory";
    public static final String MAIL_FROM = "deepjavac@gmail.com";
    public static final String FORGOT_PWD_SUBJECT = "Reset your Password";
    public static final String RESET_LINK_SENT = "Password reset link has been sent on " ;
    public static final String EMAIL_NOT_REGISTERED = "Email id not registered with us" ;
    public static final String TOKEN_VALID = "Token is valid";
    public static final String TOKEN_EXPIRED = "Token is expired";
    public static final String TOKEN_INVALID = "Token is invalid";
    public static final String PASSWORD_RESET_URL = "http://172.16.200.216:8081/api/v1/user/password/verify?token=";
    public static final String ORG_ID_MANDATORY = "Organization Id is mandatory";
    public static final String TWILIO_ACTIVE_NUMBER = "+15626625770";
    public static final String OTP_MESSAGE = "OTP is sent on your registered mobile number ";
    public static final String MOBILE_IS_REGISTERED = "This Mobile number is not registered with us";
    public static final String OTP_VALID = "OTP is Valid";
    public static final String OTP_INVALID = "OTP is Invalid";
    public static final String OTP_EXPIRED = "OTP is Expired";
    public static final String PWD_MESSAGE = "The password has been reset and sent on  ";
    public static final Integer DEFAULT_RESET_COUNT =1 ;
    public static final String RESET_COUNT_UPDATED = "Reset Count updated!";
    public static final String MSG_TYPE_OTP = "OTP";
    public static final String NEW_LINE_CHARS = "\n";
    public static final String NEW_DOUBLE_LINE_CHARS = "\n\n";
    public static final String OTP_VALIDATION_MESSAGE = "The OTP is valid for 5 minutes" ;
    public static final String MSG_TYPE_AC_CREATION = "REGISTRATION" ;
    public static final String MSG_TYPE_FORGOT_PWD = "FORGOT_PASSWORD";
    public static final String MSG_TYPE_CHANGE_PWD = "CHANGE_PASSWORD";
    public static final String NEW_APPROVAL_REQUEST = "NEW_APPROVAL_REQUEST";
    public static final String REQUEST_APPROVED = "REQUEST_APPROVED";
    public static final String REQUEST_REJECTED = "REQUEST_REJECTED";
    public static final String ACCOUNT_CREATED = "Account has been created successfully";
    public static final String APPROVAL_MESSAGE = "Approval request created";
    public static final String APPROVED_REQ_MSG = "Request is Approved";
    public static final String REJECTED_REQ_MSG = "Request is Rejected";
    public static final Long ROLEID_CFCAPPROVER = 5L;
    public static final Long ROLEID_APPROVER = 4L;
    public static final String FILE_NOT_FOUND = "File Not Found";
   // public static final long FILTER_DATE_RANGE_ALLOW = 92;
   /*
      Increase Date range from 3 month to 6 months
      Author:Deepak
      Date: 17-05-22
    */
    public static final long FILTER_DATE_RANGE_ALLOW = 184;
    public static final long FILTER_DATE_NAME_INCLUSION = 365;
    public static final String SLA_ORGID = "slaOrganizationId";
    public final static String RECORD_TYPE_SLA = "SLA";
    public static final String INDIVIDUAL = "individual";
    public static final String EVENT_FLAG_HOSPITAL = "H";
    public static final String GENDER_CODE_M = "M";
    public static final String GENDER_CODE_F = "F";
    public static final String GENDER_CODE_A = "A";
    public static final String EVENT_FLAG_HOME = "N";
    public static final String REPORT_INST = "Institutional";
    public static final String REPORT_DOM = "Domiciliary";
    public static final String TOTAL = "Total";
    public static final String REPORT_BIRTH = "Birth";
    public static final String REPORT_STILL_BIRTH = "StillBirth";
    public static final String REPORT_DEATH = "Death";
    public static final String REPRINT_CERTIFICATE = "Could not verify Certificate details. Please print new Certificate!!";
    public static final String IS_UNKNOWN_CASE = "Y";
    public static final String EVENT_DATE_BLANK = "Event Date Should not be Blank!";
    public static final String MESSAGE_URL = "http://sms.ndmc.gov.in/?SenderId=NDMCIT";
    public static final String USER_CREATION = "USER_CREATION";
    public static final String FORGOT_PASSWORD = "FORGOT_PASSWORD";
    public static final String APPLICANT = "Applicant";
    public static final String SELECT_CFC = "Select CFC First";
    public static final String TRANSACTION_TYPE_PRINT_BIRTH_CERT = "PRINT_BIRTH_CERTIFICATE";
    public static final String TRANSACTION_TYPE_PRINT_STILL_BIRTH_CERT = "PRINT_STILL_BIRTH_CERTIFICATE";
    public static final String TRANSACTION_TYPE_PRINT_DEATH_CERT = "PRINT_DEATH_CERTIFICATE";
    public static final String ALL_FIELDS_REQUIRED = "All Fields are required!";
    public static final String CAUSE_OF_DEATH = "causeOfDeath";
    public static final String LIT_OUTSOURCE = "OUTSOURCING DATA";
    public static final String LIT_ILLITERATE = "ILLITERATE";
    public static final String LIT_BP = "BELOW PRIMARY";
    public static final String LIT_NONMATRIC = "PRIMARY BUT BELOW MATRIC";
    public static final String LIT_INTER = "MATRIC BUT BELOW GRADUATE";
    public static final String LIT_GRADUATEABOVE = "GRADUATE AND ABOVE";
    public static final String LIT_UNKNOWN = "UNKNOWN";
    public static final String LIT_NSTATED = "NOT STATED";
    public static final String REL_HINDU = "HINDU";
    public static final String REL_MUSLIM = "MUSLIM";
    public static final String REL_CHRISTIAN = "CHRISTIAN";
    public static final String REL_OTHERS = "OTHERS";
    public static final String REL_NSTATED = "NOT STATED";
    public static final String REL_SIKH = "SIKH";

    public static final String OCP_PROFESSIONAL = "PROFESSIONAL, TECHNICAL AND RELATED WORKERS";
    public static final String OCP_ADMIN = "ADMINISTRATIVE, EXECUTIVE AND MANAGERIAL WORKERS";
    public static final String OCP_CLERK = "CLERICAL AND RELATED WORKERS";
    public static final String OCP_SALE = "SALES WORKERS";
    public static final String OCP_SERVICE = "SERVICE WORKERS";
    public static final String OCP_PRODUCTION = "PRODUCTION AND OTHER RELATED WORKERS, TRANSPORT EQUIPMENT OPERATORS & LABOURERS";
    public static final String OCP_WORKER = "WORKERS, WHOSE OCCUPATION ARE NOT ELSEWHERE CLASSIFIED";
    public static final String OCP_NON_WORKER = "NON WORKERS";
    public static final String OCP_NSTATED = "NOT STATED";
    public static final String OCP_BUSINESS = "BUSINESS";
    public static final String OCP_GOVT = "GOVERNMENT SERVICE";
    public static final String OCP_HWIFE = "HOUSE WIFE";
    public static final String OCP_PRIVATE = "PRIVATE SERVICE";

    public static final String MS_NSTATED = "NOT STATED";
    public static final String MS_MARRIED = "MARRIED";
    public static final String MS_UNMARRIED = "UNMARRIED";
    public static final String MS_OUTSOURCE = "OUTSORCING DATA";

    public static final String MA_OUTSOURCE = "OUTSOURCING DATA";
    public static final String MA_NATURAL = "NATURAL";
    public static final String MA_ACCIDENT = "ACCIDENT";
    public static final String MA_SUICIDE = "SUICIDE";

    public static final String MA_HOMICIDE = "HOMICIDE";
    public static final String MA_PI = "PENDING INVESTIGATION";
    public static final String MA_NSTATED = "NOT STATED";

    public static final String MA_INSTITUTIONAL = "INSTITUTIONAL";
    public static final String MA_NON_INSTITUTIONAL = "MEDICAL ATTENTION OTHER THAN INSTITUTIONAL";
    public static final String MA_NO = "NO MEDICAL ATTENTION";

    public static final String DM_NATURAL = "Natural";
    public static final String DM_CEN = "Caesarean";
    public static final String DM_VACM = "Forceps/Vaccum";

    public static final String DATTN_INST_GOVT = "INSTITUTIONAL (GOVT.)";
    public static final String DATTN_INST_NON_GOVT = "INSTITUTIONAL(NON GOVT)";
    public static final String DATTN_DR_NURSE = "DOCTOR, NURSE OR TRAINED MIDWIFE";
    public static final String DATTN_TRADITIONAL = "TRADITIONAL BIRTH ATTENDANT";
    public static final String DATTN_RELATIVES = "RELATIVES OR OTHERS";
    public static final String APPL_NUMBER = "applNo" ;
    public static final String NAME_INCLUSION_TRANSACTION = "NAME_INCLUSION";
    public static final String FILTER_DATA_CORRECTION = "data-correction";
    public static final Long FILTER_DATE_DATA_CORRECTION = 365L;
    public static final String ONLINE_BIRTH_CORRECTION = "ONLINE_BIRTH_CORRECTION";
    public static final String ONLINE_DEATH_CORRECTION = "ONLINE_DEATH_CORRECTION";
    public static final String ONLINE_STILL_BIRTH_CORRECTION = "ONLINE_STILL_BIRTH_CORRECTION";
    public static final String APPOINTMENT_CREATED = "Appointment Created Successfully!";
    public static final String APPOINTMENT_NOT_CREATED = "Appointment Not Created due to technical issue";
    public static final String OPEN = "OPEN";
    public static final String CLOSED = "CLOSED";
    public static final String APPOINTMENT_OPEN = "Appointment is already Open for this SLA";
    public static final String APPOINTMENT_UPDATED = "Appointment Updated successfully!" ;
    public static final String APPOINTMENT_CLOSED = "This Appointment is already Closed !";
    public static final String REVIEW_URL = "ndmc.gov.in/online_service.aspx" ;
    public static final String REVIEW_UAT_URL = "124.247.205.123?";
    public static final String CITIZEN_BIRTH = "CITIZEN_BIRTH";
    public static final String CITIZEN_DEATH = "CITIZEN_DEATH";
    public static final String CITIZEN_SBIRTH = "CITIZEN_SBIRTH";
    public static final String RECORD_SUBMITTED = "SUBMITTED";
    public static final String CITIZEN_TRACKING_MSG = "CITIZEN_TRACKING_MSG";
    public static final String TRACKING_NUMBER = "trackingNo";
    public static final String YES = "Y";


    public static String PRINT_CERT_SEPARATOR = "_";
    public static int APPLICATION_NUMBER_SEQ_LENGTH = 7;
    public static int HOSPITAL_BIRTH_LATE_FINE = 5;
    public static String ONLINE_TYPE_ONLINE = "O";
    public static String APPLICATION_TYPE_BIRTH = "B";
    public static String APPLICATION_TYPE_DEATH = "D";
    public static String APPLICATION_TYPE_STILL_BIRTH = "S";
    public static String APPLICATION_TYPE_CFC_BIRTH = "CFC_B";
    public static String APPLICATION_TYPE_CFC_DEATH = "CFC_D";
    public static String APPLICATION_TYPE_CFC_STILL_BIRTH = "CFC_S";
    public static String APPLICATION_TYPE_PRINT = "PRINT";
    public static String APPLICATION_TYPE_CFC_INCLUSION = "CFC_INCLUSION";

    public static String ONLINE_INCLUSION = "ONLINE_INCLUSION";
    public static String CFC_PRINT_B = "CFC_PRINT_B";
    public static String CFC_PRINT_D = "CFC_PRINT_D";
    public static String CFC_PRINT_S = "CFC_PRINT_S";
    public static String ONLINE_PRINT_B = "ONLINE_PRINT_B";
    public static String ONLINE_PRINT_D = "ONLINE_PRINT_D";
    public static String ONLINE_PRINT_S = "ONLINE_PRINT_S";

    public static String APPLICATION_NUMBER_SEPARATOR = "/";

    public static String CONTRACT_NAME;
    public static String NDMC_GOVT_USER;
    public static String BASE_PATH;
    public static String CONNECTION_PROFILE;

    @Autowired
    public void setContractName(@Value("${BLOCKCAHIN.CONTRACT_NAME}") String contractName) {
        Constants.CONTRACT_NAME = contractName;
    }

    @Autowired
    public void setNdmcGovtUser(@Value("${BLOCKCAHIN.NDMC_GOVT_USER}") String ndmcGovtUser) {
        Constants.NDMC_GOVT_USER = ndmcGovtUser;
    }

    @Autowired
    public void setBasePath(@Value("${BLOCKCAHIN.BASE_PATH}") String basePath) {
        Constants.BASE_PATH = basePath;
    }

    @Autowired
    public void setConnectionProfile(@Value("${BLOCKCAHIN.CONNECTION_PROFILE}") String connectionProfile) {
        Constants.CONNECTION_PROFILE = connectionProfile;
    }


    public final static String DATA_TYPE_MIGRATED = "MIGRATED";

    public final static String RECORD_TYPE_BIRTH = "BIRTH";
    public final static String RECORD_TYPE_SBIRTH = "STILL-BIRTH";
    public final static String RECORD_TYPE_DEATH = "DEATH";

    public final static String RECORD_TYPE_ONLINE_PRINT_BIRTH = "ONLINE_PRINT_BIRTH";
    public final static String RECORD_TYPE_ONLINE_NAME_INCLUSION = "ONLINE_NAME_INCLUSION";
    public final static String RECORD_TYPE_ONLINE_PRINT_DEATH = "ONLINE_PRINT_DEATH";
    public final static String RECORD_TYPE_ONLINE_PRINT_STILL_BIRTH = "ONLINE_PRINT_STILL_BIRTH";
    public final static String RECORD_TYPE_STILL_BIRTH = "STILL_BIRTH";



    public static Float LATE_FEE = 5F;
    public static Long LAPSE_DAY_START = 21L;
    public static Long LAPSE_DAY_END = 365L;

    public static final String RECORD_STATUS_PENDING="PENDING";
    public static final String RECORD_STATUS_APPROVED="APPROVED";
    public static final String RECORD_STATUS_REJECTED="REJECTED";
    public static final String RECORD_STATUS_DRAFT="DRAFT";
    public static final String RECORD_STATUS_UPLOADING= "UPLOADING";

    public static String RECORD_STATUS_ACTIVE= "ACTIVE";
    public static String RECORD_STATUS_INACTIVE= "INACTIVE";


    public static final String ROLE_ADMIN="ADMIN";
    public static final String ROLE_CREATOR="CREATOR";
    public static final String ROLE_APPROVER="APPROVER";
    public static final String ROLE_CFC_APPROVER="CFCAPPROVER";
    public static final String ROLE_CFC_CREATOR="CFCCREATOR";
    public static final String ROLE_PUBLIC="PUBLIC";
    public static final String ROLE_PUBLIC_CREATOR="PUBLICCREATOR";
    public static final String ROLE_PUBLIC_APPROVER="PUBLICAPPROVER";
    public static final String RECORD_TYPE_OLD="OLD";
    public static final String RECORD_TYPE_NEW="NEW";
    public static final String USER_TYPE_HOSPITAL="HOSPITAL";
    public static final String USER_TYPE_CFC="CFC";
    public static final String ROLE_CFC_REGISTRAR = "CFCREGISTRAR";
    public static final String ROLE_CHIEF_REGISTRAR = "CHIEFREGISTRAR";
    public static final String REGISTRATION_NUMBER = "registrationNumber";
    public static final String APPLICATION_NUMBER = "applicationNumber";
    public static final String ORIGINAL_APPLICATION_NUMBER = "originalApplicationNumber";
    public static final String MOTHER_NAME = "motherName";
    public static final String FATHER_NAME = "fatherName";
    public static final String DIVISION_CODE = "divisionCode";
    public static final String GENDER_CODE = "genderCode";
    public static final String ORGANIZATION_CODE = "organizationCode";
    public static final String REGISTRATION_DATE_TIME = "registrationDatetime";
    public static final String EVENT_DATE = "eventDate";
    public static final String CHILD_NAME = "name";
    public static final String HUSBAND_WIFE_NAME  = "husbandWifeName";
    public static final String CORRECTION_SLA_ID = "correctionSlaId";

    public static final String INCLUSION_SLA_ID = "inclusionSlaId";
    public static final String EVENT_PLACE = "eventPlace";
    public static final String EVENT_PLACE_FLAG = "eventPlaceFlag";
    public static final String FILTER_NAME_INCLUSION = "name-inclusion";
    public static final String FILTER_NAME_APPROVAL = "name-approval";
    public static final String FILTER_LEGAL_CORRECTIONS = "legal-correction";
    public static final String FILTER_LEGAL_CORRECTIONS_APPROVAL = "legal-correction-approval";
    public static final String FILTER_ALL = "ALL";
    public static final String RECORD_STATUS_INCLUSION_PENDING = "INCLUSION_PENDING";
    public static final String RECORD_STATUS_CORRECTION_PENDING = "CORRECTION_PENDING";
    public static final String RECORD_STATUS_CORRECTION_REJECTED = "CORRECTION_REJECTED";
    public static final String RECORD_STATUS_INCLUSION_REJECTED = "INCLUSION_REJECTED";
    public static final String FILTER_LEGAL_CORRECTION_REJECTION = "legal-correction-rejection";
    public static final String FILTER_NAME_REJECTION = "name-rejection";
    public static final String USER_ID = "userId";
    public static final long DEFAULT_DAYS = 31;
    public static final String RECORD_STATUS_UPLOADED = "UPLOADED";
    public static final String LEGAL_CORRECTIONS = "Legal Correction";
    public static final String CERTIFICATE_TYPE = "certificateType";
    public static final String TRANSACTION_TYPE = "transactionType";
    public static final String USER_NAME = "modifiedBy";
    public static final String RECORD_NAME_INCLUSION = "NAME_INCLUSION";
    public static final String RECORD_CORRECTION = "CORRECTION";

    public static String DATE_FORMAT_WITH_TIME="yyyy-MM-dd HH:mm:ss";
    public static String DATE_FORMAT_WITHOUT_TIME="yyyy-MM-dd";

    public static String RECORD_ALREADY_UPDATED ="Record already Updated";
    public static String RECORD_NOT_FOUND="Record not found";
    public static String NOT_PERMITTED="Action not permitted!";
    public static String RECORD_ALREADY_APPROVED="Record already Approved";
    public static String RECORD_ALREADY_REJECTED="Record already Rejected";
    public static String BIRTH_SUCCESS_MESSAGE="Birth record added successfully";
    public static String SBIRTH_SUCCESS_MESSAGE="Still Birth record added successfully";
    public static String DEATH_SUCCESS_MESSAGE="Death record added successfully";
    public static String NAME_ALREADY_INCLUDED_MESSAGE="Name is already included.";
    //public static String NAME_ALREADY_INCLUDED_MESSAGE="Record not found or Name is already included.";
    public static String NAME_INCLUSION_PENDING_MESSAGE="Name included request is PENDING!";
    public static String PENDING_MESSAGE="Request is PENDING!";
    public static String CHECK_REQUEST_MESSAGE="Check your request parameters,url something is wrong! ";
    public static String NAME_INCLUSION_NOT_ALLOW_MESSAGE="Do not allow the inclusion of names from records older than days ";
    public static String DATA_CORRECTION_NOT_ALLOW_MESSAGE="Do not allow Data Correction from records older than days ";
    public static String BIRTH_UPDATE_SUCCESS_MESSAGE="Birth record updated successfully";
    public static String SBIRTH_UPDATE_SUCCESS_MESSAGE="Still Birth record updated successfully";
    public static String DEATH_UPDATE_SUCCESS_MESSAGE="Death record updated successfully";
    public static String DATE_FORMAT_MESSAGE="Date format not correct! ";
    public static String RECORD_APPROVED_MESSAGE="Record Approved successfully";
    public static String RECORD_REJECTED_MESSAGE="Record is Rejected !";

    public static String BIRTH_STATUS_NOT_PENDING="Birth Status is not PENDING!";
    public static String SBIRTH_STATUS_NOT_PENDING="Still Birth Status is not PENDING!";
    public static String DEATH_STATUS_NOT_PENDING="Death Status is not PENDING!";

    public static String VISIT_CFC="Action not permitted ! Please visit nearest CFC";

    public static String START_END_DATE_MANDATORY="START and END date is mandatory";

    public static String FILE_UPLOAD_SUCCESS= "File upload successfully";
    public static String FILTER_ALL_ARGS="ALL_ARGS";
    public static String FILTER_WITHOUT_REG_NO="WITHOUT_REG_NO";
    public static String FILTER_WITHOUT_APP_NO="WITHOUT_APP_NO";
    public static String FILTER_WITHOUT_STATUS="WITHOUT_STATUS";
    public static String CONTACT_NUMBER = "contactNumber";
    public static String STATUS = "status";
    public static String HOSPITAL = "HOSPITAL";
    public static String CFC = "CFC";
    public static String ORGANIZATION_TYPE_HOSPITALS = "hospitals";
    public static String ORGANIZATION_TYPE_CFCS = "cfcs";
    public static String ORGANIZATION_TYPE_ORGANIZATIONS = "organizations";


    public static final String NAME_INCLUSION_SUCCESS_MESSAGE="Name Inclusion record insert successfully";

    public static final String BIRTH_CORRECTIONS_SUCCESS_MESSAGE = "Birth correction record insert successfully ";
    public static final String DEATH_CORRECTIONS_SUCCESS_MESSAGE = "Death correction record insert successfully ";
    public static final String STILL_BIRTH_CORRECTION_SUCCESS_MESSAGE = "Still Birth correction record insert successfully ";
    public static final String PRINT_REQUEST_SUCCESS_MESSAGE = "Print request accepted successfully ";
    public static final String PRINT_REQUEST_ERROR_MESSAGE = "Print request not accepted ";
    public static final String GET_RECORDS_MESSAGE = "Get records successfully ";
    public static final String PASS_VALID_MESSAGE =" Pass valid parameters with value!  applNo, dateOfEvent OR dateOfEvent,genderCode,fatherName,motherName,divisionCode ";
    public static final String PASS_VALID_ONLINE_REQUEST_MESSAGE =" Pass valid parameters with value! bndId,transactionType,recordType,applicantContact,applicantName,applicantAddress,dueDate,applicantEmailId,noOfCopies ";
    public static final String EMAILID_MESSAGE =" Pass valid email id ";
    public static final String MOBILE_MESSAGE =" Pass valid contact number in 10 digit ";

    public static final String PASS_MOTHER_FATHER_MESSAGE="Mother's,Father's name should not be less than 3 characters";
    public static final String PASS_GENDER_MESSAGE="Gender code not be blank or null!";
    public static final String PASS_APPL_NO_MESSAGE="Application number not be blank or null!";
    public static final String DEATH_ID = "deathId";
    public static final String BIRTH_CORRECTION = "BIRTH_CORRECTION";
    public static final String STILL_BIRTH_CORRECTION = "STILL_BIRTH_CORRECTION";
    public static final String DEATH_CORRECTION = "DEATH_CORRECTION";
    public static final String SLA_DETAILS_MODEL = "sla";
    public static final String DATA = "data";
    public static final String BLC_STATUS_TRUE = "TRUE";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    //public static String ND = "ndmc_user";

    public static final String TWILIO_ACCOUNT_SID = "AC7b7a82e63cc4014bd9624187270347d4";
    public static final String TWILIO_AUTH_TOKEN = "3b0d8d59c8e8ddd6a722d6ff47a98b9a";

    public static String RECORD_STATUS_DRAFT_ERROR="Record is not draft !";
    public static String RECORD_WRONG_ERROR="Wrong selection !";
    public static String RECORD_USER_ERROR="Are is not permitted to delete this records !";
    public static String RECORD_ALREADY_DELETED_ERROR="Records already deleted !";
    public static String RECORD_Y = "Y";
    public static String RECORD_N = "N";
    public static String BIRTH_DELETED_SUCCESS_MESSAGE="Birth record deleted successfully";
    public static String SBIRTH_DELETED_SUCCESS_MESSAGE="Still Birth record deleted successfully";
    public static String DEATH_DELETED_SUCCESS_MESSAGE="Death record deleted successfully";

    public static final String CREATED_AT = "createdAt";

    public static final String PRINTED_AT_HOSPITAL = "H";
    public static final String PRINTED_AT_ONLINE = "O";
    public static final String PRINTED_AT_CFC = "C";
    public static final String RECORD_TYPE_STILLBIRTH = "SBIRTH";
    public static final String RECORD_BIRTH_CAP = "Birth" ;
    public static final String RECORD_SBIRTH_CAP = "SBirth" ;
    public static final String RECORD_DEATH_CAP = "Death" ;

    public static final String INFANT_DEATH_UNDER1 = "InfantDeathUnderOne" ;
    public static final String INFANT_DEATH_UNDER5 = "InfantDeathUnderFive" ;
    public static final String EVENT = "EVENT";
    public static final String REGISTRATION = "REGISTRATION";
}
