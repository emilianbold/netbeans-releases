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

public interface IAggregation extends IAssociation
{
	/**
	 * The association end connected to the classifier specifying the aggregate.
	*/
	public IAssociationEnd getAggregateEnd();

	/**
	 * The association end connected to the classifier specifying the aggregate.
	*/
	public void setAggregateEnd( IAssociationEnd value );

	/**
	 * Sets the classifier that will be set as a participant on a new AssociationEnd that will be created and returned.
	*/
	public IAssociationEnd setAggregateEnd( IClassifier newVal );

	/**
	 * Sets the classifier that will be set as a participant on a new AssociationEnd that will be created but not returned.
	*/
	public void setAggregateEnd2( IClassifier newVal );

	/**
	 * Indicates the association end connected to the classifier specifying the part.
	*/
	public IAssociationEnd getPartEnd();

	/**
	 * Indicates the association end connected to the classifier specifying the part.
	*/
	public void setPartEnd( IAssociationEnd end );

	/**
	 * Sets the classifier that will be placed as the participant on a new AssociationEnd that will be created and returned on the PartEnd of this Aggregation.
	*/
	public IAssociationEnd setPartEnd( IClassifier newVal );

	/**
	 * Sets the classifier that will be placed as the participant on a new AssociationEnd that will be created ( but not returned ) on the PartEnd of this Aggregation.
	*/
	public void setPartEnd2( IClassifier newVal );

	/**
	 * Indicates the nature of the aggregation. If false, the classifier at the aggregate end represents a shared aggregate, and the instance specified by the classifier at the part end may be contained in other aggregates. If true, the classifier at the aggregate enIÒ?
	*/
	public boolean getIsComposite();

	/**
	 * Indicates the nature of the aggregation. If false, the classifier at the aggregate end represents a shared aggregate, and the instance specified by the classifier at the part end may be contained in other aggregates. If true, the classifier at the aggregate enIÒ?
	*/
	public void setIsComposite( boolean value );

	/**
	 * Makes the AggregateEnd the PartEnd and the PartEnd the AggregateEnd.
	*/
	public void reverseEnds();

	/**
	 * Demotes this Aggregation to an association.
	*/
	public IAssociation transformToAssociation();

	/**
	 * Is this end the aggregate end?
	*/
	public boolean isAggregateEnd( IAssociationEnd pQueryEnd );

}
