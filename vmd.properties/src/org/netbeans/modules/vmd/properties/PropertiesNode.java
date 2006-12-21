/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.properties.GroupPropertyEditor;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.api.properties.PropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignPropertyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;

/**
 * @author KarolHarezlak
 */
public final  class PropertiesNode extends AbstractNode {
    
    private Sheet sheet;
    private List<DesignPropertyDescriptor> designerPropertyDescriptors;
    private List<String> categories;
    private Comparator<DesignPropertyDescriptor> compareByDisplayName = new Comparator<DesignPropertyDescriptor>() {
        public int compare(DesignPropertyDescriptor descriptor1, DesignPropertyDescriptor descriptor2) {
            
            return descriptor1.getPropertyDisplayName().compareTo(descriptor2.getPropertyDisplayName());
        }
    };
    
    public PropertiesNode(DesignComponent component) {
        super(Children.LEAF);
        designerPropertyDescriptors = new ArrayList<DesignPropertyDescriptor>();
        categories = new ArrayList<String>();
        for (PropertiesPresenter propertiesPresenter : component.getPresenters(PropertiesPresenter.class)) {
            designerPropertyDescriptors.addAll(propertiesPresenter.getDesignerPropertyDescriptors());
            categories.addAll(propertiesPresenter.getPropertiesCategories());
        }
        if (designerPropertyDescriptors != null)
            Collections.sort(designerPropertyDescriptors, compareByDisplayName);
    }
    
    protected Sheet createSheet() {
        sheet = sheet == null ? new Sheet() : sheet;
        createCategoriesSet();
        for (DesignPropertyDescriptor designerPropertyDescriptor : designerPropertyDescriptors) {
            Node.Property property;
            DesignPropertyEditor propertyEditor = designerPropertyDescriptor.getPropertyEditor();
            
            if (propertyEditor instanceof GroupPropertyEditor && designerPropertyDescriptor.getPropertyNames().size() == 0)
                throw new IllegalStateException("To use AdvancedPropertyEditorSupport you need to specific at least one propertyName");
            
            if (propertyEditor instanceof GroupPropertyEditor)
                property = new AdvancedPropertySupport(designerPropertyDescriptor, designerPropertyDescriptor.getPropertyEditorType());
            else if (designerPropertyDescriptor.getPropertyNames().size() <= 1)
                property = new PrimitivePropertySupport(designerPropertyDescriptor, designerPropertyDescriptor.getPropertyEditorType());
            else {
                throw new IllegalArgumentException("Defualt PropertyEditor: "+ designerPropertyDescriptor.getPropertyDisplayName() + " " +
                    designerPropertyDescriptor.getPropertyEditorType() + " cant be maped one to many with DesignComponent property: use DefaultPropertyEditorSupport");
            }
            
            if (propertyEditor != null && (! propertyEditor.canEditAsText()) )
                property.setValue("canEditAsText", false);
            property.setValue("changeImmediate", false); // NOI18
            sheet.get(designerPropertyDescriptor.getPropertyCategory()).put(property);
        }
        
        return sheet;
    }
    
    private  Sheet.Set createPropertiesSet(String categoryName) {
        Sheet.Set setSheet = new Sheet.Set();
        setSheet.setName(categoryName);
        setSheet.setDisplayName(categoryName);
        
        return setSheet;
    }
    
    private  void createCategoriesSet() {
        for (String propertyCategory : categories) {
            PropertiesNode.this.sheet.put(createPropertiesSet(propertyCategory));
        }
    }
    
}
