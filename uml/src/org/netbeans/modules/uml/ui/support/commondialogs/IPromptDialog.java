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


package org.netbeans.modules.uml.ui.support.commondialogs;

import org.netbeans.modules.uml.common.generics.ETPairT;

public interface IPromptDialog extends ISilentDialog
{
	/**
	 * Display question dialog with an edit control for user input
	*/
	public ETPairT<Boolean, String> displayEdit( String sMessage, String sInitalValue, String sTitle );

	/**
	 * Display question dialog with an integer edit control for user input
	*/
	public long displayEdit2( String sMessage, int nInitialValue, int nMinValue, int nMaxValue, boolean bUserHitOK, int pResult, int parent, String sTitle );

	/**
	 * Display a password dialog with an password style edit control for user input
	*/
	public long displayPassword( String sMessage, boolean bUserHitOK, StringBuffer pResult, int parent, String sTitle );

}
