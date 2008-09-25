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
package org.netbeans.modules.uml.diagrams.edges.sqd;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.diagrams.engines.SequenceDiagramEngine;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
//import org.netbeans.modules.uml.diagrams.nodes.sqd.NumberedNameLabelWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.OperationLabelWidget;
import org.netbeans.modules.uml.drawingarea.AbstractLabelManager;
import org.netbeans.modules.uml.drawingarea.LabelManager.LabelType;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.actions.ToggleLabelAction;
import org.netbeans.modules.uml.drawingarea.support.ModelElementBridge;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.util.NbBundle;


/**
 *
 * @author psb
 */
public class MessageLabelManager extends AbstractLabelManager
{
    public MessageLabelManager(MessageWidget widget)
    {
        super(widget);
    }
    
    //////////////////////////////////////////////////////////////////
    // LableManager Overrides

    public void createInitialLabels()
    {
        IElement element = getModelElement();
        IMessage msgE = null;
        if(element instanceof IMessage)
        {
            msgE = (IMessage)element;
        }
        createNameMessageLabel(msgE, false);
        int kind=msgE.getKind();
        if(kind==BaseElement.MK_SYNCHRONOUS || kind==BaseElement.MK_ASYNCHRONOUS)
        {
            if(msgE.getOperationInvoked()!=null || msgE.getReceivingClassifier()!=null)
            {
                createOperationMessageLabel(msgE, false);
            }
        }
    }

    public MessageWidget getMessage()
    {
        return (MessageWidget) getConnector();
    }

    
    public Action[] getContextActions(LabelType type)
    {
        ArrayList<Action> actions = new ArrayList<Action>();

        ResourceBundle bundle = NbBundle.getBundle(MessageLabelManager.class);
//        ToggleLabelAction linkNameAction = new ToggleLabelAction(this,
//                "",
//                EnumSet.of(type),
//                "");
//        actions.add(linkNameAction);//workaround action

        ToggleLabelAction linkNameAction = new ToggleLabelAction(this,
                                                NAME,
                                                EnumSet.of(type),
                                                bundle.getString("LBL_MESSAGE_NAME"));
        actions.add(linkNameAction);
        //
        IElement element = getModelElement();
        IMessage msgE = null;
        if (element instanceof IMessage)
        {
            msgE = (IMessage) element;
        }
        if (msgE != null && msgE.getOperationInvoked() != null)
        {
            linkNameAction = new ToggleLabelAction(this,
                                                    OPERATION,
                                                    EnumSet.of(type),
                                                    bundle.getString("LBL_MESSAGE_OPERATION"));
            actions.add(linkNameAction);
        }
        Action[] retVal = new Action[actions.size()];
        actions.toArray(retVal);
        return retVal;
    }


    //////////////////////////////////////////////////////////////////
    // ProepertyChangeListener Implementation
    
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propName = evt.getPropertyName();
        DesignerScene scene=(DesignerScene) getConnector().getScene();
        IMessage msgE = (IMessage) ((IPresentationElement) scene.findObject(getConnector())).getFirstSubject();
        
        if(propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
        {
            String name = msgE.getNameWithAlias();
            updateLabel(LabelType.EDGE, NAME, name);
        }
        else if(propName.equals(ModelElementChangedKind.OPERATION_PROPERTY_CHANGE.toString()))
        {
            DataFormatter formatter=new DataFormatter();
            String op = (msgE.getOperationInvoked()!=null) ? formatter.formatElement(msgE.getOperationInvoked()) : "";
            updateLabel(LabelType.EDGE, OPERATION, op);
        }
        else if(propName.equals(ModelElementChangedKind.RELATION_CREATED.toString()) && msgE.getKind()!=BaseElement.MK_RESULT)
        {
            //check if show number enabled
            SequenceDiagramEngine engine=(SequenceDiagramEngine) scene.getEngine();
            boolean shownumbers=engine.getSettingValue(SequenceDiagramEngine.SHOW_MESSAGE_NUMBERS)==Boolean.TRUE;
            if(shownumbers)
            {
                if(isVisible(NAME))
                {
                    hideLabel(NAME);
                }
                if(!isVisible(OPERATION))
                {
                    showLabel(NAME);
                }
                else
                {
                    hideLabel(OPERATION);
                    showLabel(OPERATION);
                }
            }
        }
    }
    
    //////////////////////////////////////////////////////////////////
    // AbstractLabelManager Overrides
    
