package com.ndmc.ndmc_record.controller;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.blockchainGatway.EnrollAdminGovt;
import com.ndmc.ndmc_record.dto.BlockchainDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/blockchain")
//@CrossOrigin(origins = "*")
public class BlockchainController {

    @PostMapping("/enroll-blc-users")
    public String enrollAdminGovt(@RequestBody BlockchainDto dto) throws Exception {
        EnrollAdminGovt.main(null);
         return "Done";
    }

}
