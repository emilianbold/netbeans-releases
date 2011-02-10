/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.junit.libdef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager.TYPE;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport.LibraryDefiner;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.junit.libdef.Bundle.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Offers to download JUnit 3.x or 4.x and create NBMs for libs.junit4 and junitlib out of it.
 */
@ServiceProvider(service=LibraryDefiner.class)
public class JUnitLibraryDefiner implements LibraryDefiner {

    @Messages({
        "MSG_Downloading_all=Downloading JUnit",
        "MSG_Downloading_binary=Downloading JUnit {0} binary",
        "MSG_Downloading_javadoc=Downloading JUnit {0} Javadoc",
        "MSG_Downloading_sources=Downloading JUnit {0} sources",
        "LBL_separate_download=Separate download"
    })
    public @Override Callable<Library> missingLibrary(final String name) {
        if (!name.matches("junit(_4)?")) {
            return null;
        }
        return new Callable<Library>() {
            Map<String,File> downloads;
            int count;
            ProgressHandle handle;
            MavenEmbedder online;
            List<ArtifactRepository> remoteArtifactRepositories;
            @SuppressWarnings("SleepWhileInLoop")
            public @Override Library call() throws Exception {
                downloads = new HashMap<String,File>();
                count = 0;
                handle = ProgressHandleFactory.createHandle(MSG_Downloading_all()/*, XXX cancelable; see comment in MavenEmbedder.resolve */);
                handle.start(6);
                try {
                    online = EmbedderFactory.getOnlineEmbedder();
                    remoteArtifactRepositories = Collections.<ArtifactRepository>singletonList(new MavenArtifactRepository("central", "http://repo1.maven.org/maven2/", new DefaultRepositoryLayout(), new ArtifactRepositoryPolicy(), new ArtifactRepositoryPolicy()));
                    for (String version : new String[] {"3.8.2", "4.8.2"}) {
                        Artifact a = online.createArtifact("junit", "junit", version, NbMavenProject.TYPE_JAR);
                        download(a, MSG_Downloading_binary(version), "junit-" + version + ".jar");
                        download(online.createArtifactWithClassifier(a.getGroupId(), a.getArtifactId(), a.getVersion(), a.getType(), "javadoc"), MSG_Downloading_javadoc(version), "junit-" + version + "-javadoc.jar");
                        download(online.createArtifactWithClassifier(a.getGroupId(), a.getArtifactId(), a.getVersion(), a.getType(), "sources"), MSG_Downloading_sources(version), "junit-" + version + "-sources.jar");
                    }
                } finally {
                    handle.finish();
                    handle = null;
                    online = null;
                    remoteArtifactRepositories = null;
                }
                File junit4Module = new JARBuilder(File.createTempFile("o-n-l-ju4", ".jar")).
                        entry("org/netbeans/libs/junit4/Bundle.properties", "OpenIDE-Module-Name=JUnit 4\n").
                        header("AutoUpdate-Show-In-Client", "false").
                        header("OpenIDE-Module", "org.netbeans.libs.junit4").
                        header("OpenIDE-Module-Specification-Version", "1.13").
                        header("OpenIDE-Module-Public-Packages", "junit.**, org.junit.**, org.hamcrest.**").
                        header("OpenIDE-Module-Localizing-Bundle", "org/netbeans/libs/junit4/Bundle.properties").
                        header("OpenIDE-Module-Requires", "org.openide.modules.ModuleFormat1").
                        header("Class-Path", "ext/junit-4.8.2.jar").
                        write();
                junit4Module.deleteOnExit();
                File junit4Package = new JARBuilder(File.createTempFile("o-n-l-ju4", ".nbm")).
                        entry("Info/info.xml", JUnitLibraryDefiner.class.getResource("org-netbeans-libs-junit4-info.xml")).
                        entry("netbeans/config/Modules/org-netbeans-libs-junit4.xml", JUnitLibraryDefiner.class.getResource("org-netbeans-libs-junit4.xml")).
                        entry("netbeans/modules/org-netbeans-libs-junit4.jar", junit4Module).
                        entry("netbeans/modules/ext/junit-4.8.2.jar", downloads.get("junit-4.8.2.jar")).
                        write();
                junit4Package.deleteOnExit();
                File junitlibModule = new JARBuilder(File.createTempFile("o-n-m-jul", ".jar")).
                        entry("org/netbeans/modules/junitlib/Bundle.properties",
                            "OpenIDE-Module-Name=JUnit\n"
                          + "OpenIDE-Module-Display-Category=Java SE\n"
                          + "OpenIDE-Module-Short-Description=Bundles the JUnit test library.\n"
                          + "OpenIDE-Module-Long-Description=Includes JUnit 3.x and 4.x together with library definitions.\n"
                          + "junit=JUnit 3.8.2\n"
                          + "junit_4=JUnit 4.8.2\n").
                        header("AutoUpdate-Show-In-Client", "true").
                        header("OpenIDE-Module", "org.netbeans.modules.junitlib").
                        header("OpenIDE-Module-Specification-Version", "1.0").
                        header("OpenIDE-Module-Module-Dependencies", "org.netbeans.libs.junit4 > 1.13").
                        header("OpenIDE-Module-Localizing-Bundle", "org/netbeans/modules/junitlib/Bundle.properties").
                        header("OpenIDE-Module-Layer", "org/netbeans/modules/junitlib/layer.xml").
                        header("OpenIDE-Module-Requires", "org.openide.modules.ModuleFormat1").
                        header("OpenIDE-Module-Public-Packages", "-").
                        entry("org/netbeans/modules/junitlib/layer.xml", JUnitLibraryDefiner.class.getResource("layer.xml")).
                        entry("org/netbeans/modules/junitlib/junit-3.8.2.xml", JUnitLibraryDefiner.class.getResource("junit-3.8.2.xml")).
                        entry("org/netbeans/modules/junitlib/junit-4.8.2.xml", JUnitLibraryDefiner.class.getResource("junit-4.8.2.xml")).
                        write();
                junitlibModule.deleteOnExit();
                File junitlibPackage = new JARBuilder(File.createTempFile("o-n-m-jul", ".nbm")).
                        entry("Info/info.xml", JUnitLibraryDefiner.class.getResource("org-netbeans-modules-junitlib-info.xml")).
                        entry("netbeans/config/Modules/org-netbeans-modules-junitlib.xml", JUnitLibraryDefiner.class.getResource("org-netbeans-modules-junitlib.xml")).
                        entry("netbeans/modules/org-netbeans-modules-junitlib.jar", junitlibModule).
                        entry("netbeans/modules/ext/junit-3.8.2.jar", downloads.get("junit-3.8.2.jar")).
                        entry("netbeans/docs/junit-3.8.2-api.zip", downloads.get("junit-3.8.2-javadoc.jar")).
                        entry("netbeans/docs/junit-3.8.2-src.jar", downloads.get("junit-3.8.2-sources.jar")).
                        entry("netbeans/docs/junit-4.8.2-api.zip", downloads.get("junit-4.8.2-javadoc.jar")).
                        entry("netbeans/docs/junit-4.8.2-src.jar", downloads.get("junit-4.8.2-sources.jar")).
                        write();
                junitlibPackage.deleteOnExit();
                OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
                UpdateUnitProvider uup = UpdateUnitProviderFactory.getDefault().create(LBL_separate_download(), junit4Package, junitlibPackage);
                try {
                    for (UpdateUnit uu : uup.getUpdateUnits(TYPE.MODULE)) {
                        for (UpdateElement ue : uu.getAvailableUpdates()) {
                            oc.add(ue);
                        }
                    }
                    if (!PluginManager.openInstallWizard(oc)) {
                        throw new Exception("user canceled update");
                    }
                } finally {
                    UpdateUnitProviderFactory.getDefault().remove(uup);
                }
                // XXX new library & build.properties apparently do not show up immediately... how to listen properly?
                for (int i = 0; i < 10; i++) {
                    Library lib = LibraryManager.getDefault().getLibrary(name);
                    if (lib != null) {
                        return lib;
                    }
                    Thread.sleep(1000);
                }
                throw new Exception("failed to install properly");
            }
            private void download(Artifact a, String message, String key) throws Exception {
                handle.progress(message, ++count);
                online.resolve(a, remoteArtifactRepositories, online.getLocalRepository());
                File file = a.getFile();
                if (!file.exists()) {
                    throw new IOException("failed to download " + key);
                }
                downloads.put(key, file);
            }
        };
    }

