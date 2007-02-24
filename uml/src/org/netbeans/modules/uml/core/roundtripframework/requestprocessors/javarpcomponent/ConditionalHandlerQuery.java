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



