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
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.extension.bpel;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.ui.api.property.ComboBoxPropertyEditor;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.PropertySupport;

public class PropertyNameProperty extends PropertySupport.Reflection {

    private static QName propertyQName = BPELQName.PROPERTY.getQName();
    
    ExtensibilityElementPropertyAdapter adapter;
    
    public PropertyNameProperty(ExtensibilityElementPropertyAdapter instance, Class valueType, String getter, String setter) throws NoSuchMethodException {
        super(instance, valueType, getter, setter);
        adapter = instance;
    }
    
    
    @Override
    public PropertyEditor getPropertyEditor() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("");
        WSDLModel model = adapter.getExtensibilityElement().getModel();
        addToPropertyList(list, model);
        
        //Get from imported wsdls too.
        Collection<WSDLModel> importedModels = Utility.getImportedDocuments(model);
        for (WSDLModel imp : importedModels ) {
            addToPropertyList(list, imp);
        }
        
        return new ComboBoxPropertyEditor(list.toArray(new String[list.size()]));
    }
    
    private void addToPropertyList(List<String> propertyList, WSDLModel model) {
        List<ExtensibilityElement> ees = model.getDefinitions().getExtensibilityElements();
        String prefix = Utility.getNamespacePrefix(model.getDefinitions().getTargetNamespace(), 
                adapter.getExtensibilityElement());
        for (ExtensibilityElement ee : ees) {
            if (propertyQName.equals(ee.getQName())) {
                propertyList.add(prefix + ":" + ee.getAttribute("name"));
            }
        }
    }
    
    @Override
    public boolean canWrite() {
        return XAMUtils.isWritable(adapter.getExtensibilityElement().getModel());
    }

}
