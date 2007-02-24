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


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;

public interface IPart extends IStructuralFeature,
							   IConnectableElement,
							   IParameterableElement
{
	/**
	 * property PartKind
	*/
	public int getPartKind();

	/**
	 * property PartKind
	*/
	public void setPartKind( /* PartKind */ int value );

	/**
	 * property IsWhole
	*/
	public boolean getIsWhole();

	/**
	 * property IsWhole
	*/
	public void setIsWhole( boolean value );

	/**
	 * property InitialCardinality
	*/
	public int getInitialCardinality();

	/**
	 * property InitialCardinality
	*/
	public void setInitialCardinality( int value );

	/**
	 * The IStructuralFeature that this Part represents. This will be empty when the Part represents the entire Classifier.
	*/
	public IStructuralFeature getDefiningFeature();

	/**
	 * The IStructuralFeature that this Part represents. This will be empty when the Part represents the entire Classifier.
	*/
	public void setDefiningFeature( IStructuralFeature value );
}
