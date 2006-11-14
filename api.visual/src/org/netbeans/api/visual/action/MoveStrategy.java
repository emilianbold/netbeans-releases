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

import java.awt.*;

/**
 * This interfaces provides a movement strategy.
 *
 * @author David Kaspar
 */
public interface MoveStrategy {

    /**
     * Called after an user suggests a new location and before the suggested location is stored to a specified widget.
     * This allows to manipulate with a suggested location to perform snap-to-grid, locked-axis on any other movement strategy.
     * @param widget the moved widget
     * @param originalLocation the original location specified by the MoveProvider.getOriginalLocation method
     * @param suggestedLocation the location suggested by an user (usually by a mouse cursor position)
     * @return the new (optional modified) location processed by the strategy
     */
    Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation);

}
