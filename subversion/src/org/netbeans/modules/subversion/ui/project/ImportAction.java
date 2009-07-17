/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.subversion.ui.project;

import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.commit.CommitAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.netbeans.api.project.*;
import java.io.*;
import java.util.*;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.wizards.ImportWizard;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Petr Kuzel
 */
public final class ImportAction extends NodeAction {
    
    public ImportAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(ImportAction.class, "BK0006"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    protected boolean enable(Node[] nodes) {
        if (nodes.length == 1) {
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            File dir = lookupImportDirectory(nodes[0]);
            if (dir != null && dir.isDirectory()) {
                FileInformation status = cache.getCachedStatus(dir);
                // mutually exclusive enablement logic with commit
                if (!SvnUtils.isManaged(dir) && (status == null || (status.getStatus() & FileInformation.STATUS_MANAGED) == 0)) {
                    // do not allow to import partial/nonatomic project, all must lie under imported common root
                    FileObject fo = FileUtil.toFileObject(dir);
                    Project p = FileOwnerQuery.getOwner(fo);
                    if (p == null) {
                        return true;
                    }
                    FileObject projectDir = p.getProjectDirectory();
                    return FileUtil.isParentOf(projectDir, fo) == false;
                }
            }
        }
        return false;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected void performAction(Node[] nodes) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }

        Utils.logVCSActionEvent("SVN");

        if (nodes.length == 1) {
            final File importDirectory = lookupImportDirectory(nodes[0]);
            if (importDirectory != null) {

                List<File> list = new ArrayList<File>(1);
                list.add(importDirectory);
                Context context = new Context(Context.getEmptyList(), list, Context.getEmptyList());
                ImportWizard wizard = new ImportWizard(context);
                if (!wizard.show()) return;
                
                Map commitFiles = wizard.getCommitFiles();
                String message = wizard.getMessage();
                        
                performAction(context, commitFiles, message);
            }
        }
    }

    private void performAction(final Context context,
                               final Map/*<SvnFileNode, CommitOptions>*/ commitFiles,
                               final String message)
    {                        
        SVNUrl repository;
        try {
            repository = SvnUtils.getRepositoryRootUrl(context.getRootFiles()[0]);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }                 
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {                    
                CommitAction.performCommit(message, commitFiles, context, this, true);
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(ImportAction.class, "LBL_Import_Progress"));
    }

    public boolean cancel() {
        return true;
    }
    
    private File lookupImportDirectory(Node node) {
        File importDirectory = null;
        Project project = (Project) node.getLookup().lookup(Project.class);
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            if (groups.length == 1) {
                FileObject root = groups[0].getRootFolder();
                importDirectory = FileUtil.toFile(root);
            } else {
                importDirectory = FileUtil.toFile(project.getProjectDirectory());
            }
        } else {
            FileObject fo = null;
            Collection<? extends FileObject> fileObjects = node.getLookup().lookup(new Lookup.Template<FileObject>(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                fo = fileObjects.iterator().next();
            } else {
                DataObject dataObject = node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    fo = dataObject.getPrimaryFile();
                }
            }

            if (fo != null) {
                File f = FileUtil.toFile(fo);
                if (f != null && f.isDirectory()) {
                    importDirectory = f;
                }
            }
        }
        return importDirectory;
    }
    
}
