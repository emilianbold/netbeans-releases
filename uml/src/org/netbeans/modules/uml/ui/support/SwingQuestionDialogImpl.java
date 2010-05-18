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
package org.netbeans.modules.uml.ui.support;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
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
import javax.swing.UIManager;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.UserResultListener;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.CommonDialogResources;
import org.netbeans.modules.uml.ui.support.messaging.IMessenger;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.JFixedSizeTextArea;
import org.netbeans.modules.uml.ui.swing.commondialogs.DefaultCommonDialogResource;

/**
 * 
 * @author Trey Spiva
 */
public class SwingQuestionDialogImpl implements IQuestionDialog
{
   private JCheckBox m_Checkbox = null;
   private boolean m_RunSilent = false;
   private int m_DefaultButton = -1;
	private boolean m_CheckboxIsChecked = false;
   private Frame m_ParentFrame = null;
   private JDialog m_ParentDialog = null;
   private QuestionResponse m_RetVal = null;

   public SwingQuestionDialogImpl()
   {
		initMessaging();
		IProxyUserInterface ui = ProductHelper.getProxyUserInterface(); 
		if (ui != null)
		{
			m_ParentFrame = ui.getWindowHandle();
		}
   }
   public SwingQuestionDialogImpl(Frame pFrame)
   {
		initMessaging();
		m_ParentFrame = pFrame;
   }
   public SwingQuestionDialogImpl(JDialog pDialog)
   {
		initMessaging();
		if (pDialog != null)
		{
			m_ParentDialog = pDialog;
		}
		else
		{
			IProxyUserInterface ui = ProductHelper.getProxyUserInterface(); 
			if (ui != null)
			{
				m_ParentFrame = ui.getWindowHandle();
			}
		}
   }
   private void initMessaging()
   {
	  IMessenger pMsg = ProductHelper.getMessenger();
	  if (pMsg != null)
	  {
		 m_RunSilent = pMsg.getDisableMessaging();
	  }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.IQuestionDialog#displaySimpleQuestionDialogWithCheckbox(int, int, java.lang.String, java.lang.String, int, boolean)
    */
   public QuestionResponse displaySimpleQuestionDialogWithCheckbox(int dialogType, int dialogIcon, String message, String checkboxMsg, int defaultResult, boolean defaultIsChecked)
   {
      return displaySimpleQuestionDialogWithCheckbox(dialogType, dialogIcon, message, checkboxMsg, "", defaultResult, defaultIsChecked);
   }

   /**
    * @param message
    * @param string
    * @param icon
    * @param checkboxMsg
    */
   private JCenterDialog createDialog(String message, String title, Icon icon, String checkboxMsg, int dialogType, QuestionResponse result)
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
			  title = DefaultCommonDialogResource.getString("IDS_QUESTION");
			}
			retVal.setTitle(title);
			retVal.setModal(true);
   	
