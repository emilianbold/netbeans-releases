/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
