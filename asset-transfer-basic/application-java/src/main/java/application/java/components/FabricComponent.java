package application.java.components;

import org.hyperledger.fabric.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Component
public class FabricComponent {
    Logger log = LoggerFactory.getLogger(this.getClass());

    private Gateway gateway;

    private Gateway connect() throws Exception{
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("..", "..", "test-network", "organizations", "peerOrganizations", "org1.example.com", "connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "aUser").networkConfig(networkConfigPath).discovery(true);
        return builder.connect();
    }

    public Gateway getGateway(){
        if (Objects.isNull(gateway)){
            try {
                Gateway gt = connect();
                synchronized (this){
                    if (Objects.isNull(gateway)){
                        gateway = gt;
                    }else{
                        gt.close();
                    }
                }
            } catch (Exception e) {
                log.error("Error connnecting to gateway!", e);
            }
        }
        return gateway;
    }

    public Contract getContract(String channel, String chainCodeId) {
        log.info("Getting contract for {} {}", channel, chainCodeId);
        Gateway gateway = getGateway();
        Objects.requireNonNull(gateway, "Gateway not ready!");

        Network network = gateway.getNetwork(channel);
        return network.getContract(chainCodeId);

    }
}
