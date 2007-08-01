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
    
    public javax.swing.border.Border getSwingBorder () {
        return swingBorder;
    }

}
