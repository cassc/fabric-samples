package application.java;


import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SecureUtils {
    final static Logger log = Logger.getLogger(SecureUtils.class.getName());

    public static PublicKey publicKeyFromString(String base64PublicKey) {
        byte[] publicKeyDER = Base64.getDecoder().decode(base64PublicKey);
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyDER));
            return publicKey;
        }catch (Exception e){
            log.warning("Error loading public key from string");
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(String plainText, String signature, String publicKey) {
        try{
            return verify(plainText, signature, publicKeyFromString(publicKey));
        }catch (Exception e){
            log.warning("Verify signature error: " + e.getMessage());
            return false;
        }

    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) {
        try{
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(plainText.getBytes(UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);

            return publicSignature.verify(signatureBytes);
        }catch (Exception e){
            log.warning("Verify signature error: " + e.getMessage());
            return false;
        }
    }
}
