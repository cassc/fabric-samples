package application.java;

import lombok.Value;

@Value
public class Account {
        private final String accountID;
        private final String publicKey;
        private final double balance;
}

