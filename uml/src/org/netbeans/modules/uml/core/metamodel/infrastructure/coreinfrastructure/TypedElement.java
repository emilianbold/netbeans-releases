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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventState;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITransitionElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 * @author aztec
 *
 */
public class TypedElement extends Element implements ITypedElement
{
	//ordering Kind
	private static int OK_UNORDERED	= 0;
	private static int OK_ORDERED	= 1;
	
	public TypedElement()
	{		
		super();
	}
	
	public void setType(IClassifier newClassifier)
	{
		boolean isSame = false;
		ITypedElement curObj = this;
		if (!(getAggregator() instanceof ITransitionElement))
		{
			// If this TypedElement is currently a transition
			// element, then we don't want to check to see
			// if the types are the same, 'cause we
			// know this is a new type, so we need to set it
			IClassifier curType = getType();
			if (newClassifier != null && curType != null)
			{
				isSame = curType.isSame(newClassifier);			
			}
		}
		// Only set if the types are different
		if (!isSame)
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp =
						(IClassifierEventDispatcher) ret.getDispatcher(
									EventDispatchNameKeeper.classifier());

			boolean proceed = true;
			IEventPayload payload = null;
			if( disp != null )
			{
			   payload = disp.createPayload("PreTypeModified");	   
			   proceed = disp.firePreTypeModified((ITypedElement) getAggregator(), newClassifier,payload);
                           if (proceed && EventDispatchRetriever.isFirstRequest()) {
                               payload = disp.createPayload("PreTypeModified");	   
                               proceed = disp.firePreTypeModified((ITypedElement) getAggregator(), newClassifier,payload);
                           }
			}
			if (proceed)
			{
				super.setElement(newClassifier,"type");
				if( disp != null )
				{
				   payload = disp.createPayload("TypeModified");	   
				   disp.fireTypeModified((ITypedElement) getAggregator(),payload);
				}
			}
			else
			{
				//throw exception		
			}		
		}
	}
	
	public IClassifier getType()
	{		 
		ElementCollector<IClassifier> collector =
            new ElementCollector<IClassifier>();
        IClassifier classifier =
            collector.retrieveSingleElementWithAttrID(this, "type", IClassifier.class);
		if (classifier == null)
		{
			String id = null;
			org.dom4j.Element node = getElementNode();
			id = UMLXMLManip.getAttributeValue(node,"type");
			if (id != null && id.length() > 0)
			{
				if (getAggregator() instanceof ITransitionElement)
				{
                    Document ownerDoc = getDocument((ITypedElement) getAggregator());
							if (ownerDoc != null)
							{
								Object obj = UMLXMLManip.findAndFill(ownerDoc,id);
								if (obj != null && obj instanceof IClassifier)
								{
									classifier = (Classifier)obj;
								}
							}
//					ITransitionElement transElement = (ITransitionElement) aggregator;
//					IElement futureOwner = transElement.getFutureOwner();
//					if (futureOwner != null)
//					{
//						// We are a transition element, so what we need to do
//						// is use the document that our future owner is already
//						// associated with to retrieve the IClassifier that is in
//						// the "type" attribute of this transition element.
//						Node ownerNode = futureOwner.getNode();
//						if (ownerNode != null)
//						{
//							Document ownerDoc = ownerNode.getDocument(); 
//							if (ownerDoc != null)
//							{
//								Object obj = UMLXMLManip.findAndFill(ownerDoc,id);
//								if (obj != null && obj instanceof IClassifier)
//								{
//									classifier = (Classifier)obj;
//								}
//							}
//						}
//					}
				}				 
			}
		}
		return classifier;	
	}
	
	public int getOrdering( )
	{
		IMultiplicity multiplicity = getMultiplicity();
		int retVal = 0;
		if (multiplicity != null)
		{
			boolean isOrdered = multiplicity.getIsOrdered();
			retVal = isOrdered? OK_ORDERED : OK_UNORDERED;			
		}	   	
	    return retVal;
	}
	
	public void setOrdering(int newVal)
	{
		IMultiplicity multiplicity = getMultiplicity();
		int retVal = 0;
		if (multiplicity != null)
		{
			boolean isOrdered = (newVal == OK_ORDERED)? true : false;
			multiplicity.setIsOrdered(isOrdered);
		}		   
	}

	public IMultiplicity getMultiplicity()
	{		
		ElementCollector<IMultiplicity> collector = 
									  new ElementCollector<IMultiplicity>();
		IMultiplicity mult = collector.retrieveSingleElement(
								    m_Node,
									"UML:TypedElement.multiplicity/UML:Multiplicity", IMultiplicity.class);
		if (mult == null)
		{
//			IEventDispatcher disp = new EventDispatcher();
			EventContextManager manager = new EventContextManager();
//			IEventContext context = manager.getNoEffectContext(
//											this,
//											EventDispatchNameKeeper.modifiedName(),
//											"DefaultMultiplicityAdded",disp );
         
         ETPairT < IEventContext, IEventDispatcher > contextInfo = manager.getNoEffectContext(this,
                                                                                              EventDispatchNameKeeper.modifiedName(),
                                                                                              "DefaultMultiplicityAdded");
            
         IEventDispatcher disp = contextInfo.getParamTwo();
         IEventContext context = contextInfo.getParamOne();
         
			EventState state = new EventState(disp,context);
         try
         {
            TypedFactoryRetriever<IMultiplicity> typedFact = new 
                                    TypedFactoryRetriever<IMultiplicity>();
            mult = typedFact.createType("Multiplicity");
            // Add the child directly, so we don't cause any new events
            addMultiplicity(mult);		
         }
         finally
         {
            state.existState();
         }
		}
		return mult;
	}

	public void setMultiplicity(  IMultiplicity  newVal)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("PreMultiplicityModified");	   
		   proceed = disp.firePreMultiplicityModified((ITypedElement) getAggregator(),newVal,payload);
		}
		if (proceed)
		{
			addMultiplicity(newVal);
			if( disp != null )
			{
			   payload = disp.createPayload("MultiplicityModified");	   
			   disp.fireMultiplicityModified((ITypedElement) getAggregator(),payload);
			}
		}
		else
		{
			//throw exception		
		}				
	}
	
	public void performDuplicationProcess( ITypedElement dupType )
	{
		IMultiplicity mult = dupType.getMultiplicity();
		if (mult != null)
		{
			IVersionableElement ver = mult.duplicate();
			if (ver != null && ver instanceof IMultiplicity)
			{
				IMultiplicity dupMult = (IMultiplicity)ver;
				if (dupMult != null)
				{
					dupType.setMultiplicity(dupMult);
				}
			}
		}
	}
	
	public IVersionableElement performDuplication()
	{
		IVersionableElement dupEle = super.performDuplication();
		ITypedElement dupType = null;
		if (dupEle != null && dupEle instanceof ITypedElement)		
		 	dupType = (ITypedElement)dupEle;
		if (dupType != null)
		{
			performDuplicationProcess(dupType);	
		}
		return dupEle;
	}
        
	public String processProposedType(String newType)
	{
		String type = newType;
		if (type != null && type.length() == 0)
		{
			// Determine whether or not the parameter already has a type.
			// If it does, we are making this Parameter not have a type,
			// which is fine.
			IClassifier curType = getType();
			if (curType != null)
			{
				setType(null);
			}
			else
			{
				type = "int";
			}
		}
		return type;
	}
	
	public void setIsSet(boolean newVal)
	{		
		super.setBooleanAttributeValue("isSet",newVal);
	}
	
	public boolean getIsSet()
	{
		return super.getBooleanAttributeValue("isSet",false);
	}
        
	public String getTypeID()
	{
		org.dom4j.Element node = getElementNode();
		String typeID = null;
		if (node != null)
		{
			typeID = UMLXMLManip.getAttributeValue(node,"type"); 
		}
		return typeID;
	}
    
	private void addMultiplicity(IMultiplicity mult)
	{
        if (mult == null || m_Node == null) return;
        
        // Check to see if we already have a Multiplcity element on this element.
        // If we do, we need to Delete it, and add the new one. We can't just call
        // get_Multiplicity() here as it will create one if it doesn't exist
        
        String query = "UML:TypedElement.multiplicity/UML:Multiplicity";
        Node existingNode = XMLManip.selectSingleNode(m_Node, query);
        if (existingNode != null)
            existingNode.detach();

		addChild("UML:TypedElement.multiplicity",
					   "UML:TypedElement.multiplicity",
					   mult);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
	 */
	public boolean onPreLowerModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
	{
		boolean proceed = true;

		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
			
			if( disp != null)
			{
				IEventPayload  payload =  disp.createPayload("PreLowerModified");
				proceed =  disp.firePreLowerModified( (ITypedElement)getAggregator(), mult, range, proposedValue, payload);
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
		return proceed;
	}
        
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onLowerModified(IMultiplicity mult, IMultiplicityRange range) 
	{
		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
			if( disp != null )
			{
				IEventPayload  payload =  disp.createPayload( "LowerModified");
				disp.fireLowerModified( (ITypedElement)getAggregator(), mult, range, payload );
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
	}
        

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
	 */
	public boolean onPreUpperModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
	{
		boolean proceed = true;
		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
			if( disp != null)
			{
				IEventPayload payload = disp.createPayload( "PreUpperModified");
				proceed = disp.firePreUpperModified( (ITypedElement)getAggregator(), mult, range, proposedValue, payload);
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onUpperModified(IMultiplicity mult, IMultiplicityRange range) 
	{
		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
			
			if( disp != null )
			{
				IEventPayload payload = disp.createPayload("UpperModified");
				disp.fireUpperModified( (ITypedElement)getAggregator(), mult, range, payload );
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public boolean onPreRangeAdded(IMultiplicity mult, IMultiplicityRange range) 
	{
		boolean proceed = true;
		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
			
			if( disp != null)
			{
				IEventPayload  payload = disp.createPayload( "PreRangeAdded" );
				proceed= disp.firePreRangeAdded( (ITypedElement)getAggregator(), mult, range, payload);
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onRangeAdded(IMultiplicity mult, IMultiplicityRange range)
	{
		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
			
			if( disp != null )
			{
				IEventPayload  payload = disp.createPayload( "RangeAdded");
				disp.fireRangeAdded( (ITypedElement)getAggregator(), mult, range, payload );
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public boolean onPreRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
	{
		boolean proceed = true;
		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
			
			if( disp != null )
			{
				IEventPayload  payload = disp.createPayload("PreRangeRemoved");
				proceed = disp.firePreRangeRemoved( (ITypedElement)getAggregator(), mult, range, payload);
			}
		}
		catch( Exception e )
		{
			 Log.stackTrace(e);
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
	{
		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
			
			if( disp != null )
			{
				IEventPayload  payload =  disp.createPayload( "RangeRemoved");
				disp.fireRangeRemoved( (ITypedElement)getAggregator(), mult, range, payload);
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean)
	 */
	public boolean onPreOrderModified(IMultiplicity mult, boolean proposedValue) 
	{
		boolean proceed = true;
		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());

			if( disp != null )
			{
				IEventPayload  payload = disp.createPayload("PreOrderModified");
				proceed = disp.firePreOrderModified( (ITypedElement)getAggregator(), mult, proposedValue, payload);
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}	
		return proceed;
	}	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity)
	 */
	public void onOrderModified(IMultiplicity mult)
	{
		try
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
			if( disp != null )
			{
				IEventPayload  payload = disp.createPayload( "OrderModified");
				disp.fireOrderModified( (ITypedElement)getAggregator(), mult, payload );
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
	}
        
        public void onCollectionTypeModified(IMultiplicity mult, IMultiplicityRange range)
        {
            try
            {
                EventDispatchRetriever ret = EventDispatchRetriever.instance();
                IClassifierEventDispatcher disp = ret.getDispatcher(EventDispatchNameKeeper.classifier());
                if( disp != null )
                {
                    IEventPayload  payload =  disp.createPayload( "CollectionType");
                    disp.fireCollectionTypeModified( (ITypedElement)getAggregator(), mult, range, payload );
                }
            }
            catch( Exception e )
            {
                Log.stackTrace(e);
            }
        }
}


