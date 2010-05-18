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



package org.netbeans.modules.uml.ui.support.messaging;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.w3c.dom.css.Rect;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.support.ProgressDialogMessageKind;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogDisplayEnum;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

public abstract class ProgressDialogImp extends JCenterDialog implements IProgressExecutor
{
 //private static final Dimension COLLAPSED_SIZE = new Dimension(600, 220);
 //private static final Dimension EXPANDED_SIZE = new Dimension(600, 415);
 private Dimension COLLAPSED_SIZE = new Dimension(600, 220);
 private Dimension EXPANDED_SIZE = new Dimension(600, 415);

 private static final int SMALL_INFO = 0;
 private static final int SMALL_ERROR = 1;
 private static final int SMALL_MESSAGE = 2;
 private static final int LARGE_MESSAGE = 3;
 private static final int LARGE_ERROR = 4;
 private static final int LARGE_INFO = 5;
 private static final int ATTRIBUTE = 6;
 private static final int OPERATION = 7;

 private static int m_NextCookie;
 private String m_Title = " ";
 private int m_Lower = 0;
 private int m_Upper = 100;
 private int m_Increment = 1;
 private int m_CurMode = MessageDialogDisplayEnum.MMK_NONE;

 private IProgressController m_Controller = null; // The owning object that controls lifetime

 private List m_ImageList = null;

 private DefaultMutableTreeNode m_RootNode = null;
 private DefaultMutableTreeNode m_CurGroup = null;
 private DefaultMutableTreeNode m_CurFirst = null;
 private DefaultMutableTreeNode m_CurSecond = null;
 private JTree m_MessageTree = null;
 private DefaultTreeModel m_TreeModel = null;

 private boolean m_Cancelled = true;
 private String m_LogFile = "GDResults.txt"; // Default name of the log file
 private String m_DefExt = "*.txt"; // Default extension. Usually .txt
 private boolean m_Collapsed = true; // true if the dialog should be in its smaller state

 private boolean m_SavingMessages = false;

 private IProgressExecutor m_Executor = null;
 private long m_RevokeNum = 0;

 //	CStatic	m_FinalResult;
 //	CStatic	m_Static;

 /// Used to make sure there is no other user input while the progress dialog is displayed
 private IGUIBlocker m_cpDiagramBlocker = null;

 private boolean m_CloseWhenDone = false;

 private int lastButtonPressed;

 private JLabel m_FirstField = new JLabel();
 private JLabel m_SecondField = new JLabel();
 private JLabel m_ThirdField = new JLabel();

 private JButton m_CancelButton = new JButton();
 private JButton m_SaveMessageButton = new JButton();

 private JCheckBox cbCloseWhenDone = new JCheckBox();
 private JProgressBar m_ProgressIndicator = new JProgressBar();
 private JLabel m_GroupTitle = new JLabel();

 private JPanel pnlContents = new JPanel();
 private JPanel pnlTop = new JPanel();
 private JPanel pnlProgressMsgs = new JPanel();
 private JPanel pnlCenter = new JPanel();
 private JPanel pnlBottom = new JPanel();
 private JPanel pnlMessageCenter = new JPanel();
 private JLabel lblMessageCenter = new JLabel();
 private JPanel pnlCheckBox = new JPanel();
 private JPanel pnlButtons = new JPanel();
 private JLabel lblDummy = new JLabel();
 private JPanel pnlCancel = new JPanel();
 private JPanel pnlSaveMessage = new JPanel();
 private Frame m_ParentFrame = null;

 public ProgressDialogImp(
    Frame pParent,
    int pCurMode,
    IProgressController pController,
    int pLower,
    int pUpper,
    int pIncrement,
    DefaultMutableTreeNode pCurGroup,
    DefaultMutableTreeNode pCurFirst,
    DefaultMutableTreeNode pCurSecond,
    boolean pCancelled,
    String pLogFile,
    String pDefExt,
    boolean pCollapsed,
    int pRevokeNum)
 {

    this(pParent, MessagingResources.getString("IDS_PROGRESS"), false);

    m_CurMode = pCurMode;
    m_Controller = pController;
    m_Lower = pLower;
    m_Upper = pUpper;
    m_Increment = pIncrement;
    m_CurGroup = pCurGroup;
    m_CurFirst = pCurFirst;
    m_CurSecond = pCurSecond;
    m_Cancelled = pCancelled;
    m_LogFile = m_LogFile != null ? m_LogFile : "GDResults.txt";
    m_DefExt = m_LogFile != null ? m_LogFile : "*.txt";
    m_Collapsed = pCollapsed;
    m_RevokeNum = pRevokeNum;

    m_FirstField.setText(" ");
    m_SecondField.setText(" ");
    m_ThirdField.setText(" ");
    m_CloseWhenDone = false;
 }

