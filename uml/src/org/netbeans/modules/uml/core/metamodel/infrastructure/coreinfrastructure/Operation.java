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
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class Operation extends BehavioralFeature 
    implements IOperation, IParameterableElement
{

	private IParameterableElement m_ParameterableAggregate = null;
	private static final int OPK_PROPERTY = 0;
	private static final int OPK_FRIEND = 1;
	private static final int OPK_SUBROUTINE = 2;
	private static final int OPK_VIRTUAL = 3;
	private static final int OPK_OVERRIDE = 4;
	private static final int OPK_DELEGATE =5;
	private static final int OPK_INDEXER = 6;

	/**
	 * 
	 */
	public Operation()
	{
		super();		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getIsQuery()
	 */
	public boolean getIsQuery()
	{
		return getBooleanAttributeValue("isQuery", false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#setIsQuery(boolean)
	 */
	public void setIsQuery(boolean value)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("PreQueryModified");	   
		   proceed = disp.firePreQueryModified(this,value,payload);
		}
		if (proceed)
		{
			super.setBooleanAttributeValue("isQuery",value);
			if( disp != null )
			{
			   payload = disp.createPayload("QueryModified");	   
			   disp.fireQueryModified(this,payload);
			}
		}
		else
		{
			//throw exception		
		}		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#addPostCondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
	 */
	public void addPostCondition(IConstraint cond)
	{
		addCondition( "UML:Operation.postcondition", 
					  "UML:Operation.postcondition/UML:Constraint", 
					  cond, false); 
	}

	/**
	 * @param string
	 * @param string2
	 * @param cond
	 * @param b
	 */
	private void addCondition(String element, String query, IConstraint constraint, 
							  boolean isPre)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("ConditionPreAdded");	   
		   proceed = disp.fireConditionPreAdded(this,constraint,isPre,payload);
		}
		if (proceed)
		{
			super.addChild(element,query,constraint);
			if( disp != null )
			{
			   payload = disp.createPayload("ConditionAdded");	   
			   disp.fireConditionAdded(this,constraint,isPre,payload);
			}
		}
		else
		{
			//throw exception		
		}		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#removePostCondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
	 */
	public void removePostCondition(IConstraint constraint)
	{
		removeCondition(constraint, false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getPostConditions()
	 */
	public ETList<IConstraint> getPostConditions()
	{
      ElementCollector< IConstraint > col = new ElementCollector< IConstraint >();
	  return col.retrieveElementCollection(m_Node, "UML:Operation.postcondition/*", IConstraint.class);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#addPreCondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
	 */
	public void addPreCondition(IConstraint cond)
	{
		addCondition( "UML:Operation.precondition", 
					  "UML:Operation.precondition/UML:Constraint", 
					  cond, true); 
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#removePreCondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
	 */
	public void removePreCondition(IConstraint constraint)
	{
		removeCondition(constraint, true);
	}

	/**
	 *
	 * Removes the past in constraint from this operation
	 *
	 * @param constraint[in] The Constraint to remove.
	 * @param isPre[in] True if the constraint represent a pre-condition, else false
	 *                  if it represents a post-condition
	 */
	private void removeCondition(IConstraint constraint, boolean isPre)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("ConditionPreRemoved");	   
		   proceed = disp.fireConditionPreRemoved(this,constraint,isPre,payload);
		}
		if (proceed)
		{
			UMLXMLManip.removeChild(m_Node,constraint);
			if( disp != null )
			{
			   payload = disp.createPayload("ConditionRemoved");	   
			   disp.fireConditionRemoved(this,constraint,isPre,payload);
			}
		}
		else
		{
			//throw exception		
		}		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getPreConditions()
	 */
	public ETList<IConstraint> getPreConditions()
	{
      ElementCollector< IConstraint > col = new ElementCollector< IConstraint >();
	  return col.retrieveElementCollection(m_Node, "UML:Operation.precondition/*", IConstraint.class);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#addRaisedException(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
	 */
	public void addRaisedException(IClassifier exc)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("RaisedExceptionPreAdded");	   
		   proceed = disp.fireRaisedExceptionPreAdded(this,exc,payload);
		}
		if (proceed)
		{
			addElementByID(exc,"raisedException");
			if( disp != null )
			{
			   payload = disp.createPayload("RaisedExceptionAdded");	   
			   disp.fireRaisedExceptionAdded(this,exc,payload);
			}
		}
		else
		{
			//throw exception		
		}		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#removeRaisedException(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
	 */
	public void removeRaisedException(IClassifier exc)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;
		if( disp != null )
		{
		   payload = disp.createPayload("RaisedExceptionPreRemoved");	   
		   proceed = disp.fireRaisedExceptionPreRemoved(this,exc,payload);
		}
		if (proceed)
		{
			removeElementByID(exc,"raisedException");
			if( disp != null )
			{
			   payload = disp.createPayload("RaisedExceptionRemoved");	   
			   disp.fireRaisedExceptionRemoved(this,exc,payload);
			}
		}
		else
		{
			//throw exception		
		}	
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getRaisedExceptions()
	 */
	public ETList<IClassifier> getRaisedExceptions()
	{
		ElementCollector<IClassifier> collector = new ElementCollector<IClassifier>();
		return collector.retrieveElementCollectionWithAttrIDs(
															  this,"raisedException", IClassifier.class);
	}

	/**
	 * Adds a Classifier to this Operation that can be used in an exception.
	 *
	 * @param classifierName[in]  The name of the Classifier. If the name cannot
	 *                            be resolved, the exception is not added.
	 */
	public void addRaisedException2(String classifierName)
	{
		if (classifierName != null && classifierName.length() > 0)
		{
			INamedElement namedEle = super.resolveSingleTypeFromString(classifierName);
			IClassifier classifier = null;
			if (namedEle != null && namedEle instanceof IClassifier)
			{
				classifier = (IClassifier)namedEle;
			}
			if (classifier != null)
			{
				addRaisedException(classifier);
			}
		}
	}

	/**
	 * Determines whether or not this Operation is a constructor.
	 *
	 * @param pval [out]
	 *
	 */
	public boolean getIsConstructor()
	{
		return getBooleanAttributeValue("isConstructor", false);
	}

    

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#setIsConstructor(boolean)
	 */
	public void setIsConstructor(boolean value)
	{
		setBooleanAttributeValue("isConstructor", value);
	}

	/**
     * Determines whether or not this Operation is resonsible for cleanup of the
     * featuring classifier.
     */
    
    public boolean getIsDestructor()
    {
       return getBooleanAttributeValue("isDestructor", false );
    }
    
    /**
     * Determines whether or not this Operation is resonsible for cleanup of the
     * featuring classifier.
     *
     * @param newval [in]
     *
     */
    
    public void setIsDestructor(boolean newVal)
    {
       setBooleanAttributeValue("isDestructor", newVal);
    }


	/**
	 *
	 * Determines whether or not this Operation should be considered a property or not.
	 * This is important when dealing with language specific constructs, such as VB
	 * Let / Get properties.
	 *
	 * @param [out] The current value.
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getIsProperty()
	{
		return getBooleanAttributeValue("isProperty", false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#setIsProperty(boolean)
	 */
	public void setIsProperty(boolean value)
	{
		setPropertyAndFireEvents(OPK_PROPERTY,value);		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getIsFriend()
	 */
	public boolean getIsFriend()
	{
		return getBooleanAttributeValue("isFriend", false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#setIsFriend(boolean)
	 */
	public void setIsFriend(boolean value)
	{
		setPropertyAndFireEvents(OPK_FRIEND,value);		
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 *
	 * @return HRESULT
	 */
	public void establishNodePresence( Document doc, Node parent )
	{
	   buildNodePresence( "UML:Operation", doc, parent );
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getIsSubroutine()
	 */
	public boolean getIsSubroutine()
	{
		return getBooleanAttributeValue("isSub", false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#setIsSubroutine(boolean)
	 */
	public void setIsSubroutine(boolean value)
	{
		setPropertyAndFireEvents(OPK_SUBROUTINE,value);		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getIsVirtual()
	 */
	public boolean getIsVirtual()
	{
		return getBooleanAttributeValue("isVirtual", false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#setIsVirtual(boolean)
	 */
	public void setIsVirtual(boolean value)
	{
		setPropertyAndFireEvents(OPK_VIRTUAL,value);		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getIsOverride()
	 */
	public boolean getIsOverride()
	{
		return getBooleanAttributeValue("isOverride", false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#setIsOverride(boolean)
	 */
	public void setIsOverride(boolean value)
	{
		setPropertyAndFireEvents(OPK_OVERRIDE,value);		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getIsDelegate()
	 */
	public boolean getIsDelegate()
	{
		return getBooleanAttributeValue("isDelegate", false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#setIsDelegate(boolean)
	 */
	public void setIsDelegate(boolean value)
	{
		setPropertyAndFireEvents(OPK_DELEGATE,value);		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#getIsIndexer()
	 */
	public boolean getIsIndexer()
	{
		return getBooleanAttributeValue("isIndexer", false);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation#setIsIndexer(boolean)
	 */
	public void setIsIndexer(boolean value)
	{
		setPropertyAndFireEvents(OPK_INDEXER,value);		
	}
	
	public void setPropertyAndFireEvents(int operationPropKind,boolean proposedValue)
	{
		String sPropertyName = null;
		switch (operationPropKind)
		{
			case OPK_PROPERTY   : sPropertyName = "isProperty";
								  break;
			case OPK_FRIEND     : sPropertyName = "isFriend";
								  break;
			case OPK_SUBROUTINE : sPropertyName = "isSub";
								  break;
			case OPK_VIRTUAL    : sPropertyName = "isVirtual";
								  break;
			case OPK_OVERRIDE   : sPropertyName = "isOverride";
								  break;
			case OPK_DELEGATE   : sPropertyName = "isDelegate";
								  break;
			case OPK_INDEXER    : sPropertyName = "isIndexer";
								  break;					
		}
		if (sPropertyName != null)
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp =
						(IClassifierEventDispatcher) ret.getDispatcher(
									EventDispatchNameKeeper.classifier());
		
			boolean proceed = true;
			IEventPayload payload = null;
			if( disp != null )
			{
			   payload = disp.createPayload("PreOperationPropertyModified");	   
			   proceed = disp.firePreOperationPropertyModified(this,operationPropKind,
			   												   proposedValue,payload);
			}
			if (proceed)
			{
				super.setBooleanAttributeValue(sPropertyName,proposedValue);
				if( disp != null )
				{
				   payload = disp.createPayload("OperationPropertyModified");	   
				   disp.fireOperationPropertyModified(this,operationPropKind,payload);
				}
			}
			else
			{
				//throw exception		
			}			
		}
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

    
     /**
      *  Retrieves all the RaisedExceptions owned by this Operation and returns
      *   them as a string for appropriate display.
      *      HRESULT OwnedConstraints([out, retval] IConstraints** pVal );
      */
     
     public String getRaisedExceptionsAsString()
     {
         ETList<IClassifier> exceptions = getRaisedExceptions();
         
         String str = "";
         
         for (IClassifier exception: exceptions)
         {
             if (str.length() > 0)
                 str += "; ";
             
             str += exception.getName();
         }
         
         return str;
     }

    public boolean isSimilar(INamedElement other)
    {
        if (!(other instanceof IOperation) || !super.isSimilar(other))
            return false;
        
        IOperation otherOp = (IOperation) other;
        ETList<IParameter> otherParams = otherOp.getParameters();
        ETList<IParameter> params = getParameters();
        
        if (params.size() != otherParams.size())
            return false;
        
        // skip first param as it is return param and a diff in return type
        // doesn't make the operation unique
        // i.e. - String getFoo() is not different from int getFoo()
        // both can not coexist in the same Class
        for (int i=1; i < params.size(); i++)
        {
            if (!params.get(i).isSimilar(otherParams.get(i)))
                return false;
        }
        
        return true;
    }

     
}
