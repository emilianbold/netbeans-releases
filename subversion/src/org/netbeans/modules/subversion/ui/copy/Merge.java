/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.modules.subversion.ui.copy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class Merge extends CopyDialog implements ItemListener {
        
    private String MERGE_START_URL_HISTORY_KEY = Merge.class.getName() + "_merge_from"; // NOI18N
    private String MERGE_END_URL_HISTORY_KEY = Merge.class.getName() + "_merge_after"; // NOI18N
    
    public Merge(RepositoryFile repositoryRoot, File root) {
        super(new MergePanel(), NbBundle.getMessage(Merge.class, "CTL_Merge_Prompt", root.getName()), NbBundle.getMessage(Merge.class, "CTL_Merge_Title")); // NOI18N

        MergePanel panel = getMergePanel();

        panel.typeComboBox.setModel(new DefaultComboBoxModel(
                new MergeType[] {
                       new MergeOneFolderType(repositoryRoot, root),
                       new MergeTwoFoldersType(repositoryRoot, root),
                       new MergeSinceOriginType(repositoryRoot, root)
                }
        ));
        panel.typeComboBox.setRenderer(createTypeRenderer());
        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Merge.class, "CTL_Merge_Title"));
        panel.typeComboBox.addItemListener(this);
        mergeTypeSelected(((MergeType) panel.typeComboBox.getSelectedItem()));
    }            
    
    SVNUrl getMergeStartUrl() {
        return getSelectedType().getMergeStartUrl();
    }

    SVNUrl getMergeEndUrl() {
        return getSelectedType().getMergeEndUrl();
    }    

    SVNRevision getMergeStartRevision() {
        return getSelectedType().getMergeStartRevision();
    }

    SVNRevision getMergeEndRevision() {
        return getSelectedType().getMergeEndRevision();
    }

    private MergeType getSelectedType() {
        return (MergeType) getMergePanel().typeComboBox.getSelectedItem();
    }

    private MergePanel getMergePanel() {
        return (MergePanel) getPanel();
    }

    private ListCellRenderer createTypeRenderer() {
        return new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(value instanceof MergeType) {
                    setText(((MergeType)value).getDisplayName());
                }
                return this;
            }
        };
    }

    public void itemStateChanged(ItemEvent e) {        
        final MergeType type = (MergeType) e.getItem();        
        mergeTypeSelected(type);
    }

    private void mergeTypeSelected(MergeType type) {                        
        MergePanel panel = getMergePanel();
        panel.typeDescriptionLabel.setText(type.getDescription());

        panel.previewPanel.removeAll();        
        panel.previewPanel.setLayout(new BorderLayout());
        panel.previewPanel.add(type.getPreviewPanel(), BorderLayout.CENTER);        
        
        panel.mergeFieldsPanel.removeAll();
        panel.mergeFieldsPanel.setLayout(new BorderLayout());
        panel.mergeFieldsPanel.add(type.getFieldsPanel(), BorderLayout.CENTER);

        type.setPreviewLabels();
        panel.repaint();
        
        resetUrlComboBoxes();
        setupUrlComboBox(type.getStartUrlComboBox(), MERGE_START_URL_HISTORY_KEY);
        setupUrlComboBox(type.getEndUrlComboBox(), MERGE_END_URL_HISTORY_KEY);

    }    

    private abstract class MergeType implements DocumentListener, PropertyChangeListener {

        private RepositoryPaths mergeStartRepositoryPaths;
        private RepositoryPaths mergeEndRepositoryPaths;
        private RepositoryFile repositoryFile;

        private boolean startPathValid = false;
        private boolean endPathValid = false;
    
        MergeType (RepositoryFile repositoryFile) {
            this.repositoryFile = repositoryFile;
        }

        protected void init(RepositoryPaths mergeStartRepositoryPaths, JLabel mergeStartRepositoryFolderLabel, RepositoryPaths mergeEndRepositoryPaths, JLabel mergeEndRepositoryFolderLabel, File root) {            
            if(mergeStartRepositoryPaths != null) {
                this.mergeStartRepositoryPaths = mergeStartRepositoryPaths;
                init(mergeStartRepositoryPaths, mergeStartRepositoryFolderLabel, root);   
            } else {
                // won't ever chage -> will always be valid
                startPathValid = true;
            }          
            if(mergeEndRepositoryPaths != null) {
                this.mergeEndRepositoryPaths = mergeEndRepositoryPaths;
                init(mergeEndRepositoryPaths, mergeEndRepositoryFolderLabel, root);
            } else {
                // won't ever chage -> will always be valid
                endPathValid = true;
            } 
        }

        private void init(RepositoryPaths paths, JLabel label, File root) {
            String browserPurposeMessage;
            int browserMode;
            if(root.isFile()) {
                if(label!=null) {
                    label.setText(org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_RepositoryFile")); // NOI18N
                }
                browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageMergeFile");
                browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY | Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FILES_SELECTION_ONLY;
            } else {
                browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageMergeFolder");
                browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;                
            }
            paths.setupBehavior(browserPurposeMessage, browserMode, Browser.BROWSER_HELP_ID_MERGE, SvnSearch.SEACRH_HELP_ID_MERGE);
            paths.addPropertyChangeListener(this);
        }

        protected abstract JPanel getFieldsPanel();
        protected abstract JPanel getPreviewPanel();
        protected abstract String getDisplayName();
        protected abstract String getDescription();        
        protected abstract JComboBox getStartUrlComboBox();
        protected abstract JComboBox getEndUrlComboBox();
        protected abstract void setPreviewLabels();

        public SVNUrl getMergeStartUrl() {
            try {
                return mergeStartRepositoryPaths.getRepositoryFiles()[0].getFileUrl();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return null;
        }

        public SVNUrl getMergeEndUrl() {
            try {
                return mergeEndRepositoryPaths.getRepositoryFiles()[0].getFileUrl();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return null;
        }

        public SVNRevision getMergeStartRevision() {
            try {
                return mergeStartRepositoryPaths.getRepositoryFiles()[0].getRevision();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return null;
        }

        public SVNRevision getMergeEndRevision() {
            try {
                return mergeEndRepositoryPaths.getRepositoryFiles()[0].getRevision();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return null;
        }

        RepositoryPaths getMergeStartRepositoryPath() {
            return mergeStartRepositoryPaths;
        }

        RepositoryPaths getMergeEndRepositoryPath() {
            return mergeEndRepositoryPaths;
        }

        public void insertUpdate(DocumentEvent e) {
            setPreviewLabels();
        }

        public void removeUpdate(DocumentEvent e) {
            setPreviewLabels();
        }

        public void changedUpdate(DocumentEvent e) {
            setPreviewLabels();
        }

        protected RepositoryFile getRepositoryFile() {
            return repositoryFile;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {                                        
                boolean valid = ((Boolean) evt.getNewValue()).booleanValue();
                if(evt.getSource() == getMergeStartRepositoryPath()) {
                    startPathValid = valid;
                } else if(evt.getSource() == getMergeEndRepositoryPath()) {
                    endPathValid = valid;
                }                                    
                getOKButton().setEnabled(startPathValid && endPathValid);
            }        
        }        
    }

    private class MergeTwoFoldersType extends MergeType {

        private MergeTwoFoldersPanel panel;
        private TwoFoldersPreviewPanel previewPanel;

        public MergeTwoFoldersType(RepositoryFile repositoryRoot, File root) {
            super(repositoryRoot);

            panel = new MergeTwoFoldersPanel();
            previewPanel = new TwoFoldersPreviewPanel();

            RepositoryPaths mergeStartRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeStartBrowseButton,
                    panel.mergeStartRevisionTextField,
                    panel.mergeStartSearchButton
                );

            RepositoryPaths mergeEndRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeEndBrowseButton,
                    panel.mergeEndRevisionTextField,
                    panel.mergeEndSearchButton
                );

            init(mergeStartRepositoryPaths,
                 panel.mergeStartRepositoryFolderLabel,
                 mergeEndRepositoryPaths,
                 panel.mergeEndRepositoryFolderLabel,
                 root);

            previewPanel.localFolderTextField.setText(root.getAbsolutePath());
            ((JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
            ((JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        }

        public String getDisplayName() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_TwoRepositoryFolders"); // NOI18N
        }

        public String getDescription() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_TwoFoldersDesc"); // NOI18N
        }

        public JPanel getFieldsPanel() {
            return panel;
        }

        public JPanel getPreviewPanel() {
            return previewPanel;
        }

        public JComboBox getStartUrlComboBox() {
            return panel.mergeStartUrlComboBox;
        }

        public JComboBox getEndUrlComboBox() {
            return panel.mergeEndUrlComboBox;
        }

        protected void setPreviewLabels() {
            previewPanel.repositoryFolderTextField1.setText(getRepositoryFile().getRepositoryUrl() + "/" + panel.mergeStartUrlComboBox.getEditor().getItem().toString()); // NOI18N
            previewPanel.repositoryFolderTextField2.setText(getRepositoryFile().getRepositoryUrl() + "/" + panel.mergeEndUrlComboBox.getEditor().getItem().toString()); // NOI18N
        }
        
    }

    private class MergeOneFolderType extends MergeType {

        private RepositoryPaths mergeEndRepositoryPaths;
        private MergeOneFolderPanel panel;
        private OneFolderPreviewPanel previewPanel;

        /** Creates a new instance of MergeOneFolderType */
        public MergeOneFolderType(RepositoryFile repositoryRoot, File root) {
            super(repositoryRoot);
            
            panel = new MergeOneFolderPanel();
            previewPanel = new OneFolderPreviewPanel();

            RepositoryPaths mergeStartRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent(),
                    null,
                    panel.mergeStartRevisionTextField,
                    panel.mergeStartSearchButton
                );

            mergeEndRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeStartBrowseButton,
                    panel.mergeEndRevisionTextField,
                    panel.mergeEndSearchButton
                );

            init(mergeStartRepositoryPaths,
                 panel.mergeStartRepositoryFolderLabel,
                 mergeEndRepositoryPaths,
                 null,
                 root);
            
            previewPanel.localFolderTextField.setText(root.getAbsolutePath());
            ((JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        }

        
        public SVNUrl getMergeStartUrl() {
            // in this case iths the same folder url
            return getMergeEndUrl();
        }

        public String getDisplayName() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_OneRepositoryFolder"); // NOI18N
        }

        public String getDescription() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_OneRepositoryFolderDesc"); // NOI18N
        }

        public JPanel getFieldsPanel() {
            return panel;
        }

        public JPanel getPreviewPanel() {
            return previewPanel;
        }
        
        public JComboBox getStartUrlComboBox() {
            return panel.mergeStartUrlComboBox;
        }

        public JComboBox getEndUrlComboBox() {
            return null;
        }    

        protected void setPreviewLabels() {            
            previewPanel.repositoryFolderTextField.setText(getRepositoryFile().getRepositoryUrl() + org.openide.util.NbBundle.getMessage(Merge.class, "/") + panel.mergeStartUrlComboBox.getEditor().getItem().toString()); // NOI18N
        }

    }

    private class MergeSinceOriginType extends MergeType {

        private MergeSinceOriginPanel panel;
        private RepositoryPaths mergeEndRepositoryPaths;
        private SinceOriginPreviewPanel previewPanel;

        public MergeSinceOriginType(RepositoryFile repositoryRoot, File root) {
            super(repositoryRoot);
            
            panel = new MergeSinceOriginPanel();
            previewPanel = new SinceOriginPreviewPanel();

            mergeEndRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeEndBrowseButton,
                    panel.mergeEndRevisionTextField,
                    panel.mergeEndSearchButton
                );      
            
            init(null, null, mergeEndRepositoryPaths, panel.mergeEndRepositoryFolderLabel, root);
            previewPanel.localFolderTextField.setText(root.getAbsolutePath());
            ((JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);                     
        }

        public String getDisplayName() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_OneRepositoryFolderSinceOrigin"); // NOI18N
        }

        public String getDescription() {
            return org.openide.util.NbBundle.getMessage(Merge.class, "CTL_Merge_OneRepositoryFolderSinceOriginDesc"); // NOI18N
        }

        public JPanel getFieldsPanel() {
            return panel;
        }

        public JPanel getPreviewPanel() {
            return previewPanel;
        }

        public SVNUrl getMergeStartUrl() {
            return null;
        }

        public SVNRevision getMergeStartRevision() {
            return null;
        }

        public SVNUrl getMergeEndUrl() {
            try {
                return mergeEndRepositoryPaths.getRepositoryFiles()[0].getFileUrl();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return null;
        }

        public SVNRevision getMergeEndRevision() {
            try {
                return mergeEndRepositoryPaths.getRepositoryFiles()[0].getRevision();
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return null;
        }

        RepositoryPaths getMergeStartRepositoryPath() {
            return null;
        }

        RepositoryPaths getMergeEndRepositoryPath() {
            return mergeEndRepositoryPaths;
        }

        public JComboBox getStartUrlComboBox() {
            return null;
        }

        public JComboBox getEndUrlComboBox() {
            return panel.mergeEndUrlComboBox;
        }

        protected void setPreviewLabels() {            
            previewPanel.repositoryFolderTextField.setText(getRepositoryFile().getRepositoryUrl() + "/" + panel.mergeEndUrlComboBox.getEditor().getItem().toString()); // NOI18N
        }

    }               
}