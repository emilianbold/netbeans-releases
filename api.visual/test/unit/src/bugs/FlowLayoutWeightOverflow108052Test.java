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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package bugs;

import framework.VisualTestCase;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LayerWidget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class FlowLayoutWeightOverflow108052Test extends VisualTestCase {

    public FlowLayoutWeightOverflow108052Test (String testName) {
        super (testName);
    }

    public void testFlowLayoutWeightOverflow () {
        Scene scene = new Scene ();
        LayerWidget layer = new LayerWidget (scene);
        layer.setMinimumSize (new Dimension (300, 200));
        scene.addChild (layer);

        Widget vbox = new Widget (scene);
        vbox.setBorder (BorderFactory.createLineBorder (1, Color.BLACK));
        vbox.setLayout (LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.JUSTIFY, 0));
        layer.addChild (vbox);

        Widget hbox1 = new Widget (scene);
        hbox1.setBorder (BorderFactory.createLineBorder (1, Color.BLUE));
        hbox1.setLayout (LayoutFactory.createHorizontalFlowLayout ());
        vbox.addChild (hbox1);

        Widget item1 = new LabelWidget (scene, "Item1");
        item1.setBorder (BorderFactory.createLineBorder (1, Color.GREEN));
        hbox1.addChild (item1);

        Widget item2 = new LabelWidget (scene, "Item2");
        item2.setBorder (BorderFactory.createLineBorder (1, Color.YELLOW));
        hbox1.addChild (item2, 1000);

        Widget item3 = new LabelWidget (scene, "Item3");
        item3.setBorder (BorderFactory.createLineBorder (1, Color.RED));
        hbox1.addChild (item3);

        Widget hbox2 = new Widget (scene);
        hbox2.setBorder (BorderFactory.createLineBorder (1, Color.BLUE));
        hbox2.setPreferredSize (new Dimension (200, 20));
        vbox.addChild (hbox2);

        assertScene (scene, Color.WHITE, new Rectangle (-5, -5, 210, 100));
    }

}
