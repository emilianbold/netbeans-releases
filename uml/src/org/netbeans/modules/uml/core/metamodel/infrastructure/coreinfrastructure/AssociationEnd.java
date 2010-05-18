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

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreventElementReEntrance;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageDataType;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.dom4j.Node;
import org.dom4j.Document;

import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;


public class AssociationEnd extends StructuralFeature implements IAssociationEnd
{
        private IMultiplicity multiplicity;
        
	/**
	 * getAssociation. Gets the association
	 */
	public IAssociation getAssociation()
	{
		if(getNode() != null )
		{
		   Node parentNode = getNode().getParent();		   

		   if( parentNode != null )
		   {
			  // The node we want is always to up from the actual
			  // Parameter node. UML:BehavioralFeature/UML:BehavioralFeature.parameter..
			  Node owningNode = parentNode.getParent();			  
			  if( owningNode != null)
			  {
				 FactoryRetriever fact = FactoryRetriever.instance();
				 if( fact != null)
				 {
					Object obj = fact.createTypeAndFill( retrieveSimpleName(owningNode), 
							 							  owningNode );

					//return IAssociation after casting the obj to IAssociation.
                    if (obj instanceof IAssociation)
                        return (IAssociation) obj;
				 }
			  }
		   }
		}
		return null;		
	}	

	public void setAssociation(IAssociation assoc)
	{
		final IAssociation association = assoc;
		new ElementConnector<IAssociationEnd>().addChildAndConnect(
										 this,true,"association","association",
										 association,
										 new IBackPointer<IAssociationEnd>() 
										 {
											 public void execute(IAssociationEnd obj) 
											 {
                                                 if (association != null)
												    association.addEnd(obj);
											 }
										 }										
										);
	}
	
	public void addQualifier(IAttribute attr)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		// Pop the context that has been plugging events
		// for feature.
		EventContextManager man = new EventContextManager();
		man.revokeEventContext( attr, ret.getController() );
		
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		

