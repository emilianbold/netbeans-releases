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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;

/**
 * This class represents a pin widget in the VMD visualization style.
 * The pin widget consists of a name and a glyph set.
 *
 * @author David Kaspar
 */
public class VMDPinWidget extends Widget {

    private VMDColorScheme scheme;

    private LabelWidget nameWidget;
    private VMDGlyphSetWidget glyphsWidget;
    private VMDNodeAnchor anchor;

    /**
     * Creates a pin widget.
     * @param scene the scene
     */
    public VMDPinWidget (Scene scene) {
        this (scene, VMDFactory.getOriginalScheme ());
    }

    /**
     * Creates a pin widget with a specific color scheme.
     * @param scene the scene
     * @param scheme the color scheme
     */
    public VMDPinWidget (Scene scene, VMDColorScheme scheme) {
        super (scene);
        assert scheme != null;
        this.scheme = scheme;

        setLayout (LayoutFactory.createHorizontalFlowLayout (LayoutFactory.SerialAlignment.CENTER, 8));
        addChild (nameWidget = new LabelWidget (scene));
        addChild (glyphsWidget = new VMDGlyphSetWidget (scene));

        scheme.installUI (this);
        notifyStateChanged (ObjectState.createNormal (), ObjectState.createNormal ());
    }

    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
        scheme.updateUI (this, previousState, state);
    }

    /**
     * Returns a pin name widget.
     * @return the pin name widget
     */
    public Widget getPinNameWidget () {
        return nameWidget;
    }

    /**
     * Sets a pin name.
     * @param name the pin name
     */
    public void setPinName (String name) {
        nameWidget.setLabel (name);
    }

    /**
     * Returns a pin name.
     * @return the pin name
     */
    public String getPinName () {
        return nameWidget.getLabel();
    }

    /**
     * Sets pin glyphs.
     * @param glyphs the list of images
     */
    public void setGlyphs (List<Image> glyphs) {
        glyphsWidget.setGlyphs (glyphs);
    }

    /**
     * Sets all pin properties at once.
     * @param name the pin name
     * @param glyphs the pin glyphs
     */
    public void setProperties (String name, List<Image> glyphs) {
        setPinName (name);
        glyphsWidget.setGlyphs (glyphs);
    }

    /**
     * Creates a horizontally oriented anchor similar to VMDNodeWidget.createAnchorPin
     * @return the anchor
     */
    public Anchor createAnchor () {
        if (anchor == null)
            anchor = new VMDNodeAnchor (this, false);
        return anchor;
    }

}
