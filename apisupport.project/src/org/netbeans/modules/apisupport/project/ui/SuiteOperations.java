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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.BrandingSupport;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.BasicBrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * @author Martin Krauskopf
 */
public final class SuiteOperations implements DeleteOperationImplementation,
        MoveOperationImplementation {
    
    private static final Map<String,Set<NbModuleProject>> TEMPORARY_CACHE = new HashMap<String,Set<NbModuleProject>>();
    
    private final SuiteProject suite;
    private final FileObject projectDir;
    
    public SuiteOperations(final SuiteProject suite) {
        this.suite = suite;
        this.projectDir = suite.getProjectDirectory();
    }
    
    public void notifyDeleting() throws IOException {
        FileObject buildXML = projectDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        ActionUtils.runTarget(buildXML, new String[] { ActionProvider.COMMAND_CLEAN }, null).waitFinished();
        
        // remove all suite components from the suite - i.e. make them standalone
        SubprojectProvider spp = suite.getLookup().lookup(SubprojectProvider.class);
        for (Project suiteComponent : spp.getSubprojects()) {
            SuiteUtils.removeModuleFromSuite((NbModuleProject) suiteComponent);
        }
    }
    
    public void notifyDeleted() throws IOException {
        suite.getHelper().notifyDeleted();
    }
    
    public void notifyMoving() throws IOException {
        Set<NbModuleProject> subprojects = SuiteUtils.getSubProjects(suite);
        if (!subprojects.isEmpty()) {
            // XXX using suite's name is probably weak. Consider another solution. E.g.
            // store some "private" property and than read it.
            TEMPORARY_CACHE.put(ProjectUtils.getInformation(suite).getName(), subprojects);
        }
        // this will temporarily remove all suite components - this is needed
        // to prevent infrastructure confusion about lost suite. They will be
        // readded in the notifyMoved.
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
        if (original == null) { // called on the original project
            suite.getHelper().notifyDeleted();
        } else { // called on the new project
            String name = ProjectUtils.getInformation(suite).getName();
            Set<NbModuleProject> subprojects = TEMPORARY_CACHE.remove(name);
            if (subprojects != null) {
                Set<Project> toOpen = new HashSet<Project>();
                for (Project _originalComp : subprojects) {
                    NbModuleProject originalComp = (NbModuleProject) _originalComp;
                    
                    boolean directoryChanged = !original.getProjectDirectory().
                            equals(suite.getProjectDirectory());
                    if (directoryChanged && FileUtil.isParentOf( // wasRelative
                            original.getProjectDirectory(), originalComp.getProjectDirectory())) {
                        boolean isOpened = SuiteOperations.isOpened(originalComp);
                        Project nueComp = SuiteOperations.moveModule(originalComp, suite.getProjectDirectory());
                        SuiteUtils.addModule(suite, (NbModuleProject) nueComp);
                        if (isOpened) {
                            toOpen.add(nueComp);
                        }
                    } else {
                        SuiteUtils.addModule(suite, originalComp);
                    }
                }
                OpenProjects.getDefault().open(toOpen.toArray(new Project[toOpen.size()]), false);
            }
            boolean isRename = original.getProjectDirectory().getParent().equals(
                    suite.getProjectDirectory().getParent());
            if (isRename) {
                setDisplayName(nueName);
            }
            FileObject origSuiteFO = FileUtil.toFileObject(originalPath);
            if (origSuiteFO != null && origSuiteFO.getChildren().length == 0) {
                origSuiteFO.delete();
            }
        }
    }
    
    public List<FileObject> getMetadataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        addFile(GeneratedFilesHelper.BUILD_XML_PATH, files);
        addFile("nbproject", files); // NOI18N
        addFile("master.jnlp", files);    // NOI18N
        addFile("branding.jnlp", files);    // NOI18N
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        addFile(suite.getEvaluator().getProperty(BrandingSupport.BRANDING_DIR_PROPERTY), files);
        return files;
    }
    
    private void addFile(String fileName, List<FileObject> result) {
        FileObject file = projectDir.getFileObject(fileName);
        if (file != null) {
            result.add(file);
        }
    }
    
    private void setDisplayName(final String nueName) throws IOException {
        final SuiteProperties sp = new SuiteProperties(suite, suite.getHelper(),
                suite.getEvaluator(), SuiteUtils.getSubProjects(suite));
        final BasicBrandingModel branding = sp.getBrandingModel();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Object>() {
                public Object run() throws IOException {
                    if (branding.isBrandingEnabled()) { // application
                        branding.setTitle(nueName);
                        sp.storeProperties();
                    } else { // ordinary suite of modules
                        EditableProperties props = suite.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.setProperty(BasicBrandingModel.TITLE_PROPERTY, nueName);
                        suite.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                    }
                    ProjectManager.getDefault().saveProject(suite);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /** Package private for unit tests <strong>only</strong>. */
    static Project moveModule(final NbModuleProject original, final FileObject targetParent) throws IOException, IllegalArgumentException {
        ProjectOperations.notifyMoving(original);
        SuiteOperations.close(original);
        FileObject origDir = original.getProjectDirectory();
        FileObject copy = doCopy(original, origDir, targetParent);
        ProjectManager.getDefault().clearNonProjectCache();
        Project nueComp = ProjectManager.getDefault().findProject(copy);
        assert nueComp != null;
        File originalPath = FileUtil.toFile(origDir);
        doDelete(original, origDir);
        ProjectOperations.notifyMoved(original, nueComp, originalPath, originalPath.getName());
        return nueComp;
    }
    
    private static boolean isOpened(final Project original) {
        boolean opened = false;
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < openProjects.length; i++) {
            if (openProjects[i] == original) {
                opened = true;
                break;
            }
        }
        return opened;
    }
    
    static boolean canRun(final SuiteProject project) {
        boolean result = true;
        if (project.getTestUserDirLockFile().isFile()) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ModuleOperations.class, "ERR_ModuleIsBeingRun")));
            result = false;
        }
        return result;
    }

