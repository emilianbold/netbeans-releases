/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public final class SvnProperties implements ActionListener {

    /** Subversion properties that may be set only on directories */
    private static final HashSet<String> DIR_ONLY_PROPERTIES = new HashSet<String>(Arrays.asList(new String[] {
                                                            "svn:ignore",
                                                            "svn:externals"}));
 
    /** Subversion properties that may be set only on files (not directories) */
    private static final HashSet<String> FILE_ONLY_PROPERTIES = new HashSet<String>(Arrays.asList(new String[] {
                                                            "svn:eol-style",
                                                            "svn:executable",
                                                            "svn:keywords",
                                                            "svn:needs-lock",
                                                            "svn:mime-type"}));

    private static final HashSet<String> MIXED_PROPERTIES = new HashSet<String>(DIR_ONLY_PROPERTIES.size() + FILE_ONLY_PROPERTIES.size());
    static {
        MIXED_PROPERTIES.addAll(DIR_ONLY_PROPERTIES);
        MIXED_PROPERTIES.addAll(FILE_ONLY_PROPERTIES);
    }

    private PropertiesPanel panel;
    private File[] roots;
    private PropertiesTable propTable;
    private SvnProgressSupport support;
    private boolean loadedFromFile;
    private File loadedValueFile;
    private final Set<File> folders = new HashSet<File>();
    private final Set<File> files = new HashSet<File>();
    private final Map<String, Set<File>> filesPerProperty = new HashMap<String, Set<File>>();

    /** Creates a ew instance of SvnProperties */
    public SvnProperties(PropertiesPanel panel, PropertiesTable propTable, File[] files) {
        this.panel = panel;
        this.propTable = propTable;
        this.roots = files;
        propTable.getTable().addMouseListener(new TableMouseListener());
        panel.btnRefresh.addActionListener(this);
        panel.btnAdd.addActionListener(this);
        panel.btnRemove.addActionListener(this);
        panel.btnBrowse.addActionListener(this);
        panel.comboName.setEditable(true);
        for (File f : files) {
            if (f.isDirectory()) {
                folders.add(f);
            } else {
                this.files.add(f);
            }
        }
        panel.setForDirectory(!folders.isEmpty());
        if (folders.isEmpty()) {
            panel.setIllegalPropertyNames(
                    DIR_ONLY_PROPERTIES.toArray(new String[DIR_ONLY_PROPERTIES.size()]),
                    "PropertiesPanel.errInvalidPropertyForFile");       //NOI18N
        } else {
            panel.setRecursiveProperties(FILE_ONLY_PROPERTIES);
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

    public File[] getFiles () {
        return roots;
    }

    private void setLoadedValueFile(File file) {
        this.loadedValueFile = file;
    }

    private File getLoadedValueFile() {
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
            panel.setPredefinedPropertyNames(folders.isEmpty()
                                             ? FILE_ONLY_PROPERTIES.toArray(new String[FILE_ONLY_PROPERTIES.size()])
                                             : MIXED_PROPERTIES.toArray(new String[MIXED_PROPERTIES.size()]));
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
        StringBuilder txtValue = new StringBuilder();
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

        chooser.setCurrentDirectory(roots[0].getParentFile()); // NOI18N
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
            repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        try {
            support = new SvnProgressSupport() {
                SvnClient client;
                HashMap<String, String> properties;
                protected void perform() {
                    try {
                        client = Subversion.getInstance().getClient(repositoryUrl);
                        properties = new HashMap<String, String>();
                        for (File f : roots) {
                            ISVNStatus status = client.getSingleStatus(f);
                            if (!status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
                                addProperties(f, client.getProperties(f));
                            }
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, true, true);
                        return;
                    }
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            String[] propNames = new String[properties.size()];
                            SvnPropertiesNode[] svnProps = new SvnPropertiesNode[properties.size()];
                            int i = 0;
                            for (Map.Entry<String, String> e : properties.entrySet()) {
                                String name = e.getKey();
                                propNames[i] = name;
                                String value = e.getValue();
                                svnProps[i] = new SvnPropertiesNode(name, value);
                                ++i;
                            }
                            propTable.setNodes(svnProps);
                            panel.setExistingPropertyNames(propNames);
                        }
                    });
                }

                private void addProperties (File file, ISVNProperty[] toAddProps) {
                    for (ISVNProperty prop : toAddProps) {
                        String propName = prop.getName();
                        String propValue;
                        if (SvnUtils.isBinary(prop.getData())) {
                            propValue = org.openide.util.NbBundle.getMessage(SvnProperties.class, "Binary_Content"); //NOI18N
                        } else {
                            String tmp = prop.getValue();
                            propValue = tmp != null ? tmp : ""; //NOI18N
                        }
                        String existingValue = properties.get(propName);
                        if (existingValue != null && !existingValue.equals(propValue)) {
                            propValue = org.openide.util.NbBundle.getMessage(SvnProperties.class, "SvnProperties.VariousValues"); //NOI18N"
                        }
                        properties.put(propName, propValue);
                        Set<File> filesPerProp = filesPerProperty.get(propName);
                        if (filesPerProp == null) {
                            filesPerProp = new HashSet<File>();
                            filesPerProperty.put(propName, filesPerProp);
                        }
                        filesPerProp.add(file);
                    }
                }
            };
            support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(SvnProperties.class, "LBL_Properties_Progress"));
        } finally {
            support = null;
        }
    }

    private void setProperties() {
        final SVNUrl repositoryUrl;
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);
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
                        String propName = getPropertyName();
                        for (File root : getAllowedFiles(propName, recursively)) {
                            addFile(client, root, recursively);
                            if (isLoadedFromFile()) {
                                try {
                                    client.propertySet(root, propName, getLoadedValueFile(), recursively);
                                } catch (IOException ex) {
                                    Subversion.LOG.log(Level.SEVERE, null, ex);
                                    return;
                                }
                            } else {
                                client.propertySet(root, propName, getPropertyValue(), recursively);
                            }
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

    private File[] getAllowedFiles (String propertyName, boolean recursively) {
        List<File> fileList = new LinkedList<File>();
        for (File root : roots) {
            boolean isFile = files.contains(root);
            if (!(isFile && DIR_ONLY_PROPERTIES.contains(propertyName) // do not set folder properties on files
                    || !isFile && !recursively && FILE_ONLY_PROPERTIES.contains(propertyName))) { // do not set file properties on folders
                fileList.add(root);
            }
        }
        return fileList.toArray(new File[fileList.size()]);
    }

    private File[] getFilesWithProperty (String propertyName) {
        Set<File> filesWithProperty = filesPerProperty.get(propertyName);
        Set<File> fileList = new HashSet<File>();
        if (filesWithProperty != null) {
            fileList.addAll(filesWithProperty);
        }
        return fileList.toArray(new File[fileList.size()]);
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

    private void removeProperties() {
        final SVNUrl repositoryUrl;
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);
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
                            for (File root : getFilesWithProperty(svnPropertyName)) {
                                client.propertyDel(root, svnPropertyName, recursively);
                            }
                            try {
                                lstSvnPropertiesNodes.remove(svnPropertiesNodes[propTable.getModelIndex(rows[i])]);
                            } catch (UnsupportedOperationException e) {
                            }
                        }
                        final SvnPropertiesNode[] remainingNodes
                                = (SvnPropertiesNode[]) lstSvnPropertiesNodes.toArray();
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
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
                            }
                        });
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
