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

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.action.MoveControlPointProvider;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex
 */
public final class FreeMoveControlPointProvider implements MoveControlPointProvider {

    public List<Point> locationSuggested(ConnectionWidget connectionWidget, int index, Point suggestedLocation) {
        List<Point> controlPoints = connectionWidget.getControlPoints();
        int cpSize=controlPoints.size()-1;
        ArrayList<Point> list = new ArrayList<Point> (controlPoints);
        if (index <= 0 || index >= cpSize)return null;
        if(index==1)list.set(0,connectionWidget.getSourceAnchor().compute(connectionWidget.getSourceAnchorEntry()).getAnchorSceneLocation());
        if(index==cpSize - 1)
            list.set(cpSize,connectionWidget.getTargetAnchor().compute(connectionWidget.getTargetAnchorEntry()).getAnchorSceneLocation());
        list.set(index, suggestedLocation);
        return list;
    }
    
}
