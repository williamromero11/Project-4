# Train Yard Dispatch Simulator (Multi-threaded Java)

A concurrent train-yard simulation that models multiple trains attempting to traverse a shared switch complex under Precision Scheduled Railroading (PSR)-inspired constraints. Each train must acquire exclusive control of a required set of switches (locks) in a strict order before it can move through the yard. The simulator uses Java’s `ExecutorService` and `ReentrantLock` to coordinate train movements and avoid deadlock.

## Key Features
- **Concurrent dispatching** using `ExecutorService` with a fixed thread pool (up to 30 trains).
- **Deadlock avoidance** via **strict lock acquisition ordering** (first → second → third switch).
- **Release-and-retry strategy**: if a required switch cannot be locked, the train releases all held locks and waits a random backoff period before retrying.
- **Config-driven simulation** using CSV input files:
  - Fleet file: `(trainNumber, inboundTrack, outboundTrack)`
  - Yard file: `(inboundTrack, firstSwitch, secondSwitch, thirdSwitch, outboundTrack)`
- **Permanent hold handling**: trains whose inbound→outbound path is not defined in the yard configuration are placed on permanent hold and never dispatched.
- **Full execution log + final status table** printed at the end of the run.

## Technologies & Concepts
- Java Concurrency: `java.util.concurrent.ExecutorService`, `Executors.newFixedThreadPool`
- Locking: `java.util.concurrent.locks.ReentrantLock` (`tryLock()` pattern)
- Thread-safe coordination + nondeterministic scheduling effects

## How It Works (High Level)
1. The simulator reads `TheFleet.csv` and `TheYard.csv`.
2. For each train, it looks up the required switch sequence for its inbound→outbound route.
3. Each train runs as a `Runnable` task:
   - Attempts to acquire all required switch locks in order.
   - If any lock attempt fails, releases all held locks and sleeps for a randomized backoff.
   - If all locks are acquired, simulates moving through the yard, then releases all locks and marks itself dispatched.
4. After all runnable trains finish (excluding permanent-hold trains), the simulator prints a final summary table.

> **Note on Dispatch Sequence:** Because thread scheduling is nondeterministic, the exact dispatch order may vary between runs even with the same inputs. This is expected for concurrent simulations.

## Input Files
Place the input files in a known location (recommended: an `input/` folder in the project root).

### Fleet File (`TheFleetFile.csv`)
Each line: trainNumber,inboundTrack,outboundTrack (12,8,2)

### Yard File (`TheYardFile.csv`)
Each line: inboundTrack,firstSwitch,secondSwitch,thirdSwitch,outboundTrack (5,4,3,1,1)

 ## Project Structure
 src/project2/
├── Main.java          # Simulation entry point
├── TrainTask.java    # Runnable train logic
├── TrainStatus.java  # Per-train state tracking
├── Switch.java       # Lockable switch abstraction
├── Yard.java         # Yard coordination logic
└── YardRoute.java    # Track/switch route definitions

theFleetFile.csv
theYardFile.csv
sampleRunOutput.txt

