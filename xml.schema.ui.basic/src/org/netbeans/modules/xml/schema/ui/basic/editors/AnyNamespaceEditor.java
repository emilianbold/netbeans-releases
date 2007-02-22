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
 * AnyNamespaceEditor.java
 *
 * Created on December 22, 2005, 12:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.ui.basic.editors;

import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 *
 */
public class AnyNamespaceEditor  extends PropertyEditorSupport
        implements ExPropertyEditor{
		
	/**
     * Creates a new instance of AnyNamespaceEditor
     */
	public AnyNamespaceEditor() {
	}
	
	public String[] getTags() {
		return new String[] {NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_ANY"),
            NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Other"),
            NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_TargetNamespace"),
			NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Local")};
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (text.equalsIgnoreCase(NbBundle.getMessage(BooleanDefaultFalseEditor.class,
                "LBL_ANY"))){
			setValue("##any");
		}
		else if (text.equalsIgnoreCase(NbBundle.getMessage(BooleanDefaultFalseEditor.class,
                "LBL_Other"))){
			setValue("##other");
		}
		else if (text.equalsIgnoreCase(NbBundle.getMessage(BooleanDefaultFalseEditor.class,
                "LBL_TargetNamespace"))){
			setValue("##targetNamespace");
		}
		else if (text.equalsIgnoreCase(NbBundle.getMessage(BooleanDefaultFalseEditor.class,
                "LBL_Local"))){
			setValue("##local");
		}
		else {
			setValue(text);
		}
		
	}

	public String getAsText() {
		Object obj = getValue();
		if (obj == null){
			return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_ANY");
		}
		if (obj instanceof String){
            String  val = (String)obj;
            if(val.equals("##any"))
                return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_ANY");
            if(val.equals("##other"))
                return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Other");
            if(val.equals("##targetNamespace"))
                return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_TargetNamespace");
            if(val.equals("##local"))
                return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_Local");
            return val;
		}
		// TODO how to display invalid values?
		return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_ANY");
	}

    /**
     *
     *  implement ExPropertyEditor
     *
     */
    public void attachEnv(PropertyEnv env ) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this an editable combo tagged editor  
        desc.setValue("canEditAsText", Boolean.TRUE); // NOI18N
    }
}
