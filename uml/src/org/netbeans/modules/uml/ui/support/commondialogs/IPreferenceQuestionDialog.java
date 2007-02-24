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

import org.netbeans.modules.uml.common.generics.ETTripleT;
import java.awt.Frame;

public interface IPreferenceQuestionDialog extends IPreferenceControlledDialog
{
	/**
	 * Displays a question dialog.  The result is provided in the out parameter.
	*/
	public int displayFromResource( String sPreferenceKey,
                                   String sPreferencePath,
                                   String sPreferenceName,
                                   String sAffirmative,
                                   String sNegative,
                                   String sAsk,
                                   String message, 
                                   int nDefaultResult,
                                   String title,
                                   int nDialogType,
                                   int nDialogIcon, 
                                   Frame parent);

	/**
	 * Displays a question dialog.  The result is provided in the out parameter.
	*/
	public int displayFromStrings( String sPreferenceKey, 
                                  String sPreferencePath,
                                  String sPreferenceName, 
                                  String sAffirmative, 
                                  String sNegative, 
                                  String sAsk, 
                                  String sMessageString, 
                                  /* SimpleQuestionDialogResultKind */ int nDefaultResult, 
                                  String sTitle, 
                                  /* SimpleQuestionDialogKind */ int nDialogType, 
                                  /* ErrorDialogIconKind */ int nDialogIcon, 
                                  Frame parent );

	/**
	 * The default button (ie IDOK).  See the return values for AfxMessageBox
	*/
	public void setDefaultButton( int value );

	/**
	 * Display the dialog box asks the standard delete question with an also question/preference
	*/
	public ETTripleT<Integer, Boolean, Boolean> displayDeleteWithAlso( String sKey, 
                                                                      String sPath, 
                                                                      String sName, 
                                                                      boolean bDefaultAffectDataModel, 
                                                                      String sAlsoQuestion);

}
