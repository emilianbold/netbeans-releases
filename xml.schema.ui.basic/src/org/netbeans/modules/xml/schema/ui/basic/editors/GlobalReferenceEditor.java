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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * GlobalReferenceEditor.java
 *
 * Created on December 22, 2005, 12:58 PM
 */

package org.netbeans.modules.xml.schema.ui.basic.editors;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class GlobalReferenceEditor<T extends ReferenceableSchemaComponent>
		extends PropertyEditorSupport
        implements ExPropertyEditor {
    
    private SchemaComponentSelectionPanel panel;
    private DialogDescriptor descriptor;
    private SchemaComponent component;
    private Class<T> referenceType;
    private String typeDisplayName;
    private String propertyDisplayName;
    
    
    /**
     * Creates a new instance of GlobalReferenceEditor
     */
    public GlobalReferenceEditor(SchemaComponent component, 
            String typeDisplayName,
            String propertyDisplayName,
            Class<T> referenceType) {
        this.typeDisplayName = typeDisplayName;
        this.propertyDisplayName = propertyDisplayName;
        this.referenceType = referenceType;
        this.component = component;
    }
    
    public String getAsText() {
        Object val = getValue();
        if (val == null){
            return null;
        }
        if (val instanceof NamedComponentReference && component.getModel() != null){
            Object obj =  ((NamedComponentReference)val).get();
			if (obj == null){
				return null;
			}
            if (obj instanceof Named){
                return ((Named)obj).getName();
            }
        }
        // TODO how to display invalid values?
        return val.toString();
    }
    
    @SuppressWarnings("unchecked")
    public Component getCustomEditor() {
        NamedComponentReference<T> currentGlobalReference = (NamedComponentReference<T>) getValue();
        Collection<SchemaComponent> exclude = null;
        if(referenceType.isInstance(component)) {
            exclude = new ArrayList<SchemaComponent>();
            exclude.add(component);
        } else {
            SchemaComponent parent = component.getParent();
            while (parent!=null) {
                if(referenceType.isInstance(parent)) {
                    exclude = new ArrayList<SchemaComponent>();
                    exclude.add(parent);
                    break;
                }
                parent = parent.getParent();
            }
        }
        // Determine if primitive simple types should be shown (e.g. boolean).
        boolean primitives = GlobalSimpleType.class.isAssignableFrom(referenceType);
        panel = new SchemaComponentSelectionPanel<T>(component.getModel(),
                referenceType, currentGlobalReference==null?null:
                    currentGlobalReference.get(), exclude, primitives);
        descriptor = new SchemaComponentSelDialogDesc(panel,
                NbBundle.getMessage(GlobalReferenceEditor.class,
                "LBL_Custom_Property_Editor_Title",
                new Object[] {typeDisplayName, propertyDisplayName}),
                true,
                new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        setValue(getCurrentSelection(panel.getCurrentSelection()));
                    } catch (IllegalArgumentException iae) {
                        ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                                iae.getMessage(), iae.getLocalizedMessage(), 
                                null, new java.util.Date());
                        throw iae;
                    }
                }
            }
        });
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        return dlg;
    }
    
    
    public boolean supportsCustomEditor() {
        return true;
    }

    private NamedComponentReference getCurrentSelection(SchemaComponent sc){
        return sc==null?null:
            sc.getModel().getFactory().createGlobalReference(
                referenceType.cast(sc),
                referenceType , component);
    }
    
    public void attachEnv(PropertyEnv env ) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this is not editable  
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }

}
