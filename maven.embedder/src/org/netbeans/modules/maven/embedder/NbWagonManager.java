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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.DefaultWagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;

/**
 *
 * @author mkleint
 */
public class NbWagonManager extends DefaultWagonManager {

    private final List<Artifact> letGoes = new ArrayList<Artifact>();
    //#14276 be very picky about what extesions/plugins get through..
    //<String> has this format GroupID:ArtifactID
    private final List<String> letGoes2 = new ArrayList<String>();

    private Logger LOG = Logger.getLogger(NbWagonManager.class.getName());
    /** Creates a new instance of NbWagonManager */
    public NbWagonManager() {
    }

    //MEVENIDE-422
    public void letGoThrough(Artifact artifact) {
        synchronized (letGoes) {
            letGoes.add(artifact);
        }
    }

    //MEVENIDE-422
    public void cleanLetGone(Artifact artifact) {
        synchronized (letGoes) {
            letGoes.remove(artifact);
        }
    }
    
    //#14276
    public void letGoThrough(String key) {
        synchronized (letGoes) {
            letGoes2.add(key);
        }
    }
    
    //#14276
    public void cleanLetGone(String key) {
        synchronized (letGoes) {
            letGoes2.remove(key);
        }
    }
    
    @Override
    public void getArtifact(Artifact artifact, List remoteRepositories) throws TransferFailedException, ResourceDoesNotExistException {
//        System.out.println("getArtifact1 =" + artifact);
        boolean cont;
        synchronized (letGoes) {
            cont = letGoes2.contains(artifact.getGroupId() + ":" + artifact.getArtifactId())
                    || letGoes.contains(artifact);
        }
        if (cont) {
            LOG.fine("            downloading1=" + artifact);
            try {
                super.getArtifact(artifact, remoteRepositories);
            } catch (TransferFailedException exc) {
                //ignore, we will just pretend it didn't happen.
                artifact.setResolved(true);
            } catch (ResourceDoesNotExistException exc) {
                //ignore, we will just pretend it didn't happen.
                artifact.setResolved(true);
            }
            synchronized (letGoes) {
                letGoes.remove(artifact);
            }
        } else {
            artifact.setResolved(true);
        }
    }

    @Override
    public void getArtifact(Artifact artifact, ArtifactRepository repository) throws TransferFailedException, ResourceDoesNotExistException {
        this.getArtifact(artifact, repository, true);
    }

    @Override
    public void putArtifact(File source, Artifact artifact, ArtifactRepository deploymentRepository) throws TransferFailedException {
//        System.out.println("putArtifact=" + source);
//        original.putArtifact(source, artifact, deploymentRepository);
    }

    @Override
    public void putArtifactMetadata(File source, ArtifactMetadata artifactMetadata, ArtifactRepository repository) throws TransferFailedException {
//        System.out.println("putArtifact metadata=" + source);
//        original.putArtifactMetadata(source, artifactMetadata, repository);
    }

    @Override
    public void getArtifactMetadata(ArtifactMetadata metadata, ArtifactRepository remoteRepository, File destination, String checksumPolicy) throws TransferFailedException, ResourceDoesNotExistException {
        boolean cont;
        synchronized (letGoes) {
            cont = letGoes2.contains(metadata.getGroupId() + ":" + metadata.getArtifactId()); //NOI18N
        }
        if (cont) {
//            System.out.println("checking metadata");
            LOG.fine("            metadata=" + metadata.getGroupId() + ":" + metadata.getArtifactId());
            super.getArtifactMetadata(metadata, remoteRepository, destination, checksumPolicy);
        }
//        System.out.println("getartifact metadata=" + metadata);
//        original.getArtifactMetadata(metadata, remoteRepository, destination, checksumPolicy);
    }

    @Override
    public void getArtifact(Artifact artifact,
            ArtifactRepository repository,
            boolean forceUpdateCheck) throws TransferFailedException, ResourceDoesNotExistException 
    {
        boolean cont;
        synchronized (letGoes) {
            cont = letGoes2.contains(artifact.getGroupId() + ":" + artifact.getArtifactId()) || letGoes.contains(artifact);
        }
        if (cont) {
            LOG.fine("               downloading2=" + artifact);
            super.getArtifact(artifact, repository, forceUpdateCheck);
            synchronized (letGoes) {
                letGoes.remove(artifact);
            }
        } else {
            artifact.setResolved(true);
        }
    }

    @Override
    public void getArtifact(Artifact artifact,
            List remoteRepositories,
            boolean forceUpdateCheck)
            throws TransferFailedException, ResourceDoesNotExistException 
    {
        boolean cont;
        synchronized (letGoes) {
            cont = letGoes2.contains(artifact.getGroupId() + ":" + artifact.getArtifactId())
                    || letGoes.contains(artifact);
        }
        if (cont) {
            LOG.fine("               downloading3=" + artifact);
            try {
                super.getArtifact(artifact, remoteRepositories, forceUpdateCheck);
            } catch (TransferFailedException exc) {
                if (NbArtifactResolver.isParentPomArtifact(artifact)) { //#163919
                    throw exc;
                }
                //ignore, we will just pretend it didn't happen.
                artifact.setResolved(true);
            } catch (ResourceDoesNotExistException exc) {
                if (NbArtifactResolver.isParentPomArtifact(artifact)) { //#163919
                    throw exc;
                }
                //ignore, we will just pretend it didn't happen.
                artifact.setResolved(true);
            } finally {
                synchronized (letGoes) {
                    letGoes.remove(artifact);
                }
            }
        } else {
            artifact.setResolved(true);
        }

    }
    
}
