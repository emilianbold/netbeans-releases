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

public interface IPropertyElementManager
{
	/**
	 * Get the current presentation element
	*/
	public Object getPresentationElement();

	/**
	 * Get the current presentation element
	*/
	public void setPresentationElement( Object value );

	/**
	 * Get the current model element
	*/
	public Object getModelElement();

	/**
	 * Get the current model element
	*/
	public void setModelElement( Object value );

	/**
	 * Based on the passed in definition, build the corresponding element
	*/
	public IPropertyElement buildTopPropertyElement( IPropertyDefinition propDef);

	/**
	 * Get the definition factory
	*/
	public IPropertyDefinitionFactory getPDFactory();

	/**
	 * Get the definition factory
	*/
	public void setPDFactory( IPropertyDefinitionFactory value );

	/**
	 * Determines whether or not sub elements should be created
	*/
	public boolean getCreateSubs();

	/**
	 * Determines whether or not sub elements should be created
	*/
	public void setCreateSubs( boolean value );

	/**
	 * Performs an invoke on the passed in IDispatch to set its proper information based on the values in the element
	*/
	public void setData( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle );

	/**
	 * Performs an invoke on the passed in IDispatch to create the proper information based on the element
	*/
	public Object createData( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle );

	/**
	 * Performs an invoke on the model element stored on the pDeleteEle to delete the proper information
	*/
	public void deleteData( IPropertyElement pDeleteEle, IPropertyElement pEle );

	/**
	 * Take the information from the IDispatch and the definition and set the information on a new element
	*/
	public IPropertyElement buildElement( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle);

	/**
	 * Pass through method that determines based on the IDispatch and definition whether to create, set, or delete data
	*/
	public long processData( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle );

	/**
	 * Rebuild and refresh the data stored on the element and its necessary subelements
	*/
	public long reloadElement( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle );

	/**
	 * Rebuild and refresh the data stored on the element, but not its subelements
	*/
	public long reloadElementWithDummy( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle );

	/**
	 * Gets the xml file that defines the property elements
	*/
	public String getElementFile();

	/**
	 * Gets the xml file that defines the property elements
	*/
	public void setElementFile( String value );

	/**
	 * Based on the already set element file, build all elements for the passed in definition that are in the file
	*/
	public IPropertyElement[] buildElementsUsingXMLFile( IPropertyDefinition pDef );

	/**
	 * Based on its related definition, the value stored in the element may need to be tweeked for display purposes
	*/
	public void interpretElementValue( IPropertyElement pEle );

	/**
	 * Performs an invoke on the passed in IDispatch to insert the proper information based on the element
	*/
	public void insertData( Object pDisp, IPropertyDefinition pDef, IPropertyElement pEle );

	/**
	 * Builds and returns an empty element structure based on the passed in definition
	*/
	public IPropertyElementXML buildEmptyElementXML( IPropertyDefinition pDef);

	/**
	 * Saves property elements to an xml file
	*/
	public void saveElementsToXMLFile( String file, String dtdFile, IPropertyElement[] pEles );

	/**
	 * Translates the element's data from an enumerated value into a string.
	*/
	public void processEnumeration( IPropertyElement pEle );
	
	public Object interpretGetObjectDefinition( IPropertyElement pEle );

}
