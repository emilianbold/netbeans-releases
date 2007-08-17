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

import org.netbeans.modules.uml.common.Util;
import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementChangeDispatchHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeDispatchHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITransitionElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Parameter extends NamedElement implements 
        IParameter, ITypedElement
{
	private TypedElement m_TypedElementAggregate = new TypedElement();

    public Parameter()
    {
        m_TypedElementAggregate.setAggregator(this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node node)
    {
        super.setNode(node);
        m_TypedElementAggregate.setNode(node);
    }
    	
    /**
     * Sets / Gets the direction flag on the parameter, indicating the semantics
     *  of how that data the parameter represents is entering the behavior.
     */
    public int getDirection()
    {
            return super.getParameterDirectionKindValue("direction");
    }

    /**
     * Sets / Gets the direction flag on the parameter, indicating the semantics 
     * of how that data the parameter represents is entering the behavior.
     */
    public void setDirection( /* ParameterDirectionKind */ int kind )
    {
            EventDispatchRetriever ret = EventDispatchRetriever.instance();
            IClassifierEventDispatcher disp =
                                    (IClassifierEventDispatcher) ret.getDispatcher(
                                                            EventDispatchNameKeeper.classifier());

            boolean proceed = true;
            IEventPayload payload = null;
            if( disp != null )
            {
               payload = disp.createPayload("PreDirectionModified");	   
               proceed = disp.firePreDirectionModified(this,kind,payload);
            }
            if (proceed)
            {
                    super.setParameterDirectionKindValue("direction",kind);
                    if( disp != null )
                    {
                       payload = disp.createPayload("DirectionModified");	   
                       disp.fireDefaultExpModified(this,payload);
                    }
            }
            else
            {
                    //throw exception		
            }		
    }

    /**
     * Sets / Gets the expression that holds the default initialization for the 
     * parameter.
     */
    public IExpression getDefault()
    {
            ElementCollector<IExpression> collector = 
                                                                      new ElementCollector<IExpression>();
            return collector.retrieveSingleElement(m_Node,"UML:Parameter.default/UML:Expression", IExpression.class);	
    }

    /**
     * Sets the default expression for this parameter. Results in the firing
     * of the PreDefaultExpModified and DefaultExpModified events.
     *
     * @param exp[in] The new expresion
     */
    public void setDefault( IExpression exp )
    {
            EventDispatchRetriever ret = EventDispatchRetriever.instance();
            IClassifierEventDispatcher disp =
                                    (IClassifierEventDispatcher) ret.getDispatcher(
                                                            EventDispatchNameKeeper.classifier());

            boolean proceed = true;
            IEventPayload payload = null;
            if( disp != null )
            {
               payload = disp.createPayload("PreDefaultExpModified");	   
               proceed = disp.firePreDefaultExpModified(this,exp,payload);
            }
            if (proceed)
            {
                    super.addChild("UML:Parameter.default","UML:Parameter.default/UML:Expression",exp);
                    if( disp != null )
                    {
                       payload = disp.createPayload("DefaultExpModified");	   
                       disp.fireDefaultExpModified(this,payload);
                    }
            }
            else
            {
                    //throw exception		
            }
    }

    /**
     * Sets / Gets the name of the Parameter.
     */
    public String getName()
    {
            return super.getName();
    }

    /**
     * Sets / Gets the name of the Parameter.
     */
    public void setName( String value )
    {
            super.setName(value);
    }

    /**
     * Adds the type to this feature via a more convenient 
     * string, which will resolve the string into the appropriate
     * Classifier.
     *
     * @param newType[in] The name of the type
     */
    public void setType2( String newType )
    {
        boolean makeSureToCreateType = false;

        ETList < ILanguage > languages = getLanguages();
        for(ILanguage language : languages)
        {
            makeSureToCreateType = language.isDataType(newType);
        }

        //spaces in type name cause lot of processing power and problems, normalize it
        newType = Util.stripSpacesInString(newType);
        String type = processProposedType(newType);
        if (type != null && type.length() > 0)
        {
            // IZ 80953 - When creating a attribute with the default type, we
            // need to make sure that the UnknowClassifierCreate is not set to
            // "NO".  If the preference is set to "No" then the operation will
            // not be created.  Since we get the return type from the language
            // datatypes, we should assume that it should be present in the
            // system.  After we retrieve the type, we should make sure that
            // the preference is set to the original state.


            INamedElement element = super.resolveSingleTypeFromString(type);


            if (element != null)
            {
                IClassifier classifier = element instanceof IClassifier? (IClassifier) element : null;
                if (classifier != null)
                {
                    setType(classifier);
                }
            }
        }
    }

    /**
     * Retrieves the BehavioralFeature this parameter is a part of.
     */
    public IBehavioralFeature getBehavioralFeature()
    {
            Object obj = retrieveParentNode();
            IBehavioralFeature feature = null;
            if (obj != null)
            {
                    feature = obj instanceof IBehavioralFeature? 
                                                                    (IBehavioralFeature)obj : null;
            }
            else
            {
                    IParameter curObj = this;
                    ITransitionElement transElement = curObj instanceof ITransitionElement? (ITransitionElement) curObj : null;
                    if (transElement != null)
                    {
                            IElement futureOwner = transElement.getFutureOwner();
                            feature = (IBehavioralFeature) futureOwner;
                    }
            }
            return feature;		
    }

    /**
     * Retrieves the Behavior this parameter is a part of.
 */
    public IBehavior getBehavior()
    {
            Object obj = retrieveParentNode();
            IBehavior feature = null;
            if (obj != null)
            {
                    feature = (IBehavior)obj;
            }		
            return feature;
    }

    /**
     * The name of the Classifier who specifies this Parameter's type.
     */
    public String getTypeName()
    {
            String retName = "";
            IClassifier type = getType();
            if (type != null)
                    retName = type.getName();

            return retName;
    }

    /**
     * The name of the Classifier who specifies this Parameter's type.
     */
    public void setTypeName( String value )
    {
            setType2(value);
    }

    /**
     * The default parameter initializer. Easy access to the body property of the
     *  Expression.
     */
    public String getDefault2()
    {
            String retVal = "";
            IExpression exp = getDefault();
            if (exp != null)
                    retVal = exp.getBody();

            return retVal;	
    }

    /**
     * The default parameter initializer. Easy access to the body property of the
     *  Expression.
     */
    public void setDefault2( String value )
    {
            IExpression exp = getDefault();
            if (exp == null && value != null && value.length() >0)
            {
                    exp = establishDefault();
        setDefault(exp);
            }
            if (exp != null)
            {
                    exp.setBody(value);
            }
    }

    /**
     * The default parameter initializer. Easy access to the body property of the
     * Expression.
     * NOTE: Sending both languauge and body with comma seperation between them.
     * The caller of this method need to parse the single string to get the values
     */
    public String getDefault3()
    {
            StringBuffer values = new StringBuffer();
            IExpression exp = getDefault();
            if (exp != null)
            {
                    values.append(exp.getLanguage());
                    values.append(",");
                    values.append(exp.getBody());
            }
            return values.toString();
    }

    /**
     * The default parameter initializer. Easy access to the body property of the 
     * Expression.
     */
    public void setDefault3( String lang, String body )
    {
            IExpression exp = getDefault();
            if (exp == null)
            {
                    exp = establishDefault();
        setDefault(exp);
            }
            if (exp != null)
            {
                    exp.setLanguage(lang);
                    exp.setBody(body);
            }
    }

    private IExpression establishDefault()
    {
            TypedFactoryRetriever<IExpression> ret = 
                    new TypedFactoryRetriever<IExpression>();
            return ret.createType("Expression");		
    }
    /**
     * Specifies extra semantics associated with the Parameter.
     */
    public int getParameterKind()
    {
            return super.getParameterSemanticsKind("kind");
    }

    /**
     * Specifies extra semantics associated with the Parameter.
    */
    public void setParameterKind( /* ParameterSemanticsKind */ int value )
    {
            super.setParameterSemanticsKind("kind",value);
    }

    /**
     *
     * Retrieves the object that actually owns this Parameter.
     * This is generally a BehavioralFeature or a Behavior
     */	
    protected Object retrieveParentNode()
    {
            Object obj = null;
            if (m_Node != null)
            {
                    Node parentNode = m_Node.getParent();
                    if (parentNode != null)
                    {
                            Node owningNode = parentNode.getParent();
                            if (owningNode != null)
                            {
                                    FactoryRetriever fact = FactoryRetriever.instance();
                                    if (fact != null)
                                    {
                                            obj = fact.createTypeAndFill(super.retrieveSimpleName(owningNode),
                                                                                       owningNode);											   
                                    }
                            }
                    }
            }
            return obj;
    }

    public IVersionableElement performDuplication()
    {
            IVersionableElement ver = super.performDuplication();
            ITypedElement typedEl = ver instanceof ITypedElement? (ITypedElement) ver : null;
            performDuplicationProcess(typedEl);
            IParameter param = typedEl instanceof IParameter? (IParameter) typedEl : null;
            if (param != null)
            {
                    IExpression exp = getDefault();
                    if (exp != null)
                    {
                            IVersionableElement verr = exp.duplicate();
                            IExpression dupExp = verr instanceof IExpression? (IExpression) verr : null;
                            param.setDefault(dupExp); 
                    }
            }
            return ver;
    }

    /**
    * Fires an event to update the operation that owns this paramter.
    * This method is called when there's any change made to the parameter.
    * @param thisElement[in] The COM object representing this element
    */
    @Override
    public void performDependentElementCleanup(IVersionableElement elem) 
    {
        super.performDependentElementCleanup(elem);
        IElement opElem = getOwner();
        if (opElem != null) 
        {
            IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
            helper.dispatchElementModified(opElem);
        }
    }

    /**
     * Establishes the appropriate XML elements for this UML type.
     *
     * [in] The document where this element will reside
     * [in] The element's parent node.
     */	
    public void establishNodePresence(Document doc, Node parent)
    {
            buildNodePresence("UML:Parameter",doc,parent);
    }


    //TypedElement methods
    public void setType(IClassifier classifier)
    {			
            m_TypedElementAggregate.setType(classifier);	
    }

    public IClassifier getType()
    {
            return m_TypedElementAggregate.getType();
    }
    public int getOrdering( )
    {
       return m_TypedElementAggregate.getOrdering();
    }
    public void setOrdering(int newVal)
    {
            m_TypedElementAggregate.setOrdering(newVal);			
    }

    public IMultiplicity getMultiplicity()
    {
       return m_TypedElementAggregate.getMultiplicity();
    }

    public void setMultiplicity(  IMultiplicity  newVal)
    {
            m_TypedElementAggregate.setMultiplicity(newVal);
    }

    public void performDuplicationProcess( ITypedElement dupType )
    {
            m_TypedElementAggregate.performDuplicationProcess(dupType);
    }
    public String processProposedType(String type)
    {
            return m_TypedElementAggregate.processProposedType(type);
    }
    public void setIsSet(boolean val)
    {		
            m_TypedElementAggregate.setIsSet(val);	
    }
    public boolean getIsSet()
    {
            return  m_TypedElementAggregate.getIsSet();
    }
    public String getTypeID()
    {
            return m_TypedElementAggregate.getTypeID();
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
    
    public String toString()
    {
            return getTypeName() + " " + getName();
    }
        
    
    public boolean isSimilar(INamedElement other) 
    {
        if (!(other instanceof IParameter) || !super.isSimilar(other))
            return false;
        
        IParameter otherParam = (IParameter) other;
        
        if (!getType().isSimilar(otherParam.getType()))
            return false;
        
        return true;
    }
}
