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

/**
 * Test for issue #98307 - Widget.paintBorder method added
 * @author David Kaspar
 */
public class WidgetPaintBorderTest extends VisualTestCase {

    public WidgetPaintBorderTest (String s) {
        super (s);
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
