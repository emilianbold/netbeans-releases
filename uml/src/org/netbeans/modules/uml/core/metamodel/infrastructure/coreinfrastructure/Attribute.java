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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventState;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.support.umlutils.ETList;



public class Attribute extends StructuralFeature
        implements IAttribute, IParameterableElement, IExpressionListener
{
    private IMultiplicity multiplicity;
    IParameterableElement m_ParameterableAggregate = new ParameterableElement();
    
    public Attribute()
    {
        m_ParameterableAggregate.setAggregator(this);
    }
    
    /**
     * property DerivationRule
     */
    public IExpression getDerivationRule()
    {
        ElementCollector< IExpression > collector =
                new ElementCollector< IExpression >();
        String query = "UML:Attribute.derivationRule/*";
        return collector.retrieveSingleElement(m_Node,query, IExpression.class);
    }
    
    public void setDerivationRule( IExpression value )
    {
        String query = "UML:Attribute.derivationRule/UML:Expression";
        addChild("UML:Attribute.derivationRule",query,value);
    }
    
    /**
     * Retrieves the default expression used for this Attribute initializer.
     */
    public IExpression getDefault()
    {
        ElementCollector< IExpression > collector =
                new ElementCollector< IExpression >();
        String query = "UML:Attribute.default/*";
        IExpression exp = collector.retrieveSingleElement(m_Node,query, IExpression.class);
        if (exp == null)
        {
            // We currently don't have a default expression, so let's create a
            // new Expression and return it.
            EventContextManager manager = new EventContextManager();
            
            // Push the "NoEffectContext", telling the version control
            // mechanism that to ignore this modify if needed. This was
            // necessary, 'cause if the user simply clicked on a compartment
            // that had never had a default set on it AND the class or
            // enclosing package was checked in, the user got a
            //"Do you want to check out" dialog, just for clicking on a compartment.
//			EventDispatcher disp = new EventDispatcher();
//			IEventContext eventContext = manager.getNoEffectContext(
//									     this,
//									     EventDispatchNameKeeper.modifiedName(),
//									     "DefaultAdded",
//									     disp
//									     );
            
            ETPairT < IEventContext, IEventDispatcher > contextInfo = manager.getNoEffectContext(this,
                    EventDispatchNameKeeper.modifiedName(),
                    "DefaultAdded");
            
            IEventDispatcher disp = contextInfo.getParamTwo();
            IEventContext eventContext = contextInfo.getParamOne();
            
            EventState state = new EventState(disp,eventContext);
            try
            {
                TypedFactoryRetriever<IExpression> retriever =
                        new TypedFactoryRetriever<IExpression>();
                exp = retriever.createType("Expression");
                // Add the child directly, so we don't cause any new events.
                this.addExpression(exp);
            }
            finally
            {
                state.existState();
            }
            
        }
        return exp;
    }
    
    /**
     * Sets the default expression used for this Attribute initializer.
     *
     * @param exp[in]
     */
    public void setDefault(IExpression exp)
    {
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IClassifierEventDispatcher disp = (IClassifierEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.classifier());
        boolean proceed = true;
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("DefaultPreModified");
            proceed = disp.fireDefaultPreModified(this, exp, payload);
        }
        
        if (proceed)
        {
            addExpression(exp);
            if (disp != null)
            {
                IEventPayload payload = disp.createPayload("DefaultModified");
                disp.fireDefaultModified(this, payload);
            }
        }
        else
        {
            //cancel the event
        }
    }
    
    /**
     * property IsDerived
     */
    public boolean getIsDerived()
    {
        return getBooleanAttributeValue("isDerived",false);
    }
    /**
     * Sets the default expression used for this Attribute initializer.
     */
    public void setIsDerived(boolean newValue)
    {
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IClassifierEventDispatcher disp = (IClassifierEventDispatcher)
        ret.getDispatcher(EventDispatchNameKeeper.classifier());
        boolean proceed = true;
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("DefaultPreDerivedModified");
            proceed = disp.firePreDerivedModified(this, newValue, payload);
        }
        
        if (proceed)
        {
            super.setBooleanAttributeValue("isDerived",newValue);
            if (disp != null)
            {
                IEventPayload payload = disp.createPayload("DerivedModified");
                disp.fireDerivedModified(this, payload);
            }
        }
        else
        {
            //cancel the event
        }
    }
    
    /**
     * property AssociationEnd
     */
    public IAssociationEnd getAssociationEnd()
    {
        ElementCollector<IAssociationEnd> collector =
                new ElementCollector<IAssociationEnd>();
        return collector.retrieveSingleElementWithAttrID(this,"associationEnd", IAssociationEnd.class);
    }
    
    public void setAssociationEnd(IAssociationEnd end)
    {
        final IAssociationEnd assocEnd = end;
        new ElementConnector<IAttribute>().setSingleElementAndConnect
                (
                this, assocEnd,
                "associationEnd",
                new IBackPointer<IAssociationEnd>()
        {
            public void execute(IAssociationEnd obj)
            {
                obj.addQualifier(Attribute.this);
            }
        },
                new IBackPointer<IAssociationEnd>()
        {
            public void execute(IAssociationEnd obj)
            {
                obj.removeQualifier(Attribute.this);
            }
        }
        );
    }
    /**
     * Retrieves the Body property of the Expression that makes up this
     * Attributes default intializer.
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
     * Sets the body property of the default expression. If the expression
     * has not been established yet, it is created.
     *
     * @param newVal[in] The new value
     */
    public void setDefault2( String value )
    {
        IExpression exp = getDefault();
        boolean established = false;
        if (exp == null)
        {
            exp = establishDefault();
            established = true;
        }
        if (exp != null)
        {
            exp.setBody(value);
            if (established)
            {
                setDefault(exp);
            }
        }
    }
    
    
    /**
     * Retrieves the body and language properties of the default expression.
     *
     */
    public ETPairT<String,String> getDefault3()
    {
        String lang = null;
        String body = null;
        IExpression exp = getDefault();
        if (exp != null)
        {
            lang = exp.getLanguage();
            body = exp.getBody();
        }
        return new ETPairT<String, String>(lang, body);
    }
    
    /**
     * Sets the properties of this Attribute's default expression, making up the
     * initializer. If the Expression hasn't been established, it will be after this
     * call.
     */
    public void setDefault3( String lang, String body )
    {
        IExpression exp = getDefault();
        boolean established = false;
        if (exp == null)
        {
            exp = establishDefault();
            established = true;
        }
        if (exp != null)
        {
            exp.setLanguage(lang);
            exp.setBody(body);
            if (established)
            {
                setDefault(exp);
            }
        }
    }
    /**
     * Creates an expression to be used as the default property on this Attribute.
     */
    private IExpression establishDefault()
    {
        TypedFactoryRetriever<IExpression> ret =
                new TypedFactoryRetriever<IExpression>();
        return ret.createType("Expression");
    }
    
    /**
     * Simply adds the passed in Expression to this element.
     *
     * @param exp[in] The Expression to add
     */
    public void addExpression(IExpression exp)
    {
        String query = "UML:Attribute.default/UML:Expression";
        super.addChild("UML:Attribute.default", query, exp);
    }
    
    /**
     * Determines whether or not this attribute is modified with the VB "WithEvents"
     * construct
     */
    public boolean getIsWithEvents()
    {
        return super.getBooleanAttributeValue("isWithEvents",false);
    }
    
    public void  setIsWithEvents(boolean newVal)
    {
        super.setBooleanAttributeValue("isWithEvents",newVal);
    }
    
    /**
     * Indicates whether or not the attribute instance is created on the heap or not upon the
     * instanciation of the featuring classifier.
     */
    public boolean getHeapBased()
    {
        return super.getBooleanAttributeValue("heapBased",false);
    }
    
    public void  setHeapBased(boolean newVal)
    {
        super.setBooleanAttributeValue("heapBased",newVal);
    }
    
    /**
     * Indicates whether or not this attribute maps to a primary key column in a
     * database
     */
    public boolean getIsPrimaryKey()
    {
        return super.getBooleanAttributeValue("isPrimaryKey",false);
    }
    
    public void setIsPrimaryKey(boolean newValue)
    {
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IClassifierEventDispatcher disp = (IClassifierEventDispatcher)
        ret.getDispatcher(EventDispatchNameKeeper.classifier());
        boolean proceed = true;
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("DefaultPrePrimaryKeyModified");
            proceed = disp.firePrePrimaryKeyModified(this, newValue, payload);
        }
        
        if (proceed)
        {
            super.setBooleanAttributeValue("isPrimaryKey",newValue);
            if (disp != null)
            {
                IEventPayload payload = disp.createPayload("PrimaryKeyModified");
                disp.firePrimaryKeyModified(this, payload);
            }
        }
        else
        {
            //cancel the event
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
        super.buildNodePresence("UML:Attribute",doc,parent);
    }
    
    //IParameterableElement methods
    public IParameterableElement getDefaultElement()
    {
        if (m_ParameterableAggregate == null)
        {
            m_ParameterableAggregate = new ParameterableElement();
        }
        return m_ParameterableAggregate.getDefaultElement();
    }
    
    public void setDefaultElement( IParameterableElement element )
    {
        if (m_ParameterableAggregate == null)
        {
            m_ParameterableAggregate = new ParameterableElement();
        }
        m_ParameterableAggregate.setDefaultElement(element);
    }
    
    public void setDefaultElement2( String newVal )
    {
        if (m_ParameterableAggregate == null)
        {
            m_ParameterableAggregate = new ParameterableElement();
        }
        m_ParameterableAggregate.setDefaultElement2(newVal);
    }
    
    public IClassifier getTemplate()
    {
        if (m_ParameterableAggregate == null)
        {
            m_ParameterableAggregate = new ParameterableElement();
        }
        return m_ParameterableAggregate.getTemplate();
    }
    
    public void setTemplate( IClassifier value )
    {
        if (m_ParameterableAggregate == null)
        {
            m_ParameterableAggregate = new ParameterableElement();
        }
        m_ParameterableAggregate.setTemplate(value);
    }
    
    public String getTypeConstraint()
    {
        if (m_ParameterableAggregate == null)
        {
            m_ParameterableAggregate = new ParameterableElement();
        }
        return m_ParameterableAggregate.getTypeConstraint();
    }
    
    public void setTypeConstraint( String value )
    {
        if (m_ParameterableAggregate == null)
        {
            m_ParameterableAggregate = new ParameterableElement();
        }
        m_ParameterableAggregate.setTypeConstraint(value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onPreBodyModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, java.lang.String)
     */
    public boolean onPreBodyModified(IExpression exp, String proposedValue)
    {
        boolean proceed = true;
        
        IAttribute attr = (IAttribute) getAggregator();
        
        IClassifierEventDispatcher disp = (IClassifierEventDispatcher)
        EventDispatchRetriever.instance().getDispatcher(
                EventDispatchNameKeeper.classifier());
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("PreDefaultBodyModified");
            proceed = disp.firePreDefaultBodyModified(attr, proposedValue, payload);
        }
        return proceed;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onBodyModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
     */
    public void onBodyModified(IExpression exp)
    {
        IAttribute attr = (IAttribute) getAggregator();
        
        IClassifierEventDispatcher disp = (IClassifierEventDispatcher)
        EventDispatchRetriever.instance().getDispatcher(
                EventDispatchNameKeeper.classifier());
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("DefaultBodyModified");
            disp.fireDefaultBodyModified(attr, payload);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onPreLanguageModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, java.lang.String)
     */
    public boolean onPreLanguageModified(IExpression exp, String proposedValue)
    {
        boolean proceed = true;
        
        IAttribute attr = (IAttribute) getAggregator();
        
        IClassifierEventDispatcher disp = (IClassifierEventDispatcher)
        EventDispatchRetriever.instance().getDispatcher(
                EventDispatchNameKeeper.classifier());
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("PreDefaultLanguageModified");
            proceed = disp.firePreDefaultLanguageModified(attr, proposedValue, payload);
        }
        return proceed;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onLanguageModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
     */
    public void onLanguageModified(IExpression exp)
    {
        IAttribute attr = (IAttribute) getAggregator();
        
        IClassifierEventDispatcher disp = (IClassifierEventDispatcher)
        EventDispatchRetriever.instance().getDispatcher(
                EventDispatchNameKeeper.classifier());
        if (disp != null)
        {
            IEventPayload payload = disp.createPayload("DefaultLanguageModified");
            disp.fireDefaultLanguageModified(attr, payload);
        }
    }
    
    public IMultiplicity getMultiplicity()
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
    }
    
    public IMultiplicityRange createRange()
    {
        return this.getMultiplicity().createRange();
    }
    
    public void addRange(IMultiplicityRange range)
    {
        this.getMultiplicity().addRange(range);
        onRangeAdded(this.getMultiplicity(), range);
    }
}

