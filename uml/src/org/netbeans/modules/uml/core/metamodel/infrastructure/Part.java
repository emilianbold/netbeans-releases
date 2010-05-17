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


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.StructuralFeature;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Part extends StructuralFeature implements IPart 
{
	IConnectableElement m_ConnectElementAggregate = new ConnectableElement();
	IParameterableElement m_ParameterableAggregate = new ParameterableElement();

	public int getPartKind()
	{
		return super.getPartKind("partKind");
	}
	
	public void setPartKind( /* PartKind */ int value )
	{
		super.setPartKind("partKind",value);
	}
	
	public boolean getIsWhole()
	{
		return super.getBooleanAttributeValue("isWhole",false);
	}
	
	public void setIsWhole( boolean value )
	{
		super.setBooleanAttributeValue("isWhole",value);
	}
	
	public int getInitialCardinality()
	{
		return super.getAttributeValueInt("intialCardinality");
	}
	
	public void setInitialCardinality( int value )
	{
		super.setAttributeValue("intialCardinality",value);
	}
	
	//	Override the IFeature::get_FeaturingClassifier
	public IClassifier getFeaturingClassifier()
	{
		IElement element = super.getOwner();
		IClassifier classifier = null;
		if (element != null && element instanceof IClassifier)
		{
			classifier = (IClassifier)element;
		}
		return classifier;
	}
	
	/**
	 *
	 * The feature that this Part represents. Will be empty if the Part represents an 
	 * entire Classifier ( that is, when IsWhole returns "true", this should be empty ).
	 */
	public IStructuralFeature getDefiningFeature()
	{
		ElementCollector<IStructuralFeature> collector = new ElementCollector<IStructuralFeature>();
		return collector.retrieveSingleElementWithAttrID(this,"definingFeature", IStructuralFeature.class);	
	}
	
	public void setDefiningFeature( IStructuralFeature value )
	{
		super.setElement(value, "definingFeature");
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		super.buildNodePresence("UML:Part",doc,parent);
	}
	
	//IConnectableElement Methods
	/**
	 * method AddEnd
	*/
	public void addEnd( IConnectorEnd pConnector )
	{
		m_ConnectElementAggregate.addEnd(pConnector);
	}

	/**
	 * method RemoveEnd
	*/
	public void removeEnd( IConnectorEnd pConnector )
	{
		m_ConnectElementAggregate.removeEnd(pConnector);
	}

	/**
	 * property Ends
	*/
	public ETList<IConnectorEnd> getEnds()
	{
		return m_ConnectElementAggregate.getEnds();
	}

	/**
	 * method AddRoleContext
	*/
	public void addRoleContext( IStructuredClassifier pClassifier )
	{
		m_ConnectElementAggregate.addRoleContext(pClassifier);
	}

	/**
	 * method RemoveRoleContext
	*/
	public void removeRoleContext( IStructuredClassifier pClassifier )
	{
		m_ConnectElementAggregate.removeRoleContext(pClassifier);
	}

	/**
	 * property RoleContexts
	*/
	public ETList<IStructuredClassifier> getRoleContexts()
	{
		if (m_ConnectElementAggregate == null)
		{
			m_ConnectElementAggregate = new ConnectableElement();
		}
		return m_ConnectElementAggregate.getRoleContexts();
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
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        m_ConnectElementAggregate.setNode(n);
        m_ParameterableAggregate.setNode(n);
    }
}

