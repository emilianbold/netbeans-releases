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
import org.netbeans.modules.soa.ui.SoaConstants;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;

/**
 *
 * @author Vitaly Bychkov
 */
public class ParamTypeEditor extends PropertyEditorSupport {

    /** Creates a new instance of ParamTypeEditor */
    public ParamTypeEditor() {
    }

    @Override
    public String getAsText() {
        ParamType val = (ParamType)getValue();
        if (val == null) {
            return SoaConstants.NOT_ASSIGNED;
        } else if (val.equals(ParamType.INVALID)) {
            return SoaConstants.INVALID; 
        } else {
            return val.toString();
        }
    }
    
    @Override
    public Component getCustomEditor() {
        return null;
    }
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.length() == 0 || 
                SoaConstants.NOT_ASSIGNED.equals(text)) {
            setValue(null);
        } else {
            ParamType newValue = ParamType.parseParamType(text);
            setValue(newValue);
        }
    }
    
    @Override
    public String[] getTags() {
        return new String[] {
//            SoaConstants.NOT_ASSIGNED, 
            ParamType.PART.toString(), 
            ParamType.LITERAL.toString()};
    }
    
    @Override
    public Object getValue() {
        Object obj = super.getValue();
        if (obj != null) {
            assert obj instanceof ParamType;
        }
        return obj;
    }
    
    @Override
    public void setValue(Object newValue) {
        if (newValue != null) {
            assert newValue instanceof ParamType;
        }
        super.setValue(newValue);
    }
}