    protected Widget createLabel(String name, LabelType type)
    {
        String text = "";
        IElement element = getModelElement();
        IMessage msgE = null;
        if(element instanceof IMessage)
        {
            msgE = (IMessage)element;
        }
        Widget retVal=null;
        if(name.equals(NAME))
        {
                INamedElement namedElement = msgE;
                boolean shownumbers=((DesignerScene) getScene()).getEngine().getSettingValue(SequenceDiagramEngine.SHOW_MESSAGE_NUMBERS)==Boolean.TRUE;
                text = namedElement.getNameWithAlias();
                if(shownumbers && msgE.getAutoNumber()!=null)text=msgE.getAutoNumber()+": "+text;
                if((text==null || text.length()<1))text=retrieveDefaultName();
                element = namedElement;
                //retVal = new NumberedNameLabelWidget(getScene(), msgE,this);
                retVal = new EditableCompartmentWidget(getScene(),element ,"messageName","Message Name");
                ((EditableCompartmentWidget)retVal).setLabel(text);
        }
        else if(name.equals(OPERATION))
        {
                DataFormatter formatter = new DataFormatter();
                IOperation namedElement = msgE.getOperationInvoked();
                if(namedElement==null || msgE.getReceivingClassifier()==null)return null;
                text = formatter.formatElement(namedElement);
                if((text == null) || (text.length() == 0))
                {
                    text = retrieveDefaultName();
                }
                element = namedElement;
                retVal=new OperationLabelWidget(getScene(),namedElement,this);
        }
        else
        {
            throw new RuntimeException("Unhandled label: "+name);
        }
        
        //EditableCompartmentWidget retVal = new EditableCompartmentWidget(getScene(), element);
        return retVal;
    }
    
    protected Object createAttachedData(String name, LabelType type)
    {
        Object retVal = new ModelElementBridge(getModelElement());
        if(NAME.equals(name))return retVal;
        return null;
    }
    
    protected LayoutFactory.ConnectionWidgetLayoutAlignment getDefaultAlignment(String name,
                                                                                LabelType type)
    {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = 
                LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_CENTER;
        //check for message to self, consider message to self always right oriented (in current realization)
        MessageWidget msg=getMessage();
        Widget pin1=msg.getSourceAnchor().getRelatedWidget();
        Widget pin2=msg.getTargetAnchor().getRelatedWidget();
        boolean toself=pin1.getParentWidget()==pin2.getParentWidget().getParentWidget() || pin1.getParentWidget().getParentWidget()==pin2.getParentWidget();
        if(toself)retVal=LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_RIGHT;
        //
        return retVal;
    }
    
    
    //////////////////////////////////////////////////////////////////
    // Helper Methods

    private void createNameMessageLabel(IMessage msg, boolean assignDefaultName)
    {
        String name = msg.getNameWithAlias();
        if((name != null) && (name.length() > 0))
        {
            showLabel(NAME);
        }
    }
    private void createOperationMessageLabel(IMessage msg, boolean assignDefaultName)
    {
        IOperation op=msg.getOperationInvoked();
        if(op != null)
        {
            showLabel(OPERATION);
        }
    }
    
    @Override
    public void showLabel(String name, LabelType type)
    {
         super.showLabel(name, type);
         if(!NAME.equals(name))hideLabel(NAME);
         if(!OPERATION.equals(name))hideLabel(OPERATION);
    }

    
    private void updateLabel(LabelType type, String name, String value)
    {
        if ((name == null) || (name.length() == 0))
        {
        }
        else
        {

            if (isVisible(name, type) == true)
            {
                if(name.equals(NAME))
                {
                    EditableCompartmentWidget widget = (EditableCompartmentWidget) getLabel(name, type);
                    if (widget != null)
                    {
                        boolean shownumbers=((DesignerScene) getScene()).getEngine().getSettingValue(SequenceDiagramEngine.SHOW_MESSAGE_NUMBERS)==Boolean.TRUE;
                        String text = ((IMessage)getModelElement()).getNameWithAlias();
                        if(shownumbers && ((IMessage)getModelElement()).getAutoNumber()!=null)text=((IMessage)getModelElement()).getAutoNumber()+": "+text;
                        if((text==null || text.length()<1))text=retrieveDefaultName();
                        widget.setLabel(text);
                    }
                }
                else if(name.equals(OPERATION))
                {
                    OperationLabelWidget widget = (OperationLabelWidget) getLabel(name, type);
                    if (widget != null)
                    {
                        widget.setLabel(value);
                    }
                }
            }
            else
            {
                showLabel(name, type);
            }
        }
    }
}