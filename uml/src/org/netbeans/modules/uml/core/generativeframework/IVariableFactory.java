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


package org.netbeans.modules.uml.core.generativeframework;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
public interface IVariableFactory
{
	/**
	 * Creates a variable using the passed in node as the actual representation.
	*/
	public IExpansionVariable createVariable( Node var );

	/**
	 * The context object passed through all expansions.
	*/
	public IVariableExpander getExecutionContext();

	/**
	 * The context object passed through all expansions.
	*/
	public void setExecutionContext( IVariableExpander value );

	/**
	 * The location of a configuration file that contains Expansion variable definitions
	*/
	public String getConfigFile();

	/**
	 * The location of a configuration file that contains Expansion variable definitions
	*/
	public void setConfigFile( String value );

	/**
	 * Creates an expansion variable with the text found in a template file.
	*/
	public IExpansionVariable createVariableWithText( String varText );

	/**
	 * Removes an execution context from the internal stack of contexts.
	*/
	public IVariableExpander getPopContext();

	/**
	 * The collection of variables used to provide new or override existing expansion variables.
	*/
	public ETList<IExpansionVariable> getOverrideVariables();

	/**
	 * The collection of variables used to provide new or override existing expansion variables.
	*/
	public void setOverrideVariables( ETList<IExpansionVariable> value );

	/**
	 * Adds a new or overriding expansion variable.
	*/
	public void addOverride( IExpansionVariable var );

	/**
	 * Removes an expansion variable from the OverrideVariables collection.
	*/
	public void removeOverride( IExpansionVariable var );

}
