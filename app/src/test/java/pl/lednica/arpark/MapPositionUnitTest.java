package pl.lednica.arpark;

import org.junit.Test;

import pl.lednica.arpark.helpers.MapPosition;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MapPositionUnitTest {


    @Test
    public void mapPosition_isCorrect() throws Exception {
        double xStart = 17.374663 ;
        double xEnd = 17.380178;
        double yStart =  52.529734 ;
        double yEnd = 52.525217;
        MapPosition pos = new MapPosition(17.377378, 52.5263);
        assertEquals(210, pos.xOut);
        assertEquals(345, pos.yOut);

        pos =  new MapPosition(17.37759, 52.52590);
        assertEquals(235, pos.xOut);
        assertEquals(395,pos.yOut);



    }
}