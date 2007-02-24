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

public interface ITemplateManager
{
	/**
	 * Expands the passed in template file, using contextElement as the initial element to query against.
	*/
	public String expandTemplate( String templateFile, IElement contextElement );

	/**
	 * Expands the passed in template file, using contextNode as the initial element to query against.
	*/
	public String expandTemplateWithNode( String templateFile, Node contextNode );

    /**
     * Gets the IVariableExpander this factory is
     * @return
     */
    public IVariableExpander getVariableExpander();

	/**
	 * The location of a configuration file that contains Expansion variable definitions.
	*/
	public String getConfigLocation();

	/**
	 * The location of a configuration file that contains Expansion variable definitions.
	*/
	public void setConfigLocation( String value );

	/**
	 * The VariableFactory this TemplateManager will use.
	*/
	public IVariableFactory getFactory();

	/**
	 * The VariableFactory this TemplateManager will use.
	*/
	public void setFactory( IVariableFactory value );

	/**
	 * The absolute path to the directory where expansions are taking place.
	*/
	public String getWorkingDirectory();

	/**
	 * The absolute path to the directory where expansions are taking place.
	*/
	public void setWorkingDirectory( String value );
    
    public String expandVariable(String varName, IElement contextElement);
    
    public String expandVariableWithNode(String varName, Node contextNode);
    
    public IVariableExpander createExecutionContext();
}
