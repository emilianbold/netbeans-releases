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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

public interface IPropertyDefinitionFilter
{
	/**
	 * Filter the element based on properties for the IElement underneath it, assumes the language on the model element
	*/
	public long filterPropertyElement( IPropertyElement pEle );

	/**
	 * Filter the element based on properties for the IElement underneath it and the language passed in
	*/
	public long filterPropertyElementBasedOnLanguage( IPropertyElement pEle, String sLang );

	/**
	 * Filter the element based on properties for the passed in model element
	*/
	public long filterPropertyElementBasedOnModelElement( IPropertyElement pEle, IElement pElement );

	/**
	 * The xml file that contains the filter information
	*/
	public String getFilterFile();

	/**
	 * The xml file that contains the filter information
	*/
	public void setFilterFile( String value );

	/**
	 * Filter the element based on the filter with the passed in name
	*/
	public long filterPropertyElementBasedOnFilterName( IPropertyElement pEle, String sName );
}
