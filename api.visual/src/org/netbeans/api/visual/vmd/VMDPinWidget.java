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

import org.netbeans.api.visual.action.MouseHoverAction;
import org.netbeans.api.visual.layout.SerialLayout;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class VMDPinWidget extends Widget {

    private ImageWidget imageWidget;
    private LabelWidget nameWidget;
    private LabelWidget typeWidget;

    public VMDPinWidget (Scene scene, MouseHoverAction hoverAction) {
        super (scene);

        setLayout (new SerialLayout (SerialLayout.Orientation.HORIZONTAL, SerialLayout.Alignment.JUSTIFY, 4));

//        setBorder (new LineBorder (1, Color.GRAY));
        getActions ().addAction (hoverAction);

//        Widget innerWidget = new Widget (scene);
//        innerWidget.setBorder (new EmptyBorder (4));
//        innerWidget.setLayout (new SerialLayout (SerialLayout.Orientation.HORIZONTAL, SerialLayout.Alignment.JUSTIFY, 4));
//        addChild (innerWidget);

        imageWidget = new ImageWidget (scene);
        addChild (imageWidget);

        nameWidget = new LabelWidget (scene);
//        innerWidget.addChild (nameWidget);
        addChild (nameWidget);

        typeWidget = new LabelWidget (scene);
        typeWidget.setForeground (Color.GRAY);
//        innerWidget.addChild (typeWidget);
//        addChild (typeWidget);
    }

    public void setPinImage (Image image) {
        imageWidget.setImage (image);
    }

    public void setPinName (String name) {
        nameWidget.setLabel (name);
    }

    public void setPinType (String type) {
        typeWidget.setLabel (" [" + type + "]");
    }

}
