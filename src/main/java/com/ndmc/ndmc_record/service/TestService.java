package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.TestDto;
import com.ndmc.ndmc_record.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public interface TestService {

    public TestDto saveRecords(TestDto testDto);
}
