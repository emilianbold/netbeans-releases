/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.web;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.POHImpl;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/**
 * web module provider implementation for maven2 project type.
 * @author  Milos Kleint 
 */
public class WebModuleProviderImpl extends J2eeModuleProvider implements WebModuleProvider {
    
    
    private Project project;
    private WebModuleImpl implementation;
    private WebModule module;
    private J2eeModule j2eemodule;
    
    private ModuleChangeReporter moduleChange;
    
    private String serverInstanceID;
    private final CopyOnSave copyOnSave;

    
    public WebModuleProviderImpl(Project proj) {
        project = proj;
        implementation = new WebModuleImpl(project, this);
        moduleChange = new ModuleChangeReporterImpl();
        copyOnSave = new CopyOnSave(proj, this);
    }
    
    public WebModule findWebModule(FileObject fileObject) {
        if (implementation != null && implementation.isValid()) {
            if (module == null) {
                module = WebModuleFactory.createWebModule(implementation);
            }
            return module;
        }
        return null;
    }
    
    public synchronized J2eeModule getJ2eeModule() {
        if (j2eemodule == null) {
            j2eemodule = J2eeModuleFactory.createJ2eeModule(implementation);
        }
        return j2eemodule; 
    }
    
    /**
     * 
     * @return 
     */
    public WebModuleImpl getWebModuleImplementation() {
        return implementation;
    }
    
    public ModuleChangeReporter getModuleChangeReporter() {
        return moduleChange;
    }
    
    public void setServerInstanceID(String str) {
        String oldone = null;
        if (serverInstanceID != null) {
            oldone = POHImpl.privateGetServerId(serverInstanceID);
        }
        serverInstanceID = str;
        if (oldone != null) {
            fireServerChange(oldone, getServerID());            
        }
        // TODO write into the private/public profile..

    }

    @Override
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return copyOnSave;
    }
    
    public CopyOnSave getCopyOnSaveSupport() {
        return copyOnSave;
    }


    @Override
    public DeployOnSaveClassInterceptor getDeployOnSaveClassInterceptor() {
        return new DeployOnSaveClassInterceptor() {
            public ArtifactListener.Artifact convert(ArtifactListener.Artifact original) {
                NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
                FileObject targetClasses = FileUtilities.convertStringToFileObject(prj.getMavenProject().getBuild().getOutputDirectory());
                FileObject clazz = FileUtil.toFileObject(original.getFile());
                if (targetClasses != null && clazz != null) {
                    String path = FileUtil.getRelativePath(targetClasses, clazz);
                    if (path != null) {
                        try {
                            FileObject webBuildBase = implementation.getContentDirectory();
                            if (webBuildBase != null) {
                                File base = FileUtil.toFile(webBuildBase);
                                File dist = new File(base, "WEB-INF" + File.separator + "classes" + File.separator + path.replace("/", File.separator));
                                return original.distributionPath(dist);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                return original;
            }
        };
    }
    
    
    /** Id of server isntance for deployment. The default implementation returns
     * the default server instance selected in Server Registry.
     * The return value may not be null.
     * If modules override this method they also need to override {@link useDefaultServer}.
     */
    @Override
    public String getServerInstanceID() {
        if (serverInstanceID != null && POHImpl.privateGetServerId(serverInstanceID) != null) {
            return serverInstanceID;
        }
        return ExecutionChecker.DEV_NULL;
    }
    
    /** This method is used to determin type of target server.
     * The return value must correspond to value returned from {@link getServerInstanceID}.
     */
    @Override
    public String getServerID() {
        if (serverInstanceID != null) {
            String tr = POHImpl.privateGetServerId(serverInstanceID);
            if (tr != null) {
                return tr;
            }
        }
        return ExecutionChecker.DEV_NULL;
    }

    /**
     *  Returns list of root directories for source files including configuration files.
     *  Examples: file objects for src/java, src/conf.  
     *  Note: 
     *  If there is a standard configuration root, it should be the first one in
     *  the returned list.
     */
    
    @Override
    public FileObject[] getSourceRoots() {
        ProjectSourcesClassPathProvider cppImpl = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        ClassPath cp = cppImpl.getProjectSourcesClassPath(ClassPath.SOURCE);
        NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        List<URL> resUris = new ArrayList<URL>();
        URI webapp = prj.getWebAppDirectory();
        if (webapp != null) {
            try {
                resUris.add(webapp.toURL());
            } catch (MalformedURLException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
        for (URI uri : prj.getResources(false)) {
            try {
                resUris.add(uri.toURL());
            } catch (MalformedURLException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
        Iterator<ClassPath.Entry> en = cp.entries().listIterator();
        List<FileObject> toRet = new ArrayList<FileObject>();
        int index = 0;
        while (en.hasNext()) {
            ClassPath.Entry ent = en.next();
            if (ent.getRoot() == null) continue;
            if (resUris.contains(ent.getURL())) {
                //put resources up front..
                toRet.add(index, ent.getRoot());
                index = index + 1;
            } else {
                toRet.add(ent.getRoot());
            }
        }
        return toRet.toArray(new FileObject[0]);
    }


    // TODO what is this actually good for?
    private class ModuleChangeReporterImpl implements ModuleChangeReporter, EjbChangeDescriptor {
        public EjbChangeDescriptor getEjbChanges(long param) {
            return this;
        }
        
        public boolean isManifestChanged(long param) {
            return false;
        }

        public boolean ejbsChanged() {
            return false;
        }

        public String[] getChangedEjbs() {
            return new String[] {};
        }
        
    }
    
}
