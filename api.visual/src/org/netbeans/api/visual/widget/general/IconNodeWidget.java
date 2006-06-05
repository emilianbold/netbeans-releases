package org.netbeans.api.visual.widget.general;

import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.layout.SerialLayout;
import org.netbeans.api.visual.widget.*;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class IconNodeWidget extends Widget {

    private ImageWidget imageWidget;
    private LabelWidget labelWidget;

    public IconNodeWidget (Scene scene) {
        super (scene);
        LookFeel lookFeel = getScene ().getLookFeel ();
        setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL, SerialLayout.Alignment.CENTER, - lookFeel.getMargin () + 1));

        imageWidget = new ImageWidget (scene);
        addChild (imageWidget);

        labelWidget = new LabelWidget (scene);
        labelWidget.setFont (scene.getDefaultFont ().deriveFont (14.0f));
        addChild (labelWidget);

        setState (WidgetState.NORMAL);
    }

    public void setState (WidgetState state) {
        super.setState (state);
        LookFeel lookFeel = getScene ().getLookFeel ();
        setBorder (lookFeel.getBorder (state));
        setForeground (lookFeel.getForeground (state));
    }

    public void setImage (Image image) {
        imageWidget.setImage (image);
    }

    public void setLabel (String label) {
        labelWidget.setLabel (label);
    }

}
