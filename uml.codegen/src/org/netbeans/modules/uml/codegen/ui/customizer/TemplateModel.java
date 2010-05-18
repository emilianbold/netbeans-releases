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

package org.netbeans.modules.uml.codegen.ui.customizer;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.netbeans.modules.uml.codegen.ui.customizer.TemplateEntry;
import org.netbeans.modules.uml.codegen.ui.customizer.TabbedPanelModel;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Family;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamiliesHandler;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;

/**
 *
 * @author treyspiva
 */

public class TemplateModel extends TabbedPanelModel
{
    private TemplateFamiliesHandler handler = TemplateFamiliesHandler.getInstance(true);
    private UMLProjectProperties umlProjectProperties = null;
    private List<String> checkedTree = null;
    
    public TemplateModel(UMLProjectProperties umlPrjProps)
    {
        setUMLProjectProperties(umlPrjProps);
        propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    public List<String> getCategories()
    {
        List<String> categories = new ArrayList<String>();
        Family[] families = handler.getTemplateFamilies().getFamily();
        
        for (int i=0; i < families.length; i++)
        {
            categories.add(families[i].getName());
        }

        return categories;
    }

    public String getToolTip(String category)
    {
        return ""; // NOI18N
    }

    public JComponent getPanel(String category)
    {
        DomainObject[] domains = null;
        String familyName = null;
        Family[] families = handler.getTemplateFamilies().getFamily();
        
        for (int i=0; i < families.length; i++)
        {
            if (families[i].getName().equals(category))
            {
                domains = families[i].getDomainObject();
                familyName = families[i].getName();
                break;
            }
        }

        Box templateList = Box.createVerticalBox();
        for (DomainObject domain : domains)
        {
            TemplateEntry entry = new TemplateEntry(familyName, domain, 
                fetchCheckedValue(familyName, domain.getName()));
            
            addTemplateEntryCheckEventListener(entry);
            
            templateList.add(entry);
        }

        return templateList;
    }

    
    private boolean fetchCheckedValue(String familyName, String domainName)
    {
        return checkedTree.indexOf(familyName + ':' + domainName) != -1;
    }
    
    private void updateCheckedValue(
        String familyName, String domainName, boolean isChecked)
    {
        if (checkedTree.indexOf(familyName + ':' + domainName) == -1 &&
            isChecked)
        {
            checkedTree.add(familyName + ':' + domainName);
        }
        
        else if (checkedTree.indexOf(familyName + ':' + domainName) != -1 &&
            !isChecked)
        {
            checkedTree.remove(familyName + ':' + domainName);
        }
    }
    
    public void setUMLProjectProperties(UMLProjectProperties val)
    {
        umlProjectProperties = val;
        checkedTree = umlProjectProperties.getCodeGenTemplatesArray();
    }

    public UMLProjectProperties getUMLProjectProperties()
    {
        umlProjectProperties.setCodeGenTemplates(checkedTree);
        return umlProjectProperties;
    }

    private void addTemplateEntryCheckEventListener(TemplateEntry entry)
    {
        entry.getTemplateNameField().addItemListener(new java.awt.event.ItemListener() 
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt) 
            {
                JCheckBox checkBox = (JCheckBox)evt.getSource();
                
                updateCheckedValue(
                    checkBox.getClientProperty("familyName").toString(), // NOI18N
                    checkBox.getText(), 
                    checkBox.isSelected());
                
                getPropertyChangeSupport().firePropertyChange(
                    PROP_TEMPLATE_STATE_CHANGE, null, evt);
                
                if (checkBox.isSelected() && checkedTree.size() == 1)
                {
                    getPropertyChangeSupport().firePropertyChange(
                        PROP_ONE_TEMPLATE_ENABLED, null, evt);
                }
                
                else if (!checkBox.isSelected() && checkedTree.size() == 0)
                {
                    getPropertyChangeSupport().firePropertyChange(
                        PROP_NO_TEMPLATES_ENABLED, null, evt);
                }
            }
        });
    }    
    
    public final static String PROP_TEMPLATE_STATE_CHANGE = "TEMPLATE_STATE_CHANGE"; // NOI18N
    public final static String PROP_NO_TEMPLATES_ENABLED = "NO_TEMPLATES_ENABLED"; // NOI18N
    public final static String PROP_ONE_TEMPLATE_ENABLED = "ONE_TEMPLATE_ENABLED"; // NOI18N

    private PropertyChangeSupport propertyChangeSupport = null;
    
    public PropertyChangeSupport getPropertyChangeSupport()
    {
        return propertyChangeSupport;
    }
}
