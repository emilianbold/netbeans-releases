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

import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.WidgetState;
import org.netbeans.api.visual.laf.LookFeel;

/**
 * @author David Kaspar
 */
public class ListItemWidget extends LabelWidget {

    public ListItemWidget (Scene scene) {
        super (scene);

        setState (WidgetState.NORMAL);
    }

    public void setState (WidgetState state) {
        super.setState (state);
        LookFeel lookFeel = getScene ().getLookFeel ();
        setBorder (lookFeel.getMiniBorder (state));
        setForeground (lookFeel.getForeground (state));
    }

}
