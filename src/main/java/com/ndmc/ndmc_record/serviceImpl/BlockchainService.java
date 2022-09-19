package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;

import com.ndmc.ndmc_record.config.Constants;
import org.hyperledger.fabric.gateway.DefaultCommitHandlers;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class BlockchainService {
     private Gateway gateway;

    public Gateway getGateway() {
        if(gateway == null) {
            try {
                Gateway.Builder builder = prereq();
                gateway = builder.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return gateway;
    }


    public Gateway.Builder prereq() throws IOException {

        // Load a file system based wallet for managing identities.

        String BASE_PATH = Constants.BASE_PATH;
        String CONNECTION_PROFILE = Constants.CONNECTION_PROFILE;
        Path walletPath = Paths.get(BASE_PATH + "wallet/govt");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
//        Path networkConfigPath = Paths.
//                get("/home/welcome/Documents/workspace/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.json");
        Path networkConfigPath = Paths.get(BASE_PATH + CONNECTION_PROFILE, "govt-connection.json");

        // Gateway.Builder builder = Gateway.createBuilder();
        Gateway.Builder builder = Gateway.createBuilder().commitHandler(DefaultCommitHandlers.NONE);
        builder.identity(wallet, "admin").networkConfig(networkConfigPath).discovery(true);
        return builder;

    }

}
