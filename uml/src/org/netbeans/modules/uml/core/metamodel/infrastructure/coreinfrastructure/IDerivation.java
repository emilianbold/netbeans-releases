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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IDerivation extends IDependency
{
	/**
	 * property DerivedClassifier
	*/
	public IClassifier getDerivedClassifier();

	/**
	 * property DerivedClassifier
	*/
	public void setDerivedClassifier( IClassifier value );

	/**
	 * property Template
	*/
	public IClassifier getTemplate();

	/**
	 * property Template
	*/
	public void setTemplate( IClassifier classifier );

	/**
	 * method AddBinding
	*/
	public void addBinding( IUMLBinding pBind );

	/**
	 * method RemoveBinding
	*/
	public void removeBinding( IUMLBinding pBind );

	/**
	 * property Bindings
	*/
	public ETList<IUMLBinding> getBindings();

}
