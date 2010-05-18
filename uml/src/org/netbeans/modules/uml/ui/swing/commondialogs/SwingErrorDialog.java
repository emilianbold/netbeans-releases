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
package org.netbeans.modules.uml.ui.swing.commondialogs;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

//import org.apache.xpath.compiler.PsuedoNames;

import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessageData;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.UserResultListener;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.messaging.IMessenger;

/**
 * 
 * @author Trey Spiva
 */
public class SwingErrorDialog implements IErrorDialog
{
   private boolean m_bRunSilent = false;
   private int m_DefaultButton = -1;
   private Frame m_ParentFrame = null;
   private JDialog m_ParentDialog = null;

   public SwingErrorDialog()
   {
   		initMessaging();
   		m_ParentFrame = ProductHelper.getProxyUserInterface().getWindowHandle();
   }
   public SwingErrorDialog(Frame pFrame)
   {
		initMessaging();
		m_ParentFrame = pFrame;
   }
   public SwingErrorDialog(JDialog pDialog)
   {
		initMessaging();
		if (pDialog != null){
			m_ParentDialog = pDialog;
		}
		else{
			m_ParentFrame = ProductHelper.getProxyUserInterface().getWindowHandle();
		}
   }
   private void initMessaging()
   {
	  IMessenger pMsg = ProductHelper.getMessenger();
	  if (pMsg != null)
	  {
		 m_bRunSilent = pMsg.getDisableMessaging();
	  }
   }
   /**
   * Displays an error dialog based on the arguments.  Note that since CErrorDialog implements
   * the ISilentDialog interface, it may be silent.  In that case, no error dialog is shown 
   * and S_OK is returned.
   *
   * @param sErrorString[in] The error message presented to the user
   * @param nErrorDialogIcon[in] The icon to display in the dialog
   * @param sTitle[in] The dialog title
   */
   public void display(String sErrorString, /*ErrorDialogIconKind*/
   int nErrorDialogIcon, String sTitle)
   {
      if (sErrorString != null && sErrorString.length() > 0)
      {
         if (!m_bRunSilent)
         {
            Icon icon = getIconForType(nErrorDialogIcon);
            JCenterDialog dialog = createDialog(sErrorString, sTitle, icon);
            if (dialog != null)
            {
               dialog.pack();
               Insets insets = dialog.getInsets();
               insets.top = 5;
               insets.left = 5;
               insets.bottom = 5;
               insets.right = 5;

					if (m_ParentFrame != null){
						dialog.center(m_ParentFrame);
					}
					else if (m_ParentDialog != null){
						dialog.center(m_ParentDialog);
					}
					dialog.setModal(true);
					dialog.setVisible(true);
            }
         }
      }
   }
   /**
   * Displays an error dialog based on the arguments.  Note that since CErrorDialog implements
   * the ISilentDialog interface, it may be silent.  In that case, no error dialog is shown 
   * and S_OK is returned.
   *
   * @param sErrorString[in] The error message presented to the user
   * @param sTitle[in] The dialog title
   */
   public void display(String sErrorString, String sTitle)
   {
      String sTempTitle = sTitle;
      if (sTempTitle == null || sTempTitle.length() == 0)
      {
         sTitle = DefaultCommonDialogResource.getString("IDS_ERROR_TITLE");
      }
      if (!m_bRunSilent)
      {
         Icon icon = getIconForType(-1);
         JCenterDialog dialog = createDialog(sErrorString, sTitle, icon);
         if (dialog != null)
         {
            dialog.pack();
            Insets insets = dialog.getInsets();
            insets.top = 5;
            insets.left = 5;
            insets.bottom = 5;
            insets.right = 5;

				if (m_ParentFrame != null){
					dialog.center(m_ParentFrame);
				}
				else if (m_ParentDialog != null){
					dialog.center(m_ParentDialog);
				}
				dialog.setModal(true);
				dialog.setVisible(true);
            
         }
      }
   }
   /**
   * Displays an error dialog based on the arguments.  Note that since CErrorDialog implements
   * the ISilentDialog interface, it may be silent.  In that case, no error dialog is shown 
   * and S_OK is returned.  If the IMessageData object has sub messages then a more complex
   * error dialog will be presented.
   *
   * @param pData[in] An IMessageData object that is to be displayed to the user
   * @param sDescription[in] A toplevel description for the error
   * @param sTitle[in] The dialog title
   */
   public void display(IMessageData pData, String sDescription, String sTitle)
   {
      if (pData != null)
      {
         if (!m_bRunSilent)
         {
            String messageString = "";
            ETList < IMessageData > pSubMessages = pData.getSubMessages();
            long count = 0;
            if (pSubMessages != null)
            {
               count = pSubMessages.size();
            }
            if (count == 0)
            {
               String temp = DefaultCommonDialogResource.getString("IDS_ERROR_DIALOG_TITLE");
               // Use the simple message dialog
               messageString = pData.getFormattedMessageString(false);
               display(messageString, temp);
            }
            else
            {
               /* TODO
               HWND nParent = HWNDRetriever::GetTopMostParent((HWND)parent);
               CMFCComplexErrorDialog dlg(CWnd::FromHandle(nParent));
               
               dlg.put_MessageData(pData);
               
               dlg.DoModal();
               */
            }
         }
      }
   }
   /**
   * Returns the silent flag for this dialog.  If silent then any Display calls will
   * not display a dialog, but rather immediately return S_OK.
   *
   * @param pVal Has this dialog been silenced.
   */
   public boolean isRunSilent()
   {
      boolean bSilent = false;
      if (m_bRunSilent)
      {
         bSilent = true;
      }
      return bSilent;
   }
   /**
   * Sets the silent flag for this dialog.  If silent then any Display calls will
   * not display a dialog, but rather immediately return S_OK;
   *
   * @param newVal Whether or not this dialog should be silent
   */
   public void setIsRunSilent(boolean newVal)
   {
      m_bRunSilent = newVal;
   }
   /**
   * @param message
   * @param string
   * @param icon
   * @param checkboxMsg
   */
   private JCenterDialog createDialog(String message, String title, Icon icon)
   {
      JCenterDialog retVal = null;
      if (m_ParentFrame != null){
	      retVal = new JCenterDialog(m_ParentFrame, true);
      }
      else if (m_ParentDialog != null){
		retVal = new JCenterDialog(m_ParentDialog, true);
      }
      
      if (retVal != null)
      {
      	  if (title == null || title.length() == 0){
      	  	title = DefaultCommonDialogResource.getString("IDS_ERROR");
      	  }
	      retVal.setTitle(title);
	
	      try
	      {
	         JPanel messagePanel = new JPanel();
	         messagePanel.setLayout(new BorderLayout());
	         
	         JFixedSizeTextArea label = new JFixedSizeTextArea(message);
	         label.setOpaque(false);
	         //label.setLineWrap(true);
	         //label.setWrapStyleWord(true);
	         label.setEditable(false);
	         label.setBackground(SystemColor.control);
	         messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	
	         messagePanel.add(label, BorderLayout.CENTER);
	         if (icon != null)
	         {
	            messagePanel.add(new JLabel(icon), BorderLayout.WEST);
	            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
	         }
	         retVal.getContentPane().add(messagePanel);
	         
	         // add button
	         JPanel buttonPanel = new JPanel();
			 //buttonPanel.setLayout(new javax.swing.BoxLayout(buttonPanel, javax.swing.BoxLayout.X_AXIS));
		     //buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
	         retVal.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
				
	         QuestionResponse qr = new QuestionResponse();
	         ActionListener resultListener = new UserResultListener(qr, retVal);
	         JButton butt1 = new JButton(DefaultCommonDialogResource.getString("IDS_OK"));
	         butt1.setActionCommand("OK");
	         butt1.addActionListener(resultListener);
			 butt1.setSize(100, 20);
			 buttonPanel.add(butt1);
	         retVal.getRootPane().setDefaultButton(butt1);
	      }
	      catch (Exception e)
	      {
	         //e.printStackTrace();
	      }
      }
      return retVal;
   }
   //**************************************************
   // Helper Methods
   //**************************************************

   protected Icon getIconForType(int messageType)
   {
      Icon retVal = null;

      if (messageType >= 0 || messageType <= 7)
      {
         switch (messageType)
         {
            case MessageIconKindEnum.EDIK_ICONHAND :
            case MessageIconKindEnum.EDIK_ICONSTOP :
            case MessageIconKindEnum.EDIK_ICONERROR :
               retVal = UIManager.getIcon("OptionPane.errorIcon");
               break;
            case MessageIconKindEnum.EDIK_ICONINFORMATION :
               retVal = UIManager.getIcon("OptionPane.informationIcon");
               break;
            case MessageIconKindEnum.EDIK_ICONEXCLAMATION :
            case MessageIconKindEnum.EDIK_ICONWARNING :
            case MessageIconKindEnum.EDIK_ICONASTERISK :
               retVal = UIManager.getIcon("OptionPane.warningIcon");
               break;
            case MessageIconKindEnum.EDIK_ICONQUESTION :
               retVal = UIManager.getIcon("OptionPane.questionIcon");
               break;
         }
      }
      return retVal;
   }
}
