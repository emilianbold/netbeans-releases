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


