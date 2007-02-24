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


package org.netbeans.modules.uml.ui.support.archivesupport;

import org.dom4j.Node;

public interface IProductArchiveAttribute
{
	/**
	 * The name of the attribute.
	*/
	public String getName();

	/**
	 * The value of the attribute.
	*/
	public String getValue();

	/**
	 * The value of the attribute.
	*/
	public void setValue( String value );

	/**
	 * Gets the IXMLDOMNode that matches this node.
	*/
	public Node getDOMNode();

	/**
	 * Gets the IXMLDOMNode that matches this node.
	*/
	public void setDOMNode( Node value );

	/**
	 * Gets an attribute value by name.
	*/
	public long getLongValue();

	/**
	 * Gets an attribute value by name.
	*/
	public String getStringValue();

	/**
	 * Gets an attribute value by name.
	*/
	public boolean getBoolValue();

	/**
	 * Gets an attribute value by name.
	*/
	public double getDoubleValue();

}
