package application.java;

import application.java.components.FabricComponent;
import application.java.dto.CreateAccountDto;
import application.java.dto.GetBalanceDto;
import application.java.dto.SendDto;
import lombok.SneakyThrows;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PublicController {
    private static final HashMap<String, Object> BAD_SIG = new HashMap<>(1);
    private static final HashMap<String, Object> ACCOUNT_NOT_EXIST = new HashMap<>(1);

    static {
        BAD_SIG.put("code", Code.INVALID_SIG);
        BAD_SIG.put("msg", "Signature is invalid");
        ACCOUNT_NOT_EXIST.put("code", Code.ACCOUNT_NOT_EXIST);
        ACCOUNT_NOT_EXIST.put("msg", "Account not exists on ledger");
    }

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    FabricComponent cache;
    @Value("${channel}")
    String channel;
    @Value("${chaincode.id}")
    String chaincodeId;

    @SneakyThrows
    private Account accountByAccountId(String accountId){
        Contract contract = cache.getContract(channel, chaincodeId);
        byte[] result = contract.evaluateTransaction("ReadAccount", accountId);
        if (result != null && result.length > 0){
            JSONObject obj = new JSONObject(new String(result));
            String key = obj.getString("publicKey");
            double balance = obj.getDouble("balance");
            return new Account(accountId, key, balance);
        }else{
            return null;
        }
    }

    @GetMapping("/balance")
    @ResponseBody
    public Map<String, Object> getBalance(GetBalanceDto dto){
        log.info("params {}", dto);

        try {
            String accountId = dto.getAccountId();
            int id = dto.getId();

            Account account = accountByAccountId(accountId);

            if (account == null){
                return ACCOUNT_NOT_EXIST;
            }

            if (!dto.validate(account.getPublicKey())){
                return BAD_SIG;
            }


            Contract contract = cache.getContract(channel, chaincodeId);
            log.info("ReadAccount {}", accountId);
            byte[] result = contract.evaluateTransaction("ReadAccount", accountId);

            String r = new String(result);

            log.info("result: {}", r);
            JSONObject obj = new JSONObject(r);
            double balance = obj.getDouble("balance");

            Map<String, Object> map = new HashMap<>();
            map.put("code", Code.OK);
            map.put("balance", balance);
            map.put("id", id);
            return map;
        } catch (ContractException e) {
            log.error("Error evaluating contract", e);
            Map<String, Object> map = new HashMap<>();
            map.put("code", Code.ERR);
            map.put("msg", e.getMessage());
            return map;
        }
    }

    @PutMapping("/account")
    @ResponseBody
    public Object createAccount(CreateAccountDto dto){
        try {
            Contract contract = cache.getContract(channel, chaincodeId);
            String accountId = dto.getAccountId();
            double balance = dto.getBalance();
            String publicKey = dto.getPublicKey();

            log.info("params {}", dto);


            byte[] result = contract.submitTransaction("CreateAccount", accountId, String.valueOf(balance), publicKey);

            if (result!=null && result.length>0){
                log.info("create account returns {}", new String(result));
            }

            Map<String, Object> map = new HashMap<>();
            map.put("code", Code.OK);
            return map;
        } catch (Exception e) {
            log.error("Error evaluating contract", e);
            Map<String, Object> map = new HashMap<>();
            map.put("code", Code.ERR);
            map.put("msg", e.getMessage());
            return map;
        }
    }

    @PostMapping("/send")
    @ResponseBody
    public Object send(SendDto dto){
        log.info("params {}", dto);

        try {
            String fromId = dto.getFromId();
            Account account = accountByAccountId(fromId);
            if (!dto.validate(account.getPublicKey())){
                return BAD_SIG;
            }

            Contract contract = cache.getContract(channel, chaincodeId);
            String toId = dto.getToId();
            double amount = dto.getAmount();
            int id = dto.getId();
            byte[] result = contract.submitTransaction("Send", fromId, toId, String.valueOf(amount));

            log.info("Sending {} from {} to {}", amount, fromId, toId);
            if (result!=null && result.length>0){
                log.info("returns {}", new String(result));
            }

            Map<String, Object> map = new HashMap<>();
            map.put("code", Code.OK);
            map.put("id", id);
            return map;
        } catch (Exception e) {
            log.error("Error evaluating contract", e);
            Map<String, Object> map = new HashMap<>();
            map.put("code", Code.ERR);
            map.put("msg", e.getMessage());
            return map;
        }
    }
}
