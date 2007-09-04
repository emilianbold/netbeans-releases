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
 *
 */

package org.netbeans.modules.vmd.midpnb.screen.display;

import javax.swing.*;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class SVGImageComponent extends JComponent {

    private SVGImage image;

    public SVGImageComponent () {
        setOpaque (false);
    }


    public SVGImage getImage () {
        return image;
    }

    public void setImage (SVGImage image) {
        SVGImage old = this.image;
        if (old == image)
            return;
        this.image = image;
        firePropertyChange ("svg-image", old, image); // NOI18N
        repaint ();
    }


    @Override
    public void paint (Graphics g) {
        super.paint (g);
        if (image != null) {
            image.setViewportHeight (getHeight ());
            image.setViewportWidth (getWidth ());
            ScalableGraphics gr = ScalableGraphics.createInstance ();
            gr.bindTarget (g);
            gr.render (0, 0, image);
            gr.releaseTarget ();
        }
    }
}
