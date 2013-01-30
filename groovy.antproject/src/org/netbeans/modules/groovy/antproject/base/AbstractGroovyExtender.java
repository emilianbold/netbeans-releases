/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.antproject.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.groovy.support.spi.GroovyExtender;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Base class for each Ant based groovy extender. This class encapsulates most of
 * the code common for those types of project. The only thing that implementor
 * needs to do is return xls file used for groovy-build.xml generation.
 *
 * @author Martin Janicek
 */
public abstract class AbstractGroovyExtender implements GroovyExtender {


    private static final String EXTENSIBLE_TARGET_NAME = "-pre-pre-compile"; // NOI18N
    private static final String GROOVY_EXTENSION_ID = "groovy"; // NOI18N
    private static final String PROJECT_PROPERTIES_PATH = "nbproject/project.properties"; // NOI18N
    private static final String EXCLUDE_PROPERTY = "build.classes.excludes"; // NOI18N
    private static final String DISABLE_COMPILE_ON_SAVE = "compile.on.save.unsupported.groovy"; // NOI18N
    private static final String EXCLUSION_PATTERN = "**/*.groovy"; // NOI18N

    private final Project project;


    protected AbstractGroovyExtender(Project project) {
        this.project = project;
    }

    protected abstract URL getGroovyBuildXls();


    /**
     * Checks only build script extension, not classpath, not excludes
     * @return true if build script is modified with groovy extension
     */
    @Override
    public boolean isActive() {
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        return extender != null && extender.getExtension(GROOVY_EXTENSION_ID) != null;
    }

    @Override
    public boolean activate() {
        return addClasspath() && addExcludes() && addBuildScript() && addDisableCompileOnSaveProperty();
    }

    @Override
    public boolean deactivate() {
        return removeClasspath() && removeExcludes() && removeBuildScript() && removeDisableCompileOnSaveProperty();
    }

    /**
     * Add groovy-all.jar on classpath
     */
    private boolean addClasspath() {
        Library groovyAllLib = LibraryManager.getDefault().getLibrary("groovy-all"); // NOI18N
        if (groovyAllLib != null) {
            try {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (SourceGroup sourceGroup : sourceGroups) {
                    if (!sourceGroup.getRootFolder().getName().equals("test")) {
                        ProjectClassPathModifier.addLibraries(new Library[]{groovyAllLib}, sourceGroup.getRootFolder(), ClassPath.COMPILE);
                    }
                }
                return true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (UnsupportedOperationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    /**
     * Removes groovy-all library from classpath
     */
    private boolean removeClasspath() {
        Library groovyAllLib = LibraryManager.getDefault().getLibrary("groovy-all"); // NOI18N
        if (groovyAllLib != null) {
            try {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (SourceGroup sourceGroup : sourceGroups) {
                    ProjectClassPathModifier.removeLibraries(new Library[]{groovyAllLib}, sourceGroup.getRootFolder(), ClassPath.COMPILE);
                }
                return true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (UnsupportedOperationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    /**
     * Add *.groovy to excludes
     */
    private boolean addExcludes() {
        try {
            EditableProperties props = getEditableProperties(project, PROJECT_PROPERTIES_PATH);
            String exclude = props.getProperty(EXCLUDE_PROPERTY);
            if (!exclude.contains(EXCLUSION_PATTERN)) {
                props.setProperty(EXCLUDE_PROPERTY, exclude + "," + EXCLUSION_PATTERN); // NOI18N
                storeEditableProperties(project, PROJECT_PROPERTIES_PATH, props);
            }
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    /**
     * Removes *.groovy from excludes
     */
    private boolean removeExcludes() {
        try {
            EditableProperties props = getEditableProperties(project, PROJECT_PROPERTIES_PATH);
            String exclude = props.getProperty(EXCLUDE_PROPERTY);
            if (exclude.contains("," + EXCLUSION_PATTERN)) {
                exclude = exclude.replace("," + EXCLUSION_PATTERN, "");
                props.setProperty(EXCLUDE_PROPERTY, exclude);
                storeEditableProperties(project, PROJECT_PROPERTIES_PATH, props);
            }
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    /**
     * Wrap javac into groovyc using imported groovy-build.xml
     */
    private boolean addBuildScript() {
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender != null && extender.getExtensibleTargets().contains(EXTENSIBLE_TARGET_NAME)) {
            AntBuildExtender.Extension extension = extender.getExtension(GROOVY_EXTENSION_ID);
            if (extension == null) {
                FileObject destDirFO = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
                try {
                    GeneratedFilesHelper helper = new GeneratedFilesHelper(project.getProjectDirectory());
                    helper.generateBuildScriptFromStylesheet("nbproject/groovy-build.xml", getGroovyBuildXls());
                    FileObject destFileFO = destDirFO.getFileObject("groovy-build", "xml"); // NOI18N
                    extension = extender.addExtension(GROOVY_EXTENSION_ID, destFileFO);
                    extension.addDependency(EXTENSIBLE_TARGET_NAME, "-groovy-init-macrodef-javac"); // NOI18N
                    ProjectManager.getDefault().saveProject(project);
                    return true;
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            } else {
                // extension is already registered
                return true;
            }
        }
        return false;
    }

    private boolean removeBuildScript() {
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender != null && extender.getExtensibleTargets().contains(EXTENSIBLE_TARGET_NAME)) {
            AntBuildExtender.Extension extension = extender.getExtension(GROOVY_EXTENSION_ID);
            if (extension != null) {
                FileObject destDirFO = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
                try {
                    extension.removeDependency(EXTENSIBLE_TARGET_NAME, "-groovy-init-macrodef-javac"); // NOI18N
                    extender.removeExtension(GROOVY_EXTENSION_ID);
                    if (destDirFO != null) {
                        FileObject fileToRemove = destDirFO.getFileObject("groovy-build.xml"); // NOI18N
                        if (fileToRemove != null) {
                            fileToRemove.delete();
                        }
                    }
                    ProjectManager.getDefault().saveProject(project);
                    return true;
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            } else {
                // extension is not registered
                return true;
            }
        }
        return false;
    }

    /**
     * Disables compile on save
     */
    private boolean addDisableCompileOnSaveProperty() {
        try {
            EditableProperties props = getEditableProperties(project, PROJECT_PROPERTIES_PATH);
            props.put(DISABLE_COMPILE_ON_SAVE, "true");
            storeEditableProperties(project, PROJECT_PROPERTIES_PATH, props);
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    /**
     * Enabled compile on save
     */
    private boolean removeDisableCompileOnSaveProperty() {
        try {
            EditableProperties props = getEditableProperties(project, PROJECT_PROPERTIES_PATH);
            props.remove(DISABLE_COMPILE_ON_SAVE);
            storeEditableProperties(project, PROJECT_PROPERTIES_PATH, props);
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    private static EditableProperties getEditableProperties(final Project prj,final  String propertiesPath)
        throws IOException {
        try {
            return
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                @Override
                public EditableProperties run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    EditableProperties ep = null;
                    if (propertiesFo!=null) {
                        InputStream is = null;
                        ep = new EditableProperties(false);
                        try {
                            is = propertiesFo.getInputStream();
                            ep.load(is);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                    }
                    return ep;
                }
            });
        } catch (MutexException ex) {
            return null;
        }
    }

    private static void storeEditableProperties(final Project prj, final  String propertiesPath, final EditableProperties ep)
        throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    if (propertiesFo!=null) {
                        OutputStream os = null;
                        try {
                            os = propertiesFo.getOutputStream();
                            ep.store(os);
                        } finally {
                            if (os != null) {
                                os.close();
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
        }
    }
}
