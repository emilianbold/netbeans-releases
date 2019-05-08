/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelui.actions;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.debug.CndDiagnosticProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 */
public class CodeModelDiagnosticAction extends ProjectActionBase {
    private final static Logger LOG = Logger.getLogger("CodeModelDiagnosticAction"); // NOI18N
    public CodeModelDiagnosticAction() {
        super(true);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_CodeModelDiagnostic"); //NOI18N
    }

    @Override
    protected boolean isEnabledEx(Node[] activatedNodes, Collection<CsmProject> projects) {
        if (super.isEnabledEx(activatedNodes, projects)) {
            return true;
        }
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(NativeFileItemSet.class) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void performAction(Collection<CsmProject> csmProjects) {
        Node[] activatedNodes = getActivatedNodes();
        List<Object> lookupObjects = new ArrayList<Object>();
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        Document doc = null;
        if (lastFocusedComponent != null) {
            lookupObjects.add(lastFocusedComponent);
            doc = lastFocusedComponent.getDocument();
        }
        List<CsmFile> files = new ArrayList<CsmFile>();
        if (activatedNodes != null) {
            lookupObjects.addAll(Arrays.asList(activatedNodes));
            for (Node node : activatedNodes) {
                final DataObject dob = node.getLookup().lookup(DataObject.class);
                if (dob != null) {
                    lookupObjects.add(dob);
                    CsmFile[] csmFiles = CsmUtilities.getCsmFiles(dob, false, false);
                    if (csmFiles != null) {
                        files.addAll(Arrays.asList(csmFiles));
                    }
                }
            }
        }
        if (doc != null) {
            lookupObjects.add(doc);
            if (files.isEmpty()) {
                // add from editor
                CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
                if (csmFile != null) {
                    files.add(csmFile);
                }
                DataObject dob = NbEditorUtilities.getDataObject(doc);
                if (dob != null && !lookupObjects.contains(dob)) {
                    lookupObjects.add(dob);
                }
            }
        }
        lookupObjects.addAll(csmProjects);
        lookupObjects.addAll(files);
        LOG.log(Level.INFO, "perform actions on {0}\n nodes={1}\n", new Object[]{csmProjects, activatedNodes});
        if (!lookupObjects.isEmpty()) {
            JPanel pnl = createPanel();
            DialogDescriptor descr = new DialogDescriptor(pnl, "C/C++ Diagnostics");// NOI18N
            NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(descr));
            if (descr.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            Lookup context = Lookups.fixed(lookupObjects.toArray(new Object[lookupObjects.size()]));
            String tmpDir = System.getProperty("java.io.tmpdir"); // NOI18N
            if (tmpDir == null) {
                tmpDir = "/var/tmp";// NOI18N
            }
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");// NOI18N
                String date = df.format(new Date());
                File tmpFile;
                try {
                    File file = new File(tmpDir, "cnd_diagnostics_" + date + ".txt");// NOI18N
                    file.delete();
                    file.createNewFile();
                    tmpFile = file;
                } catch (IOException e) {
                    tmpFile = File.createTempFile("cnd_diagnostics_", ".txt", new File(tmpDir));// NOI18N
                }
                PrintWriter pw = new PrintWriter(tmpFile);
                String taskName = "Cnd Diagnostics - " + tmpFile.getName(); // NOI18N
                InputOutput io = IOProvider.getDefault().getIO(taskName, false); // NOI18N
                io.select();
                final OutputWriter out = io.getOut();
                final OutputWriter err = io.getErr();
                err.printf("dumping cnd diagnostics into %s%n", tmpFile);// NOI18N
                int i = 0;
                for (CsmFile csmFile : files) {
                    pw.printf("file [%d] [version=%d] [%s] of class %s%n", i++,  // NOI18N
                            CsmFileInfoQuery.getDefault().getFileVersion(csmFile),
                            csmFile.getAbsolutePath(), csmFile.getClass().getName());
                }
                if (doc != null) {
                    DataObject dob = NbEditorUtilities.getDataObject(doc);
                    boolean modified = false;
                    if (dob != null) {
                        modified = dob.isModified();
                    }
                    pw.printf("document version=%d timestamp=%s caret=[%d-%d]. Is modified? %s%n", // NOI18N
                            DocumentUtilities.getDocumentVersion(doc), DocumentUtilities.getDocumentTimestamp(doc),
                            lastFocusedComponent.getSelectionStart(), lastFocusedComponent.getSelectionEnd(), modified);
                }
                for (ProviderAction enabledProvider : providerActions) {
                    if (enabledProvider.isSelected()) {
                        pw.printf("**********************%ndiagnostics of %s%n", enabledProvider.getProvider().getDisplayName());// NOI18N
                        enabledProvider.getProvider().dumpInfo(context, pw);
                    }
                }
                pw.close();
                // copy all into output window
                try {
                    FileInputStream stream = new FileInputStream(tmpFile);
                    BufferedReader das = new BufferedReader(new InputStreamReader(stream));
                    String line;
                    do {
                        line = das.readLine();
                        if (line == null) {
                            break;
                        }
                        out.println(line);
                    } while (true);
                    err.printf("Cnd diagnostics is saved in %s%n", tmpFile);// NOI18N
                    das.close();
                } catch (IOException e) {
                    err.printf("Can not display file content %s,%ndue to %s%n", tmpFile, e.getMessage());// NOI18N
                }
                err.close();
                out.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    private JPanel createPanel() {
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
            Collection<? extends CndDiagnosticProvider> providers = Lookup.getDefault().lookupAll(CndDiagnosticProvider.class);
            panel.setPreferredSize(new Dimension(250, 32*providers.size()));
            for (CndDiagnosticProvider prov : providers) {
                gridBagConstraints.gridy++;
                ProviderAction providerAction = new ProviderAction(prov);
                providerActions.add(providerAction);
                JCheckBox cb = new JCheckBox(providerAction);
                cb.setSelected(providerAction.isSelected());
                panel.add(cb, gridBagConstraints);
            }
        }
        return panel;
    }

    private JPanel panel;
    private final List<ProviderAction> providerActions = new ArrayList<ProviderAction>();
    private final static class ProviderAction extends AbstractAction {
        private final CndDiagnosticProvider provider;
        private boolean selected;
        ProviderAction(CndDiagnosticProvider provider) {
            super(provider.getDisplayName());
            this.provider = provider;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selected = !selected;
        }

        boolean isSelected() {
            return selected;
        }

        public CndDiagnosticProvider getProvider() {
            return provider;
        }
    }
}
