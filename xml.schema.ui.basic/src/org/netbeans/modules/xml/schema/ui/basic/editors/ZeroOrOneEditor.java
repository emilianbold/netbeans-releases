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
import org.netbeans.modules.xml.schema.model.Occur;
import org.openide.util.NbBundle;

/**
 *
 * @author Jeri Lockhart
 */
public class ZeroOrOneEditor  extends PropertyEditorSupport{
	
	public static final String EMPTY_STRING = "";	// NOI18N
	
	/** Creates a new instance of ZeroOrOneEditor */
	public ZeroOrOneEditor() {
	}
	
	public String[] getTags() {
		return new String[] {NbBundle.getMessage(ZeroOrOneEditor.class,"LBL_0"),
			NbBundle.getMessage(ZeroOrOneEditor.class,"LBL_1")};
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (text == null){
			setValue(null);
			return;
		} else if(NbBundle.getMessage(ZeroOrOneEditor.class,"LBL_0").equals(text)) {
            setValue(Occur.ZeroOne.ZERO);
        } else if (NbBundle.getMessage(ZeroOrOneEditor.class,"LBL_1").equals(text)) {
            setValue(Occur.ZeroOne.ONE);
        }
	}

	public String getAsText() {
		Object val = getValue();
		if (val == null){
			return null;
		}
		if (val instanceof Integer){
			return ((Integer)val).toString();
		}
		// TODO how to display invalid values?
		return val.toString();
	}
}
