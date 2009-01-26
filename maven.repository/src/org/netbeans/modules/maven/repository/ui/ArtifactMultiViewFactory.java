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

package org.netbeans.modules.maven.repository.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author mkleint
 */
@ServiceProvider( service=ArtifactViewerFactory.class )
public final class ArtifactMultiViewFactory implements ArtifactViewerFactory {

    public TopComponent createTopComponent(Artifact artifact, List<ArtifactRepository> repos) {
        return createTopComponent(null, artifact, repos);
    }
    public TopComponent createTopComponent(final NBVersionInfo info) {
        return createTopComponent(info, null, null);
    }

    private TopComponent createTopComponent(final NBVersionInfo info, Artifact artifact, final List<ArtifactRepository> fRepos) {
        assert info != null || artifact != null;
        final InstanceContent ic = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(ic);
        if (artifact == null && info != null) {
            artifact = RepositoryUtil.createArtifact(info);
        }
        ic.add(artifact);
        if (info != null) {
            ic.add(info);
        }
        final Artifact fArt = artifact;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
                List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
                if (fRepos != null) {
                    repos.addAll(fRepos);
                }
                if (repos.size() == 0) {
                    //add central repo
                    repos.add(EmbedderFactory.createRemoteRepository(embedder, "http://repo1.maven.org/maven2", "central"));
                    //add repository form info
                    if (info != null && !"central".equals(info.getRepoId())) {
                        RepositoryInfo rinfo = RepositoryPreferences.getInstance().getRepositoryInfoById(info.getRepoId());
                        String url = rinfo.getRepositoryUrl();
                        if (url != null) {
                            repos.add(EmbedderFactory.createRemoteRepository(embedder, url, rinfo.getId()));
                        }
                    }
                }
                MavenProject prj = readMavenProject(embedder, fArt, repos);
                ic.add(prj);
            }
        });

        Action[] toolbarActions = new Action[] {
            CommonArtifactActions.createScmCheckoutAction(lookup)
        };
        ic.add(toolbarActions);

        MultiViewDescription artDesc = new BasicArtifactMD(lookup);
        MultiViewDescription prjDesc = new BasicProjectMD(lookup);
//        MultiViewDescription depDesc = new BasicDependencyMD(lookup);
        TopComponent tc = MultiViewFactory.createMultiView(new MultiViewDescription[]
            { artDesc, prjDesc, /*depDesc*/ }, artDesc);
        tc.setDisplayName(artifact.getArtifactId() + ":" + artifact.getVersion()); //NOI18N
        tc.setToolTipText(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion()); //NOI18N
        return tc;
    }

    public static MavenProject readMavenProject(MavenEmbedder embedder, Artifact artifact, List<ArtifactRepository> remoteRepos) {

        try {
            MavenProjectBuilder bldr = (MavenProjectBuilder) embedder.getPlexusContainer().lookup(MavenProjectBuilder.ROLE);
            return bldr.buildFromRepository(artifact, remoteRepos, embedder.getLocalRepository());
        } catch (ProjectBuildingException ex) {
            //TODO shall not end up in user's face..
            Exceptions.printStackTrace(ex);
        } catch (ComponentLookupException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new MavenProject();
    }


}
