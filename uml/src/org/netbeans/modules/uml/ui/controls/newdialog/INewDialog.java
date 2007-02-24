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


package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Frame;

import org.netbeans.modules.uml.ui.support.commondialogs.ISilentDialog;

public interface INewDialog extends ISilentDialog
{
	/**
	 * Display the dialog.
	*/
	public INewDialogTabDetails display( Frame parent );

	/**
	 * Display the dialog with a validate processor.
	*/
	public INewDialogTabDetails display2( INewDialogValidateProcessor pValidateProcessor, Frame parent );

	/**
	 * The default tab for the dialog
	*/
	public long putDefaultTab( /* NewDialogTabKind */ int nDefaultTab );

	/**
	 * Allows you to specify the tabs in the dialog.  If unset the all tabs are shown, otherwise just those specified through AddTab are shown
	*/
	public long addTab( /* NewDialogTabKind */ int nTabKind );

	/**
	 * Has this tab been added.
	*/
	public boolean isTab( /* NewDialogTabKind */ int nTabKind );

	/**
	 * Provides defaults to one of the tabs
	*/
	public long specifyDefaults( INewDialogTabDetails pDetails );

	/**
	 * Returns the result of the dialog.  NULL if the user hit cancel.
	*/
	public INewDialogTabDetails getResult();

}
