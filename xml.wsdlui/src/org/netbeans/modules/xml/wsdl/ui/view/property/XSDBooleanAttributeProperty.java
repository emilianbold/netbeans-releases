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
 * Created on Sep 19, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.property;

import java.beans.PropertyEditor;

import org.netbeans.modules.xml.wsdl.ui.api.property.ComboBoxPropertyEditor;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyAdapter;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XSDBooleanAttributeProperty extends PropertySupport.Reflection  {
    
    private PropertyAdapter mPropAdapter;
    private String valueNotSetMessage = NbBundle.getMessage(XSDBooleanAttributeProperty.class, "LBL_ValueNotSet");
    private boolean optional;
    
    public XSDBooleanAttributeProperty(PropertyAdapter instance,
            Class valueType, 
            String getter, 
            String setter, boolean isOptional) throws NoSuchMethodException {
        super(instance, valueType, getter, setter);
        mPropAdapter = instance;
        optional = isOptional;
    }
    
    @Override
    public PropertyEditor getPropertyEditor() {
        String[] enuValues = getBooleanValues();
        return new ComboBoxPropertyEditor(enuValues);
    }
    
    private String[] getBooleanValues() {
        if (optional) {
            return new String[] {valueNotSetMessage, "true", "false"};
        }
        return new String[] {"true", "false"};//NOI18N
    }
    
    @Override
    public boolean canWrite() {
        return mPropAdapter.isWritable();
    }
    
    
    
}




