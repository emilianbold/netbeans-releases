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

    public static void runArchetype(File directory, WizardDescriptor wiz) throws IOException {
        Properties props = new Properties();
        props.setProperty("artifactId", (String)wiz.getProperty("artifactId")); //NOI18N
        props.setProperty("version", (String)wiz.getProperty("version")); //NOI18N
        props.setProperty("groupId", (String)wiz.getProperty("groupId")); //NOI18N
        final String pack = (String)wiz.getProperty("package"); //NOI18N
        if (pack != null && pack.trim().length() > 0) {
            props.setProperty("package", pack); //NOI18N
        }
        final Archetype arch = (Archetype)wiz.getProperty("archetype"); //NOI18N
        props.setProperty("archetypeArtifactId", arch.getArtifactId()); //NOI18N
        props.setProperty("archetypeGroupId", arch.getGroupId()); //NOI18N
        props.setProperty("archetypeVersion", arch.getVersion()); //NOI18N
        props.setProperty("basedir", directory.getAbsolutePath());//NOI18N
        
        @SuppressWarnings("unchecked")
        HashMap<String, String> additional = (HashMap<String, String>)wiz.getProperty("additionalProps");//NOI18N
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
        try {
            handle.start(4);
            handle.progress(1);
            final File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); //NOI18N
            final File parent = dirF.getParentFile();
            if (parent != null && parent.exists()) {
                ProjectChooser.setProjectsFolder(parent);
            }

            Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
//            final Archetype archetype = (Archetype)wiz.getProperty("archetype"); //NOI18N<
            dirF.getParentFile().mkdirs();

            handle.progress(NbBundle.getMessage(MavenWizardIterator.class, "PRG_Processing_Archetype"), 2);
            runArchetype(dirF.getParentFile(), wiz);
//            } else {
//                final String art = (String)wiz.getProperty("artifactId"); //NOI18N
//                final String ver = (String)wiz.getProperty("version"); //NOI18N
//                final String gr = (String)wiz.getProperty("groupId"); //NOI18N
//                final String pack = (String)wiz.getProperty("package"); //NOI18N
//                runArchetype(dirF.getParentFile(), gr, art, ver, pack, archetype);
//            }
            handle.progress(3);
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
            return resultSet;
        } finally {
            handle.finish();
        }
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
