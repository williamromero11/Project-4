package project2;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class TrainTask implements Runnable {
    private final int trainNo;
    private final int inbound;
    private final int outbound;
    private final Yard yard;

    private final AtomicInteger dispatchCounter;

    private volatile TrainStatus status;

    // Report table:
    private volatile boolean hold;
    private volatile boolean dispatched;
    private volatile int dispatchSeq;

    private volatile int s1;
    private volatile int s2;
    private volatile int s3;

    public TrainTask(int trainNo, int inbound, int outbound, Yard yard, AtomicInteger dispatchCounter) {
        this.trainNo = trainNo;
        this.inbound = inbound;
        this.outbound = outbound;
        this.yard = yard;
        this.dispatchCounter = dispatchCounter;

        // Defaults
        this.hold = false;
        this.dispatched = false;
        this.dispatchSeq = 0;
        this.s1 = 0; this.s2 = 0; this.s3 = 0;
    }

    public int getTrainNo() { return trainNo; }
    public int getInbound() { return inbound; }
    public int getOutbound() { return outbound; }

    public TrainStatus getStatus() { return status; }
    public boolean isHold() { return hold; }
    public boolean isDispatched() { return dispatched; }
    public int getDispatchSeq() { return dispatchSeq; }

    public int getS1() { return s1; }
    public int getS2() { return s2; }
    public int getS3() { return s3; }

    @Override
    public void run() {
        YardRoute route = yard.getRoute(inbound, outbound);

        // Permanent hold: no valid route
        if (route == null) {
            status = TrainStatus.PERMANENT_HOLD;
            hold = true;
            dispatched = false;
            dispatchSeq = 0;
            s1 = s2 = s3 = 0;

            System.out.println(
                "*************\n" +
                "Train " + trainNo + " is on permanent hold and cannot be dispatched.\n" +
                "*************"
            );
            return;
        }

        // Save the route switches for the final report table
        s1 = route.s1;
        s2 = route.s2;
        s3 = route.s3;

        // Keep trying until dispatched
        while (true) {
            int first = s1, second = s2, third = s3;

            Switch sw1 = yard.getSwitch(first);
            Switch sw2 = yard.getSwitch(second);
            Switch sw3 = yard.getSwitch(third);

            // Try first
            if (!sw1.tryLock()) {
                System.out.println("Train " + trainNo + ": UNABLE TO LOCK first required switch: Switch " + first + ". Train will wait...");
                randomWait();
                continue;
            }
            System.out.println("Train " + trainNo + ": HOLDS LOCK on Switch " + first + ".");

            // Try second
            if (!sw2.tryLock()) {
                System.out.println("Train " + trainNo + ": UNABLE TO LOCK second required switch: Switch " + second + ".");
                System.out.println("Train " + trainNo + ": Releasing lock on first required switch: Switch " + first + ". Train will wait...");
                sw1.unlockIfHeldByCurrentThread();
                randomWait();
                continue;
            }
            System.out.println("Train " + trainNo + ": HOLDS LOCK on Switch " + second + ".");

            // Try third
            if (!sw3.tryLock()) {
                System.out.println("Train " + trainNo + ": UNABLE TO LOCK third required switch: Switch " + third + ".");
                System.out.println("Train " + trainNo + ": Releasing locks on first and second required switches: Switch " + first + " and Switch " + second + ".");
                System.out.println("Train will wait...");
                sw2.unlockIfHeldByCurrentThread();
                sw1.unlockIfHeldByCurrentThread();
                randomWait();
                continue;
            }
            System.out.println("Train " + trainNo + ": HOLDS LOCK on Switch " + third + ".");

            System.out.println("Train " + trainNo + ": HOLDS ALL NEEDED SWITCH LOCKS – Train movement begins.");

            // Only ONE train moves through yard at a time
            yard.lockYardControl();
            try {
                // simulate movement through yard
                sleepMs(ThreadLocalRandom.current().nextInt(250, 751));

                System.out.println("Train " + trainNo + ": Clear of yard control.");
                System.out.println("Train " + trainNo + ": Releasing all switch locks.");
                System.out.println("Train " + trainNo + ": Unlocks/releases lock on Switch " + first + ".");
                System.out.println("Train " + trainNo + ": Unlocks/releases lock on Switch " + second + ".");
                System.out.println("Train " + trainNo + ": Unlocks/releases lock on Switch " + third + ".");
            } finally {
                sw3.unlockIfHeldByCurrentThread();
                sw2.unlockIfHeldByCurrentThread();
                sw1.unlockIfHeldByCurrentThread();
                yard.unlockYardControl();
            }

            System.out.println("Train " + trainNo + ": Has been dispatched and moves on down the line out of yard control into CTC.");
            System.out.println("@ @ @ TRAIN " + trainNo + ": DISPATCHED @ @ @");

            status = TrainStatus.DISPATCHED;
            dispatched = true;
            dispatchSeq = dispatchCounter.incrementAndGet();
            break;
        }
    }

    private void randomWait() {
        sleepMs(ThreadLocalRandom.current().nextInt(150, 651));
    }

    private void sleepMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
