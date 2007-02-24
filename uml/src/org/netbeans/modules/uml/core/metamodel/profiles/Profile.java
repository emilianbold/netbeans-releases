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


package org.netbeans.modules.uml.core.metamodel.profiles;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.Package;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * A profile defines limited extensions to a reference metamodel
 * with the purpose of adapting the metamodel to a specific
 * platform or domain.
 */
public class Profile extends Package implements IProfile
{

	/**
	 * Removes a Stereotype from this Profile.
	 *
	 * @param stereoType
	 */
	public void removeStereotype(IStereotype sType) 
	{		
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.profiles.IProfile#addStereotype(org.netbeans.modules.uml.core.metamodel.profiles.IStereotype)
	 */
	public void addStereotype(IStereotype sType) 
	{		
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.profiles.IProfile#getStereotypes()
	 */
	public ETList<IStereotype> getStereotypes() 
	{		
		return null;
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Profile",doc,parent);
	}	
}
