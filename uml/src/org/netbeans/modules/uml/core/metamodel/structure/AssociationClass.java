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

package org.netbeans.modules.uml.core.metamodel.structure;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.constructs.Class;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Association;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class AssociationClass extends Class implements IAssociationClass
{

	/**
	 * 
	 */
	public AssociationClass() 
	{
		super();
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:AssociationClass",doc,parent);
	}
	
	public void establishDefaultName()
	{
		super.establishDefaultName();
	}

   /// IAssociation pass through methods

   /**
    * Adds this end to the association
   */
   public void addEnd( IAssociationEnd end )
   {
      getAssociation().addEnd( end );
   }

   /**
    * Removes this end from the assocaition
   */
   public void removeEnd( IAssociationEnd end )
   {
      getAssociation().removeEnd( end );
   }


   /**
    * Returns the assocaition ends as a list
   */
   public ETList<IAssociationEnd> getEnds()
   {
      return getAssociation().getEnds();
   }


   /**
    * Returns the number of ends in this association.
   */
   public int getNumEnds()
   {
      return getAssociation().getNumEnds();
   }

   /**
    * What is the index of this end in the ends list.  -1 if the end is not found
   */
   public int getEndIndex( IAssociationEnd end )
   {
      return getAssociation().getEndIndex( end );
   }

   /**
    * property IsDerived
   */
   public boolean getIsDerived()
   {
      return getAssociation().getIsDerived();
   }

   /**
    * property IsDerived
   */
   public void setIsDerived( boolean value )
   {
      getAssociation().setIsDerived( value );
   }

   /**
    * Adds an Classifier to this Association. The result is that addition of a new AssociationEnd.
   */
   public IAssociationEnd addEnd2( IClassifier participant )
   {
      return getAssociation().addEnd2( participant );
   }

   /**
    * Adds an Classifier to this Association. The result is that addition of a new AssociationEnd. The end is not returned.
   */
   public void addEnd3( IClassifier participant )
   {
      getAssociation().addEnd3( participant );
   }

   /**
    * Adds an Classifier to this Association. The result is that addition of a new AssociationEnd. The end is not returned.
   */
   public IAggregation transformToAggregation( boolean IsComposite )
   {
      return getAssociation().transformToAggregation( IsComposite );
   }

   /**
    * Is this association reflexive, i.e., do both ends of the association point at the same Classifier?
   */
   public boolean getIsReflexive()
   {
      return getAssociation().getIsReflexive();
   }

   /**
    * Goes through all the ends and returns all participants
   */
   public ETList<IElement> getAllParticipants()
   {
      return getAssociation().getAllParticipants();
   }

   /**
    * Returns the first end with this guy as a participant
    */
   public IAssociationEnd getFirstEndWithParticipant(IElement participant)
   {
      return getAssociation().getFirstEndWithParticipant( participant );
   }

   /**
    * Returns the end at this index
    */
   public IAssociationEnd getEndAtIndex(int nIndex)
   {
      return getAssociation().getEndAtIndex( nIndex );
   }

   
   /// IRelationship pass through methods
   
   public ETList<IElement> getRelatedElements()
   {
      return getAssociation().getRelatedElements();
   }


   // Helper methods for IAssociation

//  Sets the XML node associated with this element.
// HRESULT Node([in] IXMLDOMNode* newVal);
   public void setNode(Node n)
   {
      super.setNode( n );
      
      if( m_assocation != null )
      {
         m_assocation.setNode( getNode() );
      }
   }
   
   protected IAssociation getAssociation()
   {
      if( null == m_assocation )
      {
         m_assocation = new Association();
         m_assocation.setNode( getNode() );
      }
      
      return m_assocation;
   }

	
   private IAssociation m_assocation = null;
}


