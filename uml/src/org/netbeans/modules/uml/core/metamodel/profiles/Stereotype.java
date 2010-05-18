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


package org.netbeans.modules.uml.core.metamodel.profiles;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.constructs.Class;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 * Stereotype is the implementation of the UML Stereotype meta type.
 * A stereotype defines how an existing metaclass (or stereotype) may be
 * extended, and enables the use of platform or domain specific
 * terminology or notation in addition to the ones used for the extended
 * metaclass.
 */
public class Stereotype extends Class implements IStereotype
{

	/**
	 *
	 * Retrieves the names of the Meta types that this Stereotype can be applied to.
	 */
	public ETList<String> appliesTo() 
	{
		String values = UMLXMLManip.getAttributeValue(m_Node, "appliesTo");
		ETList<String> strs = null;
		if (values != null && values.length() > 0)
		{
			strs = StringUtilities.splitOnDelimiter(values, " ");			
		}
		return strs;				
	}

	/**
	 *
	 * Adds a meta type that this Stereotype will now be able to be applied to.
	 *
	 * @param sType[in] The name of the meta type, e.g. "Class"
	 */
	public void addApplicableMetaType(String sType) 
	{
		String curValue = UMLXMLManip.getAttributeValue( m_Node, "appliesTo" );
		if (curValue != null && curValue.length() > 0)
		{
			curValue += " ";
			curValue += sType;		
		}
		else
		{
			curValue = sType;
		}
		
		UMLXMLManip.setAttributeValue(this,"appliesTo",curValue);
	}

	/**
	 *
	 * Removes the passed-in meta type from this Stereotype's list of applicable
	 * types. Essentially, this now prevents this Stereotype from being applied 
	 * to an instance of that meta type.
	 *
	 * @param sType[in] The meta type to remove
	 */
	public void removeApplicableMetaType(String sType) 
	{
		if (sType != null && sType.length() > 0)
		{
			String curValue = UMLXMLManip.getAttributeValue(m_Node, "appliesTo" );
			if (curValue != null && curValue.length() > 0)
			{
				String newValue = UMLXMLManip.removeElementFromString(sType, curValue);
				UMLXMLManip.setAttributeValue(this,"appliesTo",newValue);
			}
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
		buildNodePresence("UML:Stereotype",doc,parent);
	}	

}


