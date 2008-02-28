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

package org.netbeans.modules.j2ee.common.project.classpath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *@author Tomas Zezula, David Konecny
 *
 */
public final class ClassPathModifier extends ProjectClassPathModifierImplementation {
    
    public static final int ADD = 1;
    public static final int REMOVE = 2;
    
    private final Project project;
    private final UpdateHelper helper;
    private final PropertyEvaluator eval;    
    private final ClassPathSupport cs;    
    private final AntProjectHelper antHelper;
    private ReferenceHelper refHelper;
    private Callback callback;
    private ClassPathSupport.Callback cpCallback;

    private static final Logger LOG = Logger.getLogger(ClassPathModifier.class.getName());

    /** Creates a new instance of J2SEProjectClassPathModifier */
    public ClassPathModifier(final Project project, final UpdateHelper helper, 
            final PropertyEvaluator eval, final ReferenceHelper refHelper, 
            ClassPathSupport.Callback cpCallback, Callback callback) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        assert refHelper != null;
        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.refHelper = refHelper;
        this.antHelper = helper.getAntProjectHelper();
        this.cs = new ClassPathSupport( eval, refHelper, antHelper, helper,
                                        cpCallback);
        this.callback = callback;
        this.cpCallback = cpCallback;
    }
    
    protected SourceGroup[] getExtensibleSourceGroups() {
        Sources s = project.getLookup().lookup(Sources.class);
        assert s != null;
        return s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }
    
    protected String[] getExtensibleClassPathTypes (SourceGroup sg) {
        return new String[] {
            ClassPath.COMPILE,
            ClassPath.EXECUTE
        };
    }

    protected boolean removeRoots(final URL[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = callback.getClassPathProperty(sourceGroup, type);
        return handleRoots (classPathRoots, classPathProperty, callback.getElementName(classPathProperty), REMOVE);
    }

    protected boolean addRoots (final URL[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {        
        String classPathProperty = callback.getClassPathProperty(sourceGroup, type);
        return handleRoots (classPathRoots, classPathProperty, callback.getElementName(classPathProperty), ADD);
    }
    
    boolean handleRoots (final URL[] classPathRoots, final String classPathProperty, final String projectXMLElementName, final int operation) throws IOException, UnsupportedOperationException {
        assert classPathRoots != null : "The classPathRoots cannot be null";      //NOI18N        
        assert classPathProperty != null;
        try {
            return ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                        public Boolean run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathProperty);                            
                            List<ClassPathSupport.Item> resources = cs.itemsList(raw, projectXMLElementName);
                            boolean changed = false;
                            File projectFolderFile = FileUtil.toFile(project.getProjectDirectory());
                            for (int i=0; i< classPathRoots.length; i++) {
                                String filePath = ClassPathModifier.this.performSharabilityHeuristics(classPathRoots[i], antHelper);
                                File f = antHelper.resolveFile(filePath);
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( filePath, projectFolderFile, null);
                                cpCallback.initAdditionalProperties(item);
                                if (operation == ADD && !resources.contains(item)) {
                                    resources.add (item);
                                    changed = true;
                                } else if (operation == REMOVE) {
                                    if (resources.remove(item)) {
                                        changed = true;
                                    } else {
                                        // can be broken item
                                        for (Iterator<ClassPathSupport.Item> it = resources.iterator(); it.hasNext();) {
                                            ClassPathSupport.Item resource = it.next();
                                            if (resource.isBroken() && resource.getType() == ClassPathSupport.Item.TYPE_JAR && filePath.equals(resource.getFilePath())) {
                                                it.remove();
                                                changed = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (changed) {
                                String itemRefs[] = cs.encodeToStrings( resources, projectXMLElementName);
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                                props.setProperty(classPathProperty, itemRefs);
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
    
    protected boolean removeAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = callback.getClassPathProperty(sourceGroup, type);
        return handleAntArtifacts (artifacts, artifactElements, classPathProperty, callback.getElementName(classPathProperty), REMOVE);
    }

    protected boolean addAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = callback.getClassPathProperty(sourceGroup, type);
        return handleAntArtifacts (artifacts, artifactElements, classPathProperty, callback.getElementName(classPathProperty), ADD);
    }
    
    boolean handleAntArtifacts (final AntArtifact[] artifacts, final URI[] artifactElements, final String classPathProperty, final String projectXMLElementName, final int operation) throws IOException, UnsupportedOperationException {
        assert artifacts != null : "Artifacts cannot be null";    //NOI18N
        assert artifactElements != null : "ArtifactElements cannot be null";  //NOI18N
        assert artifacts.length == artifactElements.length : "Each artifact has to have corresponding artifactElement"; //NOI18N
        assert classPathProperty != null;
        try {
            return ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                        public Boolean run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty (classPathProperty);
                            List<ClassPathSupport.Item> resources = cs.itemsList(raw, projectXMLElementName);
                            boolean changed = false;
                            for (int i=0; i<artifacts.length; i++) {
                                assert artifacts[i] != null;
                                assert artifactElements[i] != null;
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( artifacts[i], artifactElements[i], null);
                                cpCallback.initAdditionalProperties(item);
                                if (operation == ADD && !resources.contains(item)) {
                                    resources.add (item);
                                    changed = true;
                                }
                                else if (operation == REMOVE && resources.contains(item)) {
                                    resources.remove(item);
                                    changed = true;
                                }
                            }                            
                            if (changed) {
                                String itemRefs[] = cs.encodeToStrings( resources, projectXMLElementName);
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                                props.setProperty (classPathProperty, itemRefs);
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
    
    protected boolean removeLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = callback.getClassPathProperty(sourceGroup, type);
        return handleLibraries (libraries, classPathProperty, callback.getElementName(classPathProperty), REMOVE);
    }

    protected boolean addLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = callback.getClassPathProperty(sourceGroup, type);
        return handleLibraries (libraries, classPathProperty, callback.getElementName(classPathProperty), ADD);
    }
    
    boolean handleLibraries (final Library[] libraries, final String classPathProperty, final String projectXMLElementName, final int operation) throws IOException, UnsupportedOperationException {
        List<ClassPathSupport.Item> items = new ArrayList<ClassPathSupport.Item>(libraries.length);
        for (int i = 0; i < libraries.length; i++) {
            Library lib = checkLibrarySharability(project, antHelper, refHelper, libraries[i]);
            ClassPathSupport.Item item = ClassPathSupport.Item.create(lib, null);
            cpCallback.initAdditionalProperties(item);
            items.add(item);
        }
        return handleLibraryClassPathItems(items, classPathProperty, projectXMLElementName, operation, true);
    }
    
    public static Library checkLibrarySharability(Project project, AntProjectHelper antHelper, ReferenceHelper refHelper, Library lib) throws IOException {
        if (antHelper.isSharableProject()) {
            if (lib.getManager().getLocation() == null) {
                LOG.log(Level.FINE, "Client is adding global library ["+lib+
                        "] to sharable project.", new Exception());
                // For backward compatibility just copy the library to shared one.
                Library l = refHelper.getProjectLibraryManager().getLibrary(lib.getName());
                if (l != null) {
                    lib = l;
                } else {
                    lib = refHelper.copyLibrary(lib);
                }
            } else if (!lib.getManager().getLocation().equals(refHelper.getProjectLibraryManager().getLocation())) {
                throw new UnsupportedOperationException("Adding library '"+lib.getName()+ // NOI18N
                    "' from '"+lib.getManager().getLocation()+"' to project '"+project.getProjectDirectory()+ // NOI18N
                    "' is not supported because project libraries are defined in '"+refHelper.getProjectLibraryManager().getLocation()+"'"); // NOI18N
            }
        }
        return lib;
    }
    
    public boolean handleLibraryClassPathItems (final List<ClassPathSupport.Item> items, final String classPathProperty, final String projectXMLElementName, final int operation, final boolean saveProject) throws IOException {
        assert items != null : "Libraries cannot be null";  //NOI18N
        assert classPathProperty != null;
        // if the caller doesn't wish to save the project, it is expected to do it later,
        // in which case it must have PM.mutex() write access to avoid race conditions
        assert saveProject || ProjectManager.mutex().isWriteAccess();
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty(classPathProperty);
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, projectXMLElementName);
                        List<ClassPathSupport.Item> changed = new ArrayList<ClassPathSupport.Item>(items.size());
                        for (ClassPathSupport.Item item : items) {
                            assert item != null;
                            assert item.getType() == ClassPathSupport.Item.TYPE_LIBRARY;
                            if (operation == ADD && !resources.contains(item)) {
                                resources.add (item);                                
                                changed.add(item);
                            }
                            else if (operation == REMOVE && resources.contains(item)) {
                                resources.remove(item);
                                changed.add(item);
                            }
                        }
                        if (!changed.isEmpty()) {
                            String itemRefs[] = cs.encodeToStrings( resources, projectXMLElementName);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties                                
                            props.setProperty(classPathProperty, itemRefs);
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
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
    

    public ClassPathSupport getClassPathSupport () {
        return cs;
    }

    /**
     * Callback to customize classpath modifier behaviour.
     */
    public static interface Callback {
        
        /**
         * Returns Ant property which keeps classpath of the given source group and
         * given classpath type.
         */
        String getClassPathProperty (SourceGroup sourceGroup, String classPathType);
        
        /**
         * Returns project XML element under which extra classpath related information
         * is stored. See also {@link ClassPathSupport#Callback}
         */
        String getElementName(String classpathProperty);
    }
}
