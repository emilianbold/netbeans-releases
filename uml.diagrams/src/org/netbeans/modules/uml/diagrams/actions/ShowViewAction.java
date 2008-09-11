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
package org.netbeans.modules.uml.diagrams.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.netbeans.modules.uml.drawingarea.view.WidgetViewManager;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.awt.Actions;
import org.openide.awt.JInlineMenu;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author treyspiva
 */
public class ShowViewAction extends SceneNodeAction
{
    private WidgetViewManager lastManager = null;
    ContextPaletteManager contextManager = null;
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        Object nodes = actionContext.lookup(Node.class);
        
        // Make sure we do not have an array
        if(nodes instanceof Node)
        {
            Node activeNode = (Node)nodes;
            
            Lookup lookup = activeNode.getLookup();
            IPresentationElement presentation = lookup.lookup(IPresentationElement.class);
            DesignerScene scene= lookup.lookup(DesignerScene.class);
            
            if(scene != null)
            {
                contextManager = scene.getLookup().lookup(ContextPaletteManager.class);
            
                Widget widget = scene.findWidget(presentation);

                if((widget != null) && (widget.getLookup() != null))
                {
                    Lookup widgetLookup = widget.getLookup();
                    lastManager = widgetLookup.lookup(WidgetViewManager.class);
                }
            }
        }
        return this;
    }
    
    protected void performAction(Node[] activatedNodes)
    {
        Action[] actions = lastManager.getViewActions();
        if((actions != null) && (actions.length > 0))
        {
            actions[0].actionPerformed(null);
        }
    }
    
    protected boolean enable(Node[] activatedNodes)
    {
        boolean retVal = false;
        
        if(activatedNodes.length == 1)
        {
            Lookup lookup = activatedNodes[0].getLookup();
            IPresentationElement presentation = lookup.lookup(IPresentationElement.class);
            DesignerScene scene=activatedNodes[0].getLookup().lookup(DesignerScene.class);
            
            if(super.enable(activatedNodes) == true)
            {
                Widget widget = scene.findWidget(presentation);

                if((widget != null) && (widget.getLookup() != null))
                {
                    Lookup widgetLookup = widget.getLookup();
                    lastManager = widgetLookup.lookup(WidgetViewManager.class);
                    if(lastManager != null)
                    {
                        retVal = true;
                    }
                }
            }
        }
        
        return retVal;
    }

    public String getName()
    {
        //return NbBundle.getMessage(ShowViewAction.class, "LBL_ADD_LABELS");
//        return "Show Views";
        
        String retVal = "";
        if(lastManager != null)
        {
            Action[] actions = lastManager.getViewActions();
            if((actions != null) && (actions.length > 0))
            {
                retVal = (String)actions[0].getValue(Action.NAME);
            }
        }
        
        return retVal;
    }

    public HelpCtx getHelpCtx()
    {
        return null;
    }

    @Override
    public JMenuItem getPopupPresenter()
    {   
        
        JMenuItem retVal = null;
        if(lastManager != null)
        {
            JInlineMenu item = new JInlineMenu();

            Action[] actions = lastManager.getViewActions();
            JMenuItem[] items = new JMenuItem[actions.length];
            for(int index = 0; index < items.length; index++)
            {
                items[index] = new JMenuItem(new DecoratorAction(actions[index]));
            }

            item.setMenuItems(items);
            retVal = item;
        }
        else 
        {
            // If the menu item is not valid we want to not display the menu item
            // If we return null a warning will be written to the log file by
            // org.openide.util.Utilities.  However if we return an empty
            // JInlineMenu the menu will not appear.
            JInlineMenu item = new JInlineMenu();
            retVal = item;
        }
        
        return retVal;
    }
    
    protected class ShowViewMenuModel implements Actions.SubMenuModel
    {
        private Action[] actions = null;
        
        public int getCount()
        {
            int retVal = 0;
            
            if(lastManager != null)
            {
                
                Action[] actionList = getActions();


                if(actionList != null)
                {
                    retVal = actionList.length;
                }
            }
            return retVal;
        }

        public String getLabel(int index)
        {
            String retVal = null;
            
            Action[] actionList = getActions();
            if((actionList != null) && (actionList.length > index))
            {
                retVal = (String) actionList[index].getValue(Action.NAME);
            }
            
            return retVal;
        }

        public HelpCtx getHelpCtx(int index)
        {
            HelpCtx retVal = null;
            
            Action[] allActions = getActions();
            if((index >= 0) && (allActions.length > index))
            {
                if(allActions[index] instanceof SystemAction)
                {
                    SystemAction sysAction = (SystemAction)allActions[index];
                    retVal = sysAction.getHelpCtx();
                }
            }
            
            return retVal;
        }

        public void performActionAt(int index)
        {
            Action[] actionList = getActions();
            if((actionList != null) && (actionList.length > index))
            {
                
                if(contextManager != null)
                {
                    contextManager.cancelPalette();
                }
                
                actionList[index].actionPerformed(null);
                
                if(contextManager != null)
                {
                    contextManager.selectionChanged(null);
                }
            }
        }

        public void addChangeListener(ChangeListener l)
        {
        }

        public void removeChangeListener(ChangeListener l)
        {
        }
        
        protected Action[] getActions()
        {
            if(actions == null)
            {
                actions = lastManager.getViewActions();
            }
            
            return actions;
        }
        
    }
    
    public class DecoratorAction implements Action
    {

        private Action targetAction = null;
        
        public DecoratorAction(Action target)
        {
            targetAction = target;
        }
        
        public Object getValue(String name)
        {
            return targetAction.getValue(name);
        }

        public void putValue(String name, Object value)
        {
            targetAction.putValue(name, value);
        }

        public void setEnabled(boolean value)
        {
            targetAction.setEnabled(value);
        }

        public boolean isEnabled()
        {
            return targetAction.isEnabled();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener)
        {
            targetAction.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener)
        {
            targetAction.removePropertyChangeListener(listener);
        }

        public void actionPerformed(ActionEvent e)
        {
            if(contextManager != null)
            {
                contextManager.cancelPalette();
            }

            targetAction.actionPerformed(e);

            if(contextManager != null)
            {
                contextManager.selectionChanged(null);
            }
        }
        
    }
}
