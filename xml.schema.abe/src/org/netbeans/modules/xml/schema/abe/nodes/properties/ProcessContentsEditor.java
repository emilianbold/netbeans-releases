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

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import org.netbeans.modules.xml.schema.model.Any.ProcessContents;
import java.beans.PropertyEditorSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 * "Lax", "Skip", "Strict", Empty
 *
 */
public class ProcessContentsEditor  extends PropertyEditorSupport{

    /**
     * Creates a new instance of ProcessContentsEditor
     */
    public ProcessContentsEditor() {
    }
    
    public String[] getTags() {
        return new String[] {NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_EmptyProcessContents"),
            NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Lax"),
            NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Skip"),
            NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Strict")};
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        if (text.equals(NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_EmptyProcessContents"))){
            setValue(null);
        } else if (text.equals(NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Lax"))){
            setValue(ProcessContents.LAX);
        } else if (text.equals(NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Skip"))){
            setValue(ProcessContents.SKIP);
        } else if (text.equals(NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Strict"))){
            setValue(ProcessContents.STRICT);
        }
    }
    
    public String getAsText() {
        Object val = getValue();
        if (val instanceof ProcessContents){
            if (ProcessContents.LAX.equals(val)) {
                return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Lax");
            } else if (ProcessContents.SKIP.equals(val)) {
                return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Skip");
            } else if (ProcessContents.STRICT.equals(val)) {
                return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Strict");
            }
        }
        // TODO how to display invalid values?
        return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_EmptyProcessContents");
    }
    
}
