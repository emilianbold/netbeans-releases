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
package org.netbeans.api.visual.widget.general;

import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This class represents a general icon node widget which is rendered as a image and a label placed to the right or bottom from the image.
 * <p>
 * WARNING: This class is meant to be redesigned later.
 *
 * @author David Kaspar
 */
public class IconNodeWidget extends Widget {

    /**
     * The text orientation specified relatively to the image
     */
    public static enum TextOrientation {

        BOTTOM_CENTER, RIGHT_CENTER

    }

    private ImageWidget imageWidget;
    private LabelWidget labelWidget;

    /**
     * Creates an icon node widget with bottom-center orientation.
     * @param scene the scene
     */
    public IconNodeWidget (Scene scene) {
        this (scene, TextOrientation.BOTTOM_CENTER);
    }

    /**
     * Creates an icon node widget with a specified orientation.
     * @param scene the scene
     * @param orientation the text orientation
     */
    public IconNodeWidget (Scene scene, TextOrientation orientation) {
        super (scene);
        LookFeel lookFeel = getScene ().getLookFeel ();

        switch (orientation) {
            case BOTTOM_CENTER:
                setLayout (LayoutFactory.createVerticalLayout (LayoutFactory.SerialAlignment.CENTER, - lookFeel.getMargin () + 1));
                break;
            case RIGHT_CENTER:
                setLayout (LayoutFactory.createHorizontalLayout (LayoutFactory.SerialAlignment.CENTER, - lookFeel.getMargin () + 1));
                break;
        }

        imageWidget = new ImageWidget (scene);
        addChild (imageWidget);

        labelWidget = new LabelWidget (scene);
        labelWidget.setFont (scene.getDefaultFont ().deriveFont (14.0f));
        addChild (labelWidget);

        setState (ObjectState.createNormal ());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        LookFeel lookFeel = getScene ().getLookFeel ();
        labelWidget.setBorder (lookFeel.getBorder (state));
        labelWidget.setForeground (lookFeel.getForeground (state));
    }

    /**
     * Sets an image.
     * @param image the image
     */
    public final void setImage (Image image) {
        imageWidget.setImage (image);
    }

    /**
     * Sets a label.
     * @param label the label
     */
    public final void setLabel (String label) {
        labelWidget.setLabel (label);
    }

    /**
     * Returns the image widget part of the icon node widget.
     * @return the image widget
     */
    public final ImageWidget getImageWidget () {
        return imageWidget;
    }

    /**
     * Returns the label widget part of the icon node widget.
     * @return the label widget
     */
    public final LabelWidget getLabelWidget () {
        return labelWidget;
    }

}
