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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsArtifactsClassPathProvider implements ClassPathProvider {
    private Project project;
    private ClassPath sourceCP, compileCP, bootCP;
    
    private static final Logger LOG = Logger.getLogger(JaxWsArtifactsClassPathProvider.class.getName());
    
    JaxWsArtifactsClassPathProvider(Project project) {
        this.project = project;
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        FileObject clientArtifactsFolder = 
                project.getProjectDirectory().getFileObject("build/generated/wsimport/client"); //NOI18N
        if (clientArtifactsFolder != null && 
                (file.equals(clientArtifactsFolder) || FileUtil.isParentOf(clientArtifactsFolder,file))) {
            if (ClassPath.SOURCE.equals(type)) {
                if (sourceCP == null) {
                    sourceCP = getClassPath(ClassPath.SOURCE);
                }
                return sourceCP;
            } else if (ClassPath.COMPILE.equals(type)) {
                if (compileCP == null) {
                    compileCP = getClassPath(ClassPath.COMPILE);
                }
                return compileCP;
            } else if (ClassPath.BOOT.equals(type)) {
                if (bootCP == null) {
                    bootCP = getBootClassPath();
                }
                return bootCP;
            }               
        } else {
            FileObject serviceArtifactsFolder = 
            project.getProjectDirectory().getFileObject("build/generated/wsimport/service"); //NOI18N
            if (serviceArtifactsFolder != null && 
                    (file.equals(serviceArtifactsFolder) || FileUtil.isParentOf(serviceArtifactsFolder,file))) {
                if (ClassPath.SOURCE.equals(type)) {
                    if (sourceCP == null) {
                        sourceCP = getClassPath(ClassPath.SOURCE);
                    }
                    return sourceCP;
                } else if (ClassPath.COMPILE.equals(type)) {
                    if (compileCP == null) {
                        compileCP = getClassPath(ClassPath.COMPILE);
                    }
                    return compileCP;
                } else if (ClassPath.BOOT.equals(type)) {
                    if (bootCP == null) {
                        bootCP = getBootClassPath();
                    }
                    return bootCP;
                }
            }
        }
           
        return null;
    }
    
    private ClassPath getClassPath(String classPathType) {
        SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (groups.length > 0) {
            return ClassPath.getClassPath(groups[0].getRootFolder(), classPathType);
        }
        return null;
    }
    
    private ClassPath getBootClassPath() {
        Set<File> cp = new HashSet<File>();
        J2eeModuleProvider javaeeModule = project.getLookup().lookup(J2eeModuleProvider.class);
        if (javaeeModule != null) {
            //javaee project type (web, ejb, appclient)
            //=> get required JAX-WS jars form the server
            String serverInstanceID = javaeeModule.getServerInstanceID();
            J2eePlatform javaeeplatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            if (javaeeplatform.isToolSupported(J2eePlatform.TOOL_WSIMPORT)) {
                cp.addAll(Arrays.asList(javaeeplatform.getToolClasspathEntries(J2eePlatform.TOOL_WSIMPORT)));
}
            if (javaeeplatform.isToolSupported(J2eePlatform.TOOL_WSGEN)) {
                cp.addAll(Arrays.asList(javaeeplatform.getToolClasspathEntries(J2eePlatform.TOOL_WSGEN)));
            }
        } else {
            //javase project type
            //=> use JAX-WS API jars supplied with the IDE
            File f = InstalledFileLocator.getDefault().locate("modules/ext/jaxws21/api", null, false); //NOI18N
            cp.addAll(Arrays.asList(f.listFiles()));
        }
        List<PathResourceImplementation> path = new LinkedList<PathResourceImplementation>();
        URL url = null;
        for (File f : cp) {
            try {
                url = FileUtil.getArchiveRoot(f.toURI().toURL());
                path.add(ClassPathSupport.createResource(url));
            } catch (MalformedURLException ex) {
                LOG.severe(ex.getMessage());
            }
        }
        url = null;
        return ClassPathSupport.createClassPath(path);
    }
}
