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



package org.netbeans.modules.uml.ui.support.relationshipVerification;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;

public interface IEdgeVerification
{
	/*
	 * Verifies that the starting node is valid for this relation type.
	 */
	public boolean verifyStartNode(IElement pStartingNode, String sEdgeMetaTypeString);

	/*
	 * Verifies that the finishing node is valid for this relation type.
	 */
	public boolean verifyFinishNode(IElement pStartNode, IElement pFinishNode, String sEdgeMetaTypeString);

	/*
	 * Verifies that the edge is valid, if so then the appropriate edge IElement is created and returned
	 */
	public IElement verifyAndCreateEdgeRelation(IETEdge pEdge, INamespace pNamespace, String sEdgeMetaTypeString, String sInitializationString);

	/*
	 * Create the AssociationClassifier link.
	 */
    public IAssociationClass createAssociationClassifierRelation(IETNode pSourceNode, IETNode pTarget);
	
	/*
	 * Verifies that this edge can be reconnected.
	 */
	public boolean verifyReconnectStart(IElement pReconnectingSideElement, String sEdgeMetaTypeString);
}
