package pl.lednica.arpark.helpers;

/**
 * Created by stachu on 19.09.2016.
 */
public class MapPosition {
    private final static double xStart = 17.374663 ;
    private final static double xEnd = 17.380178;
    private final static double yStart =  52.529734 ;
    private final static double yEnd = 52.525217;
    private final static int stepSize =25;
    private final static int correctionY = -90;
    private final static int correctionX = -40;
    public int xOut=0;
    public int yOut=0;
    public MapPosition(double xIn, double yIn) {
        int xSteps =  (int)(Math.round(Math.abs(xIn-xStart)/Math.abs(xStart-xEnd)*20));
        int ySteps = (int)(Math.round(Math.abs(yIn-yStart)/Math.abs(yStart-yEnd)*22));
        xOut = (xSteps * stepSize) + correctionX;
        yOut = (ySteps * stepSize) + correctionY;
    }

}
