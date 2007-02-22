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
 * ZeroOrOneEditor.java
 *
 * Created on December 22, 2005, 12:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.ui.basic.editors;

import java.beans.PropertyEditorSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Jeri Lockhart
 * True, False, or null (which defaults to False)
 *
 */
public class BooleanDefaultFalseEditor  extends PropertyEditorSupport{
		
	/**
     * Creates a new instance of BooleanDefaultFalseEditor
     */
	public BooleanDefaultFalseEditor() {
	}
	
	public String[] getTags() {
		return new String[] {NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_DefaultFalse"),
            NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_True"),
			NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_False")};
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (text.equals(NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_DefaultFalse"))){
			setValue(null);
		}
		else if (text.equals(NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_True"))){
			setValue(Boolean.valueOf(true));
		}
		else {
			setValue(Boolean.valueOf(false));
		}
		
	}

	public String getAsText() {
		Object val = getValue();
		if (val == null){
			return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_DefaultFalse");
		}
		if (val instanceof Boolean){
			return ((Boolean)val).booleanValue()==true?
				NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_True"):
				NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_False");
		}
		// TODO how to display invalid values?
		return NbBundle.getMessage(BooleanDefaultFalseEditor.class,"LBL_False");
	}

	

}
