package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.dto.TestDto;
import com.ndmc.ndmc_record.model.TestModel;
import com.ndmc.ndmc_record.repository.TestRepository;
import com.ndmc.ndmc_record.service.TestService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    TestRepository testRepository;

    @Override
    public TestDto saveRecords(TestDto testDto) {

        TestModel testModel = new TestModel();
        BeanUtils.copyProperties(testDto, testModel);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        testModel.setCreatedAt(now);
        testModel = testRepository.save(testModel);
        BeanUtils.copyProperties(testModel, testDto);
        return testDto;
    }
}