	      try
	      {
	         if (checkboxMsg != null && checkboxMsg.length() > 0)
	         {
	            JPanel messagePanel = new JPanel();
	            messagePanel.setLayout(new BorderLayout());
	
               message.replaceAll("\\n", System.getProperty("line.separator"));
               
	            JFixedSizeTextArea label = new JFixedSizeTextArea(message);
	            label.setOpaque(false);
	            label.setEditable(false);
	            label.setBackground(SystemColor.control);
	            messagePanel.add(label, BorderLayout.CENTER);
	            messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	            if (icon != null)
	            {
	               messagePanel.add(new JLabel(icon), BorderLayout.WEST);
	               label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
	            }
	            String checkboxText = checkboxMsg;
	            String under = "";
	            int pos = checkboxMsg.indexOf('&');
	            if (pos > -1)
	            {
	               under = checkboxMsg.substring(pos + 1, pos + 2);
	               checkboxText = StringUtilities.replaceAllSubstrings(checkboxMsg, "&", "");
	            }
	            m_Checkbox = new JCheckBox(checkboxText);
	            m_Checkbox.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
	            if (under.length() > 0)
	            {
	               m_Checkbox.setMnemonic(under.charAt(0));
	            }
					m_Checkbox.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							onCheck(evt);
						}
					});
	            
	            messagePanel.add(m_Checkbox, BorderLayout.SOUTH);
	            retVal.getContentPane().add(messagePanel);
	         }
	         else
	         {
	            JPanel messagePanel = new JPanel();
	            messagePanel.setLayout(new BorderLayout());
	                        
	            JFixedSizeTextArea label = new JFixedSizeTextArea(message);
	            label.setOpaque(false);
	            label.setEditable(false);
	            label.setBackground(SystemColor.control);            
	            messagePanel.add(label, BorderLayout.CENTER);
	            if (icon != null)
	            {
	               messagePanel.add(new JLabel(icon), BorderLayout.WEST);
	               label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
	            }
	            messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	            retVal.getContentPane().add(messagePanel, BorderLayout.CENTER);
	         }
	
	         addButtons(dialogType, result, retVal);
	      }
	      catch (Exception e)
	      {
	         e.printStackTrace();
	      }
		}
      return retVal;
   }

   protected void addButtons(int dialogType, QuestionResponse result, JDialog dialog)
   {
      JPanel buttonPanel = new JPanel();
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
      dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

      ActionListener resultListener = new UserResultListener(result, dialog);
      switch (dialogType)
      {
         case MessageDialogKindEnum.SQDK_OK :
            {
               JButton defaultBtn = createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.OK_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.OK_BTN_NAME"), resultListener); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(defaultBtn);
               
               dialog.getRootPane().setDefaultButton(defaultBtn);
               break;
            }
         case MessageDialogKindEnum.SQDK_ABORTRETRYIGNORE :
            {
               JButton defaultBtn = createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.ABORT_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.ABORT_BTN_NAME"), resultListener); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(defaultBtn);
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.RETRY_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.RETRY_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.IGNORE_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.IGNORE_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               
               dialog.getRootPane().setDefaultButton(defaultBtn);
               break;
            }
         case MessageDialogKindEnum.SQDK_OKCANCEL :
            {
               JButton defaultBtn = createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.OK_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.OK_BTN_NAME"), resultListener); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(defaultBtn);
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.CANCEL_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.CANCEL_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               
               dialog.getRootPane().setDefaultButton(defaultBtn);
               break;
            }
         case MessageDialogKindEnum.SQDK_RETRYCANCEL :
            {
               JButton defaultBtn = createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.RETRY_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.RETRY_BTN_NAME"), resultListener); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(defaultBtn);
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.CANCEL_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.CANCEL_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               
               dialog.getRootPane().setDefaultButton(defaultBtn);
               break;
            }
         case MessageDialogKindEnum.SQDK_YESNO :
            {
               JButton defaultBtn = createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.YES_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.YES_BTN_NAME"), resultListener); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(defaultBtn);
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.NO_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.NO_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               
               dialog.getRootPane().setDefaultButton(defaultBtn);
               break;
            }
         case MessageDialogKindEnum.SQDK_YESNOCANCEL :
            {
               JButton defaultBtn = createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.YES_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.YES_BTN_NAME"), resultListener); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(defaultBtn);
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.NO_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.NO_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.CANCEL_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.CANCEL_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               
               dialog.getRootPane().setDefaultButton(defaultBtn);
               break;
            }
         case MessageDialogKindEnum.SQDK_YESNOALWAYS :
            {
               JButton defaultBtn = createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.YES_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.YES_BTN_NAME"), resultListener); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(defaultBtn);
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.NO_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.NO_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.ALWAYS_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.ALWAYS_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               
               dialog.getRootPane().setDefaultButton(defaultBtn);
               break;
            }
         case MessageDialogKindEnum.SQDK_YESNONEVER :
            {
               JButton defaultBtn = createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.YES_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.YES_BTN_NAME"), resultListener); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(defaultBtn);
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.NO_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.NO_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               buttonPanel.add(createActionButton(CommonDialogResources.getString("SwingQuestionDialogImpl.NEVER_BTN_TITLE"), CommonDialogResources.getString("SwingQuestionDialogImpl.NEVER_BTN_NAME"), resultListener)); //$NON-NLS-1$ //$NON-NLS-2$
               
               dialog.getRootPane().setDefaultButton(defaultBtn);
               break;
            }
      }
   }

   protected JButton createActionButton(String displayName, String command, ActionListener listener)
   {
		// figure out if there should be hot keys for the button text
		String buttonText = displayName;
		String under = "";
		int pos = displayName.indexOf('&');
		if (pos > -1)
		{
			under = displayName.substring(pos + 1, pos + 2);
			buttonText = StringUtilities.replaceAllSubstrings(displayName, "&", "");
		}
      JButton retVal = new JButton(buttonText);
      retVal.setActionCommand(command);
      retVal.addActionListener(listener);
		if (under.length() > 0)
		{
			retVal.setMnemonic(under.charAt(0));
		}
      return retVal;
   }

   /**
    * Displays a simple question dialog.  Note that since QuestionDialog implements
    * the ISilentDialog interface, it may be silent.  In that case, no dialog is shown 
    * and S_OK is returned.
    *
    * @param nDialogType[in] The type of the dialog
    * @param nErrorDialogIcon[in] The icon to be shown
    * @param sMessageString[in] The message presented to the user
    * @param nDefaultResult[in] The default result.  Returned if the dialog is silent
    * @param nResult[out] The pushbutton the user selected
    * @param parent[in] The parent HWND for the dialog
    * @param sTitle[in] The dialog title
    * @see org.netbeans.modules.uml.ui.support.IQuestionDialog#displaySimpleQuestionDialogWithCheckbox(int, int, java.lang.String, java.lang.String, java.lang.String, int, boolean)
    */
   public QuestionResponse displaySimpleQuestionDialogWithCheckbox(int dialogType, int dialogIcon, String message, String checkboxMsg, String title, int defaultResult, boolean defaultIsChecked)
   {
      m_RetVal = new QuestionResponse(defaultIsChecked, defaultResult);

      if ((message != null) && (message.length() > 0))
      {
         if (isRunSilent() == true)
         {
            m_RetVal.setResult(defaultResult);
         }
         else
         {
            // With Swing if you use NULL as the parent it will use the active
            // window.

            Icon icon = getIconForType(dialogIcon);
            JCenterDialog dialog = createDialog(message, title, icon, checkboxMsg, dialogType, m_RetVal);
            if(m_Checkbox != null)
            {
               m_Checkbox.setSelected(defaultIsChecked);
            }
				determineDefaultResult(dialogType, m_Checkbox);            
            if (dialog != null)
            {
               dialog.pack();
               Insets insets = dialog.getInsets();
               insets.top = 5;
               insets.left = 5;
               insets.bottom = 5;
               insets.right = 5;

               dialog.doLayout();

					if (m_Checkbox != null)
					{
						setCheckboxIsChecked(m_Checkbox.isSelected());
						m_RetVal.setChecked(m_Checkbox.isSelected());
					}

					if (m_ParentFrame != null){
						dialog.center(m_ParentFrame);
					}
					else if (m_ParentDialog != null){
						dialog.center(m_ParentDialog);
					}
					dialog.setVisible(true);
            }
         }
      }

      return m_RetVal;
   }

   /**
   * Returns the silent flag for this dialog.  If silent then any Display calls will
   * not display a dialog, but rather immediately return S_OK;
   *
   * @param pVal Has this dialog been silenced
   */
   public boolean isRunSilent()
   {
      return m_RunSilent || ProductHelper.getMessenger().getDisableMessaging();
   }

   /**
   * Sets the silent flag for this dialog.  If silent then any Display calls will
   * not display a dialog, but rather immediately return S_OK;
   *
   * @param newVal Whether or not this dialog should be silent.
   */
   public void setIsRunSilent(boolean value)
   {
      m_RunSilent = value;
   }

   /**
   * The default button (ie IDOK).  See the return values for AfxMessageBox.
   */
   public void setDefaultButton(int nButton)
   {
      m_DefaultButton = nButton;
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
               retVal = UIManager.getIcon(CommonDialogResources.getString("SwingQuestionDialogImpl.ERROR_ICON")); //$NON-NLS-1$
               break;
            case MessageIconKindEnum.EDIK_ICONINFORMATION :
               retVal = UIManager.getIcon(CommonDialogResources.getString("SwingQuestionDialogImpl.INFORMATION_ICON")); //$NON-NLS-1$
               break;
            case MessageIconKindEnum.EDIK_ICONEXCLAMATION :
            case MessageIconKindEnum.EDIK_ICONWARNING :
            case MessageIconKindEnum.EDIK_ICONASTERISK :
               retVal = UIManager.getIcon(CommonDialogResources.getString("SwingQuestionDialogImpl.WARNING_ICON")); //$NON-NLS-1$
               break;
            case MessageIconKindEnum.EDIK_ICONQUESTION :
               retVal = UIManager.getIcon(CommonDialogResources.getString("SwingQuestionDialogImpl.QUESTION_ICON")); //$NON-NLS-1$
               break;
         }
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog#displaySimpleQuestionDialog(int, int, java.lang.String, int, java.awt.Window, java.lang.String)
    */
   public QuestionResponse displaySimpleQuestionDialog(int dialogType, int errorDialogIcon, String messageString, int defaultResult, Window parent, String title)
   {
	  m_RetVal = new QuestionResponse();

	if ((messageString != null) && (messageString.length() > 0))
	{
	   if (isRunSilent() == true)
	   {
		  m_RetVal.setResult(defaultResult);
	   }
	   else
	   {
		  // With Swing if you use NULL as the parent it will use the active
		  // window.

		  Icon icon = getIconForType(errorDialogIcon);
		  JCenterDialog dialog = createDialog(messageString, title, icon, "", dialogType, m_RetVal);
		  determineDefaultResult(dialogType, m_Checkbox);            
		  if (dialog != null)
		  {
			 dialog.pack();
			 Insets insets = dialog.getInsets();
			 insets.top = 5;
			 insets.left = 5;
			 insets.bottom = 5;
			 insets.right = 5;

			  dialog.doLayout();
			  
			if (m_ParentFrame != null){
				dialog.center(m_ParentFrame);
			}
			else if (m_ParentDialog != null){
				dialog.center(m_ParentDialog);
			}
			dialog.setVisible(true);
		  }
	   }
	}
	return m_RetVal;
   }
   public boolean getCheckboxIsChecked()
   {
      return m_CheckboxIsChecked;
   }
   public void setCheckboxIsChecked(boolean newVal)
   {
      m_CheckboxIsChecked = newVal;
   }
   
   protected void centerDialog(JDialog dialog)
   {
      if(dialog != null)
      {
         Container parent = dialog.getParent();
         Rectangle bounds = parent.getBounds();
      
         double centerX = bounds.getCenterX();
         double centerY = bounds.getCenterY();
      
         Rectangle dialogBounds = dialog.getBounds();
         double xPos = centerX - (dialogBounds.getWidth() / 2);
         double yPos = centerY - (dialogBounds.getHeight() / 2);
      
         dialog.setLocation((int)xPos, (int)yPos);
      }
   }
   
	private void onCheck(java.awt.event.ActionEvent evt) {
		Object obj = evt.getSource();
		if (obj instanceof JCheckBox)
		{
			JCheckBox box = (JCheckBox)obj;
			boolean checkboxState = box.isSelected();
			if (checkboxState)
			{
				setCheckboxIsChecked(true);
				m_RetVal.setChecked(true);
			}
			else
			{
				setCheckboxIsChecked(false);
				m_RetVal.setChecked(false);
			}
		}
	}
	/*
	 * We needed a way to set up the result that should get passed back to the user.  This was already
	 * working if we were in silent mode, because we use what is passed into the dialog.  We were running
	 * into problems where we were using what was passed in, but the user clicked "x" or escaped out of
	 * the dialog.  In these cases, because no buttons were pressed, we used the passed in default, which
	 * is probably not what we want.
	 */
	private void determineDefaultResult(int dialogType, JCheckBox checkbox)
	{
		switch (dialogType)
		{
			case MessageDialogKindEnum.SQDK_OK :
				{
					m_RetVal.setResult(SimpleQuestionDialogResultKind.SQDRK_RESULT_OK);
					break;
				}
			case MessageDialogKindEnum.SQDK_ABORTRETRYIGNORE :
				{
					m_RetVal.setResult(SimpleQuestionDialogResultKind.SQDRK_RESULT_IGNORE);
					break;
				}
			case MessageDialogKindEnum.SQDK_OKCANCEL :
				{
					m_RetVal.setResult(SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL);
					break;
				}
			case MessageDialogKindEnum.SQDK_RETRYCANCEL :
				{
					m_RetVal.setResult(SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL);
					break;
				}
			case MessageDialogKindEnum.SQDK_YESNO :
				{
					// We are assuming that if there is a checkbox present on the dialog, it is a preference
					// controlled question dialog, so we need to react a little differently.  The default in
					// this case could not be "no" because that could then set the preference to never if the
					// user checked the box and then hit "x" or "escape".
					if (checkbox != null)
					{
						m_RetVal.setResult(SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL);
					}
					else
					{
						m_RetVal.setResult(SimpleQuestionDialogResultKind.SQDRK_RESULT_NO);
					}
					break;
				}
			case MessageDialogKindEnum.SQDK_YESNOCANCEL :
				{
					m_RetVal.setResult(SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL);
					break;
				}
			case MessageDialogKindEnum.SQDK_YESNOALWAYS :
				{
					m_RetVal.setResult(SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL);
					break;
				}
			case MessageDialogKindEnum.SQDK_YESNONEVER :
				{
					m_RetVal.setResult(SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL);
					break;
				}
		}
	}
   
}
