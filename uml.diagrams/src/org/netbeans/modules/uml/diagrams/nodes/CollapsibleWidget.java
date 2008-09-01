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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.SeparatorWidget.Orientation;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.CollapsibleWidgetManager;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;

/**
 *
 * @author treyspiva
 */
public class CollapsibleWidget extends Widget implements CollapsibleWidgetManager
{

    private Widget targetWidget = null;
    private Border border = null;
    private boolean collapsed = false;
    private CollapsibleWidgetManager mgr = null;
    String compartmentName;

    public CollapsibleWidget(Scene scene, Widget target)
    {
        super(scene);

        setLayout(LayoutFactory.createVerticalFlowLayout());

        targetWidget = target;

        SeparatorWidget separator = new SeparatorWidget(scene, Orientation.HORIZONTAL);
        separator.createActions(DesignerTools.SELECT).addAction(new CollapseControllerAction());
        separator.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        addChild(separator);
        addChild(targetWidget);
        
        // Use the parents properties.
        setForeground(null);
        setBackground(null);
        setFont(null);
    }

    public void setCompartmentName(String name)
    {
        this.compartmentName = name;
    }

    private void collapse()
    {
        // First make sure that the nodes minumum size is not set.
        Widget parent = getParentWidget();
        UMLNodeWidget node = null;
        while (parent != null)
        {
            if (parent instanceof UMLNodeWidget)
            {
                node = (UMLNodeWidget) parent;

                // I would perfer this to be the name comparment min size.  
                // Maybe the size of the children above.
                node.setMinimumSize(new Dimension(1, 1));
                break;
            }
            parent = parent.getParentWidget();
        }

        Rectangle bounds = targetWidget.getBounds();
        final SceneAnimator animator = getScene().getSceneAnimator();
        final Rectangle newBounds = collapsed == false ? new Rectangle() : null;

        for (Widget child : targetWidget.getChildren())
        {
            animator.animatePreferredBounds(child, newBounds);
        }

        if (collapsed == true)
        {
            if (border != null)
            {
                targetWidget.setBorder(border);
            }
            collapsed = false;
        } 
        else
        {
            border = targetWidget.getBorder();

            // I want to create som space between the separator and the 
            // next widget. That way the separator does not look crowded.
            targetWidget.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
            collapsed = true;
        }
    }

    public void collapseWidget(String name)
    {
        if (name.equalsIgnoreCase(UMLNodeWidget.COLLAPSE_ALL))
        {
            collapsed = false;
        }
        else if (name.equalsIgnoreCase(UMLNodeWidget.EXPAND_ALL))
        {
            collapsed = true;
        }
        
        collapse();
    }

    public String getCollapsibleCompartmentName()
    {
        return compartmentName;
    }
    
    public Widget getCollapsibleCompartmentWidget()
    {
        return this;
    }

    public class CollapseControllerAction extends WidgetAction.Adapter
    {

        @Override
        public State mouseClicked(Widget widget, WidgetMouseEvent event)
        {
            State retVal = State.REJECTED;
            if ((event.getClickCount() == 2) && (event.getButton() == MouseEvent.BUTTON1))
            {
                collapse();
            }
            return retVal;
        }
    }

    public boolean isCompartmentCollapsed()
    {
        return collapsed;
    }

}