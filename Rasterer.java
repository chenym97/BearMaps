import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.

    private static final double ROOT_LONDPP = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / 256;

    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public Rasterer(String imgRoot) {
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        //System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        double lrlon = params.get("lrlon");
        double ullon = params.get("ullon");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");
        double w = params.get("w");
        double h = params.get("h");
        if (!isValidate(lrlon, ullon, ullat, lrlat)) {
            results.put("render_grid", null);
            results.put("raster_ul_lon", null);
            results.put("raster_ul_lat", null);
            results.put("raster_lr_lon", null);
            results.put("raster_lr_lat", null);
            results.put("depth", null);
            results.put("query_success", false);
            return results;
        }
        int depth = getDepth(ullon, lrlon, w);
        int[] cornerIndex = getCornerIndex(ullon, ullat, lrlon, lrlat, depth);
        double[] cornerCoordinate = getCornerCoordinate(cornerIndex, depth);
        String[][] imageMatrix = getImageMatrix(cornerIndex, depth);
        results.put("render_grid", imageMatrix);
        results.put("raster_ul_lon", cornerCoordinate[0]);
        results.put("raster_ul_lat", cornerCoordinate[1]);
        results.put("raster_lr_lon", cornerCoordinate[2]);
        results.put("raster_lr_lat", cornerCoordinate[3]);
        results.put("depth", depth);
        results.put("query_success", true);
        return results;
    }

    private boolean isValidate(double lrlon, double ullon, double ullat, double lrlat) {
        return !(lrlon <= MapServer.ROOT_ULLON || ullon >= MapServer.ROOT_LRLON
                || ullat <= MapServer.ROOT_LRLAT || lrlat >= MapServer.ROOT_ULLAT
                || lrlon <= ullon || lrlat >= ullat);
    }

    private int getDepth(double ullon, double lrlon, double w) {
        double queryBoxLonDPP = (lrlon - ullon) / w;
        int depth = 0;
        double tempLonDPP = ROOT_LONDPP;
        while (tempLonDPP > queryBoxLonDPP) {
            depth += 1;
            if (depth == 7) {
                break;
            }
            tempLonDPP /= 2;
        }
        return depth;
    }

    private int[] getCornerIndex(double ullon, double ullat, double lrlon, double lrlat,
                                 int depth) {
        int[] cornerIndex = new int[4];
        double lonTileWidth = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / Math.pow(2, depth);
        double latTileWidth = (MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / Math.pow(2, depth);
        if (ullon < MapServer.ROOT_ULLON) {
            cornerIndex[0] = 0;
        } else {
            cornerIndex[0] = (int) Math.floor((ullon - MapServer.ROOT_ULLON) / lonTileWidth);
        }
        if (ullat > MapServer.ROOT_ULLAT) {
            cornerIndex[1] = 0;
        } else {
            cornerIndex[1] = (int) Math.floor((MapServer.ROOT_ULLAT - ullat) / latTileWidth);
        }
        if (lrlon > MapServer.ROOT_LRLON) {
            cornerIndex[2] = (int) Math.pow(2, depth) - 1;
        } else {
            cornerIndex[2] = (int) Math.ceil((lrlon - MapServer.ROOT_ULLON) / lonTileWidth) - 1;
        }
        if (lrlat < MapServer.ROOT_LRLAT) {
            cornerIndex[3] = (int) Math.pow(2, depth) - 1;
        } else {
            cornerIndex[3] = (int) Math.ceil((MapServer.ROOT_ULLAT - lrlat) / latTileWidth) - 1;
        }
        return cornerIndex;
    }

    private double[] getCornerCoordinate(int[] cornerIndex, int depth) {
        double lonTileWidth = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / Math.pow(2, depth);
        double latTileWidth = (MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / Math.pow(2, depth);
        double[] cornerCoordinate = new double[4];
        cornerCoordinate[0] = MapServer.ROOT_ULLON + cornerIndex[0] * lonTileWidth;
        cornerCoordinate[1] = MapServer.ROOT_ULLAT - cornerIndex[1] * latTileWidth;
        cornerCoordinate[2] = MapServer.ROOT_ULLON + (cornerIndex[2] + 1) * lonTileWidth;
        cornerCoordinate[3] = MapServer.ROOT_ULLAT - (cornerIndex[3] + 1) * latTileWidth;
        return cornerCoordinate;
    }

    private String convertToString(int row, int col, int depth) {
        int rowToBinary = Integer.parseInt(Integer.toBinaryString(row));
        int colToBinary = Integer.parseInt(Integer.toBinaryString(col));
        String allOnes = "";
        for (int i = 0; i < depth; i++) {
            allOnes = allOnes + "1";
        }
        int allOnesInt = Integer.parseInt(allOnes);
        int resultInt = rowToBinary * 2 + colToBinary + allOnesInt;
        return "img/" + Integer.toString(resultInt) + ".png";
    }

    private String[][] getImageMatrix(int[] cornerIndex, int depth) {
        int rowSize = cornerIndex[3] - cornerIndex[1] + 1;
        int colSize = cornerIndex[2] - cornerIndex[0] + 1;
        String[][] imageMatrix = new String[rowSize][colSize];
        if (depth == 0) {
            imageMatrix[0][0] = "img/root.png";
            return imageMatrix;
        }
        for (int i = cornerIndex[1]; i <= cornerIndex[3]; i++) {
            for (int j = cornerIndex[0]; j <= cornerIndex[2]; j++) {
                imageMatrix[i - cornerIndex[1]][j - cornerIndex[0]] = convertToString(i, j, depth);
            }
        }
        return imageMatrix;
    }
}
