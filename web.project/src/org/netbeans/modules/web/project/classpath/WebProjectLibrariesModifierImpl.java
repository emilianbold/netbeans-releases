/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.project.UpdateHelper;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

public class WebProjectLibrariesModifierImpl implements WebProjectLibrariesModifier {
    
    //TODO: optimize the code, this fast implemantation was done to don't block the visual web framework
    
    private final Project project;
    private final UpdateHelper helper;
    private final PropertyEvaluator eval;    
    private final ClassPathSupport cs;    

    /** Creates a new instance of WebProjectLibrariesModifierImpl */
    public WebProjectLibrariesModifierImpl(final Project project, final UpdateHelper helper, final PropertyEvaluator eval, final ReferenceHelper refHelper) {
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
    }
    
    public boolean addPackageLibraries(final Library[] libraries, final String path) throws IOException {
        assert libraries != null : "Libraries cannot be null";  //NOI18N
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException {
                        EditableProperties projectProperties = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        List<ClassPathSupport.Item> resources = cs.itemsList((String)projectProperties.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL), ClassPathSupport.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                        List<ClassPathSupport.Item> changed = new ArrayList<ClassPathSupport.Item>(libraries.length);
                        for (int i=0; i< libraries.length; i++) {
                            assert libraries[i] != null;
                            ClassPathSupport.Item item = ClassPathSupport.Item.create(libraries[i], null, path);
                            if (!resources.contains(item)) {
                                resources.add (item);                                
                                changed.add(item);
                            }
                        }
                        if (!changed.isEmpty()) {
                            String itemRefs[] = cs.encodeToStrings( resources.iterator(), ClassPathSupport.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                            projectProperties = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties                                
                            projectProperties.setProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL, itemRefs);                                
                            for (ClassPathSupport.Item item : changed) {
                                String prop = cs.getLibraryReference(item);
                                prop = prop.substring(2, prop.length()-1); // XXX make a PropertyUtils method for this!
                                ClassPathSupport.relativizeLibraryClassPath(projectProperties, helper.getAntProjectHelper(), prop);
                            }
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);

                            //update lib references in private properties
                            EditableProperties privateProperties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            ArrayList l = new ArrayList ();
                            l.addAll(cs.itemsList(projectProperties.getProperty(WebProjectProperties.JAVAC_CLASSPATH),  WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
                            l.addAll(resources);
                            WebProjectProperties.storeLibrariesLocations(l.iterator(), privateProperties);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);
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

    public boolean addCompileLibraries(final Library[] libraries) throws IOException {
        assert libraries != null : "Libraries cannot be null";  //NOI18N
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty(WebProjectProperties.JAVAC_CLASSPATH);
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupport.TAG_WEB_MODULE_LIBRARIES);
                        List<ClassPathSupport.Item> changed = new ArrayList<ClassPathSupport.Item>(libraries.length);
                        for (int i=0; i< libraries.length; i++) {
                            assert libraries[i] != null;
                            ClassPathSupport.Item item = ClassPathSupport.Item.create( libraries[i], null, ClassPathSupport.Item.PATH_IN_WAR_NONE);
                            if (!resources.contains(item)) {
                                resources.add (item);                                
                                changed.add(item);
                            }
                        }
                        if (!changed.isEmpty()) {
                            String itemRefs[] = cs.encodeToStrings( resources.iterator(), ClassPathSupport.TAG_WEB_MODULE_LIBRARIES);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties                                
                            props.setProperty(WebProjectProperties.JAVAC_CLASSPATH, itemRefs);                                
                            for (ClassPathSupport.Item item : changed) {
                                String prop = cs.getLibraryReference(item);
                                prop = prop.substring(2, prop.length()-1); // XXX make a PropertyUtils method for this!
                                ClassPathSupport.relativizeLibraryClassPath(props, helper.getAntProjectHelper(), prop);
                            }
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            //update lib references in private properties
                            EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            ArrayList l = new ArrayList ();
                            l.addAll(resources);
                            l.addAll(cs.itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL),  WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
                            WebProjectProperties.storeLibrariesLocations(l.iterator(), privateProps);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
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
        assert artifacts != null : "Artifacts cannot be null";    //NOI18N
        assert artifactElements != null : "ArtifactElements cannot be null";  //NOI18N
        assert artifacts.length == artifactElements.length : "Each artifact has to have corresponding artifactElement"; //NOI18N
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws Exception {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty ((String)props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL));
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupport.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                        boolean changed = false;
                        for (int i=0; i<artifacts.length; i++) {
                            assert artifacts[i] != null;
                            assert artifactElements[i] != null;
                            ClassPathSupport.Item item = ClassPathSupport.Item.create( artifacts[i], artifactElements[i], null, path);
                            if (!resources.contains(item)) {
                                resources.add (item);
                                changed = true;
                            }
                        }                            
                        if (changed) {
                            String itemRefs[] = cs.encodeToStrings( resources.iterator(), ClassPathSupport.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
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
                Exception t = new IOException ();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
            }
        }
    }

    public boolean addCompileAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements) throws IOException {
        assert artifacts != null : "Artifacts cannot be null";    //NOI18N
        assert artifactElements != null : "ArtifactElements cannot be null";  //NOI18N
        assert artifacts.length == artifactElements.length : "Each artifact has to have corresponding artifactElement"; //NOI18N
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws Exception {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty (WebProjectProperties.JAVAC_CLASSPATH);
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupport.TAG_WEB_MODULE_LIBRARIES);
                        boolean changed = false;
                        for (int i=0; i<artifacts.length; i++) {
                            assert artifacts[i] != null;
                            assert artifactElements[i] != null;
                            ClassPathSupport.Item item = ClassPathSupport.Item.create( artifacts[i], artifactElements[i], null, ClassPathSupport.Item.PATH_IN_WAR_NONE);
                            if (!resources.contains(item)) {
                                resources.add (item);
                                changed = true;
                            }
                        }                            
                        if (changed) {
                            String itemRefs[] = cs.encodeToStrings( resources.iterator(), ClassPathSupport.TAG_WEB_MODULE_LIBRARIES);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                            props.setProperty (WebProjectProperties.JAVAC_CLASSPATH, itemRefs);
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
                Exception t = new IOException ();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
            }
        }
    }

    public boolean addPackageRoots(final URL[] roots,final String path) throws IOException {
        assert roots != null : "The classPathRoots cannot be null";      //NOI18N        
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws Exception {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty((String)props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL));                            
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupport.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                        boolean changed = false;
                        for (int i=0; i< roots.length; i++) {
                            assert roots[i] != null;
                            assert roots[i].toExternalForm().endsWith("/");    //NOI18N
                            URL toAdd = FileUtil.getArchiveFile(roots[i]);
                            if (toAdd == null) {
                                toAdd = roots[i];
                            }
                            File f = FileUtil.normalizeFile( new File (URI.create(toAdd.toExternalForm())));
                            if (f == null ) {
                                throw new IllegalArgumentException ("The file must exist on disk");     //NOI18N
                            }
                            ClassPathSupport.Item item = ClassPathSupport.Item.create( f, null, path);
                            if (!resources.contains(item)) {
                                resources.add (item);
                                changed = true;
                            }                            
                        }                                                                                                                
                        if (changed) {
                            String itemRefs[] = cs.encodeToStrings( resources.iterator(), ClassPathSupport.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                            props.setProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL, itemRefs);
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
                Exception t = new IOException ();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
            }
        }
    }

