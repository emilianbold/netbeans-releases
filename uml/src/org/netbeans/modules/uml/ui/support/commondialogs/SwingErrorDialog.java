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
