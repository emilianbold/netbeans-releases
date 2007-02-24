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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;

public interface ITypedElement extends IElement, IMultiplicityListener
{
	/**
	 * Retrieve the type this element references.
	*/
	public IClassifier getType();

	/**
	 * Retrieve the type this element references.
	*/
	public void setType( IClassifier value );

	/**
	 * property Multiplicity
	*/
	public IMultiplicity getMultiplicity();

	/**
	 * property Multiplicity
	*/
	public void setMultiplicity( IMultiplicity value );

	/**
	 * property Ordering
	*/
	public int getOrdering();

	/**
	 * property Ordering
	*/
	public void setOrdering( int value );

	/**
	 * Retrieve the XML ID that is the reference to the type classifier.
	*/
	public String getTypeID();

	/**
	 * Tells whether the values of the typed element are sets of the type or not.
	*/
	public boolean getIsSet();

	/**
	 * Tells whether the values of the typed element are sets of the type or not.
	*/
	public void setIsSet( boolean value );

    /**
     * Clones this ITypedElement.
     * @return The cloned object.
     */	
	public IVersionableElement performDuplication();
}