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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This widget represents a list of glyphs rendered horizontally one after another. A glyph is a small image - usually 16x16px.
 *
 * @author David Kaspar
 */
public class VMDGlyphSetWidget extends Widget {

    /**
     * Creates a glyph set widget.
     * @param scene the scene
     */
    public VMDGlyphSetWidget (Scene scene) {
        super (scene);
        setLayout (LayoutFactory.createHorizontalLayout ());
    }

    /**
     * Sets glyphs as a list of images.
     * @param glyphs the list of images used as glyphs
     */
    public void setGlyphs (List<Image> glyphs) {
        List<Widget> children = new ArrayList<Widget> (getChildren ());
        for (Widget widget : children)
            removeChild (widget);
        if (glyphs != null)
            for (Image glyph : glyphs) {
                ImageWidget imageWidget = new ImageWidget (getScene ());
                imageWidget.setImage (glyph);
                addChild (imageWidget);
            }
    }

}
