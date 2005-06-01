/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.project;

import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.ErrorManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.xml.XMLUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.versioning.system.cvss.settings.CvsRootSettings;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.command.importcmd.ImportCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;

import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.util.Set;
import java.util.Vector;

/**
 * Imports project into CVS repository.
 *
 * @author Petr Kuzel
 */
public final class AddToRepositoryAction extends NodeAction {

    public String getName() {
        return NbBundle.getMessage(AddToRepositoryAction.class, "BK0006");
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    protected void performAction(Node[] nodes) {
        if (nodes.length == 1) {
            Project project = (Project) nodes[0].getLookup().lookup(Project.class);
            if (project != null) {
                Sources sources = (Sources) project.getLookup().lookup(Sources.class);
                if (sources != null) {
                    SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                    if (groups.length == 1) {
                        FileObject root = groups[0].getRootFolder();
                        File importDirectory = FileUtil.toFile(root);
                        if (importDirectory != null) {

                            // try to detect some resonable defaults for cvs root and repository

                            File parent = importDirectory.getParentFile();
                            File parent_cvsRoot = new File(parent, "CVS/Root");  // NOI18N
                            File parent_cvsRepo = new File(parent, "CVS/Repository");  // NOI18N
                            String cvsRoot = null;
                            String cvsRepository = null;
                            if (parent_cvsRepo.isFile() && parent_cvsRoot.isFile()) {
                                BufferedReader r = null;
                                try {
                                    r = new BufferedReader((new FileReader(parent_cvsRoot)));
                                    cvsRoot = r.readLine();
                                } catch (IOException e) {
                                    ErrorManager err = ErrorManager.getDefault();
                                    err.annotate(e, "Cannot read CVS/Root");
                                    err.notify(e);
                                } finally {
                                    if (r != null) {
                                        try {
                                            r.close();
                                        } catch (IOException alreadyClosed) {
                                        }
                                    }
                                }

                                try {
                                    r = new BufferedReader((new FileReader(parent_cvsRepo)));
                                    cvsRepository = r.readLine();
                                } catch (IOException e) {
                                    ErrorManager err = ErrorManager.getDefault();
                                    err.annotate(e, "Cannot read CVS/Repository");
                                    err.notify(e);
                                } finally {
                                    if (r != null) {
                                        try {
                                            r.close();
                                        } catch (IOException alreadyClosed) {
                                        }
                                    }
                                }
                            }

                            ImportPanel importPanel = new ImportPanel();
                            if (cvsRepository != null) {
                                importPanel.moduleTextField.setText(cvsRepository + "/" + root.getName());
                            } else {
                                importPanel.moduleTextField.setText(root.getName());
                            }
                            Set roots = CvsRootSettings.listCvsRoots();
                            Vector vector = new Vector();
                            if (cvsRoot != null) {
                                vector.add(cvsRoot);
                            } else {
                                String txt = NbBundle.getMessage(AddToRepositoryAction.class, "BK0008");
                                vector.add(txt);
                            }
                            vector.addAll(roots);
                            DefaultComboBoxModel model = new DefaultComboBoxModel(vector);
                            importPanel.rootComboBox.setModel(model);

                            // TODO add user input validation

                            importPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

                            String title = NbBundle.getMessage(AddToRepositoryAction.class, "BK0007");
                            DialogDescriptor descriptor = new DialogDescriptor(importPanel, title);
                            descriptor.setModal(true);
                            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                            dialog.show();
                            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {

                                String logMessage = importPanel.commentTextArea.getText();
                                String module = importPanel.moduleTextField.getText();
                                String vendorTag = "default_vendor";
                                String releaseTag = "default_release";
                                String selectedRoot = (String) importPanel.rootComboBox.getSelectedItem();

                                GlobalOptions gtx = new GlobalOptions();
                                gtx.setCVSRoot(selectedRoot);
                                ImportCommand importCommand = new ImportCommand();
                                importCommand.setModule(module);
                                importCommand.setLogMessage(logMessage);
                                importCommand.setVendorTag(vendorTag);
                                importCommand.setReleaseTag(releaseTag);
                                importCommand.setImportDirectory(importDirectory.getPath());
                                ImportExecutor executor = new ImportExecutor(importCommand, gtx);
                                executor.execute();
                            }

                        }
                    } else {
                        StringBuffer paths = new StringBuffer();
                        paths.append("<dl>"); // NOi18N
                        for (int i = 0; i < groups.length; i++) {
                            SourceGroup group = groups[i];
                            FileObject fo = group.getRootFolder();
                            String name = group.getDisplayName();
                            paths.append("<dt>").append(escape(name)).append("</dt>");  // NOi18N
                            paths.append("<dd>").append(escape(fo.getPath())).append("</dd>");  // NOI18N
                        }
                        paths.append("</dl>"); // NOi18N
                        String msg = NbBundle.getMessage(AddToRepositoryAction.class, "BK0004", paths.toString());
                        NotifyDescriptor desc = new NotifyDescriptor.Message(msg);
                        DialogDisplayer.getDefault().notify(desc);

                        // implementation sketch ...
                        // MetadataAttic.scheduleFolder();
                    }
                }
            }
        }
    }

    private static String escape(String path) {
        try {
            return XMLUtil.toElementContent(path);
        } catch (CharConversionException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Can not HTML escape '" + path + "'");  // NOI18N
            err.notify(e);
        }
        return NbBundle.getMessage(AddToRepositoryAction.class, "BK0005");
    }


    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] nodes) {
        if (nodes.length > 0) {
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (Utils.isVersionedProject(node)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
