import java.util.HashSet;

/**
 * Node helper class.
 */
public class Node implements Comparable<Node>{
    private long id;
    private String name;
    private double lon;
    private double lat;
    private HashSet<Edge> adj;
    private double priority = 0.0;

    public Node(long id, double lon, double lat) {
        this.id = id;
        this.name = null;
        this.lon = lon;
        this.lat = lat;
        adj = new HashSet<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public long id() {
        return id;
    }

    public double lon() {
        return lon;
    }

    public double lat() {
        return lat;
    }

    public HashSet<Edge> adj() {
        return adj;
    }

    public double priority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public int compareTo(Node other) {
        return Double.compare(priority, other.priority);
    }
}
