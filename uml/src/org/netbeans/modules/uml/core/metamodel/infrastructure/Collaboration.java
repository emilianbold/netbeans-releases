/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Collaboration extends StructuredClassifier 
						   implements ICollaboration  
{
	public ETList<INamedElement> getConstrainingElements()
	{
		ElementCollector<INamedElement> coll = new ElementCollector<INamedElement>();
		return coll.retrieveElementCollection
											(m_Node, "UML:Collaboration.constrainingElement/*", INamedElement.class);
	}
	
	public void removeConstrainingElement( INamedElement element )
	{
		UMLXMLManip.removeChild(m_Node,element);
	}
	
	public void addConstrainingElement( INamedElement element )
	{
		super.addChild("UML:Collaboration.constrainingElement",
					   "UML:Collaboration.constrainingElement",
					   element);
	}
	
	public ETList<IClassifier> getNestedClassifiers()
	{
		ElementCollector<IClassifier> coll = new ElementCollector<IClassifier>();
		return coll.retrieveElementCollection
											(m_Node, "UML:Collaboration.nestedClassifier/*", IClassifier.class);			
	}
	
	public void removeNestedClassifier( IClassifier classifier )
	{
		UMLXMLManip.removeChild(m_Node,classifier);
	}
	
	public void addNestedClassifier( IClassifier classifier )
	{
		super.addChild("UML:Collaboration.nestedClassifier",
					   "UML:Collaboration.nestedClassifier",
					   classifier);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Collaboration",doc,parent);
	}	
	
	/**
	 * Retrieves the name of the element typically used for creating icons.  
	 * It is composed of the element type and any other information needed to 
	 * make this type unique, such as 'Class' or 'PseudoState_Interface'
	 *
	 * The default implementation of this routine just returns the element type.
	 */
	public String getExpandedElementType()
	{
            String retType = getElementType();
            // if we have a collaboration with template parameters then we are
            // assuming that it is a design pattern
            ETList<IParameterableElement> parameters = super.getTemplateParameters();
            if (parameters != null)
            {
                int count = parameters.size();
                if (count > 0)
                {
                    retType = "DesignPattern";
                }
            }
            return retType;
	}

	/**
	 * Does this element have an expanded element type or is the expanded element type always the element type?
	 */
	public boolean getHasExpandedElementType()
	{
		return true;
	}
}


