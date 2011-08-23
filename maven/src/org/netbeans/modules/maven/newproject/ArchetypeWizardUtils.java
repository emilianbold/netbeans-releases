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

package org.netbeans.modules.maven.newproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.options.MavenCommandSettings;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author mkleint
 */
public class ArchetypeWizardUtils {

    /** {@code Map<String,String>} of custom archetype properties to define. */
    public static final String ADDITIONAL_PROPS = "additionalProps"; // NOI18N

    private static final String USER_DIR_PROP = "user.dir"; //NOI18N

    /**
     * No instances, utility class.
     */
    private ArchetypeWizardUtils() {
    }

    static final Archetype[] WEB_APP_ARCHS;
    static final Archetype[] EJB_ARCHS;
    static final Archetype[] EAR_ARCHS;
    static final Archetype[] APPCLIENT_ARCHS;
    static final Archetype EA_ARCH;
    
    static final String[] EE_LEVELS = {
        NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_JEE6"), //NOI18N
        NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_JEE5"), //NOI18N
        NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_J2EE14") //NOI18N
    };

    static {
        WEB_APP_ARCHS = new Archetype[3];

        Archetype arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.5"); //NOI18N
        arch.setArtifactId("webapp-javaee6"); //NOI18N
        WEB_APP_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.3"); //NOI18N
        arch.setArtifactId("webapp-jee5"); //NOI18N
        WEB_APP_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.3"); //NOI18N
        arch.setArtifactId("webapp-j2ee14"); //NOI18N
        WEB_APP_ARCHS[2] = arch;

        EJB_ARCHS = new Archetype[3];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.5"); //NOI18N
        arch.setArtifactId("ejb-javaee6"); //NOI18N
        EJB_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.3"); //NOI18N
        arch.setArtifactId("ejb-jee5"); //NOI18N
        EJB_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.3"); //NOI18N
        arch.setArtifactId("ejb-j2ee14"); //NOI18N
        EJB_ARCHS[2] = arch;

        APPCLIENT_ARCHS = new Archetype[3];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0"); //NOI18N
        arch.setArtifactId("appclient-javaee6"); //NOI18N
        APPCLIENT_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0"); //NOI18N
        arch.setArtifactId("appclient-jee5"); //NOI18N
        APPCLIENT_ARCHS[1] = arch;
        
//        arch = new Archetype();
//        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
//        arch.setVersion("1.0"); //NOI18N
//        arch.setArtifactId("appclient-javaee14"); //NOI18N
        APPCLIENT_ARCHS[2] = arch;

        EAR_ARCHS = new Archetype[3];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.5"); //NOI18N
        arch.setArtifactId("ear-javaee6"); //NOI18N
        EAR_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.4"); //NOI18N
        arch.setArtifactId("ear-jee5"); //NOI18N
        EAR_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.4"); //NOI18N
        arch.setArtifactId("ear-j2ee14"); //NOI18N
        EAR_ARCHS[2] = arch;

        EA_ARCH = new Archetype();
        EA_ARCH.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        EA_ARCH.setVersion("1.1"); //NOI18N
        EA_ARCH.setArtifactId("pom-root"); //NOI18N
    }

    private static void runArchetype(File directory, ProjectInfo vi, Archetype arch, @NullAllowed Map<String,String> additional) throws IOException {
        BeanRunConfig config = new BeanRunConfig();
        config.setProperty("archetypeGroupId", arch.getGroupId()); //NOI18N
        config.setProperty("archetypeArtifactId", arch.getArtifactId()); //NOI18N
        config.setProperty("archetypeVersion", arch.getVersion()); //NOI18N
        String repo = arch.getRepository();
        config.setProperty("archetypeRepository", repo != null ? repo : RepositoryPreferences.REPO_CENTRAL); //NOI18N
        config.setProperty("groupId", vi.groupId); //NOI18N
        config.setProperty("artifactId", vi.artifactId); //NOI18N
        config.setProperty("version", vi.version); //NOI18N
        final String pack = vi.packageName;
        if (pack != null && pack.trim().length() > 0) {
            config.setProperty("package", pack); //NOI18N
        }
        config.setProperty("basedir", directory.getAbsolutePath());//NOI18N

        if (additional != null) {
            for (String key : additional.keySet()) {
                config.setProperty(key, additional.get(key));
            }
        }
        config.setActivatedProfiles(Collections.<String>emptyList());
        config.setExecutionDirectory(directory);
        config.setExecutionName(NbBundle.getMessage(ArchetypeWizardUtils.class, "RUN_Project_Creation"));
        config.setGoals(Collections.singletonList(MavenCommandSettings.getDefault().getCommand(MavenCommandSettings.COMMAND_CREATE_ARCHETYPENG))); //NOI18N

        //ExecutionRequest.setInteractive seems to have no influence on archetype plugin.
        config.setInteractive(false);
        config.setProperty("archetype.interactive", "false");//NOI18N
        //#136853 make sure to get the latest snapshot always..
        if (arch.getVersion().contains("SNAPSHOT")) { //NOI18N
            config.setUpdateSnapshots(true);
        }

        config.setTaskDisplayName(NbBundle.getMessage(ArchetypeWizardUtils.class, "RUN_Maven"));
        // setup executor now..
        //hack - we need to setup the user.dir sys property..
        String oldUserdir = System.getProperty(USER_DIR_PROP); //NOI18N
        System.setProperty(USER_DIR_PROP, directory.getAbsolutePath()); //NOI18N
        try {
            ExecutorTask task = RunUtils.executeMaven(config); //NOI18N
            task.result();
        } finally {
            if (oldUserdir == null) {
                System.getProperties().remove(USER_DIR_PROP); //NOI18N
            } else {
                System.setProperty(USER_DIR_PROP, oldUserdir); //NOI18N
            }
        }
    }

