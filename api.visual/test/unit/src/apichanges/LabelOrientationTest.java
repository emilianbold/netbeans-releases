package apichanges;

import framework.VisualTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Test for #98641 - Missing vertical labels
 * @author David Kaspar
 */
public class LabelOrientationTest extends VisualTestCase {

    public LabelOrientationTest (String testName) {
        super (testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("LabelOrientationTestSuite");
        suite.addTestSuite(LabelOrientationTest.class);
        return suite;
    }

    public void testLabelOrientations () {
        Scene scene = new Scene ();
        LayerWidget layer = new LayerWidget (scene);
        scene.addChild(layer);
        layer.addChild (new Widget (scene));

        createLabel (layer, "N O R M A L", 100, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.LEFT, LabelWidget.VerticalAlignment.BASELINE).setPreferredBounds (null);
        createLabel (layer, "R O T A T E 9 0", 100, 100, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.LEFT, LabelWidget.VerticalAlignment.BASELINE).setPreferredBounds (null);

        createLabel (layer, "NORMAL BASELINE", 200, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.BASELINE, LabelWidget.VerticalAlignment.BASELINE);
        createLabel (layer, "ROTATE90 BASELINE", 200, 300, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.BASELINE, LabelWidget.VerticalAlignment.BASELINE);

        createLabel (layer, "NORMAL LEFT,TOP", 400, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.LEFT, LabelWidget.VerticalAlignment.TOP);
        createLabel (layer, "ROTATE90 LEFT,TOP", 400, 300, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.LEFT, LabelWidget.VerticalAlignment.TOP);

        createLabel (layer, "NORMAL CENTER", 600, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.CENTER, LabelWidget.VerticalAlignment.CENTER);
        createLabel (layer, "ROTATE90 CENTER", 600, 300, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.CENTER, LabelWidget.VerticalAlignment.CENTER);

        createLabel (layer, "NORMAL RIGHT,BOTTOM", 800, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.RIGHT, LabelWidget.VerticalAlignment.BOTTOM);
        createLabel (layer, "ROTATE90 RIGHT,BOTTOM", 800, 300, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.RIGHT, LabelWidget.VerticalAlignment.BOTTOM);

        BufferedImage snapshot = takeOneTimeSnapshot (scene);
        BufferedImage clean = clearRegions (snapshot, Color.RED,
                new Rectangle (100, 80, 100, 30),
                new Rectangle (80, 0, 30, 110),

                new Rectangle (190, 90, 130, 30),
                new Rectangle (380, 70, 110, 30),
                new Rectangle (610, 150, 120, 30),
                new Rectangle (810, 230, 150, 30),

                new Rectangle (180, 270, 30, 130),
                new Rectangle (380, 270, 30, 160),
                new Rectangle (650, 290, 30, 150),
                new Rectangle (945, 275, 30, 190)

        );
        assertCleaness (testCleaness (clean, Color.WHITE, Color.YELLOW, Color.RED), snapshot, clean);
    }

    private static LabelWidget createLabel (LayerWidget layer, String label, int x, int y, LabelWidget.Orientation orientation, LabelWidget.Alignment alignment, LabelWidget.VerticalAlignment verticalAlignment) {
        LabelWidget widget = new LabelWidget (layer.getScene (), label);
        widget.setOpaque (true);
        widget.setBackground (Color.YELLOW);
        widget.setPreferredLocation (new Point (x, y));
        widget.setPreferredBounds (new Rectangle (-20, -30, 180, 180));
        widget.setOrientation (orientation);
        widget.setAlignment (alignment);
        widget.setVerticalAlignment (verticalAlignment);
        layer.addChild (widget);
        return widget;
    }

}
