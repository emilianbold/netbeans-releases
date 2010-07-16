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
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.properties.Constants;

/**
 *
 * @author nk160297
 */
public class TBooleanEditor extends PropertyEditorSupport {

    /** Creates a new instance of ModelReferenceEditor */
    public TBooleanEditor() {
    }

    public String getAsText() {
        TBoolean val = (TBoolean)getValue();
        if (val == null) {
            return Constants.NOT_ASSIGNED;
        } else if (val.equals(TBoolean.INVALID)) {
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
            TBoolean newValue = TBoolean.forString(text);
            setValue(newValue);
        }
    }
    
    public String[] getTags() {
        return new String[] { Constants.NOT_ASSIGNED, TBoolean.YES.toString(), TBoolean.NO.toString()};
    }
    
    public Object getValue() {
        Object obj = super.getValue();
        if (obj != null) {
            assert obj instanceof TBoolean;
        }
        return obj;
    }
    
    public void setValue(Object newValue) {
        if (newValue != null) {
            assert newValue instanceof TBoolean;
        }
        super.setValue(newValue);
    }
}
