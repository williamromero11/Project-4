package project2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Yard {
    private final Map<Integer, Switch> switches = new HashMap<>();
    private final Map<String, YardRoute> routes = new HashMap<>();

    // Ensures only ONE train "moves through yard" at a time (per spec)
    private final ReentrantLock yardControl = new ReentrantLock(true);

    public Yard(int maxSwitchId) {
        for (int i = 1; i <= maxSwitchId; i++) {
            switches.put(i, new Switch(i));
        }
    }

    public Switch getSwitch(int id) {
        Switch s = switches.get(id);
        if (s == null) throw new IllegalArgumentException("Unknown switch id: " + id);
        return s;
    }

    public YardRoute getRoute(int inbound, int outbound) {
        return routes.get(inbound + "-" + outbound);
    }

    public void lockYardControl() {
        yardControl.lock();
    }

    public void unlockYardControl() {
        if (yardControl.isHeldByCurrentThread()) {
            yardControl.unlock();
        }
    }

    public void loadYardFile(String yardFilePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(yardFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // ✅ Supports comma-separated OR space-separated (or mixed)
                String[] parts = line.split("\\s*,\\s*|\\s+");
                if (parts.length != 5) continue; // ignore malformed lines

                int inbound = Integer.parseInt(parts[0]);
                int s1 = Integer.parseInt(parts[1]);
                int s2 = Integer.parseInt(parts[2]);
                int s3 = Integer.parseInt(parts[3]);
                int outbound = Integer.parseInt(parts[4]);

                YardRoute route = new YardRoute(inbound, s1, s2, s3, outbound);
                routes.put(route.key(), route);
            }
        }
    }


    public int routeCount() {
        return routes.size();
    }

    public Set<Integer> getSwitchIds() {
        return new TreeSet<>(switches.keySet());
    }
}

