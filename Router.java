import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest, 
     * where the longs are node IDs.
     */

    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                                double destlon, double destlat) {
        long startId = g.closest(stlon, stlat);
        long endId = g.closest(destlon, destlat);
        HashSet<Long> evaluated = new HashSet<>();
        PriorityQueue<Node> fringe = new PriorityQueue<>();
        Node startNode = g.findNode(startId);
        startNode.setPriority(g.distance(startId, endId));
        fringe.add(startNode);
        HashMap<Long, Double> gScore = new HashMap<>();
        gScore.put(startId, 0.0);
        HashMap<Long, Long> previousNode = new HashMap<>();
        previousNode.put(startId, null);
        while (!fringe.isEmpty()) {
            Node current = fringe.poll();
            long currentId = current.id();
            if (currentId == endId) {
                break;
            }
            evaluated.add(currentId);
            for (long neighborId : g.adjacent(currentId)) {
                if (evaluated.contains(neighborId)) {
                    continue;
                }
                Node neighborNode = g.findNode(neighborId);
                double gScoreTemp = gScore.get(currentId) + g.distance(currentId, neighborId);
                if (fringe.contains(neighborNode) && gScoreTemp >= gScore.get(neighborId)) {
                    continue;
                }
                if (fringe.contains(neighborNode) && gScoreTemp < gScore.get(neighborId)) {
                    fringe.remove(neighborNode);
                }
                gScore.put(neighborId, gScoreTemp);
                previousNode.put(neighborId, currentId);
                double newPriority = gScoreTemp + g.distance(neighborId, endId);
                neighborNode.setPriority(newPriority);
                fringe.add(neighborNode);
            }
        }
        LinkedList<Long> solution = new LinkedList<>();
        Long currId = endId;
        while (currId != null) {
            solution.addFirst(currId);
            currId = previousNode.get(currId);
        }
        return solution;
    }
}
