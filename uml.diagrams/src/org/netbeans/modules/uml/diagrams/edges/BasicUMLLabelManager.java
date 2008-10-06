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
import org.netbeans.api.visual.widget.ConnectionWidget;
//import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.drawingarea.AbstractLabelManager;
import org.netbeans.modules.uml.drawingarea.LabelManager.LabelType;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.actions.ToggleLabelAction;
import org.netbeans.modules.uml.drawingarea.support.ModelElementBridge;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.openide.util.NbBundle;


/**
 * The BasicUMLLabelManager provides the labels that all UML connection 
 * widget should provide.
 * 
 * @author treyspiva
 */
public class BasicUMLLabelManager extends AbstractLabelManager
{
    private String keywords;
    
    public BasicUMLLabelManager(ConnectionWidget widget)
    {
        this(widget,null);
    }
    public BasicUMLLabelManager(ConnectionWidget widget,String keywordsAsString)
    {
        super(widget);
        keywords=keywordsAsString;
    }
    
    //////////////////////////////////////////////////////////////////
    // LableManager Overrides

    public void createInitialLabels()
    {
        IElement element = getModelElement();
        if(element instanceof INamedElement)
        {
            INamedElement namedElement = (INamedElement)element;
            createNameLabel(namedElement, false);
            createStereotypeLabel(namedElement, false);
            
        }
    }

    public Action[] getContextActions(LabelType type)
    {
        ArrayList < Action > actions = new ArrayList < Action >();
        ResourceBundle bundle = NbBundle.getBundle(BasicUMLLabelManager.class);
        
        if(type == LabelType.EDGE)
        {
            EnumSet < LabelType > thisEndType = EnumSet.of(type);
            IElement element = getModelElement();
            if(element instanceof INamedElement)
            {
                ToggleLabelAction nameAction = new ToggleLabelAction(this, 
                                                                 NAME, 
                                                                 thisEndType, 
                                                                 bundle.getString("LBL_NAME_LABEL"));

                actions.add(nameAction);
            }
            
            List stereotypes = element.getAppliedStereotypes();
            if(stereotypes.size() > 0 || keywords!=null)
            {
                 ToggleLabelAction stereotypeAction = new ToggleLabelAction(this, 
                                                             STEREOTYPE, 
                                                             thisEndType, 
                                                             bundle.getString("LBL_STEREOTYPE_LABEL"));
            
                actions.add(stereotypeAction);
            }
        }
        
        Action[] retVal = new Action[actions.size()];
        actions.toArray(retVal);
        return retVal;
    }
    
    //////////////////////////////////////////////////////////////////
    // ProepertyChangeListener Implementation
    
    /**
     * Listen to property changes that occur to the model.  If one of the
     * standard properties are modified, the label is automatically updated.
     * 
     * @param evt the property change information.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propName = evt.getPropertyName();
        
        if(propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
        {
            INamedElement element = (INamedElement) getModelElement();
            String name = element.getNameWithAlias();
            updateLabel(LabelType.EDGE, NAME, name);
        }
        else if(propName.equals(ModelElementChangedKind.STEREOTYPE.toString()))
        {
            IElement element = getModelElement();
            String stereotype = element.getAppliedStereotypesAsString(true);
            if(((stereotype != null) && (stereotype.length() > 0)) || (keywords!=null))
            {
                String txt="";
                if(stereotype!=null && stereotype.length() > 0)
                {
                    txt+=stereotype;
                }
                if(keywords!=null)
                {
                    if(txt.length()>0)
                    {
                        txt="<<"+keywords+","+txt.substring(2);
                    }
                    else
                    {
                        txt="<<"+keywords+">>";
                    }
                }
                updateLabel(LabelType.EDGE, STEREOTYPE, txt);
            }
            else
            {
                hideLabel(STEREOTYPE);
            }
        }
    }
    
    //////////////////////////////////////////////////////////////////
    // AbstractLabelManager Overrides
    
    protected Widget createLabel(String name, LabelType type)
    {
        String text = "";
        Widget retVal = null;
        
        if(name.equals(NAME) == true)
        {
            INamedElement element = (INamedElement) getModelElement();
            text = element.getNameWithAlias();
            if((text == null) || (text.length() == 0))
            {
                text = retrieveDefaultName();
            }
            
            EditableCompartmentWidget widget = new EditableCompartmentWidget(getScene(), element, "dummy", text);
            widget.setLabel(text);
            retVal = widget;
        }
        else if(name.equals(STEREOTYPE) == true)
        {
            IElement element = getModelElement();
            
            UMLLabelWidget label = new UMLLabelWidget(getScene(), "dummy", null);
            String stereotype = element.getAppliedStereotypesAsString(true);
            String txt="";
            if(stereotype!=null && stereotype.length() > 0)
            {
                txt+=stereotype;
            }
            if(keywords!=null)
            {
                if(txt.length()>0)
                {
                    txt="<<"+keywords+","+txt.substring(2);
                }
                else
                {
                    txt="<<"+keywords+">>";
                }
            }
            label.setLabel(txt);
            retVal = label;
        }
        
        
        return retVal;
    }
    
    protected Object createAttachedData(String name, LabelType type)
    {
        return new ModelElementBridge((getModelElement()));
    }
    
    protected LayoutFactory.ConnectionWidgetLayoutAlignment getDefaultAlignment(String name,
                                                                                LabelType type)
    {
        return LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_CENTER;
    }
    
    
    //////////////////////////////////////////////////////////////////
    // Helper Methods

    /**
     * If the name property is set, the name label will be shown.
     * 
     * @param element the associated model element.
     * @param assignDefaultName if true a default name will be used.
     */
    private void createNameLabel(INamedElement element, boolean assignDefaultName)
    {
        String name = element.getNameWithAlias();
        if((name != null) && (name.length() > 0))
        {
            showLabel(NAME);
        }
    }
    /**
     * If the stereotype property is set, the name label will be shown.
     * 
     * @param element the associated model element.
     * @param assignDefaultName if true a default name will be used.
     */
    private void createStereotypeLabel(IElement element, boolean assignDefaultName)
    {
        String name = element.getAppliedStereotypesAsString(true);
        if(((name != null) && (name.length() > 0)) || (keywords!=null))
        {
            showLabel(STEREOTYPE);
        }
    }

    /**
     * Updates a label.
     * 
     * @param type the type of label.
     * @param name the name of the label.
     * @param value the new value of the label.
     */
    protected void updateLabel(LabelType type, String name, String value)
    {
        if ((name == null) || (name.length() == 0))
        {
            hideLabel(name, type);
        }
        else
        {
            if (isVisible(name, type) == true)
            {
                Widget widget = getLabel(name, type);
                if(widget instanceof EditableCompartmentWidget)
                {
                    EditableCompartmentWidget editable = (EditableCompartmentWidget) widget;
                    if (editable != null)
                    {
                        editable.setLabel(value);
                    }
                }
                else if(widget instanceof UMLLabelWidget)
                {
                    UMLLabelWidget label = (UMLLabelWidget) widget;
                    if (label != null)
                    {
                        label.setLabel(value);
                    }
                }
            }
            else
            {
                showLabel(name, type);
            }
        }
    }
    
    public String getKeywords()
    {
        return keywords!=null ? "<<"+keywords+">>" : null;
    }
}