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

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IBehavior extends IClassifier
{
	/**
	 * property Context
	*/
	public IClassifier getContext();

	/**
	 * property Context
	*/
	public void setContext( IClassifier value );

	/**
	 * property Specification
	*/
	public IBehavioralFeature getSpecification();

	/**
	 * property Specification
	*/
	public void setSpecification( IBehavioralFeature value );

	/**
	 * property RepresentedFeature
	*/
	public IBehavioralFeature getRepresentedFeature();

	/**
	 * property RepresentedFeature
	*/
	public void setRepresentedFeature( IBehavioralFeature value );

	/**
	 * method AddParameter
	*/
	public void addParameter( IParameter parm );

	/**
	 * method RemoveParameter
	*/
	public void removeParameter( IParameter parm );

	/**
	 * property Parameters
	*/
	public ETList<IParameter> getParameters();

	/**
	 * Tells whether whether the behavior can be invoked while its still executing from a previous invocation.
	*/
	public boolean getIsReentrant();

	/**
	 * Tells whether whether the behavior can be invoked while its still executing from a previous invocation.
	*/
	public void setIsReentrant( boolean value );

}
