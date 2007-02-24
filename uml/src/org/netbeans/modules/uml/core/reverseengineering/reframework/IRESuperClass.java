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

package org.netbeans.modules.uml.core.reverseengineering.reframework;
import org.dom4j.Node;
public interface IRESuperClass
{
	/**
	 * Get / Set the XML DOM node that contains the super class information.
	*/
	public Node getDOMNode();

	/**
	 * Get / Set the XML DOM node that contains the super class information.
	*/
	public void setDOMNode( Node value );

	/**
	 * Retrieve the name of the extended class.
	*/
	public String getName();

	/**
	 * Retrieve the line number that contains the declaration
	*/
	public int getLine();

	/**
	 * Retrieve the column that that contains the declaration
	*/
	public int getColumn();

	/**
	 * Retrieve the stream position that contains the declaration
	*/
	public int getPosition();
}
