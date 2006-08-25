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
package org.netbeans.api.visual.widget.general;

import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.*;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class ListWidget extends Widget {

    private Widget header;
    private ImageWidget imageWidget;
    private LabelWidget labelWidget;

    public ListWidget (Scene scene) {
        super (scene);

        LookFeel lookFeel = scene.getLookFeel ();
        setOpaque (true);
        setBackground (lookFeel.getBackground ());
        setBorder (BorderFactory.createLineBorder ());
        setLayout (LayoutFactory.createVerticalLayout ());

        header = new Widget (scene);
        header.setLayout (LayoutFactory.createHorizontalLayout (LayoutFactory.SerialAlignment.CENTER, 0));
        header.addChild (imageWidget = new ImageWidget (scene));
        header.addChild (labelWidget = new LabelWidget (scene));
        addChild (header);

        addChild (new SeparatorWidget (scene, SeparatorWidget.Orientation.HORIZONTAL));

        setState (ObjectState.createNormal ());
    }

    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        LookFeel lookFeel = getScene ().getLookFeel ();
        header.setBorder (BorderFactory.createCompositeBorder (BorderFactory.createEmptyBorder (2), lookFeel.getBorder (state)));
        labelWidget.setForeground (lookFeel.getForeground (state));
    }

    public void setImage (Image image) {
        imageWidget.setImage (image);
    }

    public void setLabel (String label) {
        labelWidget.setLabel (label);
    }

    public ImageWidget getImageWidget () {
        return imageWidget;
    }

    public LabelWidget getLabelWidget () {
        return labelWidget;
    }

}
