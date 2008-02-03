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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *@author Tomas Zezula
 *
 */
public class WebProjectClassPathModifier extends ProjectClassPathModifierImplementation implements PropertyChangeListener {
    
    public static final int ADD = 1;
    public static final int REMOVE = 2;
    
    private static final String DEFAULT_WEB_MODULE_ELEMENT_NAME = ClassPathSupport.TAG_WEB_MODULE_LIBRARIES;

    private final WebProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator eval;    
    private final ClassPathSupport cs;    

    private volatile boolean projectDeleted;

    private boolean dontFireChange = false;
    
    private final PropertyChangeListener listener = WeakListeners.propertyChange(this, null);

    /** Creates a new instance of J2SEProjectClassPathModifier */
    public WebProjectClassPathModifier(final WebProject project, final UpdateHelper helper, final PropertyEvaluator eval, final ReferenceHelper refHelper) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        assert refHelper != null;
        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.cs = new ClassPathSupport( eval, refHelper, helper.getAntProjectHelper(), 
                                        WebProjectProperties.WELL_KNOWN_PATHS, 
                                        WebProjectProperties.LIBRARY_PREFIX, 
                                        WebProjectProperties.LIBRARY_SUFFIX, 
                                        WebProjectProperties.ANT_ARTIFACT_PREFIX );
        
