/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
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