 public ProgressDialogImp(Frame frame, String title, boolean modal)
 {
    super(frame, title, modal);

    m_ParentFrame = frame;

    try
    {
       createUI();
       pack();
       this.setSize(COLLAPSED_SIZE);
       //CBeckham - added this code to allow dialog to dynamiclaly resize for larger fonts
       setCollapsedPanelSize();
       setExpandedPanelSize();
       // end of add
       setCollapse( true );
       this.setResizable(true);

       if (m_ParentFrame != null)
       {
          this.center(m_ParentFrame);
       }
       else
       {
          // center on screen
          Dimension SS = this.getToolkit().getScreenSize();
          Dimension CS = this.getSize();
          this.setLocation((SS.width - CS.width) / 2, (SS.height - CS.height) / 2);
       }

       this.updateData(false);
    }
    catch (Exception ex)
    {
       ex.printStackTrace();
    }
 }

 public ProgressDialogImp()
 {
    this(null, MessagingResources.getString("IDS_PROGRESS"), false);
 }

 private void initMessageTree()
 {
    m_RootNode = new DefaultMutableTreeNode(MessagingResources.getString("IDS_PROGRESSINFORMATION"));
    m_TreeModel = new DefaultTreeModel(m_RootNode);
    m_MessageTree.setModel(m_TreeModel);
 }

