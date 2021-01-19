package application.java.dto;

import application.java.SignedData;
import application.java.SecureUtils;
import lombok.Value;

@Value
public class GetBalanceDto implements SignedData {
    private int id;
    private long timestamp;
    private String signature;
    private String accountId;

    @Override
    public boolean validate(String key) {
        // id+timestamp+param1+val1+param2+val2+...+paramN+valN+key
        String s = String.format("%d%d%s%s", id, timestamp, "accountId", accountId);
        return SecureUtils.verify(s, signature, key);
    }

}