// XXX following is copy-pasted from the Project APIs
//<editor-fold defaultstate="collapsed" desc="copy-pasted from Project API">
    private static FileObject doCopy(final Project original,
            final FileObject from, final FileObject toParent) throws IOException {
        if (!VisibilityQuery.getDefault().isVisible(from)) {
            //Do not copy invisible files/folders.
            return null;
        }
        
        if (!original.getProjectDirectory().equals(FileOwnerQuery.getOwner(from).getProjectDirectory())) {
            return null;
        }
        
        FileObject copy;
        if (from.isFolder()) {
            copy = toParent.createFolder(from.getNameExt());
            FileObject[] kids = from.getChildren();
            for (int i = 0; i < kids.length; i++) {
                doCopy(original, kids[i], copy);
            }
        } else {
            assert from.isData();
            copy = FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
        }
        return copy;
    }
    
    private static boolean doDelete(final Project original,
            final FileObject toDelete) throws IOException {
        if (!original.getProjectDirectory().equals(FileOwnerQuery.getOwner(toDelete).getProjectDirectory())) {
            return false;
        }
        
        if (toDelete.isFolder()) {
            FileObject[] kids = toDelete.getChildren();
            boolean delete = true;
            
            for (int i = 0; i < kids.length; i++) {
                delete &= doDelete(original, kids[i]);
            }
            
            if (delete) {
                toDelete.delete();
            }
            
            return delete;
        } else {
            assert toDelete.isData();
            toDelete.delete();
            return true;
        }
    }
    
    private static void close(final Project prj) {
        Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
            public Object run() {
                LifecycleManager.getDefault().saveAll();
                
                Action closeAction = CommonProjectActions.closeProjectAction();
                closeAction = closeAction instanceof ContextAwareAction ? ((ContextAwareAction) closeAction).createContextAwareInstance(Lookups.fixed(new Object[] {prj})) : null;
                
                if (closeAction != null && closeAction.isEnabled()) {
                    closeAction.actionPerformed(new ActionEvent(prj, -1, "")); // NOI18N
                } else {
                    //fallback:
                    OpenProjects.getDefault().close(new Project[] {prj});
                }
                
                return null;
            }
        });
    }
//</editor-fold>
    
}
