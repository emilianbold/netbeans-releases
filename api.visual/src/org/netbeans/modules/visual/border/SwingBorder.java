/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.border;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.border.Border;

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - Scene.getView can return null before Scene.createView is called
public final class SwingBorder implements Border {

    private Scene scene;
    private javax.swing.border.Border swingBorder;

    public SwingBorder (Scene scene, javax.swing.border.Border swingBorder) {
        assert scene != null  &&  swingBorder != null;
        this.scene = scene;
        this.swingBorder = swingBorder;
    }

    public Insets getInsets () {
        return swingBorder.getBorderInsets (scene.getView ());
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        swingBorder.paintBorder (scene.getView (), gr, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean isOpaque () {
        return false;
    }

}
