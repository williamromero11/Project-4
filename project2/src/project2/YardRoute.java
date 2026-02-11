package project2;

public class YardRoute {
    public final int inbound;
    public final int s1;
    public final int s2;
    public final int s3;
    public final int outbound;

    public YardRoute(int inbound, int s1, int s2, int s3, int outbound) {
        this.inbound = inbound;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.outbound = outbound;
    }

    public String key() {
        return inbound + "-" + outbound;
    }

    @Override
    public String toString() {
        return "(" + inbound + " -> " + outbound + ") switches: [" + s1 + "," + s2 + "," + s3 + "]";
    }
}

