/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.ant;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;

/**
 * 
 * Enables javascript debugging in web projects
 * 
 * XXX Remove this class (and the layer registration) once the javascript
 * debugger is integrated with the trunk
 * 
 * @author quynguyen
 */
public class NbJSDebuggerLookupProvider implements LookupProvider {
    static final String JSDEBUGGER_PROP = "debug.client.available"; // NOI18N
    
    public NbJSDebuggerLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project project = baseContext.lookup(Project.class);
        
        ProjectOpenedHook hook = new ProjectOpenedHook() {

            @Override
            protected void projectOpened() {
                setJSDebuggerProperty(project);
            }

            @Override
            protected void projectClosed() {
                unsetJSDebuggerProperty(project);
            }
        };
        
        return Lookups.fixed(hook);
    }

    void setJSDebuggerProperty(Project project) {
        final FileObject projectDir = project.getProjectDirectory();
        
        try {
            ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Object>() {

                        public Object run() throws Exception {
                            FileObject privateFO = projectDir.getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            EditableProperties props = new EditableProperties();
                            
                            if (privateFO == null) {
                                privateFO = FileUtil.createData(projectDir, JSDEBUGGER_PROP);
                            }else {
                                InputStream is = privateFO.getInputStream();
                                props.load(is);
                                is.close();
                            }
                            props.setProperty(JSDEBUGGER_PROP, "true"); // NOI18N
                            
                            OutputStream os = privateFO.getOutputStream();
                            props.store(os);
                            os.close();
                            
                            return null;
                        }
                    });

        } catch (Exception ex) {
            Log.getLogger().log(
                    Level.SEVERE, 
                    "Could not enable javascript debugger functionality in project: " + // NOI18N
                    FileUtil.getFileDisplayName(projectDir), ex);
        }
    }

    void unsetJSDebuggerProperty(Project project) {
        final FileObject projectDir = project.getProjectDirectory();
        
        try {
            ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Object>() {

                        public Object run() throws Exception {
                            FileObject privateFO = projectDir.getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            EditableProperties props = new EditableProperties();
                            
                            if (privateFO != null) {
                                InputStream is = privateFO.getInputStream();
                                props.load(is);
                                is.close();

                                props.remove(JSDEBUGGER_PROP);

                                OutputStream os = privateFO.getOutputStream();
                                props.store(os);
                                os.close();
                            }
                            return null;
                        }
                    });

        } catch (Exception ex) {
            Log.getLogger().log(
                    Level.SEVERE, 
                    "Could not disable javascript debugger functionality in project: " + // NOI18N
                    FileUtil.getFileDisplayName(projectDir), ex);
        }
    }
    
}
