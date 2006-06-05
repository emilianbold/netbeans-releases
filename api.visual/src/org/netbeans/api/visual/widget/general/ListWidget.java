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

import org.netbeans.api.visual.border.LineBorder;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.layout.SerialLayout;
import org.netbeans.api.visual.widget.*;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class ListWidget extends Widget {

    private Widget header;
    private ImageWidget imageWidget;
    private LabelWidget labelWidget;
    private Widget itemsWidget;

    public ListWidget (Scene scene) {
        super (scene);

        LookFeel lookFeel = scene.getLookFeel ();
        setOpaque (true);
        setBackground (lookFeel.getBackground ());
        setBorder (new LineBorder (1));
        setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL));

        header = new Widget (scene);
        header.setLayout (new SerialLayout (SerialLayout.Orientation.HORIZONTAL, SerialLayout.Alignment.CENTER, 0));
        header.addChild (imageWidget = new ImageWidget (scene));
        header.addChild (labelWidget = new LabelWidget (scene));
        addChild (header);

        addChild (new SeparatorWidget (scene, SeparatorWidget.Orientation.HORIZONTAL));

        setState (WidgetState.NORMAL);
    }

    public void setState (WidgetState state) {
        super.setState (state);
        LookFeel lookFeel = getScene ().getLookFeel ();
        header.setBorder (lookFeel.getBorder (state));
        header.setForeground (lookFeel.getForeground (state));
    }

    public void setImage (Image image) {
        imageWidget.setImage (image);
    }

    public void setLabel (String label) {
        labelWidget.setLabel (label);
    }

}
