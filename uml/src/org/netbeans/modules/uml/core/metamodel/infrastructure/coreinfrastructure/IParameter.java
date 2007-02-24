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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;

public interface IParameter extends INamedElement, ITypedElement
{
	/**
	 * Sets / Gets the direction flag on the parameter, indicating the semantics of how that data the parameter represents is entering the behavior.
	*/
	public int getDirection();

	/**
	 * Sets / Gets the direction flag on the parameter, indicating the semantics of how that data the parameter represents is entering the behavior.
	*/
	public void setDirection( /* ParameterDirectionKind */ int value );

	/**
	 * Sets / Gets the expression that holds the default initialization for the parameter.
	*/
	public IExpression getDefault();

	/**
	 * Sets / Gets the expression that holds the default initialization for the parameter.
	*/
	public void setDefault( IExpression exp );

	/**
	 * Sets / Gets the name of the Parameter.
	*/
	public String getName();

	/**
	 * Sets / Gets the name of the Parameter.
	*/
	public void setName( String value );

	/**
	 * Set the type via a name that will be resolved into a Classifier.
	*/
	public void setType2( String value );

	/**
	 * Retrieves the BehavioralFeature this parameter is a part of.
	*/
	public IBehavioralFeature getBehavioralFeature();

	/**
	 * Retrieves the Behavior this parameter is a part of.
	*/
	public IBehavior getBehavior();

	/**
	 * The name of the Classifier who specifies this Parameter's type.
	*/
	public String getTypeName();

	/**
	 * The name of the Classifier who specifies this Parameter's type.
	*/
	public void setTypeName( String value );

	/**
	 * The default parameter initializer. Easy access to the body property of the Expression.
	*/
	public String getDefault2();

	/**
	 * The default parameter initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault2( String value );

	/**
	 * The default parameter initializer. Easy access to the body property of the Expression.
	*/
	public String getDefault3();

	/**
	 * The default parameter initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault3( String lang, String body );

	/**
	 * Specifies extra semantics associated with the Parameter.
	*/
	public int getParameterKind();

	/**
	 * Specifies extra semantics associated with the Parameter.
	*/
	public void setParameterKind( /* ParameterSemanticsKind */ int value );
	
	public IVersionableElement performDuplication();
	
}
