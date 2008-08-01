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
package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.diagrams.Util;
import org.netbeans.modules.uml.diagrams.nodes.sqd.CombinedFragmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.InteractionOperandWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.netbeans.modules.uml.drawingarea.actions.WidgetContext;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author psb
 */
public class InteractionOperandAction extends SceneNodeAction
{
    private WidgetContext context = null;
    private LabelManager.LabelType type = LabelManager.LabelType.EDGE;
    
    public InteractionOperandAction()
    {
        super();
    }
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        context = actionContext.lookup(WidgetContext.class);
        //context.
        return this;
    }
    
    protected void performAction(Node[] activatedNodes)
    {
        
    }
    
    protected boolean enable(Node[] activatedNodes)
    {
        boolean retVal = false;
        
        if(super.enable(activatedNodes) == true && activatedNodes.length == 1)
        {
            Lookup lookup = activatedNodes[0].getLookup();
            IPresentationElement presentation = lookup.lookup(IPresentationElement.class);
            ObjectScene scene=activatedNodes[0].getLookup().lookup(ObjectScene.class);
            
            if(scene != null)
            {
                Widget widget = scene.findWidget(presentation);
                
                if(widget!=null)
                {
                    Widget cfW=org.netbeans.modules.uml.drawingarea.util.Util.getParentNodeWidget(widget);

                    if(cfW!=null && (cfW instanceof CombinedFragmentWidget) && (context.getContextName().equals("Operand") || context.getContextName().equals("Operator")))retVal=true;
                }
            }
        }
        
        return retVal;
    }

    public String getName()
    {
        return NbBundle.getMessage(InteractionOperandAction.class, "LBL_OPERAND");
    }

    public HelpCtx getHelpCtx()
    {
        return null;
    }

    @Override
    public JMenuItem getPopupPresenter()
    {   
        JMenuItem item =  new Actions.SubMenu(this, new OperationsMenuModel());
        Actions.connect(item, (Action)this, true);
        
        return item;
    }

    @Override
    public JMenuItem getMenuPresenter()
    {
        return super.getMenuPresenter();
    }
    
    public Action[] getOperationsActons()
    {
        ArrayList < Action > actions = new ArrayList < Action >();
        if(context!=null && (context.getContextName().equals("Operand") || context.getContextName().equals("Operator"))){
            InteractionOperandWidget operandW=(InteractionOperandWidget) context.getContextItems()[0];
            CombinedFragmentWidget cfW=(CombinedFragmentWidget) Util.getParentByClass(operandW, CombinedFragmentWidget.class);
            IInteractionOperand operandE=operandW.getOperand();
            ObjectScene scene=(ObjectScene) cfW.getScene();
            ICombinedFragment cf=(ICombinedFragment) (((IPresentationElement) scene.findObject(cfW)).getFirstSubject());
            
             actions.add(new InsertOperandAction(cf,cfW));
             actions.add(new EditInteractionConstraintAction(operandW));
             
             if(operandW.getParentWidget().getChildren().indexOf(operandW)>0)
             {
                 actions.add(new DeleteInteractionOperandAction(cf, cfW, operandE));
             }
        }
        Action[] retVal = new Action[actions.size()];
        actions.toArray(retVal);
        return retVal;
    }
    
    protected class OperationsMenuModel implements Actions.SubMenuModel
    {
        private Action[] actions = null;
        
        public int getCount()
        {
            int retVal = 0;
            
            if(context != null && (context.getContextName().equals("Operand") || context.getContextName().equals("Operator")))
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
                actions = getOperationsActons();
            }
            
            return actions;
        }
        
    }
}
