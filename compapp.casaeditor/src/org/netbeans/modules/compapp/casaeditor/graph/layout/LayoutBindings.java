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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.graph.layout;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.*;

/**
 * @author Josh Sandusky
 */
public final class LayoutBindings extends CustomizablePersistLayout {
    
    private static final int Y_SPACING = 20;
    
    
    public void layout (Widget widget) {
        
        if (widget == null) {
            return;
        }
        
        // First determine the relative Y ordering.
        List<CasaNodeWidget> orderedNodeList = new ArrayList<CasaNodeWidget>();
        for (Widget child : widget.getChildren()) {
            if (child instanceof CasaNodeWidget) {
                orderedNodeList.add((CasaNodeWidget) child);
            }
        }
        Collections.sort(orderedNodeList, new YOrderComparator(
                (CasaModelGraphScene) widget.getScene()));
        
        final int parentWidth  = (int) widget.getBounds().getWidth();

        int nextYStart = ((CasaRegionWidget) widget).getLabelYOffset() + Y_SPACING;
        for (CasaNodeWidget child : orderedNodeList) {
            int x = parentWidth - child.getBounds().width;
            int y = nextYStart;
            nextYStart += child.getEntireBounds().height + Y_SPACING;
            moveWidget(child, new Point(x, y));
        }
        
        widget.getScene().validate();
    }
}
