package apichanges;

import framework.VisualTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Test for issue #98307 - Widget.paintBorder method added
 * @author David Kaspar
 */
public class WidgetPaintBorderTest extends VisualTestCase {

    public WidgetPaintBorderTest (String s) {
        super (s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("WidgetPaintBorderTestSuite");
        suite.addTestSuite(WidgetPaintBorderTest.class);
        return suite;
    }

    public void testPaintWidgetBorder () {
        Scene scene = new Scene ();
        MyWidget widget = new MyWidget (scene);
        scene.addChild (widget);
        takeOneTimeSnapshot (scene, 10, 10);
        assertTrue ("Widget border is not painted", widget.borderPainted);
    }

    private static class MyWidget extends Widget {

        private boolean borderPainted = false;

        public MyWidget (Scene scene) {
            super (scene);
        }

        protected void paintBorder () {
            borderPainted = true;
            super.paintBorder ();
        }

    }

}
