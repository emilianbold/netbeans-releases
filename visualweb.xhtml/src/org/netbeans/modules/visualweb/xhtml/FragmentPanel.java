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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.xhtml;

import org.netbeans.modules.visualweb.api.insync.InSyncService;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.project.Project;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import javax.swing.JDialog;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

/**
 * Panel for selecting a page fragment
 *
 * @author  Tor Norbye
 */
public class FragmentPanel extends javax.swing.JPanel implements ActionListener, ItemListener {
    private DesignProperty prop;

    /** Creates new form FragmentPanel */
    public FragmentPanel(DesignProperty prop) {
        this.prop = prop;
        initComponents();
        createButton.addActionListener(this);
        comboBox.addItemListener(this);
        addComboFragments();
    }

    /** Sync gui to property */
    private void update() {
    }

    private Project getProject() {
        DesignContext context = prop.getDesignBean().getDesignContext();
        DesignProject lp = context.getProject();
//        FacesModelSet set = (FacesModelSet)lp;
//        return set.getProject();
        return InSyncService.getProvider().getProjectForDesignProject(lp);
    }

    private void addComboFragments() {
        comboBox.setEditable(false);
//        RequestProcessor.postRequest(new Runnable() {
//                public void run() {
        DesignContext context = prop.getDesignBean().getDesignContext();
        DataObject curr = null;
        if (context != null) {
//            LiveUnit lu = (LiveUnit)context;
//            FacesPageUnit fu = (FacesPageUnit)lu.getBeansUnit();
//            MarkupUnit mu = fu.getPageUnit();
//            FileObject fo = mu.getFileObject();
            FileObject fo = InSyncService.getProvider().getMarkupFileObjectForDesignContext(context);
            try {
                curr = DataObject.find(fo);
            } catch (DataObjectNotFoundException ce) {
            }
        }
//        final ArrayList formList = new ArrayList();
        Project project = getProject();
        if (project == null) {
            return;
        }
        FileObject webforms = JsfProjectUtils.getDocumentRoot(project);
        if (webforms == null)
            return;
//        FacesModelSet modelset = FacesModelSet.getInstance(project);
        DataObject folderObj = null;
        try {
            folderObj = DataObject.find(webforms);
        } catch (DataObjectNotFoundException e) {
            return;
        }
        final ArrayList formList = new ArrayList();
        if (folderObj instanceof DataFolder) {
            addSubFolderFragments((DataFolder)folderObj, formList, /*modelset,*/ curr);
        }
//                      SwingUtilities.invokeLater(new Runnable() {
//                            public void run() {
        String[] comboValues = (String[])formList.toArray(new String[formList.size()]);
        comboBox.setModel(new DefaultComboBoxModel(comboValues));
        comboBox.setEditable(true);
//                        int index = formList.indexOf(prop.getValue().toString());
        if (prop.getValue() != null) {
            comboBox.setSelectedItem(prop.getValue().toString());
        } else if (comboValues.length > 0) {
            prop.setValue(comboValues[0]);
        }
//                            }
//                        }
//                    );

//                }
//            }
//         );
    }

