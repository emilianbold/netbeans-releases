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
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;

public interface IAttribute extends IStructuralFeature, IParameterableElement
{
	/**
	 * Specifies whether the Attribute is derived, i.e. its value or values can be computed from other information. The default value is false.
	*/
	public boolean getIsDerived();

	/**
	 * Specifies whether the Attribute is derived, i.e. its value or values can be computed from other information. The default value is false.
	*/
	public void setIsDerived( boolean value );

	/**
	 * References an optional expression specifying how to set the attribute when creating an instance in the absence of a specific setting for the attribute.
	*/
	public IExpression getDefault();

	/**
	 * References an optional expression specifying how to set the attribute when creating an instance in the absence of a specific setting for the attribute.
	*/
	public void setDefault( IExpression value );

	/**
	 * property DerivationRule
	*/
	public IExpression getDerivationRule();

	/**
	 * property DerivationRule
	*/
	public void setDerivationRule( IExpression value );

	/**
	 * property AssociationEnd
	*/
	public IAssociationEnd getAssociationEnd();

	/**
	 * property AssociationEnd
	*/
	public void setAssociationEnd( IAssociationEnd value );

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public String getDefault2();

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault2( String value );

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public ETPairT<String,String> getDefault3();

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault3( String lang, String body );

	/**
	 * Determines whether or not this attribute has the WithEvents modifier associated with it. This is specific to the VB programming language.
	*/
	public boolean getIsWithEvents();

	/**
	 * Determines whether or not this attribute has the WithEvents modifier associated with it. This is specific to the VB programming language.
	*/
	public void setIsWithEvents( boolean value );

	/**
	 * Indicates whether or not the attribute instance is created on the heap or not upon the instanciation of the featuring classifier.
	*/
	public boolean getHeapBased();

	/**
	 * Indicates whether or not the attribute instance is created on the heap or not upon the instanciation of the featuring classifier.
	*/
	public void setHeapBased( boolean value );

	/**
	 * Indicates whether or not this attribute maps to a primary key column in a database.
	*/
	public boolean getIsPrimaryKey();

	/**
	 * Indicates whether or not this attribute maps to a primary key column in a database.
	*/
	public void setIsPrimaryKey( boolean value );

}
