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
