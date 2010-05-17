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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;

public class SearchDirectoriesPage extends JCenterDialog
{
   private Vector m_cachedNames = new Vector();
   private boolean m_ShowAgain = false;
   private boolean m_Cancelled = false;
   private JDialog m_ThisDialog = null;
   private JList dirList = null;
   private JButton addBtn = null;
   private JButton removeBtn = null;
   private JButton loadBtn = null;
   private JButton saveBtn = null;
   private JButton okBtn = null;
   private JButton cancelBtn = null;
   private JCheckBox askChk = null;
   private DefaultListModel m_listModel = new DefaultListModel();

   public SearchDirectoriesPage(Frame parent)
   {
      super(parent, true);
   }

   /**
    * Adds a new directory to the list of base directories.
    *
    * @param newDirectory [in] The new directory.
    */
   public void addBaseDirectory(String newDir)
   {
      String baseDir = newDir;
      if (baseDir != null && baseDir.length() > 0)
      {
         if (newDir.endsWith(File.pathSeparator))
         {
            newDir = newDir.substring(0, newDir.length()-1);
         }
         m_cachedNames.add(newDir);
      }
   }
   
   /* Adds the direcitories to the list of base directories.
   *
   * @param newDirectory [in] The new direcitories.
   */
   public void addBaseDirectories(IStrings newDirectories)
   {
      if (newDirectories != null)
      {
         int count = newDirectories.getCount();
         for (int i=0; i<count; i++)
         {
            String curStr = newDirectories.item(i);
            addBaseDirectory(curStr);
         }
      }
   }
   
   /**
    * Retrieves the list of base directories selected by the user.
    *
    * @param pVal [out] The list of directories.
    */
   public IStrings getBaseDirectories()
   {
      IStrings retVal = new Strings();
      if (m_cachedNames != null)
      {
         for (int i=0; i<m_cachedNames.size(); i++)
         {
            retVal.add((String)m_cachedNames.get(i));
         }
      }
      return retVal;
   }
   
   public void addSettingsFile(String fileName)
   {
      if (fileName != null && fileName.length() > 0)
      {
         m_cachedNames.clear();
         try
         {
         	FileReader fileReader = new FileReader(fileName);
         	BufferedReader bufferedReader = new BufferedReader(fileReader);
         	String newFile = "";
         	while (true)
         	{
					newFile = bufferedReader.readLine();
					if (newFile != null)
					{
						if (!checkExisting(newFile))
						{
							m_cachedNames.add(newFile);
						}
					}
					else
					{
						break;
					}
         	}
         }
         catch (Exception ex)
         {
         }
      }
   }
   
   public boolean isShowAgain()
   {
      return m_ShowAgain;
   }
   
   public boolean doModal()
   {
      //Cbeckham - modified this code to dynamically adjust panel size for larger fonts 
      boolean cancel = false;
      int fontsize = getFont().getSize();
      int width  = 600;
      int height = 300;
      int multiplyer = 1;      
      initControl();
        
      if (fontsize > 17) multiplyer = 3;
        width  = width  + Math.round(width*(multiplyer*fontsize/100f));
        height = height + Math.round(height*(multiplyer*fontsize/100f));
              
      setSize(width,height);
      setVisible(true);
      if (m_Cancelled)
      {
         cancel = true;
      }
      return cancel;
   }
   
   
   
