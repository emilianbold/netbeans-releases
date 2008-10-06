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
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.LabelManager.LabelType;
import org.netbeans.modules.uml.drawingarea.actions.ToggleLabelAction;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.openide.util.NbBundle;


/**
 *
 * @author treyspiva
 */
public class DerivationLabelManager extends BasicUMLLabelManager
{
    ResourceBundle bundle = NbBundle.getBundle(DerivationLabelManager.class);
    public DerivationLabelManager(ConnectionWidget widget)
    {
        super(widget);
    }
    
    //////////////////////////////////////////////////////////////////
    // LableManager Overrides

    public void createInitialLabels()
    {
        super.createInitialLabels();
        
        IElement element = getModelElement();
        if(element instanceof IDerivation)
        {
            showLabel(BINDING);
        }
    }

    @Override
    protected Widget createLabel(String name, LabelType type)
    {
        String text = "";
        Widget retVal = null;
        
        if(name.equals(BINDING) == true)
        {
            LabelWidget label = new UMLLabelWidget(getScene());
            label.setLabel(buildBindingLabel());
            retVal = label;
        } 
        else if (name.equals(BasicUMLLabelManager.NAME))
        {
            retVal = super.createLabel(name, type);
        }
        return retVal;
    }
    
    protected String buildBindingLabel()
    {
        IDerivation element = (IDerivation) getModelElement();
        String retVal = "<<binding>> "; // NOI18N
        int len0 = retVal.length();
         
        ETList < IUMLBinding > pBindings = element.getBindings();
        String sFormalName = "";
	String sActualName = "";
        
        for(IUMLBinding binding : pBindings)
        {
            if ( binding != null)
            {
                if (retVal.length() > len0)
                {
                    retVal += ", ";
                }
                
                sFormalName = binding.getFormal().getNameWithAlias();
                sActualName = binding.getActual().getNameWithAlias();
                
                if (sFormalName != null && sFormalName.length() > 0) 
                {
                    retVal += binding.getFormal().getNameWithAlias();
                    retVal += "::"; // NO18N
                }
                if (sActualName != null)
                {
                    if (sActualName.length() == 0)
                    {
                        sActualName = "int";
                    }
                    retVal += sActualName;
                }
            }
        }

        //retVal += ">"; // NOI18N
        return retVal;
    }
    
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propName = evt.getPropertyName();
        
//        if(propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
//        {
//            INamedElement element = (INamedElement) getModelElement();
//            String name = element.getNameWithAlias();
//            updateLabel(LabelType.EDGE, NAME, name);
//        }
        super.propertyChange(evt);
        updateLabel(LabelType.EDGE, BINDING, buildBindingLabel());
    }
    
    @Override
    public Action[] getContextActions(LabelType type)
    {
        List<Action> actions = new ArrayList<Action>();
        if (type == LabelType.EDGE)
        {
            boolean visible =  isVisible(BINDING, type);
            String messageKey = visible ? "LBL_HIDE_BINDING" : "LBL_SHOW_BINDING";
            EnumSet<LabelType> labelType = EnumSet.of(type);
            ToggleLabelAction showHideBindingAction = new ToggleLabelAction(this,
                                                                                   BINDING,
                                                                                   labelType,
                                                                                   bundle.getString(messageKey));
            visible =  isVisible(NAME, type);
            messageKey = visible ? "LBL_HIDE_EDGE_NAME" : "LBL_SHOW_EDGE_NAME";
            ToggleLabelAction showHideNameAction = new ToggleLabelAction(this,
                                                                         NAME,
                                                                         labelType,
                                                                         bundle.getString(messageKey));
            actions.add(showHideBindingAction);
            actions.add(showHideNameAction);
        }
        Action[] retVal = new Action[actions.size()];
        actions.toArray(retVal);
        return retVal;
    }
}
