package com.ndmc.ndmc_record.utils;

import com.google.common.hash.Hashing;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.domain.Url;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.exception.DateRangeException;
import com.ndmc.ndmc_record.model.UserModel;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CommonUtil {



    @Value("${MESSAGE_URL}")
    private String MESSAGE_URL;

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static String getSevenDigitNumber(int count) {
        String str = count + "";
        while(str.length() < Constants.APPLICATION_NUMBER_SEQ_LENGTH) {
            str = "0" + str;
        }

        return str;
    }

    public static long getDayBetweenDates(LocalDate firstDate, LocalDate secondDate){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fromDate = dtf.format(firstDate);
        String toDate = dtf.format(secondDate);
        LocalDate dateOfBirth = LocalDate.parse(fromDate);
        LocalDate dateOfRegistration = LocalDate.parse(toDate);
        long daysBetween = ChronoUnit.DAYS.between(dateOfBirth,dateOfRegistration);
        return daysBetween;
    }


    public static boolean checkNullOrBlank(String string) {
        if(string == null)
            return  true;
        else if (string.trim().isEmpty())
            return true;
        else if("null".equalsIgnoreCase(string))
            return  true;

        return  false;
    }

    public static Map<LocalDate, BigInteger> getMapFromList(List<Object[]> objectList) {
        Map<LocalDate, BigInteger> mappedResult = new HashMap<>();

        for (Object obj[] : objectList ) {
            Date d = (Date) obj[0];
            LocalDate ld = d.toLocalDate();

          //  LocalDate ld = (LocalDate) obj[0];
            BigInteger count = (BigInteger) obj[1];
            mappedResult.put(ld, count);
        }

        return mappedResult;
    }

    public static String updateExceptionMessage(String message) {
        if(message == null ){
            message = "Exception message null";
        }else{
            message =  message.length() > 1000 ? message.substring(0, 1000) : message;
        }
        return message;
    }


     public static String getDifference(Object objOld, Object objNew, String cols) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // if objnew is null

        //split cols with |
        String[] colsArr = cols.split("\\|");
        StringBuilder sb = new StringBuilder();
        if (objOld == null) {
            //return "";
            //for each colsArr split with ~
//            for (String col : colsArr) {
//                //split col with ~
//                String[] colArr = col.split("\\~");
//                //get col name colArr[1]
//                String colName = colArr[1];
//                //get col value from objOld
//                String colValue = getColValue(objNew, colArr[0]);
//                //append colName and colValue to sb
//                sb.append(colName).append(" added as ").append(colValue).append("<br />");
//
//            }

            sb.append(Constants.RECORD_CREATED);
        } else {
            //for each colsArr split with ~
            for (String col : colsArr) {
                //split col with ~
                String[] colArr = col.split("\\~");
                //get col name colArr[1]
                String colName = colArr[1];
                //get col value from objOld
                String colValueOld = getColValue(objOld, colArr[0]);
                //get col value from objNew
                String colValueNew = getColValue(objNew, colArr[0]);
                //if colValueOld is not equal to colValueNew
                if (!colValueOld.equals(colValueNew)) {
                    //append colName and colValueOld and colValueNew to sb
                    sb.append(colName).append(" changed from ").append(colValueOld).append(" to ").append(colValueNew).append("<br />");
                }
            }
        }

        return sb.toString();
    }

    private static String getColValue(Object objOld, String colName) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = objOld.getClass().getDeclaredMethod("get" + colName);
        Object objvalue = method.invoke(objOld, (Object[]) null);
        return String.valueOf(objvalue);
    }

    public static String getApprovalTimeLeft(LocalDateTime previousDateTime, LocalDateTime currentDateTime) throws Exception {

        String result = "";

        try {
           LocalDateTime d1 = previousDateTime;
            LocalDateTime d2 = currentDateTime;

            // logger.info("==== Prev Date time==="+d1+" ===== Current Date time ===="+d2);
            //in milliseconds
           // long diff = d2.getNano() - d1.getNano();
            d1 = d1.plusHours(Constants.APPROVAL_TIME_HOUR);
            //logger.info("==== Prev Date time==="+d1+" ===== Current Date time ===="+d2);

            Duration duration = Duration.between(d2, d1);
            // logger.info("===== Duration Days = "+duration.toDays() + " == Duration Hours = "+duration.toHours());
           // System.out.println("==== diffrence between dates ===== "+diff+ "==== current Date time =="+d2.getSecond()+ " ==== Modifieddate time ==== "+d1.getSecond());

            long diffDays = duration.toDays();
            long diffHours = duration.toHours() - (diffDays * 24);


            result = diffDays+" Days, "+diffHours+" Hours";

        } catch (Exception e) {
            e.printStackTrace();
        }
        //return String.valueOf(objvalue);
        return result;
    }

    public static void main(String[] args)  {
        try {
            logger.info(getApprovalTimeLeft(LocalDateTime.now(), LocalDateTime.now()));

            logger.info(getApprovalTimeLeft(LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
            logger.info(getApprovalTimeLeft(LocalDateTime.now(), LocalDateTime.now().minusHours(1)));
            logger.info(getApprovalTimeLeft(LocalDateTime.now(), LocalDateTime.now().plusHours(72)));
        }catch (Exception e){
            logger.error("Exception from getDiffrent function ", e);
        }
    }

    public static String convertDateTimeFormat(LocalDateTime dateTime) {
        String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT));
        return formattedDateTime;
    }

    public static String convertDateFormat(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT));
        return formattedDate;
    }

    public static String getCorrectionStatus(String status) {
        switch(status) {
            case Constants.RECORD_STATUS_PENDING:
                return Constants.RECORD_STATUS_CORRECTION_PENDING;
                
            case Constants.RECORD_STATUS_REJECTED:
                return Constants.RECORD_STATUS_CORRECTION_REJECTED;
                
            case Constants.RECORD_STATUS_APPROVED:
                return Constants.RECORD_STATUS_APPROVED;
                
            default:
                throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    public static String getInclusionStatus(String status) {
        switch(status) {
            case Constants.RECORD_STATUS_PENDING:
                return Constants.RECORD_STATUS_INCLUSION_PENDING;
                
            case Constants.RECORD_STATUS_REJECTED:
                return Constants.RECORD_STATUS_INCLUSION_REJECTED;
                
            case Constants.RECORD_STATUS_APPROVED:
                return Constants.RECORD_STATUS_APPROVED;
                
            default:
                throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

   
    public static String getTransactionType(String recordType, String transactionType) {
        if(Constants.RECORD_TYPE_BIRTH.equals(recordType) && transactionType.equalsIgnoreCase(Constants.FILTER_INCLUSION_SEARCH) ) {
            return Constants.RECORD_NAME_INCLUSION;
        } else if (Constants.RECORD_TYPE_BIRTH.equals(recordType)){
            return Constants.BIRTH_CORRECTION;
        } else if (Constants.RECORD_TYPE_SBIRTH.equals(recordType)){
            return Constants.STILL_BIRTH_CORRECTION;
        } else if (Constants.RECORD_TYPE_DEATH.equals(recordType)){
            return Constants.DEATH_CORRECTION;
        } else {
            throw new IllegalArgumentException("Invalid record type: " + recordType);
        }
    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    public static ApiResponse sendMessage(String textToSend, UserModel userDetails, String mobileNumber, String msgType) {
        ApiResponse res = new ApiResponse();
        String msgBody = "";
        String resMsg = "";
       // if(false) {
            switch (msgType) {
                case Constants.MSG_TYPE_OTP:
                    msgBody = "Dear " + userDetails.getFirstName() + "," + Constants.NEW_LINE_CHARS +
                            " Your OTP for mobile verification is " + textToSend + "." + Constants.NEW_LINE_CHARS + Constants.OTP_VALIDATION_MESSAGE;
                    resMsg = Constants.OTP_MESSAGE;
                    break;
                case Constants.MSG_TYPE_AC_CREATION:
                    msgBody = "Dear " + userDetails.getFirstName() + "," + Constants.NEW_LINE_CHARS +
                            " Your account has been created. Kindly use below details for login" + Constants.NEW_LINE_CHARS +
                            "USER ID: " + userDetails.getUserName() + Constants.NEW_LINE_CHARS +
                            "Password: " + textToSend + Constants.NEW_LINE_CHARS;
                    resMsg = Constants.ACCOUNT_CREATED;
                    break;
                case Constants.MSG_TYPE_FORGOT_PWD:
                    msgBody = "Dear " + userDetails.getFirstName() + "," + Constants.NEW_LINE_CHARS +
                            "The password has been reset" + Constants.NEW_LINE_CHARS +
                            "Your new password is " + textToSend;
                    resMsg = Constants.PWD_MESSAGE + " " + mobileNumber;
                    break;
                case Constants.MSG_TYPE_CHANGE_PWD:
                    msgBody = "Dear " + userDetails.getFirstName() + "," + Constants.NEW_LINE_CHARS +
                            "Your new password is " + textToSend;
                    resMsg = Constants.PWD_MESSAGE + " " + mobileNumber;
                    break;

            }
            Twilio.init(Constants.TWILIO_ACCOUNT_SID, Constants.TWILIO_AUTH_TOKEN);
            Message message = Message.creator(new PhoneNumber("+91" + mobileNumber),
                    new PhoneNumber(Constants.TWILIO_ACTIVE_NUMBER),
                    msgBody).create();
            logger.info("====Message response is =====" + message);
            if (message != null) {
                res.setStatus(HttpStatus.OK);
                res.setMsg(resMsg);
            }
//        }else {
//            res.setStatus(HttpStatus.OK);
//            res.setMsg(resMsg);
//        }
        return  res;
    }

    public static ApiResponse sendEventMessage(String name, String applNo, String mobileNumber, String msgType) {
        ApiResponse res = new ApiResponse();
        String msgBody = "";
        String resMsg = "";
        if(false) {
            switch (msgType) {
                case Constants.NEW_APPROVAL_REQUEST:
                    msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
                            "You have received a new approval request for application number " + applNo;
                    resMsg = Constants.APPROVAL_MESSAGE;
                    break;
                case Constants.REQUEST_APPROVED:
                    msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
                            "Your application number " + applNo + " has been approved.";
                    resMsg = Constants.APPROVED_REQ_MSG;
                    break;
                case Constants.REQUEST_REJECTED:
                    msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
                            "Your application number " + applNo + " has been rejected.";
                    resMsg = Constants.REJECTED_REQ_MSG;
                    break;
            }

            Twilio.init(Constants.TWILIO_ACCOUNT_SID, Constants.TWILIO_AUTH_TOKEN);
            Message message = Message.creator(new PhoneNumber("+91" + mobileNumber),
                    new PhoneNumber(Constants.TWILIO_ACTIVE_NUMBER),
                    msgBody).create();
            logger.info("====Message respons eis =====" + message);
            if (message != null) {
                res.setStatus(HttpStatus.OK);
                res.setMsg(resMsg);
            }
            return res;
        }else {
            res.setStatus(HttpStatus.OK);
            res.setMsg(resMsg);
        }
        return res;
    }


    public static void saveFile(Path uploadDir, String fileName,
                MultipartFile multipartFile) throws IOException {
            //Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            try (InputStream inputStream = multipartFile.getInputStream()) {
                Path filePath = uploadDir.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioe) {
                throw new IOException("Could not save image file: " + fileName, ioe);
            }
        }
    public static long betweenDates(LocalDateTime firstDate, LocalDateTime secondDate) throws DateRangeException
    {
        Duration daysBetween = Duration.between(firstDate, secondDate);
        // logger.info("getDayBetweenDates ========>>>" + daysBetween.toDays());
        // logger.info("FIRSTDATE ========>>>"+ firstDate + "  == second date = "+secondDate);
        if(daysBetween.toDays() >= Constants.FILTER_DATE_RANGE_ALLOW)
            throw new DateRangeException("Date range not allowed up to "+Constants.FILTER_DATE_RANGE_ALLOW+" days");
        return daysBetween.toDays();
   }
    public static void addPredicate(List<Predicate> predicates, CriteriaBuilder criterailBuilder, javax.persistence.criteria.Path expression, Long value) {
        predicates.add(criterailBuilder.in(expression).value(value));
    }

    public static String getPublicIp() throws Exception {
        InetAddress localhost = InetAddress.getLocalHost();
        String myLocalhost = localhost.getHostAddress();

        String systemipaddress = "";
        try
        {
           // URL url_name = new URL("http://bot.whatismyipaddress.com");
            URL url_name = new URL("https://checkip.amazonaws.com");

            BufferedReader sc =
                    new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        }
        catch (Exception e)
        {
            systemipaddress = "Cannot Execute Properly";
        }
       // System.out.println("Public IP Address: " + systemipaddress +"\n");

        return systemipaddress;
    }

    public static String getIpAddressByRequest(HttpServletRequest request) {

       String ipAddress = request.getHeader("X-FORWARDED-FOR");
       logger.info("X-FORWARDED-FOR IP"+ipAddress);
      //  String ipAddress = request.getRemoteAddr();
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
            logger.info("IP FROM SERVELET REQUEST"+ipAddress);
        }
        return ipAddress;
    }

    public static String getLiteracyValueById(String literacyCode) {
        String result = "";
        switch (literacyCode){
            case "0":
               // result = "OUTSOURCING DATA";
                result = Constants.LIT_OUTSOURCE;
                break;
            case "1":
               // result = "ILLITERATE";
                result = Constants.LIT_ILLITERATE;
            break;
            case "2":
                //result = "BELOW PRIMARY";
                result = Constants.LIT_BP;
                break;
            case "3":
                //result = "BELOW PRIMARY";
                result = Constants.LIT_NONMATRIC;
                break;
            case "4":
                //result = "BELOW PRIMARY";
                result = Constants.LIT_INTER;
                break;

            case "5":
                //result = "BELOW PRIMARY";
                result = Constants.LIT_GRADUATEABOVE;
                break;
            case "6":
                //result = "BELOW PRIMARY";
                result = Constants.LIT_UNKNOWN;
                break;
            case "9":
                //result = "BELOW PRIMARY";
                result = Constants.LIT_NSTATED;
                break;
        }
        return result;
    }

    public static String getReligionValueById(String religionCode) {
        String result = "";
        switch (religionCode){

            case "1":
                result = Constants.REL_HINDU;
                break;
            case "2":
                result = Constants.REL_MUSLIM;
                break;
            case "3":
                result = Constants.REL_CHRISTIAN;
                break;
            case "7":
                result = Constants.REL_OTHERS;
                break;

            case "9":
                result = Constants.REL_NSTATED;
                break;
            case "10":
                result = Constants.REL_SIKH;
                break;

        }
        return result;
    }

    public static String getOccupationValueById(String occupationCode) {

        String result = "";
        switch (occupationCode){

            case "0":
                result = Constants.OCP_PROFESSIONAL;
                break;
            case "1":
                result = Constants.OCP_ADMIN;
                break;
            case "2":
                result = Constants.OCP_CLERK;
                break;
            case "3":
                result = Constants.OCP_SALE;
                break;
            case "4":
                result = Constants.OCP_SERVICE;
                break;

            case "6":
                result = Constants.OCP_PRODUCTION;
                break;
            case "7":
                result = Constants.OCP_WORKER;
                break;
            case "8":
                result = Constants.OCP_NON_WORKER;
                break;

            case "9":
                result = Constants.OCP_NSTATED;
                break;
            case "10":
                result = Constants.OCP_BUSINESS;
                break;

            case "11":
                result = Constants.OCP_GOVT;
                break;
            case "12":
                result = Constants.OCP_HWIFE;
                break;

            case "13":
                result = Constants.OCP_PRIVATE;
                break;


        }
        return result;
    }

    public static String getMaritalStatusById(String maritalStatusCode) {
        String result = "";
        switch (maritalStatusCode){
            case "0":
                result = Constants.MS_NSTATED;
                break;
            case "1":
                result = Constants.MS_UNMARRIED;
                break;
            case "2":
                result = Constants.MS_MARRIED;
                break;
            case "4":
                result = Constants.MS_OUTSOURCE;
                break;

        }
        return result;
    }

    public static String getDeliveryAttentionById(String deliveryAttnCode) {
        String result = "";
        switch (deliveryAttnCode){
            case "1":
                result = Constants.DATTN_INST_GOVT;
                break;
            case "2":
                result = Constants.DATTN_INST_NON_GOVT;
                break;
            case "3":
                result = Constants.DATTN_DR_NURSE;
                break;
            case "4":
                result = Constants.DATTN_TRADITIONAL;
                break;
            case "5":
                result = Constants.DATTN_RELATIVES;
                break;

        }
        return result;
    }

    public static String getDeliveryMethodById(String deliveryMethodCode) {
        String result = "";
        switch (deliveryMethodCode){
            case "1":
                result = Constants.DM_NATURAL;
                break;
            case "2":
                result = Constants.DM_CEN;
                break;
            case "3":
                result = Constants.DM_VACM;
                break;

        }
        return result;
    }

    public static String getMedicalAttentionByCode(String medicalAttentionCode) {

        String result = "";
        switch (medicalAttentionCode){
            case "0":
                result = Constants.MA_OUTSOURCE;
                break;
            case "1":
                result = Constants.MA_NATURAL;
                break;
            case "2":
                result = Constants.MA_ACCIDENT;
                break;
            case "4":
                result = Constants.MA_HOMICIDE;
                break;

            case "5":
                result = Constants.MA_PI;
                break;
            case "6":
                result = Constants.MA_NSTATED;
                break;

            case "7":
                result = Constants.MA_INSTITUTIONAL;
                break;

            case "8":
                result = Constants.MA_NON_INSTITUTIONAL;
                break;
            case "9":
                result = Constants.MA_NO;
                break;

        }
        return result;
    }

    public static String shortenUrl(String originalUrl) {

        // generating murmur3 based hash key as short URL
        String key = Hashing.murmur3_32().hashString(originalUrl, Charset.defaultCharset()).toString();
        String url =  Url.builder().key(key).createdAt(LocalDateTime.now()).url(originalUrl).build().toString();
        return url;
    }


    public void sendTextMessage(String name, String contactNumber, String applNo, String recordType, String templateType, String userName, String password, String reviewUrl, String org, String trackNo) {

        ApiResponse res = new ApiResponse();
        String msgBody = "";
        String resMsg = "";
        String tempId = "";



        switch (templateType) {
            case Constants.NEW_APPROVAL_REQUEST:
//                msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
//                        "We have received a new approval request for "+recordType+" with application number "+applNo+"." +
//                        "Click on this link to review your record "+reviewUrl+" For any change in record visit your respective hospital within 48 Hours"+
//                         Constants.NEW_LINE_CHARS +"Team NDMC";
//                tempId = "1007166124407146166";
//                break;

            msgBody = "Dear "+name+", We have received a new approval request for "+recordType+" with application number "+applNo+".Click on this link to review your record "+reviewUrl+" for any change in record visit your respective hospital within 48 Hours. Team NDMC";
            tempId = "1007166124407146166";
            break;

            case Constants.REQUEST_APPROVED:
                msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
                        "Your application number "+applNo+" for "+recordType+" has been approved."+ Constants.NEW_LINE_CHARS +"Team NDMC";
                tempId = "1007164818604430371";
                break;

            case Constants.REQUEST_REJECTED:
                msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
                        "Your application number "+applNo+" for "+recordType+" has been rejected."+ Constants.NEW_LINE_CHARS +"Team NDMC";
                tempId = "1007164818606852391";
                break;

            case Constants.USER_CREATION:
                msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
                        "Your account has been created. Kindly use the below details for login."+
                        Constants.NEW_LINE_CHARS +
                        "USER ID:"+userName +Constants.NEW_LINE_CHARS +
                        "Password:"+password+
                        Constants.NEW_LINE_CHARS +"Team NDMC";
                tempId = "1007164818595044064";
                break;

            case Constants.FORGOT_PASSWORD:
                msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
                        "Your new password is "+password+

                        Constants.NEW_LINE_CHARS +"Team NDMC";
                tempId = "1007164818598768354";
                break;

            case Constants.CITIZEN_TRACKING_MSG:
                msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
                        "We got application for"+recordType+ " at "+org+"+ hospital"+
                        "Use"+ trackNo+" Tracking No. for further communication until record not accepted from the hospital"+
                        Constants.NEW_LINE_CHARS +"Team NDMC";
                tempId = "";
                break;

        }

        if(msgBody != null && contactNumber != null && tempId !=null) {
            String url = Constants.MESSAGE_URL + "&Mobile=" + contactNumber + "&message=" + msgBody + "&template_id=" + tempId;
            Object response = restTemplate().getForObject(url, String.class);
            logger.info("SMS URL IS ====" + url);
            logger.info("SMS RESPONSE ====" + response+" mobile No :"+contactNumber);
        }else{
            logger.info("=== MSG AND CONTACT NUMBER IS BLANK OR NULL");
        }
    }

    public ApiResponse sendTextMessageForOtp(String name, String contactNumber, String applNo, String recordType, String templateType, String userName, String password, String reviewUrl) {

        ApiResponse res = new ApiResponse();
        String msgBody = "";
        String resMsg = "";
        String tempId = "";


        switch (templateType) {
            case Constants.FORGOT_PASSWORD:
                msgBody = "Dear " + name + "," + Constants.NEW_LINE_CHARS +
                        "Your new password is "+password+Constants.NEW_LINE_CHARS +"Team NDMC";
                tempId = "1007164818598768354";
                break;

        }

        if(msgBody != null && contactNumber != null && tempId !=null) {
            String url = Constants.MESSAGE_URL + "&Mobile=" + contactNumber + "&message=" + msgBody + "&template_id=" + tempId;
            Object response = restTemplate().getForObject(url, String.class);
            res.setMsg(Constants.PWD_MESSAGE+contactNumber);
            res.setStatus(HttpStatus.OK);
            logger.info("SMS URL IS ====" + url);
            logger.info("SMS RESPONSE ====" + response+" mobile No :"+contactNumber);
        }else{
            logger.info("=== MSG AND CONTACT NUMBER IS BLANK OR NULL");
        }
        return res;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    public static String getAlphaNumericString(int n, String orgCode)
    {

        // lower limit for LowerCase Letters
        int lowerLimit = 97;

        // lower limit for LowerCase Letters
        int upperLimit = 122;

        Random random = new Random();

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer(n);

        for (int i = 0; i < n; i++) {

            // take a random value between 97 and 122
            int nextRandomChar = lowerLimit
                    + (int)(random.nextFloat()
                    * (upperLimit - lowerLimit + 1));

            // append a character at the end of bs
            r.append((char)nextRandomChar);
        }

        // return the resultant string
        return (r.toString()).toUpperCase();
    }


}

