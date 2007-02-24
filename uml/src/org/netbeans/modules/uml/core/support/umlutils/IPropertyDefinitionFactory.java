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

package org.netbeans.modules.uml.core.support.umlutils;

import org.dom4j.Document;
import org.dom4j.Node;
import java.util.Vector;

public interface IPropertyDefinitionFactory
{
	/**
	 * Get or create a property definition matching the type or the dispatch kind passed in
	*/
	public IPropertyDefinition getPropertyDefinitionForElement( String eleName, Object pDisp);

	/**
	 * Initialize the factory
	*/
	public void initialize();

	/**
	 * Retrieve a particular method from an already built map
	*/
	public Object getFromFunctionMap( Object pDisp, String methName, String[] parmTypes );

	/**
	 * Retrieve documentation information from an already built map based on Object type
	*/
	public void getFromElementTypeDocMap( Object pDisp, String pVal );

	/**
	 * Gets the xml file that defines the property definitions
	*/
	public String getDefinitionFile();

	/**
	 * Gets the xml file that defines the property definitions
	*/
	public void setDefinitionFile( String value );

	/**
	 * Based on the already set definition file, build all definitions within the file
	*/
	public Vector<IPropertyDefinition> buildDefinitionsUsingFile();

	/**
	 * Gets the xml document that defines the property definitions
	*/
	public Document getXMLDocument();

	/**
	 * Gets the xml document that defines the property definitions
	*/
	public void setXMLDocument( Document value );

	/**
	 * Get or create a property definition matching the name passed in
	*/
	public IPropertyDefinition getPropertyDefinitionByName( String name);

	/**
	 * Build a definition given the xml node
	*/
	public IPropertyDefinition buildDefinitionFromNode( Node pNode);

	/**
	 * Build a definition given a string
	*/
	public IPropertyDefinition buildDefinitionFromString( String str);

}
