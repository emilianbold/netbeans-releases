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



package org.netbeans.modules.uml.ui.controls.doccontrol;

/**
 * @author sumitabhk
 *
 */
public interface IDocumentationControl
{
	/**
	 * Initialize the Doc Control
	*/
	public void initialize();

	/**
	 * Enable the Doc Control
	*/
	public void enableDocCtrl();

	/**
	 * Disable the Doc Control
	*/
	public void disableDocCtrl();

	/**
	 * Get DHTML Editor's IUnknown
	*/
	public void getEditorCtrl( Object obj );

	/**
	 * property Enabled
	*/
	public int getEnabled();

	/**
	 * property Enabled
	*/
	public void setEnabled( int value );

	/**
	 * Saves the currently selected element description
	*/
	public void setCurElementDescription();

	/**
	 * Registers or revokes event sinks
	*/
	public void connectSinks( boolean connect );

	/**
	 * Shows current Diagram or ProjectTree selection.
	*/
	public void showLastSelectedElement();

	/**
	 * Sets Focus to the control window
	*/
	public void setFocus();

}


