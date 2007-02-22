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
 * Created on Feb 9, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.xpath.impl;

import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.TreeCompiler;

/**
 * @author radval
 *
 * Extended TreeCompiler for handling of Number values.
 * We want to distinguish between Long and Double
 * value of the Constant.
 */
public class XPathTreeCompiler extends TreeCompiler {
	
	/**
	 * overriden this method to create
	 * appropriate Long or Double as the value stored in
	 * Constant.
	 */
	public Object number(String value) {
		//distinguish between Long and Double 
		try {
			int intVal = Integer.parseInt(value);
			return new Constant(new Long(value));
		} catch(NumberFormatException ex) {
			//Do Nothing
		}
		
        return new Constant(new Double(value));
    }
}
