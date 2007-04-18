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

package org.netbeans.modules.compapp.javaee.sunresources.ui;

import java.beans.PropertyEditorSupport;

/**
 *
 * @author echou
 */
public class TxSupportPropEditor extends PropertyEditorSupport {
    
    public String curr_Sel;
    public String[] choices = {
        "", // NOI18N
        "XATransaction", // NOI18N
        "LocalTransaction", // NOI18N
        "NoTransaction" // NOI18N
    };
    
    /** Creates a new instance of TxSupportPropEditor */
    public TxSupportPropEditor() {
        curr_Sel = choices[0];
    }
    
    public String getAsText() {
        return curr_Sel;
    }
    
    public void setAsText(String string) throws IllegalArgumentException {
        curr_Sel = string;
    }
    
    public void setValue(Object val) {
        curr_Sel = (String) val;
    }
    
    public Object getValue() {
        return curr_Sel;
    }
    
    public String getJavaInitializationString() {
        return getAsText();
    }
    
    public String[] getTags() {
        return choices;
    }
    
}