    private static class JARBuilder { // XXX would be useful to move to a utility module
        private final File jar;
        private final Map<String,String> textEntries = new LinkedHashMap<String,String>();
        private final Map<String,URL> otherEntries = new LinkedHashMap<String,URL>();
        private final Manifest manifest = new Manifest();
        public JARBuilder(File jar) {
            this.jar = jar;
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        }
        public JARBuilder entry(String name, String text) {
            textEntries.put(name, text);
            return this;
        }
        public JARBuilder entry(String name, URL content) {
            otherEntries.put(name, content);
            return this;
        }
        public JARBuilder entry(String name, File content) {
            try {
                return entry(name, content.toURI().toURL());
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            }
        }
        public JARBuilder header(String key, String value) {
            manifest.getMainAttributes().putValue(key, value);
            return this;
        }
        public File write() throws IOException {
            OutputStream os = new FileOutputStream(jar);
            try {
                JarOutputStream jos = new JarOutputStream(os, manifest);
                Set<String> parents = new HashSet<String>();
                for (Map.Entry<String,String> entry : textEntries.entrySet()) {
                    writeEntry(entry.getKey(), parents, jos, entry.getValue().getBytes("UTF-8"));
                }
                for (Map.Entry<String,URL> entry : otherEntries.entrySet()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream is = entry.getValue().openStream();
                    try {
                        FileUtil.copy(is, baos);
                    } finally {
                        is.close();
                    }
                    writeEntry(entry.getKey(), parents, jos, baos.toByteArray());
                }
                jos.finish();
                jos.close();
            } finally {
                os.close();
            }
            return jar;
        }
        private void writeEntry(String name, Set<String> parents, JarOutputStream jos, byte[] data) throws IOException {
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) == '/') {
                    String parent = name.substring(0, i + 1);
                    if (parents.add(parent)) {
                        JarEntry je = new JarEntry(parent);
                        je.setMethod(ZipEntry.STORED);
                        je.setSize(0);
                        je.setCrc(0);
                        jos.putNextEntry(je);
                        jos.closeEntry();
                    }
                }
            }
            JarEntry je = new JarEntry(name);
            je.setMethod(ZipEntry.STORED);
            je.setSize(data.length);
            CRC32 crc = new CRC32();
            crc.update(data);
            je.setCrc(crc.getValue());
            jos.putNextEntry(je);
            jos.write(data, 0, data.length);
            jos.closeEntry();
        }
    }

}
