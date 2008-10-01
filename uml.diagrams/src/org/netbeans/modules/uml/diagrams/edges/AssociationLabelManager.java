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
package org.netbeans.modules.uml.diagrams.edges;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager.LabelType;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.actions.ToggleLabelAction;
import org.netbeans.modules.uml.drawingarea.support.ModelElementBridge;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.openide.util.NbBundle;


/**
 *
 * @author treyspiva
 */
public class AssociationLabelManager extends BasicUMLLabelManager
{
    
    public AssociationLabelManager(AssociationConnector widget)
    {
        super(widget);
    }
    
    //////////////////////////////////////////////////////////////////
    // LableManager Overrides

    public void createInitialLabels()
    {
        super.createInitialLabels();
        
        IElement element = getModelElement();
        if(element instanceof IAssociation)
        {
            IAssociation assoc = (IAssociation)element;
            List<IAssociationEnd> pEnds = null;
            int numEnds = 0;

            pEnds = assoc.getEnds();

            numEnds = assoc.getNumEnds();

            if (pEnds != null && numEnds == 2)
            {
                IAssociationEnd pEnd0 = pEnds.get(0);
                IAssociationEnd pEnd1 = pEnds.get(1);

                if (pEnd0 != null)
                {
                    createAssociationEndNameLabel(pEnd0);
                    createAssociationEndMultiplicityLabel(pEnd0);
                }
                if (pEnd1 != null)
                {
                    createAssociationEndNameLabel(pEnd1);
                    createAssociationEndMultiplicityLabel(pEnd1);
                }
            }
            
        }
    }

    public Action[] getContextActions(LabelType type)
    {
        ArrayList < Action > actions = new ArrayList < Action >();
        
        ResourceBundle bundle = NbBundle.getBundle(AssociationLabelManager.class);
        
        if(type != LabelType.EDGE)
        {
            EnumSet < LabelType > thisEndType = EnumSet.of(type);
            ToggleLabelAction thisEndNameAction = new ToggleLabelAction(this, 
                                                             END_NAME, 
                                                             thisEndType, 
                                                             bundle.getString("LBL_THIS_END_NAME"));
            
            ToggleLabelAction thisMultiplicityAction = new ToggleLabelAction(this, 
                                                             MULTIPLICITY, 
                                                             thisEndType, 
                                                             bundle.getString("LBL_THIS_MULTIPLICITY"));
            
            actions.add(thisEndNameAction);
            actions.add(thisMultiplicityAction);
        }
        
        ToggleLabelAction linkNameAction = new ToggleLabelAction(this, 
                                                             NAME, 
                                                             EnumSet.of(LabelType.EDGE), 
                                                             bundle.getString("LBL_ASSOCATION_EDGE"));

        EnumSet < LabelType > sourceTargetSet = EnumSet.of(LabelType.SOURCE, LabelType.TARGET);
        ToggleLabelAction bothEndNameAction = new ToggleLabelAction(this, 
                                                             END_NAME, 
                                                             sourceTargetSet, 
                                                             bundle.getString("LBL_BOTH_END_NAMES"));
        ToggleLabelAction bothMultiplicityAction = new ToggleLabelAction(this, 
                                                             MULTIPLICITY, 
                                                             sourceTargetSet, 
                                                             bundle.getString("LBL_BOTH_MULTIPLICITY"));
        actions.add(linkNameAction);
        actions.add(bothEndNameAction);
        actions.add(bothMultiplicityAction);

        Action[] retVal = new Action[actions.size()];
        actions.toArray(retVal);
        return retVal;
    }
    
    //////////////////////////////////////////////////////////////////
    // ProepertyChangeListener Implementation
    
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propName = evt.getPropertyName();
        
        if(evt.getSource() instanceof IAssociationEnd)
        {
            IAssociationEnd end = (IAssociationEnd) evt.getSource();
            LabelType type = LabelType.TARGET;
            if(AssociationConnector.isSourceEnd(getConnector(), end) == true)
            {
                type = LabelType.SOURCE;
            }
            
            if(propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
            {
                String name = end.getNameWithAlias();
                updateLabel(type, END_NAME, name);
            }
            else if(propName.equals(ModelElementChangedKind.MULTIPLICITYMODIFIED.toString()))
            {
                String name = getLabelText(MULTIPLICITY, end, false);
                updateLabel(type, MULTIPLICITY, name);
            }
        }
        else 
        {
            super.propertyChange(evt);
        }
    }
    
    //////////////////////////////////////////////////////////////////
    // AbstractLabelManager Overrides
    
    protected Widget createLabel(String name, LabelType type)
    {
        String text = "";
        IElement element = null;
        
        switch(type)
        {
            case SOURCE:
                IAssociationEnd source = getSourceEnd();
                text = getLabelText(name, source, true);
                element = getLabelElement(source, name);
                break;
            case TARGET:
                IAssociationEnd target = getTargetEnd();
                text = getLabelText(name, target, true);
                element = getLabelElement(target, name);
                break;
        }
        
        Widget retVal = null;
        if(element != null)
        {
            EditableCompartmentWidget editable = new EditableCompartmentWidget(getScene(), element, "associationLabel", "Association Label");
            editable.setLabel(text);
            retVal = editable;
        }
        else
        {
            retVal = super.createLabel(name, type);
        }
        
        return retVal;
    }
    
