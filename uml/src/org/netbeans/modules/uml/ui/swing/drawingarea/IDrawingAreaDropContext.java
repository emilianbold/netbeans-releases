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


package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;

public interface IDrawingAreaDropContext
{
	/**
	 * The drag and drops transferable object.
	*/
	public ADTransferable.ADTransferData getDropData();

	/**
	 * The presentation element dropped upon, could be NULL.
	*/
	public IPresentationElement getPEDroppedOn();

	/**
	 * The compartment dropped upon, could be NULL.
	*/
	public ICompartment getCompartmentDroppedOn();

	/**
	 * Set this to true in the pre to cancel the event.
	*/
	public boolean getCancel();

	/**
	 * Set this to true in the pre to cancel the event.
	*/
	public void setCancel( boolean value );

	/**
	 * External listeners to the drop events can add model elements to add to those in the clipboard.  These will act as if they were dropped as well.
	*/
	public ETList < IElement > getAdditionalDropElements();

	/**
	 * External listeners to the drop events can add model elements to add to those in the clipboard.  These will act as if they were dropped as well.
	*/
	public void addAdditionalDropElement( IElement newVal );

}
