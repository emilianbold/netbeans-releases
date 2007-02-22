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

/*
 * FormPropertyEditor.java
 *
 */

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.beans.PropertyEditorSupport;
import org.netbeans.modules.xml.schema.model.Form;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 * "qualified", "unqualified", Empty (Default for Schema)
 *
 */
public class FormPropertyEditor  extends PropertyEditorSupport{

    /**
     * Creates a new instance of FormPropertyEditor
     */
    public FormPropertyEditor() {
    }

    public String[] getTags() {
        return new String[] {NbBundle.getMessage(FormPropertyEditor.class,getEmptyLabel()),
            NbBundle.getMessage(FormPropertyEditor.class,"LBL_Qualified"),
            NbBundle.getMessage(FormPropertyEditor.class,"LBL_Unqualified")};
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        if (text.equals(NbBundle.getMessage(FormPropertyEditor.class,getEmptyLabel()))){
            setValue(null);
        } else if (text.equals(NbBundle.getMessage(FormPropertyEditor.class,"LBL_Qualified"))){
            setValue(Form.QUALIFIED);
        } else if (text.equals(NbBundle.getMessage(FormPropertyEditor.class,"LBL_Unqualified"))){
            setValue(Form.UNQUALIFIED);
        }
    }
    
    public String getAsText() {
        Object val = getValue();
        if (val instanceof Form){
            if (Form.QUALIFIED.equals(val)) {
                return NbBundle.getMessage(FormPropertyEditor.class,"LBL_Qualified");
            } else if (Form.UNQUALIFIED.equals(val)) {
                return NbBundle.getMessage(FormPropertyEditor.class,"LBL_Unqualified");
            }
        }
        // TODO how to display invalid values?
        return NbBundle.getMessage(FormPropertyEditor.class,getEmptyLabel());
    }
    
    protected String getEmptyLabel() {
        return "LBL_Empty";
    }
    
    public static class SchemaFormPropertyEditor  extends FormPropertyEditor{
        /**
         * Creates a new instance of SchemaFormPropertyEditor
         */
        public SchemaFormPropertyEditor() {
        }
    
        protected String getEmptyLabel() {
            return "LBL_EmptyForSchema";
        }

    }
}
