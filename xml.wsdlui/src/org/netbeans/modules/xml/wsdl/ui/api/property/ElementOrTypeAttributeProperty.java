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

package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.beans.PropertyEditor;

import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.ErrorManager;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author skini
 *
 */
public class ElementOrTypeAttributeProperty
extends PropertySupport.Reflection {
    public static final String ELEMENT_ATTRIBUTE_NAME = "elementAttributeName";
    public static final String TYPE_ATTRIBUTE_NAME = "typeAttributeName";
    
    private ElementOrTypeProvider mProv;
    
    public ElementOrTypeAttributeProperty(ElementOrTypeProvider prov) throws NoSuchMethodException {
        super(prov, ElementOrType.class, "elementOrType");//NOI18N
        mProv = prov;
    }
    
    
    /** Returns our customized Editor
     *
     * @return a <code>PropertyEditor</code> value
     */
    @Override
    public PropertyEditor getPropertyEditor() {
        
        ElementOrTypePropertyEditor editor = new ElementOrTypePropertyEditor(mProv);
        try {
            editor.setValue(this.getValue());
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return editor;
        
    }
    
    @Override
    public boolean canWrite() {
        return XAMUtils.isWritable(mProv.getModel());
    }
    
}