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

/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Behavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public class ActionSequence extends Behavior implements IActionSequence
{

	public ActionSequence()
	{
	}
	
	/**
	 * Retrieves all tagged values, incuding standard tags.
	 * 
	 * @return All tagged values
	 */
	public ETList<IAction> getActions()
	{
		return new ElementCollector< IAction >()
			.retrieveElementCollection(m_Node,"UML:ActionSequence.action/*", IAction.class);		
	}
	
	/**
	 * Removes an action from this sequence.
	 *
	 * @param action[in]
	 */
	public void removeAction( IAction action )
	{
		UMLXMLManip.removeChild(m_Node,action);
	}
	
	/**
	 * Adds an action to this sequence
	 *
	 * @param action[in]
	 */
	public void addAction( IAction action )
	{
		addChild("UML:ActionSequence.action","UML:ActionSequence.action",action);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:ActionSequence",doc,parent);
	}	
}



