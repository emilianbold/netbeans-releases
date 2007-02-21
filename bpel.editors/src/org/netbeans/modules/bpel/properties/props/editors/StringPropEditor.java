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
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author nk160297
 */
public class StringPropEditor extends PropertyEditorSupport
        implements ExPropertyEditor, Reusable {
    
    private static StringPropertyCustomizer customizer = null;
    
    protected PropertyEnv myPropertyEnv = null;
    
    /**
     * Allows to use single instance of editor for differen properties
     */
    /** Creates a new instance of QNamePropEditor */
    public StringPropEditor() {
    }
    
    public String getAsText() {
        Object value = super.getValue();
        return value == null ? "" : String.valueOf(value);
    }
    
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        setValue(text);
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        customizer = PropertyUtils.propertyCustomizerPool.
                getObjectByClass(StringPropertyCustomizer.class);
        customizer.init(myPropertyEnv, this);
        return customizer;
    }
    
    public void attachEnv(PropertyEnv newPropertyEnv) {
        myPropertyEnv = newPropertyEnv;
    }
    
}
