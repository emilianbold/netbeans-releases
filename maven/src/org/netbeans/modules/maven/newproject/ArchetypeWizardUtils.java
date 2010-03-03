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

package org.netbeans.modules.maven.newproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.archetype.metadata.io.xpp3.ArchetypeDescriptorXpp3Reader;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.options.MavenCommandSettings;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.MavenProjectPropsImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * @author mkleint
 */
public class ArchetypeWizardUtils {
    private static final String USER_DIR_PROP = "user.dir"; //NOI18N

    /**
     * No instances, utility class.
     */
    private ArchetypeWizardUtils() {
    }

    public static Archetype[] WEB_APP_ARCHS;
    public static Archetype[] EJB_ARCHS;
    public static Archetype[] EAR_ARCHS;
    public static final Archetype EA_ARCH;
    
    public static final Archetype NB_MODULE_ARCH, NB_APP_ARCH;
    public static final Archetype OSGI_ARCH, NB_MODULE_OSGI_ARCH;

    public static final String[] EE_LEVELS = new String[] {
        NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_JEE6"), //NOI18N
        NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_JEE5"), //NOI18N
        NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_J2EE14") //NOI18N
    };

    static {
        WEB_APP_ARCHS = new Archetype[3];

        Archetype arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0.2"); //NOI18N
        arch.setArtifactId("webapp-javaee6"); //NOI18N
        WEB_APP_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0.1"); //NOI18N
        arch.setArtifactId("webapp-jee5"); //NOI18N
        WEB_APP_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0.1"); //NOI18N
        arch.setArtifactId("webapp-j2ee14"); //NOI18N
        WEB_APP_ARCHS[2] = arch;

        EJB_ARCHS = new Archetype[3];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0.2"); //NOI18N
        arch.setArtifactId("ejb-javaee6"); //NOI18N
        EJB_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0.1"); //NOI18N
        arch.setArtifactId("ejb-jee5"); //NOI18N
        EJB_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0.1"); //NOI18N
        arch.setArtifactId("ejb-j2ee14"); //NOI18N
        EJB_ARCHS[2] = arch;

        EAR_ARCHS = new Archetype[3];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0"); //NOI18N
        arch.setArtifactId("ear-javaee6"); //NOI18N
        EAR_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0.1"); //NOI18N
        arch.setArtifactId("ear-jee5"); //NOI18N
        EAR_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0.1"); //NOI18N
        arch.setArtifactId("ear-j2ee14"); //NOI18N
        EAR_ARCHS[2] = arch;

        EA_ARCH = new Archetype();
        EA_ARCH.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        EA_ARCH.setVersion("1.0.1"); //NOI18N
        EA_ARCH.setArtifactId("pom-root"); //NOI18N

        NB_MODULE_ARCH = new Archetype();
        NB_MODULE_ARCH.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        NB_MODULE_ARCH.setVersion("1.3-SNAPSHOT"); //NOI18N
        NB_MODULE_ARCH.setArtifactId("nbm-archetype"); //NOI18N
        NB_MODULE_ARCH.setRepository("http://snapshots.repository.codehaus.org/");

        NB_APP_ARCH = new Archetype();
        NB_APP_ARCH.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        NB_APP_ARCH.setVersion("1.3-SNAPSHOT"); //NOI18N
        NB_APP_ARCH.setArtifactId("netbeans-platform-app-archetype"); //NOI18N
        NB_APP_ARCH.setRepository("http://snapshots.repository.codehaus.org/");

        OSGI_ARCH = new Archetype();
        OSGI_ARCH.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        OSGI_ARCH.setVersion("1.0-SNAPSHOT"); //NOI18N
        OSGI_ARCH.setArtifactId("osgi-archetype"); //NOI18N
        OSGI_ARCH.setRepository("http://snapshots.repository.codehaus.org/");

        NB_MODULE_OSGI_ARCH = new Archetype();
        NB_MODULE_OSGI_ARCH.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        NB_MODULE_OSGI_ARCH.setVersion("1.0-SNAPSHOT"); //NOI18N
        NB_MODULE_OSGI_ARCH.setArtifactId("nbm-osgi-archetype"); //NOI18N
        NB_MODULE_OSGI_ARCH.setRepository("http://snapshots.repository.codehaus.org/");
    }


