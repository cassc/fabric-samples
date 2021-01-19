package application.java.dto;

import application.java.SecureUtils;
import application.java.SignedData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
public class SendDto implements SignedData {
    private int id;
    private long timestamp;
    private String signature;

    private String fromId;
    private String toId;
    private double amount;

    @Override
    public boolean validate(String key) {
        // id+timestamp+param1+val1+param2+val2+...+paramN+valN+key
        String s = String.format("%d%d%s%s%s%s%s%f", id, timestamp,
                "fromId", fromId,
                "toId", toId,
                "amount", amount);
        return SecureUtils.verify(s, signature, key);
    }
}
