/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.diagrams.DefaultWidgetContext;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.view.Customizable;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author treyspiva
 */
public class TemplateWidget extends LabelWidget implements Customizable
{

    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);
    private ResourceType[] customizableResTypes = Customizable.DEFAULT_RESTYPES;

    public TemplateWidget(Scene scene)
    {
        super(scene);

        lookupContent.add(new DefaultWidgetContext("TemplateParameter"));
        setOpaque(true);
        ResourceValue.initResources(getID(), this);
//        setBackground(Color.WHITE);
    }

    @Override
    public Lookup getLookup()
    {
        return lookup;
    }
    
    protected void updateUI(IClassifier classifier)
    {
        removeChildren();

        setLabel(classifier.getTemplateParametersAsString());
        
        setBorder(BorderFactory.createDashedBorder(Color.BLACK, 5, 5, true));
        setAlignment(Alignment.CENTER);
    }

    ///////////////////////////////////////////////////////////////
    // PropertyChangeListener Implementation
    /**
     * The property change listener is used by the diagram to notify widgets of
     * model element property change events.
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if(event.getSource() instanceof IParameterableElement)
        {
            String eventType = event.getPropertyName();
            if(eventType.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
            {
                IParameterableElement element = (IParameterableElement) event.getSource();
                IClassifier template = element.getTemplate();

                if(template != null)
                {
                    setLabel(template.getTemplateParametersAsString());
                }
            }
        }
    }

    public void save(NodeWriter nodeWriter)
    {
    }

    ////////////////////////////////////////////////////////////////////////////
    // Customizable Implementation
    
    public String getID()
    {
        return UMLWidgetIDString.UMLCLASSWIDGET.toString() + ".TemplateParameters"; 
    }

    public String getDisplayName()
    {
        return "Template Parameters";
    }

    public void update()
    {
        
    }

    public void setCustomizableResourceTypes(ResourceType[] resTypes)
    {
        customizableResTypes = resTypes;
    }

    public ResourceType[] getCustomizableResourceTypes()
    {
        return customizableResTypes;
    }
}