    private static void runArchetype(File directory, ProjectInfo vi, Archetype arch, Map<String, String> additional) throws IOException {
        Properties props = new Properties();

        props.setProperty("artifactId", vi.artifactId); //NOI18N
        props.setProperty("version", vi.version); //NOI18N
        props.setProperty("groupId", vi.groupId); //NOI18N
        final String pack = vi.packageName;
        if (pack != null && pack.trim().length() > 0) {
            props.setProperty("package", pack); //NOI18N
        }
        props.setProperty("archetypeArtifactId", arch.getArtifactId()); //NOI18N
        props.setProperty("archetypeGroupId", arch.getGroupId()); //NOI18N
        props.setProperty("archetypeVersion", arch.getVersion()); //NOI18N
        props.setProperty("basedir", directory.getAbsolutePath());//NOI18N

        if (additional != null) {
            for (String key : additional.keySet()) {
                props.setProperty(key, additional.get(key));
            }
        }
        BeanRunConfig config = new BeanRunConfig();
        config.setActivatedProfiles(Collections.<String>emptyList());
        config.setExecutionDirectory(directory);
        config.setExecutionName(NbBundle.getMessage(ArchetypeWizardUtils.class, "RUN_Project_Creation"));
        config.setGoals(Collections.singletonList(MavenCommandSettings.getDefault().getCommand(MavenCommandSettings.COMMAND_CREATE_ARCHETYPENG))); //NOI18N
        if (arch.getRepository() != null) {
            props.setProperty("archetype.repository", arch.getRepository()); //NOI18N
            props.setProperty("archetypeRepository", arch.getRepository()); //NOI18N
        }

        //ExecutionRequest.setInteractive seems to have no influence on archetype plugin.
        config.setInteractive(false);
        props.setProperty("archetype.interactive", "false");//NOI18N
        config.setProperties(props);
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

    public static Map<String, String> getAdditionalProperties(Artifact art) {
        HashMap<String, String> map = new HashMap<String, String>();
        File fil = art.getFile();
        JarFile jf = null;
        try {
            jf = new JarFile(fil);
            ZipEntry entry = jf.getJarEntry("META-INF/maven/archetype-metadata.xml");//NOI18N
            if (entry != null) {
                InputStream in = jf.getInputStream(entry);
                Reader rd = new InputStreamReader(in);
                ArchetypeDescriptorXpp3Reader reader = new ArchetypeDescriptorXpp3Reader();
                ArchetypeDescriptor desc = reader.read(rd);
                List lst = desc.getRequiredProperties();
                if (lst != null && lst.size() > 0) {
                    Iterator it = lst.iterator();
                    while (it.hasNext()) {
                        RequiredProperty prop = (RequiredProperty) it.next();
                        map.put(prop.getKey(), prop.getDefaultValue());
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ArchetypeWizardUtils.class.getName()).log(Level.INFO, ex.getMessage(), ex);
            //TODO should we do someting like delete the non-zip file? with the exception thrown the download failed?
        } catch (XmlPullParserException ex) {
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
    public static Set<FileObject> instantiate (ProgressHandle handle, WizardDescriptor wiz) throws IOException {
        ProjectInfo vi = new ProjectInfo();
        vi.groupId = (String)wiz.getProperty("groupId"); //NOI18N
        vi.artifactId = (String)wiz.getProperty("artifactId"); //NOI18N
        vi.version = (String)wiz.getProperty("version"); //NOI18N
        vi.packageName = (String)wiz.getProperty("package"); //NOI18N

        Archetype arch = (Archetype) wiz.getProperty("archetype"); //NOI18N
        logUsage(arch.getGroupId(), arch.getArtifactId(), arch.getVersion());
        
        @SuppressWarnings("unchecked")
        Map<String, String> additional = (Map<String, String>)wiz.getProperty("additionalProps"); //NOI18N

        try {
            ProjectInfo ear_vi = (ProjectInfo)wiz.getProperty("ear_versionInfo"); //NOI18N
            if (ear_vi != null) {
                // enterprise application wizard, multiple archetypes to run
                ProjectInfo web_vi = (ProjectInfo)wiz.getProperty("web_versionInfo"); //NOI18N
                ProjectInfo ejb_vi = (ProjectInfo)wiz.getProperty("ejb_versionInfo"); //NOI18N

                handle.start(8 + (web_vi != null ? 3 : 0) + (ejb_vi != null ? 3 : 0));
                File rootFile = createFromArchetype(handle, (File)wiz.getProperty("projdir"), vi, //NOI18N
                        (Archetype)wiz.getProperty("archetype"), additional, 0); //NOI18N
                createFromArchetype(handle, (File)wiz.getProperty("ear_projdir"), ear_vi, //NOI18N
                        (Archetype)wiz.getProperty("ear_archetype"), null, 4); //NOI18N
                int progressCounter = 6;
                if (web_vi != null) {
                    createFromArchetype(handle, (File)wiz.getProperty("web_projdir"), web_vi, //NOI18N
                            (Archetype)wiz.getProperty("web_archetype"), null, progressCounter); //NOI18N
                    progressCounter += 3;
                }
                if (ejb_vi != null) {
                    createFromArchetype(handle, (File)wiz.getProperty("ejb_projdir"), ejb_vi, //NOI18N
                            (Archetype)wiz.getProperty("ejb_archetype"), null, progressCounter); //NOI18N
                    progressCounter += 3;
                }
                addEARDeps((File)wiz.getProperty("ear_projdir"), ejb_vi, web_vi, progressCounter);
                updateProjectName(rootFile,
                        NbBundle.getMessage(ArchetypeWizardUtils.class, "TXT_EAProjectName", vi.artifactId));
                return openProjects(handle, rootFile, progressCounter);
            } else {
                // other wizards, just one archetype
                handle.start(4);
                File projFile = createFromArchetype(handle, (File)wiz.getProperty("projdir"), vi, //NOI18N
                        (Archetype)wiz.getProperty("archetype"), additional, 0); //NOI18N
                return openProjects(handle, projFile, 3);
            }
        } finally {
            handle.finish();
        }
    }

    private static final String loggerName = "org.netbeans.ui.metrics.maven"; // NOI18N
    private static final String loggerKey = "USG_PROJECT_CREATE_MAVEN"; // NOI18N

    // http://wiki.netbeans.org/UsageLoggingSpecification
    private static void logUsage(String groupId, String artifactId, String version) {
        LogRecord logRecord = new LogRecord(Level.INFO, loggerKey);
        logRecord.setLoggerName(loggerName);
        logRecord.setParameters(new Object[] {groupId + ":" + artifactId + ":" + version}); // NOI18N
        Logger.getLogger(loggerName).log(logRecord);
    }
    
    private static File createFromArchetype (ProgressHandle handle, File projDir, ProjectInfo vi,
        Archetype arch, Map<String, String> additional, int progressCounter) throws IOException {
        handle.progress(++progressCounter);

        final File dirF = FileUtil.normalizeFile(projDir); //NOI18N
        final File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        dirF.getParentFile().mkdirs();
        handle.progress(NbBundle.getMessage(MavenWizardIterator.class, "PRG_Processing_Archetype"), ++progressCounter);

        runArchetype(dirF.getParentFile(), vi, arch, additional);

        handle.progress(++progressCounter);
        return dirF;
    }

    private static Set<FileObject> openProjects (ProgressHandle handle, File dirF, int progressCounter) throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        // Always open top dir as a project:
        FileObject fDir = FileUtil.toFileObject(dirF);
        if (fDir != null) {
            // the archetype generation didn't fail.
            resultSet.add(fDir);
            processProjectFolder(fDir, null);

            FileObject nbAppModuleDir = findNbAppProjectDir(fDir);
            // Look for nested projects to open as well:
            Enumeration<? extends FileObject> e = fDir.getFolders(true);
            while (e.hasMoreElements()) {
                FileObject subfolder = e.nextElement();
                if (ProjectManager.getDefault().isProject(subfolder)) {
                    resultSet.add(subfolder);
                    processProjectFolder(subfolder, nbAppModuleDir);
                }
            }
        }
        handle.progress(++progressCounter);
        return resultSet;
    }

    private static void processProjectFolder(final FileObject fo, final FileObject nbAppModuleDir) {
        try {
            Project prj = ProjectManager.getDefault().findProject(fo);
            if (prj == null) { //#143596
                return;
            }
            final NbMavenProject watch = prj.getLookup().lookup(NbMavenProject.class);
            if (watch != null) {
                // do not create java/test for pom type projects.. most probably not relevant.
                if (! NbMavenProject.TYPE_POM.equals(watch.getPackagingType())) {
                    URI mainJava = FileUtilities.convertStringToUri(watch.getMavenProject().getBuild().getSourceDirectory());
                    URI testJava = FileUtilities.convertStringToUri(watch.getMavenProject().getBuild().getTestSourceDirectory());
                    File file = new File(mainJava);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    file = new File(testJava);
                    if (!file.exists()) {
                        file.mkdirs();
                    }

                    if( nbAppModuleDir != null && NbMavenProject.TYPE_NBM.equals(watch.getPackagingType()) ) {
                        storeNbAppModuleDirInfo( prj, nbAppModuleDir );
                    }
                }
                //see #163529 for reasoning
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        watch.downloadDependencyAndJavadocSource();
                    }
                });
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static FileObject findNbAppProjectDir( FileObject dir ) throws IOException {
        FileObject res = null;
        Enumeration<? extends FileObject> e = dir.getFolders(false); //scan top-level subfolders only
        while (e.hasMoreElements()) {
            FileObject subfolder = e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                Project prj = ProjectManager.getDefault().findProject(subfolder);
                if (prj != null) {
                    NbMavenProject watch = prj.getLookup().lookup(NbMavenProject.class);
                    if (watch != null && NbMavenProject.TYPE_NBM_APPLICATION.equals(watch.getPackagingType())) {
                        res = subfolder;
                        break;
                    }
                }
            }
        }

        return res;
    }

    private static void storeNbAppModuleDirInfo( Project prj, FileObject nbAppModuleDir ) {
        final AuxiliaryConfiguration auxConfig = prj.getLookup().lookup(AuxiliaryConfiguration.class);
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                Element el = auxConfig.getConfigurationFragment(Constants.PROP_PATH_NB_APPLICATION_MODULE,
                        MavenProjectPropsImpl.NAMESPACE, true);
                if (el == null) {
                    el = XMLUtil.createDocument(Constants.PROP_PATH_NB_APPLICATION_MODULE,
                            MavenProjectPropsImpl.NAMESPACE, null, null).getDocumentElement();
                }
                //TODO the following works fine for current nb app suite archetype,
                //otherwise calculate real relative path from nbAppModuleDir
                el.setTextContent("../application"); //NOI18N

                auxConfig.putConfigurationFragment(el, true);
            }
        });
    }

