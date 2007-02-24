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


package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

public interface ITopographyChangeAction extends IDelayedAction, IExecutableAction
{
	/**
	 * The kind of the action
	*/
	public int getKind();

	/**
	 * The kind of the action
	*/
	public void setKind( /* _TopographyActionKind */ int value );

	/**
	 * The presentation element to be acted upon
	*/
	public IPresentationElement getPresentationElement();

	/**
	 * The presentation element to be acted upon
	*/
	public void setPresentationElement( IPresentationElement value );

	/**
	 * The x coordinate
	*/
	public int getX();

	/**
	 * The x coordinate
	*/
	public void setX( int value );

	/**
	 * The y coordinate
	*/
	public int getY();

	/**
	 * The y coordinate
	*/
	public void setY( int value );

	/**
	 * The new width
	*/
	public int getWidth();

	/**
	 * The new width
	*/
	public void setWidth( int value );

	/**
	 * The new height
	*/
	public int getHeight();

	/**
	 * The new height
	*/
	public void setHeight( int value );

	/**
	 * Set/Get the current layout style.
	*/
	public void setLayoutStyle( /* LayoutKind */ int value );

	/**
	 * Set/Get the current layout style.
	*/
	public void setLayoutStyle( boolean bDoZoom, boolean bCreateBusyState, /* LayoutKind */ int value );

	/**
	 * Set/Get the current layout style.
	*/
	public int getLayoutStyle();
}
