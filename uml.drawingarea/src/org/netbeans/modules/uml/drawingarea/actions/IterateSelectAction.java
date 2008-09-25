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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetKeyEvent;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Widget.Dependency;
import org.openide.util.Lookup;

/**
 *
 * @author treyspiva
 */
public class IterateSelectAction extends WidgetAction.Adapter
{

    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event)
    {
        State retVal = State.REJECTED;
        
        if(event.isShiftDown() == true) 
        {
            Widget toSelect = null;
            if(event.getKeyCode() == KeyEvent.VK_UP)
            {
                toSelect = findPreviousSelectedWidget(getTargetWidget(widget));
                retVal = State.CONSUMED;
            }
            else if(event.getKeyCode() == KeyEvent.VK_DOWN)
            {
                toSelect = findNextSelectedWidget(getTargetWidget(widget));
                retVal = State.CONSUMED;
                
            }
            
            if(toSelect != null)
            {
                Selectable selectable = toSelect.getLookup().lookup(Selectable.class);
                if(selectable != null)
                {
                    selectable.select(toSelect);
                }
            }
        }
        
        return retVal;
    }
    
    protected Widget findNextSelectedWidget(Widget widget)
    {
        Widget retVal = null;
        
        List < Widget > widgets = getAllSelectableWidgets(widget);
        Collections.sort(widgets, new WidgetLocationComparator());
        
        ObjectScene scene = (ObjectScene)widget.getScene();
        Set selectedObjects = scene.getSelectedObjects();

        for(int index = widgets.size() - 1; index >= 0; index--)
        {
            Widget curWidget = widgets.get(index);
            
            Object curObj = scene.findObject(curWidget);
            if(selectedObjects.contains(curObj) == true)
            {
                if(curWidget.getState().isSelected() == true)
                {
                    if(index < (widgets.size() - 1))
                    {
                        retVal = widgets.get(index + 1);
                    }
                    break;
                }
            }
        }
        
        if((retVal == null) && (widgets.size() > 0))
        {
            retVal = widgets.get(0);
        }
        
        return retVal;
    }
    
    protected Widget findPreviousSelectedWidget(Widget widget)
    {
        Widget retVal = null;
        
        List < Widget > widgets = getAllSelectableWidgets(widget);
        Collections.sort(widgets, new WidgetLocationComparator());
        
        ObjectScene scene = (ObjectScene)widget.getScene();
        Set selectedObjects = scene.getSelectedObjects();

        for(int index = 0; index < widgets.size(); index++)
        {
            Widget curWidget = widgets.get(index);
            if(curWidget != null)
            {
                Object curObj = scene.findObject(curWidget);
                if(selectedObjects.contains(curObj) == true)
                {
                    if(index > 0)
                    {
                        retVal = widgets.get(index - 1);
                    }
                    else
                    {
                        retVal = widgets.get(widgets.size() - 1);
                    }
                    break;
                }
            }
        }
        
        if((retVal == null) && (widgets.size() > 0))
        {
            retVal = widgets.get(0);
        }
        
        return retVal;
    }
    
    public List < Widget > getAllSelectableWidgets(Widget widget)
    {
        ArrayList < Widget > retVal = new ArrayList < Widget >();
        Lookup lookup = widget.getLookup();
        if(lookup != null && lookup.lookup(Selectable.class) != null)
        {
            if (widget.isVisible())
            {
                retVal.add(widget);
            }
        }
        
        for (Widget child : widget.getChildren())
        {
            List<Widget> selectables = getAllSelectableWidgets(child);
            if (selectables.size() > 0)
            {
                retVal.addAll(selectables);
            }
            //now get all node labels 
            for (Dependency dep : widget.getDependencies())
            {
                if (dep instanceof LabelWidget)
                {
                    List<Widget> nodeLabels = getAllSelectableWidgets((Widget) dep);
                    if (nodeLabels.size() > 0)
                    {
                        retVal.addAll(nodeLabels);
                    }
                }
            }
        }
        
        return retVal;
    }

    protected Widget getTargetWidget(Widget widget)
    {
        return widget;
    }
    
    public class WidgetLocationComparator implements Comparator < Widget >
    {

        public int compare(Widget o1, Widget o2)
        {
            int retVal = 0;

            Point p1 = o1.getParentWidget().convertLocalToScene(o1.getLocation());
            Point p2 = o2.getParentWidget().convertLocalToScene(o2.getLocation());

            // I am only going to compare the horizontal hieght, since that
            // is currently all we care about.
            if(p1.y < p2.y)
            {
                retVal = -1;
            }
            else if(p1.y > p2.y)
            {
                retVal = 1;
            }

            return retVal;
        }
    }
}
