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
package org.netbeans.api.visual.anchor;

import org.netbeans.modules.visual.anchor.ImageAnchorShape;

import java.awt.*;

/**
 * The factory class of all built-in anchor shapes.
 * The instances of all built-in anchor shapes can be used multiple connection widgets.
 *
 * @author David Kaspar
 */
public class AnchorShapeFactory {

    private AnchorShapeFactory () {
    }

    /**
     * Creates an image anchor shape.
     * @param image the image
     * @return the anchor shape
     */
    public static AnchorShape createImageAnchorShape (Image image) {
        return new ImageAnchorShape (image);
    }

}
