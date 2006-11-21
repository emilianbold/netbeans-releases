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

import org.netbeans.api.visual.widget.Widget;

/**
 * This interface provides an ability for switching focus.
 *
 * @author David Kaspar
 */
public interface CycleFocusProvider {

    /**
     * Switches a focus to the previous widget/object on a scene.
     * @param widget the widget where the action was invoked
     * @return true, if switching was successful
     */
    boolean switchPreviousFocus (Widget widget);

    /**
     * Switches a focus to the next widget/object on a scene.
     * @param widget the widget where the action was invoked
     * @return true, if switching was successful
     */
    boolean switchNextFocus (Widget widget);

}
