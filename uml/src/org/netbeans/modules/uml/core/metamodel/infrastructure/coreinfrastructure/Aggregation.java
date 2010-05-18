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

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IAggregationKind;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

public class Aggregation extends Association implements IAggregation
{
	/**
	 * Retrieves the AssociationEnd that is the part in this Aggregation.
	 */
	public IAssociationEnd getPartEnd()
	{
		return getEnd(true);
	}
	
	/**
	 * Sets the passed-in end as the part side of this Aggregate.
	 *
	 * @param end[in] The end to play the part
	 */
	public void setPartEnd( IAssociationEnd end )
	{
		setEnd(true,end);
	}
	
	/**
	 * The association end connected to the classifier specifying the aggregate.
	 *
	 * @param end[in] The end to play the part
	 */
	public IAssociationEnd getAggregateEnd()
	{
		return getEnd(false);
	}
	
	/**
	 * The association end connected to the classifier specifying the aggregate.
	 *
	 * @param end[in] The end to play the part
	 */
	public void setAggregateEnd( IAssociationEnd end )
	{
		setEnd(false,end);
	}	
	
	/**
	 * Indicates the nature of the aggregation.  If false, the classifier at the
	 * aggregate end represents a shared aggregate, and the instance
	 * specified by the classifier at the part end may be contained in other aggregates.
	 *
	 * @param newVal [in]
	 */
	public boolean getIsComposite()
	{
		return super.getBooleanAttributeValue("isComposite",false); 	
	}
	
	/**
	 * Indicates the nature of the aggregation.  If false, the classifier at the
	 * aggregate end represents a shared aggregate, and the instance
	 * specified by the classifier at the part end may be contained in other aggregates.
	 */
	public void setIsComposite( boolean newValue )
	{
		super.setBooleanAttributeValue("isComposite",newValue);
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		super.buildNodePresence("UML:Aggregation",doc,parent);
	}
	
	/**
	 *
	 * Retrieves a particular end of this aggregation.
	 *
	 * @param partEnd[in] True to retrieve the part end, else false to retrieve
	 *                    the aggregate end
	 */
	protected IAssociationEnd getEnd(boolean partEnd)
	{
		IAssociationEnd assocEnd = null;
		String attrName = "aggregateEnd";
		if (partEnd)
		{
			attrName = "partEnd";
		}
		String id = UMLXMLManip.getAttributeValue(m_Node,attrName);
		if (id != null && id.length() > 0)
		{
			StringBuffer tempQuery = new StringBuffer(); 
			tempQuery.append("./UML:Association.end/*[@xmi.id=\"")
					 .append(id)
					 .append("\"]");
			String query = tempQuery.toString();
			ElementCollector< IAssociationEnd > collector = 
									new ElementCollector< IAssociationEnd >();
			assocEnd = collector.retrieveSingleElement(m_Node,query, IAssociationEnd.class);							 					 
		}
		return assocEnd;		
	}
	
	/**
	 *
	 * Sets one of the ends of this Aggregation.
	 *
	 * @param partEnd[in] True if end is to be the part end of this aggregation,
	 *                    else false if it is to be the aggregate end
	 * @param end[in] The new end
	 */
	protected void setEnd(boolean partEnd,IAssociationEnd assocEnd)
	{		
		String attrName = "aggregateEnd";		
		String id = null;
		if (partEnd)
		{
			attrName = "partEnd";
		}
		if (assocEnd != null)
		{
			id = assocEnd.getXMIID();
		}
		super.addEnd(assocEnd);
		XMLManip.setAttributeValue(m_Node,attrName,id);					
	}
	
	/**
	 * Makes the part end the aggregate end, and the aggregate end the part end.
	 */
	public void reverseEnds()
	{	
		String partID = UMLXMLManip.getAttributeValue(m_Node,"partEnd");
		String aggID = 	UMLXMLManip.getAttributeValue(m_Node,"aggregateEnd");
		UMLXMLManip.setAttributeValue(this,"partEnd",aggID);	
		UMLXMLManip.setAttributeValue(this,"aggregateEnd",partID);
	}
	
	/**
	 * Transforms this Aggregation into an Association.
	 */
	public IAssociation transformToAssociation()
	{
		IAssociation assoc = null;
		IVersionableElement ver = (IVersionableElement)this;
		Object obj = UMLXMLManip.transformElement(ver,"Association");
		if (obj instanceof Association)
		{
			assoc = (Association)obj;
		}			
		return assoc;
	}
	
	/**
	 * Is this end the aggregate end?
	 *
	 * @param pQueryEnd [in] The end to query to see if this one is the 
	 * aggregations aggregate end
	 */
	public boolean isAggregateEnd( IAssociationEnd queryEnd )
	{
		boolean isAggregEnd = false;
		IAssociationEnd  pAggregateEnd = getAggregateEnd();
		if (pAggregateEnd != null)
		{
			isAggregEnd = pAggregateEnd.isSame(queryEnd);
		}
		return isAggregEnd;
	}
	
	/**
	 * Sets the classifier that will be set as a participant on a new AssociationEnd that will be created and returned
	 *
	 * @param newVal[in] The Classifier
	 */
	public IAssociationEnd setAggregateEnd( IClassifier newValue )
	{
		return setEnd(false,newValue);
	}
	
   /**
	*  Sets the classifier that will be set as a participant on a new AssociationEnd that will be created but not returned
	*
	* @param newVal[in] The classifier
	*/
   public void setAggregateEnd2( IClassifier newVal )
   {
   	    setEnd(false,newVal);
   }
   
   /**
	* Sets the classifier that will be placed as the participant on a new AssociationEnd that will be 
	* created and returned on the PartEnd of this Aggregation
	*
	* @param newVal[in]    The classifier
	*/
   public IAssociationEnd setPartEnd( IClassifier newVal )
   {
        return setEnd(true,newVal);
   }
   
   /**
	* Sets the classifier that will be placed as the participant on a new AssociationEnd that will be created 
 	* ( but not returned ) on the PartEnd of this Aggregation
	*
	* @param newVal[in]    The classifier
	*/
   public void setPartEnd2( IClassifier newVal )
   {
		setEnd(true,newVal);
   }   
   
   /**
	* Sets the appropriate end on this Aggregation based on parameters passed in.
	*
	* @param partEnd[in]      true if the end is to be placed on the part end, else false to be placed
	*                         on the aggregate end.
	* @param classifier[in]   The Classifier that will be the participant of the end
	*/
   protected IAssociationEnd setEnd(boolean partEnd,IClassifier classifier)
   {
		IAssociationEnd assocEnd = null;
		String attrName = "aggregateEnd";		
		String id = null;
		if (partEnd)
		{
			attrName = "partEnd";
		} 
		assocEnd = super.addEnd2(classifier);
		if (assocEnd != null)
		{
			id = assocEnd.getXMIID();
		}
		XMLManip.setAttributeValue(m_Node,attrName,id);
		return assocEnd;
   }   
   
   public String getExpandedElementType()
    {
       boolean isNavigable = false;
       boolean isComposite = getIsComposite();
       String expandedType = null;
       
       IAssociationEnd partEnd = this.getPartEnd();
       if (partEnd != null) 
       {
           isNavigable = partEnd.getIsNavigable();
       }
       
       if (isComposite)  // composition
       {
           expandedType = isNavigable ? IAggregationKind.NAV_COMPOSITION : IAggregationKind.COMPOSITION;
       }
       else   // Aggregation
       {
           expandedType = isNavigable ? IAggregationKind.NAV_AGGREGATION : IAggregationKind.AGGREGATION;
       }
       
       return expandedType;
    }
}
