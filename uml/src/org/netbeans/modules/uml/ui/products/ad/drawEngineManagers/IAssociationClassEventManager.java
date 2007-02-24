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



package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 * @author KevinM
 *
 */
public interface IAssociationClassEventManager extends IADClassifierEventManager {

	/*
	 * Returns all the bridges (3 links and 1 small node)
	 */ 
	public IBridgeElements getBridgeElements();

	// Deletes and reconnects the bridges
	public boolean reconnectBridges(IPresentationElement pClass1, IPresentationElement pClass);

	// Discovers bridges if they aren't already there, 
	public boolean discoverBridges(ETList < IElement > pDiscoverOnTheseElements);

	// Creates the necessary bridges
	public void createBridges(IETEdge pInitialEdge);

	// Verifies that the edges have the correct elements, used to upgrade from old association class code
	public boolean verifyEdgeParents();
	
	public INodePresentation getAssociationClassPE();
}
