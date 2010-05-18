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
import java.util.ArrayList;
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.bpel.properties.Constants;

/**
 *
 * @author nk160297
 */
public class PatternEditor extends PropertyEditorSupport {

    /** Creates a new instance of ModelReferenceEditor */
    public PatternEditor() {
    }

    public String getAsText() {
        Pattern val = (Pattern)getValue();
        if (val == null || Pattern.NOT_SPECIFIED.equals(val)) {
            return Constants.NOT_ASSIGNED;
        } else if (Pattern.INVALID.equals(val)) {
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
                Pattern.NOT_SPECIFIED.equals(text)) {
            setValue(null);
        } else {
            Pattern newValue = Pattern.forString(text);
            setValue(newValue);
        }
    }
    
    public String[] getTags() {
        return new String[] {
            Pattern.NOT_SPECIFIED.toString(),
            Pattern.REQUEST.toString(),
            Pattern.RESPONSE.toString(),
            Pattern.REQUEST_RESPONSE.toString()};
    }
    
    public Object getValue() {
        Object obj = super.getValue();
        if (obj != null) {
            assert obj instanceof Pattern;
        }
        return obj;
    }
    
    public void setValue(Object newValue) {
        if (newValue != null) {
            assert newValue instanceof Pattern;
        }
        if (Pattern.NOT_SPECIFIED.equals(newValue)) {
            newValue = null;
        }
        super.setValue(newValue);
    }
}
