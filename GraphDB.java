import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */
    private HashMap<Long, Node> nodes = new HashMap<>();

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    void addNode(Node n) {
        nodes.put(n.id(), n);
    }

    Node findNode(Long id) {
        return nodes.get(id);
    }

    void connect(Node v, Node w) {
        double dist = distance(v, w);
        Edge edgeToW = new Edge(w.id(), dist);
        v.adj().add(edgeToW);
        Edge edgeToV = new Edge(v.id(), dist);
        w.adj().add(edgeToV);
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        HashSet<Long> nodesToDelete = new HashSet<>();
        for (Long v : vertices()) {
            if (nodes.get(v).adj().size() == 0) {
                nodesToDelete.add(v);
            }
        }
        for (Long v : nodesToDelete) {
            nodes.remove(v);
        }
    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        return nodes.keySet();
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        HashSet<Long> adjOfV = new HashSet<>();
        for (Edge e : nodes.get(v).adj()) {
            adjOfV.add(e.nodeid());
        }
        return adjOfV;
    }

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ). */
    double distance(long v, long w) {
        return distance(nodes.get(v), nodes.get(w));
    }

    double distance(Node v, Node w) {
        return Math.sqrt(Math.pow(v.lon() - w.lon(), 2) + Math.pow(v.lat() - w.lat(), 2));
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat) {
        clean();
        double minDist = 1000.0;
        Long minVertex = (long) 0;
        Node vertexOutOfNoWhere = new Node(1111111, lon, lat);
        for (Long v : vertices()) {
            double dist = distance(vertexOutOfNoWhere, nodes.get(v));
            if (dist < minDist) {
                minDist = dist;
                minVertex = v;
            }
        }
        return minVertex;
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        return nodes.get(v).lon();
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        return nodes.get(v).lat();
    }
}
