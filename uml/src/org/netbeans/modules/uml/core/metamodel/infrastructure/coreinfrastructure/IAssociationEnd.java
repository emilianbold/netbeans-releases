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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IAssociationEnd extends IStructuralFeature
{
	/**
	 * property Association
	*/
	public IAssociation getAssociation();

	/**
	 * property Association
	*/
	public void setAssociation( IAssociation value );

	/**
	 * method AddQualifier
	*/
	public void addQualifier( IAttribute qual );

	/**
	 * method RemoveQualifier
	*/
	public void removeQualifier( IAttribute qual );

	/**
	 * Creates an Qualifier. The new qualifier is not added to this AssociationEnd.
	*/
	public IAttribute createQualifier( String Type, String Name );

	/**
	 * Creates an Qualifier. The new qualifier is not added to this AssociationEnd.
	*/
	public IAttribute createQualifier2( IClassifier Type, String Name );

	/**
	 * Creates an Qualifier with a default name and type, dependent on the current language settings. The new attribute is not added to this Classifier.
	*/
	public IAttribute createQualifier3();

	/**
	 * property Qualifiers
	*/
	public ETList<IAttribute> getQualifiers();

	/**
	 * Designates the Classifier participating in the Association at the given end.
	*/
	public IClassifier getParticipant();

	/**
	 * Designates the Classifier participating in the Association at the given end.
	*/
	public void setParticipant( IClassifier value );

	/**
	 * Retrieves the other ends of the Association this end is a part of.
	*/
	public ETList<IAssociationEnd> getOtherEnd();

	/**
	 * Turns this end into a NavigableEnd.
	*/
	public INavigableEnd makeNavigable();

	/**
	 * Determines whether or not this end is navigable.
	*/
	public boolean getIsNavigable();

	/**
	 * Retrieves the first end found in the OtherEnd collection. This is usually sufficient in every association other than a ternary.
	*/
	public IAssociationEnd getOtherEnd2();

	/**
	 * Determines whether or not the participant encapsulates the same data as the passed in element
	*/
	public boolean isSameParticipant( IVersionableElement element );

}
