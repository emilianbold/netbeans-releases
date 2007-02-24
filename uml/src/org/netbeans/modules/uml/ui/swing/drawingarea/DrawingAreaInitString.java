/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.modules.uml.ui.swing.drawingarea;

/**
 * @author josephg
 *
 * This class defines an initialization string.  It's made up of the TS part and
 * the part we use to provide initializers to the node/edge.
 */
public class DrawingAreaInitString {
	public void empty() {
		m_TSInitializationString = null;
		m_ObjectInitializersString = null;
	}
	
	public void set(String tsInitString, String objectInitString) {
		m_TSInitializationString = tsInitString;
		m_ObjectInitializersString = objectInitString;
	}
	
	public String getInitString() {
		return m_TSInitializationString + " " + m_ObjectInitializersString;
	}
	
	public String splitViewDescription(String xsDesc) {
		String descriptionString  = xsDesc.trim();
		StringBuffer initializationString = new StringBuffer();
		empty();
		
		String[] tokens = descriptionString.split(" ");
		
		int length = tokens.length;
		
		if(length > 0) {
			descriptionString = tokens[0];
			for(int index = 1;index < length;++index) {
				initializationString.append(tokens[index]);
				initializationString.append(' ');
			}
			set(descriptionString,initializationString.toString());
		}
		return descriptionString;
	}
	
	public String m_TSInitializationString = null;
	public String m_ObjectInitializersString = null;
}
