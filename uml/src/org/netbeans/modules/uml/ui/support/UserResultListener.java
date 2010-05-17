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
