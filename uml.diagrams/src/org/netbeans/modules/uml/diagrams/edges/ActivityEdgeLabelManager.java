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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.edges;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager.LabelType;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.actions.ToggleLabelAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Thuy
 */
public class ActivityEdgeLabelManager extends BasicUMLLabelManager
{
    ResourceBundle bundle = NbBundle.getBundle(ActivityEdgeLabelManager.class);

    public ActivityEdgeLabelManager(ConnectionWidget widget)
    {
        super(widget);
    }

    @Override
    protected Widget createLabel(String name, LabelType type)
    {
        String text = "";
        Widget retVal = null;

        if (name.equals(GUARD_CONDITION))
        {
            retVal = createGuardLabel();
        } else if (name.equals(BasicUMLLabelManager.NAME))
        {
            retVal = super.createLabel(name, type);
        }
        return retVal;
    }

    public void createInitialLabels()
    {
        super.createInitialLabels();
        
        IElement pElement = getModelElement();

        IActivityEdge pActivityEdge = pElement instanceof IActivityEdge ? (IActivityEdge) pElement : null;
        if (pActivityEdge != null)
        {
            IValueSpecification pGuard = pActivityEdge.getGuard();
            if(pGuard != null)
            {
                showLabel(GUARD_CONDITION);
            }
        }
    }
    
    public Widget createGuardLabel()
    {
        Widget retVal = null;
        // Get the text to display
        ETPairT<String, IExpression> result = getGuardText();

        String expressionStr = result.getParamOne();
        IExpression expressionElem = result.getParamTwo();

        if (expressionStr != null && expressionStr.length() > 0 && expressionElem != null)
        {
            EditableCompartmentWidget editable = new EditableCompartmentWidget(getScene(),
                                                                               expressionElem,
                                                                               null,
                                                                               null);
            editable.setLabel(expressionStr);
            retVal = editable;
        }
        return retVal;
    }

    public ETPairT<String, IExpression> getGuardText()
    {
        String sText = null;
        IExpression pFoundExpression = null;
        IElement pElement = getModelElement();
        String sGuardWithBrackets = "";

        IActivityEdge pActivityEdge = pElement instanceof IActivityEdge ? (IActivityEdge) pElement : null;
        if (pActivityEdge != null)
        {
            IValueSpecification pGuard = pActivityEdge.getGuard();
            IExpression pExpression = (pGuard instanceof IExpression) ? (IExpression) pGuard : null;

            if (pExpression != null)
            {
                sGuardWithBrackets = pExpression.getBody();
                pFoundExpression = pExpression;
            } else
            {
                String defaultName = retrieveDefaultName();

                if (defaultName != null && defaultName.length() > 0)
                {
                    IExpression pNewExpression = new TypedFactoryRetriever<IExpression>().createType("Expression");
                    if (pNewExpression != null)
                    {
                        pNewExpression.setBody(defaultName);
                        pActivityEdge.setGuard(pNewExpression);
                        sGuardWithBrackets = pNewExpression.getBody();
                        pFoundExpression = pNewExpression;
                    }
                }
            }
        }
        if (sGuardWithBrackets != null)
        {
            String newGuard = "[";
            newGuard += sGuardWithBrackets;
            newGuard += "]";
            sText = newGuard;
        }

        return new ETPairT<String, IExpression>(sText, pFoundExpression);
    }

    public void updateGuardLabel(LabelType type, String name)
    {
        if (type != null && name != null)
        {
            ETPairT<String, IExpression> result = getGuardText();
            String expressionStr = result.getParamOne();

            if (isVisible(name, type))
            {
                Widget widget = getLabel(name, type);
                if (widget != null && widget instanceof EditableCompartmentWidget)
                {
                    ((EditableCompartmentWidget) widget).setLabel(expressionStr);
                }
            }
        }
    }

//    @Override
//    protected Object createAttachedData(String name, LabelType type)
//    {
//        Object retVal = null;
//        
//        if (type == LabelType.EDGE && name.equals(GUARD_CONDITION))
//        {
//            ETPairT<String, IExpression> result = getGuardText();
//            IExpression expressionElem = result.getParamTwo();
//            retVal = new ModelElementBridge(expressionElem);
//        } else
//        {
//            retVal = new ModelElementBridge(getModelElement());
//        }
//        return retVal;
//    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propName = evt.getPropertyName();
        Object src = evt.getSource();
        if (propName.equals(ModelElementChangedKind.ACTIVITYEDGE_GUARDMODIFIED.toString()))
        {
            this.updateGuardLabel(LabelType.EDGE, GUARD_CONDITION);
        } else
        {
            super.propertyChange(evt);
        }
    }

    @Override
    public Action[] getContextActions(LabelType type)
    {
        List<Action> actions = new ArrayList<Action>();
        if (type == LabelType.EDGE)
        {
            boolean visible =  isVisible(GUARD_CONDITION, type);
            String messageKey = visible ? "LBL_HIDE_GUARD_CONDITION" : "LBL_SHOW_GUARD_CONDITION";
            EnumSet<LabelType> labelType = EnumSet.of(type);
            ToggleLabelAction showHideGuardContidionAction = new ToggleLabelAction(this,
                                                                                   GUARD_CONDITION,
                                                                                   labelType,
                                                                                   bundle.getString(messageKey));
            visible =  isVisible(NAME, type);
            messageKey = visible ? "LBL_HIDE_EDGE_NAME" : "LBL_SHOW_EDGE_NAME";
            ToggleLabelAction showHideNameAction = new ToggleLabelAction(this,
                                                                         NAME,
                                                                         labelType,
                                                                         bundle.getString(messageKey));
            actions.add(showHideGuardContidionAction);
            actions.add(showHideNameAction);
        }
        Action[] retVal = new Action[actions.size()];
        actions.toArray(retVal);
        return retVal;
    }
}
