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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

/**
 *
 * @author mkleint
 */
public class NbArtifactResolver extends DefaultArtifactResolver {    
    
    private ResolutionListener listener;
    protected Field wagonMan;
    
    /** Creates a new instance of NbWagonManager */
    public NbArtifactResolver() {
        super();
        try {
            wagonMan = DefaultArtifactResolver.class.getDeclaredField("wagonManager");
            wagonMan.setAccessible(true);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void resolve(Artifact artifact, List list, ArtifactRepository artifactRepository) throws ArtifactResolutionException, ArtifactNotFoundException {
//        artifact.setResolved(true);
        //MEVENIDE-422 
        if (isParentPomArtifact(artifact)) {
            try {
                letArtifactGo(artifact);
                super.resolve(artifact, list, artifactRepository);
            } catch (ArtifactResolutionException exc) {
                if (exc.getCause() instanceof IOException) {
                    // DefaultArtifactResolver:193 when having snapshots something gets copied and fails
                    // when the wagon manager just pretends to download something..
                    System.out.println("exc=" + exc.getCause().getMessage());
                    return;
                }
                throw exc;
            }
            finally {
                cleanLetGone(artifact);
            }
        } else {
            super.resolve(artifact, list, artifactRepository);
        }
    }
    
    @Override
    public void resolveAlways(Artifact artifact, List list, ArtifactRepository artifactRepository) throws ArtifactResolutionException, ArtifactNotFoundException {
        if (isParentPomArtifact(artifact)) {
            try {
                letArtifactGo(artifact);
                super.resolveAlways(artifact, list, artifactRepository);
            } catch (ArtifactResolutionException exc) {
                if (exc.getCause() instanceof IOException) {
                    // DefaultArtifactResolver:193 when having snapshots something gets copied and fails
                    // when the wagon manager just pretends to download something..
                    System.out.println("exc=" + exc.getCause().getMessage());
                    return;
                }
                throw exc;
            } finally {
                cleanLetGone(artifact);
            }
        } else {
            super.resolveAlways(artifact, list, artifactRepository);
            
        }
    }

    @Override
    public ArtifactResolutionResult resolveTransitively(
                   Set set, Artifact artifact, 
                   Map map, ArtifactRepository artifactRepository, 
                   List list, ArtifactMetadataSource artifactMetadataSource, 
                   ArtifactFilter artifactFilter, List listeners) throws ArtifactResolutionException, ArtifactNotFoundException {
//        System.out.println("resolve trans6=" + artifact);
        ArrayList newListeners = new ArrayList();
        if (listeners != null) {
            newListeners.addAll(listeners);
        }
        if (listener != null) {
            newListeners.add(listener);
        }
        return super.resolveTransitively(set, artifact, map, artifactRepository, list, artifactMetadataSource, artifactFilter, newListeners);
    }

    private void cleanLetGone(Artifact artifact) {
        if (wagonMan != null) {
            try {
                Object manObj = wagonMan.get(this);
                if (manObj instanceof NbWagonManager) {
                    NbWagonManager manager = (NbWagonManager)manObj;
                    manager.cleanLetGone(artifact);
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void letArtifactGo(Artifact artifact) {
        if (wagonMan != null) {
            try {
                Object manObj = wagonMan.get(this);
                if (manObj instanceof NbWagonManager) {
                    NbWagonManager manager = (NbWagonManager)manObj;
                    manager.letGoThrough(artifact);
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    static boolean isParentPomArtifact(Artifact artifact) {
        //the condition is meant to mean.. "if we look for parent pom", not sure it's close enough..
        return artifact.getScope() == null && "pom".equals(artifact.getType()); //NOI18N
    }
    
}
