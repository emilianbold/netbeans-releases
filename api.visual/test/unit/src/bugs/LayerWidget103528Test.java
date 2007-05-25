package bugs;

import framework.VisualTestCase;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class LayerWidget103528Test extends VisualTestCase {

    public LayerWidget103528Test (String testName) {
        super (testName);
    }

    public void testLayerPreferredLocation () {
        Scene scene = new Scene ();

        scene.addChild (new LayerWidget (scene));

        LayerWidget layer = new LayerWidget (scene);
        layer.setPreferredLocation (new Point (100, 100));
        scene.addChild (layer);

        Widget widget = new Widget (scene);
        widget.setPreferredBounds (new Rectangle (-20, -10, 100, 50));
        widget.setOpaque (true);
        widget.setBackground (Color.RED);
        layer.addChild (widget);

        assertScene (scene, Color.WHITE, new Rectangle (80, 90, 100, 50));
    }

}
