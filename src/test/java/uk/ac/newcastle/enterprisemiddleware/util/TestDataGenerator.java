package uk.ac.newcastle.enterprisemiddleware.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for generating unique test data to avoid conflicts in tests.
 */
public class TestDataGenerator {
    
    /**
     * Generates guaranteed-unique email for testing
     * @param prefix The prefix for the email (e.g., "john.doe")
     * @return Unique email address
     */
    public static String uniqueEmail(String prefix) {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return prefix + uuid + "@example.com";
    }
    
    /**
     * Generates valid UK phone number with unique digits
     * @param prefix The phone prefix (e.g., "074" for mobile, "015" for landline)
     * @return Valid UK phone number
     */
    public static String uniquePhone(String prefix) {
        int randomDigits = ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000);
        return prefix + randomDigits;
    }
    
    /**
     * Generates valid UK postcode in XX9999 format
     * @param prefix Two-letter area code (e.g., "NE", "SW")
     * @return Valid UK postcode
     */
    public static String uniquePostcode(String prefix) {
        int digits = ThreadLocalRandom.current().nextInt(1000, 10000);
        return prefix + digits;
    }
}
