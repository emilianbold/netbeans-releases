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
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class NavigableEnd extends AssociationEnd implements INavigableEnd 
{
	private IAttribute m_AttributeAggregate = null;
	private IParameterableElement m_ParameterableAggregate = null;
	
	public NavigableEnd()
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
		super.buildNodePresence("UML:NavigableEnd",doc,parent);
	}
	
	/**
	 *
	 * Turns this end into a normal AssociationEnd.
	 */
	public IAssociationEnd makeNonNavigable()
	{
		IAssociationEnd assoEnd = null;	
		String newForm = "AssociationEnd";
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
					(IClassifierEventDispatcher) ret.getDispatcher(
								EventDispatchNameKeeper.classifier());
		
		boolean proceed = true;
		IEventPayload payload = null;		
		if( disp != null )
		{
		   payload = disp.createPayload("PreAssociationEndTransform");	   
		   proceed = disp.firePreAssociationEndTransform(this,newForm,payload);
		}
		if (proceed)
		{
			assoEnd = (IAssociationEnd) UMLXMLManip.transformElement(this,newForm);
			if( disp != null )
			{
			   payload = disp.createPayload("AssociationEndTransform");	   
			   disp.fireAssociationEndTransformed(assoEnd,payload);
			}
		}
		return assoEnd;
	}
	
	/**
	 *
	 * Retrieves the Classifier that has this end installed as a referenced attribute.
	 */
	public IClassifier getReferencingClassifier()
	{
		IClassifier classifier = null;
		IAssociationEnd otherEnd = super.getOtherEnd2();
		if (otherEnd != null)
		{		
			classifier = otherEnd.getParticipant();
		}
		return classifier;		
	}
	
	/**
	 *
	 * Queries this element for IArtifacts that represent source files. For
	 * each one found, retrieves the Language object associated with that
	 * file.
	 *
	 * A Navigable end is really a part of the referencing classifier. So,
	 * this function is redefined here to get the referencing classifier and
	 * get its languages.
	 */
	public ETList<ILanguage> retrieveLanguagesFromArtifacts()
	{
		ETList<ILanguage> languages = null;
		IClassifier classifier = getReferencingClassifier();
		if (classifier != null)
		{
			languages = classifier.getLanguages();			
		}
		return languages;
	}
	
	/** 
	 * Returns the source file artifacts for the navigable end's referencing class.
	 */
	public ETList<IElement> getSourceFiles()
	{
		ETList<IElement> files = null;
		IClassifier classifier = getReferencingClassifier();
		if (classifier != null)
		{
			files = classifier.getSourceFiles();			
		}
		return files;
	}
	
	//Attribute class methods.
	/**
	 * Specifies whether the Attribute is derived, i.e. its value or values can be computed from other information. The default value is false.
	*/
	public boolean getIsDerived()
	{
		ensureAttributeAggregate();
		return m_AttributeAggregate.getIsDerived();
	}

	/**
	 * Specifies whether the Attribute is derived, i.e. its value or values can be computed from other information. The default value is false.
	*/
	public void setIsDerived( boolean value )
	{
		ensureAttributeAggregate();
		m_AttributeAggregate.setIsDerived(value);
	}

	/**
	 * References an optional expression specifying how to set the attribute when creating an instance in the absence of a specific setting for the attribute.
	*/
	public IExpression getDefault()
	{
		ensureAttributeAggregate();
		return m_AttributeAggregate.getDefault();
	}
	/**
	 * References an optional expression specifying how to set the attribute when creating an instance in the absence of a specific setting for the attribute.
	*/
	public void setDefault( IExpression value )
	{
		ensureAttributeAggregate();
		m_AttributeAggregate.setDefault(value);
	}

	/**
	 * property DerivationRule
	*/
	public IExpression getDerivationRule()
	{
		ensureAttributeAggregate();
		return m_AttributeAggregate.getDerivationRule();
	}

	/**
	 * property DerivationRule
	*/
	public void setDerivationRule( IExpression value )
	{
		ensureAttributeAggregate();
		m_AttributeAggregate.setDerivationRule(value);
	}

	/**
	 * property AssociationEnd
	*/
	public IAssociationEnd getAssociationEnd()
	{
		ensureAttributeAggregate();
		return m_AttributeAggregate.getAssociationEnd();
	}

	/**
	 * property AssociationEnd
	*/
	public void setAssociationEnd( IAssociationEnd value )
	{
		ensureAttributeAggregate();
		m_AttributeAggregate.setAssociationEnd(value);
	}

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public String getDefault2()
	{
		ensureAttributeAggregate();
		return m_AttributeAggregate.getDefault2();
	}

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault2( String value )
	{		
		ensureAttributeAggregate();
		m_AttributeAggregate.setDefault2(value);
	}

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public ETPairT<String,String> getDefault3()
	{
		ensureAttributeAggregate();
		return m_AttributeAggregate.getDefault3();
	}

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault3( String lang, String body )
	{
		ensureAttributeAggregate();
		m_AttributeAggregate.setDefault3(lang,body);
	}

	/**
	 * Determines whether or not this attribute has the WithEvents modifier associated with it. This is specific to the VB programming language.
	*/
	public boolean getIsWithEvents()
	{
		ensureAttributeAggregate();
		return m_AttributeAggregate.getIsWithEvents();
	}

	/**
	 * Determines whether or not this attribute has the WithEvents modifier associated with it. This is specific to the VB programming language.
	*/
	public void setIsWithEvents( boolean value )
	{
		ensureAttributeAggregate();
		m_AttributeAggregate.setIsWithEvents(value);
	}

	/**
	 * Indicates whether or not the attribute instance is created on the heap or not upon the instanciation of the featuring classifier.
	*/
	public boolean getHeapBased()
	{
		ensureAttributeAggregate();
		return m_AttributeAggregate.getHeapBased();
	}

	/**
	 * Indicates whether or not the attribute instance is created on the heap or not upon the instanciation of the featuring classifier.
	*/
	public void setHeapBased( boolean value )
	{
		ensureAttributeAggregate();
		m_AttributeAggregate.setHeapBased(value);
	}

	/**
	 * Indicates whether or not this attribute maps to a primary key column in a database.
	*/
	public boolean getIsPrimaryKey()
	{
		ensureAttributeAggregate();
		return m_AttributeAggregate.getIsPrimaryKey();
	}

	/**
	 * Indicates whether or not this attribute maps to a primary key column in a database.
	*/
	public void setIsPrimaryKey( boolean value )
	{
		ensureAttributeAggregate();
		m_AttributeAggregate.setIsPrimaryKey(value);
	}
	
	//IParameterableElement methods
	public IParameterableElement getDefaultElement()
	{
		ensureParameterableAggregate();
	   return m_ParameterableAggregate.getDefaultElement();
	}
   
	public void setDefaultElement( IParameterableElement element )
	{
		ensureParameterableAggregate();
		m_ParameterableAggregate.setDefaultElement(element);   	
	}
   
	public void setDefaultElement2( String newVal )
	{
		ensureParameterableAggregate();
		 m_ParameterableAggregate.setDefaultElement2(newVal);   	
	}
   
	public IClassifier getTemplate()
	{
		ensureParameterableAggregate();
		 return m_ParameterableAggregate.getTemplate();      	
	}
   
	public void setTemplate( IClassifier value )
	{
		ensureParameterableAggregate();
		 m_ParameterableAggregate.setTemplate(value);   	
	} 
   
	public String getTypeConstraint()
	{
		ensureParameterableAggregate();
		 return m_ParameterableAggregate.getTypeConstraint();      	
	}
   
	public void setTypeConstraint( String value )
	{
		ensureParameterableAggregate();
		 m_ParameterableAggregate.setTypeConstraint(value);   	
	}
	
	// ********************************************************
	// Helper classes to create and initialize the aggregations
	// ********************************************************
	
	protected void ensureAttributeAggregate()
	{
		if (m_AttributeAggregate == null)
		{
			m_AttributeAggregate = new Attribute();
			m_AttributeAggregate.setNode(getNode());
		}
	}
	
	protected void ensureParameterableAggregate()
	{
		if (m_ParameterableAggregate == null)
		{
			m_ParameterableAggregate = new ParameterableElement();
			m_ParameterableAggregate.setNode(getNode());
		}
	}
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode()
	 */
	public void setNode(Node n)
	{
		super.setNode(n);
		
		if (m_ParameterableAggregate != null)
		{
			m_ParameterableAggregate.setNode(n);
		}
		
		if (m_AttributeAggregate != null)
		{
			m_AttributeAggregate.setNode(n);
		}
	}

}