        //#56140
        eval.addPropertyChangeListener(listener); //listen for changes of libraries list
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                registerLibraryListeners();
            }
        });
    }
    
    protected SourceGroup[] getExtensibleSourceGroups() {
        Sources s = (Sources) this.project.getLookup().lookup(Sources.class);
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
        String classPathProperty = getClassPathProperty(sourceGroup, type);
        return handleRoots (classPathRoots, classPathProperty, getElementName(classPathProperty), REMOVE);
    }

    protected boolean addRoots (final URL[] classPathRoots, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {        
        String classPathProperty = getClassPathProperty(sourceGroup, type);
        return handleRoots (classPathRoots, classPathProperty, getElementName(classPathProperty), ADD);
    }
    
    boolean handleRoots (final URL[] classPathRoots, final String classPathProperty, final String webModuleElementName, final int operation) throws IOException, UnsupportedOperationException {
        assert classPathRoots != null : "The classPathRoots cannot be null";      //NOI18N        
        assert classPathProperty != null;
        try {
            return ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                        public Boolean run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathProperty);                            
                            List<ClassPathSupport.Item> resources = cs.itemsList(raw, webModuleElementName);
                            boolean changed = false;
                            for (int i=0; i< classPathRoots.length; i++) {
                                assert classPathRoots[i] != null;
                                assert classPathRoots[i].toExternalForm().endsWith("/");    //NOI18N
                                URL toAdd = FileUtil.getArchiveFile(classPathRoots[i]);
                                if (toAdd == null) {
                                    toAdd = classPathRoots[i];
                                }
                                File f = FileUtil.normalizeFile( new File (URI.create(toAdd.toExternalForm())));
                                if (f == null ) {
                                    throw new IllegalArgumentException ("The file must exist on disk");     //NOI18N
                                }
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( f, null, f.isDirectory() ? ClassPathSupport.Item.PATH_IN_WAR_DIR : ClassPathSupport.Item.PATH_IN_WAR_LIB);
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
                                            if (resource.isBroken() && resource.getType() == ClassPathSupport.Item.TYPE_JAR && f.equals(resource.getFile())) {
                                                it.remove();
                                                changed = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (changed) {
                                String itemRefs[] = cs.encodeToStrings( resources.iterator(), webModuleElementName);
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
        String classPathProperty = getClassPathProperty(sourceGroup, type);
        return handleAntArtifacts (artifacts, artifactElements, classPathProperty, getElementName(classPathProperty), REMOVE);
    }

    protected boolean addAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = getClassPathProperty(sourceGroup, type);
        return handleAntArtifacts (artifacts, artifactElements, classPathProperty, getElementName(classPathProperty), ADD);
    }
    
    boolean handleAntArtifacts (final AntArtifact[] artifacts, final URI[] artifactElements, final String classPathProperty, final String webModuleElementName, final int operation) throws IOException, UnsupportedOperationException {
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
                            List<ClassPathSupport.Item> resources = cs.itemsList(raw, webModuleElementName);
                            boolean changed = false;
                            for (int i=0; i<artifacts.length; i++) {
                                assert artifacts[i] != null;
                                assert artifactElements[i] != null;
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( artifacts[i], artifactElements[i], null, ClassPathSupport.Item.PATH_IN_WAR_LIB);
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
                                String itemRefs[] = cs.encodeToStrings( resources.iterator(), webModuleElementName);
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
        String classPathProperty = getClassPathProperty(sourceGroup, type);
        return handleLibraries (libraries, classPathProperty, getElementName(classPathProperty), REMOVE);
    }

    protected boolean addLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = getClassPathProperty(sourceGroup, type);
        return handleLibraries (libraries, classPathProperty, getElementName(classPathProperty), ADD);
    }
    
    boolean handleLibraries (final Library[] libraries, final String classPathProperty, final String webModuleElementName, final int operation) throws IOException, UnsupportedOperationException {
        List<ClassPathSupport.Item> items = new ArrayList<ClassPathSupport.Item>(libraries.length);
        for (int i = 0; i < libraries.length; i++) {
            items.add(ClassPathSupport.Item.create(libraries[i], null, ClassPathSupport.Item.PATH_IN_WAR_LIB));
        }
        return handleLibraryClassPathItems(items, classPathProperty, webModuleElementName, operation, true);
    }
    
    public boolean handleLibraryClassPathItems (final List<ClassPathSupport.Item> items, final String classPathProperty, final String webModuleElementName, final int operation, final boolean saveProject) throws IOException {
        assert items != null : "Libraries cannot be null";  //NOI18N
        assert classPathProperty != null;
        // if the caller doesn't wish to save the project, it is expected to do it later,
        // in which case it must have PM.mutex() write access to avoid race conditions
        assert saveProject || ProjectManager.mutex().isWriteAccess();
        try {
            dontFireChange = true;
            unregisterLibraryListeners();
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty(classPathProperty);
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, webModuleElementName);
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
                            String itemRefs[] = cs.encodeToStrings( resources.iterator(), webModuleElementName);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties                                
                            props.setProperty(classPathProperty, itemRefs);                                
                            if (operation == ADD) {
                                for (ClassPathSupport.Item item : changed) {
                                    String prop = cs.getLibraryReference(item);
                                    prop = prop.substring(2, prop.length()-1); // XXX make a PropertyUtils method for this!
                                    ClassPathSupport.relativizeLibraryClassPath(props, helper.getAntProjectHelper(), prop);
                                }
                            }
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            //update lib references in private properties
                            EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            ArrayList l = new ArrayList ();
                            l.addAll(resources);
                            l.addAll(cs.itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL),  WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
                            WebProjectProperties.storeLibrariesLocations(l.iterator(), privateProps);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                            if (saveProject) {
                                ProjectManager.getDefault().saveProject(project);
                            }
                            registerLibraryListeners(props);
                            dontFireChange = false;
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
    
    private String getClassPathProperty (final SourceGroup sg, final String type) throws UnsupportedOperationException {
        assert sg != null : "SourceGroup cannot be null";  //NOI18N
        assert type != null : "Type cannot be null";  //NOI18N
        final String classPathProperty = project.getClassPathProvider().getPropertyName (sg, type);
        if (classPathProperty == null) {
            throw new UnsupportedOperationException ("Modification of [" + sg.getRootFolder().getPath() +", " + type + "] is not supported"); //NOI8N
        }
        return classPathProperty;
    }
    
    private void unregisterLibraryListeners() {
        Library libs [] = LibraryManager.getDefault().getLibraries();
        for (int i = 0; i < libs.length; i++) {
            libs [i].removePropertyChangeListener(listener);
        }
    }
    
    private void registerLibraryListeners() {
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH); //Reread the properties, PathParser changes them
        registerLibraryListeners(props);
    }
    
    private void registerLibraryListeners(EditableProperties props) {
        unregisterLibraryListeners();
        HashSet set = new HashSet();
        set.addAll(cs.itemsList(props.getProperty(WebProjectProperties.JAVAC_CLASSPATH),  WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
        set.addAll(cs.itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL),  WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
        Iterator i = set.iterator();
        while (i.hasNext()) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)i.next();
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY && !item.isBroken()) {
                item.getLibrary().addPropertyChangeListener(listener);
            }
        }
    }
    
    public void propertyChange (PropertyChangeEvent e) {
        if (projectDeleted) {
            return;
        }
        
        if (dontFireChange) {
            return;
        }
        
        if (e.getSource().equals(eval) && (e.getPropertyName().equals(WebProjectProperties.JAVAC_CLASSPATH)
            || e.getPropertyName().equals(WebProjectProperties.WAR_CONTENT_ADDITIONAL))) {
                EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH); //Reread the properties, PathParser changes them
                String javacCp = props.getProperty(WebProjectProperties.JAVAC_CLASSPATH);
                if (javacCp != null) {
                    registerLibraryListeners(props);
		    if (ProjectManager.getDefault().isValid(project)) {
			storeLibLocations();
                    }
                }
        } else if (e.getPropertyName().equals(Library.PROP_CONTENT)) {
            storeLibLocations();
        }
    }
    
    private void storeLibLocations() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                        //update lib references in private properties
                        EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                        List wmLibs = cs.itemsList(props.getProperty(WebProjectProperties.JAVAC_CLASSPATH),  WebProjectProperties.TAG_WEB_MODULE_LIBRARIES);
                        List additionalLibs = cs.itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL),  WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                        cs.encodeToStrings(wmLibs.iterator(), WebProjectProperties.TAG_WEB_MODULE_LIBRARIES);
                        cs.encodeToStrings(additionalLibs.iterator(), WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                        HashSet set = new HashSet();
                        set.addAll(wmLibs);
                        set.addAll(additionalLibs);
                        WebProjectProperties.storeLibrariesLocations(set.iterator(), privateProps);
                        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                        
                        try {
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                });
            }
        });
    }

    public void notifyDeleting() {
        projectDeleted = true;
        eval.removePropertyChangeListener(listener);
    }

    public ClassPathSupport getClassPathSupport () {
        return cs;
    }

    // #123223
    /**
     * We have to decide whether update project.xml file as well or not; project.xml file is updated if
     * and only if the classpath property is "javac.classpath".
     */
    private String getElementName(String classpathProperty) {
        if (WebProjectProperties.JAVAC_CLASSPATH.equals(classpathProperty)) {
            return DEFAULT_WEB_MODULE_ELEMENT_NAME;
        }
        return null;
    }
}
