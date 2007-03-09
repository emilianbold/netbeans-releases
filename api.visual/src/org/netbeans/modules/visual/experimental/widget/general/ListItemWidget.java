package org.netbeans.modules.visual.experimental.widget.general;

import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.border.BorderFactory;

/**
 * This class represents a general list item widget. Right now it presented as a label.
 *
 * @author David Kaspar
 */
public class ListItemWidget extends LabelWidget {

    /**
     * Creates a list item widget.
     * @param scene the scene
     */
    public ListItemWidget (Scene scene) {
        super (scene);

        setState (ObjectState.createNormal ());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        LookFeel lookFeel = getScene ().getLookFeel ();
        setBorder (BorderFactory.createCompositeBorder (BorderFactory.createEmptyBorder (8, 2), lookFeel.getMiniBorder (state)));
        setForeground (lookFeel.getForeground (state));
    }

}
