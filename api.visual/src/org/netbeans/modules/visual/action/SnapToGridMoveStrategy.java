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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class SnapToGridMoveStrategy implements MoveStrategy {

    private int horizontalGridSize;
    private int verticalGridSize;

    public SnapToGridMoveStrategy (int horizontalGridSize, int verticalGridSize) {
        assert horizontalGridSize > 0 && verticalGridSize > 0;
        this.horizontalGridSize = horizontalGridSize;
        this.verticalGridSize = verticalGridSize;
    }

    public Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation) {
        return new Point (suggestedLocation.x - suggestedLocation.x % horizontalGridSize, suggestedLocation.y - suggestedLocation.y % verticalGridSize);
    }

}
