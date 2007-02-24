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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;

public interface IStructuralFeature extends IFeature,ITypedElement
{
	/**
	 * property ClientChangeability
	*/
	public int getClientChangeability();

	/**
	 * property ClientChangeability
	*/
	public void setClientChangeability( /* ChangeableKind */ int value );

	/**
	 * Sets the type of this feature via a string type. That string will be resolved into the proper Classifier.
	*/
	public void setType2( String value );

	/**
	 * property Type
	*/
	public IClassifier getType();

	/**
	 * property Type
	*/
	public void setType( IClassifier value );

	/**
	 * property Ordering
	*/
	public int getOrdering();

	/**
	 * property Ordering
	*/
	public void setOrdering( /* OrderingKind */ int value );

	/**
	 * property Multiplicity
	*/
	public IMultiplicity getMultiplicity();

	/**
	 * property Multiplicity
	*/
	public void setMultiplicity( IMultiplicity value );

	/**
	 * The name of the Classifier who specifies this Parameter's type.
	*/
	public String getTypeName();

	/**
	 * The name of the Classifier who specifies this Parameter's type.
	*/
	public void setTypeName( String value );

	/**
	 * The volatility state of this feature. Most applicable to the C/C++ languages.
	*/
	public boolean getIsVolatile();

	/**
	 * The volatility state of this feature. Most applicable to the C/C++ languages.
	*/
	public void setIsVolatile( boolean value );

	/**
	 * Determines whether or not this feature persists or not.
	*/
	public boolean getIsTransient();

	/**
	 * Determines whether or not this feature persists or not.
	*/
	public void setIsTransient( boolean value );

    
    public String getRangeAsString();
}
