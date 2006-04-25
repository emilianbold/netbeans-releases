/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.copy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.MalformedURLException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.openide.ErrorManager;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class Merge extends CopyDialog implements ItemListener {
        
    private String MERGE_START_URL_HISTORY_KEY = Merge.class.getName() + "_merge_from";
    private String MERGE_END_URL_HISTORY_KEY = Merge.class.getName() + "_merge_after";
    
    public Merge(RepositoryFile repositoryRoot, File root) {
        super(new MergePanel(), "Merge " + root.getName() + " to...", "Merge");

        MergePanel panel = getMergePanel();
        panel.mergeFiledsPanel.setLayout(new BorderLayout());

        panel.typeComboBox.setModel(new DefaultComboBoxModel(
                new MergeType[] {
                       new MergeOneFolderType(repositoryRoot, root),
                       new MergeTwoFoldersType(repositoryRoot, root),
                       new MergeSinceOriginType(repositoryRoot, root)
                }
        ));
        panel.typeComboBox.setRenderer(createTypeRenderer());
        panel.typeComboBox.addItemListener(this);
        mergeTypeSelected(panel, ((MergeType) panel.typeComboBox.getSelectedItem()));
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
        MergePanel panel = getMergePanel();        
        MergeType type = (MergeType) e.getItem();
        
        if(e.getStateChange() == e.DESELECTED) {
            RepositoryPaths path = type.getMergeStartRepositoryPath();
            if(path!=null) {
                path.removePropertyChangeListener(this);
            }
            path = type.getMergeEndRepositoryPath();
            if(path!=null) {
                path.removePropertyChangeListener(this);
            }
            return;
        }
        mergeTypeSelected(panel, type);

        panel.typeDescriptionLabel.setText(type.getDescription());
    }

    private void mergeTypeSelected(MergePanel panel, MergeType type) {
        panel.mergeFiledsPanel.removeAll();
        panel.mergeFiledsPanel.add(type.getFieldsPanel(), BorderLayout.CENTER);
        panel.mergeFiledsPanel.revalidate();

        RepositoryPaths path = type.getMergeStartRepositoryPath();
        if(path!=null) {
            path.addPropertyChangeListener(this);
        }
        path = type.getMergeEndRepositoryPath();
        if(path!=null) {
            path.addPropertyChangeListener(this);
        }

        resetUrlComboBoxes();
        setupUrlComboBox(type.getStartUrlComboBox(), MERGE_START_URL_HISTORY_KEY);
        setupUrlComboBox(type.getEndUrlComboBox(), MERGE_END_URL_HISTORY_KEY);
    }

    private static abstract class MergeType {

        private RepositoryPaths mergeStartRepositoryPaths;
        private RepositoryPaths mergeEndRepositoryPaths;

        void init(RepositoryPaths mergeStartRepositoryPaths, JLabel mergeStartRepositoryFolderLabel, RepositoryPaths mergeEndRepositoryPaths, JLabel mergeEndRepositoryFolderLabel, File root) {
            this.mergeStartRepositoryPaths = mergeStartRepositoryPaths;
            init(mergeStartRepositoryPaths, mergeStartRepositoryFolderLabel, root);
            this.mergeEndRepositoryPaths = mergeEndRepositoryPaths;
            init(mergeEndRepositoryPaths, mergeEndRepositoryFolderLabel, root);
        }

        protected static void init(RepositoryPaths paths, JLabel label, File root) {
            if(root.isFile()) {
                if(label!=null) {
                    label.setText("Repository File");
                }
                paths.setupBrowserBehavior(true, true, true);
            } else {
                paths.setupBrowserBehavior(true, false, false);
            }
        }

        public abstract String getDisplayName();
        public abstract String getDescription();
        public abstract JPanel getFieldsPanel();
        public abstract JComboBox getStartUrlComboBox();
        public abstract JComboBox getEndUrlComboBox();

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
    }

    private class MergeTwoFoldersType extends MergeType {

        private MergeTwoFoldersPanel panel;

        public MergeTwoFoldersType(RepositoryFile repositoryRoot, File root) {
            MergeTwoFoldersPanel panel = (MergeTwoFoldersPanel) getFieldsPanel();
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
        }

        public String getDisplayName() {
            return "Two Repository Folders";
        }

        public String getDescription() {
            return "Merge into local folder changes between two repository folders.";
        }

        public JPanel getFieldsPanel() {
            if(panel == null) {
                panel = new MergeTwoFoldersPanel();
            }
            return panel;
        }

        public JComboBox getStartUrlComboBox() {
            return panel.mergeStartUrlComboBox;
        }

        public JComboBox getEndUrlComboBox() {
            return panel.mergeEndUrlComboBox;
        }

    }

    private static class MergeOneFolderType extends MergeType {

        private RepositoryPaths mergeEndRepositoryPaths;
        private MergeOneFolderPanel panel;

        /** Creates a new instance of MergeOneFolderType */
        public MergeOneFolderType(RepositoryFile repositoryRoot, File root) {
            MergeOneFolderPanel panel = (MergeOneFolderPanel) getFieldsPanel();
            RepositoryPaths mergeStartRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeStartUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeStartBrowseButton,
                    panel.mergeStartRevisionTextField,
                    panel.mergeStartSearchButton
                );

            mergeEndRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    null,
                    null,
                    panel.mergeEndRevisionTextField,
                    panel.mergeEndSearchButton
                );

            init(mergeStartRepositoryPaths,
                 panel.mergeStartRepositoryFolderLabel,
                 mergeEndRepositoryPaths,
                 null,
                 root);
        }

        public SVNUrl getMergeEndUrl() {
            // in this case iths the same folder url
            return getMergeStartUrl();
        }

        public String getDisplayName() {
            return "One Repository Folder";
        }

        public String getDescription() {
            return "Merge into local folder changes from one repository folder.";
        }

        public JPanel getFieldsPanel() {
            if(panel == null) {
                panel = new MergeOneFolderPanel();
            }
            return panel;
        }

        public JComboBox getStartUrlComboBox() {
            return panel.mergeStartUrlComboBox;
        }

        public JComboBox getEndUrlComboBox() {
            return null;
        }

    }

    private static class MergeSinceOriginType extends MergeType {

        private MergeSinceOriginPanel panel;
        private RepositoryPaths mergeEndRepositoryPaths;

        public MergeSinceOriginType(RepositoryFile repositoryRoot, File root) {
            MergeSinceOriginPanel panel = (MergeSinceOriginPanel) getFieldsPanel();

            mergeEndRepositoryPaths =
                new RepositoryPaths(
                    repositoryRoot,
                    (JTextComponent) panel.mergeEndUrlComboBox.getEditor().getEditorComponent(),
                    panel.mergeEndBrowseButton,
                    panel.mergeEndRevisionTextField,
                    panel.mergeEndSearchButton
                );

            init(mergeEndRepositoryPaths, panel.mergeEndRepositoryFolderLabel, root);
        }

        public String getDisplayName() {
            return "One Repository Folder Since Its Origin";
        }

        public String getDescription() {
            return "Merge into local folder changes from one repository folder since its origin.";
        }

        public JPanel getFieldsPanel() {
            if(panel == null) {
                panel = new MergeSinceOriginPanel();
            }
            return panel;
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

    }    
}
