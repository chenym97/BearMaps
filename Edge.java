/**
 * Edge helper class.
 */
public class Edge {
    private long nodeid;
    private double length;

    public Edge(long nodeid, double length) {
        this.nodeid = nodeid;
        this.length = length;
    }

    public long nodeid() {
        return nodeid;
    }
}
