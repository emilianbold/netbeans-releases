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
 * This interface defines a simple parameter reader that returns a
 * parameter requested with a given key. Applications that implement
 * this interface should parse their command line arguments;
 */
public interface ADParameterReader
{
	/**
	 * This method returns the requested parameter.
	 * @param key the key to which the requested parameter is mapped.
	 * @return the requested parameter, or <code>null</code> if it
	 * was not found.
	 */
	public String getParameter(String key);
}