    private static void addEARDeps (File earDir, ProjectInfo ejbVi, ProjectInfo webVi, int progressCounter) {
        FileObject earDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(earDir));
        if (earDirFO == null) {
            return;
        }
        //TODO this will save the file twice - suboptimal
        if (ejbVi != null) {
            // EAR ---> ejb
            ModelUtils.addDependency(earDirFO.getFileObject("pom.xml"), ejbVi.groupId, //NOI18N
                    ejbVi.artifactId, ejbVi.version, "ejb", null, null, true); //NOI18N
        }
        if (webVi != null) {
            // EAR ---> war
            ModelUtils.addDependency(earDirFO.getFileObject("pom.xml"), webVi.groupId, //NOI18N
                    webVi.artifactId, webVi.version, "war", null, null, false); //NOI18N
        }
        progressCounter++;
    }

    private static void updateProjectName (final File projDir, final String newName) {
        FileObject pomFO = FileUtil.toFileObject(new File(projDir, "pom.xml")); //NOI18N
        if (pomFO != null) {
            ModelOperation<POMModel> op = new ModelOperation<POMModel> () {
                @Override
                public void performOperation(POMModel model) {
                    model.getProject().setName(newName);
                }
            };
            Utilities.performPOMModelOperations(pomFO, Collections.singletonList(op));
        }
    }

}
