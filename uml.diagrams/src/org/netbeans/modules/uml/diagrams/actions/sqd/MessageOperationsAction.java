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
import java.util.EnumSet;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageLabelManager;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.netbeans.modules.uml.drawingarea.actions.SubMenuAction;
import org.netbeans.modules.uml.drawingarea.actions.ToggleLabelAction;
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
public class MessageOperationsAction extends SceneNodeAction
{
    private MessageLabelManager lastManager = null;
    private WidgetContext context = null;
    private LabelManager.LabelType type = LabelManager.LabelType.EDGE;
    private IPresentationElement lastPresentationElement;
    private ObjectScene lastScene;
    
    public MessageOperationsAction()
    {
        super();
    }
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext)
    {
        context = actionContext.lookup(WidgetContext.class);
        if(context!=null)
        {
            type = LabelManager.LabelType.valueOf(context.getContextName());
            lastPresentationElement=actionContext.lookup(IPresentationElement.class);
            lastScene=(ObjectScene) actionContext.lookup(Scene.class);
            if(lastScene!=null && lastPresentationElement!=null)
            {
                Widget w=lastScene.findWidget(lastPresentationElement);
                if(!(w instanceof MessageWidget))
                {
                    lastPresentationElement=null;//may be label selected or other related
                    //search for parents if will find, set pres element
                    if(w!=null)
                        for(Widget par=w.getParentWidget();par!=null;par=par.getParentWidget())
                        {
                            if(par instanceof MessageWidget)
                            {
                                lastPresentationElement=(IPresentationElement) lastScene.findObject(par);
                                break;
                            }
                        }
                }
                //other option is to exclude message finding below, sepatrate this clas to sevelal for each message kind
            }
        }
        //
        return this;
    }
    
    protected void performAction(Node[] activatedNodes)
    {
        
    }
    
    protected boolean enable(Node[] activatedNodes)
    {
        boolean retVal = false;
        
        if(super.enable(activatedNodes) == true && 
           activatedNodes.length == 1 && lastPresentationElement!=null)
        {
            Lookup lookup = activatedNodes[0].getLookup();
            IPresentationElement presentation = lookup.lookup(IPresentationElement.class);
            ObjectScene scene=activatedNodes[0].getLookup().lookup(ObjectScene.class);
            if(scene != null)
            {
                Widget widget = scene.findWidget(presentation);
                if((widget != null) && (widget.getLookup() != null))
                {
                    Lookup widgetLookup = widget.getLookup();
                    lastManager = widgetLookup.lookup(MessageLabelManager.class);
                    if(lastManager != null && 
                            lastPresentationElement!=null && 
                            lastPresentationElement.getFirstSubject() instanceof IMessage && 
                            ((IMessage)lastPresentationElement.getFirstSubject()).getReceivingClassifier()!=null)
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
        return NbBundle.getMessage(MessageOperationsAction.class, "LBL_OPERATIONS");
    }

    public HelpCtx getHelpCtx()
    {
        return null;
    }

    @Override
    public JMenuItem getPopupPresenter()
    {   
        //JMenuItem item =  new Actions.SubMenu(this, new OperationsMenuModel());
        if(lastPresentationElement==null)return new JMenuItem(getName());
        JMenu item=new JMenu(getName());
        
        Action[] actions= getOperationsActons();
        for(int i=0;i<actions.length;i++)
        {
            JMenuItem it=null;
            Action action=actions[i];
            if(action instanceof SubMenuAction)
            {
                it=((SubMenuAction)action).getPopupPresenter();
            }
            else
            {
                it=new JMenuItem(action);
            }
            it.setName((String) action.getValue(NAME));
            item.add(it);
        }
        Actions.connect(item, (Action)this, true);
        return item;
    }

    @Override
    public JMenuItem getMenuPresenter()
    {
        return super.getMenuPresenter();
    }
    
    public MessageLabelManager getlabelManager()
    {
        return lastManager;
    }
    
    public Action[] getOperationsActons()
    {
        ArrayList < Action > actions = new ArrayList < Action >();
        ResourceBundle bundle = NbBundle.getBundle(MessageLabelManager.class);
            MessageWidget messageW=null;
            IElement element = lastPresentationElement.getFirstSubject();
            IMessage msgE = null;
            if(element instanceof IMessage)
            {
                msgE = (IMessage)element;
                messageW=(MessageWidget) lastScene.findWidget(lastPresentationElement);
            
            int kind=msgE.getKind();
            if(kind==BaseElement.MK_SYNCHRONOUS || kind==BaseElement.MK_ASYNCHRONOUS)
            {
                ToggleLabelAction linkOperationAction = null;
                //
                if(msgE.getReceivingClassifier()!=null)
                {
                    linkOperationAction = new OperationLabelAction(messageW, 
                                                                     MessageLabelManager.OPERATION, 
                                                                     EnumSet.of(type), 
                                                                     bundle.getString("LBL_MESSAGE_ADDOPERATION"),
                                                                     OperationLabelAction.KIND.NEW,
                                                                     msgE,
                                                                     null);
                    actions.add(linkOperationAction);
                }
                SubMenuAction opProGr=new MessageOperationsSubMenuAction("Protected");
                boolean proExists=false;
                SubMenuAction opPacGr=new MessageOperationsSubMenuAction("Package");
                boolean pacExists=false;
                SubMenuAction opPriGr=new MessageOperationsSubMenuAction("Private");
                boolean priExists=false;
                actions.add(opProGr);
                actions.add(opPriGr);
                actions.add(opPacGr);
                //
                if(msgE.getReceivingClassifier()!=null)
                {
                    IClassifier cl=msgE.getReceivingClassifier();
                    ETList<IOperation> ops=cl.getOperations();
                    if(ops!=null && ops.size()>0)
                    {
                        for(int i=0;i<ops.size();i++)
                        {
                            IOperation op=ops.get(i);
                            if(op.getIsConstructor() || op.getIsDestructor())
                            {
                                //nothing, do not list constructor in asynch and synch messages
                            }
                            else 
                                if(op.getVisibility()==IVisibilityKind.VK_PUBLIC)
                            { 
                                //public goes to Opearations submenu
                                actions.add(new OperationLabelAction(messageW, 
                                        MessageLabelManager.OPERATION,
                                        EnumSet.of(type),
                                        new DataFormatter().formatElement(ops.get(i)),
                                        OperationLabelAction.KIND.SELECT,
                                        msgE,
                                        ops.get(i)));
                            }
                            else if(op.getVisibility()==IVisibilityKind.VK_PRIVATE)
                            {
                                priExists=true;
                                opPriGr.addAction(new OperationLabelAction(messageW, 
                                        MessageLabelManager.OPERATION,
                                        EnumSet.of(type),
                                        new DataFormatter().formatElement(ops.get(i)),
                                        OperationLabelAction.KIND.SELECT,
                                        msgE,
                                        ops.get(i)));
                            }
                            else if(op.getVisibility()==IVisibilityKind.VK_PROTECTED)
                            {
                                proExists=true;
                                opProGr.addAction(new OperationLabelAction(messageW, 
                                        MessageLabelManager.OPERATION,
                                        EnumSet.of(type),
                                        new DataFormatter().formatElement(ops.get(i)),
                                        OperationLabelAction.KIND.SELECT,
                                        msgE,
                                        ops.get(i)));
                            }
                            else if(op.getVisibility()==IVisibilityKind.VK_PACKAGE)
                            {
                                pacExists=true;
                                opPacGr.addAction(new OperationLabelAction(messageW, 
                                        MessageLabelManager.OPERATION,
                                        EnumSet.of(type),
                                        new DataFormatter().formatElement(ops.get(i)),
                                        OperationLabelAction.KIND.SELECT,
                                        msgE,
                                        ops.get(i)));
                            }
                        }
                    }
                }
                if(!proExists)actions.remove(opProGr);
                if(!priExists)actions.remove(opPriGr);
                if(!pacExists)actions.remove(opPacGr);
            }
            else if(kind==BaseElement.MK_CREATE)
            {
                ToggleLabelAction linkOperationAction = null;
                //
                if(msgE.getReceivingClassifier()!=null)
                {
                    linkOperationAction = new OperationLabelAction(messageW, 
                                                                     MessageLabelManager.OPERATION, 
                                                                     EnumSet.of(type), 
                                                                     bundle.getString("LBL_MESSAGE_ADDCONSTRUCTOR"),
                                                                     OperationLabelAction.KIND.NEW,
                                                                     msgE,
                                                                     null);
                    actions.add(linkOperationAction);
                    IClassifier cl=msgE.getReceivingClassifier();
                    ETList<IOperation> ops=cl.getOperations();
                    if(ops!=null && ops.size()>0)
                    {
                        for(int i=0;i<ops.size();i++)
                        {
                            IOperation op=ops.get(i);
                            if(op.getIsConstructor() || op.getIsDestructor())
                            {
                                actions.add(new OperationLabelAction(messageW, 
                                        MessageLabelManager.OPERATION,
                                        EnumSet.of(type),
                                        new DataFormatter().formatElement(ops.get(i)),
                                        OperationLabelAction.KIND.SELECT,
                                        msgE,
                                        ops.get(i)));
                            }
                         }
                    }
                }
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
