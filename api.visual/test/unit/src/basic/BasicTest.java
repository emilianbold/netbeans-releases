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
package basic;

import framework.VisualTestCase;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
public class BasicTest extends VisualTestCase {
    
    public BasicTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("FirstTimeTestSuite");
        suite.addTestSuite(BasicTest.class);
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testShow () {
        Scene scene = new Scene ();
        
        LayerWidget mainLayer = new LayerWidget (scene);
        scene.addChild(mainLayer);
        
        Widget w1 = new Widget (scene);
        w1.setBorder (BorderFactory.createLineBorder ());
        w1.setPreferredLocation (new Point (100, 100));
        w1.setPreferredSize (new Dimension (40, 20));
        mainLayer.addChild(w1);
        
        Widget w2 = new Widget (scene);
        w2.setBorder (BorderFactory.createLineBorder ());
        w2.setPreferredLocation (new Point (200, 100));
        w2.setPreferredSize (new Dimension (40, 20));
        mainLayer.addChild(w2);
        
        LayerWidget connLayer = new LayerWidget (scene);
        scene.addChild(connLayer);
        
        ConnectionWidget conn = new ConnectionWidget(scene);
        conn.setSourceAnchor(AnchorFactory.createRectangularAnchor(w1));
        conn.setTargetAnchor(AnchorFactory.createRectangularAnchor(w2));
        connLayer.addChild(conn);
        
        assertScene (scene, Color.WHITE,
                new Rectangle (99, 99, 42, 22),
                new Rectangle (199, 99, 42, 22),
                new Rectangle (138, 108, 64, 4)
        );
    }

}
