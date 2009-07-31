/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
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
 *
 * Contributor(s): Andrei Badea
 *                 Petr Hrebejk
 */

package org.netbeans.modules.jumpto.file;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.jumpto.SearchHistory;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hrebejk, Andrei Badea
 */
public class FileSearchPanel extends javax.swing.JPanel implements ActionListener {
    
    private static final int BRIGHTER_COLOR_COMPONENT = 10;    
    private final ContentProvider contentProvider;
    private final Project currentProject;
    private boolean containsScrollPane;
    
    private JLabel messageLabel;
    private String oldText;
    /* package */ long time;

    private FileDescription[] selectedFile;

    private final SearchHistory searchHistory;

    public FileSearchPanel(ContentProvider contentProvider, Project currentProject) {
        this.contentProvider = contentProvider;
        this.currentProject = currentProject;
        
        initComponents();        
        
        this.containsScrollPane = true;
        Color bgColorBrighter = new Color(
                                    Math.min(getBackground().getRed() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getGreen() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getBlue() + BRIGHTER_COLOR_COMPONENT, 255)
                            );
        messageLabel = new JLabel();
        messageLabel.setBackground(bgColorBrighter);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setEnabled(true);
        messageLabel.setText(NbBundle.getMessage(FileSearchPanel.class, "TXT_NoTypesFound")); // NOI18N
        messageLabel.setFont(resultList.getFont());
        
        caseSensitiveCheckBox.setSelected(FileSearchOptions.getCaseSensitive());
        hiddenFilesCheckBox.setSelected(FileSearchOptions.getShowHiddenFiles());
        mainProjectCheckBox.setSelected(FileSearchOptions.getPreferMainProject());

        if ( currentProject == null ) {
            mainProjectCheckBox.setEnabled(false);
            mainProjectCheckBox.setSelected(false);
        }
        else {
            ProjectInformation pi = currentProject.getLookup().lookup(ProjectInformation.class);
            mainProjectCheckBox.setText(NbBundle.getMessage(FileSearchPanel.class, "FMT_CurrentProjectLabel", pi.getDisplayName())); // NOI18N
        }
        
        mainProjectCheckBox.addActionListener(this);
        caseSensitiveCheckBox.addActionListener(this);
        hiddenFilesCheckBox.addActionListener(this);
        hiddenFilesCheckBox.setVisible(false);
        
        resultList.setCellRenderer( contentProvider.getListCellRenderer( resultList ) );
        contentProvider.setListModel( this, null );
                
        fileNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                update();
            }
            
            public void insertUpdate(DocumentEvent e) {
                update();
            }
            
            public void removeUpdate(DocumentEvent e) {
                update();
            }
        });

        searchHistory = new SearchHistory(FileSearchPanel.class, fileNameTextField);
    }

    @Override
    public void removeNotify() {
        searchHistory.saveHistory();
        super.removeNotify();
    }
  
    //Good for setting model form any thread  
    public void setModel( final ListModel model ) {
        // XXX measure time here
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               if (model.getSize() > 0 || getText() == null || getText().trim().length() == 0 ) {
                   resultList.setModel(model);
                   resultList.setSelectedIndex(0);
                   ((FileDescription.Renderer) resultList.getCellRenderer()).setColorPrefered(isPreferedProject());
                   setListPanelContent(null,false);
                   if ( time != -1 ) {
                       FileSearchAction.LOGGER.fine("Real search time " + (System.currentTimeMillis() - time) + " ms.");
                       time = -1;
                   }
               }
               else {
                   if (getText()!=null) {
                       try {
                           Pattern.compile(getText().replace(".", "\\.").replace( "*", ".*" ).replace( '?', '.' ), Pattern.CASE_INSENSITIVE); // NOI18N
                           setListPanelContent( NbBundle.getMessage(FileSearchPanel.class, "TXT_NoTypesFound") ,false ); // NOI18N
                       } catch (PatternSyntaxException pse) {
                           setListPanelContent( NbBundle.getMessage(FileSearchPanel.class, "TXT_SyntaxError", pse.getDescription(),pse.getIndex()) ,false ); // NOI18N
                       }
                   } else
                       setListPanelContent( NbBundle.getMessage(FileSearchPanel.class, "TXT_NoTypesFound") ,false ); // NOI18N
               }
           }
       });
    }

