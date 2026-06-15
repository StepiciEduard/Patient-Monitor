package itee.licenta.monitorizare.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PatientTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Patient getPatientSample1() {
        return new Patient().id(1L).cnp("cnp1").phoneNumber("phoneNumber1").address("address1").gender("gender1");
    }

    public static Patient getPatientSample2() {
        return new Patient().id(2L).cnp("cnp2").phoneNumber("phoneNumber2").address("address2").gender("gender2");
    }

    public static Patient getPatientRandomSampleGenerator() {
        return new Patient()
            .id(longCount.incrementAndGet())
            .cnp(UUID.randomUUID().toString())
            .phoneNumber(UUID.randomUUID().toString())
            .address(UUID.randomUUID().toString())
            .gender(UUID.randomUUID().toString());
    }
}
