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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;

/**
 * @author sumitabhk
 *
 */
public interface IADSequenceDiagEngine extends IADCoreEngine
{
	/** Sequence Diagram specific call from the CDrawingAreaButtonHandler */ 
	public void setShowAllReturnMessages(boolean bShowAllReturnMessages);
	
	/** Sequence Diagram specific call from the CDrawingAreaButtonHandler */ 
	public boolean isShowAllReturnMessages();

	/** Indicates that the auto-message numbers should be displayed. */ 
	public void setShowMessageNumbers(boolean bShowMessageNumbers);
	
	/** Indicates that the auto-message numbers should be displayed. */ 
	public boolean isShowMessageNumbers();

	/** Indicates wether the interaction boundary is already being shown, or not */ 
	public boolean isInteractionBoundaryShowing();

	/** Process the Show Interaction Boundary request */ 
	public void showInteractionBoundary(boolean bIsShowing);

	/** Retrieve the interaction operand that contains the edge and the 
    * compartment that contians the edge.
    */
	public ETPairT < IInteractionOperand, ICompartment > getEdgesInteractionOperand( IETEdge edge );

	/** Find 1st element below the logicial vertical location on this diagram */
	public IElement findFirstElementBelow(String sMetaType, int lY);

	/** Find 1st element above the logicial vertical location on this diagram */
	public IElement findFirstElementAbove(String sMetaType, int lY);

	/** Find 1st draw engine above the logicial vertical location on this diagram */
	public IDrawEngine findFirstDrawEngineAbove(String  sMetaType, int lY);

	/** Find 1st draw engine below the logicial vertical location on this diagram */
	public IDrawEngine findFirstDrawEngineBelow(String sMetaType, int lY);
}


