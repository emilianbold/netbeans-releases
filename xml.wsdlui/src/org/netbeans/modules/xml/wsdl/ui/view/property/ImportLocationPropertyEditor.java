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

package org.netbeans.modules.xml.wsdl.ui.view.property;

import java.awt.Component;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.view.ImportWSDLCustomizer;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * Property editor for import component location property.
 *
 * @author radval
 * @author Nathan Fiedler
 */
public class ImportLocationPropertyEditor extends PropertyEditorSupport
        implements ExPropertyEditor {
    private Import component;

    public ImportLocationPropertyEditor(Import component) {
        this.component = component;
    }

    public void attachEnv(PropertyEnv env) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }

    public Component getCustomEditor() {
        ImportWSDLCustomizer customizer = new ImportWSDLCustomizer(component);
        DialogDescriptor descriptor = UIUtilities.getCustomizerDialog(
                customizer, NbBundle.getMessage(ImportLocationPropertyEditor.class,
                "ImportLocationPropertyPanel_COMMON_MSG_HEADER"), true);
        descriptor.setValid(false);
        return DialogDisplayer.getDefault().createDialog(descriptor);
    }

    public boolean supportsCustomEditor() {
        return XAMUtils.isWritable(component.getModel());
    }
}
