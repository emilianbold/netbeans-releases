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
