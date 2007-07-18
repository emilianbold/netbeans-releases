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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class FlowLayout105400Test extends VisualTestCase {

    public FlowLayout105400Test (String testName) {
        super (testName);
    }

    public void testFlowLayoutInsets () {
        Scene scene = new Scene ();
        Widget parent = new Widget (scene);
        parent.setBorder (BorderFactory.createResizeBorder (10));
        parent.setLayout (LayoutFactory.createVerticalFlowLayout ());
        scene.addChild (parent);

        Widget child = new Widget (scene);
        child.setBackground (Color.BLUE);
        child.setOpaque (true);
        child.setPreferredBounds (new Rectangle (-50, -30, 30, 20));
        parent.addChild (child);

        assertScene (scene, Color.WHITE, new Rectangle (-1, -1, 52, 42));
    }

}