    private void addSubFolderFragments(DataFolder folder, List formList,
            /*FacesModelSet modelset,*/ DataObject current) {
        DataObject[] children = folder.getChildren();
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof DataFolder) {
                addSubFolderFragments((DataFolder)children[i], formList, /*modelset,*/ current);
            } else {
                DataObject d = children[i];

                if (d == current) {
                    continue;
                }
                // See if it's a fragment

                if (d.getPrimaryFile().getNameExt().endsWith(".jspf")) {
                    String path = computePathFromTo(current, d);
                    formList.add(path);
                }
            }
        }
    }

    public static String computePathFromTo(DataObject from, DataObject to) {
        FileObject fromFile = from.getPrimaryFile();
        FileObject toFile = to.getPrimaryFile();
        ArrayList fromPathList = getPathList(fromFile);
        ArrayList toPathList = getPathList(toFile);
        int index = 0;
        // find the first non matching sub dir
        for (; index < fromPathList.size() && index < toPathList.size();
        index++) {
            if (!fromPathList.get(index).equals(toPathList.get(index))) {
                break;
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        // create a file that goes up to match found
        for (int i = index; i < fromPathList.size(); i++) {
            stringBuffer.append("../"); // NOI18N
        }
        // create a file that goes down from match found
        for (int i = index; i < toPathList.size(); i++) {
            stringBuffer.append(toPathList.get(i));
            stringBuffer.append("/"); // NOI18N
        }
        if (toFile.isData()) {
            stringBuffer.append(toFile.getNameExt());
        }
        return stringBuffer.toString();
    }

    public static ArrayList getPathList(FileObject file) {
        if (!file.isFolder()) {
            file = file.getParent();
        }
        ArrayList result = new ArrayList(4);
        while (file != null) {
            result.add(file.getName());
            file = file.getParent();
        }
        Collections.reverse(result);
        return result;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        comboBox = new javax.swing.JComboBox();
        createButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(NbBundle.getMessage(FragmentPanel.class, "JspPageDesc")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(jLabel1, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/xhtml/Bundle"); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(bundle.getString("PF_LBL_ACCESS_DESC")); // NOI18N

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/xhtml/Bundle").getString("JspPageMnemonic").charAt(0));
        jLabel2.setLabelFor(comboBox);
        jLabel2.setText(NbBundle.getMessage(FragmentPanel.class, "JspPage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 6);
        add(jLabel2, gridBagConstraints);

        comboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(comboBox, gridBagConstraints);
        comboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FragmentPanel.class, "PF_COMBOBOX_ACCESS_NAME")); // NOI18N
        comboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FragmentPanel.class, "PF_COMBOBOX_ACCESS_DESC")); // NOI18N

        createButton.setMnemonic(org.openide.util.NbBundle.getMessage(FragmentPanel.class, "CREATE_NEW_PF_BUTTON_MNEMONIC").charAt(0));
        createButton.setText(NbBundle.getMessage(FragmentPanel.class, "CreateNew")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 11);
        add(createButton, gridBagConstraints);
        createButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FragmentPanel.class, "CREATE_NEW_PF_BUTTON_ACCESS_DESC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Implements ActionListener
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource() == createButton) {
            String name = createPageFragment();
            if (name != null) {
                //addComboFragments();
                comboBox.setSelectedItem(name);
                prop.setValue(name);
                // TODO - add to the combo list too?
            }
        }
    }

    // Implements ItemListener
    public void itemStateChanged(java.awt.event.ItemEvent e) {
        prop.setValue(comboBox.getSelectedItem().toString());
    }


    /** Create a pagefragment and return its URL string - or null
     * if creating the webform didn't succeed.
     */
    private String createPageFragment() {
        DataFolder folderObj = null;
        Project project = getProject();
        if (project != null) {
            FileObject webforms = JsfProjectUtils.getDocumentRoot(project);
            try {
                folderObj = (DataFolder)DataObject.find(webforms);
            } catch (DataObjectNotFoundException e) {
            }
        }
        if (prop != null) {
//            LiveUnit unit = (LiveUnit)prop.getDesignBean().getDesignContext();
//            if (unit.getBeansUnit() instanceof FacesPageUnit) {
//                FacesPageUnit fpu = (FacesPageUnit)unit.getBeansUnit();
//                DataObject dobj = fpu.getPageUnit().getDataObject();
            FileObject fo = InSyncService.getProvider().getMarkupFileObjectForDesignContext(prop.getDesignBean().getDesignContext());
            if (fo != null) {
                DataObject dobj;
                try {
                    dobj = DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    dobj = null;
                }
                if (dobj != null) {
                    DataFolder df = dobj.getFolder();
                    if (df != null) {
                        folderObj = df;
                    }
                }
            }
        }
        String error = null;
        String name = null;
        //!CQ This is bad to have this base name buried in the source here... should talk to project some how..
        FormNamePanel panel = new FormNamePanel(project, "Fragment");

        String title = NbBundle.getMessage(FragmentPanel.class, "CreateFragment"); // NOI18N
        DialogDescriptor dlg = new DialogDescriptor(
                panel,
                title,
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, // DialogDescriptor.BOTTOM_ALIGN,
                null, //new HelpCtx("new_page_fragment"), // NOI18N
                null);

        JDialog dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);
        dialog.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(FragmentPanel.class, "CreateFragmentAccessibleDesc")); // NOI18N
        panel.setDescriptor(dlg);
        dialog.show();
        String answer = dlg.getValue().toString();
        if (!dlg.getValue().equals(DialogDescriptor.OK_OPTION)) {
            // Cancel, or Esc: do nothing
            return null;
        }
        name = panel.getName();

        // Check name - shouldn't be necessary, form should enforce it.
        boolean validName;
        validName = JsfProjectUtils.isValidJavaFileName(name);
        if (!validName) {
            return null;
        }

        // Create files
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String tmpl = "Templates/JSF/PageFragment.jspf"; // NOI18N
            FileObject fo = fs.findResource(tmpl);
            if (fo == null)
                throw new IOException("Can't find template FileObject for " + tmpl);  // NOI18N
            DataObject webformTemplate = DataObject.find(fo);
            DataObject webform = webformTemplate.createFromTemplate(folderObj, name);

            // Next, try to access the insync units! This is important for
            // bug 4960018; the backing file template is empty so we've gotta
            // force insync to "create" it
            if (webform != null) {
//                FacesModelSet modelset = FacesModelSet.getInstance(project);
//                FacesModel model = modelset.getFacesModel(webform.getPrimaryFile());
//                if (model == null) {
//                    ErrorManager.getDefault().log(webform + " has no insync Model!");
//                }
                InSyncService.getProvider().initModelsForWebformFile(project, webform.getPrimaryFile());
            }

            return webform.getPrimaryFile().getNameExt();

        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comboBox;
    private javax.swing.JButton createButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
