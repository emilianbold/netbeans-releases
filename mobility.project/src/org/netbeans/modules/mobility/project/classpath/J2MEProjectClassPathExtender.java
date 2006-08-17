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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.project.classpath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.configurations.ProjectConfiguration;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("deprecation")
public class J2MEProjectClassPathExtender implements ProjectClassPathExtender {
    
    private static final String CP_CLASS_PATH = "libs.classpath"; //NOI18N
    
    protected Project project;
    protected ClassPathSupport cs;
    protected AntProjectHelper helper;
    
    public J2MEProjectClassPathExtender(Project project, AntProjectHelper helper, ReferenceHelper refHelper) {
        this.project = project;
        this.helper = helper;
        this.cs = new ClassPathSupport( helper, refHelper);
    }
    
    public boolean addLibrary(final Library library) throws IOException {
        final ArrayList<String> list = collectAllConfigurationPropertiesToChange(CP_CLASS_PATH);
        
        boolean result = false;
        for (int i = 0; i < list.size(); i++)
            result |= addLibrary(list.get(i), library);
        
        return result;
    }
    
    private ArrayList<String> collectAllConfigurationPropertiesToChange(final String property) {
        final ProjectConfigurationsHelper confHelper = project.getLookup().lookup(ProjectConfigurationsHelper.class);
        final ArrayList<String> properties = new ArrayList<String>();
        
        properties.add(property);
        
        ProjectManager.mutex().readAccess(new Runnable() {
            public void run() {
                final EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                
                final ProjectConfiguration[] configurations = confHelper.getConfigurations();
                if (configurations != null)
                    for (int i = 0; i < configurations.length; i++) {
                    final String propName = "configs." + configurations[i].getName() + "." + property;
                    if (props.containsKey(propName))
                        properties.add(propName);
                    }
            }
        });
        
        return properties;
    }
    
    public boolean addLibrary(final String classPathId, final Library library) throws IOException {
        assert library != null : "Parameter cannot be null";       //NOI18N
        try {
            return (ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws Exception {
                    EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    final String raw = props.getProperty(classPathId);
                    final List<VisualClassPathItem> resources = cs.itemsList( raw );
                    final VisualClassPathItem item = VisualClassPathItem.create( library );
                    if (!resources.contains(item)) {
                        resources.add(item);
                        final String itemRefs = cs.encodeToString( resources );
                        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties
                        props.setProperty(classPathId, itemRefs);
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        ProjectManager.getDefault().saveProject(project);
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            }
            )).booleanValue();
        } catch (Exception e) {
                final Exception t = new IOException();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
        }
    }
    
    public boolean addArchiveFile(final FileObject archiveFile) throws IOException {
        final ArrayList<String> list = collectAllConfigurationPropertiesToChange(CP_CLASS_PATH);
        
        boolean result = false;
        for (int i = 0; i < list.size(); i++) 
            result |= addArchiveFile(list.get(i), archiveFile);
        
        return result;
    }
    
    public boolean addArchiveFile(final String classPathId, final FileObject archiveFile) throws IOException {
        assert archiveFile != null : "Parameter cannot be null";       //NOI18N
        try {
            return (ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws Exception {
                    EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    final String raw = props.getProperty(classPathId);
                    final List<VisualClassPathItem> resources = cs.itemsList( raw );
                    final File f = FileUtil.toFile(archiveFile);
                    if (f == null ) {
                        throw new IllegalArgumentException("The file must exist on disk");     //NOI18N
                    }
                    final VisualClassPathItem item = VisualClassPathItem.create( f );
                    
                    if (!resources.contains(item)) {
                        resources.add(item);
                        final String itemRefs = cs.encodeToString( resources );
                        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                        props.setProperty(classPathId, itemRefs);
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        ProjectManager.getDefault().saveProject(project);
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            }
            )).booleanValue();
        } catch (Exception e)   {
                final Exception t = new IOException();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
        }
    }
    
    public boolean addAntArtifact(final AntArtifact artifact, final URI artifactElement) throws IOException {
        final ArrayList<String> list = collectAllConfigurationPropertiesToChange(CP_CLASS_PATH);
        
        boolean result = false;
        for (int i = 0; i < list.size(); i++)
            result |= addAntArtifact(list.get(i), artifact, artifactElement);
        
        return result;
    }
    
    public boolean addAntArtifact(final String classPathId, final AntArtifact artifact, final URI artifactElement) throws IOException {
        assert artifact != null : "Parameter cannot be null";       //NOI18N
        try {
            return (ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws Exception {
                    EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    final String raw = props.getProperty(classPathId);
                    final List<VisualClassPathItem> resources = cs.itemsList( raw );
                    final VisualClassPathItem item = VisualClassPathItem.create( artifact, artifactElement );
                    if (!resources.contains(item)) {
                        resources.add(item);
                        final String itemRefs = cs.encodeToString( resources );
                        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                        props.setProperty(classPathId, itemRefs);
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        ProjectManager.getDefault().saveProject(project);
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            }
            )).booleanValue();
        } catch (Exception e) {
                final Exception t = new IOException();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
        }
    }
    
    private class ClassPathSupport {
        private final AntProjectHelper helper;
        private final ReferenceHelper refHelper;
        
        public ClassPathSupport(AntProjectHelper helper, ReferenceHelper refHelper) {
            this.helper = helper;
            this.refHelper = refHelper;
        }
        
        public List<VisualClassPathItem> itemsList(final String raw) {
            return (List<VisualClassPathItem>)DefaultPropertyParsers.PATH_PARSER.decode(raw, helper, refHelper);
        }
        
        public String encodeToString(final List<VisualClassPathItem> lt) {
            return DefaultPropertyParsers.PATH_PARSER.encode(lt, helper, refHelper);
        }
    }
}
