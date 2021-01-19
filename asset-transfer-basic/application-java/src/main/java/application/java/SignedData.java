package application.java;

public interface SignedData {
    boolean validate(String pubkey);
}
