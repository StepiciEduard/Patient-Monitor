package itee.licenta.monitorizare.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MedicalDataTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static MedicalData getMedicalDataSample1() {
        return new MedicalData().id(1L).heartRate(1).systolicBp(1).diastolicBp(1).qtInterval(1).respiratoryRate(1);
    }

    public static MedicalData getMedicalDataSample2() {
        return new MedicalData().id(2L).heartRate(2).systolicBp(2).diastolicBp(2).qtInterval(2).respiratoryRate(2);
    }

    public static MedicalData getMedicalDataRandomSampleGenerator() {
        return new MedicalData()
            .id(longCount.incrementAndGet())
            .heartRate(intCount.incrementAndGet())
            .systolicBp(intCount.incrementAndGet())
            .diastolicBp(intCount.incrementAndGet())
            .qtInterval(intCount.incrementAndGet())
            .respiratoryRate(intCount.incrementAndGet());
    }
}
