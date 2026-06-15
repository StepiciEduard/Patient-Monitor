package itee.licenta.monitorizare.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class AppointmentSlotTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static AppointmentSlot getAppointmentSlotSample1() {
        return new AppointmentSlot().id(1L);
    }

    public static AppointmentSlot getAppointmentSlotSample2() {
        return new AppointmentSlot().id(2L);
    }

    public static AppointmentSlot getAppointmentSlotRandomSampleGenerator() {
        return new AppointmentSlot().id(longCount.incrementAndGet());
    }
}
