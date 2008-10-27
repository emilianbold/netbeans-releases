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

package org.netbeans.modules.uml.diagrams.actions;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.WidgetKeyEvent;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.edges.AssociationConnector;
import org.netbeans.modules.uml.diagrams.nodes.AttributeWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.actions.Selectable;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 *
 * @author jyothi
 */
public class AssociationConnectorIteratorAction extends WidgetAction.Adapter 
{
    DesignerScene scene;
    LabelManager mgr = null;
    private String name;

    
    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event)
    {
        State retVal = State.REJECTED;
        scene = (DesignerScene) widget.getScene();
        boolean status = false;
        
        if(event.isShiftDown() == true) 
        {
            if(event.getKeyCode() == KeyEvent.VK_LEFT || event.getKeyCode() == KeyEvent.VK_RIGHT)
            {
                status = selectNextQualifier(widget);
                if (status) 
                {
                    //we successfully selected a label..
                    retVal = State.CONSUMED;
                }    
            }
        }        
        return retVal;
    }
    
    private boolean selectNextQualifier(Widget widget)
    {
        boolean retVal = false;
        if (widget != null && widget instanceof AssociationConnector)
        {
            List <Widget> children = getAllChildren(widget, new ArrayList<Widget>());
            for (Widget child : children)
            {
                if (child instanceof AttributeWidget)
                {                                       
                    if (child.getState().isSelected() == false)
                    {
                        Selectable selectable = child.getLookup().lookup(Selectable.class); 
                       if (selectable != null)
                        {
                            selectable.select(child);
                        }
                        retVal = true;
                        break;
                    }
                }
            }
        }
        
        return retVal;
        
    }
    
    private List getAllChildren(Widget widget, List<Widget> children)
    {
        if (widget != null && children != null)
        {
            for (Widget child : widget.getChildren())
            {
                children.add(child);
                getAllChildren(child, children);
            }
        }  
        return children;
    }
}
