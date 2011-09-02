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
package org.netbeans.modules.maven;

import java.util.prefs.BackingStoreException;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Repository;
import org.netbeans.modules.maven.classpath.ClassPathProviderImpl;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.cos.CopyResourcesOnSave;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.problems.BatchProblemNotifier;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * openhook implementation, register global classpath and also
 * register the project in the fileOwnerQuery impl, that's important for interproject
 * dependencies to work.
 * @author  Milos Kleint
 */
@SuppressWarnings("ClassWithMultipleLoggers")
class ProjectOpenedHookImpl extends ProjectOpenedHook {
    private static final String PROP_BINARIES_CHECKED = "binariesChecked";
    private static final String PROP_JAVADOC_CHECKED = "javadocChecked";
    private static final String PROP_SOURCE_CHECKED = "sourceChecked";
   
    private final NbMavenProjectImpl project;
    private List<URI> uriReferences = new ArrayList<URI>();

    // ui logging
    static final String UI_LOGGER_NAME = "org.netbeans.ui.maven.project"; //NOI18N
    static final Logger UI_LOGGER = Logger.getLogger(UI_LOGGER_NAME);

    static final String USG_LOGGER_NAME = "org.netbeans.ui.metrics.maven"; //NOI18N
    static final Logger USG_LOGGER = Logger.getLogger(USG_LOGGER_NAME);

    private static final Logger LOGGER = Logger.getLogger(ProjectOpenedHookImpl.class.getName());
    private static final AtomicBoolean checkedIndices = new AtomicBoolean();
    
    ProjectOpenedHookImpl(NbMavenProjectImpl proj) {
        project = proj;
    }

