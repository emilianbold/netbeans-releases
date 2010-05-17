/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.awt.Point;
import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.AlignWithWidgetCollector;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineWidget;
import java.util.ArrayList;
/**
 * the same as commin provider but disable movement above y zero level
 * @author sp153251
 */
public class AlignWithMoveStrategyProvider extends org.netbeans.modules.uml.drawingarea.view.AlignWithMoveStrategyProvider {
    
    private Boolean horizontalOnly=null;//flag used to check if there is any lifeline and all elements should be moved horizontally only
    private Widget originalParent;

    public AlignWithMoveStrategyProvider (AlignWithWidgetCollector collector, 
                                          LayerWidget interractionLayer, 
                                          LayerWidget widgetLayer,
                                          AlignWithMoveDecorator decorator, 
                                          boolean outerBounds) {
        super (collector, interractionLayer,widgetLayer, decorator,outerBounds);
    }

    
    @Override
    public void movementStarted(Widget widget) {
        originalParent=widget.getParentWidget();
        super.movementStarted(widget);
    }

    @Override
    public void movementFinished(Widget widget) {
        super.movementFinished(widget);
        horizontalOnly=null;
    }

    
    
    @Override
    public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
        Point ret= super.locationSuggested(widget, originalLocation, suggestedLocation);
        if(horizontalOnly==null)
        {
            ArrayList<MovingWidgetDetails> details=getMovingDetails();
            if(details!=null)
            {
                horizontalOnly=false;
                if(details!=null)for(MovingWidgetDetails d:details)
                {
                    if(d.getWidget() instanceof LifelineWidget)
                    {
                        horizontalOnly=true;
                        break;
                    }
                }
            }
        }
        if(horizontalOnly==Boolean.TRUE)ret.y=originalLocation.y;
        else
        {
            Point sceneRet=originalParent.convertLocalToScene(ret);
            if((sceneRet.y+widget.getBounds().y)<0)
            {
                sceneRet.y-=(sceneRet.y+widget.getBounds().y);
                ret=originalParent.convertSceneToLocal(sceneRet);
            }
        }
        //
        //
        return ret;
    }
}
