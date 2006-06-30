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

package org.netbeans.modules.i18n;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.openide.*;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.properties.PropertiesDataObject;

/**
 * Panel for selecting a properties file (by browsing a project tree). Also
 * allows to create a new file. Should be displayed as a dialog - using
 * getDialog(...) method. Substitutes use of NodeOperation.select which does
 * not provide a satisfactory UI.
 */
public class FileSelector extends JPanel implements PropertyChangeListener, ExplorerManager.Provider {

    // [this could be configurable to make the file selector more general]
    private static final String PROPERTIES_EXT = ".properties"; // NOI18N
    private static final String DEFAULT_BUNDLE_NAME = "Bundle"; // NOI18N

    private DataObject template;

    private ExplorerManager manager;

    private DataObject selectedDataObject;
    private DataFolder selectedFolder;
    private boolean confirmed;

    private JButton newButton;
    private JButton okButton;
    private JButton cancelButton;
    private JTextField fileNameTextField;

    public FileSelector(FileObject fileInProject, DataObject template) {
        this(SelectorUtils.bundlesNode(null, fileInProject, template == null), template);
    }

    private FileSelector(Node root, DataObject template) {
        this.template = template;

        manager = new ExplorerManager();
        manager.setRootContext(root);
        try {
            manager.setSelectedNodes (new Node[] { root });
        } catch(PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        manager.addPropertyChangeListener(this);

        if (template != null) {
            newButton = new JButton();
            Mnemonics.setLocalizedText(newButton, NbBundle.getMessage(FileSelector.class, "CTL_CreateNewButton")); // NOI18N
            newButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (selectedFolder == null)
                        return;

                    String fileName = fileNameTextField.getText();
                    try {
                        if (fileName.equals(""))
                            fileName = DEFAULT_BUNDLE_NAME; // NOI18N
                        else if (fileName.toLowerCase().endsWith(PROPERTIES_EXT))
                            fileName = fileName.substring(0, fileName.length()-PROPERTIES_EXT.length());

                        selectedDataObject = FileSelector.this.template.createFromTemplate(selectedFolder, fileName);
                        // select created
                        Node[] selected = manager.getSelectedNodes();
                        if (selected != null && selected.length == 1
                                && selected[0].getCookie(DataObject.class) == selectedFolder) {
                            Node[] sub = selected[0].getChildren().getNodes(true);
                            for (int i=0; i < sub.length; i++) {
                                if (sub[i].getCookie(DataObject.class) == selectedDataObject) {
                                    manager.setSelectedNodes(new Node[] { sub[i] });
                                    break;
                                }
                            }
                        }
                    }
                    catch (Exception ex) { // TODO report failure
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            });
            newButton.setEnabled(false);
        }
        okButton = new JButton(NbBundle.getMessage(FileSelector.class, "CTL_OKButton")); // NOI18N
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                confirmed = true;
            }
        });
        okButton.setEnabled(false);
        cancelButton = new JButton(NbBundle.getMessage(FileSelector.class, "CTL_CancelButton")); // NOI18N

        BeanTreeView treeView = new BeanTreeView ();
        treeView.setPopupAllowed(false);
        treeView.setDefaultActionAllowed(false);
        treeView.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
        treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelector.class, "ACSN_FileSelectorTreeView")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelector.class, "ACSD_FileSelectorTreeView")); // NOI18N
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelector.class, "ACSD_FileSelectorPanel")); // NOI18N

        // label and text field with mnemonic
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, NbBundle.getMessage(FileSelector.class, "LBL_FileName")); // NOI18N
        fileNameTextField = new JTextField();
        fileNameTextField.getDocument().addDocumentListener(new DocumentListener() { // NOI18N
            public void changedUpdate(DocumentEvent e) {
            }
            public void insertUpdate(DocumentEvent e) {
                checkFileName();
            }
            public void removeUpdate(DocumentEvent e) {
                checkFileName();
            }
        });
        label.setLabelFor(fileNameTextField);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutocreateGaps(true);
        layout.setAutocreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
            .add(treeView, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(label)
                .add(fileNameTextField)));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .add(treeView, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createParallelGroup(GroupLayout.BASELINE)
                .add(label)
                .add(fileNameTextField)));
    }

    /**
     * Creates a modal dialog containing the file selector with given title.
     * Use ActionListener to be informed about pressing OK button.
     * @param title
     * @param listener ActionListener attached to the OK button (if not null)
     */
    public Dialog getDialog(String title, ActionListener listener) {
        DialogDescriptor dd = new DialogDescriptor(
            this, title,  true,
            newButton != null ?
                new JButton[] { newButton, okButton, cancelButton } :
                new JButton[] { okButton, cancelButton },
            okButton,
            DialogDescriptor.DEFAULT_ALIGN, HelpCtx.DEFAULT_HELP,
            null
        );
        dd.setClosingOptions(new JButton[] { okButton, cancelButton });
        if (listener != null)
            okButton.addActionListener(listener);
        return DialogDisplayer.getDefault().createDialog(dd);
    }

    public void addNotify() {
        confirmed = false;
        super.addNotify();
    }

    boolean isConfirmed() {
        return confirmed;
    }

    public DataObject getSelectedDataObject() {
        return selectedDataObject;
    }

    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
            Node[] nodes = manager.getSelectedNodes();
            selectedDataObject = null;
            selectedFolder = null;
            if (nodes != null && nodes.length == 1) {
                DataObject dobj = (DataObject) nodes[0].getCookie(DataObject.class);
                if (dobj != null) {
                    if (dobj instanceof PropertiesDataObject) {
                        fileNameTextField.setText(dobj.getName());
                        selectedDataObject = dobj;
                        selectedFolder = dobj.getFolder();
                    }
                    else if (dobj instanceof DataFolder) {
                        fileNameTextField.setText(""); // NOI18N
                        selectedFolder = (DataFolder) dobj;
                    }
                    else selectedFolder = dobj.getFolder();
                }
            }
            okButton.setEnabled(selectedDataObject != null);
            if (newButton != null)
                newButton.setEnabled(selectedFolder != null
                                     && selectedDataObject == null
                                     && !checkForDefaultBundle());
        }
    }

    private boolean checkForDefaultBundle() {
        if (selectedFolder != null) {
            return selectedFolder.getPrimaryFile().getFileObject(DEFAULT_BUNDLE_NAME + PROPERTIES_EXT) != null;
        }
        return false;
    }

    private void checkFileName() {
        if (selectedFolder == null)
            return;

        selectedDataObject = null;
        String fileName = fileNameTextField.getText();
        if ("".equals(fileName)) { // NOI18N
            okButton.setEnabled(false);
            if (newButton != null)
                newButton.setEnabled(!checkForDefaultBundle());
        }
        else {
            if (!fileName.toLowerCase().endsWith(PROPERTIES_EXT))
                fileName = fileName + PROPERTIES_EXT;

            FileObject fo = selectedFolder.getPrimaryFile().getFileObject(fileName);
            if (fo != null) {
                try {
                    selectedDataObject = DataObject.find(fo);
                }
                catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }

            okButton.setEnabled(selectedDataObject != null);
            if (newButton != null)
                newButton.setEnabled(selectedDataObject == null);
        }
    }

    /**
     * Implementation of ExplorerManager.Provider. Needed for the tree view to work.
     */
    public ExplorerManager getExplorerManager() {
        return manager;
    }
}
