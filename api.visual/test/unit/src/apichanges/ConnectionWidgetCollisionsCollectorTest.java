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
package apichanges;

import framework.VisualTestCase;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.router.CollisionsCollector;
import org.netbeans.api.visual.router.ConnectionWidgetCollisionsCollector;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Test for #99054 - CollisionsCollector context
 * @author David Kaspar
 */
public class ConnectionWidgetCollisionsCollectorTest extends VisualTestCase {

    public ConnectionWidgetCollisionsCollectorTest (String name) {
        super (name);
    }

    public void testCollisionsCollector () {
        Scene scene = new Scene ();

        ConnectionWidget widget = new ConnectionWidget (scene);
        widget.setSourceAnchor (AnchorFactory.createFixedAnchor (new Point (100, 100)));
        widget.setTargetAnchor (AnchorFactory.createFixedAnchor (new Point (300, 200)));
        widget.setRouter (RouterFactory.createOrthogonalSearchRouter (new CollisionsCollector() {
            public void collectCollisions (List<Rectangle> verticalCollisions, List<Rectangle> horizontalCollisions) {
                getRef ().println ("CollisionsCollector invoked");
            }
        }));
        scene.addChild (widget);

        JFrame frame = showFrame (scene);
        frame.setVisible(false);
        frame.dispose ();
        
        compareReferenceFiles ();
    }

    public void testConnectionWidgetCollisionsCollector () {
        Scene scene = new Scene ();

        final ConnectionWidget widget = new ConnectionWidget (scene);
        widget.setSourceAnchor (AnchorFactory.createFixedAnchor (new Point (100, 100)));
        widget.setTargetAnchor (AnchorFactory.createFixedAnchor (new Point (300, 200)));
        widget.setRouter (RouterFactory.createOrthogonalSearchRouter (new ConnectionWidgetCollisionsCollector () {
            public void collectCollisions (ConnectionWidget connectionWidget, List<Rectangle> verticalCollisions, List<Rectangle> horizontalCollisions) {
                getRef ().println ("ConnectionWidgetCollisionsCollector invoked - is widget valid: " + (connectionWidget == widget));
            }
        }));
        scene.addChild (widget);

        JFrame frame = showFrame (scene);
        frame.setVisible(false);
        frame.dispose ();

        compareReferenceFiles ();
    }

}
