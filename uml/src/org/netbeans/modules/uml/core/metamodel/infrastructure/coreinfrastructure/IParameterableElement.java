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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;

public interface IParameterableElement extends INamedElement
{
	/**
	 * property Default
	*/
	public IParameterableElement getDefaultElement();

	/**
	 * property Default
	*/
	public void setDefaultElement( IParameterableElement element );

	/**
	 * property Default
	*/
	public void setDefaultElement2( String newVal );

	/**
	 * property Template
	*/
	public IClassifier getTemplate();

	/**
	 * property Template
	*/
	public void setTemplate( IClassifier value );

	/**
	 * Name of the meta type that must be used when instantiating the template.
	*/
	public String getTypeConstraint();

	/**
	 * Name of the meta type that must be used when instantiating the template.
	*/
	public void setTypeConstraint( String value );

}
