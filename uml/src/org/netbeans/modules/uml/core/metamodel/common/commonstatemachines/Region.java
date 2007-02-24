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
 * Created on Sep 19, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Namespace;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public class Region extends Namespace implements IRegion
{

	/**
	 *  Constructor
	 */
	public Region()
	{
		super();		
	}
	
	public ETList<ITransition> getTransitions()
	{
		return new ElementCollector< ITransition >().
			retrieveElementCollection((IElement)this, "UML:Element.ownedElement/UML:Transition", ITransition.class);												
	}
	
	public void removeTransition(ITransition trans)
	{
		removeOwnedElement(trans);
	}
	
	public void addTransition(ITransition trans)
	{
		addOwnedElement(trans);
	}
	
	public ETList<IStateVertex> getSubVertexes()
	{
		return new ElementCollector< IStateVertex >().
			retrieveElementCollection((IElement)this, "UML:Element.ownedElement/*[ not( name(.) = \"UML:Transition\" )]", IStateVertex.class);
	}
	
	public void removeSubVertex(IStateVertex pVert)
	{
		removeOwnedElement(pVert);
	}
	
	public void addSubVertex(IStateVertex pVert)
	{
		addOwnedElement(pVert);
	}
	
	public void establishNodePresence(Document doc, Node node)
	{
		buildNodePresence("UML:Region", doc, node);
	} 
}



