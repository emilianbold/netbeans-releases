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

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreventElementReEntrance;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import java.util.List;
import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.tree.DefaultDocument;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IAggregationKind;

public class Association extends Classifier implements IAssociation, IRelationship 
{
	//	Used to make sure this query remains consistant in the couple of operations
	//	that use it in this file.
 	public static final String END_QUERY = "UML:Association.end/*" ;
 	
	/**
	 * property IsDerived
	 */
	public boolean getIsDerived()
	{
		return getBooleanAttributeValue("isDerived",false);
	}
	
	/**
	 * property setIsDerived
	 */
	public void setIsDerived(boolean newVal)
	{
		setBooleanAttributeValue("isDerived",newVal);
	}
		
	/**
	 *
	 * Returns the association ends as a list.
	 */
	public ETList<IAssociationEnd> getEnds()
	{
		ElementCollector<IAssociationEnd> collector = new ElementCollector<IAssociationEnd>();
		return collector.retrieveElementCollection(m_Node, END_QUERY, IAssociationEnd.class);
	}
	
	/**
	 * Removes this end from the assocaition
	*/
	public void removeEnd( IAssociationEnd end )
	{
            PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
            try {
                if ( !reEnt.isBlocking() )
    		{
                    RelationshipEventsHelper help = new RelationshipEventsHelper(this);
                    if (help.firePreEndRemoved(end,null))
                    {
                        final IAssociationEnd assocEnd = end;
                        new ElementConnector<IAssociation>().removeElement(
                           this,assocEnd,"UML:Association.end/*",
                           new IBackPointer<IAssociation>() 
                           {
                                  public void execute(IAssociation obj) 
                                  {
                                         assocEnd.setAssociation(obj);
                                  }
                          }
                        );
                        help.fireEndRemoved();		
                    }
                    else
                    {
                            //Cancel the event
                    }
    		}
            }
            finally
            {
                reEnt.releaseBlock();
            }		
	}
	
	/**
	 * Adds this end to the association
	*/
	public void addEnd( IAssociationEnd end )
	{
            RelationshipEventsHelper help = new RelationshipEventsHelper(this);
            addEnd(help,true,end);
	}
	
	/**
	 * Returns the number of ends in this association
	 *
	 */
	public int getNumEnds()
	{
            ETList<IAssociationEnd>  pEnds = getEnds();
            int len = -1;		  
            if (pEnds != null)
            {
                 len = pEnds.size();
            }
            return len;
	}	
	/**
	 * Returns the index of this end in the list.  -1 if not found.
	 *
	 * @param pEnd [in]
	 */
	public int getEndIndex(IAssociationEnd pEnd)
	{
		ETList<IAssociationEnd>  pEnds = getEnds();
		int retVal = -1;
		int len = -1;		  
		if (pEnds != null)
		{
			len = pEnds.size();
			boolean bIsSame = false;
			for (int i = 0; i < len && !bIsSame; i++)
			{
				IAssociationEnd asso = pEnds.get(i);
				if (asso != null)
				{
					bIsSame = asso.isSame(pEnd);
					if (bIsSame)
						retVal = i;
				}
			}
		}
		return retVal;
	}
	
	/**
	 *
	 * Creates, sets the participant, and returns a new AssociationEnd object.
	 *
	 * @param participant[in] The Classifier that is the Participant of the new
	 *                        AssociationEnd object
	 */
	public IAssociationEnd addEnd2(IClassifier participant)
	{		
		IAssociationEnd assocEnd = (IAssociationEnd)createEnd();
		if (assocEnd != null)
		{
			addEnd(assocEnd);
			assocEnd.setParticipant(participant); 
		}
		return assocEnd;
	}
	
	/**
	 *
	 * Creates and sets the participant on a new AssociationEnd that is added
	 * to this Association.
	 *
	 * @param participant[in] The Classifier that is the Participant of the new
	 *                        AssociationEnd object.
	 */
	public void addEnd3(IClassifier participant)
	{
		IAssociationEnd assocEnd = addEnd2(participant); 
	}
	
