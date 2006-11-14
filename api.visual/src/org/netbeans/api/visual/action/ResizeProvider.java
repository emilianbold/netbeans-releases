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
 * @author David Kaspar
 */
public interface ResizeProvider {

    /**
     * This enum represents a control point of a resize action.
     */
    // TODO - could be moved to ResizeStrategy interface, where it is used
    public enum ControlPoint {

        TOP_CENTER, BOTTOM_CENTER, CENTER_LEFT, CENTER_RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT

    }

    /**
     * Called to notify about the start of resizing.
     * @param widget the resizing widget
     */
    void resizingStarted (Widget widget);

    /**
     * Called to notify about the finish of resizing.
     * @param widget the resized widget
     */
    void resizingFinished (Widget widget);

}
