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
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.drawingarea.LabelManager.LabelType;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;


/**
 *
 * @author treyspiva
 */
public class DerivationLabelManager extends BasicUMLLabelManager
{
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
        
        
        return retVal;
    }
    
    protected String buildBindingLabel()
    {
        IDerivation element = (IDerivation) getModelElement();
        String retVal = "<<binding>> <"; // NOI18N
        int len0=retVal.length();
            
        for(IUMLBinding binding : element.getBindings())
        {
            if(retVal.length()>len0)retVal+=", ";
            retVal += binding.getFormal().getNameWithAlias();
            retVal += "->"; // NO18N
            retVal += binding.getActual().getNameWithAlias();
        }

        retVal += ">"; // NOI18N
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
}
