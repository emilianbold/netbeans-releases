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

package org.netbeans.modules.vmd.api.properties.common;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.properties.DesignPropertyDescriptor;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.api.properties.GroupPropertyEditor;
import org.netbeans.modules.vmd.api.properties.PropertiesPresenter;
import org.netbeans.modules.vmd.properties.AdvancedPropertySupport;
import org.netbeans.modules.vmd.properties.PrimitivePropertySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public final class PropertiesSupport {
    
    private PropertiesSupport() {
    }
    
    private static Comparator<DesignPropertyDescriptor> compareByDisplayName = new Comparator<DesignPropertyDescriptor>() {
        public int compare(DesignPropertyDescriptor descriptor1, DesignPropertyDescriptor descriptor2) {
            return descriptor1.getPropertyDisplayName().compareTo(descriptor2.getPropertyDisplayName());
        }
    };
    
    public static Sheet createSheet(final DesignComponent component) {
        final Sheet sheet = new Sheet();
        
        component.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                List<DesignPropertyDescriptor> designerPropertyDescriptors;
                List<String> categories;
                designerPropertyDescriptors = new ArrayList<DesignPropertyDescriptor>();
                categories = new ArrayList<String>();
                for (PropertiesPresenter propertiesPresenter : component.getPresenters(PropertiesPresenter.class)) {
                    designerPropertyDescriptors.addAll(propertiesPresenter.getDesignPropertyDescriptors());
                    categories.addAll(propertiesPresenter.getPropertiesCategories());
                }
                if (designerPropertyDescriptors != null)
                    Collections.sort(designerPropertyDescriptors, compareByDisplayName);
                createCategoriesSet(sheet, categories);
                for (DesignPropertyDescriptor designerPropertyDescriptor : designerPropertyDescriptors) {
                    Node.Property property;
                    DesignPropertyEditor propertyEditor = designerPropertyDescriptor.getPropertyEditor();
                    
                    if (propertyEditor instanceof GroupPropertyEditor && designerPropertyDescriptor.getPropertyNames().size() == 0)
                        throw new IllegalStateException("To use AdvancedPropertyEditorSupport you need to specific at least one propertyName"); //NOI18N
                    
                    if (propertyEditor instanceof GroupPropertyEditor)
                        property = new AdvancedPropertySupport(designerPropertyDescriptor, designerPropertyDescriptor.getPropertyEditorType());
                    else if (designerPropertyDescriptor.getPropertyNames().size() <= 1)
                        property = new PrimitivePropertySupport(designerPropertyDescriptor, designerPropertyDescriptor.getPropertyEditorType());
                    else {
                        throw new IllegalArgumentException();
                    }
                    if (propertyEditor != null &&  propertyEditor.canEditAsText() != null)
                        property.setValue("canEditAsText", propertyEditor.canEditAsText()); //NOI18N
                    property.setValue("changeImmediate", false); // NOI18
                    sheet.get(designerPropertyDescriptor.getPropertyCategory()).put(property);
                }
            }
        });
        
        return sheet;
    }
    
    //multi selection not supported
    public synchronized static void showPropertyEditorForCurrentComponent (DesignComponent component, String propertyName) {
        boolean propertyEditorExists = false;
        if (component.getDocument().getTransactionManager().isWriteAccess())
            Debug.warning("Calling PropertiesSupport.showPropertyEditorForCurrentComponent form write transaction may generate problems"); //NOI18N
        Sheet sheet = createSheet(component);
        for (PropertySet propertySet : sheet.toArray()) {
            for (Property property : propertySet.getProperties()) {
                if(propertyName.equals(property.getName())) {
                    PropertyPanel propertyPanel = new PropertyPanel(property, PropertyPanel.PREF_CUSTOM_EDITOR);
                    propertyEditorExists = true;
                    propertyPanel.setChangeImmediate(false);
                    DialogDescriptor dd = new DialogDescriptor(propertyPanel, property.getDisplayName(), true, null); // NOI18N
                    Object helpID = property.getValue(ExPropertyEditor.PROPERTY_HELP_ID);
                    if (helpID != null) {
                        assert helpID instanceof String;
                        HelpCtx helpCtx = new HelpCtx((String)helpID);
                        dd.setHelpCtx(helpCtx);
                    }
                    
                    Object res = DialogDisplayer.getDefault().notify(dd);
                    
                    if (res == DialogDescriptor.OK_OPTION) {
                        ((DesignPropertyEditor) property.getPropertyEditor()).customEditorOKButtonPressed();
                        try {
                            property.setValue(property.getPropertyEditor().getValue());
                        } catch (IllegalAccessException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InvocationTargetException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    return;
                }
            }
        }
        if (!propertyEditorExists) {
            throw new IllegalArgumentException("PropertyEditor for " + propertyName +" not fond in the component " + component); //NOI18N 
        }
    }
    
    private synchronized static Sheet.Set createPropertiesSet(String categoryName) {
        Sheet.Set setSheet = new Sheet.Set();
        setSheet.setName(categoryName);
        setSheet.setDisplayName(categoryName);
        
        return setSheet;
    }
    
    private synchronized static void createCategoriesSet(Sheet sheet, List<String> categories) {
        for (String propertyCategory : categories) {
            sheet.put(createPropertiesSet(propertyCategory));
        }
    }

}
