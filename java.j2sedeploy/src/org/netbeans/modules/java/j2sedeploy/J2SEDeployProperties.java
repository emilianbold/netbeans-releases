/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2sedeploy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *  Java SE Deployment panel Project Properties support
 * 
 * @author Petr Somol
 * @since java.j2seproject 1.65
 */
public final class J2SEDeployProperties {

    // Deployment - native packaging
    public static final String JAVASE_NATIVE_BUNDLING_ENABLED = "native.bundling.enabled"; //NOI18N
    // copied from JFXProjectProperties
    public static final String JAVAFX_ENABLED = "javafx.enabled"; // NOI18N
    // Deploy panel component properties (to transfer FX listeners from FX project)
    public static final String PASS_OK_LISTENER = "pass.OK.listener"; // NOI18N
    public static final String PASS_STORE_LISTENER = "pass.Store.listener"; // NOI18N
    public static final String PASS_CLOSE_LISTENER = "pass.Close.listener"; // NOI18N
    
    // Project related references
    private J2SEPropertyEvaluator j2sePropEval;
    private PropertyEvaluator evaluator;
    private Project project;

    public Project getProject() {
        return project;
    }
    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    boolean nativeBundlingEnabled;
    public boolean getNativeBundlingEnabled() {
        return nativeBundlingEnabled;
    }
    public void setNativeBundlingEnabled(boolean enabled) {
        this.nativeBundlingEnabled = enabled;
    }

    /** Keeps singleton instance of J2SEDeployProperties for SE project with Deployment category open */
    private static Map<String, J2SEDeployProperties> propInstance = new HashMap<String, J2SEDeployProperties>();

    /** Factory method */
    public static J2SEDeployProperties getInstance(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        J2SEDeployProperties prop = propInstance.get(projDir);
        if(prop == null) {
            prop = new J2SEDeployProperties(context);
            propInstance.put(projDir, prop);
        }
        return prop;
    }

    /** Getter method */
    public static J2SEDeployProperties getInstanceIfExists(Project proj) {
        assert proj != null;
        String projDir = proj.getProjectDirectory().getPath();
        J2SEDeployProperties prop = propInstance.get(projDir);
        if(prop != null) {
            return prop;
        }
        return null;
    }

    /** Getter method */
    public static J2SEDeployProperties getInstanceIfExists(Lookup context) {
        Project proj = context.lookup(Project.class);
        return getInstanceIfExists(proj);
    }

    public static void cleanup(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        propInstance.remove(projDir);
    }

    /** Creates a new instance of J2SEDeployProperties */
    private J2SEDeployProperties(Lookup context) {       
        project = context.lookup(Project.class);
        if (project != null) {
            j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            evaluator = j2sePropEval.evaluator();
            nativeBundlingEnabled = isTrue(evaluator.getProperty(JAVASE_NATIVE_BUNDLING_ENABLED));
        }
    }
    
    private static void setOrRemove(@NonNull org.netbeans.spi.project.support.ant.EditableProperties props, @NonNull String name, String value) {
        if (value != null) {
            props.setProperty(name, value);
        } else {
            props.remove(name);
        }
    }
  
    public void store() throws IOException {
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    final InputStream is = projPropsFO.getInputStream();
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    setOrRemove(ep, JAVASE_NATIVE_BUNDLING_ENABLED, nativeBundlingEnabled ? "true" : null); //NOI18N
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (os != null) {
                            os.close();
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
    }
    
    public static boolean isTrue(final String value) {
        return value != null &&
                (value.equalsIgnoreCase("true") ||  //NOI18N
                 value.equalsIgnoreCase("yes") ||   //NOI18N
                 value.equalsIgnoreCase("on"));     //NOI18N
    }

    public static boolean isNonEmpty(String s) {
        return s != null && !s.isEmpty();
    }
            
    public static boolean isEqual(final String s1, final String s2) {
        return (s1 == null && s2 == null) ||
                (s1 != null && s2 != null && s1.equals(s2));
    }                                   

    public static boolean isEqualIgnoreCase(final String s1, final String s2) {
        return (s1 == null && s2 == null) ||
                (s1 != null && s2 != null && s1.equalsIgnoreCase(s2));
    }                                   

    public static boolean isEqualText(final String s1, final String s2) {
        return ((s1 == null || s1.isEmpty()) && (s2 == null || s2.isEmpty())) ||
                (s1 != null && s2 != null && s1.equals(s2));
    }                                   

}
