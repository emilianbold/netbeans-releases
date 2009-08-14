/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.subversion.ui.properties;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Peter Pis
 * @author Marian Petras
 */
public class SvnProperties implements ActionListener {

    /** Subversion properties that may be set only on directories */
    private static final String[] DIR_ONLY_PROPERTIES = new String[] {
                                                            "svn:ignore",
                                                            "svn:externals"};
 
    /** Subversion properties that may be set only on files (not directories) */
    private static final String[] FILE_ONLY_PROPERTIES = new String[] {
                                                            "svn:eol-style",
                                                            "svn:executable",
                                                            "svn:keywords",
                                                            "svn:needs-lock",
                                                            "svn:mime-type"};

    private PropertiesPanel panel;
    private File root;
    private PropertiesTable propTable;
    private SvnProgressSupport support;
    private boolean loadedFromFile;
    private File loadedValueFile;

    /** Creates a ew instance of SvnProperties */
    public SvnProperties(PropertiesPanel panel, PropertiesTable propTable, File root) {
        this.panel = panel;
        this.propTable = propTable;
        this.root = root;
        propTable.getTable().addMouseListener(new TableMouseListener());
        panel.btnRefresh.addActionListener(this);
        panel.btnAdd.addActionListener(this);
        panel.btnRemove.addActionListener(this);
        panel.btnBrowse.addActionListener(this);
        panel.comboName.setEditable(true);
        boolean rootIsDirectory = root.isDirectory();
        panel.setForDirectory(rootIsDirectory);
        if (rootIsDirectory) {
            panel.setIllegalPropertyNames(
                    FILE_ONLY_PROPERTIES,
                    "PropertiesPanel.errInvalidPropertyForDirectory");  //NOI18N
        } else {
            panel.setIllegalPropertyNames(
                    DIR_ONLY_PROPERTIES,
                    "PropertiesPanel.errInvalidPropertyForFile");       //NOI18N
        }
        setLoadedValueFile(null);
        initPropertyNameCbx();
        setLoadedFromFile(false);
        refreshProperties();
        panel.initInteraction();
    }

    public PropertiesPanel getPropertiesPanel() {
        return panel;
    }

    public void setPropertiesPanel(PropertiesPanel panel) {
        this.panel = panel;
    }

    public File getRoot() {
        return root;
    }

    public void setRoot(File root) {
        this.root = root;
    }

    public void setLoadedValueFile(File file) {
        this.loadedValueFile = file;
    }

    public File getLoadedValueFile() {
        return loadedValueFile;
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source.equals(panel.btnRefresh)) {
            refreshProperties();
        }

        if (source.equals(panel.btnAdd)) {
            setProperties();
        }

        if (source.equals(panel.btnRemove)) {
            removeProperties();
        }

