/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.compapp.casaeditor.graph.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.*;

/**
 * @author Josh Sandusky
 */
public final class LayoutEngines extends CustomizablePersistLayout {
    
    private static final int SPACING_FROM_REGION_EDGES = 40;
    
    
    public LayoutEngines() {
        setYSpacing(60);
    }
    
    
    public void layout(Widget widget) {
        
        if (widget == null) {
            return;
        }
        CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
        
        // First determine the relative Y ordering.
        int maxWidth = 0;
        List<CasaNodeWidget> orderedNodeList = new ArrayList<CasaNodeWidget>();
        for (Widget child : widget.getChildren()) {
            if (child instanceof CasaNodeWidget) {
                orderedNodeList.add((CasaNodeWidget) child);
                if(maxWidth < child.getBounds().width) {
                    maxWidth = child.getBounds().width;
                }
            }
        }
        Collections.sort(orderedNodeList, new YOrderComparator(scene));
        
        /* Update region width to accomade the new widget */
        int parentWidth  = (int) widget.getBounds().getWidth();
        maxWidth = maxWidth + 2 * SPACING_FROM_REGION_EDGES + 20;
        if(parentWidth < maxWidth) {
            Rectangle bounds = widget.getBounds();
            bounds.width = maxWidth;
            widget.setPreferredBounds(bounds);
            parentWidth  = (int) widget.getBounds().getWidth();
        }

        CenteredFlowLayout layout = new CenteredFlowLayout(
                parentWidth, 
                SPACING_FROM_REGION_EDGES, 
                getYSpacing());
        for (CasaNodeWidget child : orderedNodeList) {
            layout.add(child);
        }
        Map<CasaNodeWidget, Rectangle> widgetMap = new HashMap<CasaNodeWidget, Rectangle>();
        layout.positionWidgets(
                
                ((CasaRegionWidget) widget).getTitleYOffset() + getYSpacing(),
                widgetMap,isAdjustingForOverlapOnly() && scene.isModelPositionsFinalized());
        for (CasaNodeWidget iterWidget : widgetMap.keySet()) {
            moveWidget(iterWidget, widgetMap.get(iterWidget).getLocation(), false);
        }
        
        widget.getScene().validate();
    }
}
