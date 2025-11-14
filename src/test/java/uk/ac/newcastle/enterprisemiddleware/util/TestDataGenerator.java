package uk.ac.newcastle.enterprisemiddleware.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {
    
    public static String uniqueEmail(String prefix) {
        String id=UUID.randomUUID().toString();
        String noHyphen=id.replace("-", "");
        String short1=noHyphen.substring(0, 10);
        String result=prefix + short1 + "@example.com";
        return result;
    }
    
    public static String uniquePhone(String prefix) {
        ThreadLocalRandom rand=ThreadLocalRandom.current();
        int min=10_000_000;
        int max=100_000_000;
        int randomDigits = rand.nextInt(min, max);
        String phone=prefix + randomDigits;
        return phone;
    }
    
    public static String uniquePostcode(String prefix) {
        ThreadLocalRandom r=ThreadLocalRandom.current();
        int min=1000;
        int max=10000;
        int digits = r.nextInt(min, max);
        String postcode=prefix + digits;
        return postcode;
    }
}
