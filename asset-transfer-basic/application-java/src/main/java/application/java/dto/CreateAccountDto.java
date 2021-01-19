package application.java.dto;

import lombok.Value;

@Value
public class CreateAccountDto {
    private String accountId;
    private String publicKey;
    private double balance;
}
