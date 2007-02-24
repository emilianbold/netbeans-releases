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
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IParameterEventsSink
{
	/**
	 * Fired whenever the default expression for the parameter is about to change.
	*/
	public void onPreDefaultExpModified( IParameter feature, IExpression proposedValue, IResultCell cell );

	/**
	 * Fired whenever the default expression for the parameter has changed.
	*/
	public void onDefaultExpModified( IParameter feature, IResultCell cell );

	/**
	 * Fired whenever the default expression's body property for the parameter is about to change.
	*/
	public void onPreDefaultExpBodyModified( IParameter feature, String bodyValue, IResultCell cell );

	/**
	 * Fired whenever the default expression's body property for the parameter has changed.
	*/
	public void onDefaultExpBodyModified( IParameter feature, IResultCell cell );

	/**
	 * Fired whenever the default expression's language property for the parameter is about to change.
	*/
	public void onPreDefaultExpLanguageModified( IParameter feature, String language, IResultCell cell );

	/**
	 * Fired whenever the default expression's language property for the parameter has changed.
	*/
	public void onDefaultExpLanguageModified( IParameter feature, IResultCell cell );

	/**
	 * Fired whenever the direction value of the parameter is about to change.
	*/
	public void onPreDirectionModified( IParameter feature, /* ParameterDirectionKind */ int proposedValue, IResultCell cell );

	/**
	 * Fired whenever the direction value of the parameter has changed.
	*/
	public void onDirectionModified( IParameter feature, IResultCell cell );
}
