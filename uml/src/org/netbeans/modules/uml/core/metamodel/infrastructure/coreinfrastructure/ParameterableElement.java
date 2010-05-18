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
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;


public class ParameterableElement extends NamedElement
								  implements IParameterableElement  
{
	public ParameterableElement()
	{
		super();
	}
	
	/**
	 * property Default
	*/
	public IParameterableElement getDefaultElement()
	{
		ElementCollector<IParameterableElement> collector = 
									new ElementCollector<IParameterableElement>();
		return collector.retrieveSingleElementWithAttrID(this,"default", IParameterableElement.class);				
	}

	/**
	 * property Default
	*/
	public void setDefaultElement( IParameterableElement element )
	{
		super.setElement(element,"default");
	}

	/**
	 *  Sets the default element on this parameter via a name indicating its type.
 	 *
 	 * @param newVal[in] The type to use as the DefaultElement
	 */
	public void setDefaultElement2( String newVal )
	{
		INamedElement type = super.resolveSingleTypeFromString(newVal);
		if (type != null)
			setDefaultElement((IParameterableElement)type);
	}

	/**
	 * property Template
	*/
	public IClassifier getTemplate()
	{
		return OwnerRetriever.getOwnerByType(this, IClassifier.class);
	}

	/**
	 * property Template
	*/
	public void setTemplate( IClassifier newVal )
	{
		super.setOwner(newVal);
	}

	/**
	 * Name of the meta type that must be used when instantiating the template.
	*/
	public String getTypeConstraint()
	{
		return super.getAttributeValue("typeConstraint");
	}

	/**
	 * Name of the meta type that must be used when instantiating the template.
	*/
	public void setTypeConstraint( String newVal )
	{
		super.setAttributeValue("typeConstraint",newVal);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		super.buildNodePresence("UML:ParameterableElement",doc,parent);
	}		
}


