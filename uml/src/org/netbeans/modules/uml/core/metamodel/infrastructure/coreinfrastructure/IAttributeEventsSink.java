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
public interface IAttributeEventsSink
{
	/**
	 * Fired whenever the default value of an IAttribute is about to be modified.
	*/
	public void onDefaultPreModified( IAttribute attr, IExpression proposedValue, IResultCell cell );

	/**
	 * Fired whenever the default value of an IAttribute was modified.
	*/
	public void onDefaultModified( IAttribute attr, IResultCell cell );

	/**
	 * Fired whenever the default expression's body property for the attribute is about to change.
	*/
	public void onPreDefaultBodyModified( IAttribute feature, String bodyValue, IResultCell cell );

	/**
	 * Fired whenever the default expression's body property for the attribute has changed.
	*/
	public void onDefaultBodyModified( IAttribute feature, IResultCell cell );

	/**
	 * Fired whenever the default expression's language property for the attribute is about to change.
	*/
	public void onPreDefaultLanguageModified( IAttribute feature, String language, IResultCell cell );

	/**
	 * Fired whenever the default expression's language property for the attribute has changed.
	*/
	public void onDefaultLanguageModified( IAttribute feature, IResultCell cell );

	/**
	 * Fired whenever the attributes derived property is about to change.
	*/
	public void onPreDerivedModified( IAttribute feature, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the attributes derived property has changed.
	*/
	public void onDerivedModified( IAttribute feature, IResultCell cell );

	/**
	 * Fired whenever the attributes primary key property is about to change.
	*/
	public void onPrePrimaryKeyModified( IAttribute feature, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the attributes primary key property has changed.
	*/
	public void onPrimaryKeyModified( IAttribute feature, IResultCell cell );
}