		boolean proceed = true;
		IEventPayload payload = null;		
		if( disp != null )
		{
		   payload = disp.createPayload("PreQualifierAttributeAdded");	   
		   proceed = disp.firePreQualifierAttributeAdded(this,attr,payload);
		}
		if (proceed)
		{
			final IAttribute attribute = attr;
			new ElementConnector<IAssociationEnd>().addChildAndConnect(
											this,false,"UML:AssociationEnd.qualifier",
											"UML:AssociationEnd.qualifier",attribute,
											 new IBackPointer<IAssociationEnd>() 
											 {
												 public void execute(IAssociationEnd obj) 
												 {
													attribute.setAssociationEnd(obj);
												 }
											 }										
											);
			if (disp != null)
			{
				payload = disp.createPayload("QualifierAttributeAdded");
				disp.fireQualifierAttributeAdded(this,attr,payload);
			}
		}
	}
	
	public void removeQualifier(IAttribute attr)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		// Pop the context that has been plugging events
		// for feature.
		EventContextManager man = new EventContextManager();
		man.revokeEventContext( attr,ret.getController() );
		
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		

		boolean proceed = true;
		IEventPayload payload = null;		
		if( disp != null )
		{
		   payload = disp.createPayload("PreQualifierAttributeRemove");	   
		   proceed = disp.firePreQualifierAttributeRemoved(this,attr,payload);
		}
		if (proceed)
		{
			final IAttribute attribute = attr;
			new ElementConnector<IAssociationEnd>().removeElement(
											   this,attribute,"UML:AssociationEnd.qualifier/*",
											   new IBackPointer<IAssociationEnd>() 
											   {
												  public void execute(IAssociationEnd obj) 
												  {
													attribute.setAssociationEnd(obj);
												  }
											  }
											);
			if (disp != null)
			{
				payload = disp.createPayload("QualifierAttributeRemoved");
				disp.fireQualifierAttributeRemoved(this,attr,payload);
			}
		}
	}
	
	public ETList<IAttribute> getQualifiers()
	{
		ElementCollector<IAttribute> collector = new ElementCollector<IAttribute>();
		return collector.retrieveElementCollection(
											m_Node,
											"UML:AssociationEnd.qualifier/*", IAttribute.class);				
	}
   
   public void setType(IClassifier classifier)
   {
      setParticipant(classifier);
   }
   
   public void setFeaturingClassifier(IClassifier classifier)
   {
      setParticipant(classifier);
   }

   public IClassifier getFeaturingClassifier()
   {
      return getParticipant();
   }
      
	/**
	 * Designates the Classifier participating in the Association 
	 * at the given end.
	 * 
	 * @return IClassifier
	 */
	public IClassifier getParticipant()
	{
		return getType();		
	}
	
	/**
	 * Designates the Classifier participating in the Association 
	 * at the given end.
	 * 
	 * @param IClassifier
	 */
	public void setParticipant(IClassifier newParti)
	{
		PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
        try
        {
    		if ( !reEnt.isBlocking() )
    		{
    			IAssociation asso = getAssociation();
    			IClassifier currentParticipant = getParticipant();
    			if (currentParticipant != null)
    			{
    				IAssociationEnd curObj = (IAssociationEnd)this;
    				// Remove the current participant before putting the new one
    				currentParticipant.removeAssociationEnd(curObj);
    			}
            
            RelationshipEventsHelper helper = new RelationshipEventsHelper(this);
    			boolean isModified = helper.firePreEndModified("type",newParti,null);
    			if (isModified)
    			{
    				super.setType(newParti);
               if (newParti != null)
               {
        				newParti.addAssociationEnd(this);
               }
    				helper.fireEndModified();
    			}
    			else
    			{
    				//throw exception.				
    			}
    		}
        }
        finally
        {
            reEnt.releaseBlock();
        }
	}
	
	/**	 
	 * Retrieves the other ends of the Association this end is a part of.
	 */
	public ETList<IAssociationEnd> getOtherEnd()
	{
		IAssociation assoc = getAssociation();
		ETList<IAssociationEnd> otherEnds = new ETArrayList<IAssociationEnd>();
		if (assoc != null)
		{
			ETList<IAssociationEnd> assocEnds = assoc.getEnds();
			if (assocEnds != null)
			{
				int noOfEnds = assocEnds.size();
				boolean found = false;
				for (int i = 0; i < noOfEnds; i++)
				{
					IAssociationEnd end = assocEnds.get(i);
					if (end != null)
					{
						boolean same = isSame(end);
						if (!same)
						{
							 otherEnds.add(end);
							 found = true;
						}
					}
				}
				//Let's make sure we don't have a reflexive link
				if (!found && (noOfEnds ==2))
				{
					
				}
			}
		}
		return otherEnds;
	}
	
	/**
	 *
	 * Turns this end into a navigable end.
	 *
	 * @return INavigableEnd
	 * @warning Once MakeNavigable() has been called, the AssociationEnd that the call 
	 *          was made on should be discarded immediately. Use the INavigableEnd
	 *          returned from this method instead.
	 *
	 */
	public INavigableEnd makeNavigable()
	{
		String newForm = "NavigableEnd";
		
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
				(IClassifierEventDispatcher) ret.getDispatcher(
					EventDispatchNameKeeper.classifier());
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("PreAssociationEndTransform");
			proceed = disp.firePreAssociationEndTransform(this, newForm, payload);
		}

		if (proceed)
		{
			Object navEnd = UMLXMLManip.transformElement(this, newForm);
            INavigableEnd nEnd = navEnd instanceof INavigableEnd?
                    (INavigableEnd) navEnd : null;
            
            
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("AssociationEndTransform");
				disp.fireAssociationEndTransformed(nEnd, payload);
			}
            
            if (nEnd != null)
                return nEnd;
		}
		else
		{
			//cancel the event
		}
		return null;
	}
	
	public boolean getIsNavigable()
	{
		boolean isNavigable = false;
		if (this instanceof INavigableEnd)
			isNavigable = true ;
		
		return isNavigable;
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node	 
	 */
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:AssociationEnd",doc,parent);		
	}
	
	public void establishDefaultName()
	{
		//For now, AssociationEnds won't be named by default. Need a preference here
	}
	
	/**	 
	 * Retrieves the first end found in the OtherEnd collection. This is usually sufficient in 
	 * every association other than a ternary.
	 */
	public IAssociationEnd getOtherEnd2()
	{
		ETList<IAssociationEnd> assocEnds = getOtherEnd();
		IAssociationEnd end = null;
		if (assocEnds != null)
		{
			int noOfEnds = assocEnds.size();
			if (noOfEnds > 0)
			{
				end = assocEnds.get(0);				
			}		
		}
		return end;		
	}
	
	/**
	 *
	 * Determines whether or not the participant encapsulates the same data as the passed in element.
	 *
	 * @param element The element to check
	 */
	public boolean isSameParticipant(IVersionableElement elem)
	{
		boolean isSame = false;
		IClassifier participant = getParticipant();
		if (participant != null)
		{
			isSame = participant.isSame(elem);	
		}
		return isSame;
	}
	
	/**
	* Creates a new attribute with the passed-in information. The new attribute
	* is returned. NOTE: the attribute is NOT added to this Classifier.
	*
	* @param newType[in] The type of this attribute. If the type is not found
	*             in the model, a dummy DataType will be created with
	*             that type as the name. If 0 or "" is passed, a default type is
	*             used.
	* @param newName[in] The name of the attribute. If 0 or "" is passed, a default
	*                    name is used.
	*/
	public IAttribute createQualifier(String newType, String newName)
	{
		IAttribute retAttr = null;
		if (newType == null || newType.length() == 0)
		{			
			ICoreProduct prod = ProductRetriever.retrieveProduct();	
			if (prod != null)
			{
				ILanguageManager langMan = prod.getLanguageManager();
				if (langMan != null)
				{
					ILanguageDataType dataType = langMan.getAttributeDefaultType(this);
					if (dataType != null)
					{
						newType = dataType.getName();
					}					 
				}
			}
		}
		if (newName == null || newName.length() == 0)
		{
			newName = retrieveDefaultName();			
		}
        
		IClassifier classifier = resolveSingleClassifierFromString(newType);
		if (classifier != null)
		{
			retAttr = createQualifier2(classifier,newName);
		}
		return retAttr;
	}
	
	/**
	* Creates a new attribute with the passed-in information. The new attribute
	* is returned. NOTE: the attribute is NOT added to this Classifier.
	*
	* @param type[in] The type of this attribute
	* @param name[in] The name of the attribute
	*/
	public IAttribute createQualifier2(IClassifier type, String name)
	{
		//	Now create the new attribute and the type of the attribute
		FactoryRetriever fact = FactoryRetriever.instance();
		IAttribute newAttr = null;
		if (fact != null)
		{
			Object obj = new Object();
			Object ret = fact.createType("Attribute",obj);
			if (ret != null)
			{
				newAttr = (IAttribute)ret;
				if (newAttr != null)
					establishEventContext(newAttr);
				ITypedElement element = (ITypedElement)newAttr;
				if (element != null)
				{
					element.setType(type);		
				}
				newAttr.setName(name);
			}
		}
		return newAttr;
	}
	
	/**
	 * Creates a new Qualifier, giving it a default type and name.
	 */
	public IAttribute createQualifier3()
	{
		return createQualifier(null,null);
	}

	/**
	 *
	 * Attempts to find a single classifier in this classifier's namespace or 
	 * above namespaces. If more than one classifier is found with the same name, 
	 * only the first one is used.
	 *
	 * @param typeName[in] The name to match against
	 */
	public IClassifier resolveSingleClassifierFromString(String typeName)
	{
		INamedElement element = null;
		if (typeName != null && typeName.length() > 0)
		{
			element = resolveSingleTypeFromString(typeName);			
		}
		if (element != null)
			return (IClassifier)element;
		return null;			
	}
	
	/**
	 *
	 * Creates a new EventContext that will be propogated to all 
	 * EventDispatchers on the Product's EventDispatchController.
	 * This Context will prevent events from firing when initiated 
	 * from the passed-in element.
	 *
	 * @param feature[in] The feature to create the context and 
	 *                    EventFilter with
	 */
	public void establishEventContext(IFeature feature)
	{
		EventContextManager manager = new EventContextManager();
		manager.establishVersionableElementContext(this,feature,null);
	}
        
        public IMultiplicity getMultiplicity () 
        {
            if (multiplicity == null)
            {
                multiplicity = super.getMultiplicity();
            }
            return multiplicity;
        }
        
        public ETList<IMultiplicityRange> getRanges() 
        {
            return this.getMultiplicity().getRanges();
        }
        
        public void setRanges() 
        {
            super.setMultiplicity(this.getMultiplicity());
        }
        
        public void removeRange(IMultiplicityRange range) 
        {
            this.getMultiplicity().removeRange(range);
            //super.setMultiplicity(multiplicity);
        }
        
        public IMultiplicityRange createRange() 
        {
            return this.getMultiplicity().createRange();
	}
        
        public void addRange(IMultiplicityRange range) {
            this.getMultiplicity().addRange(range);
            //super.setMultiplicity(multiplicity);
	}
        
        
}


