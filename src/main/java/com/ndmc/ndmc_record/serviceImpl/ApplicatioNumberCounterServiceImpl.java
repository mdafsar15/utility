package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.model.ApplicationNumberCounter;
import com.ndmc.ndmc_record.repository.ApplicatioNumberCounterRepository;
import com.ndmc.ndmc_record.service.ApplicatioNumberCounterService;
import com.ndmc.ndmc_record.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

@Service
public class ApplicatioNumberCounterServiceImpl implements ApplicatioNumberCounterService {

    private final Logger logger = LoggerFactory.getLogger(ApplicatioNumberCounterServiceImpl.class);

    @Autowired
    ApplicatioNumberCounterRepository repository;

    @Override
    public String getApplicationNumber(String orgCode, String registrationType, LocalDateTime registrationDate, String regNo) throws Exception{
        int year = registrationDate.getYear();
        ApplicationNumberCounter counter = new ApplicationNumberCounter();
        counter.setOrgCode(orgCode);
        counter.setYear(year);
        counter.setRegistrationType(registrationType);
        counter.setCount(Integer.parseInt(regNo));
        String applicationNumber = generateApplicationNumber(counter);
        logger.info("===== applicationNumber is  ====" + applicationNumber);

        return applicationNumber;
    }


    @Override
    public String getRegistrationNumber(Long organizationId, String organizationCode, String registrationType, LocalDateTime registrationDate) throws Exception {
        return getRegistrationNumberCounter(organizationId, organizationCode, registrationType, registrationDate).getCount() + "";
    }

    @Override
    @Transactional
    public synchronized ApplicationNumberCounter getRegistrationNumberCounter(Long organizationId, String organizationCode, String registrationType, LocalDateTime registrationDate) throws Exception{
        int year = registrationDate.getYear();
        logger.info("===== getRegistrationNumberCounter is " +
                " organizationCode ====" +organizationCode+" , " +
                " year ====" +year+",  " +
                " registrationType ====" +registrationType);

        ApplicationNumberCounter counter = repository.getCounter(organizationCode, year, registrationType);

        // String dateOfEvent = birthDto.getEventDate().format(dtf);
       // logger.info("===== ApplicationNumberCounter is  ====" + counter.toString());

        // Backed previous code instead of Procedure due to issue 25-04-22
        int count = 1;
        if(counter == null) {
            counter = new ApplicationNumberCounter();
            counter.setOrgId(organizationId);
            counter.setOrgCode(organizationCode);
            counter.setYear(year);
            counter.setRegistrationType(registrationType);
            count = 1;
        } else {
            count = counter.getCount() + 1;
        }
        counter.setCount(count);
        repository.save(counter);

        return counter;
    }

    @Override
    public String generateApplicationNumber(ApplicationNumberCounter counter) throws Exception {
        return counter.getOrgCode() + counter.getRegistrationType() + CommonUtil.getSevenDigitNumber(counter.getCount()) + Constants.APPLICATION_NUMBER_SEPARATOR + counter.getYear();
    }
}
