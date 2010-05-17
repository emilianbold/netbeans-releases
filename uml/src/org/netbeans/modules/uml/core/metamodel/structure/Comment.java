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

package org.netbeans.modules.uml.core.metamodel.structure;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class Comment extends NamedElement implements IComment 
{

	/**
	 * 
	 */
	public Comment() 
	{
		super();	
	}
	
	/**
	 * Is this element in the AnnotatedElements list?
	 *
	 * @param element[in] The element to check
	 */
	public boolean getIsAnnotatedElement( INamedElement element )
	{
		boolean isAnnotated = false;
		String annotatedElems = XMLManip.getAttributeValue(m_Node,
                        "annotatedElement");
		if (annotatedElems != null && annotatedElems.length() > 0)
		{
			String xmiID = element.getXMIID();
			if ( annotatedElems.indexOf(xmiID) != -1)
			{
				isAnnotated = true;														   
			}
		}
		return isAnnotated;
	}
	
	public ETList<INamedElement> getAnnotatedElements()
	{
		ElementCollector<INamedElement> collector = 
                        new ElementCollector<INamedElement>();
		return collector.retrieveElementCollectionWithAttrIDs(this,
                        "annotatedElement", INamedElement.class);
	}
	
	public void removeAnnotatedElement( INamedElement element )
	{
		removeElementByID(element,"annotatedElement");
	}
	
	public void addAnnotatedElement( INamedElement element )
	{
		addElementByID(element,"annotatedElement");
	}
	
	/**
	 *
	 * The body of the comment.
	 */
	public void setBody( String newValue )
	{
		UMLXMLManip.setNodeTextValue(this,"UML:Comment.body",newValue,false);
	}
	
	public String getBody()
	{
		return XMLManip.retrieveNodeTextValue(m_Node,"UML:Comment.body");
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Comment",doc,parent);
	}	
}