    @Messages("UI_MAVEN_PROJECT_OPENED=A Maven project was opened. Appending the project's packaging type.")
    protected @Override void projectOpened() {
        checkBinaryDownloads();
        checkSourceDownloads();
        checkJavadocDownloads();
        project.attachUpdater();
        registerWithSubmodules(FileUtil.toFile(project.getProjectDirectory()), new HashSet<File>());
        Set<URI> uris = new HashSet<URI>();
        uris.addAll(Arrays.asList(project.getSourceRoots(false)));
        uris.addAll(Arrays.asList(project.getSourceRoots(true)));
        //#167572 in the unlikely event that generated sources are located outside of
        // the project root.
        uris.addAll(Arrays.asList(project.getGeneratedSourceRoots(false)));
        uris.addAll(Arrays.asList(project.getGeneratedSourceRoots(true)));
        URI rootUri = FileUtil.toFile(project.getProjectDirectory()).toURI();
        File rootDir = new File(rootUri);
        for (URI uri : uris) {
            if (FileUtilities.getRelativePath(rootDir, new File(uri)) == null) {
                FileOwnerQuery.markExternalOwner(uri, project, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                //TODO we do not handle properly the case when someone changes a
                // ../../src path to ../../src2 path in the lifetime of the project.
                uriReferences.add(uri);
            }
        }
        
        // register project's classpaths to GlobalPathRegistry
        ClassPathProviderImpl cpProvider = project.getLookup().lookup(org.netbeans.modules.maven.classpath.ClassPathProviderImpl.class);
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        GlobalPathRegistry.getDefault().register(ClassPath.EXECUTE, cpProvider.getProjectClassPaths(ClassPath.EXECUTE));
        BatchProblemNotifier.opened(project);
        
        //UI logging.. log what was the packaging type for the opened project..
        LogRecord record = new LogRecord(Level.INFO, "UI_MAVEN_PROJECT_OPENED"); //NOI18N
        record.setLoggerName(UI_LOGGER_NAME); //NOI18N
        record.setParameters(new Object[] {project.getProjectWatcher().getPackagingType()});
        record.setResourceBundle(NbBundle.getBundle(ProjectOpenedHookImpl.class));
        UI_LOGGER.log(record);

        //USG logging.. log what was the packaging type for the opened project..
        record = new LogRecord(Level.INFO, "USG_PROJECT_OPEN_MAVEN"); //NOI18N
        record.setLoggerName(USG_LOGGER_NAME); //NOI18N
        record.setParameters(new Object[] {project.getProjectWatcher().getPackagingType()});
        USG_LOGGER.log(record);

        MavenProject mp = project.getOriginalMavenProject();
        for (ArtifactRepository repo : mp.getRemoteArtifactRepositories()) {
            register(repo, mp.getModel().getRepositories());
        }
        for (ArtifactRepository repo : mp.getPluginArtifactRepositories()) {
            register(repo, mp./* MNG-5163 */getModel().getPluginRepositories());
        }

        CopyResourcesOnSave.opened();

        //only check for the updates of index, if the indexing was already used.
        if (checkedIndices.compareAndSet(false, true) && existsDefaultIndexLocation()) {
            final int freq = RepositoryPreferences.getInstance().getIndexUpdateFrequency();
            new RequestProcessor("Maven Repo Index Transfer/Scan").post(new Runnable() { // #138102
                public @Override void run() {
                    List<RepositoryInfo> ris = RepositoryPreferences.getInstance().getRepositoryInfos();
                    for (final RepositoryInfo ri : ris) {
                        //check this repo can be indexed
                        if (!ri.isRemoteDownloadable() && !ri.isLocal()) {
                            continue;
                        }
                        if (freq != RepositoryPreferences.FREQ_NEVER) {
                            boolean run = false;
                            if (freq == RepositoryPreferences.FREQ_STARTUP) {
                                LOGGER.log(Level.FINER, "Index At Startup :{0}", ri.getId());//NOI18N
                                run = true;
                            } else if (freq == RepositoryPreferences.FREQ_ONCE_DAY && checkDiff(ri.getId(), 86400000L)) {
                                LOGGER.log(Level.FINER, "Index Once a Day :{0}", ri.getId());//NOI18N
                                run = true;
                            } else if (freq == RepositoryPreferences.FREQ_ONCE_WEEK && checkDiff(ri.getId(), 604800000L)) {
                                LOGGER.log(Level.FINER, "Index once a Week :{0}", ri.getId());//NOI18N
                                run = true;
                            }
                            if (run && ri.isRemoteDownloadable()) {
                                RepositoryIndexer.indexRepo(ri);
                            }
                        }
                    }
                }
            }, 1000 * 60 * 2);
        }
    }
    private void register(ArtifactRepository repo, List<Repository> definitions) {
        String id = repo.getId();
        String displayName = id;
        for (Repository r : definitions) {
            if (id.equals(r.getId())) {
                String n = r.getName();
                if (n != null) {
                    displayName = n;
                    break;
                }
            }
        }
        List<ArtifactRepository> mirrors = repo.getMirroredRepositories();
        try {
            RepositoryPreferences.getInstance().addTransientRepository(this, id, displayName, mirrors.size() == 1 ? mirrors.get(0).getUrl() : repo.getUrl());
        } catch (URISyntaxException x) {
            LOGGER.log(Level.WARNING, "Ignoring repo with malformed URL: {0}", x.getMessage());
        }
    }
    private boolean existsDefaultIndexLocation() {
        File cacheDir = new File(Places.getCacheDirectory(), "mavenindex");//NOI18N
        return cacheDir.exists() && cacheDir.isDirectory();
    }
    private boolean checkDiff(String repoid, long amount) {
        Date date = RepositoryPreferences.getInstance().getLastIndexUpdate(repoid);
        Date now = new Date();
        LOGGER.log(Level.FINER, "Check Date Diff :{0}", repoid);//NOI18N
        LOGGER.log(Level.FINER, "Last Indexed Date :{0}", SimpleDateFormat.getInstance().format(date));//NOI18N
        LOGGER.log(Level.FINER, "Now :{0}", SimpleDateFormat.getInstance().format(now));//NOI18N
        long diff = now.getTime() - date.getTime();
        LOGGER.log(Level.FINER, "Diff :{0}", diff);//NOI18N
        return (diff < 0 || diff > amount);
    }

    protected @Override void projectClosed() {
        uriReferences.clear();
        project.detachUpdater();
        // unregister project's classpaths to GlobalPathRegistry
        ClassPathProviderImpl cpProvider = project.getLookup().lookup(org.netbeans.modules.maven.classpath.ClassPathProviderImpl.class);
        GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
        GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        GlobalPathRegistry.getDefault().unregister(ClassPath.EXECUTE, cpProvider.getProjectClassPaths(ClassPath.EXECUTE));
        BatchProblemNotifier.closed(project);
        CopyResourcesOnSave.closed();
        RepositoryPreferences.getInstance().removeTransientRepositories(this);
    }
   
   private void checkBinaryDownloads() {
       MavenSettings.DownloadStrategy ds = MavenSettings.getDefault().getBinaryDownloadStrategy();
       if (ds.equals(MavenSettings.DownloadStrategy.NEVER)) {
           return;
       }

       NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
       Preferences prefs = ProjectUtils.getPreferences(project, NbMavenProject.class, false);
       if (ds.equals(MavenSettings.DownloadStrategy.EVERY_OPEN)) {
            watcher.synchronousDependencyDownload();
            prefs.putBoolean(PROP_BINARIES_CHECKED, true);
            try {
                prefs.sync();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
       } else if (ds.equals(MavenSettings.DownloadStrategy.FIRST_OPEN)) {
           boolean alreadyChecked = prefs.getBoolean(PROP_BINARIES_CHECKED, false);
           if (!alreadyChecked) {
                watcher.synchronousDependencyDownload();
                prefs.putBoolean(PROP_BINARIES_CHECKED, true);
                try {
                    prefs.sync();
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
           }
       }
   }

   private void checkJavadocDownloads() {
       MavenSettings.DownloadStrategy ds = MavenSettings.getDefault().getJavadocDownloadStrategy();
       if (ds.equals(MavenSettings.DownloadStrategy.NEVER)) {
           return;
       }

       NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
       Preferences prefs = ProjectUtils.getPreferences(project, NbMavenProject.class, false);
       if (ds.equals(MavenSettings.DownloadStrategy.EVERY_OPEN)) {
            watcher.triggerSourceJavadocDownload(true);
            prefs.putBoolean(PROP_JAVADOC_CHECKED, true);
            try {
                prefs.sync();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
       } else if (ds.equals(MavenSettings.DownloadStrategy.FIRST_OPEN)) {
           boolean alreadyChecked = prefs.getBoolean(PROP_JAVADOC_CHECKED, false);
           if (!alreadyChecked) {
                watcher.triggerSourceJavadocDownload(true);
                prefs.putBoolean(PROP_JAVADOC_CHECKED, true);
                try {
                    prefs.sync();
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
           }
       }
   }

   private void checkSourceDownloads() {
       MavenSettings.DownloadStrategy ds = MavenSettings.getDefault().getSourceDownloadStrategy();
       if (ds.equals(MavenSettings.DownloadStrategy.NEVER)) {
           return;
       }

       NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
       Preferences prefs = ProjectUtils.getPreferences(project, NbMavenProject.class, false);
       if (ds.equals(MavenSettings.DownloadStrategy.EVERY_OPEN)) {
            watcher.triggerSourceJavadocDownload(false);
            prefs.putBoolean(PROP_SOURCE_CHECKED, true);
            try {
                prefs.sync();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
       } else if (ds.equals(MavenSettings.DownloadStrategy.FIRST_OPEN)) {
           boolean alreadyChecked = prefs.getBoolean(PROP_SOURCE_CHECKED, false);
           if (!alreadyChecked) {
                watcher.triggerSourceJavadocDownload(false);
                prefs.putBoolean(PROP_SOURCE_CHECKED, true);
                try {
                    prefs.sync();
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
           }
       }
   }

    /** Similar to {@link SubprojectProviderImpl#addProjectModules} but more efficient for large numbers of modules. */
    private static void registerWithSubmodules(File basedir, Set<File> registered) { // #200445
        if (!registered.add(basedir)) {
            return;
        }
        File pom = new File(basedir, "pom.xml");
        if (!pom.isFile()) {
            return;
        }
        Element project;
        try {
            project = XMLUtil.parse(new InputSource(pom.toURI().toString()), false, false, XMLUtil.defaultErrorHandler(), null).getDocumentElement();
        } catch (Exception x) {
            LOGGER.log(Level.FINE, "could not parse " + pom, x);
            return;
        }
        Element parent = XMLUtil.findElement(project, "parent", null);
        Element groupIdE = XMLUtil.findElement(project, "groupId", null);
        if (groupIdE == null) {
            groupIdE = XMLUtil.findElement(parent, "groupId", null);
            if (groupIdE == null) {
                LOGGER.log(Level.WARNING, "no groupId in {0}", pom);
                return;
            }
        }
        String groupId = XMLUtil.findText(groupIdE);
        Element artifactIdE = XMLUtil.findElement(project, "artifactId", null);
        if (artifactIdE == null) {
            artifactIdE = XMLUtil.findElement(parent, "artifactId", null);
            if (artifactIdE == null) {
                LOGGER.log(Level.WARNING, "no artifactId in {0}", pom);
                return;
            }
        }
        String artifactId = XMLUtil.findText(artifactIdE);
        if (groupId.contains("${") || artifactId.contains("${")) {
            LOGGER.log(Level.FINE, "Unevaluated groupId/artifactId in {0}", basedir);
            FileObject basedirFO = FileUtil.toFileObject(basedir);
            if (basedirFO != null) {
                try {
                    Project p = ProjectManager.getDefault().findProject(basedirFO);
                    if (p != null) {
                        NbMavenProjectImpl nbmp = p.getLookup().lookup(NbMavenProjectImpl.class);
                        if (nbmp != null) {
                            MavenFileOwnerQueryImpl.getInstance().registerProject(nbmp);
                        } else {
                            LOGGER.log(Level.FINE, "not a Maven project in {0}", basedir);
                        }
                    } else {
                        LOGGER.log(Level.FINE, "no project in {0}", basedir);
                    }
                } catch (IOException x) {
                    LOGGER.log(Level.FINE, null, x);
                }
            } else {
                LOGGER.log(Level.FINE, "no FileObject for {0}", basedir);
            }
        } else {
            try {
                MavenFileOwnerQueryImpl.getInstance().registerCoordinates(groupId, artifactId, basedir.toURI().toURL());
            } catch (MalformedURLException x) {
                LOGGER.log(Level.FINE, null, x);
            }
        }
        scanForSubmodulesIn(project, basedir, registered);
        Element profiles = XMLUtil.findElement(project, "profiles", null);
        if (profiles != null) {
            for (Element profile : XMLUtil.findSubElements(profiles)) {
                if (profile.getTagName().equals("profile")) {
                    scanForSubmodulesIn(profile, basedir, registered);
                }
            }
        }
    }
    private static void scanForSubmodulesIn(Element projectOrProfile, File basedir, Set<File> registered) throws IllegalArgumentException {
        Element modules = XMLUtil.findElement(projectOrProfile, "modules", null);
        if (modules != null) {
            for (Element module : XMLUtil.findSubElements(modules)) {
                if (module.getTagName().equals("module")) {
                    registerWithSubmodules(FileUtilities.resolveFilePath(basedir, XMLUtil.findText(module)), registered);
                }
            }
        }
    }

}
