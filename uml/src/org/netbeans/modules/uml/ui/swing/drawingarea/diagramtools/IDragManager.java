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



package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;

/**
 * @author KevinM
 *
 */
public interface IDragManager {
	// Resets the tool so its clean and ready for use.  Use when you have a tool instance you use over and over again.
	public void reset();

	// Gets the orientation of the drag manager.
	public int getOrientation();
	
	// Sets the orientation of the drag manager.
	public void setOrientation(int orientation);

	// Adds a compartment that is notified about the stretch as the tool moves.
	public void setStretchCompartment(ICompartment pCompartment);

	// The up most logical location where the tool is allowed to move
	public void setTop(int lTopLogical);
	// The down most logical location where the tool is allowed to move
	public void setBottom(int lBottomLogical);

	// Adds these presentation elements to the list of above elements.
	public void addElementsAbove(ETList < IPresentationElement > pPresentationElements);
	// Adds these presentation elements to the list of below elements.
	public void addElementsBelow(ETList < IPresentationElement > pPresentationElements);
}
