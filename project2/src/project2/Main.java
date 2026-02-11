package project2;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final int MAX_TRAINS = 30;

    public static void main(String[] args) throws Exception {
        String fleetPath = (args.length >= 1) ? args[0] : "theFleetFile.csv";
        String yardPath  = (args.length >= 2) ? args[1] : "theYardFile.csv";

        System.out.println("$ $ $ TRAIN MOVEMENT SIMULATION BEGINS. $ $ $");

        // Load yard routes and also compute max switch id dynamically from yard file
        int maxSwitchId = findMaxSwitchId(yardPath);
        Yard yard = new Yard(maxSwitchId);
        yard.loadYardFile(yardPath);

        // Shared dispatch sequence counter (1,2,3,... in order trains get dispatched)
        AtomicInteger dispatchCounter = new AtomicInteger(0);

        List<TrainTask> trains = loadFleetFile(fleetPath, yard, dispatchCounter);

        ExecutorService exec = Executors.newFixedThreadPool(Math.max(1, trains.size()));
        for (TrainTask t : trains) exec.submit(t);

        exec.shutdown();
        exec.awaitTermination(10, TimeUnit.MINUTES);

        //  “Final report” 
        printFinalReport(trains);

        System.out.println("$ $ $ SIMULATION ENDS $ $ $");
    }

    private static List<TrainTask> loadFleetFile(String fleetFilePath, Yard yard, AtomicInteger dispatchCounter)
            throws IOException {

        List<TrainTask> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fleetFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Supports: "10,1,5" OR "10 1 5"
                String[] parts = line.split("\\s*,\\s*|\\s+");
                if (parts.length != 3) continue; // skip header/malformed

                int trainNo = Integer.parseInt(parts[0]);
                int inbound = Integer.parseInt(parts[1]);
                int outbound = Integer.parseInt(parts[2]);

                list.add(new TrainTask(trainNo, inbound, outbound, yard, dispatchCounter));

                if (list.size() >= MAX_TRAINS) break;
            }
        }
        return list;
    }

    private static void printFinalReport(List<TrainTask> trains) {
        System.out.println();
        System.out.println("FINAL STATUS OF ALL TRAINS");
        System.out.println("--------------------------");
        System.out.println();

        // format
        for (TrainTask t : trains) {
            System.out.println("Train Number " + t.getTrainNo() + " assigned.");
            System.out.printf("%-12s %-14s %-14s %-9s %-9s %-9s %-8s %-11s %-16s%n",
                    "Train Number", "Inbound Track", "Outbound Track",
                    "Switch 1", "Switch 2", "Switch 3",
                    "Hold", "Dispatched", "Dispatch Sequence");
            System.out.println("-----------------------------------------------------------------------------------------------");

            System.out.printf("%-12d %-14d %-14d %-9d %-9d %-9d %-8s %-11s %-16d%n",
                    t.getTrainNo(),
                    t.getInbound(),
                    t.getOutbound(),
                    t.getS1(),
                    t.getS2(),
                    t.getS3(),
                    String.valueOf(t.isHold()),
                    String.valueOf(t.isDispatched()),
                    t.getDispatchSeq()
            );

            System.out.println();
        }
    }

    private static int findMaxSwitchId(String yardFile) throws IOException {
        int max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(yardFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] p = line.split("\\s*,\\s*|\\s+");
                if (p.length != 5) continue; // skip bad/header

                // format
                int s1 = Integer.parseInt(p[1]);
                int s2 = Integer.parseInt(p[2]);
                int s3 = Integer.parseInt(p[3]);

                max = Math.max(max, Math.max(s1, Math.max(s2, s3)));
            }
        }
        return Math.max(max, 1);
    }
}

