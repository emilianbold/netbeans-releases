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
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IAssociation extends IClassifier, IRelationship
{
	/**
	 * Adds this end to the association
	*/
	public void addEnd( IAssociationEnd end );

	/**
	 * Removes this end from the assocaition
	*/
	public void removeEnd( IAssociationEnd end );

	/**
	 * Returns the assocaition ends as a list
	*/
	public ETList<IAssociationEnd> getEnds();

	/**
	 * Returns the number of ends in this association.
	*/
	public int getNumEnds();

	/**
	 * What is the index of this end in the ends list.  -1 if the end is not found
	*/
	public int getEndIndex( IAssociationEnd pEnd );

	/**
	 * property IsDerived
	*/
	public boolean getIsDerived();

	/**
	 * property IsDerived
	*/
	public void setIsDerived( boolean value );

	/**
	 * Adds an Classifier to this Association. The result is that addition of a new AssociationEnd.
	*/
	public IAssociationEnd addEnd2( IClassifier participant );

	/**
	 * Adds an Classifier to this Association. The result is that addition of a new AssociationEnd. The end is not returned.
	*/
	public void addEnd3( IClassifier participant );

	/**
	 * Adds an Classifier to this Association. The result is that addition of a new AssociationEnd. The end is not returned.
	*/
	public IAggregation transformToAggregation( boolean IsComposite );

	/**
	 * Is this association reflexive, i.e., do both ends of the association point at the same Classifier?
	*/
	public boolean getIsReflexive();

	/**
	 * Goes through all the ends and returns all participants
	*/
	public ETList<IElement> getAllParticipants();

	/**
	 * Returns the first end with this guy as a participant
	 */
	public IAssociationEnd getFirstEndWithParticipant(IElement pParticipant);

	/**
	 * Returns the end at this index
	 */
	public IAssociationEnd getEndAtIndex(int nIndex);

}
