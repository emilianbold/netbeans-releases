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
 * Created on Jun 30, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.property;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.SimpleType;
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
public class XSDEnumeratedAttributeProperty extends PropertySupport.Reflection {
    
    private SimpleType mType;
    private PropertyAdapter mPropAdapter;
    private boolean optional;
    private String valueNotSetMessage = NbBundle.getMessage(XSDEnumeratedAttributeProperty.class, "LBL_ValueNotSet");
    
    public XSDEnumeratedAttributeProperty(SimpleType type,
                                            PropertyAdapter instance,
                                            Class valueType, 
                                            String getter, 
                                            String setter, boolean isOptional) throws NoSuchMethodException {
        super(instance, valueType, getter, setter);
        this.mType = type;
        mPropAdapter = instance;
        optional = isOptional;
    }
    
    @Override
    public PropertyEditor getPropertyEditor() {
        String[] enuValues = getAllEnumeratedValues();
        return new ComboBoxPropertyEditor(enuValues);
    }
    
    private String[] getAllEnumeratedValues() {
        ArrayList<String> enuValueList = new ArrayList<String>();
        if (optional) {
            enuValueList.add(valueNotSetMessage);
        }
        Collection<Enumeration> coll = mType.getDefinition().getChildren(Enumeration.class);
        if(coll != null) {
            for(Enumeration enuFacet : coll) {
                enuValueList.add(enuFacet.getValue());
            }
        }
        return enuValueList.toArray(new String[] {});
    }
    
    @Override
    public boolean canWrite() {
        return mPropAdapter.isWritable();
    }
    
}



