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

package apichanges;

import framework.VisualTestCase;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.anchor.Anchor;

import javax.swing.*;
import java.awt.*;

/**
 * Test for #111987 - VMDNodeAnchor recalculates unnecessarily
 * @author David Kaspar
 */
public class AnchorNotificationTest extends VisualTestCase {

    public AnchorNotificationTest (String testName) {
        super (testName);
    }

    public void testNotify () {
        StringBuffer log = new StringBuffer ();
        Scene scene = new Scene ();

        Widget w = new Widget (scene);
        scene.addChild (w);

        ConnectionWidget c = new ConnectionWidget (scene);
        scene.addChild (c);
        TestAnchor testAnchor = new TestAnchor (w, log);
        c.setSourceAnchor (testAnchor);
        c.setTargetAnchor (testAnchor);

        JFrame frame = showFrame (scene);

        c.setSourceAnchor (null);
        c.setTargetAnchor (null);
        scene.validate ();

        frame.setVisible (false);
        frame.dispose ();

        assertEquals (log.toString (),
                "notifyEntryAdded\n" +
                "notifyUsed\n" +
                "notifyRevalidate\n" +
                "notifyEntryAdded\n" +
                "notifyRevalidate\n" +
                "notifyRevalidate\n" +
                "compute\n" +
                "compute\n" +
                "notifyEntryRemoved\n" +
                "notifyRevalidate\n" +
                "notifyEntryRemoved\n" +
                "notifyUnused\n" +
                "notifyRevalidate\n"
                );
    }

    private class TestAnchor extends Anchor {

        private StringBuffer log;

        protected TestAnchor (Widget relatedWidget, StringBuffer log) {
            super (relatedWidget);
            this.log = log;
        }

        protected void notifyEntryAdded (Entry entry) {
            log.append ("notifyEntryAdded\n");
        }

        protected void notifyEntryRemoved (Entry entry) {
            log.append ("notifyEntryRemoved\n");
        }

        protected void notifyUsed () {
            log.append ("notifyUsed\n");
        }

        protected void notifyUnused () {
            log.append ("notifyUnused\n");
        }

        protected void notifyRevalidate () {
            log.append ("notifyRevalidate\n");
        }

        public Result compute (Entry entry) {
            log.append ("compute\n");
            return new Result (new Point (0, 0), DIRECTION_ANY);
        }
    }

}
