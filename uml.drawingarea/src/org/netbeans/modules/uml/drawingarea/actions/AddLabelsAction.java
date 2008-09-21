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

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * The AddLabelsAction is used to control the labels on a Widget.  The widget
 * looks for a LabelManager on the selected nodes.  The AddLabelsAction 
 * will create a pull right that contains the actions specified by the active
 * LabelManager.  
 * 
 * If no label manager is present, no menu will be returned via the popup
 * presenter.
 * 
 * @author treyspiva
 */
public class AddLabelsAction extends SceneNodeAction
{
    private LabelManager lastManager = null;
    private WidgetContext context = null;
    private LabelManager.LabelType type = LabelManager.LabelType.EDGE;
    private LabelMenuModel model = new LabelMenuModel();
    
    /**
     * @param actionContext
     * @return
     */
    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        context = actionContext.lookup(WidgetContext.class);
        
        type = LabelManager.LabelType.EDGE;
        model.reset();
        lastManager = actionContext.lookup(LabelManager.class);
        
        if (context == null)
        {
            // (LLS) If you return null you the error message 
            // ContextAwareAction.createContextAwareInstance(context) returns null. That is illegal!
            // Therefore return "this".  The enable method will disable the action
            // if the context is null.
            return this;
        }
        else
        {
            type = LabelManager.LabelType.valueOf(context.getContextName());
        }
        
        
        return this;
    }
    
    protected void performAction(Node[] activatedNodes)
    {
        if(model.getCount() == 1)
        {
            model.performActionAt(0);
        }
    }
    
    /**
     * Test if there is only one active node, and that node has a label manager.
     * 
     * @param activatedNodes the active set of nodes
     * @return true if there is an active label manager.
     */
    protected boolean enable(Node[] activatedNodes)
    {
        return lastManager != null;
    }

    /**
     * retreives the an of the action.
     * 
     * @return
     */
    public String getName()
    {
        String retVal = NbBundle.getMessage(AddLabelsAction.class, "LBL_ADD_LABELS");
        
        if(model.getCount() == 1)
        {
            retVal = model.getLabel(0);
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
        JMenuItem item = super.getPopupPresenter();
        if(lastManager != null)
        {
            item =  new Actions.SubMenu(this, model);
            Actions.connect(item, (Action)this, true);
        }
        
        return item;
    }

    @Override
    public JMenuItem getMenuPresenter()
    {
        return super.getMenuPresenter();
    }
    
    /**
     * LabelMenuModel is an instance of the Actions.SubMenuModel that is used
     * to manage a LabelManagers actions.
     */
    protected class LabelMenuModel implements Actions.SubMenuModel
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
            
            // If there is only one action the Actions.SubMenu class want to
            // not make a submenu, but to inline the action into the parent
            // menu.  This is not what we want, since it seems to be a little
            // confusing.  So, I will increment the return value.
            //
            // the getLabel(int index) method will return null if the index
            // is greater than the number of real actions.  Returning null
            // tells the Actions.SubMenu implementation to add a seperator.
            // However, since the seperator is the last item, it will not 
            // actually add the seperator to the menu :-)
            
            if(retVal == 1)
            {
                retVal++;
            }
            
            // Include the reset label action, as a seperator
            return retVal+2;
        }

        public String getLabel(int index)
        {
            String retVal = null;
            
            Action[] actionList = getActions();
            if((actionList != null) && (actionList.length > index))
            {
                // A null means seperator.
                if(actionList[index] != null)
                {
                    retVal = (String) actionList[index].getValue(Action.NAME);
                }
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
                actionList[index].actionPerformed(null);
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
                Action[] contextActions = lastManager.getContextActions(type);
                actions = new Action[contextActions.length + 2];
                
                for(int index = 0; index < contextActions.length; index++)
                {
                    actions[index] = contextActions[index];
                }
                
                // Since the array is 0 based, substract one from the length to add the 
                // action to the last position.
                // The second to last element is to be null to specify a seperator.
                actions[actions.length - 2] = null;
                actions[actions.length - 1] = new ResetLabelsAction(lastManager);
            }
            
            return actions;
        }
        
        /**
         * Resets all of the data help by the model.  Since the action will
         * be help by the system, we have to be able to reset the data each
         * time a the context menu is about to open.
         */
        void reset()
        {
            actions = null;
        }
    }
}