    public boolean addCompileRoots(final URL[] roots) throws IOException {
        assert roots != null : "The classPathRoots cannot be null";      //NOI18N        
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws Exception {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty(WebProjectProperties.JAVAC_CLASSPATH);                            
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, ClassPathSupport.TAG_WEB_MODULE_LIBRARIES);
                        boolean changed = false;
                        for (int i=0; i< roots.length; i++) {
                            assert roots[i] != null;
                            assert roots[i].toExternalForm().endsWith("/");    //NOI18N
                            URL toAdd = FileUtil.getArchiveFile(roots[i]);
                            if (toAdd == null) {
                                toAdd = roots[i];
                            }
                            File f = FileUtil.normalizeFile( new File (URI.create(toAdd.toExternalForm())));
                            if (f == null ) {
                                throw new IllegalArgumentException ("The file must exist on disk");     //NOI18N
                            }
                            ClassPathSupport.Item item = ClassPathSupport.Item.create( f, null, ClassPathSupport.Item.PATH_IN_WAR_NONE);
                            if (!resources.contains(item)) {
                                resources.add (item);
                                changed = true;
                            }                            
                        }                                                                                                                
                        if (changed) {
                            String itemRefs[] = cs.encodeToStrings( resources.iterator(), ClassPathSupport.TAG_WEB_MODULE_LIBRARIES);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                            props.setProperty(WebProjectProperties.JAVAC_CLASSPATH, itemRefs);
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
                Exception t = new IOException ();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
            }
        }
    }

}
