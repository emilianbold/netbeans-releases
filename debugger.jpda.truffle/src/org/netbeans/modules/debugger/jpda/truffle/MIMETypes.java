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
package org.netbeans.modules.debugger.jpda.truffle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.base.ProcessBuilder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Get the MIME types of languages installed in the Truffle/Graal platform.
 * 
 * @author Martin
 */
public final class MIMETypes {

    private static final Logger LOG = Logger.getLogger(MIMETypes.class.getName());
    
    public static final String PROP_MIME_TYPES = "MIME types";                  // NOI18N
    
    private static final String MIME_TYPES_MAIN = "org.netbeans.modules.debugger.jpda.backend.truffle.GetMIMETypes";    // NOI18N
    private static final MIMETypes INSTANCE = new MIMETypes();
    private static String TEMP_TRUFFLE_JAR;

    private final Map<JavaPlatform, Set<String>> platformMIMETypes = new WeakHashMap<>();
    private Set<String> allPlatformsMIMETypes;
    private PropertyChangeListener allPlatformsListener;
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private MIMETypes() {
    }
    
    public static MIMETypes getDefault() {
        return INSTANCE;
    }
    
    public Set<String> get(Project prj) {
        JavaPlatform jp = getProjectPlatform(prj);
        if (jp == null) {
            return Collections.EMPTY_SET;
        }
        return get(jp);
    }
    
    private synchronized Set<String> get(JavaPlatform jp) {
        Set<String> mTypes = platformMIMETypes.get(jp);
        if (mTypes == null) {
            FileObject graalvm = jp.findTool("graalvm");                        // NOI18N
            if (graalvm != null) {
                File graalvmFile = FileUtil.toFile(graalvm);
                if (graalvmFile != null) {
                    ProcessBuilder pb = ProcessBuilder.getLocal();
                    pb.setExecutable(graalvmFile.getAbsolutePath());
                    try {
                        pb.setArguments(Arrays.asList("-J:-cp", "-J:"+getTruffleJarPath(), MIME_TYPES_MAIN));   // NOI18N
                        Process proc = pb.call();
                        try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                            mTypes = new HashSet<>();
                            String line;
                            while ((line = r.readLine()) != null) {
                                mTypes.add(line);
                            }
                        }
                        try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                            String line;
                            while ((line = r.readLine()) != null) {
                                LOG.info("Error from "+graalvmFile+" : "+line);
                            }
                        }
                        LOG.log(Level.FINE, "MIME types of {0} are: {1}", new Object[]{jp, mTypes});
                    } catch (IOException ioex) {
                        LOG.log(Level.CONFIG, "", ioex);
                    }
                }
            }
            if (mTypes == null) {
                mTypes = Collections.EMPTY_SET;
            }
            platformMIMETypes.put(jp, mTypes);
        }
        return mTypes;
    }
    
    private static synchronized String getTruffleJarPath() throws IOException {
        if (TEMP_TRUFFLE_JAR == null) {
            File truffleJarFile = File.createTempFile("TmpTruffleBcknd", ".jar");   // NOI18N
            truffleJarFile.deleteOnExit();
            FileUtil.copy(RemoteServices.openRemoteClasses(), new FileOutputStream(truffleJarFile));
            TEMP_TRUFFLE_JAR = truffleJarFile.getAbsolutePath();
        }
        return TEMP_TRUFFLE_JAR;
    }
    
    /**
     * Get MIME types based on registered Java platforms.
     * The call returns either a cached set, or queries the platforms.
     * 
     * @return a set of MIME types.
     */
    public synchronized Set<String> get() {
        if (allPlatformsMIMETypes != null) {
            return allPlatformsMIMETypes;
        }
        JavaPlatformManager pm = JavaPlatformManager.getDefault();
        if (allPlatformsListener == null) {
            allPlatformsListener = new PropertyChangeListener() {
                @Override public void propertyChange(PropertyChangeEvent evt) {
                    synchronized (MIMETypes.this) {
                        allPlatformsMIMETypes = null;
                    }
                    pcs.firePropertyChange(PROP_MIME_TYPES, null, null);
                }
            };
            pm.addPropertyChangeListener(allPlatformsListener);
        }
        JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification ("j2se", null));   //NOI18N
        Set<String> mTypes = new HashSet<>();
        for (int i = 0; i < installedPlatforms.length; i++) {
            mTypes.addAll(get(installedPlatforms[i]));
        }
        allPlatformsMIMETypes = mTypes;
        return mTypes;
    }
    
    /**
     * Get cached MIME types based on registered Java platforms.
     * @return a cached set, or <code>null</code>.
     */
    public synchronized Set<String> getCached() {
        return allPlatformsMIMETypes;
    }
    
    private static JavaPlatform getProjectPlatform(Project prj) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath bootClassPath = ClassPath.getClassPath(sourceGroups[0].getRootFolder(), ClassPath.BOOT);
        FileObject[] prjBootRoots = bootClassPath.getRoots();
        JavaPlatformManager pm = JavaPlatformManager.getDefault();
        JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification ("j2se", null));   //NOI18N
        for (int i = 0; i < installedPlatforms.length; i++) {
            ClassPath bootstrapLibraries = installedPlatforms[i].getBootstrapLibraries();
            if (Arrays.equals(prjBootRoots, bootstrapLibraries.getRoots())) {
                return installedPlatforms[i];
            }
        }
        return null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
}