    static Map<String, String> getAdditionalProperties(Artifact art) {
        HashMap<String, String> map = new HashMap<String, String>();
        File fil = art.getFile();
        JarFile jf = null;
        try {
            jf = new JarFile(fil);
            ZipEntry entry = jf.getJarEntry("META-INF/maven/archetype-metadata.xml");//NOI18N
            if (entry == null) {
                entry = jf.getJarEntry("META-INF/maven/archetype.xml");//NOI18N
            }
            if (entry != null) {
                // http://maven.apache.org/archetype/maven-archetype-plugin/specification/archetype-metadata.html
                InputStream in = jf.getInputStream(entry);
                try {
                    Document doc = XMLUtil.parse(new InputSource(in), false, false, XMLUtil.defaultErrorHandler(), null);
                    NodeList nl = doc.getElementsByTagName("requiredProperty"); // NOI18N
                    for (int i = 0; i < nl.getLength(); i++) {
                        Element rP = (Element) nl.item(i);
                        Element dV = XMLUtil.findElement(rP, "defaultValue", null); // NOI18N
                        map.put(rP.getAttribute("key"), dV != null ? XMLUtil.findText(dV) : null); // NOI18N
                    }
                } finally {
                    in.close();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ArchetypeWizardUtils.class.getName()).log(Level.INFO, ex.getMessage(), ex);
            //TODO should we do someting like delete the non-zip file? with the exception thrown the download failed?
        } catch (SAXException ex) {
            Logger.getLogger(ArchetypeWizardUtils.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        } finally {
            if (jf != null) {
                try {
                    jf.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return map;
    }

    /**
     * Instantiates archetype stored in given wizard descriptor, with progress UI notification.
     */
    static Set<FileObject> instantiate(ProgressHandle handle, WizardDescriptor wiz) throws IOException {
        ProjectInfo vi = new ProjectInfo((String) wiz.getProperty("groupId"), (String) wiz.getProperty("artifactId"), (String) wiz.getProperty("version"), (String) wiz.getProperty("package")); //NOI18N

        Archetype arch = (Archetype) wiz.getProperty("archetype"); //NOI18N
        logUsage(arch.getGroupId(), arch.getArtifactId(), arch.getVersion());

        @SuppressWarnings("unchecked")
        Map<String,String> additional = (Map<String,String>) wiz.getProperty(ADDITIONAL_PROPS);

        try {
            ProjectInfo ear_vi = (ProjectInfo)wiz.getProperty("ear_versionInfo"); //NOI18N
            if (ear_vi != null) {
                // enterprise application wizard, multiple archetypes to run
                ProjectInfo web_vi = (ProjectInfo)wiz.getProperty("web_versionInfo"); //NOI18N
                ProjectInfo ejb_vi = (ProjectInfo)wiz.getProperty("ejb_versionInfo"); //NOI18N

                handle.start(8 + (web_vi != null ? 3 : 0) + (ejb_vi != null ? 3 : 0));
                File rootFile = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
                createFromArchetype(handle, rootFile, vi, arch, additional, 0, true);
                File earFile = FileUtil.normalizeFile((File) wiz.getProperty("ear_projdir")); // NOI18N
                createFromArchetype(handle, earFile, ear_vi, (Archetype) wiz.getProperty("ear_archetype"), null, 4, false); //NOI18N
                int progressCounter = 6;
                if (web_vi != null) {
                    createFromArchetype(handle, FileUtil.normalizeFile((File)wiz.getProperty("web_projdir")), web_vi, //NOI18N
                            (Archetype)wiz.getProperty("web_archetype"), null, progressCounter, false); //NOI18N
                    progressCounter += 3;
                }
                if (ejb_vi != null) {
                    createFromArchetype(handle, FileUtil.normalizeFile((File)wiz.getProperty("ejb_projdir")), ejb_vi, //NOI18N
                            (Archetype)wiz.getProperty("ejb_archetype"), null, progressCounter, false); //NOI18N
                    progressCounter += 3;
                }
                addEARDeps((File)wiz.getProperty("ear_projdir"), ejb_vi, web_vi, progressCounter);
                Set<FileObject> projects = openProjects(rootFile, earFile);
                handle.progress(++progressCounter);
                return projects;
            } else {
                handle.start(4);
                File projFile = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
                createFromArchetype(handle, projFile, vi, arch, additional, 0, true);
                Set<FileObject> projects = openProjects(projFile, null);
                handle.progress(4);
                Templates.setDefinesMainProject(wiz, projects.size() > 1);
                return projects;
            }
        } finally {
            handle.finish();
        }
    }

    private static final String loggerName = "org.netbeans.ui.metrics.maven"; // NOI18N
    private static final String loggerKey = "USG_PROJECT_CREATE_MAVEN"; // NOI18N

    // http://wiki.netbeans.org/UsageLoggingSpecification
    public static void logUsage(String groupId, String artifactId, String version) {
        LogRecord logRecord = new LogRecord(Level.INFO, loggerKey);
        logRecord.setLoggerName(loggerName);
        logRecord.setParameters(new Object[] {groupId + ":" + artifactId + ":" + version}); // NOI18N
        Logger.getLogger(loggerName).log(logRecord);
    }
    
    public static void createFromArchetype(ProgressHandle handle, File projDir, ProjectInfo vi, Archetype arch, @NullAllowed Map<String,String> additional, int progressCounter, boolean updateLastUsedProjectDir) throws IOException {
        handle.progress(++progressCounter);

        final File parent = projDir.getParentFile();
        if (updateLastUsedProjectDir && parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        parent.mkdirs();
        handle.progress(NbBundle.getMessage(MavenWizardIterator.class, "PRG_Processing_Archetype"), ++progressCounter);

        runArchetype(parent, vi, arch, additional);

        handle.progress(++progressCounter);
    }

    public static Set<FileObject> openProjects(File dirF, File mainProjectDir) throws IOException {
        List<FileObject> resultList = new ArrayList<FileObject>();

        // Always open top dir as a project:
        FileObject fDir = FileUtil.toFileObject(dirF);
        if (fDir != null) {
            // the archetype generation didn't fail.
            FileObject mainFO = mainProjectDir != null ? FileUtil.toFileObject(mainProjectDir) : null;
            resultList.add(fDir);
            processProjectFolder(fDir);

            // Look for nested projects to open as well:
            Enumeration<? extends FileObject> e = fDir.getFolders(true);
            while (e.hasMoreElements()) {
                FileObject subfolder = e.nextElement();
                if (ProjectManager.getDefault().isProject(subfolder)) {
                    if (subfolder.equals(mainFO)) {
                        resultList.add(0, subfolder);
                    } else {
                        resultList.add(subfolder);
                    }
                    processProjectFolder(subfolder);
                }
            }
        }
        return new LinkedHashSet<FileObject>(resultList);
    }

    private static void processProjectFolder(final FileObject fo) {
        try {
            Project prj = ProjectManager.getDefault().findProject(fo);
            if (prj == null) { //#143596
                return;
            }
            final NbMavenProject watch = prj.getLookup().lookup(NbMavenProject.class);
            if (watch != null) {
                watch.downloadDependencyAndJavadocSource(false);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void addEARDeps (File earDir, ProjectInfo ejbVi, ProjectInfo webVi, int progressCounter) {
        FileObject earDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(earDir));
        if (earDirFO == null) {
            return;
        }
        List<ModelOperation<POMModel>> operations = new ArrayList<ModelOperation<POMModel>>();
        if (ejbVi != null) {
            // EAR ---> ejb
            operations.add(new AddDependencyOperation(ejbVi, "ejb"));
        }
        if (webVi != null) {
            // EAR ---> war
            operations.add(new AddDependencyOperation(webVi, "war"));
        }

        Utilities.performPOMModelOperations(earDirFO.getFileObject("pom.xml"), operations);
        progressCounter++;
    }

    public static class AddDependencyOperation implements ModelOperation<POMModel> {
        private final String group;
        private final String artifact;
        private final String version;
        private final String type;

        public AddDependencyOperation(ProjectInfo info, String type) {
            this.group = info.groupId;
            this.artifact = info.artifactId;
            this.version = info.version;
            this.type = type;
        }

        @Override
        public void performOperation(POMModel model) {
            Dependency dep = ModelUtils.checkModelDependency(model, group, artifact, true);
            dep.setVersion(version);
            dep.setType(type);
        }
    }
}
