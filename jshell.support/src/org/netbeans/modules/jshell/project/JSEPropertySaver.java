/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.api.J2SECustomPropertySaver;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author sdedic
 */
//@ProjectServiceProvider(projectType = "org-netbeans-modules-java-j2seproject", 
//        service = {
//            JSEPropertySaver.class
//        })
public class JSEPropertySaver implements J2SECustomPropertySaver {
    public static final String PROP_JSHELL_ENABLED = "jshell.run.enabled"; // NOI18N
    public static final String PROP_CLASSLOADER = "jshell.run.classloader"; // NOI18N
    public static final String PROP_CLASSNAME = "jshell.classloader.from.class"; // NOI18N
    public static final String PROP_FROM_METHOD = "jshell.classloader.from.method"; // NOI18N
    public static final String PROP_FROM_FIELD = "jshell.classloader.from.field"; // NOI18N
    
    private static final Map<URI, Reference<RunOptionsModel>> models = new HashMap<>();
    
    private final Project project;

    public JSEPropertySaver(Project project) {
        this.project = project;
    }
    
    public static RunOptionsModel getModel(Project p) {
        return getModel(p, false);
    }
    
    public boolean isJShellEnabled() {
        return getModel(project, true).isJshellEnabled();
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

    private synchronized static RunOptionsModel getModel(Project p, boolean create) {
        RunOptionsModel mdl;
        
        Reference<RunOptionsModel> ref =  models.get(p.getProjectDirectory().toURI());
        if (ref != null) {
            mdl = ref.get();
            if (!create || mdl != null) {
                return mdl;
            }
        }
        mdl = loadModel(new RunOptionsModel(), p);
        if (mdl != null) {
            models.put(p.getProjectDirectory().toURI(), new WeakReference<>(mdl));
        }
        return mdl;
    }
    
    public static RunOptionsModel load(Project p) {
        return getModel(p, true);
    }
    
    private static RunOptionsModel loadModel(RunOptionsModel mdl, Project p) {
        try {
            EditableProperties props = getEditableProperties(p, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
            String policy = props.getProperty(PROP_CLASSLOADER);
            RunOptionsModel.LoaderPolicy val = null;
            mdl.setJshellEnabled(Boolean.valueOf(props.getOrDefault(PROP_JSHELL_ENABLED, Boolean.FALSE.toString())));
            if (policy != null) {
                try {
                    val = RunOptionsModel.LoaderPolicy.valueOf(policy.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    // malformed property value
                }
            }
            if (val == null) {
                val = RunOptionsModel.LoaderPolicy.SYSTEM;
            }
            mdl.setPolicy(val);
            mdl.setLoadClassName(props.getProperty(PROP_CLASSNAME));
            mdl.setMethodName(props.getProperty(PROP_FROM_FIELD));
            mdl.setFieldName(props.getProperty(PROP_FROM_METHOD));

            return mdl;
        } catch (IOException ex) {
            return null;
        }
    }
    
    @Override
    public void save(Project p) {
        RunOptionsModel mdl = getModel(p);
        if (mdl == null) {
            return;
        }
        
        try {
            EditableProperties props = getEditableProperties(p, AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.put(PROP_JSHELL_ENABLED, Boolean.toString(mdl.isJshellEnabled()));
            if (!mdl.isJshellEnabled()) {
                return;
            }
            props.put(PROP_CLASSLOADER, mdl.getPolicy().toString().toLowerCase());
            switch (mdl.getPolicy()) {
                case SYSTEM:
                    props.remove(PROP_CLASSNAME);
                    props.remove(PROP_FROM_METHOD);
                    props.remove(PROP_FROM_FIELD);
                    break;
                case CLASS:
                    props.put(PROP_CLASSNAME, mdl.getLoadClassName());
                    props.remove(PROP_FROM_METHOD);
                    props.remove(PROP_FROM_FIELD);
                    break;
                case EVAL:
                    props.put(PROP_CLASSNAME, mdl.getLoadClassName());
                    if (mdl.getMethodName() == null) {
                        props.remove(PROP_FROM_METHOD);
                    } else {
                        props.put(PROP_FROM_METHOD, mdl.getMethodName());
                    }
                    if (mdl.getFieldName() == null) {
                        props.remove(PROP_FROM_FIELD);
                    } else {
                        props.put(PROP_FROM_FIELD, mdl.getFieldName());
                    }
                    break;

            }
            storeEditableProperties(p, AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
