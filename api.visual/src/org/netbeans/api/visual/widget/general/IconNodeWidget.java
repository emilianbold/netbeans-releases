package org.netbeans.api.visual.widget.general;

import org.netbeans.api.visual.widget.*;
import org.netbeans.api.visual.layout.SerialLayout;
import org.netbeans.api.visual.border.*;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class IconNodeWidget extends Widget {

    private static final Color COLOR = new Color (0x447BCD);
    private static final int ARC = 4;

    private static final Border BORDER_NORMAL = new EmptyBorder (ARC, ARC);
    private static final Border BORDER_HOVERED = new RoundedBorder (ARC, ARC, true, true, COLOR.brighter ());
    private static final Border BORDER_FOCUSED = new RoundedBorder (ARC, ARC, false, true, COLOR);
    private static final Border BORDER_SELECTED = new RoundedBorder (ARC, ARC, true, true, COLOR);
    private ImageWidget imageWidget;
    private LabelWidget labelWidget;

    public IconNodeWidget (Scene scene) {
        super (scene);
        setLayout (new SerialLayout (SerialLayout.Orientation.VERTICAL, SerialLayout.Alignment.CENTER, - ARC));

        imageWidget = new ImageWidget (scene);
        addChild (imageWidget);

        labelWidget = new LabelWidget (scene);
        labelWidget.setFont (scene.getDefaultFont ().deriveFont (Font.BOLD).deriveFont (14.0f));
        addChild (labelWidget);

        setState (WidgetState.NORMAL);
    }

    public void setState (WidgetState state) {
        super.setState (state);
        if (state.isHovered ()) {
            labelWidget.setBorder (BORDER_HOVERED);
            labelWidget.setForeground (Color.BLACK);
        } else if (state.isSelected ()) {
            labelWidget.setBorder (BORDER_SELECTED);
            labelWidget.setForeground (Color.WHITE);
        } else if (state.isFocused ()) {
            labelWidget.setBorder (BORDER_FOCUSED);
            labelWidget.setForeground (Color.BLACK);
        } else {
            labelWidget.setBorder (BORDER_NORMAL);
            labelWidget.setForeground (Color.BLACK);
        }
    }

    public void setImage (Image image) {
        imageWidget.setImage (image);
    }

    public void setLabel (String label) {
        labelWidget.setText (label);
    }

}
