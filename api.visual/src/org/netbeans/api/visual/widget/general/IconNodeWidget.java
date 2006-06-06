package org.netbeans.api.visual.widget.general;

import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.layout.SerialLayout;
import org.netbeans.api.visual.widget.*;
import org.netbeans.api.visual.model.ObjectState;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class IconNodeWidget extends Widget {

    private ImageWidget imageWidget;
    private LabelWidget labelWidget;

    public IconNodeWidget (Scene scene) {
        this (scene, SerialLayout.Orientation.VERTICAL);
    }

    public IconNodeWidget (Scene scene, SerialLayout.Orientation orientation) {
        super (scene);
        LookFeel lookFeel = getScene ().getLookFeel ();
        setLayout (new SerialLayout (orientation, SerialLayout.Alignment.CENTER, - lookFeel.getMargin () + 1));

        imageWidget = new ImageWidget (scene);
        addChild (imageWidget);

        labelWidget = new LabelWidget (scene);
        labelWidget.setFont (scene.getDefaultFont ().deriveFont (14.0f));
        addChild (labelWidget);

        setState (ObjectState.NORMAL);
    }

    public void setState (ObjectState state) {
        super.setState (state);
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

}
