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


/*
 *
 * Created on Jul 1, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.commondialogs;

import java.awt.Window;

import org.netbeans.modules.uml.ui.support.QuestionResponse;

/**
 *
 * @author Trey Spiva
 */
public interface IQuestionDialog extends ISilentDialog
{
	public static int IDOK = 1;
	public static int IDCANCEL = 2;
	public static int IDABORT = 3;
	public static int IDRETRY = 4;
	public static int IDIGNORE = 5;
	public static int IDYES = 6;
	public static int IDNO = 7;
	public static int IDALWAYS = 8;
	public static int IDNEVER = 9;
   /**
    * Displays a question dialog that also has a checkbox.  The checkbox can be
    * use to prompt the user whether the dialog should be shown again.
    * 
    * @param dialogType The type of the dialog.  The dialog type should be one 
    *                   of the MessageDialogKindEnum values.
    * @param dialogIcon The type of icon to display.  The icon tyep must be one
    *                   of the MessageIconKindEnum values.
    * @param message The message to be displayed in the dialog.
    * @param checkboxMsg The message to be displayed on the check box.
    * @param defaultResult The default result of the dialog.  The result should 
    *                      be one of the MessageDialogKindEnum values.
    * @param defaultIsChecked Specifies if the check box is checked by default.
    * @return Specifes the users response to the question and the value of the
    *         check box.
    */
   public QuestionResponse displaySimpleQuestionDialogWithCheckbox(int dialogType,
                                                                   int dialogIcon,
                                                                   String message,
                                                                   String checkboxMsg,
                                                                   int    defaultResult,
                                                                   boolean defaultIsChecked);
                                                      
   public QuestionResponse displaySimpleQuestionDialogWithCheckbox(int     dialogType,
                                                                   int     dialogIcon,
                                                                   String  message,
                                                                   String  checkboxMsg,
                                                                   String  title,
                                                                   int     defaultResult,
                                                                   boolean defaultIsChecked);
   
   public QuestionResponse displaySimpleQuestionDialog(
           int dialogType, 
           int errorDialogIcon, 
           String messageString, 
           int defaultResult, 
           Window parent,
           String title);
   
	/**
	 * The default button (ie IDOK).  See the return values for AfxMessageBox
	*/
	public void setDefaultButton( int value );
}