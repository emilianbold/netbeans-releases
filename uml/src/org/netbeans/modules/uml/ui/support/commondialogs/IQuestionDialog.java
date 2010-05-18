/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
