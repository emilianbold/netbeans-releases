/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.ui.diff;

import java.awt.EventQueue;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.ExportDiffSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.diff.ExportUncommittedChangesAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ExportUncommittedChangesAction_Name")
public class ExportUncommittedChangesAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(ExportUncommittedChangesAction.class.getName());
    
    @Override
    protected void performAction (final File repository, final File[] roots, VCSContext context) {
        if (roots.length == 0 || !Git.getInstance().getFileStatusCache().containsFiles(new HashSet<File>(Arrays.asList(roots)), FileInformation.STATUS_LOCAL_CHANGES, true)) {
            NotifyDescriptor msg = new NotifyDescriptor.Message(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.emptyContext"), NotifyDescriptor.INFORMATION_MESSAGE); //NOI18N
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                new ExportDiffSupport(roots, GitModuleConfig.getDefault().getPreferences()) {
                    @Override
                    public void writeDiffFile (final File toFile) {
                        saveFolderToPrefs(toFile);
                        GitProgressSupport ps = new GitProgressSupport() {
                            @Override
                            protected void perform () {
                                boolean success = false;
                                OutputStream out = null;
                                OutputLogger logger = getLogger();
                                int exportedFiles = 0;
                                Git git = Git.getInstance();
                                try {
                                    List<Setup> setups;
                                    File [] files = git.getFileStatusCache().listFiles(roots, FileInformation.STATUS_LOCAL_CHANGES);
                                    setups = new ArrayList<Setup>(files.length);
                                    for (int i = 0; i < files.length; i++) {
                                        File file = files[i];
                                        LOG.log(Level.FINE, "preparing setup {0}", file); //NOI18N
                                        Setup setup = new Setup(file, Mode.HEAD_VS_WORKING_TREE, true);
                                        LOG.log(Level.FINE, "setup prepared {0}", setup.getBaseFile()); //NOI18N
                                        setups.add(setup);
                                    }

                                    logger.output(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.title")); //NOI18N
                                    logger.output(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.separator")); //NOI18N
                                    logger.output(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.exportingTo", toFile)); //NOI18N

                                    String sep = System.getProperty("line.separator"); //NOI18N
                                    ensureParentExists(toFile);
                                    out = new BufferedOutputStream(new FileOutputStream(toFile));
                                    // Used by PatchAction as MAGIC to detect right encoding
                                    out.write(("# This patch file was generated by NetBeans IDE" + sep).getBytes("utf8"));  // NOI18N
                                    out.write(("# Following Index: paths are relative to: " + repository.getAbsolutePath() + sep).getBytes("utf8"));  // NOI18N
                                    out.write(("# This patch can be applied using context Tools: Patch action on respective folder." + sep).getBytes("utf8"));  // NOI18N
                                    out.write(("# It uses platform neutral UTF-8 encoding and \\n newlines." + sep).getBytes("utf8"));  // NOI18N
                                    out.write(("# Above lines and this line are ignored by the patching process." + sep).getBytes("utf8"));  // NOI18N


                                    Collections.sort(setups, new Comparator<Setup>() {
                                        @Override
                                        public int compare(Setup o1, Setup o2) {
                                            return o1.getBaseFile().compareTo(o2.getBaseFile());
                                        }
                                    });
                                    Iterator<Setup> it = setups.iterator();
                                    int i = 0;
                                    while (it.hasNext()) {
                                        Setup setup = it.next();
                                        File file = setup.getBaseFile();                
                                        LOG.log(Level.FINE, "exporting setup {0}", file.getName()); //NOI18N
                                        logger.output(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.exporting", file.getName())); //NOI18N
                                        if (file.isDirectory()) continue;
                                        setProgress(file.getName());

                                        String index = "Index: ";   // NOI18N
                                        String rootPath = repository.getAbsolutePath();
                                        String filePath = file.getAbsolutePath();
                                        String relativePath = filePath;
                                        if (filePath.startsWith(rootPath)) {
                                            relativePath = filePath.substring(rootPath.length() + 1).replace(File.separatorChar, '/');
                                            index += relativePath + sep;
                                            out.write(index.getBytes("utf8")); // NOI18N
                                        }
                                        exportDiff(setup, relativePath, out);
                                        i++;
                                    }

                                    exportedFiles = i;
                                    success = true;
                                    logger.outputInRed(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.done")); //NOI18N
                                } catch (IOException ex) {
                                    logger.outputInRed(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.failed")); //NOI18N
                                    LOG.log(Level.INFO, NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.failed"), ex); //NOI18N
                                } finally {
                                    if (out != null) {
                                        try {
                                            out.flush();
                                            out.close();
                                        } catch (IOException alreadyClsoed) {
                                        }
                                    }
                                    if (success) {
                                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.exported", new Integer(exportedFiles))); //NOI18N
                                        if (exportedFiles == 0) {
                                            toFile.delete();
                                        } else {
                                            Utils.openFile(toFile);
                                        }
                                    } else {
                                        toFile.delete();
                                    }
                                    logger.output(""); // NOI18N
                                }
                            }
                        };
                        ps.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ExportUncommittedChangesAction.class, "LBL_ExportUncommittedChangesAction_Progress")).waitFinished();
                    }
                }.export();
            }
        });
    }
    
    static void saveFolderToPrefs (final File file) {
        if (file.getParent() != null) {
            GitModuleConfig.getDefault().getPreferences().put("ExportDiff.saveFolder", file.getParent()); //NOI18N
        }
    }
    
    /** Writes contextual diff into given stream.*/
    private static void exportDiff (Setup setup, String relativePath, OutputStream out) throws IOException {
        setup.initSources();
        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);

        Reader r1 = null;
        Reader r2 = null;
        Difference[] differences;

        try {
            r1 = setup.getFirstSource().createReader();
            if (r1 == null) {
                r1 = new StringReader(""); //NOI18N
            }
            r2 = setup.getSecondSource().createReader();
            if (r2 == null) {
                r2 = new StringReader(""); //NOI18N
            }
            differences = diff.computeDiff(r1, r2);
        } finally {
            if (r1 != null) {
                try {
                    r1.close();
                } catch (Exception e) {
                }
            }
            if (r2 != null) {
                try {
                    r2.close();
                } catch (Exception e) {
                }
            }
        }

        try {
            InputStream is;
            r1 = setup.getFirstSource().createReader();
            if (r1 == null) {
                r1 = new StringReader(""); //NOI18N
            }
            r2 = setup.getSecondSource().createReader();
            if (r2 == null) {
                r2 = new StringReader(""); //NOI18N
            }
            TextDiffVisualizer.TextDiffInfo info = new TextDiffVisualizer.TextDiffInfo(
                    relativePath + " " + setup.getFirstSource().getTitle(), //NOI18N
                    relativePath + " " + setup.getSecondSource().getTitle(), //NOI18N
                    null,
                    null,
                    r1,
                    r2,
                    differences);
            info.setContextMode(true, 3);
            String diffText = TextDiffVisualizer.differenceToUnifiedDiffText(info);
            is = new ByteArrayInputStream(diffText.getBytes("utf8")); //NOI18N
            while (true) {
                int i = is.read();
                if (i == -1) {
                    break;
                }
                out.write(i);
            }
        } finally {
            if (r1 != null) {
                try {
                    r1.close();
                } catch (Exception e) {
                }
            }
            if (r2 != null) {
                try {
                    r2.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static void ensureParentExists(File destination) {
        File parent = destination.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
    }
}
