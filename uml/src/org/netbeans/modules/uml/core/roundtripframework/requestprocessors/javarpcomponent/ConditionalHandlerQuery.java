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
 * Created on Oct 30, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;

/**
 * @author aztec
 *
 */
public class ConditionalHandlerQuery extends HandlerQuery implements IConditionalHandlerQuery
{
	private String m_CheckText;
	
	public ConditionalHandlerQuery( String queryKey, int textID, int titleID, int checkTextID, 
									boolean defaultAnswer, int iconType, boolean silent,
									boolean persist)
	{
		super(queryKey, textID, titleID, defaultAnswer, iconType, silent, persist);
		
		//Aztec: The string should be loaded from a resource bundle 
		//m_CheckText = checkTextID;
	}
	
	public ConditionalHandlerQuery( String queryKey, String text, String title, String checkText, 
									boolean defaultAnswer, int iconType, boolean silent,
									boolean persist)
	{
		super(queryKey, text, title, defaultAnswer, iconType, silent, persist);
		m_CheckText = checkText;
	}	
	
	protected int displayDialog(int parent, String text, String title )
	{
        int dlgAnswer = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
        
        try
        {
            IQuestionDialog dlg = new SwingQuestionDialogImpl();
            int defaultDlgAnswer = 
                 getDefaultAnswer()?
                    SimpleQuestionDialogResultKind.SQDRK_RESULT_YES
                  : SimpleQuestionDialogResultKind.SQDRK_RESULT_NO;
            QuestionResponse qr = dlg.displaySimpleQuestionDialogWithCheckbox(
                            SimpleQuestionDialogKind.SQDK_YESNO,
                            getIconType(),
                            text,
                            getCheckText(),
                            title,
                            defaultDlgAnswer,
                            false);

            // This is the difference between HandlerQuery and ConditionalHandlerQuery.
            // We set the SingleQuery flag based on value of the checkbox, so we do it here,
            // and NOT necessarily in SetAnswer.
            setSingleQueried(qr.isChecked());
            
            dlgAnswer = qr.getResult();
        }
        catch (Exception e)
        {
            Log.stackTrace(e);
        }
        return dlgAnswer;
	}
	
	protected boolean setAnswer ( int dialogResult )
	{
        if (dialogResult == SimpleQuestionDialogResultKind.SQDRK_RESULT_ALWAYS || dialogResult == SimpleQuestionDialogResultKind.SQDRK_RESULT_NEVER)
        {
            setSingleQueried( true );
        }

        setSingleQueryAnswer(dialogResult == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES || dialogResult == SimpleQuestionDialogResultKind.SQDRK_RESULT_ALWAYS);
        return getSingleQueryAnswer();
	}
	
	protected String getCheckText()
	{
		return m_CheckText;
	}

}



