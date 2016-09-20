package pl.lednica.arpark.helpers;

/**
 * Created by stachu on 19.09.2016.
 */
public class MapPosition {
    double xStart = 17.374663 ;
    double xEnd = 17.380178;
    double yStart =  52.529734 ;
    double yEnd = 52.525217;
    public int xOut=0;
    public int yOut=0;
    int stepSize =25;
    int correctionY = -90;
    int correctionX = -40;
    public MapPosition(double xIn, double yIn) {

                        // szerokość kroku
        int xSteps =  (int)(Math.round(Math.abs(xIn-xStart)/Math.abs(xStart-xEnd)*20));
        int ySteps = (int)(Math.round(Math.abs(yIn-yStart)/Math.abs(yStart-yEnd)*22));
        xOut = (xSteps * stepSize) + correctionX;
        yOut = (ySteps * stepSize) + correctionY;
    }

}
