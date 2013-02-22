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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.action;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.editor.JPAEditorUtil;
import org.netbeans.modules.j2ee.persistence.jpqleditor.Utils;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action which show file chooser and save sql for entities creation component.
 *
 */
public class GenerateDBCreateScriptsAction extends NodeAction {

    private FileChooserBuilder fileChooser;
    private static final String EXTENSION = "sql";
    private Project project;
    private static final Logger LOGGER = Logger.getLogger(GenerateDBCreateScriptsAction.class.getName());

    public GenerateDBCreateScriptsAction() {
        super();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        //save dialog
        fileChooser = new FileChooserBuilder(GenerateDBCreateScriptsAction.class);
        fileChooser.setDefaultWorkingDirectory(FileUtil.toFile(project.getProjectDirectory()));
        fileChooser.setFileFilter(new MyFileFilter());
        fileChooser.setFilesOnly(true);
        File sFile = fileChooser.showSaveDialog();
        PersistenceEnvironment pe = project.getLookup().lookup(PersistenceEnvironment.class);
        if (sFile != null) {
            //execution
            run(project, sFile, pe);
        }
    }

    private void run(final Project project, final File sFile, final PersistenceEnvironment pe) {
        final List<URL> localResourcesURLList = new ArrayList<URL>();

        //
        final HashMap<String, String> props = new HashMap<String, String>();
        final List<String> initialProblems = new ArrayList<String>();
        PersistenceUnit[] pus = null;
        try {
            pus = Util.getPersistenceUnits(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (pus == null || pus.length == 0) {
            initialProblems.add( NbBundle.getMessage(GenerateDBCreateScriptsAction.class, "ERR_NoPU"));

            return;
        }
        final PersistenceUnit pu = pus[0];
        //connection open
        final DatabaseConnection dbconn = JPAEditorUtil.findDatabaseConnection(pu, pe.getProject());
        if (dbconn != null) {
            if (dbconn.getJDBCConnection() == null) {
                Mutex.EVENT.readAccess(new Mutex.Action<DatabaseConnection>() {
                    @Override
                    public DatabaseConnection run() {
                        ConnectionManager.getDefault().showConnectionDialog(dbconn);
                        return dbconn;
                    }
                });
            }
        }
        //
        final boolean containerManaged = Util.isSupportedJavaEEVersion(pe.getProject());
        final Provider provider = ProviderUtil.getProvider(pu.getProvider(), pe.getProject());
        if (containerManaged && provider != null) {
            Utils.substitutePersistenceProperties(pe, pu, dbconn, props);
        }
        final ClassLoader defClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Construct custom classpath here.
            initialProblems.addAll(Utils.collectClassPathURLs(pe, pu, dbconn, localResourcesURLList));

            ClassLoader customClassLoader = pe.getProjectClassLoader(
                    localResourcesURLList.toArray(new URL[]{}));
            Thread.currentThread().setContextClassLoader(customClassLoader);
            Thread t = new Thread() {
                @Override
                public void run() {
                    if (initialProblems.isEmpty()) {
                        new GenerateScriptExecutor().execute(project, sFile, pe, pu, initialProblems);
                    } 
                    if(!initialProblems.isEmpty()){
                        StringBuilder sb = new StringBuilder();
                        for (String txt : initialProblems) {
                            sb.append(txt).append("\n");
                        }
                        LOGGER.info(sb.toString());
                    }
                    Thread.currentThread().setContextClassLoader(defClassLoader);
                }
            };
            t.setContextClassLoader(customClassLoader);
            t.start();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(defClassLoader);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if ((activatedNodes != null) && (activatedNodes.length == 1)) {
            if (activatedNodes[0] != null) {
                DataObject data = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
                if (data != null) {
                    FileObject pXml = data.getPrimaryFile();
                    project = pXml != null ? FileOwnerQuery.getOwner(pXml) : null;
                    PersistenceEnvironment pe = project != null ? project.getLookup().lookup(PersistenceEnvironment.class) : null;
                    if (pe != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GenerateDBCreateScriptsAction.class, "CTL_GenerateDBCreateScripsAction");
    }

    private static class MyFileFilter extends FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return file.getName().toLowerCase().endsWith("." + EXTENSION); // NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(GenerateDBCreateScriptsAction.class, "SQL"); // NOI18N
        }
    }
}