//    public Project[] getProjects() {
//        return OpenProjects.getDefault().getOpenProjects();
//    }
//
//    public void openSelectedItems() {
//        Object[] selectedValues = resultList.getSelectedValues();
//        if ( selectedValues != null ) {
//            for(Object v : selectedValues) {
//                if ( v instanceof FileDescription) {
//                    ((FileDescription)v).open();
//                }
//            }
//        }
//    }
//
//    public Project getPreferedProject() {
//        if ( !isPreferedProject() ) {
//            return null;
//        }
//        else {
//            return currentProject;
//        }
//
//    }
    
    void setListPanelContent( String message, boolean waitIcon ) {
        
        if ( message == null && !containsScrollPane ) {
           listPanel.remove( messageLabel );
           listPanel.add( resultScrollPane );
           containsScrollPane = true;
           revalidate();
           repaint();
        }        
        else if ( message != null ) { 
           jTextFieldLocation.setText(""); 
           messageLabel.setText(message);
           messageLabel.setIcon( waitIcon ? FileDescription.Renderer.WAIT_ICON : null);
           if ( containsScrollPane ) {
               listPanel.remove( resultScrollPane );
               listPanel.add( messageLabel );
               containsScrollPane = false;
           }
           revalidate();
           repaint();
       }                
    }
    
    public boolean isShowHiddenFiles() {
        return hiddenFilesCheckBox.isSelected();
    }
    
    public boolean isPreferedProject() {
        return mainProjectCheckBox.isSelected();
    }
    
    public boolean isCaseSensitive() {
        return caseSensitiveCheckBox.isSelected();
    }
    
    private void update() {
        time = System.currentTimeMillis();
        String text = getText();
        if ( oldText == null || oldText.trim().length() == 0 || !text.startsWith(oldText) ) {
            setListPanelContent(NbBundle.getMessage(FileSearchPanel.class, "TXT_Searching"),true); // NOI18N
        }
        oldText = text;
        contentProvider.setListModel(this, text);
    }

//
//    void cleanup() {
//        if ( search != null ) {
//            search.cancel( true );
//        }
//    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fileNameLabel = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        resultLabel = new javax.swing.JLabel();
        listPanel = new javax.swing.JPanel();
        resultScrollPane = new javax.swing.JScrollPane();
        resultList = new javax.swing.JList();
        caseSensitiveCheckBox = new javax.swing.JCheckBox();
        hiddenFilesCheckBox = new javax.swing.JCheckBox();
        mainProjectCheckBox = new javax.swing.JCheckBox();
        jLabelLocation = new javax.swing.JLabel();
        jTextFieldLocation = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setPreferredSize(new java.awt.Dimension(540, 280));
        setLayout(new java.awt.GridBagLayout());

        fileNameLabel.setFont(fileNameLabel.getFont());
        fileNameLabel.setLabelFor(fileNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileNameLabel, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "CTL_FileName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(fileNameLabel, gridBagConstraints);

        fileNameTextField.setFont(new java.awt.Font("Monospaced", 0, getFontSize()));
        fileNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileNameTextFieldActionPerformed(evt);
            }
        });
        fileNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fileNameTextFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        add(fileNameTextField, gridBagConstraints);
        fileNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AN_SearchText")); // NOI18N
        fileNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_SearchText")); // NOI18N

        resultLabel.setLabelFor(resultList);
        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "CTL_MatchingFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(resultLabel, gridBagConstraints);

        listPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        listPanel.setLayout(new java.awt.BorderLayout());

        resultScrollPane.setBorder(null);

        resultList.setFont(new java.awt.Font("Monospaced", 0, getFontSize()));
        resultList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                resultListMouseReleased(evt);
            }
        });
        resultList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                resultListValueChanged(evt);
            }
        });
        resultScrollPane.setViewportView(resultList);
        resultList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AN_MatchingList")); // NOI18N
        resultList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_MatchingList")); // NOI18N

        listPanel.add(resultScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(listPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(caseSensitiveCheckBox, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_CaseSensitive")); // NOI18N
        caseSensitiveCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(caseSensitiveCheckBox, gridBagConstraints);
        caseSensitiveCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_CaseSensitive")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(hiddenFilesCheckBox, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_HiddenFiles")); // NOI18N
        hiddenFilesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(hiddenFilesCheckBox, gridBagConstraints);
        hiddenFilesCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_HiddenFiles")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mainProjectCheckBox, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_PreferMainProject")); // NOI18N
        mainProjectCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(mainProjectCheckBox, gridBagConstraints);
        mainProjectCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_PreferMainProject")); // NOI18N

        jLabelLocation.setLabelFor(jTextFieldLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelLocation, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 4, 0);
        add(jLabelLocation, gridBagConstraints);
        jLabelLocation.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AN_Location")); // NOI18N
        jLabelLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_Location")); // NOI18N

        jTextFieldLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(jTextFieldLocation, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void fileNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileNameTextFieldActionPerformed
    if (contentProvider.hasValidContent()) {
        contentProvider.closeDialog();
        setSelectedFile();
    }
}//GEN-LAST:event_fileNameTextFieldActionPerformed

private void resultListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultListMouseReleased
    if ( evt.getClickCount() == 2 ) {
        fileNameTextFieldActionPerformed(null);
    }
}//GEN-LAST:event_resultListMouseReleased

