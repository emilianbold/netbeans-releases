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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.project.classpath;

import java.io.File;
import java.net.URL;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.web.project.api.*;
import org.netbeans.modules.web.project.classpath.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathModifier;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WarIncludesUiSupport;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

public class WebProjectLibrariesModifierImpl implements WebProjectLibrariesModifier {
    
    private final WebProject project;
    private final UpdateHelper helper;
    private final ClassPathSupport cs;    
    private final ReferenceHelper refHelper;

    public static final int ADD = 1;
    public static final int REMOVE = 2;

    /** Creates a new instance of WebProjectLibrariesModifierImpl */
    public WebProjectLibrariesModifierImpl(final WebProject project, final UpdateHelper helper, final PropertyEvaluator eval, final ReferenceHelper refHelper) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        assert refHelper != null;
        this.project = project;
        this.helper = helper;
        this.cs = new ClassPathSupport( eval, refHelper, helper.getAntProjectHelper(), helper,
                                        new ClassPathSupportCallbackImpl(helper.getAntProjectHelper()));
        this.refHelper = refHelper;
    }
    
    public boolean addPackageLibraries(final Library[] libraries, final String path) throws IOException {
        return handlePackageLibraries(libraries, path, ADD);
    }

    public boolean removePackageLibraries(final Library[] libraries, final String path) throws IOException {
        return handlePackageLibraries(libraries, path, REMOVE);
    }
    
    private boolean handlePackageLibraries(final Library[] libraries, final String path, final int operation) throws IOException {
        List<ClassPathSupport.Item> items = new ArrayList<ClassPathSupport.Item>(libraries.length);
        for (int i = 0; i < libraries.length; i++) {
            Library lib = ClassPathModifier.checkLibrarySharability(project, project.getAntProjectHelper(), refHelper, libraries[i]);
            ClassPathSupport.Item item = ClassPathSupport.Item.create(lib, null);
            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, path);
            items.add(item);
        }
        return handlePackageLibraryClassPathItems(items, operation, true);
    }

    public boolean handlePackageLibraryClassPathItems(final List<ClassPathSupport.Item> items, final int operation, final boolean saveProject) throws IOException {
        assert items != null : "Libraries cannot be null";  //NOI18N
        // if the caller doesn't wish to save the project, it is expected to do it later,
        // in which case it must have PM.mutex() write access to avoid race conditions
        assert saveProject || ProjectManager.mutex().isWriteAccess();
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException {
                        EditableProperties projectProperties = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        List<ClassPathSupport.Item> resources = cs.itemsList((String)projectProperties.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL), ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                        List<ClassPathSupport.Item> changed = new ArrayList<ClassPathSupport.Item>(items.size());
                        for (ClassPathSupport.Item item : items) {
                            assert item != null;
                            assert item.getType() == ClassPathSupport.Item.TYPE_LIBRARY;
                            if (operation == ADD && !resources.contains(item)) {
                                resources.add(item);
                                changed.add(item);
                            } else if (operation == REMOVE && resources.contains(item)) {
                                resources.remove(item);
                                changed.add(item);
                            }
                        }
                        if (!changed.isEmpty()) {
                            String itemRefs[] = cs.encodeToStrings( resources, ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                            projectProperties = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties                                
                            projectProperties.setProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL, itemRefs);                                

                            ArrayList l = new ArrayList ();
                            l.addAll(cs.itemsList(projectProperties.getProperty(ProjectProperties.JAVAC_CLASSPATH),  WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
                            l.addAll(resources);
                            ProjectProperties.storeLibrariesLocations(l.iterator(), projectProperties, project.getProjectDirectory());
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
                            if (saveProject) {
                                ProjectManager.getDefault().saveProject(project);
                            }
                            return true;
                        }
                        return false;
                    }
                }
            );
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }

    public boolean addCompileLibraries(final Library[] libraries) throws IOException {
        return handleCompileLibraries(libraries, ADD);
    }

    public boolean removeCompileLibraries(final Library[] libraries) throws IOException {
        return handleCompileLibraries(libraries, REMOVE);
    }

    private boolean handleCompileLibraries(final Library[] libraries, final int operation) throws IOException {
        assert libraries != null : "Libraries cannot be null";  //NOI18N
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty(ProjectProperties.JAVAC_CLASSPATH);
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES);
                        List<ClassPathSupport.Item> changed = new ArrayList<ClassPathSupport.Item>(libraries.length);
                        for (int i=0; i< libraries.length; i++) {
                            assert libraries[i] != null;
                            Library lib = ClassPathModifier.checkLibrarySharability(project, project.getAntProjectHelper(), refHelper, libraries[i]);
                            ClassPathSupport.Item item = ClassPathSupport.Item.create( lib, null);
                            if (operation == ADD && !resources.contains(item)) {
                                resources.add(item);
                                changed.add(item);
                            } else if (operation == REMOVE && resources.contains(item)) {
                                resources.remove(item);                                
                                changed.add(item);
                            }
                        }
                        if (!changed.isEmpty()) {
                            String itemRefs[] = cs.encodeToStrings( resources, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties                                
                            props.setProperty(ProjectProperties.JAVAC_CLASSPATH, itemRefs);                                
                            ArrayList l = new ArrayList ();
                            l.addAll(resources);
                            l.addAll(cs.itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL),  WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
                            ProjectProperties.storeLibrariesLocations(l.iterator(), props, project.getProjectDirectory());
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            ProjectManager.getDefault().saveProject(project);
                            return true;
                        }
                        return false;
                    }
                }
            );
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }

    public boolean addPackageAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final String path) throws IOException {
        return handlePackageAntArtifacts(artifacts, artifactElements, path, ADD);
    }

    public boolean removePackageAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final String path) throws IOException {
        return handlePackageAntArtifacts(artifacts, artifactElements, path, REMOVE);
    }

    private boolean handlePackageAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final String path, final int operation) throws IOException {
        assert artifacts != null : "Artifacts cannot be null";    //NOI18N
        assert artifactElements != null : "ArtifactElements cannot be null";  //NOI18N
        assert artifacts.length == artifactElements.length : "Each artifact has to have corresponding artifactElement"; //NOI18N
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws Exception {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty ((String)props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL));
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                        boolean changed = false;
                        for (int i=0; i<artifacts.length; i++) {
                            assert artifacts[i] != null;
                            assert artifactElements[i] != null;
                            ClassPathSupport.Item item = ClassPathSupport.Item.create( artifacts[i], artifactElements[i], null);
                            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, path);
                            if (operation == ADD && !resources.contains(item)) {
                                resources.add(item);
                                changed = true;
                            } else if (operation == REMOVE && resources.contains(item)) {
                                resources.remove(item);
                                changed = true;
                            }
                        }                            
                        if (changed) {
                            String itemRefs[] = cs.encodeToStrings( resources, ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                            props.setProperty (WebProjectProperties.WAR_CONTENT_ADDITIONAL, itemRefs);
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            ProjectManager.getDefault().saveProject(project);
                            return true;
                        }
                        return false;
                    }
                }
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            else {
                IOException t = new IOException();
                t.initCause(e);
                throw t;
            }
        }
    }

    public boolean addCompileAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements) throws IOException {
        return handleCompileAntArtifacts(artifacts, artifactElements, ADD);
    }

    public boolean removeCompileAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements) throws IOException {
        return handleCompileAntArtifacts(artifacts, artifactElements, REMOVE);
    }

    private boolean handleCompileAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final int operation) throws IOException {
        assert artifacts != null : "Artifacts cannot be null";    //NOI18N
        assert artifactElements != null : "ArtifactElements cannot be null";  //NOI18N
        assert artifacts.length == artifactElements.length : "Each artifact has to have corresponding artifactElement"; //NOI18N
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws Exception {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty (ProjectProperties.JAVAC_CLASSPATH);
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES);
                        boolean changed = false;
                        for (int i=0; i<artifacts.length; i++) {
                            assert artifacts[i] != null;
                            assert artifactElements[i] != null;
                            ClassPathSupport.Item item = ClassPathSupport.Item.create( artifacts[i], artifactElements[i], null);
                            if (operation == ADD && !resources.contains(item)) {
                                resources.add(item);
                                changed = true;
                            } else if (operation == REMOVE && resources.contains(item)) {
                                resources.remove(item);
                                changed = true;
                            }
                        }                            
                        if (changed) {
                            String itemRefs[] = cs.encodeToStrings( resources, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                            props.setProperty (ProjectProperties.JAVAC_CLASSPATH, itemRefs);
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            ProjectManager.getDefault().saveProject(project);
                            return true;
                        }
                        return false;
                    }
                }
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            else {
                IOException t = new IOException();
                t.initCause(e);
                throw t;
            }
        }
    }

    public boolean addPackageRoots(final URL[] roots,final String path) throws IOException {
        return handlePackageRoots(roots, path, ADD);
    }

    public boolean removePackageRoots(final URL[] roots,final String path) throws IOException {
        return handlePackageRoots(roots, path, REMOVE);
    }

    private boolean handlePackageRoots(final URL[] roots,final String path, final int operation) throws IOException {
        assert roots != null : "The classPathRoots cannot be null";      //NOI18N        
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws Exception {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        
                        File projectFolderFile = FileUtil.toFile(project.getProjectDirectory());
                        if (operation == ADD) {
                            //Temporary solution till missing libraries described in issue #100114 are fixed
                            WarIncludesUiSupport.ClasspathTableModel addModel = WarIncludesUiSupport.createTableModel(cs.itemsList((String) props.get(WebProjectProperties.WAR_CONTENT_ADDITIONAL), ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
                            String[] filePaths = new String[roots.length];
                            for (int i = 0; i < roots.length; i++) {
                                URL toAdd = FileUtil.getArchiveFile(roots[i]);
                                if (toAdd == null) {
                                    toAdd = roots[i];
                                }
                                String filePath = LibrariesSupport.convertURLToFilePath(toAdd);
                                final File f = PropertyUtils.resolveFile(projectFolderFile, filePath);
                                if (f == null ) {
                                    throw new IllegalArgumentException ("The file must exist on disk");     //NOI18N
                                }
                                filePaths[i] = filePath;
                            }
                            WarIncludesUiSupport.addJarFiles(filePaths, projectFolderFile, addModel);
                            int count = addModel.getRowCount();
                            for (int i = 0; i < filePaths.length; i++) {
                                ClassPathSupport.Item item = (ClassPathSupport.Item) addModel.getValueAt(count - i - 1, 0);
                                item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, path);
                                addModel.setValueAt(path, count - i - 1, 1);
                            }

                            String[] war_includes = cs.encodeToStrings(WarIncludesUiSupport.getList(addModel), ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                            props.setProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL, war_includes);

                            ArrayList libs = new ArrayList ();
                            libs.addAll(cs.itemsList(props.getProperty(ProjectProperties.JAVAC_CLASSPATH),  WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
                            libs.addAll(WarIncludesUiSupport.getList(addModel));

                            ProjectProperties.storeLibrariesLocations (libs.iterator(), props, project.getProjectDirectory());
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);

                            ProjectManager.getDefault().saveProject(project);
                            return true;
                        } else if (operation == REMOVE) {
                            String raw = props.getProperty((String)props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL));                            
                            List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                            boolean changed = false;
                            for (int i=0; i< roots.length; i++) {
                                assert roots[i] != null;
                                assert roots[i].toExternalForm().endsWith("/");    //NOI18N
                                URL toAdd = FileUtil.getArchiveFile(roots[i]);
                                if (toAdd == null) {
                                    toAdd = roots[i];
                                }
                                String filePath = LibrariesSupport.convertURLToFilePath(toAdd);
                                final File f = PropertyUtils.resolveFile(projectFolderFile, filePath);
                                if (f == null ) {
                                    throw new IllegalArgumentException ("The file must exist on disk");     //NOI18N
                                }
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( filePath, projectFolderFile, null);
                                item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, path);
                                if (resources.contains(item)) {
                                    resources.remove(item);
                                    changed = true;
                                }                            
                            }                                                                                                                
                            if (changed) {
                                String itemRefs[] = cs.encodeToStrings( resources, ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                                props.setProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL, itemRefs);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                ProjectManager.getDefault().saveProject(project);
                                return true;
                            }
                            return false;
                        }
                        return false;
                    }
                }
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            else {
                IOException t = new IOException();
                t.initCause(e);
                throw t;
            }
        }
    }

    public boolean addCompileRoots(final URL[] roots) throws IOException {
        return addCompileRoots(roots, ADD);
    }

    public boolean removeCompileRoots(final URL[] roots) throws IOException {
        return addCompileRoots(roots, REMOVE);
    }

    private boolean addCompileRoots(final URL[] roots, final int operation) throws IOException {
        assert roots != null : "The classPathRoots cannot be null";      //NOI18N        
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws Exception {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty(ProjectProperties.JAVAC_CLASSPATH);                            
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES);
                        boolean changed = false;
                        File projectFolderFile = FileUtil.toFile(project.getProjectDirectory());
                        for (int i=0; i< roots.length; i++) {
                            assert roots[i] != null;
                            assert roots[i].toExternalForm().endsWith("/");    //NOI18N
                            URL toAdd = FileUtil.getArchiveFile(roots[i]);
                            if (toAdd == null) {
                                toAdd = roots[i];
                            }
                            String filePath = LibrariesSupport.convertURLToFilePath(toAdd);
                            final File f = PropertyUtils.resolveFile(projectFolderFile, filePath);
                            if (f == null ) {
                                throw new IllegalArgumentException ("The file must exist on disk");     //NOI18N
                            }
                            ClassPathSupport.Item item = ClassPathSupport.Item.create( filePath, projectFolderFile, null);
                            if (operation == ADD && !resources.contains(item)) {
                                resources.add(item);
                                changed = true;
                            } else if (operation == REMOVE && resources.contains(item)) {
                                resources.remove(item);
                                changed = true;
                            }
                        }                                                                                                                
                        if (changed) {
                            String itemRefs[] = cs.encodeToStrings( resources, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                            props.setProperty(ProjectProperties.JAVAC_CLASSPATH, itemRefs);
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            ProjectManager.getDefault().saveProject(project);
                            return true;
                        }
                        return false;
                    }
                }
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            else {
                IOException t = new IOException();
                t.initCause(e);
                throw t;
            }
        }
    }

    public ClassPathSupport getClassPathSupport() {
        return cs;
    }
}
