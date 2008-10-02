/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 *
 * @author Sheryl Su
 */
public class NavigateLinkAction extends WidgetAction.Adapter
{
    
    private int lastTargetedWidgetIndex=-1;//used in nodes link navigation
    private Widget lastTargetWidget;//used in links link navigation

    @Override
    public State mouseClicked(Widget widget, WidgetMouseEvent event)
    {
        if (event.getButton() != MouseEvent.BUTTON1)
        {
            return State.REJECTED;
        }
        
        if (widget instanceof ConnectionWidget)
        {
            Widget target = ((ConnectionWidget) widget).getSourceAnchor().getRelatedWidget();
            if(target==lastTargetWidget)//cirle source-target, may be better to check selection?
            {
                target = ((ConnectionWidget) widget).getTargetAnchor().getRelatedWidget();
            }
//            Util.centerWidget(target);
            positionWidget(target, widget.convertLocalToScene(event.getPoint()));
            lastTargetWidget=target;
        } else
        {
            Collection<Widget.Dependency> deps = widget.getDependencies();
            ArrayList<Widget> oppositeWidgets=new ArrayList<Widget>();
            if (deps.size() > 0)
            {
                Widget.Dependency[] depArray = new Widget.Dependency[deps.size()];
                deps.toArray(depArray);

                for(Widget.Dependency dep: depArray)
                {
                    if (dep instanceof Anchor)
                    {
                        List<Anchor.Entry> entries = ((Anchor) dep).getEntries();
                        if (entries.size() > 0)
                        {
                            Anchor.Entry entry = entries.get(0);
                            Widget target = entry.getOppositeAnchor().getRelatedWidget();
    //                        Util.centerWidget(target);
                            // try to position target widget at the cursor point
//                            positionWidget(target, widget.convertLocalToScene(event.getPoint()));
//
//                            // to place the already traversed entry to the end
                            Widget nodeW=Util.getParentNodeWidget(target);
                            if(nodeW != null && !oppositeWidgets.contains(nodeW) )
                            {
                                oppositeWidgets.add(nodeW);
                            }
//                            ((Anchor) dep).removeEntry(entry);
//                            ((Anchor) dep).addEntry(entry);
                        }
                    }
                }
            }
            
            if(widget.getScene() instanceof DesignerScene)//object relations oriented part, works on sqd, 
                //but TODO: need to verify if only code above with dependencies if necessary.
            {
                DesignerScene scene=(DesignerScene) widget.getScene();
                IPresentationElement node=(IPresentationElement) scene.findObject(widget);
                if(node!=null)
                {
                    Collection <IPresentationElement> edges=scene.findNodeEdges(node, true, false);
                     //find all opposite widgets now
                    for(IPresentationElement edge:edges)
                    {
                        ConnectionWidget conn=(ConnectionWidget) scene.findWidget(edge);
                        Widget targW=conn.getTargetAnchor().getRelatedWidget();
                        Widget nodeW=Util.getParentNodeWidget(targW);
                        if(!oppositeWidgets.contains(nodeW))oppositeWidgets.add(nodeW);
                    }
                    edges=scene.findNodeEdges(node, false, true);
                     //find all opposite widgets now
                    for(IPresentationElement edge:edges)
                    {
                        ConnectionWidget conn=(ConnectionWidget) scene.findWidget(edge);
                        Widget targW=conn.getSourceAnchor().getRelatedWidget();
                        Widget nodeW=Util.getParentNodeWidget(targW);
                        if(!oppositeWidgets.contains(nodeW))oppositeWidgets.add(nodeW);
                    }
                }
            }
            if(oppositeWidgets.size()>0)
            {

                lastTargetedWidgetIndex+=event.isShiftDown() ? -1 : 1;
                if(lastTargetedWidgetIndex<0)lastTargetedWidgetIndex=oppositeWidgets.size()-1;
                else if(lastTargetedWidgetIndex>=oppositeWidgets.size())lastTargetedWidgetIndex=0;
                positionWidget(oppositeWidgets.get(lastTargetedWidgetIndex), widget.convertLocalToScene(event.getPoint()));
            }
        }
        return State.CONSUMED;
    }
    
    
     private void positionWidget(Widget widget, Point point)
    {
         if ( widget == null)
         {
             return;
         }
        Scene scene = widget.getScene();

        JComponent view = scene.getView();
        if (view != null)
        {
            Rectangle viewBounds = view.getVisibleRect();
            Rectangle rectangle = widget.convertLocalToScene(widget.getBounds());
            Point center = new Point(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);
            center = scene.convertSceneToView(center);

            Point p = scene.convertSceneToView(point);
            view.scrollRectToVisible(new Rectangle(viewBounds.x - (p.x - center.x), 
                    viewBounds.y - (p.y - center.y), viewBounds.width, viewBounds.height));
            
            if (scene instanceof ObjectScene)
            {
                ObjectScene objectScene = (ObjectScene) scene;
                Object obj = objectScene.findObject(widget);
                HashSet<Object> set = new HashSet<Object>();
                set.add(obj);
                objectScene.userSelectionSuggested(set, false);
            }
        }
    }
}