private void resultListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_resultListValueChanged
         
        Object svObject = resultList.getSelectedValue();
        if ( svObject != null && svObject instanceof FileDescription ) {
            FileDescription selectedValue = (FileDescription)svObject;
            jTextFieldLocation.setText(FileUtil.getFileDisplayName(selectedValue.getFileObject()));
        }
        else {
            jTextFieldLocation.setText("");
        }
}//GEN-LAST:event_resultListValueChanged

    private void fileNameTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fileNameTextFieldKeyPressed
        Object actionKey = resultList.getInputMap().get(KeyStroke.getKeyStrokeForEvent(evt));
        
        // see JavaFastOpen.boundScrollingKey()
        boolean isListScrollAction = 
            "selectPreviousRow".equals(actionKey) || // NOI18N
            "selectPreviousRowExtendSelection".equals(actionKey) || // NOI18N            
            "selectNextRow".equals(actionKey) || // NOI18N
            "selectNextRowExtendSelection".equals(actionKey) || // NOI18N
            // "selectFirstRow".equals(action) || // NOI18N
            // "selectLastRow".equals(action) || // NOI18N
            "scrollUp".equals(actionKey) || // NOI18N            
            "scrollUpExtendSelection".equals(actionKey) || // NOI18N            
            "scrollDown".equals(actionKey) || // NOI18N
            "scrollDownExtendSelection".equals(actionKey); // NOI18N
        
        
        int selectedIndex = resultList.getSelectedIndex();
        ListModel model = resultList.getModel();
        int modelSize = model.getSize();
        
        // Wrap around
        if ( "selectNextRow".equals(actionKey) && 
              ( selectedIndex == modelSize - 1 ||
                ( selectedIndex == modelSize - 2 && 
                  model.getElementAt(modelSize - 1) == FileDescription.SEARCH_IN_PROGRES )
             ) ) {
            resultList.setSelectedIndex(0);
            resultList.ensureIndexIsVisible(0);
            return;
        }
        else if ( "selectPreviousRow".equals(actionKey) &&
                   selectedIndex == 0 ) {
            int last = modelSize - 1;
            
            if ( model.getElementAt(last) == FileDescription.SEARCH_IN_PROGRES ) {
                last--;
            } 
            
            resultList.setSelectedIndex(last);
            resultList.ensureIndexIsVisible(last);
            return;
        }
        
        if (isListScrollAction) {
            Action a = resultList.getActionMap().get(actionKey);
            a.actionPerformed(new ActionEvent(resultList, 0, (String)actionKey));
            evt.consume();
        }
    }//GEN-LAST:event_fileNameTextFieldKeyPressed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox caseSensitiveCheckBox;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JCheckBox hiddenFilesCheckBox;
    private javax.swing.JLabel jLabelLocation;
    private javax.swing.JTextField jTextFieldLocation;
    private javax.swing.JPanel listPanel;
    private javax.swing.JCheckBox mainProjectCheckBox;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JList resultList;
    private javax.swing.JScrollPane resultScrollPane;
    // End of variables declaration//GEN-END:variables
    
    public void actionPerformed(ActionEvent e) {
        if ( e.getSource() == caseSensitiveCheckBox ) {
            FileSearchOptions.setCaseSensitive(caseSensitiveCheckBox.isSelected());
        }
        else if ( e.getSource() == hiddenFilesCheckBox ) {
            FileSearchOptions.setShowHiddenFiles(hiddenFilesCheckBox.isSelected());            
        }
        else if ( e.getSource() == mainProjectCheckBox ) {            
            FileSearchOptions.setPreferMainProject(isPreferedProject());            
        }

        update();
    }
    
    private String getText() {
        try {
            String text = fileNameTextField.getDocument().getText(0, fileNameTextField.getDocument().getLength());
            return text;
        } catch( BadLocationException ex ) {
            return null;
        }
    }

    private int getFontSize () {
        return this.resultList.getFont().getSize();
    }        
    
    public void setSelectedFile() {
        List<FileDescription> list = new ArrayList(Arrays.asList(resultList.getSelectedValues()));
        selectedFile = list.toArray(new FileDescription[0]);
    }

    public FileDescription[] getSelectedFiles() {
        return selectedFile;
    }

   public Project getCurrentProject() {
       return currentProject;
   }

//    public boolean accept(Object obj) {
//        if ( obj instanceof FileDescription ) {
//            FileDescription fd = (FileDescription)obj;
//            return isShowHiddenFiles() ? true : fd.isVisible();
//        }
//        return true;
//    }
//
//    public void scheduleUpdate(Runnable run) {
//        SwingUtilities.invokeLater( run );
//    }

    public static interface ContentProvider {

        public ListCellRenderer getListCellRenderer( JList list );

        public void setListModel( FileSearchPanel panel, String text );

        public void closeDialog();

        public boolean hasValidContent ();

    }

}
