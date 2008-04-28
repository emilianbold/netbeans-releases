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

package org.netbeans.modules.groovy.support.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.groovy.support.AntHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public final class GroovyCustomizer implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String GROOVY = "Groovy"; // NOI18N
    private static final String PROJECT_PROPERTIES_PATH_J2SE = "nbproject/project.properties"; // NOI18N
    private static final String EXCLUDE_PROPERTY_J2SE = "build.classes.excludes"; // NOI18N
    private static final String EXCLUDE_GROOVY = "**/*.groovy"; // NOI18N

    public static GroovyCustomizer create() {
        return new GroovyCustomizer();
    }

    public Category createCategory(Lookup context) {
        ProjectCustomizer.Category category = ProjectCustomizer.Category.create(
                GROOVY,
                NbBundle.getMessage(GroovyCustomizer.class, "LBL_Groovy_Compiling"),
                null,
                (ProjectCustomizer.Category[]) null
                );
        category.setStoreListener(new StoreActionListener(context));
        return category;
    }

    public JComponent createComponent(Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        AntHelper antHelper = null;
        if (project != null) {
            antHelper = new AntHelper(project);
        }
        return new GroovyCustomizerPanel(antHelper);
    }
    
    private static final class StoreActionListener implements ActionListener {

        private final Lookup context;
        
        public StoreActionListener(Lookup context) {
            this.context = context;
        }
        
        public void actionPerformed(ActionEvent e) {
            
            Project project = context.lookup(Project.class);
            if (project != null) {
                
                // add groovy-all.jar on classpath
                Library groovyAllLib = LibraryManager.getDefault().getLibrary("groovy-all"); // NOI18N
                if (groovyAllLib != null) {
                    FileObject projectDir = project.getProjectDirectory();
                    try {
                        ProjectClassPathModifier.addLibraries(
                                new Library[]{groovyAllLib}, projectDir, ClassPath.COMPILE);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (UnsupportedOperationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    // enhance build script
                    AntHelper antHelper = new AntHelper(project);
                    antHelper.enableGroovy();
                    // add *.groovy to excludes
                    try {
                        EditableProperties props = getEditableProperties(project, PROJECT_PROPERTIES_PATH_J2SE);
                        String exclude = props.getProperty(EXCLUDE_PROPERTY_J2SE);
                        if (!exclude.contains(EXCLUDE_GROOVY)) {
                            props.setProperty(EXCLUDE_PROPERTY_J2SE, exclude + "," + EXCLUDE_GROOVY); // NOI18N
                            storeEditableProperties(project, PROJECT_PROPERTIES_PATH_J2SE, props);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
            }
        }
        
    }
    
    private static EditableProperties getEditableProperties(final Project prj,final  String propertiesPath) 
        throws IOException {        
        try {
            return
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                public EditableProperties run() throws IOException {                                             
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    EditableProperties ep = null;
                    if (propertiesFo!=null) {
                        InputStream is = null; 
                        ep = new EditableProperties();
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