        if (source.equals(panel.btnBrowse)) {
            loadFromFile();
        }
    }

    protected void initPropertyNameCbx() {
        if (panel.comboName.isEditable()) {
            panel.setPredefinedPropertyNames(root.isDirectory()
                                             ? DIR_ONLY_PROPERTIES
                                             : FILE_ONLY_PROPERTIES);
        }
    }

    protected String getPropertyValue() {
        return SvnUtils.fixLineEndings(panel.getPropertyValue());
    }

    protected String getPropertyName() {
        return panel.getPropertyName();
    }

    public boolean isLoadedFromFile() {
        return loadedFromFile;
    }

    public void setLoadedFromFile(boolean value) {
        loadedFromFile = value;
        if (loadedFromFile) {
            panel.setPropertyValueChangeListener(this);
        }
    }

    public void handleBinaryFile(File source) {
        setLoadedValueFile(source);
        StringBuffer txtValue = new StringBuffer();
        txtValue.append(NbBundle.getMessage(SvnProperties.class, "Binary_Content"));
        txtValue.append("\n");
        try {
            txtValue.append(source.getCanonicalPath());
        } catch (IOException ex) {
            Subversion.LOG.log(Level.SEVERE, null, ex);
        }
        panel.txtAreaValue.setText(txtValue.toString());
        setLoadedFromFile(true);
    }

    public void loadFromFile() {
        final JFileChooser chooser = new AccessibleJFileChooser(NbBundle.getMessage(SvnProperties.class, "ACSD_Properties"));
        chooser.setDialogTitle(NbBundle.getMessage(SvnProperties.class, "CTL_Load_Value_Title"));
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter[] fileFilters = chooser.getChoosableFileFilters();
        for (int i = 0; i < fileFilters.length; i++) {
            javax.swing.filechooser.FileFilter fileFilter = fileFilters[i];
            chooser.removeChoosableFileFilter(fileFilter);
        }

        chooser.setCurrentDirectory(root.getParentFile()); // NOI18N
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.exists();
            }
            public String getDescription() {
                return "";
            }
        });

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(SvnProperties.class, "MNE_LoadValue").charAt(0));
        chooser.setApproveButtonText(NbBundle.getMessage(SvnProperties.class, "CTL_LoadValue"));
        DialogDescriptor dd = new DialogDescriptor(chooser, NbBundle.getMessage(SvnProperties.class, "CTL_Load_Value_Title"));
        dd.setOptions(new Object[0]);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        chooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String state = e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    File source = chooser.getSelectedFile();

                    if (Utils.isFileContentText(source)) {
                        if (source.canRead()) {
                            StringWriter sw = new StringWriter();
                            try {
                                Utils.copyStreamsCloseAll(sw, new FileReader(source));
                                panel.txtAreaValue.setText(sw.toString());
                            } catch (IOException ex) {
                                Subversion.LOG.log(Level.SEVERE, null, ex);
                            }
                        }
                    } else {
                        handleBinaryFile(source);
                    }
                }
                dialog.dispose();
            }
        });
        dialog.setVisible(true);

    }

    protected void refreshProperties() {
        final SVNUrl repositoryUrl;
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        try {
            support = new SvnProgressSupport() {
                SvnClient client;
                ISVNProperty[] isvnProps;
                protected void perform() {
                    try {
                        client = Subversion.getInstance().getClient(repositoryUrl);
                        ISVNStatus status = client.getSingleStatus(root);
                        if(status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
                            return;
                        }
                        isvnProps = client.getProperties(root);
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, true, true);
                        return;
                    }
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            String[] propNames = new String[isvnProps.length];
                            SvnPropertiesNode[] svnProps = new SvnPropertiesNode[isvnProps.length];
                            for (int i = 0; i < isvnProps.length; i++) {
                                if (isvnProps[i] == null) {
                                    return;
                                }
                                String name = isvnProps[i].getName();
                                propNames[i] = name;
                                String value;
                                if (SvnUtils.isBinary(isvnProps[i].getData())) {
                                    value = org.openide.util.NbBundle.getMessage(SvnProperties.class, "Binary_Content");
                                } else {
                                    String tmp = isvnProps[i].getValue();
                                    value = tmp != null ? tmp : "";
                                }
                                svnProps[i] = new SvnPropertiesNode(name, value);
                            }
                            propTable.setNodes(svnProps);
                            panel.setExistingPropertyNames(propNames);
                        }
                    });
                }
            };
            support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(SvnProperties.class, "LBL_Properties_Progress"));
        } finally {
            support = null;
        }
    }

    public void setProperties() {
        final SVNUrl repositoryUrl;
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        try {
            support = new SvnProgressSupport() {
                SvnClient client;
                ISVNProperty[] isvnProps;
                protected void perform() {
                    try {
                        client = Subversion.getInstance().getClient(repositoryUrl);
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, true, true);
                        return;
                    }
                    boolean recursively = panel.cbxRecursively.isSelected();
                    try {
                        addFile(client, root, recursively);
                        if (isLoadedFromFile()) {
                            try {
                                client.propertySet(root, getPropertyName(), getLoadedValueFile(), recursively);
                            } catch (IOException ex) {
                                Subversion.LOG.log(Level.SEVERE, null, ex);
                                return;
                            }
                        } else {
                            client.propertySet(root, getPropertyName(), getPropertyValue(), recursively);
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, true, true);
                        return;
                    }
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            panel.comboName.getEditor().setItem("");
                            panel.txtAreaValue.setText("");
                        }
                    });
                }
            };
            support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(SvnProperties.class, "LBL_Properties_Progress"));
        } finally {
            support = null;
        }
        refreshProperties();
    }

    private void addFile(SvnClient client, File file, boolean recursively) throws SVNClientException {
        if(SvnUtils.isPartOfSubversionMetadata(file)) return;
        ISVNStatus status = client.getSingleStatus(file);
        if(status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
            client.addFile(file);
            if(recursively && file.isDirectory()) {
                File[] files = file.listFiles();
                if(files == null) return;
                for (File f : files) {
                    addFile(client, f, recursively);
                }
            }
        }
    }

    public void removeProperties() {
        final SVNUrl repositoryUrl;
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        final int[] rows = propTable.getSelectedItems();
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        try {
            support = new SvnProgressSupport() {
                SvnClient client;
                protected void perform() {
                    try {
                        client = Subversion.getInstance().getClient(repositoryUrl);
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, true, true);
                        return;
                    }

                    try {
                        boolean recursively = panel.cbxRecursively.isSelected();
                        SvnPropertiesNode[] svnPropertiesNodes = propTable.getNodes();
                        List<SvnPropertiesNode> lstSvnPropertiesNodes = Arrays.asList(svnPropertiesNodes);
                        for (int i = rows.length - 1; i >= 0; i--) {
                            String svnPropertyName = svnPropertiesNodes[propTable.getModelIndex(rows[i])].getName();
                            client.propertyDel(root, svnPropertyName, recursively);
                            try {
                                lstSvnPropertiesNodes.remove(svnPropertiesNodes[propTable.getModelIndex(rows[i])]);
                            } catch (UnsupportedOperationException e) {
                            }
                        }
                        SvnPropertiesNode[] remainingNodes
                                = (SvnPropertiesNode[]) lstSvnPropertiesNodes.toArray();
                        propTable.setNodes(remainingNodes);

                        if (remainingNodes.length == 0) {
                            panel.setExistingPropertyNames(new String[0]);
                        } else {
                            String[] propNames = new String[remainingNodes.length];
                            for (int i = 0; i < propNames.length; i++) {
                                propNames[i] = remainingNodes[i].getName();
                            }
                            panel.setExistingPropertyNames(propNames);
                        }
                        //refreshProperties();
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, true, true);
                        return;
                    }
                }
            };
            support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(SvnProperties.class, "LBL_Properties_Progress"));
        } finally {
            support = null;
        }
        refreshProperties();
    }

    public void propertyValueChanged() {
        assert isLoadedFromFile();
        panel.removePropertyValueChangeListener();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                panel.txtAreaValue.setText("");
            }
        });
        setLoadedFromFile(false);
    }

    public class TableMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent event) {
            //super.mouseClicked(arg0);
            if (event.getClickCount() == 2) {
                int[] rows = propTable.getSelectedItems();
                SvnPropertiesNode[] svnPropertiesNodes = propTable.getNodes();
                if (svnPropertiesNodes == null)
                    return;
                final String svnPropertyName = svnPropertiesNodes[propTable.getModelIndex(rows[0])].getName();
                final String svnPropertyValue = svnPropertiesNodes[propTable.getModelIndex(rows[0])].getValue();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        panel.comboName.getEditor().setItem(svnPropertyName);
                        panel.txtAreaValue.setText(svnPropertyValue);
                    }
                });
            }
        }
}
}