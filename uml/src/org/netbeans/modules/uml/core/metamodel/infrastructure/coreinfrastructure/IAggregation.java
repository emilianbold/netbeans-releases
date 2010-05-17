/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
	 * Indicates the nature of the aggregation. If false, the classifier at the aggregate end represents a shared aggregate, and the instance specified by the classifier at the part end may be contained in other aggregates. If true, the classifier at the aggregate
	*/
	public boolean getIsComposite();

	/**
	 * Indicates the nature of the aggregation. If false, the classifier at the aggregate end represents a shared aggregate, and the instance specified by the classifier at the part end may be contained in other aggregates. If true, the classifier at the aggregate
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
