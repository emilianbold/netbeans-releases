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

package org.netbeans.modules.maven.embedder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;
import hidden.org.codehaus.plexus.util.IOUtil;

/**
 *
 * @author mkleint
 */
public class NbArtifact implements Artifact {
    
    private Artifact original;
    private static Map<String, File> cache = new HashMap<String, File>();
    private boolean fakesSystem = false;
    private boolean fakePom = false;
    private File originalSystemFile;
    
    public static synchronized File getCachedPom(String id) {
        return cache.get(id);
    }
    
    public static synchronized void putCachedPom(String id, File fil) {
        cache.put(id, fil);
    }
    
    /** Creates a new instance of NbArtifact */
    public NbArtifact(Artifact orig) {
        original = orig;
    }
    
    public String getGroupId() {
        return original.getGroupId();
    }
    
    public String getArtifactId() {
        return original.getArtifactId();
    }
    
    public String getVersion() {
        return original.getVersion();
    }
    
    public void setVersion(String version) {
        original.setVersion(version);
    }
    
    public String getScope() {
        return original.getScope();
    }
    
    public String getType() {
        return original.getType();
    }
    
    public String getClassifier() {
        return original.getClassifier();
    }
    
    public boolean hasClassifier() {
        return original.hasClassifier();
    }
    
    public File getFile() {
        /** #163919 **/
        if ("pom".equals(getType()) && isResolved()) {
            if (original.getFile() != null && !original.getFile().exists()) {
                File orig = NbArtifact.getCachedPom(getId());
                originalSystemFile = original.getFile();
                if (orig != null) {
                    original.setFile(orig);
                } else {
                    PrintWriter writer = null;
                    try {
                        File temp = File.createTempFile("mevenide", "pom");
                        temp.deleteOnExit();
                        writer = new PrintWriter(new FileOutputStream(temp));
                        writer.println("<project>");
                        writer.println("<modelVersion>4.0.0</modelVersion>");
                        writer.println("<packaging>pom</packaging>");
                        writer.println("<groupId>" + getGroupId() + "</groupId>");
                        writer.println("<artifactId>" + getArtifactId() + "</artifactId>");
                        writer.println("<version>" + getVersion() + "</version>");
                        writer.println("</project>");
                        writer.close();
                        original.setFile(temp);
                        NbArtifact.putCachedPom(getId(), temp);
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException io) {
                        io.printStackTrace();
                    } finally {
                        IOUtil.close(writer);
                    }
                }
                fakePom = true;
            }
        }
        if (Artifact.SCOPE_SYSTEM.equals(getScope())) {
            File systemFile = original.getFile();
            if (systemFile == null || !systemFile.exists()) {
                File tempSystemFile = null;
                JarOutputStream out = null;
                try {
                    tempSystemFile = File.createTempFile("mvn-system-dep", ".jar");
//                    tempSystemFile.deleteOnExit();
                    out = new JarOutputStream(new FileOutputStream(tempSystemFile), new Manifest());
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    IOUtil.close(out);
                }
                if (tempSystemFile != null) {
                    originalSystemFile = systemFile;
                    setFile(tempSystemFile);
                    fakesSystem = true;
                } else {
                    // oh well..
                }
            }
        }
        return original.getFile();
    }
    
    /** before calling this method, call getFile()
     */
    public boolean isFakedSystemDependency() {
        return fakesSystem;
    }
    /** before calling this method, call getFile()
     */
    public boolean isFakedPomDependency() {
        return fakePom;
    }
    
    public File getNonFakedFile() {
        return originalSystemFile;
    }
    
    public void setFile(File destination) {
        original.setFile(destination);
    }
    
    public String getBaseVersion() {
        return original.getBaseVersion();
    }
    
    public void setBaseVersion(String baseVersion) {
        original.setBaseVersion(baseVersion);
    }
    
    public String getId() {
        return original.getId();
    }
    
    public String getDependencyConflictId() {
        return original.getDependencyConflictId();
    }
    
    public void addMetadata(ArtifactMetadata metadata) {
        original.addMetadata(metadata);
    }
    
    public Collection getMetadataList() {
        return original.getMetadataList();
    }
    
    public void setRepository(ArtifactRepository remoteRepository) {
        original.setRepository(remoteRepository);
    }
    
    public ArtifactRepository getRepository() {
        return original.getRepository();
    }
    
    public void updateVersion(String version, ArtifactRepository localRepository) {
        original.updateVersion(version, localRepository);
    }
    
    public String getDownloadUrl() {
        return original.getDownloadUrl();
    }
    
    public void setDownloadUrl(String downloadUrl) {
        original.setDownloadUrl(downloadUrl);
    }
    
    public ArtifactFilter getDependencyFilter() {
        return original.getDependencyFilter();
    }
    
    public void setDependencyFilter(ArtifactFilter artifactFilter) {
        original.setDependencyFilter(artifactFilter);
    }
    
    public ArtifactHandler getArtifactHandler() {
        return original.getArtifactHandler();
    }
    
    public List getDependencyTrail() {
        return original.getDependencyTrail();
    }
    
    public void setDependencyTrail(List dependencyTrail) {
        original.setDependencyTrail(dependencyTrail);
    }
    
    public void setScope(String scope) {
        original.setScope(scope);
    }
    
    public VersionRange getVersionRange() {
        return original.getVersionRange();
    }
    
    public void setVersionRange(VersionRange newRange) {
        original.setVersionRange(newRange);
    }
    
    public void selectVersion(String version) {
        original.selectVersion(version);
    }
    
    public void setGroupId(String groupId) {
        original.setGroupId(groupId);
    }
    
    public void setArtifactId(String artifactId) {
        original.setArtifactId(artifactId);
    }
    
    public boolean isSnapshot() {
        return original.isSnapshot();
    }
    
    public void setResolved(boolean resolved) {
        original.setResolved(resolved);
    }
    
    public boolean isResolved() {
        return original.isResolved();
    }
    
    public void setResolvedVersion(String version) {
        original.setResolvedVersion(version);
    }
    
    public void setArtifactHandler(ArtifactHandler handler) {
        original.setArtifactHandler(handler);
    }
    
    public boolean isRelease() {
        return original.isRelease();
    }
    
    public void setRelease(boolean release) {
        original.setRelease(release);
    }
    
    public List getAvailableVersions() {
        return original.getAvailableVersions();
    }
    
    public void setAvailableVersions(List versions) {
        original.setAvailableVersions(versions);
    }
    
    public boolean isOptional() {
        return original.isOptional();
    }
    
    public void setOptional(boolean optional) {
        original.setOptional(optional);
    }
    
    public ArtifactVersion getSelectedVersion() throws OverConstrainedVersionException {
        return original.getSelectedVersion();
    }
    
    public boolean isSelectedVersionKnown() throws OverConstrainedVersionException {
        return original.isSelectedVersionKnown();
    }
    
    public int compareTo(Object o) {
        return original.compareTo(o);
    }
    
    @Override
    public String toString() {
        return original.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return original.equals(obj);
    }

    @Override
    public int hashCode() {
        return original.hashCode();
    }
    
    
}
