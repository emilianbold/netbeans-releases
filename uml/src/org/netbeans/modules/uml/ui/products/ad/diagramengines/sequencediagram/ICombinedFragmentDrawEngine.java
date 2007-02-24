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



package org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import com.tomsawyer.editor.TSEEdge;

/**
 * @author sumitabhk
 *
 */
public interface ICombinedFragmentDrawEngine extends IADContainerDrawEngine
{
	// Sets the value for the interaction operator
	public void setOperator( /*IInteractionOperator*/ int newVal);

	// Edit the interaction constraint associated with the input interaction operand
	public void editConstraint( IInteractionOperand pOperand );

	// Retrieve the interaction operand that contains the edge
	public ETPairT < IInteractionOperand, ICompartment > getEdgesInteractionOperand( IETEdge pEdge);

	// Expands this draw engine to include all child interaction operand compartments 
	public void expandToIncludeInteractionOperands( boolean bAllowToShrink );

	// Graphically selects all meta model covered items for each of the interaction operands
	public int getSelectAllCoveredItems();
}
