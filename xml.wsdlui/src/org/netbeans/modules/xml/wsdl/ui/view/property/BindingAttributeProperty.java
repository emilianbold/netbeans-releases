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
 * Created on May 20, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.property;

import java.beans.PropertyEditor;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.api.property.ComboBoxPropertyEditor;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyUtil;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.PropertySupport;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BindingAttributeProperty extends PropertySupport.Reflection {

    private WSDLModel mDocument;

    public BindingAttributeProperty(PropertyAdapter instance, 
            Class valueType, 
            String getter, 
            String setter) throws NoSuchMethodException {
        super(instance, valueType, getter, setter);
        this.mDocument = instance.getDelegate().getModel();
    }

    public BindingAttributeProperty(PropertyAdapter instance, 
            Class valueType, 
            String attributeName) throws NoSuchMethodException {
        super(instance, valueType, attributeName);
        this.mDocument = instance.getDelegate().getModel();
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        String[] bindings = PropertyUtil.getAllBindings(this.mDocument);
        return new ComboBoxPropertyEditor(bindings);
    }

    @Override
    public boolean canWrite() {
        return XAMUtils.isWritable(mDocument);
    }




}

