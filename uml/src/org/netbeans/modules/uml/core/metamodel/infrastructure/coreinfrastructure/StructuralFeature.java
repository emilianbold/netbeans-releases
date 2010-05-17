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

import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITransitionElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

import org.dom4j.Element;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class StructuralFeature extends Feature implements IStructuralFeature, ITypedElement
{
    /**
     * Aggregate that'll be the delegate for ITypedElement methods. We override
     * getNode() to return 
     */
	private ITypedElement m_TypedElementAggregate = new TypedElement();

	public StructuralFeature()
	{
		m_TypedElementAggregate.setAggregator(this);
	}
    
    /**
     * Sets the XML node which contains this element's data.
     */
    public void setNode(Node newNode)
    {
        super.setNode(newNode);
        
        // Delegate to our delegates (!) so that both our methods and our
        // delegate's methods refer to the same element.
		m_TypedElementAggregate.setNode(newNode);
    }
    
	public int getClientChangeability()
	{
		return getChangeableKindValue( "clientChangeability" );	
	}
	
	/**	 
	 * Sets the Changeability flag. Results in the firing of the 
	 * PreChangeabilityModified and ChangeabilityModified events.
	 *
	 * @param newVal[in] The new value
	 */
	public void setClientChangeability(int newVal)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
								
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = null;		
			if( disp != null )
			{
			   payload = disp.createPayload("PreChangeabilityModified");	   
			   proceed = disp.firePreChangeabilityModified(this,newVal,payload);
			}
			if (proceed)
			{
				setChangeableKindValue("clientChangeability",newVal);
				if (disp != null)
				{
					payload = disp.createPayload("ChangeabilityModified");
					disp.fireChangeabilityModified(this,payload);
				}
				else
				{
					//Throw exception.
				}
			}			
		}
	}
	public IClassifier getType()
	{
		return m_TypedElementAggregate.getType();
		
	}
	public void setType(IClassifier newClassifier)
	{	
		//m_TypedElementAggregate.setType(newClassifier);		
		
		//here I cannot just pass on the request to TypedElement, because in doing so, the element
		//becomes TypedElement and then its no longer an instanceof IFeature - because of which the
		//Notification is not sent to the drawing area to update the right presentation elements.
		
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
			   payload = disp.createPayload("PreTypeModified"); //NOI18N
			   proceed = disp.firePreTypeModified((ITypedElement) getAggregator(), newClassifier,payload);
                           if (proceed && EventDispatchRetriever.isFirstRequest()) {
                               payload = payload = disp.createPayload("PreTypeModified"); //NOI18N
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
	public int getOrdering()
	{
	   return m_TypedElementAggregate.getOrdering();
	}
	
	public void setOrdering(int newVal)
	{
		m_TypedElementAggregate.setOrdering( newVal );
	}

	public IMultiplicity getMultiplicity()
	{
	   return m_TypedElementAggregate.getMultiplicity();
	}

	public void setMultiplicity(IMultiplicity  newVal)
	{
		m_TypedElementAggregate.setMultiplicity( newVal );
	}
	
	/**
         * Adds the type to this feature via a more convenient
         * string, which will resolve the string into the appropriate
         * Classifier.
         *
         * @param newType[in] The name of the type
         */
        public void setType2(String newType)
        {
            boolean makeSureToCreateType = false;
            
            ETList < ILanguage > languages = getLanguages();
            for(ILanguage language : languages)
            {
                makeSureToCreateType = language.isDataType(newType);
            }
            
            //spaces in type name cause lot of processing power and problems, normalize it
            newType = Util.stripSpacesInString(newType);
            String type = processProposedType( newType );
            if (type != null && type.length() > 0)
            {
                
                // IZ 80953 - When creating a attribute with the default type, we
                // need to make sure that the UnknowClassifierCreate is not set to
                // "NO".  If the preference is set to "No" then the operation will
                // not be created.  Since we get the return type from the language
                // datatypes, we should assume that it should be present in the
                // system.  After we retrieve the type, we should make sure that
                // the preference is set to the original state.
               
                
                INamedElement element = resolveSingleTypeFromString(type);

                if (element != null && element instanceof IClassifier)
                {
                    IClassifier classifier = (IClassifier)element;
                    setType(classifier);
                }
            }
        }
	
	/**
	 *
	 * Makes sure that new features are named according to the default
	 *
	 * @param node[in] The new node
	 */
	public void establishNodeAttributes(Element node) 
	{
		super.establishNodeAttributes(node);
		XMLManip.setAttributeValue(node,"visibility","private");
		establishDefaultName();	
	}
	
	/**
	 * Retrieves the name of the typing Classifier.
	 */
	public String getTypeName()
	{
		String retName = null;
      
		IClassifier type = getType();
		if (type != null)
      {
         retName = type.getName();
      }
      
		return (retName != null) ? retName : "";
	}
	
	/**
	 *
	 * Passes through to put_Type2
	 */
	public void setTypeName(String newVal)
	{
		setType2(newVal);	
	}
	/**
	 * The volatility state of this feature.
	 */
	public boolean getIsVolatile()
	{
		return getBooleanAttributeValue("isVolatile",false);
	}
	/**
	 * The volatility state of this feature.
	 */
	public void setIsVolatile(boolean newVal)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
								
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = null;		
			if( disp != null )
			{
			   payload = disp.createPayload("PreVolatileModified");	   
			   proceed = disp.firePreVolatileModified(this,newVal,payload);
			}
			if (proceed)
			{
				setBooleanAttributeValue("isVolatile",newVal);
				if (disp != null)
				{
					payload = disp.createPayload("VolatileModified");
					disp.fireVolatileModified(this,payload);
				}
				else
				{
					//Throw exception.
				}
			}			
		}			
	}
	
	public boolean getIsTransient()
	{
		return getBooleanAttributeValue("isTransient",false);
	}
	
	/**
	 * Determines whether or not this feature persists or not.
	 *
	 * @param newVal[in]
	 */
	public void setIsTransient(boolean newVal)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
								
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = null;		
			if( disp != null )
			{
			   payload = disp.createPayload("PreTransientModified");	   
			   proceed = disp.firePreTransientModified(this,newVal,payload);
			}
			if (proceed)
			{
				setBooleanAttributeValue("isTransient",newVal);
				if (disp != null)
				{
					payload = disp.createPayload("TransientModified");
					disp.fireTransientModified(this,payload);
				}
				else
				{
					//Throw exception.
				}
			}
		}
	}
	
	public IVersionableElement performDuplication()
	{
		IVersionableElement verEle = super.performDuplication();
		((TypedElement)m_TypedElementAggregate).performDuplicationProcess((ITypedElement) verEle);
		return verEle;
	}
	
	public void performDependentElementCleanUp(IVersionableElement thisElement)
	{
		performDependentElementCleanup(thisElement);
	}
	
	public void setIsSet( boolean value )
	{
		m_TypedElementAggregate.setIsSet(value);		
	}
	public boolean getIsSet()
	{
		return m_TypedElementAggregate.getIsSet();		
	}
	public String getTypeID()
	{
		return m_TypedElementAggregate.getTypeID();		
	}
	public String processProposedType(String newType)
	{
            return ((TypedElement)m_TypedElementAggregate).processProposedType(newType);		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
	 */
	public boolean onPreLowerModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
	{
		return m_TypedElementAggregate.onPreLowerModified(mult, range, proposedValue);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onLowerModified(IMultiplicity mult, IMultiplicityRange range) 
	{
		m_TypedElementAggregate.onLowerModified(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
	 */
	public boolean onPreUpperModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
	{
		return m_TypedElementAggregate.onPreUpperModified(mult, range, proposedValue);	
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onUpperModified(IMultiplicity mult, IMultiplicityRange range) 
	{
		m_TypedElementAggregate.onUpperModified(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public boolean onPreRangeAdded(IMultiplicity mult, IMultiplicityRange range) 
	{
		return m_TypedElementAggregate.onPreRangeAdded(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onRangeAdded(IMultiplicity mult, IMultiplicityRange range) 
	{
		m_TypedElementAggregate.onRangeAdded(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public boolean onPreRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
	{
		return m_TypedElementAggregate.onPreRangeRemoved(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	 */
	public void onRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
	{
		m_TypedElementAggregate.onRangeRemoved(mult, range);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean)
	 */
	public boolean onPreOrderModified(IMultiplicity mult, boolean proposedValue)
	{
		return m_TypedElementAggregate.onPreOrderModified(mult, proposedValue);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity)
	 */
	public void onOrderModified(IMultiplicity mult) 
	{
		m_TypedElementAggregate.onOrderModified(mult);
	}
        
    public void onCollectionTypeModified(IMultiplicity mult, IMultiplicityRange range)
    {
        m_TypedElementAggregate.onCollectionTypeModified(mult, range);
    }

    public String getRangeAsString()
    {
        return getMultiplicity().getRangeAsString();
    }
}