 private void createUI() throws Exception
 {

    getAccessibleContext().setAccessibleDescription(MessagingResources.getString("ACSD_ProgressDialog"));

 	  m_RootNode = new DefaultMutableTreeNode(MessagingResources.getString("IDS_PROGRESSINFORMATION"));
    m_TreeModel = new DefaultTreeModel(m_RootNode);

    m_MessageTree = new JTree(m_TreeModel);
    m_MessageTree.getAccessibleContext().setAccessibleName(MessagingResources.getString("ACSN_MessageTree"));
    m_MessageTree.getAccessibleContext().setAccessibleDescription(MessagingResources.getString("ACSD_MessageTree"));

    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    m_MessageTree.setCellRenderer(renderer);

    m_MessageTree.setEditable(false);
    m_MessageTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    m_MessageTree.setShowsRootHandles(true);

    pnlContents.setLayout(new BorderLayout());
    pnlContents.setMaximumSize(new Dimension(-1, -1));
    pnlContents.setMinimumSize(new Dimension(-1, -1));
    pnlContents.setPreferredSize(new Dimension(-1, -1));

    pnlTop.setLayout(new GridBagLayout());
    pnlTop.setMinimumSize(new Dimension(591, 150));
    pnlTop.setPreferredSize(new Dimension(591, 150));

    pnlProgressMsgs.setLayout(new GridBagLayout());
    pnlProgressMsgs.setBorder(BorderFactory.createEtchedBorder());
    pnlProgressMsgs.setMinimumSize(new Dimension(50, 50));
    pnlProgressMsgs.setPreferredSize(new Dimension(50, 50));

    m_FirstField.setFont(new java.awt.Font("SansSerif", 0, 10));
    m_FirstField.setBorder(BorderFactory.createLoweredBevelBorder());
    m_FirstField.setMaximumSize(new Dimension(495, 18));
    m_FirstField.setMinimumSize(new Dimension(495, 18));
    m_FirstField.setPreferredSize(new Dimension(495, 18));

    m_SecondField.setFont(new java.awt.Font("SansSerif", 0, 10));
    m_SecondField.setBorder(BorderFactory.createLoweredBevelBorder());
    m_SecondField.setMaximumSize(new Dimension(495, 18));
    m_SecondField.setMinimumSize(new Dimension(495, 18));
    m_SecondField.setPreferredSize(new Dimension(495, 18));

    m_ThirdField.setFont(new java.awt.Font("Dialog", 0, 10));
    m_ThirdField.setBorder(BorderFactory.createLoweredBevelBorder());
    m_ThirdField.setMaximumSize(new Dimension(495, 18));
    m_ThirdField.setMinimumSize(new Dimension(495, 18));
    m_ThirdField.setPreferredSize(new Dimension(495, 18));

    m_GroupTitle.setOpaque(true);
    // Fix J2638:  Display a blank message for the group box
    m_GroupTitle.setText( "" );

//    m_SaveMessageButton.setMinimumSize(new Dimension(130, 25));
//    m_SaveMessageButton.setPreferredSize(new Dimension(130, 25));
    m_SaveMessageButton.setText(MessagingResources.determineText(MessagingResources.getString("IDS_SAVEMESSAGE")));
    MessagingResources.setMnemonic(m_SaveMessageButton, MessagingResources.getString("IDS_SAVEMESSAGE"));
    m_SaveMessageButton.getAccessibleContext().setAccessibleDescription(MessagingResources.getString("ACSD_SaveMessage"));

//    m_CancelButton.setMaximumSize(new Dimension(75, 27));
//    m_CancelButton.setMinimumSize(new Dimension(75, 25));
//    m_CancelButton.setPreferredSize(new Dimension(75, 25));
    m_CancelButton.setText(MessagingResources.determineText(MessagingResources.getString("IDS_CANCEL")));
    MessagingResources.setMnemonic(m_CancelButton, MessagingResources.getString("IDS_CANCEL"));
    m_CancelButton.getAccessibleContext().setAccessibleDescription(MessagingResources.getString("ACSD_Cancel"));

    pnlBottom.setLayout(new BorderLayout());
    pnlBottom.setMinimumSize(new Dimension(591, 45));
    pnlBottom.setPreferredSize(new Dimension(591, 45));

    cbCloseWhenDone.setText(MessagingResources.getString("IDS_CLOSEWHENDONE"));
    cbCloseWhenDone.setVisible(false);

    pnlMessageCenter.setBorder(BorderFactory.createEtchedBorder());
    pnlMessageCenter.setLayout(new GridBagLayout());
    pnlCenter.setLayout(new GridBagLayout());
    lblMessageCenter.setText(MessagingResources.determineText(MessagingResources.getString("IDS_MESSAGECENTER")));
    MessagingResources.setMnemonic(lblMessageCenter, MessagingResources.getString("IDS_MESSAGECENTER"));
    lblMessageCenter.setOpaque(true);
    lblMessageCenter.setLabelFor(m_MessageTree);
    m_MessageTree.setBorder(BorderFactory.createLoweredBevelBorder());
    m_ProgressIndicator.setToolTipText("");
    pnlCenter.setMinimumSize(new Dimension(-1, -1));
    pnlCenter.setPreferredSize(new Dimension(-1, -1));
    pnlButtons.setLayout(new GridBagLayout());
//    pnlButtons.setMinimumSize(new Dimension(250, 32));
//    pnlButtons.setPreferredSize(new Dimension(250, 32));
    lblDummy.setText("     ");
//    pnlCancel.setMinimumSize(new Dimension(85, 40));
//    pnlCancel.setPreferredSize(new Dimension(85, 40));
    pnlCancel.setLayout(new FlowLayout());
//    pnlSaveMessage.setMinimumSize(new Dimension(135, 40));
//    pnlSaveMessage.setPreferredSize(new Dimension(135, 40));
    pnlSaveMessage.setLayout(new FlowLayout());
    pnlCheckBox.add(lblDummy, null);

    this.getContentPane().add(pnlContents, BorderLayout.CENTER);

    pnlContents.add(pnlTop, BorderLayout.NORTH);
    pnlProgressMsgs.add(m_FirstField, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(18, 30, 0, 24), 100, 0));
    pnlProgressMsgs.add(m_SecondField, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 30, 0, 24), 100, 0));
    pnlProgressMsgs.add(m_ThirdField, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 30, 15, 24), 100, 0));
    pnlTop.add(m_GroupTitle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 30, 85, 252), 0, 0));
    pnlTop.add(m_ProgressIndicator, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(7, 20, 9, 18), 403, 0));
    pnlTop.add(pnlProgressMsgs, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(23, 20, 0, 18), 503, 45));
    pnlContents.add(pnlCenter, BorderLayout.CENTER);
    pnlCenter.add(lblMessageCenter, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 30, 173, 352), 0, 0));
    pnlCenter.add(pnlMessageCenter, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(7, 20, 3, 18), 0, 0));
    pnlMessageCenter.add(new JScrollPane(m_MessageTree), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(17, 9, 8, 10), 448, 83));

    pnlContents.add(pnlBottom, BorderLayout.SOUTH);
    pnlBottom.add(pnlCheckBox, BorderLayout.WEST);
    pnlCheckBox.add(cbCloseWhenDone, null);
    pnlBottom.add(pnlButtons, BorderLayout.EAST);
    pnlButtons.add(pnlSaveMessage, new GridBagConstraints(0, 0, 1, 1, 5.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(25, 338, 25, 0), 0, 0));
    pnlSaveMessage.add(m_SaveMessageButton, null);
    pnlButtons.add(pnlCancel, new GridBagConstraints(1, 0, 1, 1, 5.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(25, 5, 25, 12), 0, 0));
    pnlCancel.add(m_CancelButton, null);

    this.addActionListeners();

    m_FirstField.setText("  ");
    m_SecondField.setText("  ");
    m_ThirdField.setText("  ");
 }

 private void addActionListeners()
 {
    m_SaveMessageButton.addActionListener(new java.awt.event.ActionListener()
    {
       public void actionPerformed(ActionEvent e)
       {
          m_SaveMessageButton_actionPerformed(e);
       }
    });

    m_CancelButton.addActionListener(new java.awt.event.ActionListener()
    {
       public void actionPerformed(ActionEvent e)
       {
          m_CancelButton_actionPerformed(e);
       }
    });

    cbCloseWhenDone.addActionListener(new java.awt.event.ActionListener()
    {
       public void actionPerformed(ActionEvent e)
       {
          cbCloseWhenDone_actionPerformed(e);
       }
    });
 }

 void m_CancelButton_actionPerformed(ActionEvent e)
 {
    lastButtonPressed = IProgressDialog.FINISH;

    if (m_CancelButton.getText().equals(MessagingResources.determineText(
            MessagingResources.getString("IDS_DONE"))))
    {
       onCancel();
    }
    else if (m_CancelButton.getText().equals(MessagingResources.determineText(
            MessagingResources.getString("IDS_CANCEL"))))
    {
       onCancel();
    }
 }

 void m_SaveMessageButton_actionPerformed(ActionEvent e)
 {
    onSaveMessages();
 }

 void cbCloseWhenDone_actionPerformed(ActionEvent e)
 {
    this.m_CloseWhenDone = this.cbCloseWhenDone.isSelected();
 }

 /**
  * Sets the owner of this dialog.  This method explicitily does not up the refcount on comDiag. Object ownership
  * lies in comDiag, i.e., comDiag owns this object, so to up the refcount on comDiag would put us into
  * a circular dependency.
  *
  * @param comDiag [in] The owner - the COM interface that is using this MFC Dialog
  * @param cookie  [in] The cookie that RevokeActiveObject returns.
  */
 public void advise(IProgressController comDiag, long cookie)
 {
    m_Controller = comDiag;

    // m_RevokeNum is the cookie that RegisterActiveObject returns.  It
    // will be used to call RevokeActiveObject during the clean up.
    m_RevokeNum = cookie;
 }

 /**
  * Tells our owner that a cancel has occurred
  */
 public void broadCastCancel()
 {
    try
    {
       if (m_Controller != null)
       {
          m_Controller.onProgressEnd();
       }
       flushEventQueue();
    }
    catch (Exception e)
    {
       Log.stackTrace(e);
    }
 }

 /**
  * Flushes the event queue
  */
 private void flushEventQueue()
 {
    try
    {
       if (!this.m_Cancelled && !m_SavingMessages)
       {
          repaint();
          //updateData(false);
          updateData(true);
       }
    }
    catch(Exception e)
    {
       Log.stackTrace(e);
    }
 }

 //CBeckham - added this code to allow dialog to dynamiclaly resize for larger fonts
 private void setCollapsedPanelSize() {
      int fontsize = getFont().getSize();
      int width  = 600;
      int height = 220;
      int multiplyer = 1;

      if (fontsize > 17) multiplyer = 3;
      width  = width  + Math.round(width*(multiplyer*fontsize/100f));
      height = height + Math.round(height*(multiplyer*fontsize/100f));
      COLLAPSED_SIZE = new Dimension(width, height);

  }

 //CBeckham - added this code to allow dialog to dynamiclaly resize for larger fonts
 private void setExpandedPanelSize() {

      int fontsize = getFont().getSize();
      int width  = 600;
      int height = 415;
      int multiplyer = 1;

      if (fontsize > 17) multiplyer = 3;
      width  = width  + Math.round(width*(multiplyer*fontsize/100f));
      height = height + Math.round(height*(multiplyer*fontsize/100f));

     EXPANDED_SIZE = new Dimension(width, height);
  }

 private void collapseDialog(boolean collapse)
 {
    if (collapse)
    {
       pnlContents.remove(pnlCenter);
       this.setSize(COLLAPSED_SIZE);
       m_SaveMessageButton.setVisible(false);
    m_CancelButton.setText(MessagingResources.determineText(MessagingResources.getString("IDS_CANCEL")));
    MessagingResources.setMnemonic(m_CancelButton, MessagingResources.getString("IDS_CANCEL"));
    }
    else
    {
       pnlContents.add(pnlCenter, BorderLayout.CENTER);
       this.setSize(EXPANDED_SIZE);
       m_SaveMessageButton.setVisible(true);
       m_CancelButton.setText(MessagingResources.determineText(MessagingResources.getString("IDS_DONE")));
       MessagingResources.setMnemonic(m_CancelButton, MessagingResources.getString("IDS_DONE"));
       m_TreeModel.reload(m_RootNode);
    }

    this.validate();
    this.repaint();
 }

 private int doModal()
 {
//    this.setModal(true);
    super.show();
    return this.lastButtonPressed;
 }

 //	Returns true if current position is equal to or greater than
 //	the progress controlls maximum range
 private boolean isProgressIndicatorAtMaximumPosition()
 {
    boolean atMax = false;

    int currentPos = m_ProgressIndicator.getValue();

    if (currentPos >= m_Upper)
    {
       atMax = true;
    }

    return atMax;
 }

 private void setProgressIndicatorToMaximum()
 {
     m_ProgressIndicator.setValue(m_Upper);
 }
 
 /**
  * Gets an image for use when this message gets inserted into the tree
  */
 private int retrieveImage(int type, boolean smallIcon)
 {
    int image = -1;

    switch (type)
    {
       case ProgressDialogMessageKind.PDMK_MESSAGE :
          image = smallIcon ? SMALL_MESSAGE : LARGE_MESSAGE;
          break;

       case ProgressDialogMessageKind.PDMK_INFO :
          image = smallIcon ? SMALL_INFO : LARGE_INFO;
          break;

       case ProgressDialogMessageKind.PDMK_ERROR :
          image = smallIcon ? SMALL_ERROR : LARGE_ERROR;
          break;

       case ProgressDialogMessageKind.PDMK_ATTRIBUTE :
          image = ATTRIBUTE;
          break;

       case ProgressDialogMessageKind.PDMK_OPERATION :
          image = OPERATION;
          break;
    }

    return image;
 }

 /**
  * Inserts a node into the tree if the current group has been set
  */
 private DefaultMutableTreeNode establishFirstNode(int type, String first)
 {
    DefaultMutableTreeNode node = null;

    if (m_CurGroup != null)
    {
       node = establishNode(type, first, retrieveImage(type, true), m_CurFirst, m_CurGroup);
    }

    return node;
 }

 /**
  * Inserts a node into the tree if the current group has been set
  */
 private DefaultMutableTreeNode establishSecondNode(int type, String second)
 {
    DefaultMutableTreeNode node = null;

    if (m_CurFirst != null)
    {
       node = establishNode(type, second, retrieveImage(type, true), m_CurSecond, m_CurFirst);
    }

    return node;
 }

 /**
  * Inserts a group node into the tree.
  */
 private DefaultMutableTreeNode establishGroupNode(int type, String group)
 {
     //	This block is added to solve the problem J2525 which is an timing issue.
     try
     {
         if(m_RootNode.getChildCount() == 0)
         {
             Thread.sleep(100);
         }
     }
     catch (InterruptedException e)
     {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
     
     DefaultMutableTreeNode retVal = establishNode(type, group, 
                                                   retrieveImage(type, false), 
                                                   m_CurGroup, m_RootNode);
     
     if(group.length() > 0)
     {
         m_CurFirst = null;
         m_CurSecond = null;
     }
     
     return retVal;
 }

 /**
  * Inserts the node into the tree.
  */
 private DefaultMutableTreeNode establishNode(int type, String name, int image, DefaultMutableTreeNode persistNode, DefaultMutableTreeNode parent)
 {
    DefaultMutableTreeNode node = null;

    if (persistNode == null)
    {
       // If m_CurGroup is 0, that means that we have never been told to
       // set the group message. group must be valid in order to continue

       if (name.length() > 0)
       {
          persistNode = new DefaultMutableTreeNode(name);

          if (parent != null)
          {
             m_TreeModel.insertNodeInto(persistNode, parent, parent.getChildCount());
             node = persistNode;
          }
       }
    }
    else
    {
       // Check to see if the current group node matches the message coming in.
       if (name != null && name.length() > 0)
       {
          if (!persistNode.toString().equals(name))
          {
             persistNode = new DefaultMutableTreeNode(name);
             if (parent != null)
             {
                m_TreeModel.insertNodeInto(persistNode, parent, parent.getChildCount());
                // m_MessageTree.InsertItem(name.c_str(), image, image, parent);
             }
             node = persistNode;
          }
       }
       else
       { // No new message for the group, so use the one that is there.
          node = persistNode;
       }
    }

    return node;
 }

 /**
  * Closes the dialog
  */
 public void onCancel()
 {
    try
    {
       m_Cancelled = true;
       broadCastCancel();
       cleanUp();

       // Fix J2638:  Need to clear the group title
       m_GroupTitle.setText( "" );

       m_Controller = null;

       if (m_CurMode == MessageDialogDisplayEnum.MMK_MODAL)
       {
          this.dispose();
       }
       else /* if (::IsWindow(m_hWnd) == TRUE) */ // Removed because in Windows 98 this was always false
          {
          this.dispose();
       }
    }
    catch (Exception e)
    {
       Log.stackTrace(e);
    }
 }

 private void onSaveMessages()
 {
    try
    {
       TreeSaver saver = new TreeSaver(m_MessageTree);

       m_SavingMessages = true;
       saver.save();
       m_SavingMessages = false;
    }
    catch (Exception e)
    {
       Log.stackTrace(e);
    }
 }

 private void onCloseWhenDone()
 {
    this.dispose();
 }

 /**
  * Handles de-initializing this class
  */
 private void cleanUp()
 {
    m_CurMode = MessageDialogDisplayEnum.MMK_NONE;
    m_CurGroup = null;
    m_CurFirst = null;
    m_CurSecond = null;

    m_ProgressIndicator.setIndeterminate(false);
    m_ProgressIndicator.setMinimum( 0 );
    m_ProgressIndicator.setMaximum( 100 );
    m_ProgressIndicator.setValue(0);

    if (m_Controller != null)
    {
       // RevokeActiveObject is called to decrement the IUnknown refCount
       // that RegisterActiveObject hangs onto.  IProgressDialog will
       // not be removed from the ROT until this happens.
       //		  ::RevokeActiveObject( m_RevokeNum, 0 );
    }

    // Make sure we unblock the user input
    m_cpDiagramBlocker = null;
 }

 /**
  * Expand the tree to the specified level
  */
 private void expandTree(DefaultMutableTreeNode hti, int nLevel)
 {
    if (nLevel >= 0)
    {
       DefaultMutableTreeNode hCurrent = hti;
       while (hCurrent != null)
       {
          //			 m_MessageTree.Expand( hCurrent, TVE_EXPAND );

          int nNextLevel = nLevel - 1;
          if (nNextLevel >= 0)
          {
             expandTree(hCurrent, nNextLevel);
          }

          //			 hCurrent = m_MessageTree.GetNextItem( hCurrent, TVGN_NEXT );
       }
    }
 }

 private void updateData(boolean value)
 {
    if( value )
    {
       update(getGraphics());
    }
 }

 ///////////////////////////////////////

 // IProgressDialog inteface methods
 protected long clearFields()
 {
    m_FirstField.setText("  ");
    m_SecondField.setText("  ");
    m_ThirdField.setText("  ");

    updateData(false);

    return 0;
 }

 protected long close()
 {
    this.dispose();
    return 0;
 }

 protected boolean display(int mode)
 {
    boolean result = true;
    m_Cancelled = false;

    if (mode == MessageDialogDisplayEnum.MMK_MODAL)
    {
       m_CurMode = MessageDialogDisplayEnum.MMK_MODAL;
       doModal();
    }
    else if (mode == MessageDialogDisplayEnum.MMK_MODELESS)
    {
       m_CurMode = MessageDialogDisplayEnum.MMK_MODELESS;
       setModal(false);

       show();
       updateData( true );
    }
    initMessageTree();

    return result;
 }

 protected boolean getCloseWhenDone()
 {
    return this.cbCloseWhenDone.isSelected();
 }

 protected boolean getCollapse()
 {
    return this.m_Collapsed;
 }

 protected String getDefaultExtension()
 {
    return this.m_DefExt;
 }

 protected String getFieldOne()
 {
    return this.m_FirstField.getText();
 }

 protected String getFieldThree()
 {
    return this.m_SecondField.getText();
 }

 protected String getFieldTwo()
 {
    return this.m_ThirdField.getText();
 }

 protected String getGroupingTitle()
 {
    return this.m_GroupTitle.getText();
 }

 protected int getIncrementAmount()
 {
    return this.m_Increment;
 }

 protected boolean getIsCancelled()
 {
    return this.m_Cancelled;
 }

 protected ETPairT < Integer, Integer > getLimits()
 {
    return new ETPairT < Integer, Integer > (new Integer(m_Lower), new Integer(m_Upper));
 }

 protected String getLogFileName()
 {
    return this.m_LogFile;
 }

 protected int getPosition()
 {
    return this.m_ProgressIndicator.getValue();
 }

 protected IProgressExecutor getProgressExecutor()
 {
    return m_Executor;
 }

 protected int increment()
 {
    int result;

    if (!isProgressIndicatorAtMaximumPosition())
    {
       m_ProgressIndicator.setValue(m_ProgressIndicator.getValue() + 1);

       result = m_ProgressIndicator.getValue();
    }
    else
    {
       result = m_Upper;
    }

    flushEventQueue();

    return result;
 }

 protected int increment(int value)
 {
    int result;

    if (!isProgressIndicatorAtMaximumPosition())
    {
			int increment = value > 0 ? value : 1;
    	int newValue = m_ProgressIndicator.getValue() + increment;

       m_ProgressIndicator.setValue(newValue);
       result = m_ProgressIndicator.getValue();
    }
    else
    {
       result = m_Upper;
    }

    flushEventQueue();

    return result;
 }

 protected void lockMessageCenterUpdate()
 {
 }

 protected void log(int type, String group, String first, String second, String third)
 {

    m_CurGroup = establishGroupNode(type, group);
    m_CurFirst = establishFirstNode(type, first);
    m_CurSecond = establishSecondNode(type, second);

    if (third.length() > 0 && m_CurSecond != null)
    {
       int image = retrieveImage(type, true);
       m_TreeModel.insertNodeInto(new DefaultMutableTreeNode(third), m_CurSecond, m_CurSecond.getChildCount());
    }
 }

 protected void promptForClosure(String buttonTitle, boolean beep)
 {
    try
    {
       setProgressIndicatorToMaximum();
       updateData(true);

       if (m_CloseWhenDone)
       {
          onCancel();
       }
       else
       {
          if (buttonTitle.length() > 0)
          {

             unlockMessageCenterUpdate();

             // This tree expansion is too expensive if you have too many nodes.
             // For example, RE DigSim and you'll experience a very significant
             // delay at the end of the operation.
             // ExpandTree( m_MessageTree.GetRootItem(), 3 );

             showLastTreeItem();
             Thread runner = new Thread()
             {
                public void run()
                {

                   while (!m_Cancelled)
                   {
                      try
                      {
                         if (!m_SavingMessages)
                         {
                            flushEventQueue();
                         }
                         Thread.sleep(200);
                      }
                      catch (InterruptedException e)
                      {
                         e.printStackTrace();
                      }
                   }

                   Runnable runme = new Runnable()
                   {
                      public void run()
                      {
                         flushEventQueue();
                      }
                   };

                   SwingUtilities.invokeLater(runme);
                }
             };

//             runner.start();
          }
       }
    }
    catch (Exception e)
    {
       Log.stackTrace(e);
    }
 }

 /**
  * Ensures that the last tree item is shown in the tree control's window
  */
 private void showLastTreeItem()
 {
    // Select the last node, so the entire tree is displayed
    DefaultMutableTreeNode htiLast = null;
    DefaultMutableTreeNode hCurrent = (DefaultMutableTreeNode) m_MessageTree.getModel().getRoot();
    //		  while ( hCurrent != null )
    if (hCurrent != null)
    {
       htiLast = (DefaultMutableTreeNode) m_MessageTree.getModel().getChild(hCurrent, hCurrent.getChildCount() - 1);
    }

    if (htiLast != null)
    {
       m_MessageTree.makeVisible(new TreePath(htiLast.getPath()));
       //			 m_MessageTree.Select( htiLast, TVGN_FIRSTVISIBLE );
       //			 TRACE(_T("Item selected = '%s'\n"), m_MessageTree.GetItemText( htiLast ) );
    }
 }

 protected void setCloseWhenDone(boolean newVal)
 {
    m_CloseWhenDone = newVal ? true : false;
    updateData(false);
 }

 protected void setCollapse(boolean bCollapse)
 {
    m_Collapsed = bCollapse;

    collapseDialog(bCollapse);

    if (bCollapse)
    {
       lockMessageCenterUpdate();
    }
    else
    {
       unlockMessageCenterUpdate();
    }
 }

 protected void setDefaultExtension(String newVal)
 {
    this.m_DefExt = newVal;
 }

 /**
  * Sets the first text field contained by the group box at the top of the dialog
  *
  * @param inc The text.
  * @param type The type of this message as it should appear in the tree control.
  */
 protected void setFieldOne(String newValue)
 {
    this.setFieldOne(newValue, ProgressDialogMessageKind.PDMK_NONE);
 }

 protected void setFieldOne(String text, int type)
 {
  	m_FirstField.setText(text != null ? text : "  ");
    updateData(false);
    log(type, "", text, "", "");
    flushEventQueue();
 }

 protected void setFieldTwo(String newVal)
 {
    this.setFieldTwo(newVal, ProgressDialogMessageKind.PDMK_NONE);
 }

 protected void setFieldTwo(String newVal, int type)
 {
    this.m_SecondField.setText(newVal != null ? newVal : "  ");

    updateData(false);
    log(type, "", "", newVal, "");
    flushEventQueue();
 }

 protected void setFieldThree(String newVal)
 {
    this.setFieldThree(newVal, ProgressDialogMessageKind.PDMK_NONE);
 }

 protected void setFieldThree(String newVal, int type)
 {
    this.m_ThirdField.setText(newVal != null ? newVal : "  ");

    updateData(false);
    log(type, "", "", "", newVal);
    flushEventQueue();
 }

 protected void setGroupingTitle(String newVal)
 {
    this.setGroupingTitle(newVal, ProgressDialogMessageKind.PDMK_NONE);
 }

 /**
  * Sets the grouping title  That is the title of the group box grouping the first, second
  * and third fields.
  *
  * @param inc The grouping title.
  * @param type The type of this message as it should appear in the tree control.
  */
 protected void setGroupingTitle(String newVal, int type)
 {
    this.m_GroupTitle.setText(newVal != null ? newVal : "  ");

    log(type, newVal, "", "", "");

    flushEventQueue();
 }

 protected void setIncrementAmount(int newVal)
 {
    m_Increment = newVal;
    flushEventQueue();
 }

 protected void setLimits(ETPairT < Integer, Integer > pLimits)
 {
    if (m_ParentFrame != null)
    {
       this.center(m_ParentFrame);
       this.updateData(false);
    }

    if (pLimits != null)
    {
       this.m_Lower = pLimits.getParamOne().intValue();
       this.m_Upper = pLimits.getParamTwo().intValue();

       m_ProgressIndicator.setMinimum(m_Lower);
       m_ProgressIndicator.setMaximum(m_Upper);

       flushEventQueue();
    }
 }

 protected void setLogFileName(String newVal)
 {
    this.m_LogFile = newVal;
 }

 protected void setPosition(int newVal)
 {
    this.m_ProgressIndicator.setValue(newVal);
    flushEventQueue();
 }

 protected void setProgressExecutor(IProgressExecutor newVal)
 {
    this.m_Executor = newVal;
 }

 protected void setIndeterminate(boolean newVal)
 {
    this.m_ProgressIndicator.setIndeterminate(newVal);
 }

 protected void unlockMessageCenterUpdate()
 {
    /*
    AFX_MANAGE_STATE(AfxGetStaticModuleState())

     if( ::IsWindow( m_hWnd ) )
     {
    	CWaitCursor wait;

    	m_Static.ShowWindow( SW_HIDE );
    	m_Static.InvalidateRect( 0 );

    	m_SaveMessageButton.ShowWindow( SW_SHOW );
    	m_MessageTree.ShowWindow( SW_SHOW );
    	m_MessageTree.InvalidateRect( 0 );

    	FlushEventQueue();
     }
    */
 }

 public long execute(IProgressDialog progDiag)
 {
    // TODO Auto-generated method stub
    return 0;
 }

 protected void setProgressController(IProgressController pController)
 {
    m_Controller = pController;
 }

 protected IProgressController getProgressController()
 {
    return m_Controller;
 }
}
