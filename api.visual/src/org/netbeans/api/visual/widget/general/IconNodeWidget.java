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
 * @author David Kaspar
 */
public class IconNodeWidget extends Widget {

    public static enum TextOrientation {

        BOTTOM_CENTER, RIGHT_CENTER

    }

    private ImageWidget imageWidget;
    private LabelWidget labelWidget;

    public IconNodeWidget (Scene scene) {
        this (scene, TextOrientation.BOTTOM_CENTER);
    }

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

        setState (ObjectState.NORMAL);
    }

    public void notifyStateChanged (ObjectState state) {
        LookFeel lookFeel = getScene ().getLookFeel ();
        labelWidget.setBorder (lookFeel.getBorder (state));
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
