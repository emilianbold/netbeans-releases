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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IExpansionVariable
{
	/**
	 * Name of the expansion variable. This is what is used inline in template files.
	*/
	public String getName();

	/**
	 * Name of the expansion variable. This is what is used inline in template files.
	*/
	public void setName( String value );

    /**
     * Retrieves the operator currently set on this variable.
     * @return The operator (enumerated integer EOK).
     */
    public int getOperator();
    
    /**
     * Sets the operator for this variable.
     * @param operator The operator (enumerated integer EOK).
     */
    public void setOperator(int operator);
    
	/**
	 * A query that results in an xmi id after expansion.
	*/
	public String getIDLookup();

	/**
	 * A query that results in an xmi id after expansion.
	*/
	public void setIDLookup( String value );

	/**
	 * The name of an existing expansion variable that will be used after the initial query of this var is done.
	*/
	public String getExpansionName();

	/**
	 * The name of an existing expansion variable that will be used after the initial query of this var is done.
	*/
	public void setExpansionName( String value );

	/**
	 * A query generally resulting in various elements that can then be applied to other expansion variables.
	*/
	public String getQuery();

	/**
	 * A query generally resulting in various elements that can then be applied to other expansion variables.
	*/
	public void setQuery( String value );

	/**
	 * Name of the get property to be invoked on the result node of this expansion.
	*/
	public String getMethodGet();

	/**
	 * Name of the get property to be invoked on the result node of this expansion.
	*/
	public void setMethodGet( String value );

	/**
	 * Determines whether or not the result of this variable expansion is an xml attribute.
	*/
	public boolean getIsAttributeResult();

	/**
	 * The context object passed through all expansions
	*/
	public IVariableExpander getExecutionContext();

	/**
	 * The context object passed through all expansions
	*/
	public void setExecutionContext( IVariableExpander value );

	/**
	 * The file location of an XSL transform to apply to the expanded results of this variable.
	*/
	public String getXSLFilter();

	/**
	 * The file location of an XSL transform to apply to the expanded results of this variable.
	*/
	public void setXSLFilter( String value );

	/**
	 * The actual variable used during post expansion.
	*/
	public IExpansionVariable getExpansionVariable();

	/**
	 * The actual variable used during post expansion.
	*/
	public void setExpansionVariable( IExpansionVariable value );

	/**
	 * The actual xml representation of this variable.
	*/
	public Node getNode();

	/**
	 * The actual xml representation of this variable.
	*/
	public void setNode( Node value );

	/**
	 * Expands this variable, using context to query against.
	*/
	public String expand( Node context );
    
    /**
     * Expands this variable, using context to query against.
     */
    public String expand( IElement context );

	/**
	 * The collection of nodes that were retrieved during the expansion process.
	*/
	public ETList<Node> getResultNodes();

	/**
	 * The collection of nodes that were retrieved during the expansion process.
	*/
	public void setResultNodes( ETList<Node> value );

	/**
	 * The kind of variable this is.
	*/
	public int getKind();

	/**
	 * The kind of variable this is.
	*/
	public void setKind( /* VariableKind */ int value );

	/**
	 * Use in conjunction with the IDLookup property. Filters the result on nodes that have a node name of the value found in the 'type' attribute.
	*/
	public String getTypeFilter();

	/**
	 * Use in conjunction with the IDLookup property. Filters the result on nodes that have a node name of the value found in the 'type' attribute.
	*/
	public void setTypeFilter( String value );

	/**
	 * The text result of this variable's expansion process.
	*/
	public String getResults();

	/**
	 * The text result of this variable's expansion process.
	*/
	public void setResults( String value );

	/**
	 * A string in this format: <actual value>=<translated value>, ... Used for easy translation from meta values to user-defined values.
	*/
	public String getValueFilter();

	/**
	 * A string in this format: <actual value>=<translated value>, ... Used for easy translation from meta values to user-defined values.
	*/
	public void setValueFilter( String value );

	/**
	 * A string in this format: <string to replace>=<replace string>. Used to post-process an expansion result.
	*/
	public String getReplaceFilter();

	/**
	 * A string in this format: <string to replace>=<replace string>. Used to post-process an expansion result.
	*/
	public void setReplaceFilter( String value );

	/**
	 * Name of the expansion variable this expansion variable is overriding. Useful when reusing existing variables.
	*/
	public String getOverrideName();

	/**
	 * Name of the expansion variable this expansion variable is overriding. Useful when reusing existing variables.
	*/
	public void setOverrideName( String value );

	/**
	 * The value that will result in a 'true' value in the expansion. Used when the Kind property is of type VK_BOOLEAN.
	*/
	public String getTrueValue();

	/**
	 * The value that will result in a 'true' value in the expansion. Used when the Kind property is of type VK_BOOLEAN.
	*/
	public void setTrueValue( String value );

	/**
	 * Indicates whether or not this boolean expansion variable expanded into a true result or not.
	*/
	public boolean getIsTrue();

	/**
	 * Indicates whether or not this boolean expansion variable expanded into a true result or not.
	*/
	public void setIsTrue( boolean value );
}
