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
 * SwingErrorDialog.java
 *
 * Created on July 26, 2004, 12:22 PM
 */

package org.netbeans.modules.uml.ui.support.commondialogs;

import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessageData;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author  Trey Spiva
 */
public class SwingErrorDialog  implements IErrorDialog
{
   private boolean m_RunSilent = false;
   private Component m_Parent = null;
   
   /** Creates a new instance of SwingErrorDialog */
   public SwingErrorDialog()
   {
      IProxyUserInterface ui = ProductHelper.getProxyUserInterface(); 
		if (ui != null)
		{
			m_Parent = ui.getWindowHandle();
		}
   }
   
   public SwingErrorDialog(Frame parent)
   {
      m_Parent = parent;
      if(parent == null)
      {
         IProxyUserInterface ui = ProductHelper.getProxyUserInterface(); 
         if (ui != null)
         {
            m_Parent = ui.getWindowHandle();
         }
      }
   }
   
   public SwingErrorDialog(JDialog parent)
   {
      m_Parent = parent;
      if(parent == null)
		{
			IProxyUserInterface ui = ProductHelper.getProxyUserInterface(); 
			if (ui != null)
			{
				m_Parent = ui.getWindowHandle();
			}
		}
   }
   
   public void display(String sErrorString, String sTitle)
   {
      if((sErrorString.length() > 0) && (isRunSilent() == true))
      {
         JOptionPane.showMessageDialog(m_Parent, sErrorString, sTitle, 
                                       JOptionPane.ERROR_MESSAGE);
      }
   }
   
   public void display(String sErrorString, int nErrorDialogIcon, String sTitle)
   {
      if((sErrorString.length() > 0) && (isRunSilent() == true))
      {
         Icon icon = getIconForType(nErrorDialogIcon);
         if(icon != null)
         {
            JOptionPane.showMessageDialog(m_Parent, sErrorString, sTitle, 
                                          JOptionPane.ERROR_MESSAGE, icon);
         }
         else
         {
            JOptionPane.showMessageDialog(m_Parent, sErrorString, sTitle, 
                                          JOptionPane.ERROR_MESSAGE);
         }
      }
   }
   
   public void display(IMessageData pData, String sDescription, String sTitle)
   {
      if((pData != null) && (isRunSilent() == true))
      {
         ETList<IMessageData> subMessages = pData.getSubMessages();
         if(subMessages.size() == 0)
         {
            String messageString = pData.getFormattedMessageString(false);
            JOptionPane.showMessageDialog(m_Parent, messageString, sTitle, 
                                          JOptionPane.ERROR_MESSAGE);
         }
         else
         {
            displayComplexErrorDialog(pData, sDescription, sTitle);
         }
      }
   }   
   
   public boolean isRunSilent()
   {
      return m_RunSilent;
   }
   
   public void setIsRunSilent(boolean value)
   {
      m_RunSilent = value;
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

   protected void displayComplexErrorDialog(IMessageData pData, 
                                            String sDescription, 
                                            String sTitle)
   {
//      JPanel messagePane = new JPanel();
//      messagePane.setLayout(new BorderLayout());
//      
//      if((sDescription != null) && (sDescription.length() > 0))
//      {
//         JTextArea txt = new JTextArea(sDescription);
//         txt.setLineWrap(true);
//         txt.setOpaque(false);
//         txt.setEnabled(false);
//         txt.setWrapStyleWord(true);
//         messagePane.add(txt, BorderLayout.NORTH);
//      }
//      messagePane.add(new JTree(createTreeModel(pData)), BorderLayout.CENTER);
//      
//      Box buttonPane = Box.createHorizontalBox();
//      
//      JButton saveButton = new JButton("Save Log...");
//      buttonPane.add(saveButton);
//      
//      buttonPane.add(Box.createHorizontalGlue());
//      
//      JButton closeButton = new JButton("Close");
//      buttonPane.add(closeButton);
//      
//      Box checkBoxPane = Box.createVerticalBox();
//      checkBoxPane.add(checkBox);
//      checkBoxPane.add(Box.createVerticalGlue());
   }
}
