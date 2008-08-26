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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.*;
import java.lang.reflect.Field;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.CredentialsDataSource;
import org.apache.maven.artifact.manager.CredentialsDataSourceException;
import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 *
 * @author mkleint
 */
public class NbArtifactResolverTest extends TestCase {
    
    public NbArtifactResolverTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(NbArtifactResolverTest.class);
        
        return suite;
    }

    public void testDefaultArtifactResolverHasWagonManagerField() throws Exception {
        Field wagonMan = DefaultArtifactResolver.class.getDeclaredField("wagonManager");
        assertNotNull(wagonMan);
    }
    
    public void testWagonManagerMethods() {
        new TestWagonManager();
    }
    
    public void testArtifactResolverMethods() {
        new TestArtifactResolver();
    }

    //will fail to compile when new method is added to the interface.
    // we need to update the NbWagonManager then
    private class TestWagonManager implements WagonManager {

        public Wagon getWagon(String protocol) throws UnsupportedProtocolException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Wagon getWagon(Repository repository) throws UnsupportedProtocolException, WagonConfigurationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void getArtifact(Artifact artifact, List remoteRepositories) throws TransferFailedException, ResourceDoesNotExistException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void getArtifact(Artifact artifact, List remoteRepositories, boolean forceUpdateCheck) throws TransferFailedException, ResourceDoesNotExistException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void getArtifact(Artifact artifact, ArtifactRepository repository) throws TransferFailedException, ResourceDoesNotExistException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void getArtifact(Artifact artifact, ArtifactRepository repository, boolean forceUpdateCheck) throws TransferFailedException, ResourceDoesNotExistException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void putArtifact(File source, Artifact artifact, ArtifactRepository deploymentRepository) throws TransferFailedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void putArtifactMetadata(File source, ArtifactMetadata artifactMetadata, ArtifactRepository repository) throws TransferFailedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void getArtifactMetadata(ArtifactMetadata metadata, ArtifactRepository remoteRepository, File destination, String checksumPolicy) throws TransferFailedException, ResourceDoesNotExistException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setOnline(boolean online) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isOnline() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addProxy(String protocol, String host, int port, String username, String password, String nonProxyHosts) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void registerCredentialsDataSource(CredentialsDataSource cds) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addAuthenticationCredentials(String repositoryId, String username, String password, String privateKey, String passphrase) throws CredentialsDataSourceException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addAuthenticationInfo(String repositoryId, String username, String password, String privateKey, String passphrase) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addMirror(String id, String mirrorOf, String url) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setDownloadMonitor(TransferListener downloadMonitor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addPermissionInfo(String repositoryId, String filePermissions, String directoryPermissions) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ProxyInfo getProxy(String protocol) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public AuthenticationInfo getAuthenticationInfo(String id) throws CredentialsDataSourceException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addConfiguration(String repositoryId, Xpp3Dom configuration) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setInteractive(boolean interactive) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void registerWagons(Collection wagons, PlexusContainer extensionContainer) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void findAndRegisterWagons(PlexusContainer container) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setDefaultRepositoryPermissions(RepositoryPermissions permissions) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void getArtifactMetadataFromDeploymentRepository(ArtifactMetadata arg0, ArtifactRepository arg1, File arg2, String arg3) throws TransferFailedException, ResourceDoesNotExistException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ArtifactRepository getMirrorRepository(ArtifactRepository arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    //will fail to compile when new method is added to the interface.
    // we need to update the NbWagonManager then
    private class TestArtifactResolver implements ArtifactResolver {

        public void resolve(Artifact artifact, List remoteRepositories, ArtifactRepository localRepository) throws ArtifactResolutionException, ArtifactNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, List remoteRepositories, ArtifactRepository localRepository, ArtifactMetadataSource source) throws ArtifactResolutionException, ArtifactNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, List remoteRepositories, ArtifactRepository localRepository, ArtifactMetadataSource source, List listeners) throws ArtifactResolutionException, ArtifactNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source, ArtifactFilter filter) throws ArtifactResolutionException, ArtifactNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, Map managedVersions, ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source) throws ArtifactResolutionException, ArtifactNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, Map managedVersions, ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source, ArtifactFilter filter) throws ArtifactResolutionException, ArtifactNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, Map managedVersions, ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source, ArtifactFilter filter, List listeners) throws ArtifactResolutionException, ArtifactNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ArtifactResolutionResult resolveTransitively(Set artifacts, Artifact originatingArtifact, Map managedVersions, ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source, ArtifactFilter filter, List listeners, List conflictResolvers) throws ArtifactResolutionException, ArtifactNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void resolveAlways(Artifact artifact, List remoteRepositories, ArtifactRepository localRepository) throws ArtifactResolutionException, ArtifactNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ArtifactResolutionResult resolve(ArtifactResolutionRequest request) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}
