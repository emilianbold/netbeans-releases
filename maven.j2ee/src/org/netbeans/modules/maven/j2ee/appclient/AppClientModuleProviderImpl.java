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
package org.netbeans.modules.maven.j2ee.appclient;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.POHImpl;
import org.netbeans.modules.maven.j2ee.CopyOnSave;
import org.netbeans.modules.maven.j2ee.web.WebRunCustomizerPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class AppClientModuleProviderImpl extends J2eeModuleProvider   {
    
    private AppClientImpl appClientImpl;
    private Project project;
    private String serverInstanceID;
    private J2eeModule j2eemodule;    
    private NbMavenProject mavenproject;
    private final CopyOnSave copyOnSave;

    
    /** Creates a new instance of EjbModuleProviderImpl */
    public AppClientModuleProviderImpl(Project proj) {
        project = proj;
        appClientImpl = new AppClientImpl(project, this);
        mavenproject = project.getLookup().lookup(NbMavenProject.class);
        copyOnSave = new CopyOnSave(proj, this);
    }

    @Override
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return copyOnSave;
    }

    public CopyOnSave getCopyOnSaveSupport() {
        return copyOnSave;
    }

    @Override
    public boolean isOnlyCompileOnSaveEnabled() {
        return RunUtils.hasApplicationCompileOnSaveEnabled(project) && !WebRunCustomizerPanel.isDeployOnSave(project);
    }

    public AppClientImpl getModuleImpl() {
        return appClientImpl;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public synchronized J2eeModule getJ2eeModule() {
        if (j2eemodule == null) {
            j2eemodule = J2eeModuleFactory.createJ2eeModule(appClientImpl);
        }
        return j2eemodule; 
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public ModuleChangeReporter getModuleChangeReporter() {
        return appClientImpl;
    }


    @Override
    public void setServerInstanceID(String string) {
        String oldone = null;
        if (serverInstanceID != null) {
            oldone = POHImpl.privateGetServerId(serverInstanceID);
        }
        serverInstanceID = string;
        if (oldone != null) {
            fireServerChange(oldone, getServerID());            
        }
    }
    
    @Override
    public String getServerInstanceID() {
        if (serverInstanceID != null && POHImpl.privateGetServerId(serverInstanceID) != null) {
            return serverInstanceID;
        }
        return ExecutionChecker.DEV_NULL;
    }
    
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
    
    @Override
    public FileObject[] getSourceRoots() {
        ProjectSourcesClassPathProvider cppImpl = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        ClassPath cp = cppImpl.getProjectSourcesClassPath(ClassPath.SOURCE);
        List<URL> resUris = new ArrayList<URL>();
        for (URI uri : mavenproject.getResources(false)) {
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

    @Override
    public File[] getRequiredLibraries() {
        ProjectSourcesClassPathProvider cppImpl = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        // do not use COMPILE classpath here because it contains dependencies
        // with *provided* scope which should not be deployed
        ClassPath cp = cppImpl.getProjectSourcesClassPath(ClassPath.EXECUTE);
        List<File> files = new ArrayList<File>();
        for (FileObject fo : cp.getRoots()) {
            fo = FileUtil.getArchiveFile(fo);
            if (fo == null) {
                continue;
            }
            files.add(FileUtil.toFile(fo));
        }
        return files.toArray(new File[files.size()]);
    }
}