   private void initControl()
   {
      setTitle(PFMessages.getString("RE_Operation_Dialog_Title"));
      getContentPane().removeAll();
      JPanel panel = new JPanel();
      java.awt.GridBagConstraints gridBagConstraints;

      //CBeckham - changed Text Label to Text Area to provide multi line for larger fontsizes
      JTextArea textLabel = new javax.swing.JTextArea();
      IStrings cachedNames = getBaseDirectories();
      if (cachedNames != null)
      {
	      for (int i = 0; i < cachedNames.getCount(); i++)
	      {
				m_listModel.addElement(cachedNames.item((i)));
	      }
      }
      dirList = new javax.swing.JList(m_listModel);
      addBtn = new javax.swing.JButton();
      removeBtn = new javax.swing.JButton();
      loadBtn = new javax.swing.JButton();
      saveBtn = new javax.swing.JButton();
      okBtn = new javax.swing.JButton();
      cancelBtn = new javax.swing.JButton();
      askChk = new javax.swing.JCheckBox();

      //Add button should allow user to add existing directories.
      addBtn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(m_ThisDialog);
            if (result == JFileChooser.APPROVE_OPTION)
            {
               File file = chooser.getSelectedFile();
               if (file != null)
               {
                  //dirList.add(file.getCanonicalPath());
                  try
                  {
                  	String newName = file.getCanonicalPath();
                  	if (!checkExisting(newName))
                  	{
								m_listModel.addElement(newName);
								addBaseDirectory(newName);
                  	}
                  }
                  catch(Exception ex)
                  {
                  }
               }
            }
         }
      });
      
      //load button should allow user to add existing directories.
      loadBtn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.addChoosableFileFilter(new FileFilter()
				{
					 public boolean accept(File file) {
						  return file.isDirectory() ||
								file.toString().toLowerCase().endsWith(PFMessages.getString("IDS_CONFIG"));
					 }

					 public String getDescription() {
						  return PFMessages.getString("IDS_CONFIG_DES");
					 }
				});
            int result = chooser.showOpenDialog(m_ThisDialog);
            if (result == JFileChooser.APPROVE_OPTION)
            {
               File file = chooser.getSelectedFile();
               if (file != null)
               {
                  //dirList.add(file.getCanonicalPath());
                  String fileName = "";
                  try
                  {
                  	fileName = file.getCanonicalPath();
                  }
                  catch (Exception ex)
                  {
                  }
                  if (fileName != null && fileName.length() > 0)
                  {
							addSettingsFile(fileName);
							m_listModel.clear();
							copyCacheToGUI();
                  }
               }
            }
         }
      });
      
      //remove button should remove the selected item in the list
      removeBtn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            int index = dirList.getSelectedIndex();
            if (index >= 0)
            {
               String removeName = (String)m_listModel.get(index);
               m_listModel.remove(index);
					
					if (m_cachedNames != null)
					{
						for (int i=0; i<m_cachedNames.size(); i++)
						{
							String fileName = ((String)m_cachedNames.get(i));
							if (fileName.toLowerCase().equals(removeName.toLowerCase()))
							{
								m_cachedNames.remove(i);
								break;
							}
						}
					}
            }
         }
      });

      //Save button should allow user to save existing directories.
      saveBtn.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              JFileChooser chooser = new JFileChooser();
              //				chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
              //					 public boolean accept(File file) {
              //						  return file.isDirectory() ||
              //								file.toString().toLowerCase().endsWith(".cfg");
              //					 }
              //
              //					 public String getDescription() {
              //						  return "Config Files";
              //					 }
              //				});
              chooser.addChoosableFileFilter(new FileFilter() {
                  public boolean accept(File file) {
                      return file.isDirectory() ||
                              file.toString().toLowerCase().endsWith(".cfg");
                  }
                  
                  public String getDescription() {
                      return "Config Files(*.cfg)";
                  }
              });
              
              int result = chooser.showSaveDialog(m_ThisDialog);
              if (result == JFileChooser.APPROVE_OPTION) {
                  File file = chooser.getSelectedFile();
                  if (file != null) {
                      //dirList.add(file.getCanonicalPath());
                      String fileName = "";
                      try {
                          fileName = file.getCanonicalPath();
                          if (!fileName.toLowerCase().endsWith(".cfg")) {
                              fileName += ".cfg";
                          }
                      } catch(Exception ex) {
                      }
                      
                      IStrings pDirectories = getBaseDirectories();
                      if(pDirectories != null) {
                          try {
                              FileWriter fileWriter = new FileWriter(fileName);
                              BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                              
                              int count = pDirectories.getCount();
                              for(int index = 0; index < count; index++) {
                                  String curDir = pDirectories.item(index);
                                  
                                  bufferedWriter.write(curDir);
                                  bufferedWriter.newLine();
                              }
                              bufferedWriter.flush();
                              bufferedWriter.close();
                          } catch(Exception ex) {
                          }
                          
                          IQuestionDialog pQuestionDialog = new SwingQuestionDialogImpl();
                          if(pQuestionDialog != null) {
                              String question = PFMessages.getString("IDS_PROMPT_TO_SET_SEARCHDIRS");
                              String title = PFMessages.getString("IDS_PROMPT_TO_SET_SEARCHDIRS_TITLE");
                              QuestionResponse nResult = pQuestionDialog.displaySimpleQuestionDialog(MessageDialogKindEnum.SQDK_YESNO,
                                      MessageIconKindEnum.EDIK_ICONQUESTION,
                                      question,
                                      SimpleQuestionDialogResultKind.SQDRK_RESULT_YES,
                                      null,
                                      title);
                              
                              
                          }
                      }
                  }
              }
          }
      });
      

      //when OK button is clicked, take the default directory and use it
      okBtn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            m_Cancelled = false;
            dispose();
         }
      });

      //when cancel button is clicked, need to cancel out of this operation.
      cancelBtn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            m_Cancelled = true;
            dispose();
         }
      });

      //when the check box is toggled, we need to set our internal variable.
      askChk.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            Object source = e.getSource();
            if (source instanceof JCheckBox)
            {
               m_ShowAgain = ((JCheckBox)source).isSelected();
            }
         }
      });


      panel.setLayout(new java.awt.GridBagLayout());

      addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent evt) 
          {
              setVisible(false);
              dispose();
          }
      });

      textLabel.setText(PFMessages.getString("RE_Operation_Dialog_Text"));
      textLabel.setLineWrap(true);
      textLabel.setBackground(new java.awt.Color(224, 223, 227));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
      panel.add(textLabel, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridheight = 4;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      panel.add(dirList, gridBagConstraints);

      addBtn.setText(PFMessages.getString("RE_Operation_Dialog_Add"));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
      panel.add(addBtn, gridBagConstraints);

      removeBtn.setText(PFMessages.getString("RE_Operation_Dialog_Remove"));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
      panel.add(removeBtn, gridBagConstraints);

      loadBtn.setText(PFMessages.getString("RE_Operation_Dialog_Load"));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
      panel.add(loadBtn, gridBagConstraints);

      saveBtn.setText(PFMessages.getString("RE_Operation_Dialog_Save"));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 4;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
      panel.add(saveBtn, gridBagConstraints);

      okBtn.setText(PFMessages.getString("RE_Operation_Dialog_Ok"));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 6;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      panel.add(okBtn, gridBagConstraints);

      cancelBtn.setText(PFMessages.getString("RE_Operation_Dialog_Cancel"));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 6;
      gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
      panel.add(cancelBtn, gridBagConstraints);

      askChk.setText(PFMessages.getString("RE_Operation_Dialog_Ask"));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 5;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      panel.add(askChk, gridBagConstraints);

      getContentPane().add(panel, BorderLayout.CENTER);
   }
	
	protected void copyCacheToGUI()
	{
		if (m_cachedNames != null)
		{
			for (int i=0; i<m_cachedNames.size(); i++)
			{
				addStringToList((String)m_cachedNames.get(i));
			}
		}
	}
	
	protected void addStringToList(String item)
	{
		m_listModel.addElement(item);
	}
	
	protected boolean checkExisting(String tempName)
	{
		boolean bExisting = false;
		
		if (m_cachedNames != null)
		{
			for (int i=0; i<m_cachedNames.size(); i++)
			{
				String fileName = ((String)m_cachedNames.get(i));
				if (fileName.toLowerCase().equals(tempName.toLowerCase()))
				{
					bExisting = true;
					break;
				}
			}
		}
		
		return bExisting;
	}
}
