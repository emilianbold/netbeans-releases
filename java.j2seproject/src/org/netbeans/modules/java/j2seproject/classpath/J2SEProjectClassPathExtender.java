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
package org.netbeans.modules.java.j2seproject.classpath;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.util.List;
import org.netbeans.modules.java.j2seproject.ui.customizer.AntArtifactChooser;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.java.j2seproject.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.java.j2seproject.UpdateHelper;




public class J2SEProjectClassPathExtender implements ProjectClassPathExtender {
    
    private static final String CP_CLASS_PATH = "javac.classpath"; //NOI18N

    private Project project;
    private UpdateHelper helper;
    private ReferenceHelper refHelper;
    private PropertyEvaluator eval;

    public J2SEProjectClassPathExtender (Project project, UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper) {
        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.refHelper = refHelper;
    }

    public boolean addLibrary(final Library library) throws IOException {
        return addLibrary(CP_CLASS_PATH, library);
    }

    public boolean addLibrary(final String classPathId, final Library library) throws IOException {
        assert library != null : "Parameter cannot be null";       //NOI18N
        try {
            return ((Boolean)ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction () {
                        public Object run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathId);
                            J2SEProjectProperties.PathParser parser = new J2SEProjectProperties.PathParser ();
                            List resources = (List) parser.decode(raw, project, helper.getAntProjectHelper(), eval, refHelper);
                            VisualClassPathItem item = VisualClassPathItem.create (library);
                            if (!resources.contains(item)) {
                                resources.add (item);
                                raw = parser.encode (resources, project, helper.getAntProjectHelper(), refHelper);
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties                                
                                String[] items = PropertyUtils.tokenizePath(raw);
                                for (int i=0; i<items.length-1; i++) {
                                    items[i] += ':'; //NOI18N
                                }                                
                                props.setProperty(classPathId, items);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                ProjectManager.getDefault().saveProject(project);
                                return Boolean.TRUE;
                            }
                            return Boolean.FALSE;
                        }
                    }
            )).booleanValue();
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

    public boolean addArchiveFile(final FileObject archiveFile) throws IOException {
        return addArchiveFile(CP_CLASS_PATH,archiveFile);
    }

    public boolean addArchiveFile(final String classPathId, final FileObject archiveFile) throws IOException {
        assert archiveFile != null : "Parameter cannot be null";       //NOI18N
        try {
            return ((Boolean)ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction () {
                        public Object run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathId);
                            J2SEProjectProperties.PathParser parser = new J2SEProjectProperties.PathParser ();
                            List resources = (List) parser.decode(raw, project, helper.getAntProjectHelper(), eval, refHelper);
                            File f = FileUtil.toFile (archiveFile);
                            if (f == null ) {
                                throw new IllegalArgumentException ("The file must exist on disk");     //NOI18N
                            }
                            VisualClassPathItem item = VisualClassPathItem.create (f);
                            if (!resources.contains(item)) {
                                resources.add (item);
                                raw = parser.encode (resources, project, helper.getAntProjectHelper(), refHelper);
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                                String[] items = PropertyUtils.tokenizePath(raw);
                                for (int i=0; i<items.length-1; i++) {
                                    items[i] += ':';  //NOI18N
                                }                                
                                props.setProperty(classPathId, items);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                ProjectManager.getDefault().saveProject(project);
                                return Boolean.TRUE;
                            }
                            return Boolean.FALSE;
                        }
                    }
            )).booleanValue();
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

    public boolean addAntArtifact(final AntArtifact artifact, final URI artifactElement) throws IOException {
        return addAntArtifact(CP_CLASS_PATH,artifact, artifactElement);
    }

    public boolean addAntArtifact(final String classPathId, final AntArtifact artifact, final URI artifactElement) throws IOException {
        assert artifact != null : "Parameter cannot be null";       //NOI18N
        try {
            return ((Boolean)ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction () {
                        public Object run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty (classPathId);
                            J2SEProjectProperties.PathParser parser = new J2SEProjectProperties.PathParser ();
                            List resources = (List) parser.decode(raw, project, helper.getAntProjectHelper(), eval, refHelper);
                            AntArtifactChooser.ArtifactItem ai = new AntArtifactChooser.ArtifactItem(artifact, artifactElement);
                            VisualClassPathItem item = VisualClassPathItem.create (ai);
                            if (!resources.contains(item)) {
                                resources.add (item);
                                raw = parser.encode (resources, project, helper.getAntProjectHelper(), refHelper);
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                                String[] items = PropertyUtils.tokenizePath(raw);
                                for (int i=0; i<items.length-1; i++) {
                                    items[i] += ':'; //NOI18N
                                }                                                                                                
                                props.setProperty (classPathId, items);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                ProjectManager.getDefault().saveProject(project);
                                return Boolean.TRUE;
                            }
                            return Boolean.FALSE;
                        }
                    }
            )).booleanValue();
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
