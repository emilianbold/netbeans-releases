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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
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
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * TODO, include this in the main module (nb-project)
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

    public static final String[] EE_LEVELS = new String[] {
        NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_JEE5"), //NOI18N
        NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_J2EE14"), //NOI18N
        NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_J2EE13") //NOI18N
    };

    static {
        WEB_APP_ARCHS = new Archetype[3];

        Archetype arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0-SNAPSHOT"); //NOI18N
        arch.setRepository("http://snapshots.repository.codehaus.org"); //NOI18N
        arch.setArtifactId("webapp-jee5"); //NOI18N
        WEB_APP_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0-SNAPSHOT"); //NOI18N
        arch.setRepository("http://snapshots.repository.codehaus.org"); //NOI18N
        arch.setArtifactId("webapp-j2ee14"); //NOI18N
        WEB_APP_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0-SNAPSHOT"); //NOI18N
        arch.setRepository("http://snapshots.repository.codehaus.org"); //NOI18N
        arch.setArtifactId("webapp-j2ee13"); //NOI18N
        WEB_APP_ARCHS[2] = arch;

        EJB_ARCHS = new Archetype[3];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0-SNAPSHOT"); //NOI18N
        arch.setRepository("http://snapshots.repository.codehaus.org"); //NOI18N
        arch.setArtifactId("ejb-jee5"); //NOI18N
        EJB_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0-SNAPSHOT"); //NOI18N
        arch.setRepository("http://snapshots.repository.codehaus.org"); //NOI18N
        arch.setArtifactId("ejb-j2ee14"); //NOI18N
        EJB_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0-SNAPSHOT"); //NOI18N
        arch.setRepository("http://snapshots.repository.codehaus.org"); //NOI18N
        arch.setArtifactId("ejb-j2ee13"); //NOI18N
        EJB_ARCHS[2] = arch;

        EAR_ARCHS = new Archetype[1];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0-SNAPSHOT"); //NOI18N
        arch.setRepository("file:///home/dafe/.m2/repository"); //NOI18N
        arch.setArtifactId("ear-jee5"); //NOI18N
        EAR_ARCHS[0] = arch;

        EA_ARCH = new Archetype();
        EA_ARCH.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        EA_ARCH.setVersion("1.0-SNAPSHOT"); //NOI18N
        EA_ARCH.setRepository("file:///home/dafe/.m2/repository"); //NOI18N
        EA_ARCH.setArtifactId("ea-root"); //NOI18N
    }


    private static void runArchetype(File directory, NBVersionInfo vi, Archetype arch, Map<String, String> additional) throws IOException {
        Properties props = new Properties();

        props.setProperty("artifactId", vi.getArtifactId()); //NOI18N
        props.setProperty("version", vi.getVersion()); //NOI18N
        props.setProperty("groupId", vi.getGroupId()); //NOI18N
        final String pack = vi.getPackaging();
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
        //TODO externalize somehow to allow advanced users to change the value..
        config.setGoals(Collections.singletonList(MavenCommandSettings.getDefault().getCommand(MavenCommandSettings.COMMAND_CREATE_ARCHETYPENG))); //NOI18N
        if (arch.getRepository() != null) {
            props.setProperty("archetype.repository", arch.getRepository()); //NOI18N
            props.setProperty("archetypeRepository", arch.getRepository()); //NOI18N
        }

        //ExecutionRequest.setInteractive seems to have no influence on archetype plugin.
        config.setInteractive(false);
        props.setProperty("archetype.interactive", "false");//NOI18N
        config.setProperties(props);

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
            Exceptions.printStackTrace(ex);
        } catch (XmlPullParserException ex) {
            Exceptions.printStackTrace(ex);
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
        NBVersionInfo vi = new NBVersionInfo(
                null,
                (String)wiz.getProperty("groupId"), //NOI18N
                (String)wiz.getProperty("artifactId"), //NOI18N
                (String)wiz.getProperty("version"),
                null,
                (String)wiz.getProperty("package"),
                null, null, null);
        @SuppressWarnings("unchecked")
        Map<String, String> additional = (Map<String, String>)wiz.getProperty("additionalProps"); //NOI18N

        try {
            NBVersionInfo ear_vi = (NBVersionInfo)wiz.getProperty("ear_versionInfo");
            if (ear_vi != null) {
                // enterprise application wizard, multiple archetypes to run
                Set<FileObject> resultSet = new HashSet<FileObject>();
                NBVersionInfo web_vi = (NBVersionInfo)wiz.getProperty("web_versionInfo");
                NBVersionInfo ejb_vi = (NBVersionInfo)wiz.getProperty("ejb_versionInfo");

                handle.start(8 + (web_vi != null ? 4 : 0) + (ejb_vi != null ? 4 : 0));
                resultSet.addAll(instantiate(handle, (File)wiz.getProperty("projdir"), vi,
                        (Archetype)wiz.getProperty("archetype"), additional, 0));
                resultSet.addAll(instantiate(handle, (File)wiz.getProperty("ear_projdir"), ear_vi,
                        (Archetype)wiz.getProperty("ear_archetype"), null, 4));
                int progressCounter = 8;
                if (web_vi != null) {
                    resultSet.addAll(instantiate(handle, (File)wiz.getProperty("web_projdir"), web_vi,
                            (Archetype)wiz.getProperty("web_archetype"), null, progressCounter));
                    progressCounter += 4;
                }
                if (ejb_vi != null) {
                    resultSet.addAll(instantiate(handle, (File)wiz.getProperty("ejb_projdir"), ejb_vi,
                            (Archetype)wiz.getProperty("ejb_archetype"), null, progressCounter));
                }
                return resultSet;
            } else {
                // other wizards, just one archetype
                handle.start(4);
                return instantiate(handle, (File)wiz.getProperty("projdir"), vi,
                        (Archetype)wiz.getProperty("archetype"), additional, 0);
            }
        } finally {
            handle.finish();
        }
    }

    private static Set<FileObject> instantiate (ProgressHandle handle, File projDir, NBVersionInfo vi,
        Archetype arch, Map<String, String> additional, int progressCounter) throws IOException {
        handle.progress(++progressCounter);
        final File dirF = FileUtil.normalizeFile(projDir); //NOI18N
        final File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
//            final Archetype archetype = (Archetype)wiz.getProperty("archetype"); //NOI18N<
        dirF.getParentFile().mkdirs();

        handle.progress(NbBundle.getMessage(MavenWizardIterator.class, "PRG_Processing_Archetype"), ++progressCounter);
        runArchetype(dirF.getParentFile(), vi, arch, additional);
//            } else {
//                final String art = (String)wiz.getProperty("artifactId"); //NOI18N
//                final String ver = (String)wiz.getProperty("version"); //NOI18N
//                final String gr = (String)wiz.getProperty("groupId"); //NOI18N
//                final String pack = (String)wiz.getProperty("package"); //NOI18N
//                runArchetype(dirF.getParentFile(), gr, art, ver, pack, archetype);
//            }
        handle.progress(++progressCounter);
        // Always open top dir as a project:
        FileObject fDir = FileUtil.toFileObject(dirF);
        if (fDir != null) {
            // the archetype generation didn't fail.
            resultSet.add(fDir);
            addJavaRootFolders(fDir);
            // Look for nested projects to open as well:
            Enumeration e = fDir.getFolders(true);
            while (e.hasMoreElements()) {
                FileObject subfolder = (FileObject) e.nextElement();
                if (ProjectManager.getDefault().isProject(subfolder)) {
                    resultSet.add(subfolder);
                    addJavaRootFolders(subfolder);
                }
            }
            Project prj = ProjectManager.getDefault().findProject(fDir);
            if (prj != null) {
                NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
                if (nbprj != null) { //#147006 how can this happen?
                    // maybe when the archetype contains netbeans specific project files?
                    prj.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
                }
            }
        }
        handle.progress(++progressCounter);
        return resultSet;
    }

    private static void addJavaRootFolders(FileObject fo) {
        try {
            Project prj = ProjectManager.getDefault().findProject(fo);
            if (prj == null) { //#143596
                return;
            }
            NbMavenProject watch = prj.getLookup().lookup(NbMavenProject.class);
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
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
