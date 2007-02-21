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

package org.netbeans.modules.bpel.properties.props.editors;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * @author nk160297
 */
public class TypePropEditor extends PropertyEditorSupport
        implements ExPropertyEditor, Reusable {
    private PropertyEnv myPropertyEnv = null;
    private StereotypeFilter myFilter;
    
    public TypePropEditor() {
        myFilter = StereotypeFilter.ALL;
    }
    
    protected TypePropEditor(StereotypeFilter filter) {
        myFilter = filter;
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        TypePropertyCustomizer customizer = PropertyUtils.propertyCustomizerPool.
                getObjectByClass(TypePropertyCustomizer.class);
        customizer.init(myPropertyEnv, this);
        return customizer;
        // return new MyCustomEditor(myPropertyEnv, this);
    }
    
    public String getAsText() {
        Object value = getValue();
        if (value != null) {
            assert value instanceof TypeContainer; // has to be!!!
            return ((TypeContainer)value).getTypeName();
        }
        //
        return "";
    }
    
    public void attachEnv(PropertyEnv propertyEnv) {
        myPropertyEnv = propertyEnv;
    }
    
    public StereotypeFilter getSTypeFilter() {
        return myFilter;
    }
    
}
