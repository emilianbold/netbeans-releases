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

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.ContentModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class GlobalReferenceEditor
		extends PropertyEditorSupport
        implements ExPropertyEditor {
    
    private DialogDescriptor descriptor;
    private AXIComponent component;
    private List<Class> filterTypes;
    private String referenceTypeDisplayName;
    private String typeDisplayName;
    private String propertyDisplayName;
	private AXIComponentSelectionPanel panel;
    
    
    /**
     * Creates a new instance of GlobalReferenceEditor
     */
    public GlobalReferenceEditor(AXIComponent component, 
            String typeDisplayName,
            String propertyDisplayName,
            String referenceTypeDisplayName,
            List<Class> filterTypes) {
        this.typeDisplayName = typeDisplayName;
        this.propertyDisplayName = propertyDisplayName;
        this.filterTypes = filterTypes;
        this.component = component;
        this.referenceTypeDisplayName = referenceTypeDisplayName;
    }
    
    public String getAsText() {
        Object val = getValue();
        if (val instanceof AXIType && component.getModel() != null)
			return ((AXIType)val).getName();
		else
			return null;
    }
    
    @SuppressWarnings("unchecked")
    public Component getCustomEditor() {
        Object currentGlobalReference = getValue();
        Collection<AXIComponent> exclude = null;
		for(Class filterType: filterTypes) {
			if(filterType.isInstance(component)) {
				exclude = new ArrayList<AXIComponent>();
				exclude.add(component);
			} else {
				AXIComponent parent = component.getParent();
				while (parent!=null) {
					if(filterType.isInstance(parent)) {
						exclude = new ArrayList<AXIComponent>();
						exclude.add(parent);
						break;
					}
					parent = parent.getParent();
				}
			}
		}
        panel = new AXIComponentSelectionPanel(component.getModel(),
                referenceTypeDisplayName, filterTypes, 
				currentGlobalReference, exclude);
        descriptor = new AXIComponentSelDialogDesc(panel,
                NbBundle.getMessage(GlobalReferenceEditor.class,
                "LBL_Custom_Property_Editor_Title",
                new Object[] {typeDisplayName, propertyDisplayName}),
                true,
                new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        setValue(panel.getCurrentSelection());
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
    
    public void attachEnv(PropertyEnv env ) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this is not editable  
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }
}
