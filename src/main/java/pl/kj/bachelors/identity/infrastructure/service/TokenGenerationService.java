package pl.kj.bachelors.identity.infrastructure.service;

import com.google.common.base.Charsets;
import javassist.bytecode.ByteArray;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.*;

@Service
public class TokenGenerationService {
    public String generateToken(String prefix, String suffix, int contentLength) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        Future<String> token = executor.submit(() -> {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            String content = this.generateContent(contentLength);
            String timestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
            String rawValue = String.format("%s%s%s%s", prefix, timestamp, content, suffix);
            md5.update(rawValue.getBytes(StandardCharsets.UTF_8));

            return DatatypeConverter.printHexBinary(md5.digest());
        });

        return token.get();
    }

    public String generateNumericToken(int length) {
        Random random = new Random();
        String[] pinElements = new String[length];
        for(int i = 0; i < pinElements.length; i++) {
            pinElements[i] = String.valueOf(random.nextInt(9));
        }

        return String.join("", pinElements);
    }

    private String generateContent(final int length) {
        Random random = new Random();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return String.valueOf(Hex.encode(bytes));
    }
}
