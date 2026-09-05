package org.tss.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class DigitalSignatureService {
    public SignatureData sign(String value) {
        try {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            var keyPair = generator.generateKeyPair();
            var signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(keyPair.getPrivate());
            signer.update(value.getBytes(StandardCharsets.UTF_8));
            return new SignatureData(
                    Base64.getEncoder().encodeToString(signer.sign()),
                    Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign timesheet", e);
        }
    }

    public boolean verify(String value, String signature, String publicKey) {
        if (signature == null || publicKey == null) return false;
        try {
            var key = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
            var verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(key);
            verifier.update(value.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            return false;
        }
    }

    public record SignatureData(String signature, String publicKey) {}
}
