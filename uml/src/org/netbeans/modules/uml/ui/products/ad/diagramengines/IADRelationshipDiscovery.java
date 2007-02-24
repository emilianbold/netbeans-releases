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



package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IRelationshipDiscovery;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 * @author KevinM
 * The IADRelationshipDiscovery provides drawing support for an TSGraphObject.
 * There is a one to one relationship between an TSGraphObject and an IADRelationshipDiscovery
 */
public interface IADRelationshipDiscovery extends IRelationshipDiscovery {

	public ETList<IPresentationElement> discoverNestedLinks(ETList<IElement> pDiscoverOnTheseElements);
	public ETList<IPresentationElement> discoverCommentLinks(ETList<IElement> pDiscoverOnTheseElement);
	public ETList<IPresentationElement> discoverPartFacadeLinks(ETList<IElement> pDiscoverOnTheseElements);
	public ETList<IPresentationElement> discoverAssociationClassLinks(ETList<IElement> pDiscoverOnTheseElements);
	public ETList<IPresentationElement> createPortPresentationElements(IPresentationElement pComponentPE);
	public IPresentationElement createPortPresentationElement(IPresentationElement pComponentPE,	IPort pPortToCreate);
	public ETList<IPresentationElement> createPortProvidedAndRequiredInterfaces(IPresentationElement pPortPE);
	public ETList<IPresentationElement> discoverMessages( ETList<IElement> pDiscoverOnTheseElements);
	public ETList<IPresentationElement> createPortProvidedInterface(IPresentationElement pPortPE, IInterface pInterface);   
	public ETList<IPresentationElement> createPortRequiredInterface(IPresentationElement pPortPE, IInterface pInterface);

	/// Discovers generalization relationships on the current diagram
	public ETList<IPresentationElement> discoverGeneralizations();
	public IPresentationElement createInterfaceAsIconPresentationElement(IInterface pElement,IPresentationElement pClass);
	public IPresentationElement createInterfaceAsClassPresentationElement(IInterface pElement);
	public IPresentationElement createAssociationClassPresentationElement(IAssociationClass pAssociationClass,
														IPresentationElement pClass1, IPresentationElement pClass2);
														
	//	These routines are used in DiscoverCommonRelations														
	public ETList<IPresentationElement> discoverNestedLinks(boolean bAutoRouteEdges,
									ETList<IElement> pNewElementsBeingCreated,
									ETList<IElement> pElementsAlreadyOnTheDiagrams);
									
	public ETList<IPresentationElement> discoverCommentLinks(boolean bAutoRouteEdges,
									ETList<IElement> pNewElementsBeingCreated,
									ETList<IElement> pElementsAlreadyOnTheDiagrams);
	public ETList<IPresentationElement> discoverPartFacadeLinks(boolean bAutoRouteEdges,
									ETList<IElement> pNewElementsBeingCreated,
									ETList<IElement> pElementsAlreadyOnTheDiagrams);
	public ETList<IPresentationElement> discoverAssociationClassLinks(boolean bAutoRouteEdges,
									ETList<IElement> pNewElementsBeingCreated,
									ETList<IElement> pElementsAlreadyOnTheDiagrams);
									
																						
}
