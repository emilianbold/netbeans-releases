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


package org.netbeans.modules.uml.core.metamodel.diagrams;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ICoreRelationshipDiscovery
{
	/**
	 * Adds a node or link to the diagram
	*/
	public IPresentationElement createPresentationElement( IElement pElement );

	/**
	 * Adds a node to the diagram at this location
	*/
	public IPresentationElement createNodePresentationElement( IElement pElement, IETPoint pLocation );

	/**
	 * Creates a link presentation element.
	*/
	public IPresentationElement createLinkPresentationElement( IElement pElement, IPresentationElement pFromPresentationElement, IPresentationElement pToPresentationElement );

	/**
	 * Creates a link between the two elements
	*/
	public IPresentationElement createLinkPresentationElement( IElement pElement, IElement pFromElement, IElement pToElement );

	/**
	 * Discovers common relationships (generalization, association...) among the presentation elements on the diagram.
	*/
	public ETList<IPresentationElement> discoverCommonRelations(boolean bAutoRouteEdges);

	/**
	 * Discovers common relationships on these objects(Generalizations, associations...)
	*/
	public ETList<IPresentationElement> discoverCommonRelations( boolean bAutoRouteEdges, ETList<IElement> pDiscoverOnTheseElements );

	/**
	 * Discovers common relationships on these objects(Generalizations, associations...). Relations are discovered among the elements being dropped and then between those elements being dropped and elements on the diagram.
	*/
	public ETList<IPresentationElement> discoverCommonRelations( boolean bAutoRouteEdges, ETList<IElement> pNewElementsBeingCreated, ETList<IElement> pElementsAlreadyOnTheDiagrams );

	/**
	 * Discovers common relationships (generalization, association...) among the selected elements on the diagram.
	*/
	public ETList<IPresentationElement> discoverCommonRelationsAmongSelectedElements();

}
