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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * This interface decorates of a align-with move action.
 *
 * @author David Kaspar
 */
public interface AlignWithMoveDecorator {

    /**
     * Creates a connection widget that will be used as a alignment lines for align-with effect.
     * @param scene the scene where the widget is going to be used
     * @return the connection widget
     */
    ConnectionWidget createLineWidget (Scene scene);

}
