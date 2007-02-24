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

public interface IVariableExpander
{
	/**
	 * The location of a configuration file that contains Expansion variable definitions
	*/
	public String getConfigFile();

	/**
	 * The location of a configuration file that contains Expansion variable definitions
	*/
	public void setConfigFile( String value );

	/**
	 * Retrieves the XML representation of an Expansion Variable
	*/
	public Node retrieveVarNode( String Name );

	/**
	 * A reference to the TemplateManager conduction expansions.
	*/
	public ITemplateManager getManager();

	/**
	 * A reference to the TemplateManager conduction expansions.
	*/
	public void setManager( ITemplateManager value );

    /**
     * Expands the text of the passed in expansion variable, making sure 
     * formatting with the previous text formatted is continued.
     *
     * @param prevText  The text found before the expansion variable
     * @param var       The variable to expand
     * @param context   The context to expand against
     * @return Formatted results, else "" if none found
     */
	public String expand(String prevText, IExpansionVariable var, Node context);

	/**
	 * Should be called whenever processing option expansion variables.
	*/
	public void beginGathering();

	/**
	 * Should match every call to BeginGathering.
	*/
	public boolean endGathering();

	/**
	 * Adds a result to this expander.
	*/
	public void addResult( IExpansionResult pResult );

	/**
	 * Removes a result to this expander.
	*/
	public void removeResult( IExpansionResult pResult );

	/**
	 * Appends the passed in results to the internal collection of results.
	*/
	public void appendResults( ETList<IExpansionResult> pResult );

	/**
	 * Retrieves the results of this expander
	*/
	public ETList<IExpansionResult> getExpansionResults();

	/**
	 * Retrieves the results of this expander
	*/
	public void setExpansionResults( ETList<IExpansionResult> value );
}