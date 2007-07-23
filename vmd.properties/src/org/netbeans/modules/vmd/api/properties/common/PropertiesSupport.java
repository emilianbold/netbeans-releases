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
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.properties.PropertiesNodesManager;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Karol Harezlak
 */
public final class PropertiesSupport {
    /**
     * DataEditorView Tag. This tag hides Netbeans Property Window
     */ 
    public static final String DO_NOT_OPEN_PROPERTIES_WINDOW_TAG = "DO_NOT_OPEN_PROPERTIES_WINDOW"; //NOI18N
    
    private PropertiesSupport() {
    }
    
    public static Sheet getSheet(DataEditorView view, DesignComponent component) {
        return PropertiesNodesManager.getInstance(view).getSheet(component);
    }
    
    public static void addInstanceContent(DataEditorView view, InstanceContent ic) {
        PropertiesNodesManager.getInstance(view).add(ic);
    }
    
    //multi selection not supported
    //DO NOT invoke this method inside read transaction!
    public synchronized static void showCustomPropertyEditor(DesignComponent component, String propertyName) {
        boolean propertyEditorExists = false;
        if (component.getDocument().getTransactionManager().isWriteAccess())
            Debug.warning("Calling PropertiesSupport.showPropertyEditorForCurrentComponent form write transaction may generate problems"); //NOI18N
        DataEditorView view = ActiveViewSupport.getDefault().getActiveView();
        assert (view != null);
        Sheet sheet = PropertiesNodesManager.getInstance(view).createSheet(component);
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
    
}
