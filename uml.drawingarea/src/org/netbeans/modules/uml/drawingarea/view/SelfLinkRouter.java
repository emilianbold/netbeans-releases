/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.drawingarea.view;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author treyspiva
 */
public class SelfLinkRouter implements Router
{
    public static final int BEND_SPACE = 10;
    
    public List<Point> routeConnection(ConnectionWidget widget)
    {
        Anchor sourceAnchor = widget.getSourceAnchor();
        Anchor targetAnchor = widget.getTargetAnchor();
        
        int modifier = findTotalLinkToSelfBefore(widget);
        int adjustmentValue = BEND_SPACE * modifier;
        
        ArrayList < Point > retVal = new ArrayList < Point >();
        
        Point sourcePt = sourceAnchor.compute(widget.getSourceAnchorEntry()).getAnchorSceneLocation();
        Point targetPt = targetAnchor.compute(widget.getTargetAnchorEntry()).getAnchorSceneLocation();
        
        retVal.add(sourcePt);
        
        if(sourcePt.y < targetPt.y)
        {
            // Default to the source being to the right of the target.
            int xAdjust = adjustmentValue;
            
            // Source is above target
            if(sourcePt.x < targetPt.x)
            {
                // Source is to the left of the target.
                xAdjust = -adjustmentValue;
            }
            
            retVal.add(new Point(sourcePt.x + xAdjust, sourcePt.y));
            retVal.add(new Point(sourcePt.x + xAdjust, targetPt.y + adjustmentValue));
            retVal.add(new Point(targetPt.x , targetPt.y + adjustmentValue));
        }
        else
        {
            // Default to the source being to the right of the target.
            int xAdjust = adjustmentValue;

            // Source is below the target.
            if(sourcePt.x < targetPt.x)
            {
                // Source is to the left of the target.
                xAdjust = -adjustmentValue;
            }
            
            retVal.add(new Point(sourcePt.x + xAdjust, sourcePt.y));
            retVal.add(new Point(sourcePt.x + xAdjust, targetPt.y - adjustmentValue));
            retVal.add(new Point(targetPt.x, targetPt.y - adjustmentValue));
        }
        
        retVal.add(targetPt);
        
        return retVal;
        
    }

    private int findTotalLinkToSelfBefore(ConnectionWidget widget)
    {
        int retVal = 0;
        
        if (widget.getScene() instanceof GraphScene)
        {
            GraphScene scene = (GraphScene) widget.getScene();
            
            Widget nodeWidget = widget.getSourceAnchor().getRelatedWidget();
            Object nodeObject = scene.findObject(nodeWidget);
            
            for(Object edgeObject : scene.findNodeEdges(nodeObject, true, false))
            {
                ConnectionWidget edgeWidget = (ConnectionWidget) scene.findWidget(edgeObject);
                Widget targetNode = edgeWidget.getTargetAnchor().getRelatedWidget();
                Widget sourceNode = edgeWidget.getSourceAnchor().getRelatedWidget();
                
                if(targetNode.equals(sourceNode) == true)
                {
                    // We found a self link.
                    retVal++;
                }
                
                if(edgeWidget.equals(widget) == true)
                {
                    // We found the edge we where lookig to find.
                    break;
                }
            }
        }
        
        return retVal;
    }
}
