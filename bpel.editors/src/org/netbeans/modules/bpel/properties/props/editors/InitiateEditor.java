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
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.properties.Constants;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author nk160297
 */
public class InitiateEditor extends PropertyEditorSupport implements ExPropertyEditor {
    
    private PropertyEnv myPropertyEnv;
    
    /** Creates a new instance of ModelReferenceEditor */
    public InitiateEditor() {
    }
    
    public String getAsText() {
        Initiate val = (Initiate)getValue();
        if (val == null) {
            return Constants.NOT_ASSIGNED;
        } else if (val.equals(Initiate.INVALID)) {
            return Constants.INVALID;
        } else {
            return val.toString();
        }
    }
    
    public Component getCustomEditor() {
        return null;
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.length() == 0 ||
                Constants.NOT_ASSIGNED.equals(text)) {
            setValue(null);
        } else {
            Initiate newValue = Initiate.forString(text);
            setValue(newValue);
        }
    }
    
    public String[] getTags() {
        return new String[] {
            Initiate.NO.toString(),
            Initiate.YES.toString(),
            Initiate.JOIN.toString()};
    }
    
    public Object getValue() {
        Object obj = super.getValue();
        if (obj != null) {
            assert obj instanceof Initiate;
        }
        return obj;
    }
    
    public void setValue(Object newValue) {
        if (newValue != null) {
            assert newValue instanceof Initiate;
        }
        super.setValue(newValue);
    }
    
    public void attachEnv(PropertyEnv propertyEnv) {
        myPropertyEnv = propertyEnv;
//        Object[] beansArr = propertyEnv.getBeans();
//        if (beansArr != null && beansArr.length != 0 && beansArr[0] != null) {
//            // a workaround to overcame issue with the ReusablePropertyEnv
//            myPropertyEnv = propertyEnv;
//        }
    }
}