	/**
	 *
	 * Creates an AssociationEnd for this association.
	 */
	public IElement createEnd()
	{
		FactoryRetriever fact = FactoryRetriever.instance();
		if (fact != null)
		{
			Object obj = new Object();
			// FIXME: Pass null Object here?
			return (IElement) fact.createType("AssociationEnd",obj);
		}
		return null;
	}
	
	/**
	 *
	 * Transforms this association into an aggregation.
	 *
	 * @param isComposite[in] True if the new aggregation is a composition
	 */
	public IAggregation transformToAggregation(boolean isComposite) 
	{
		IAggregation agg = null;
		ETList<IAssociationEnd>  pEnds = getEnds();
		
		int retVal = -1;
		int len = -1;		  
		if (pEnds != null)
		{
			len = pEnds.size();
			if (len <= 2)
			{
				// The first end in the collection will be the aggregateEnd, the
				// second will be the partEnd
				IAssociationEnd end = pEnds.get(0);
				Document doc = null;
				if (end != null)
				{
					doc = end.getNode().getDocument();
					String aggID = end.getXMIID();
					end = pEnds.get(1);
					if (end != null)
					{
						String partID = end.getXMIID();
						
						agg = (IAggregation) transform("Aggregation");
						agg.setIsComposite(isComposite);
						UMLXMLManip.setAttributeValue(agg,"partEnd",partID);
						UMLXMLManip.setAttributeValue(agg,"aggregateEnd",aggID);
					}									
				}
				
				// Enforce to clean the cache for the same ids
				for (int i = 0; i < pEnds.size(); i++)
				{
					IAssociationEnd end1 = pEnds.get(0);
					((DefaultDocument)doc).removeIDFromTable(end1.getXMIID());						
					((DefaultDocument)doc).addIDtoTable(end1.getXMIID(), end1.getNode());
				}
			}
			else
			{
				//ids cann't be changed. throw exception
			}
		}
		return agg;				
	}
	
	/**
	 *
	 * Determines whether or not this association is reflexive.
	 */
	public boolean getIsReflexive()
	{
		boolean retVal = false;		
		List list = XMLManip.selectNodeList(m_Node, "./UML:Association.end/*");
		if (list != null)
		{
			int size = list.size();
			if (size == 2)
			{
				Node firstNode = (Node)list.get(0);
				Node secondNode = (Node)list.get(1);
				if (firstNode != null && secondNode != null)
				{
					String firstType = XMLManip.getAttributeValue(firstNode,"type");
					String secondType = XMLManip.getAttributeValue(secondNode,"type");
					if (firstType != null && firstType.equals(secondType))
						retVal = true;
				}
			}			
		}

		return retVal;		
	}
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node
	 */
	public void establishNodePresence( Document doc, Node parent )
	{
		buildNodePresence("UML:Association" ,doc,parent);	
	}
	
	public void establishDefaultName()
	{		
	}	
	
	public void addEnd( RelationshipEventsHelper helper, boolean bool , IAssociationEnd end )
	{
            final IAssociationEnd assocEnd = end;
            new ElementConnector<IAssociation>().addChildAndConnect(
                this,false,"UML:Association.end",
                "UML:Association.end",assocEnd,
                new IBackPointer<IAssociation>() 
                {
                     public void execute(IAssociation obj) 
                     {
                         assocEnd.setAssociation(obj);
                     }
                }										
            );
	}
	
	/**
	 *
	 * Called by the framework after this Association has been 
	 * completely deleted.
	 *
	 * @param ver[in] The COM object representing this impl instance
	 */
	public void fireDelete( IVersionableElement ver )
	{
		super.fireDelete(ver);
		IAssociation asso = (IAssociation)ver;
		RelationshipEventsHelper helper = new RelationshipEventsHelper(this);
		helper.fireRelationDeleted();
		markParticipantsDirty();
	}
	
	/**
	 *
	 * Fired by the framework right before this Association is to
	 * be deleted.
	 *
	 * @param ver[in] The COM object representing this impl instance
	 */
	public boolean firePreDelete( IVersionableElement ver )
	{
		boolean retVal = super.firePreDelete(ver);
		if (retVal)
		{
			RelationshipEventsHelper helper = new RelationshipEventsHelper(this);
			helper.firePreRelationDeleted();
		}
		return retVal;	
	}
	
