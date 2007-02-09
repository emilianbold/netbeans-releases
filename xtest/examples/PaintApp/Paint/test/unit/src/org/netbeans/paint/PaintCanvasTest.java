package org.netbeans.paint;

import org.netbeans.junit.*;
import org.netbeans.paint.PaintCanvas;

public class PaintCanvasTest extends NbTestCase {
    
    public PaintCanvasTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(PaintCanvasTest.class);
        return suite;
    }
    
    public void testSetDiam() {
        PaintCanvas paintCanvas = new PaintCanvas();
        paintCanvas.setDiam(10);
        assertEquals("Diam should be set.", 10, paintCanvas.getDiam());
    }
}
