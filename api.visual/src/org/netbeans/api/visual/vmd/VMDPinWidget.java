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

import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;

/**
 * @author David Kaspar
 */
public class VMDPinWidget extends Widget {

    private LabelWidget nameWidget;
    private VMDGlyphSetWidget glyphsWidget;

    public VMDPinWidget (Scene scene) {
        super (scene);

        setLayout (LayoutFactory.createHorizontalLayout (LayoutFactory.SerialAlignment.CENTER, 8));
        addChild (nameWidget = new LabelWidget (scene));
        addChild (glyphsWidget = new VMDGlyphSetWidget (scene));

        notifyStateChanged (ObjectState.NORMAL, ObjectState.NORMAL);
    }

    protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
        LookFeel lookFeel = getScene ().getLookFeel ();
        setBorder (BorderFactory.createCompositeBorder (BorderFactory.createEmptyBorder (8, 2), lookFeel.getMiniBorder (state)));
        setForeground (lookFeel.getForeground (state));
    }

    public Widget getPinNameWidget () {
        return nameWidget;
    }

    public void setPinName (String name) {
        nameWidget.setLabel (name);
    }

    public String getPinName () {
        return nameWidget.getLabel();
    }

    public void setGlyphs (List<Image> glyphs) {
        glyphsWidget.setGlyphs (glyphs);
    }

    public void setProperties (String name, List<Image> glyphs) {
        setPinName (name);
        glyphsWidget.setGlyphs (glyphs);
    }

}
