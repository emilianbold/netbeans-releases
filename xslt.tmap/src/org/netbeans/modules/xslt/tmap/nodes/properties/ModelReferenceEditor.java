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
package org.netbeans.modules.xslt.tmap.nodes.properties;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author Vitaly Bychkov
 * @author nk160297
 */
public class ModelReferenceEditor extends PropertyEditorSupport
        implements ExPropertyEditor {
    
    private Reference myRef;
    private static StringPropertyCustomizer customizer = null;
    private PropertyEnv myPropertyEnv = null;
    
    /** Creates a new instance of ModelReferenceEditor */
    public ModelReferenceEditor() {
    }
    
    public String getAsText() {
        return getAsText(myRef);
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
    
    public Object getValue() {
        if (myRef != null){
            try {
                //check if reference still pointing to valid element
                myRef.get();
                return myRef;
            } catch (IllegalStateException ex){
                return null;
            }
        }
        return null;
    }
    
    public void setValue(Object newValue) {
        if (newValue != null) {
            assert newValue instanceof Reference;
        }
        myRef = (Reference)newValue;
        firePropertyChange();
    }
    
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        // DO NOTHING HERE!
    }
    
    public static String getAsText(Reference ref) {
        String result = ResolverUtility.getNameByRef(ref);
        return result == null ? "" : result;
    }
    
    public void attachEnv(PropertyEnv newPropertyEnv) {
        myPropertyEnv = newPropertyEnv;
    }
    
}
