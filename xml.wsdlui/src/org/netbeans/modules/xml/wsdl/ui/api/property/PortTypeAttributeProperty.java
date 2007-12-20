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
 * PortTypeAttributeProperty.java
 *
 * Created on April 17, 2006, 12:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.beans.PropertyEditor;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author radval
 */
public class PortTypeAttributeProperty extends PropertySupport.Reflection {
	
	private WSDLModel mDocument;
	private ExtensibilityElementPropertyAdapter adapter;
	
	public PortTypeAttributeProperty(ExtensibilityElementPropertyAdapter instance, 
                                        Class valueType, 
                                        String getter, 
                                        String setter) throws NoSuchMethodException {
		super(instance, valueType, getter, setter);
		this.mDocument = instance.getExtensibilityElement().getModel();
        adapter = instance;
	}
	
	@Override
    public PropertyEditor getPropertyEditor() {
		String[] bindings = PropertyUtil.getAllPortTypes(this.mDocument, adapter.isOptional());
		return new ComboBoxPropertyEditor(bindings);
	}

    @Override
    public boolean canWrite() {
        return XAMUtils.isWritable(mDocument);
    }
    
    
}