	/**
	 * Goes through all the ends and returns all participants.
	 */
	public ETList<IElement> getAllParticipants()
	{
		int numEnds = 0;
		ETList<IElement> retVal = new ETArrayList<IElement>();
		ETList<IAssociationEnd> pEnds = getEnds();
		numEnds = pEnds.size();		

		for (int i = 0 ; i < numEnds ; i++ )
		{
		   IAssociationEnd pEnd = pEnds.get(i);		   

		   if (pEnd != null)
		   {
			  IClassifier pEndParticipant = pEnd.getParticipant();

			  if (pEndParticipant != null)
			  {
			  	 retVal.add(pEndParticipant);				 
			  }
		   }
		}		
		return retVal;
	}
	
	/**
	 *
	 * Called when this Association has been deleted. Simply makes sure that all the Participant ends of 
	 * this association are marked as dirty if those elements have been versioned.
	 *
	 * @param ver[in] The Association being deleted
	 */
	public void markParticipantsDirty()
	{
		int numEnds = 0;		
		ETList<IAssociationEnd> pEnds = getEnds();
		numEnds = pEnds.size();		

		for (int i = 0 ; i < numEnds ; i++ )
		{
		   IAssociationEnd pEnd = pEnds.get(i);		   

		   if (pEnd != null)
		   {
			  IClassifier pEndParticipant = pEnd.getParticipant();

			  if (pEndParticipant != null)
			  {
				  pEndParticipant.setDirty(true);				 
			  }
		   }
		}		
	}
	
	/**
	 *
	 * Removes all elements that should also be deleted or at least modified
	 * when this element is deleted.
	 *
	 * @param thisElement[in] The COM object representing this element
	 */
	public void performDependentElementCleanUp( IVersionableElement thisElement )
	{
		super.performDependentElementCleanUp(thisElement);
		
		//Clean up the ends
		ETList<IAssociationEnd> ends = getEnds();
		
		new CollectionTranslator().cleanCollection(ends);
	}
	
	public ETList<IElement> getRelatedElements()
	{
		ElementCollector<IElement> collector = new ElementCollector<IElement>();	
		return collector.retrieveElementCollection(m_Node, END_QUERY, IElement.class);
	}


	public IVersionableElement performDuplication()
	{
		IVersionableElement dup = super.performDuplication();
		return dup;
	}
	
	public void establishNodeAttributes( Element node )
	{		
		super.establishNodeAttributes(node);
	}	

	/**
	 * Returns the first end with this guy as a participant
	 */
	public IAssociationEnd getFirstEndWithParticipant(IElement pParticipant)
	{
		IAssociationEnd retObj = null;
		ETList<IAssociationEnd> ends = getEnds();
		if (ends != null)
		{
			int count = ends.size();
			for (int i=0; i<count; i++)
			{
				IAssociationEnd end = ends.get(i);
				if (end.isSameParticipant(pParticipant))
				{
					retObj = end;
					break;
				}
			}
		}
		return retObj;
	}

	/**
	 * Returns the end at this index
	 */
	public IAssociationEnd getEndAtIndex(int nIndex)
	{
		IAssociationEnd retObj = null;
		ETList<IAssociationEnd> ends = getEnds();
		if (ends != null)
		{
			int count = ends.size();
			if (nIndex < count)
			{
				retObj = ends.get(nIndex);
			}
		}
		return retObj;
	}
        
        public boolean isNavigable () 
        {
            boolean navigable = false;
            ETList<IAssociationEnd> ends = getEnds();
            if (ends != null) 
            {
                int count = ends.size();
                for (int i = 0; i < count; i++) 
                {
                    IAssociationEnd end = ends.get(i);
                    if (end.getIsNavigable()) 
                    {
                        navigable = true;
                        break;
                    }
                }
            }
            return navigable;
        }
        
        public String getExpandedElementType()
        {
           return this.isNavigable()? IAggregationKind.NAV_ASSOCIATION : IAggregationKind.ASSOCIATION;
        }
}