    protected Object createAttachedData(String name, LabelType type)
    {
        Object retVal = new ModelElementBridge(getModelElement());
        
        switch(type)
        {
            case SOURCE:
                retVal = new ModelElementBridge(getSourceEnd());
                break;
            case TARGET:
                retVal = new ModelElementBridge(getTargetEnd());
                break;
        }
        
        return retVal;
    }
    
    protected LayoutFactory.ConnectionWidgetLayoutAlignment getDefaultAlignment(String name,
                                                                                LabelType type)
    {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = 
                LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_CENTER;
        
        if(name.equals(MULTIPLICITY) == true)
        {
            if(type == LabelType.SOURCE)
            {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_SOURCE;
            }
            else
            {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_TARGET;
            }
        }
        else if(name.equals(END_NAME) == true)
        {
            if(type == LabelType.SOURCE)
            {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_SOURCE;
            }
            else
            {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_TARGET;
            }
        }
        
        return retVal;
    }
    
    
    //////////////////////////////////////////////////////////////////
    // Helper Methods
    
    /**
     * If the ends multiplicity property is set, the multiplicity label will be shown.
     * 
     * @param element the associated model element.
     */
    private void createAssociationEndMultiplicityLabel(IAssociationEnd end)
    {
        IMultiplicity mult = end.getMultiplicity();

        String multString = null;
        if( mult != null)
        {
            multString = mult.getRangeAsString(false);
        }
        
        if((multString != null) &&  (multString.length() > 0))
        {
            showLabel(MULTIPLICITY, getEndTypeFor(end));
        }
    }
    
    /**
     * If the ends name property is set, the ends name label will be shown.
     * 
     * @param element the associated model element.
     */
    private void createAssociationEndNameLabel(IAssociationEnd end)
    {
        String text = getLabelText(END_NAME, end, false);
        if((text != null) && (text.length() > 0))
        {
            showLabel(END_NAME, getEndTypeFor(end));
        }
    }
    
    /**
     * Determines if the end is on the target or source end of the connection
     * widget.
     * 
     * @param end the end to test
     * @return the LabelType associated with the end.
     */
    private LabelType getEndTypeFor(IAssociationEnd end)
    {
        LabelType type = LabelType.TARGET;
        if(AssociationConnector.isSourceEnd(getConnector(), end) == true)
        {
            type = LabelType.SOURCE;
        }
        
        return type;
    }

    /**
     * Retrieves the source end of the connection widget.
     * @return the source association end.
     */
    private IAssociationEnd getSourceEnd()
    {
        Widget w = getConnector().getSourceAnchor().getRelatedWidget();
        return findAssociatedEnd(w);
    }

    /**
     * Retrieves the target end of the connection widget.
     * @return the target association end.
     */
    private IAssociationEnd getTargetEnd()
    {
        Widget w = getConnector().getTargetAnchor().getRelatedWidget();
        return findAssociatedEnd(w);
    }
    
    /**
     * Retrieves the association end that is connected to the specified
     * model element.
     * 
     * @param connectedToWidget the widget connected to the association.
     * @return the association end.
     */
    private IAssociationEnd findAssociatedEnd(Widget connectedToWidget)
    {
        IAssociationEnd retVal = null;
        
        if (connectedToWidget.getScene() instanceof ObjectScene)
        {
            // First get the presentation element that represents the node
            // on the source side.
            ObjectScene scene = (ObjectScene) connectedToWidget.getScene();
            IPresentationElement element = (IPresentationElement) scene.findObject(connectedToWidget);
            
            // Next find which end is connected to the source element.
            
            IAssociation assoc = (IAssociation) getModelElement();
            
            for(IAssociationEnd end : assoc.getEnds())
            {
                if(end.isSameParticipant(element.getFirstSubject()) == true)
                {
                    retVal = end;
                    break;
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Retrieves the model element for the model elements property.
     * 
     * @param data the association end
     * @param property the property of the model eleent.  Either MULTIPLICITY 
     *        or END_NAME.
     * @return the associated model element.
     */
    private IElement getLabelElement(IAssociationEnd data, String property)
    {
        IElement retVal = null;
        
        if(property.equals(MULTIPLICITY) == true)
        {
            retVal = data.getMultiplicity();
        }
        else if(property.equals(END_NAME) == true)
        {
            retVal = data;
        }
        
        return retVal;
    }
    
    /**
     * Retrieves the label for the associated model element property.
     * 
     * @param property the property of the model eleent.  Either MULTIPLICITY 
     *        or END_NAME.
     * @param end the association end
     * @param assignDefaultValue if true a default value is assigned
     * @return
     */
    private String getLabelText(String property, 
                                IAssociationEnd end,
                                boolean assignDefaultValue)
    {
        String retVal = "";
        
        if(property.equals(MULTIPLICITY) == true)
        {
            IMultiplicity mult = end.getMultiplicity();

            if( mult != null)
            {
                retVal = mult.getRangeAsString(false);
            }

            if((retVal == null || retVal.length() == 0) && 
                (assignDefaultValue == true))
            {
                mult.setRange2("1", "1");
                retVal = mult.getRangeAsString(false);
            }
        }
        else if(property.equals(END_NAME) == true)
        {
            retVal = end.getNameWithAlias();
            if((retVal == null) || (retVal.length() <= 0))
            {
                if(assignDefaultValue == true)
                {
                    retVal = retrieveDefaultName();
                }
            }
        }
        
        return retVal;
    }
}