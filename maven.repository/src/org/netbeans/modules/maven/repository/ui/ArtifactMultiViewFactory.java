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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.swing.Action;
import javax.swing.JButton;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerPanelProvider;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.repository.dependency.AddAsDependencyAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
        return createTopComponent(null, null, artifact, repos);
    }
    public TopComponent createTopComponent(NBVersionInfo info) {
        return createTopComponent(null, info, null, null);
    }

    public TopComponent createTopComponent(Project prj) {
        return createTopComponent(prj, null, null, null);
    }

    private TopComponent createTopComponent(final Project prj, final NBVersionInfo info, Artifact artifact, final List<ArtifactRepository> fRepos) {
        assert info != null || artifact != null || prj != null;
        final InstanceContent ic = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(ic);
        if (artifact == null && info != null) {
            artifact = RepositoryUtil.createArtifact(info);
        }
        if (artifact == null && prj != null) {
            NbMavenProject mvPrj = prj.getLookup().lookup(NbMavenProject.class);
            MavenProject mvn = mvPrj.getMavenProject();
            ic.add(prj);
            artifact = mvn.getArtifact();
        }
        assert artifact != null;
        ic.add(artifact);
        if (info != null) {
            ic.add(info);
        }
        final Artifact fArt = artifact;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
                MavenProject mvnprj;
                try {
                    if (prj == null) {
                        List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
                        if (fRepos != null) {
                            repos.addAll(fRepos);
                        }
                        if (repos.size() == 0) {
                            //add central repo
                            repos.add(EmbedderFactory.createRemoteRepository(embedder, "http://repo1.maven.org/maven2", "central")); //NOI18N
                            //add repository form info
                            if (info != null && !"central".equals(info.getRepoId())) { //NOI18N
                                RepositoryInfo rinfo = RepositoryPreferences.getInstance().getRepositoryInfoById(info.getRepoId());
                                String url = rinfo.getRepositoryUrl();
                                if (url != null) {
                                    repos.add(EmbedderFactory.createRemoteRepository(embedder, url, rinfo.getId()));
                                }
                            }
                        }
                        mvnprj = readMavenProject(embedder, fArt, repos);
                    } else {
                        NbMavenProject im = prj.getLookup().lookup(NbMavenProject.class);
                        @SuppressWarnings("unchecked")
                        List<String> profiles = im.getMavenProject().getActiveProfiles();
                        mvnprj = im.loadAlternateMavenProject(embedder, profiles, new Properties());
                    FileObject fo = prj.getLookup().lookup(FileObject.class);
                    if (fo != null) {
                        ModelSource ms = Utilities.createModelSource(fo);
                        if (ms.isEditable()) {
                            POMModel model = POMModelFactory.getDefault().getModel(ms);
                            if (model != null) {
                                ic.add(model);
                            }
                        }
                    }
                    }
                    ic.add(mvnprj);
                    DependencyNode root = DependencyTreeFactory.createDependencyTree(mvnprj, EmbedderFactory.getOnlineEmbedder(), Artifact.SCOPE_TEST);
                    ic.add(root);
                } catch (ComponentLookupException ex) {
                    Exceptions.printStackTrace(ex); //this should not happen, if it does, report.
                } catch (ProjectBuildingException ex) {
                    ErrorPanel pnl = new ErrorPanel(ex);
                    DialogDescriptor dd = new DialogDescriptor(pnl, NbBundle.getMessage(ArtifactMultiViewFactory.class, "TIT_Error"));
                    JButton close = new JButton();
                    org.openide.awt.Mnemonics.setLocalizedText(close, NbBundle.getMessage(ArtifactMultiViewFactory.class, "BTN_CLOSE"));
                    dd.setOptions(new Object[] { close });
                    dd.setClosingOptions(new Object[] { close });
                    DialogDisplayer.getDefault().notify(dd);
                    File fallback = InstalledFileLocator.getDefault().locate("maven2/fallback_pom.xml", null, false); //NOI18N
                    try {
                        MavenProject m = embedder.readProject(fallback);
                        m.setDescription(null);
                        ic.add(m);
                    } catch (Exception x) {
                        // oh well..
                        //NOPMD
                    }
                }
            }
        });

        Action[] toolbarActions = new Action[] {
            new AddAsDependencyAction(fArt),
            CommonArtifactActions.createScmCheckoutAction(lookup),
            CommonArtifactActions.createLibraryAction(lookup)
        };
        ic.add(toolbarActions);

        Collection<? extends ArtifactViewerPanelProvider> provs = Lookup.getDefault().lookupAll(ArtifactViewerPanelProvider.class);
        MultiViewDescription[] panels = new MultiViewDescription[provs.size()];
        int i = 0;
        for (ArtifactViewerPanelProvider prov : provs) {
            panels[i] = prov.createPanel(lookup);
            i = i + 1;
        }
        TopComponent tc = MultiViewFactory.createMultiView(panels, panels[0]);
        tc.setDisplayName(artifact.getArtifactId() + ":" + artifact.getVersion()); //NOI18N
        tc.setToolTipText(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion()); //NOI18N
        return tc;
    }

    private static MavenProject readMavenProject(MavenEmbedder embedder, Artifact artifact, List<ArtifactRepository> remoteRepos) throws ComponentLookupException, ProjectBuildingException {
        MavenProjectBuilder bldr = (MavenProjectBuilder) embedder.getPlexusContainer().lookup(MavenProjectBuilder.ROLE);
        return bldr.buildFromRepository(artifact, remoteRepos, embedder.getLocalRepository());
    }


}
