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
 * Created on Jul 2, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.CommonDialogResources;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;

/**
 *
 * @author Trey Spiva
 */
public class UserResultListener implements ActionListener, MessageResultKindEnum
{
   QuestionResponse m_UserResponse = null;
   JDialog          m_ParentDialog = null;
   
   public UserResultListener(QuestionResponse result, JDialog parent)
   {
      setUserResponse(result);
      setParentDialog(parent);
   }
   
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {
      if(getUserResponse() != null)
      {
         String command = e.getActionCommand();
         if(command.equals(CommonDialogResources.getString("SwingQuestionDialogImpl.OK_BTN_NAME")))
         {
            getUserResponse().setResult(SQDRK_RESULT_OK);
         }
         else if(command.equals(CommonDialogResources.getString("SwingQuestionDialogImpl.CANCEL_BTN_NAME")))
         {
            getUserResponse().setResult(SQDRK_RESULT_CANCEL);
         }
         else if(command.equals(CommonDialogResources.getString("SwingQuestionDialogImpl.ABORT_BTN_NAME")))
         {
            getUserResponse().setResult(SQDRK_RESULT_ABORT);
         }
         else if(command.equals(CommonDialogResources.getString("SwingQuestionDialogImpl.RETRY_BTN_NAME")))
         {
            getUserResponse().setResult(SQDRK_RESULT_RETRY);
         }
         else if(command.equals(CommonDialogResources.getString("SwingQuestionDialogImpl.IGNORE_BTN_NAME")))
         {
            getUserResponse().setResult(SQDRK_RESULT_IGNORE);
         }
         else if(command.equals(CommonDialogResources.getString("SwingQuestionDialogImpl.YES_BTN_NAME")))
         {
            getUserResponse().setResult(SQDRK_RESULT_YES);
         }
         else if(command.equals(CommonDialogResources.getString("SwingQuestionDialogImpl.NO_BTN_NAME")))
         {
            getUserResponse().setResult(SQDRK_RESULT_NO);
         }
         else if(command.equals(CommonDialogResources.getString("SwingQuestionDialogImpl.ALWAYS_BTN_NAME")))
         {
            getUserResponse().setResult(SQDRK_RESULT_ALWAYS);
         }
         else if(command.equals(CommonDialogResources.getString("SwingQuestionDialogImpl.NEVER_BTN_NAME")))
         {
            getUserResponse().setResult(SQDRK_RESULT_NEVER);
         }
         else 
         {
            getUserResponse().setResult(SQDRK_RESULT_UNKNOWN);
         }
         
         if(getParentDialog() != null)
         {
            getParentDialog().setVisible(false);
         }
      }
   }

   /**
    * @return
    */
   public QuestionResponse getUserResponse()
   {
      return m_UserResponse;
   }

   /**
    * @param response
    */
   public void setUserResponse(QuestionResponse response)
   {
      m_UserResponse = response;
   }

   /**
    * @return
    */
   public JDialog getParentDialog()
   {
      return m_ParentDialog;
   }

   /**
    * @param dialog
    */
   public void setParentDialog(JDialog dialog)
   {
      m_ParentDialog = dialog;
   }

}
