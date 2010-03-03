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

package org.netbeans.modules.maven.apisupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import hidden.org.codehaus.plexus.util.DirectoryScanner;
import hidden.org.codehaus.plexus.util.IOUtil;
import hidden.org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import java.util.Collections;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class MavenNbModuleImpl implements NbModuleProvider {
    private Project project;
    private DependencyAdder dependencyAdder = new DependencyAdder();
    private RequestProcessor.Task tsk = RequestProcessor.getDefault().create(dependencyAdder);
    
    /**
     * the property defined by nbm-maven-plugin's run-ide goal.
     * can help finding the defined netbeans platform.
     */ 
    public static final String PROP_NETBEANS_INSTALL = "netbeans.installation"; //NOI18N
    
    /** Creates a new instance of MavenNbModuleImpl 
     * @param project 
     */
    public MavenNbModuleImpl(Project project) {
        this.project = project;
    }
    
    private File getModuleXmlLocation() {
        String file = PluginPropertyUtils.getPluginProperty(project, 
                "org.codehaus.mojo", //NOI18N
                "nbm-maven-plugin", //NOI18N
                "descriptor", null); //NOI18N
        if (file == null) {
            file = "src/main/nbm/module.xml"; //NOI18N
        }
        File rel = new File(file);
        if (!rel.isAbsolute()) {
            rel = new File(FileUtil.toFile(project.getProjectDirectory()), file);
        }
        return FileUtil.normalizeFile(rel);
    }
    
    private Xpp3Dom getModuleDom() throws UnsupportedEncodingException, IOException, XmlPullParserException {
        //TODO convert to FileOBject and have the IO stream from there..
        if (!getModuleXmlLocation().exists()) {
            return null;
        }
        FileInputStream is = new FileInputStream(getModuleXmlLocation());
        Reader reader = new InputStreamReader(is, "UTF-8"); //NOI18N
        try {
            return Xpp3DomBuilder.build(reader);
        } finally {
            IOUtil.close(reader);
        }
    }
    
    @Override
    public String getSpecVersion() {
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        String specVersion = AdaptNbVersion.adaptVersion(watch.getMavenProject().getVersion(), AdaptNbVersion.TYPE_SPECIFICATION);
        return specVersion;
    }

    @Override
    public String getCodeNameBase() {
        try {
            Xpp3Dom dom = getModuleDom();
            if (dom != null) {
                Xpp3Dom cnb = dom.getChild("codeNameBase"); //NOI18N
                if (cnb != null) {
                    String val = cnb.getValue();
                    if (val.indexOf( "/") > -1) { //NOI18N
                        val = val.substring(0, val.indexOf("/")); //NOI18N
                    }
                    return val;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        MavenProject prj = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        //same fallback is in nbm-maven-plugin
        return prj.getGroupId() + "." + prj.getArtifactId(); //NOI18N
    }

    @Override
    public String getSourceDirectoryPath() {
        //TODO
        return "src/main/java"; //NOI18N
    }

    @Override
    public FileObject getSourceDirectory() {
        FileObject fo = project.getProjectDirectory().getFileObject(getSourceDirectoryPath());
        if (fo == null) {
            try {
                fo = FileUtil.createFolder(project.getProjectDirectory(),
                                           getSourceDirectoryPath());
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return fo;
    }

    @Override
    public FileObject getManifestFile() {
        String path = "src/main/nbm/manifest.mf";  //NOI18N

        try {
            Xpp3Dom dom = getModuleDom();
            if (dom != null) {
                Xpp3Dom cnb = dom.getChild("manifest"); //NOI18N
                if (cnb != null) {
                    path = cnb.getValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return project.getProjectDirectory().getFileObject(path);
    }

    @Override
    public String getResourceDirectoryPath(boolean isTest) {
        if (isTest) {
            return "src/test/resources"; //NOI18N
        }
        return "src/main/resources"; //NOI18N
    }

    @Override
    public boolean addDependency(String codeNameBase, String releaseVersion,
                                 SpecificationVersion version,
                                 boolean useInCompiler) throws IOException {
        String artifactId = codeNameBase.replaceAll("\\.", "-"); //NOI18N
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        if (hasDependency(codeNameBase)) {
            //TODO
            //not sure we ought to check for spec or release version.
            // just ignore for now, not any easy way to upgrade anyway I guess.
            return false;
        }
        Dependency dep = null;
        File platformFile = lookForModuleInPlatform(artifactId);
        if (platformFile != null) {
            try {
                List<NBVersionInfo> lst = RepositoryQueries.findBySHA1(platformFile);
                for (NBVersionInfo elem : lst) {
                    dep = new Dependency();
                    dep.setArtifactId(elem.getArtifactId());
                    dep.setGroupId(elem.getGroupId());
                    dep.setVersion(elem.getVersion());
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (dep == null) {
            //TODO try to guess 
            dep = new Dependency();
            dep.setGroupId("org.netbeans.api"); //NOI18N
            dep.setArtifactId(artifactId);
            if (version != null) {
                dep.setVersion(version.toString());
            } else {
                //try guessing the version according to the rest of netbeans dependencies..
                List deps = watch.getMavenProject().getModel().getDependencies();
                if (deps != null) {
                    Iterator it = deps.iterator();
                    while (it.hasNext()) {
                        Dependency d = (Dependency)it.next();
                        if ("org.netbeans.api".equals(d.getGroupId())) { //NOI18N
                            dep.setVersion(d.getVersion());
                        }
                    }
                }
            }
        }
        if (dep.getVersion() == null) {
            dep.setVersion("RELEASE67"); //NOI18N
        }
        dependencyAdder.addDependency(dep);
        tsk.schedule(200);
        return true;
    }

    /**
     * 6.7 and higher apisupport uses this to add projects to Libraries for suite.
     *
     * Cannot use Maven-based apisupport projects this way as it doesn't build
     * modules into clusters. Workaround is to unpack resulting NBM somewhere
     * and add it as an external binary cluster.
     * @return null
     */
    @Override
    public File getModuleJarLocation() {
        return null;
    }

    public @Override boolean hasDependency(String codeNameBase) throws IOException {
        String artifactId = codeNameBase.replaceAll("\\.", "-"); //NOI18N
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        Set<?> set = watch.getMavenProject().getDependencyArtifacts();
        if (set != null) {
            Iterator<?> it = set.iterator();
            while (it.hasNext()) {
                Artifact art = (Artifact) it.next();
                if (art.getGroupId().startsWith("org.netbeans") && art.getArtifactId().equals(artifactId)) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean prepareContext() throws IllegalStateException {
        //todo if PROP_NETBEANS_INSTALL isn't set yet then ask user to browse for
        //app suite module to provide correct 'layer in context' class path
        return true;
    }
    
    private class DependencyAdder implements Runnable {
        List<Dependency> toAdd = new ArrayList<Dependency>();
        
        private synchronized void addDependency(Dependency dep) {
            toAdd.add(dep);
        }
        
        @Override
        public void run() {
            FileObject fo = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
            ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    synchronized (MavenNbModuleImpl.DependencyAdder.this) {
                        for (Dependency dep : toAdd) {
                            org.netbeans.modules.maven.model.pom.Dependency mdlDep =
                                    ModelUtils.checkModelDependency(model, dep.getGroupId(), dep.getArtifactId(), true);
                            mdlDep.setVersion(dep.getVersion());
                        }
                        toAdd.clear();
                    }
                }
            };
            Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
            //TODO is the manual reload necessary if pom.xml file is being saved?
            NbMavenProject.fireMavenProjectReload(project);
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }
    }
            

    @Override
    public NbModuleType getModuleType() {
        return NbModuleProvider.STANDALONE;
    }


    @Override
    public String getProjectFilePath() {
        return "pom.xml"; //NOI18N
    }

    /**
     * get specification version for the given module.
     * The module isn't necessary a project dependency, more a property of the associated 
     * netbeans platform.
     */ 
    @Override
    public SpecificationVersion getDependencyVersion(String codenamebase) throws IOException {
        String artifactId = codenamebase.replaceAll("\\.", "-"); //NOI18N
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        Set set = watch.getMavenProject().getDependencyArtifacts();
        if (set != null) {
            Iterator it = set.iterator();
            while (it.hasNext()) {
                Artifact art = (Artifact)it.next();
                if (art.getGroupId().startsWith("org.netbeans") && art.getArtifactId().equals(artifactId)) { //NOI18N
                    ExamineManifest exa = new ExamineManifest();
                    exa.setJarFile(art.getFile());
                    if (exa.getSpecVersion() != null) {
                        return new SpecificationVersion(exa.getSpecVersion());
                    }
                }
            }
        }
        File fil = lookForModuleInPlatform(artifactId);
        if (fil != null) {
            ExamineManifest exa = new ExamineManifest();
            exa.setJarFile(fil);
            if (exa.getSpecVersion() != null) {
                return new SpecificationVersion(exa.getSpecVersion());
            }
        }
        //TODO search local repository?? that's probably irrelevant here..
        
        //we're completely clueless.
        return new SpecificationVersion("1.0"); //NOI18N
    }
    
    private File lookForModuleInPlatform(String artifactId) {
        File actPlatform = getActivePlatformLocation();
        if (actPlatform != null) {
            DirectoryScanner walk = new DirectoryScanner();
            walk.setBasedir(actPlatform);
            walk.setIncludes(new String[] {
                "**/" + artifactId + ".jar" //NOI18N
            });
            walk.scan();
            String[] candidates = walk.getIncludedFiles();
            assert candidates != null && candidates.length <= 1;
            if (candidates.length > 0) {
                return new File(actPlatform, candidates[0]);
            }
        }
        return null;
    }

    @Override
    public File getActivePlatformLocation() {
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        String installProp = watch.getMavenProject().getProperties().getProperty(PROP_NETBEANS_INSTALL);
        if (installProp == null) {
            installProp = PluginPropertyUtils.getPluginProperty(watch.getMavenProject(), 
                    "org.codehaus.mojo", "nbm-maven-plugin", "netbeansInstallation", "run-ide"); //NOI18N
        }
        if (installProp != null) {
            File fil = new File(installProp);
            if (fil.exists()) {
                return fil;
            }
        }
        return null;
    }

}
