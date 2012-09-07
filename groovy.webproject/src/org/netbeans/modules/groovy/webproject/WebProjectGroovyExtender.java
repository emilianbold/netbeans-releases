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

package org.netbeans.modules.groovy.webproject;

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
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Implementation of the GroovyExtender for Java EE Ant based projects.
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service =
        GroovyExtender.class,
    projectType = {
        "org-netbeans-modules-web-project"
    }
)
public class WebProjectGroovyExtender implements GroovyExtender {

    private static final String EXTENSIBLE_TARGET_NAME = "-pre-pre-compile"; // NOI18N
    private static final String GROOVY_EXTENSION_ID = "groovy"; // NOI18N
    private static final String GROOVY_BUILD_XSL = "org/netbeans/modules/groovy/webproject/resources/groovy-build.xsl"; // NOI18N
    private static final String J2EE_PROJECT_PROPERTIES_PATH = "nbproject/project.properties"; // NOI18N
    private static final String J2EE_EXCLUDE_PROPERTY = "build.classes.excludes"; // NOI18N
    private static final String J2EE_DISABLE_COMPILE_ON_SAVE = "j2ee.compile.on.save"; // NOI18N
    private static final String J2EE_COMPILE_ON_SAVE_UNSUPPORTED = "compile.on.save.unsupported.groovy"; // NOI18N
    private static final String EXCLUSION_PATTERN = "**/*.groovy"; // NOI18N
    private final Project project;


    public WebProjectGroovyExtender(Project project) {
        this.project = project;
    }


    @Override
    public boolean isActive() {
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        return extender != null && extender.getExtension(GROOVY_EXTENSION_ID) != null;
    }

    @Override
    public boolean activate() {
        return addGroovyToClasspath() && addExcludes() && addBuildScript() && disableCompileOnSave();
    }

    @Override
    public boolean deactivate() {
        return true; // There is currently no way how to disable groovy in existing project
    }

    private boolean addGroovyToClasspath() {
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

    private boolean addExcludes() {
        try {
            EditableProperties props = getEditableProperties(project, J2EE_PROJECT_PROPERTIES_PATH);
            String exclude = props.getProperty(J2EE_EXCLUDE_PROPERTY);
            if (!exclude.contains(EXCLUSION_PATTERN)) {
                props.setProperty(J2EE_EXCLUDE_PROPERTY, exclude + "," + EXCLUSION_PATTERN); // NOI18N
                storeEditableProperties(project, J2EE_PROJECT_PROPERTIES_PATH, props);
            }
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    private boolean addBuildScript() {
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender != null && extender.getExtensibleTargets().contains(EXTENSIBLE_TARGET_NAME)) {
            AntBuildExtender.Extension extension = extender.getExtension(GROOVY_EXTENSION_ID);
            if (extension == null) {
                FileObject destDirFO = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
                try {
                    GeneratedFilesHelper helper = new GeneratedFilesHelper(project.getProjectDirectory());
                    URL stylesheet = this.getClass().getClassLoader().getResource(GROOVY_BUILD_XSL);
                    helper.generateBuildScriptFromStylesheet("nbproject/groovy-build.xml", stylesheet);
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

    private boolean disableCompileOnSave() {
        try {
            EditableProperties props = getEditableProperties(project, J2EE_PROJECT_PROPERTIES_PATH);
            props.put(J2EE_DISABLE_COMPILE_ON_SAVE, "false");
            props.put(J2EE_COMPILE_ON_SAVE_UNSUPPORTED, "true");
            storeEditableProperties(project, J2EE_PROJECT_PROPERTIES_PATH, props);
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    private static EditableProperties getEditableProperties(final Project prj,final  String propertiesPath) throws IOException {
        try {
            return
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                @Override
                public EditableProperties run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    EditableProperties ep = null;
                    if (propertiesFo!=null) {
                        InputStream is = null;
                        ep = new EditableProperties(true);
                        try {
                            is = propertiesFo.getInputStream();
                            ep.load(is);
                        } finally {
                            if (is!=null) is.close();
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
                            if (os!=null) os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
        }
    }
}

