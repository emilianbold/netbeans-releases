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

package org.netbeans.modules.maven.nodes;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.util.FileUtils;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.NbArtifactFixer;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.dependencies.DependencyExcludeNodeVisitor;
import org.netbeans.modules.maven.dependencies.ExcludeDependencyPanel;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.POMModel;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.PropertiesAction;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * node representing a dependency
 * @author  Milos Kleint 
 */
public class DependencyNode extends AbstractNode implements PreferenceChangeListener {
    private static final @StaticResource String JAVADOC_BADGE_ICON = "org/netbeans/modules/maven/DependencyJavadocIncluded.png"; //NOI18N
    private static final @StaticResource String SOURCE_BADGE_ICON = "org/netbeans/modules/maven/DependencySrcIncluded.png"; //NOI18N
    private static final @StaticResource String MANAGED_BADGE_ICON = "org/netbeans/modules/maven/DependencyManaged.png"; //NOI18N
    private static final @StaticResource String ARTIFACT_ICON = "org/netbeans/modules/maven/ArtifactIcon.png";
    private static final @StaticResource String DEPENDENCY_ICON = "org/netbeans/modules/maven/DependencyIcon.png";
    private static final @StaticResource String MAVEN_ICON = "org/netbeans/modules/maven/resources/Maven2Icon.gif";
    private static final @StaticResource String TRANSITIVE_ARTIFACT_ICON = "org/netbeans/modules/maven/TransitiveArtifactIcon.png";
    private static final @StaticResource String TRANSITIVE_DEPENDENCY_ICON = "org/netbeans/modules/maven/TransitiveDependencyIcon.png";
    private static final @StaticResource String TRANSITIVE_MAVEN_ICON = "org/netbeans/modules/maven/TransitiveMaven2Icon.gif";

    private Artifact art;
    private NbMavenProjectImpl project;
    private boolean longLiving;
    private PropertyChangeListener listener;
    private ChangeListener listener2;
    private volatile String iconBase = DEPENDENCY_ICON;
    
    private static String toolTipJavadoc = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(JAVADOC_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_JavadocBadge");//NOI18N
    private static String toolTipSource = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(SOURCE_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_SourceBadge");//NOI18N
    private static String toolTipMissing = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(MavenProjectNode.BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_MissingBadge");//NOI18N
    private static String toolTipManaged = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(MANAGED_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_ManagedBadge");//NOI18N

    private static final RequestProcessor RP = new RequestProcessor(DependencyNode.class);

    public static Children createChildren(Artifact art, boolean longLiving) {
        assert art != null;
        assert art.getFile() != null;
        if (!longLiving) {
            return Children.LEAF;
        }
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(art.getFile()));
        if (fo != null && FileUtil.isArchiveFile(fo)) {
            return new JarContentFilterChildren(PackageView.createPackageView(new ArtifactSourceGroup(art)));
        }
        return Children.LEAF;
    }

    public DependencyNode(NbMavenProjectImpl project, Artifact art, boolean isLongLiving) {
        super(createChildren(art, isLongLiving), Lookups.fixed(project, art));
        this.project = project;
        this.art = art;
        longLiving = isLongLiving;
        if (longLiving) {
            listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                        refreshNode();
                    }
                }
            };
            NbMavenProject.addPropertyChangeListener(project, WeakListeners.propertyChange(listener, project.getProjectWatcher()));
            listener2 = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent event) {
                    refreshNode();
                }
            };
            //TODO check if this one is a performance bottleneck.
            MavenFileOwnerQueryImpl.getInstance().addChangeListener(
                    WeakListeners.change(listener2,
                    MavenFileOwnerQueryImpl.getInstance()));
            DependenciesNode.prefs().addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, DependenciesNode.prefs()));
        }
        setDisplayName(createName(false));
        setIconBase(false);
        if (longLiving) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    refreshNode();
                }
            });
        }
    }

    /**
     * public because of the property sheet
     */
    public boolean isTransitive() {
        List trail = art.getDependencyTrail();
        return trail != null && trail.size() > 2;
    }

    public boolean isManaged() {
        DependencyManagement dm = project.getOriginalMavenProject().getDependencyManagement();
        if (dm != null) {
            List<Dependency> dmList = dm.getDependencies();
            for (Dependency d : dmList) {
                if (art.getGroupId().equals(d.getGroupId()) &&
                    art.getArtifactId().equals(d.getArtifactId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setIconBase(boolean longLiving) {
        String base;
        if (longLiving && isDependencyProjectAvailable()) {
            if (isTransitive()) {
                base = TRANSITIVE_MAVEN_ICON;
            } else {
                base = MAVEN_ICON;
            }
        } else if (isTransitive()) {
            if (isAddedToCP()) {
                base = TRANSITIVE_DEPENDENCY_ICON;
            } else {
                base = TRANSITIVE_ARTIFACT_ICON;
            }
        } else if (isAddedToCP()) {
            base = DEPENDENCY_ICON;
        } else {
            base = ARTIFACT_ICON;
        }
        this.iconBase = base;
        setIconBaseWithExtension(base);
    }

    @Messages({
        "DESC_Dep1=GroupId:",
        "DESC_Dep2=ArtifactId:",
        "DESC_Dep3=Version:",
        "DESC_Dep4=Type:",
        "DESC_Dep5=Classifier:",
        "DESC_scope=Scope:",
        "DESC_via=Via:"
    })
    @Override public String getShortDescription() {
        StringBuilder buf = new StringBuilder();
        buf.append("<html><i>").append(DESC_Dep1()).append("</i><b> ").append(art.getGroupId()).append("</b><br><i>"); //NOI18N
        buf.append(DESC_Dep2()).append("</i><b> ").append(art.getArtifactId()).append("</b><br><i>");//NOI18N
        buf.append(DESC_Dep3()).append("</i><b> ").append(art.getVersion()).append("</b><br><i>");//NOI18N
        buf.append(DESC_Dep4()).append("</i><b> ").append(art.getType()).append("</b><br>");//NOI18N
        if (art.getClassifier() != null) {
            buf.append("<i>").append(DESC_Dep5()).append("</i><b> ").append(art.getClassifier()).append("</b><br>");//NOI18N
        }
        buf.append("<i>").append(DESC_scope()).append("</i><b> ").append(art.getScope()).append("</b><br>");
        List<String> trail = art.getDependencyTrail();
        for (int i = trail.size() - 2; i > 0 && /* just to be safe */ i < trail.size(); i--) {
            String[] id = trail.get(i).split(":"); // g:a:t[:c]:v
            buf.append("<i>").append(DESC_via()).append("</i> ").append(id[1]).append("<br>");
        }
        // it seems that with ending </html> tag the icon descriptions are not added.
//        buf.append("</html>");//NOI18N
        return buf.toString();
    }

    private boolean isAddedToCP() {
        return art.getArtifactHandler().isAddedToClasspath();
    }

    /**
     * this call is slow
     * @return
     */
    private boolean isDependencyProjectAvailable() {
        return getDependencyProjectAvailable() != null;
    }
    
     /**
     * this call is slow
     * @return
     */
    private Project getDependencyProjectAvailable() {
        if ( Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
            return null;
        }
        URI uri = org.openide.util.Utilities.toURI(art.getFile());
        return FileOwnerQuery.getOwner(uri);
    }   

    
    private void refreshNode() {
        setDisplayName(createName(longLiving));
        setIconBase(longLiving);
        fireIconChange();
        fireDisplayNameChange(null, getDisplayName());
//        if (longLiving) {
//            ((DependencyChildren)getChildren()).doRefresh();
//        }
        //#142784
        if (longLiving) {
            if (Children.LEAF == getChildren()) {
                Children childs = createChildren(art, true);
                if (childs != Children.LEAF) {
                    setChildren(childs);
                }
            }
        }
    }

    @Override
    public String getHtmlDisplayName() {
        StringBuilder n = new StringBuilder("<html>");
        n.append(getDisplayName());
        if (ArtifactUtils.isSnapshot(art.getVersion()) && art.getVersion().indexOf("SNAPSHOT") < 0) { //NOI18N
            n.append(" <b>[").append(art.getVersion()).append("]</b>");
        }
        if (!art.getArtifactHandler().isAddedToClasspath() && !Artifact.SCOPE_COMPILE.equals(art.getScope())) {
            n.append("  <i>[").append(art.getScope()).append("]</i>");
        }
        n.append("</html>");
        return n.toString();
    }

    private String createName(boolean longLiving) {
        if (longLiving) {
            //TODO when the name changes on the other end (dep project) we have no way of knowing..
            Project prj = getDependencyProjectAvailable();
            if (prj != null) {
                return ProjectUtils.getInformation(prj).getDisplayName();
            }
        }
        if (NbArtifactFixer.isFallbackFile(art.getFile())) {
            return art.getArtifactId() + "-" + art.getBaseVersion() + (art.getClassifier() != null ? "-" + art.getClassifier() : "") + "." + art.getArtifactHandler().getExtension();
        }
        return art.getFile().getName();
    }

    @Override
    public Action[] getActions(boolean context) {
        Collection<Action> acts = new ArrayList<Action>();
        if (longLiving && isDependencyProjectAvailable()) {
            acts.add(OpenProjectAction.SINGLETON);
        }
        if (isAddedToCP()) {
            InstallLocalArtifactAction act = new InstallLocalArtifactAction();
            acts.add(act);
            if (!isLocal()) {
                act.setEnabled(true);
            }
        }

//        acts.add(new EditAction());
//        acts.add(RemoveDepAction.get(RemoveDepAction.class));
        if (!hasJavadocInRepository()) {
            acts.add(new DownloadJavadocSrcAction(true));
            if (isAddedToCP()) {
                acts.add(new InstallLocalJavadocAction());
            }
        }
        if (!hasSourceInRepository()) {
            acts.add(new DownloadJavadocSrcAction(false));
            if (isAddedToCP()) {
                acts.add(new InstallLocalSourcesAction());
            }
        }
        if (isTransitive()) {
            acts.add(new ExcludeTransitiveAction());
            acts.add(SETINCURRENTINSTANCE);
        } else {
            acts.add(REMOVEDEPINSTANCE);
        }
        acts.add(null);
        acts.add(CommonArtifactActions.createViewArtifactDetails(art, project.getOriginalMavenProject().getRemoteArtifactRepositories()));
        acts.add(CommonArtifactActions.createFindUsages(art));
        acts.add(null);
        acts.add(CommonArtifactActions.createViewJavadocAction(art));
        /* #164992: disabled
        acts.add(CommonArtifactActions.createViewProjectHomeAction(art, project.getOriginalMavenProject().getRemoteArtifactRepositories()));
        acts.add(CommonArtifactActions.createViewBugTrackerAction(art, project.getOriginalMavenProject().getRemoteArtifactRepositories()));
        acts.add(CommonArtifactActions.createSCMActions(art, project.getOriginalMavenProject().getRemoteArtifactRepositories()));
         */
        acts.add(null);
        acts.add(PropertiesAction.get(PropertiesAction.class));
        return acts.toArray(new Action[acts.size()]);
    }

    @Override
    public boolean canDestroy() {
        return !isTransitive();
    }
    @Override
    public void destroy() throws IOException {
        REMOVEDEPINSTANCE.createContextAwareInstance(getLookup()).actionPerformed(null);
    }

    @Override
    public boolean canRename() {
        return false;
    }

    
    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean hasCustomizer() {
        return false;
    }

    public boolean isLocal() {
        return art.getFile().exists() && !NbArtifactFixer.isFallbackFile(art.getFile());
    }

    public boolean hasJavadocInRepository() {
        return (!Artifact.SCOPE_SYSTEM.equals(art.getScope())) && getJavadocFile().exists();
    }

    public File getJavadocFile() {
        return getJavadocFile(art.getFile());
    }

    private static File getJavadocFile(File artifact) {
        String version = artifact.getParentFile().getName();
        String artifactId = artifact.getParentFile().getParentFile().getName();
        return new File(artifact.getParentFile(), artifactId + "-" + version + "-javadoc.jar"); //NOI18N
    }

    public File getSourceFile() {
        return getSourceFile(art.getFile());
    }

    private static File getSourceFile(File artifact) {
        String version = artifact.getParentFile().getName();
        String artifactId = artifact.getParentFile().getParentFile().getName();
        return new File(artifact.getParentFile(), artifactId + "-" + version + "-sources.jar"); //NOI18N
    }

    public boolean hasSourceInRepository() {
        if (Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
            return false;
        }
        return getSourceFile().exists();
    }

    void downloadJavadocSources(ProgressContributor progress, boolean isjavadoc) {
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        progress.start(2);
        if ( Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
            progress.finish();
            return;
        }
        try {
            if (isjavadoc) {
                Artifact javadoc = project.getEmbedder().createArtifactWithClassifier(
                    art.getGroupId(),
                    art.getArtifactId(),
                    art.getVersion(),
                    art.getType(),
                    "javadoc"); //NOI18N
                progress.progress(org.openide.util.NbBundle.getMessage(DependencyNode.class, "MSG_Checking_Javadoc", art.getId()), 1);
                online.resolve(javadoc, project.getOriginalMavenProject().getRemoteArtifactRepositories(), project.getEmbedder().getLocalRepository());
            } else {
                Artifact sources = project.getEmbedder().createArtifactWithClassifier(
                    art.getGroupId(),
                    art.getArtifactId(),
                    art.getVersion(),
                    art.getType(),
                    "sources"); //NOI18N
                progress.progress(org.openide.util.NbBundle.getMessage(DependencyNode.class, "MSG_Checking_Sources",art.getId()), 1);
                online.resolve(sources, project.getOriginalMavenProject().getRemoteArtifactRepositories(), project.getEmbedder().getLocalRepository());
            }
        } catch (ArtifactNotFoundException ex) {
            // just ignore..ex.printStackTrace();
        } catch (ArtifactResolutionException ex) {
            // just ignore..ex.printStackTrace();
        } finally {
            progress.finish();
        }
        refreshNode();
    }


    
    @Override
    public Image getIcon(int param) {
        return badge(super.getIcon(param));
    }
    
    private boolean isIconProjectBased() {
        String base = iconBase;
        return TRANSITIVE_MAVEN_ICON.equals(base) || MAVEN_ICON.equals(base);
    }

    private Image badge(Image retValue) {
        if (isLocal()) {
            if (!isIconProjectBased()) {
                if (hasJavadocInRepository()) {
                    Image ann = ImageUtilities.loadImage(JAVADOC_BADGE_ICON); //NOI18N
                    ann = ImageUtilities.addToolTipToImage(ann, toolTipJavadoc);
                    retValue = ImageUtilities.mergeImages(retValue, ann, 12, 0);//NOI18N
                }
                if (hasSourceInRepository()) {
                    Image ann = ImageUtilities.loadImage(SOURCE_BADGE_ICON); //NOI18N
                    ann = ImageUtilities.addToolTipToImage(ann, toolTipSource);
                    retValue = ImageUtilities.mergeImages(retValue, ann, 12, 8);//NOI18N
                }
            }
            if (showManagedState() && isManaged()) {
                Image ann = ImageUtilities.loadImage(MANAGED_BADGE_ICON); //NOI18N
                ann = ImageUtilities.addToolTipToImage(ann, toolTipManaged);
                retValue = ImageUtilities.mergeImages(retValue, ann, 0, 8);//NOI18N
            }
            return retValue;
        } else if (!isIconProjectBased()) {
            Image ann = ImageUtilities.loadImage(MavenProjectNode.BADGE_ICON); //NOI18N
            ann = ImageUtilities.addToolTipToImage(ann, toolTipMissing);
            return ImageUtilities.mergeImages(retValue, ann, 0, 0);//NOI18N
        } else {
            return retValue;
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return badge(super.getOpenedIcon(type));
    }

    @Override
    public Component getCustomizer() {
        return null;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set basicProps = sheet.get(Sheet.PROPERTIES);
        try {
            PropertySupport.Reflection artifactId = new PropertySupport.Reflection<String>(art, String.class, "getArtifactId", null); //NOI18N
            artifactId.setName("artifactId"); //NOI18N
            artifactId.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Artifact"));
            artifactId.setShortDescription(""); //NOI18N
            PropertySupport.Reflection groupId = new PropertySupport.Reflection<String>(art, String.class, "getGroupId", null); //NOI18N
            groupId.setName("groupId"); //NOI18N
            groupId.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Group"));
            groupId.setShortDescription(""); //NOI18N
            PropertySupport.Reflection version = new PropertySupport.Reflection<String>(art, String.class, "getVersion", null); //NOI18N
            version.setName("version"); //NOI18N
            version.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Version"));
            version.setShortDescription(org.openide.util.NbBundle.getMessage(DependencyNode.class, "HINT_Version"));
            PropertySupport.Reflection type = new PropertySupport.Reflection<String>(art, String.class, "getType", null); //NOI18N
            type.setName("type"); //NOI18N
            type.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Type"));
            PropertySupport.Reflection scope = new PropertySupport.Reflection<String>(art, String.class, "getScope", null); //NOI18N
            scope.setName("scope"); //NOI18N
            scope.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Scope"));
            PropertySupport.Reflection classifier = new PropertySupport.Reflection<String>(art, String.class, "getClassifier", null); //NOI18N
            classifier.setName("classifier"); //NOI18N
            classifier.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Classifier"));
            PropertySupport.Reflection hasJavadoc = new PropertySupport.Reflection<Boolean>(this, Boolean.TYPE, "hasJavadocInRepository", null); //NOI18N
            hasJavadoc.setName("javadoc"); //NOI18N
            hasJavadoc.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Javadoc_Locally"));
            PropertySupport.Reflection hasSources = new PropertySupport.Reflection<Boolean>(this, Boolean.TYPE, "hasSourceInRepository", null); //NOI18N
            hasSources.setName("sources"); //NOI18N
            hasSources.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Sources_Locally"));
            PropertySupport.Reflection transitive = new PropertySupport.Reflection<Boolean>(this, Boolean.TYPE, "isTransitive", null); //NOI18N
            transitive.setName("transitive"); //NOI18N
            transitive.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Transitive"));

            basicProps.put(new Node.Property[] {
                artifactId, groupId, version, type, scope, classifier, transitive, hasJavadoc, hasSources
            });
        } catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
        return sheet;
    }
//    private class DownloadJavadocAndSourcesAction extends AbstractAction implements Runnable {
//        public DownloadJavadocAndSourcesAction() {
//            putValue(Action.NAME, "Download Javadoc & Source");
//        }
//
//        public void actionPerformed(ActionEvent event) {
//            RP.post(this);
//        }
//
//        public void run() {
//            ProgressContributor contrib = AggregateProgressFactory.createProgressContributor("single"); //NOI18N
//            AggregateProgressHandle handle = AggregateProgressFactory.createHandle("Download Javadoc and Sources", new ProgressContributor[] {contrib}, null, null);
//            handle.start();
//            downloadJavadocSources(EmbedderFactory.getOnlineEmbedder(), contrib);
//            handle.finish();
//        }
//    }

    //why oh why do we have to suffer through this??
    private static RemoveDependencyAction REMOVEDEPINSTANCE = new RemoveDependencyAction(Lookup.EMPTY);

    @Messages("BTN_Remove_Dependency=Remove Dependency")
    private static class RemoveDependencyAction extends AbstractAction implements ContextAwareAction {

        private Lookup lkp;

        RemoveDependencyAction(Lookup look) {
            putValue(Action.NAME, BTN_Remove_Dependency());
            lkp = look;
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<NbMavenProjectImpl>(res);
            if (prjs.size() != 1) {
                setEnabled(false);
            }
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new RemoveDependencyAction(actionContext);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            final Collection<? extends Artifact> artifacts = lkp.lookupAll(Artifact.class);
            if (artifacts.isEmpty()) {
                return;
            }
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<NbMavenProjectImpl>(res);
            if (prjs.size() != 1) {
                return;
            }

            final NbMavenProjectImpl project = prjs.iterator().next();
            final List<Artifact> unremoved = new ArrayList<Artifact>();

            final ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    for (Artifact art : artifacts) {
                        org.netbeans.modules.maven.model.pom.Dependency dep =
                                model.getProject().findDependencyById(art.getGroupId(), art.getArtifactId(), null);
                        if (dep != null) {
                            model.getProject().removeDependency(dep);
                        } else {
                            unremoved.add(art);
                        }
                    }
                }
            };
            RP.post(new Runnable() {
                @Override
                public void run() {
                    FileObject fo = FileUtil.toFileObject(project.getPOMFile());
                    Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
                    if (unremoved.size() > 0) {
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DependencyNode.class, "MSG_Located_In_Parent", unremoved.size()), Integer.MAX_VALUE);
                    }
                }
            });
        }
    }

    private static final String SHOW_MANAGED_DEPENDENCIES = "show.managed.dependencies";

    private static boolean showManagedState() {
        return DependenciesNode.prefs().getBoolean(SHOW_MANAGED_DEPENDENCIES, false);
    }

    @Override public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().equals(SHOW_MANAGED_DEPENDENCIES)) {
            refreshNode();
        }
    }

    static class ShowManagedStateAction extends AbstractAction implements Presenter.Popup {

        @NbBundle.Messages("LBL_ShowManagedState=Show Managed State for dependencies")
        ShowManagedStateAction() {
            String s = LBL_ShowManagedState();
            putValue(Action.NAME, s);
        }

        @Override public void actionPerformed(ActionEvent e) {
            DependenciesNode.prefs().putBoolean(SHOW_MANAGED_DEPENDENCIES, !showManagedState());
        }

        @Override public JMenuItem getPopupPresenter() {
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(this);
            mi.setSelected(showManagedState());
            return mi;
        }

    }
    @Messages({"BTN_Exclude_Dependency=Exclude Dependency",
              "TIT_Exclude=Exclude Transitive Dependency"})
    private class ExcludeTransitiveAction extends AbstractAction {

        public ExcludeTransitiveAction() {
            putValue(Action.NAME, BTN_Exclude_Dependency());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    org.apache.maven.shared.dependency.tree.DependencyNode rootnode = DependencyTreeFactory.createDependencyTree(project.getOriginalMavenProject(), EmbedderFactory.getOnlineEmbedder(), Artifact.SCOPE_TEST);
                    DependencyExcludeNodeVisitor nv = new DependencyExcludeNodeVisitor(art.getGroupId(), art.getArtifactId(), art.getType());
                    rootnode.accept(nv);
                    final Set<org.apache.maven.shared.dependency.tree.DependencyNode> nds = nv.getDirectDependencies();
                    Collection<org.apache.maven.shared.dependency.tree.DependencyNode> directs;
                    if (nds.size() > 1) {
                        final ExcludeDependencyPanel pnl = new ExcludeDependencyPanel(project.getOriginalMavenProject(), art, nds, rootnode);
                        DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Exclude());
                        Object ret = DialogDisplayer.getDefault().notify(dd);
                        if (ret == DialogDescriptor.OK_OPTION) {
                            directs = pnl.getDependencyExcludes().get(art);
                        } else {
                            return;
                        }
                    } else {
                        directs = nds;
                    }
                    runModifyExclusions(art, directs);
                }
            });
        }

        private void runModifyExclusions(final Artifact art, final Collection<org.apache.maven.shared.dependency.tree.DependencyNode> nds) {
            ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    for (org.apache.maven.shared.dependency.tree.DependencyNode nd : nds) {
                        Artifact directArt = nd.getArtifact();
                        org.netbeans.modules.maven.model.pom.Dependency dep = model.getProject().findDependencyById(directArt.getGroupId(), directArt.getArtifactId(), null);
                        if (dep == null) {
                            // now check the active profiles for the dependency..
                            List<String> profileNames = new ArrayList<String>();
                            Iterator it = project.getOriginalMavenProject().getActiveProfiles().iterator();
                            while (it.hasNext()) {
                                Profile prof = (Profile) it.next();
                                profileNames.add(prof.getId());
                            }
                            for (String profileId : profileNames) {
                                org.netbeans.modules.maven.model.pom.Profile modProf = model.getProject().findProfileById(profileId);
                                if (modProf != null) {
                                    dep = modProf.findDependencyById(directArt.getGroupId(), directArt.getArtifactId(), null);
                                    if (dep != null) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (dep == null) {
                            dep = model.getFactory().createDependency();
                            dep.setArtifactId(directArt.getArtifactId());
                            dep.setGroupId(directArt.getGroupId());
                            dep.setType(directArt.getType());
                            dep.setVersion(directArt.getVersion());
                            model.getProject().addDependency(dep);
                            //mkleint: TODO why is the dependency being added? i forgot already..
                        }
                        Exclusion ex = dep.findExclusionById(art.getGroupId(), art.getArtifactId());
                        if (ex == null) {
                            Exclusion exclude = model.getFactory().createExclusion();
                            exclude.setArtifactId(art.getArtifactId());
                            exclude.setGroupId(art.getGroupId());
                            dep.addExclusion(exclude);
                        }
                    }
                }
            };
            FileObject fo = FileUtil.toFileObject(project.getPOMFile());
            org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
        }
    }

    @SuppressWarnings("serial")
    private class DownloadJavadocSrcAction extends AbstractAction {
        private boolean javadoc;
        public DownloadJavadocSrcAction(boolean javadoc) {
            putValue(Action.NAME, javadoc ? LBL_Download_Javadoc() : LBL_Download__Sources());
            this.javadoc = javadoc;
        }

       
        
        @Override
        public void actionPerformed(ActionEvent evnt) {
            RP.post(new Runnable() {
                public @Override void run() {
                    ProgressContributor contributor =AggregateProgressFactory.createProgressContributor("multi-1");
                   
                    String label = javadoc ? Progress_Javadoc() : Progress_Source();
                    AggregateProgressHandle handle = AggregateProgressFactory.createHandle(label, 
                            new ProgressContributor [] {contributor}, ProgressTransferListener.cancellable(), null);
                    handle.start();
                    try {
                        ProgressTransferListener.setAggregateHandle(handle);

                        if (javadoc && !hasJavadocInRepository()) {
                            downloadJavadocSources(contributor, javadoc);
                        } else if (!javadoc && !hasSourceInRepository()) {
                            downloadJavadocSources(contributor, javadoc);
                        } else {
                            contributor.finish();
                        }
                        
                    } catch (ThreadDeath d) { // download interrupted
                    } catch (IllegalStateException ise) { //download interrupted in dependent thread. #213812
                        if (!(ise.getCause() instanceof ThreadDeath)) {
                            throw ise;
                        }
                    } finally {
                        handle.finish();
                        ProgressTransferListener.clearAggregateHandle();
                    }
                }
            });
        }
    } 
    
    //why oh why do we have to suffer through this??
    private static SetInCurrentAction SETINCURRENTINSTANCE = new SetInCurrentAction(Lookup.EMPTY);

    @Messages("BTN_Set_Dependency=Declare as Direct Dependency")
    private static class SetInCurrentAction extends AbstractAction  implements ContextAwareAction {
        private Lookup lkp;

        SetInCurrentAction(Lookup lookup) {
            putValue(Action.NAME, BTN_Set_Dependency());
            lkp = lookup;
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<NbMavenProjectImpl>(res);
            if (prjs.size() != 1) {
                setEnabled(false);
            }
        }
        
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new SetInCurrentAction(actionContext);
        }


        @Override
        public void actionPerformed(ActionEvent event) {
            final Collection<? extends Artifact> artifacts = lkp.lookupAll(Artifact.class);
            if (artifacts.isEmpty()) {
                return;
            }
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<NbMavenProjectImpl>(res);
            if (prjs.size() != 1) {
                return;
            }
            final NbMavenProjectImpl project = prjs.iterator().next();

            final ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    for (Artifact art : artifacts) {
                        org.netbeans.modules.maven.model.pom.Dependency dep = model.getProject().findDependencyById(art.getGroupId(), art.getArtifactId(), null);
                        if (dep == null) {
                            // now check the active profiles for the dependency..
                            List<String> profileNames = new ArrayList<String>();
                            Iterator it = project.getOriginalMavenProject().getActiveProfiles().iterator();
                            while (it.hasNext()) {
                                Profile prof = (Profile) it.next();
                                profileNames.add(prof.getId());
                            }
                            for (String profileId : profileNames) {
                                org.netbeans.modules.maven.model.pom.Profile modProf = model.getProject().findProfileById(profileId);
                                if (modProf != null) {
                                    dep = modProf.findDependencyById(art.getGroupId(), art.getArtifactId(), null);
                                    if (dep != null) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (dep == null) {
                            dep = model.getFactory().createDependency();
                            dep.setArtifactId(art.getArtifactId());
                            dep.setGroupId(art.getGroupId());
                            dep.setType(art.getType());
                            dep.setVersion(art.getVersion());
                            if (!Artifact.SCOPE_COMPILE.equals(art.getScope())) {
                                dep.setScope(art.getScope());
                            }
                            if (art.getClassifier() != null) {
                                dep.setClassifier(art.getClassifier());
                            }
                            model.getProject().addDependency(dep);
                        }
                    }
                }
            };
            RP.post(new Runnable() {
                @Override
                public void run() {
                    FileObject fo = FileUtil.toFileObject(project.getPOMFile());
                    org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
                }
            });
        }

    }


    @Messages("BTN_Manually_install=Manually install artifact")
    private class InstallLocalArtifactAction extends AbstractAction {

        public InstallLocalArtifactAction() {
            putValue(Action.NAME, BTN_Manually_install());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            File fil = InstallPanel.showInstallDialog(art);
            if (fil != null) {
                InstallPanel.runInstallGoal(project, fil, art);
            }
        }
    }

    @Messages("BTN_Add_javadoc=Add local Javadoc")
    private class InstallLocalJavadocAction extends AbstractAction implements Runnable {

        private File source;

        public InstallLocalJavadocAction() {
            putValue(Action.NAME, BTN_Add_javadoc());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            source = InstallDocSourcePanel.showInstallDialog(true);
            if (source != null) {
                RP.post(this);
            }
        }

        @Override
        public void run() {
            File target = getJavadocFile();
            try {
                FileUtils.copyFile(source, target);
            } catch (IOException ex) {
                ex.printStackTrace();
                target.delete();
            }
            refreshNode();
        }
    }

    @Messages("BTN_Add_sources=Add local sources")
    private class InstallLocalSourcesAction extends AbstractAction implements Runnable {

        private File source;

        public InstallLocalSourcesAction() {
            putValue(Action.NAME, BTN_Add_sources());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            source = InstallDocSourcePanel.showInstallDialog(false);
            if (source != null) {
                RP.post(this);
            }
        }

        @Override
        public void run() {
            File target = getSourceFile();
            try {
                FileUtils.copyFile(source, target);
            } catch (IOException ex) {
                ex.printStackTrace();
                target.delete();
            }
            refreshNode();
        }
    }

//    private class EditAction extends AbstractAction {
//        public EditAction() {
//            putValue(Action.NAME, "Edit...");
//        }
//
//        public void actionPerformed(ActionEvent event) {
//
//            DependencyEditor ed = new DependencyEditor(project, change);
//            DialogDescriptor dd = new DialogDescriptor(ed, "Edit Dependency");
//            Object ret = DialogDisplayer.getDefault().notify(dd);
//            if (ret == NotifyDescriptor.OK_OPTION) {
//                HashMap props = ed.getProperties();
//                MavenSettings.getDefault().checkDependencyProperties(props.keySet());
//                change.setNewValues(ed.getValues(), props);
//                try {
//                    NbProjectWriter writer = new NbProjectWriter(project);
//                    List changes = (List)getLookup().lookup(List.class);
//                    writer.applyChanges(changes);
//                } catch (Exception exc) {
//                    ErrorManager.getDefault().notify(ErrorManager.USER, exc);
//                }
//            }
//        }
//    }
//
    

    private static class ArtifactSourceGroup implements SourceGroup {

        private Artifact art;

        public ArtifactSourceGroup(Artifact art) {
            this.art = art;
        }

        @Override
        public FileObject getRootFolder() {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(art.getFile()));
            if (fo != null) {
                return FileUtil.getArchiveRoot(fo);
            }
            return null;
        }

        @Override
        public String getName() {
            return art.getId();
        }

        @Override
        public String getDisplayName() {
            return art.getId();
        }

        @Override
        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override public boolean contains(FileObject file) {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    private static class JarContentFilterChildren extends FilterNode.Children {

        JarContentFilterChildren(Node orig) {
            super(orig);
        }

        @Override
        protected Node copyNode(Node node) {
            return new JarFilterNode(node);
        }
    }

    private static class JarFilterNode extends FilterNode {

        JarFilterNode(Node original) {
            super(original, Children.LEAF == original.getChildren() ?
                            Children.LEAF : new JarContentFilterChildren(original));
        }

        @Override
        public Action[] getActions(boolean context) {
            List<Action> result = new ArrayList<Action>();
            result.addAll(Arrays.asList(super.getActions(false)));
            result.add(new OpenJavadocAction());

            return result.toArray(new Action[result.size()]);
        }

        @Messages("BTN_View_Javadoc=Show Javadoc")
        private class OpenJavadocAction extends AbstractAction {

            private OpenJavadocAction() {
                putValue(Action.NAME, BTN_View_Javadoc());
            }

            @Override
            @Messages("ERR_No_Javadoc_Found=Javadoc for {0} not found.")
            public void actionPerformed(ActionEvent e) {
                DataObject dobj = getOriginal().getLookup().lookup(DataObject.class);
                if (dobj == null) {
                    return;
                }
                FileObject fil = dobj.getPrimaryFile();
                FileObject jar = FileUtil.getArchiveFile(fil);
                FileObject root = FileUtil.getArchiveRoot(jar);
                String rel = FileUtil.getRelativePath(root, fil);
                rel = rel.replaceAll("[.]class$", ".html"); //NOI18N
                JavadocForBinaryQuery.Result res = JavadocForBinaryQuery.findJavadoc(root.toURL());
                if (fil.isFolder()) {
                    rel = rel + "/package-summary.html"; //NOI18N
                }
                URL javadocUrl = findJavadoc(rel, res.getRoots());
                if (javadocUrl != null) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(javadocUrl);
                } else {
                    StatusDisplayer.getDefault().setStatusText(ERR_No_Javadoc_Found(fil.getPath()));
                }
            }

            /**
             * Locates a javadoc page by a relative name and an array of javadoc roots
             * @param resource the relative name of javadoc page
             * @param urls the array of javadoc roots
             * @return the URL of found javadoc page or null if there is no such a page.
             */
            URL findJavadoc(String resource, URL[] urls) {
                for (int i = 0; i < urls.length; i++) {
                    String base = urls[i].toExternalForm();
                    if (!base.endsWith("/")) { // NOI18N
                        base = base + "/"; // NOI18N
                    }
                    try {
                        URL u = new URL(base + resource);
                        FileObject fo = URLMapper.findFileObject(u);
                        if (fo != null) {
                            return u;
                        }
                    } catch (MalformedURLException ex) {
//                            ErrorManager.getDefault().log(ErrorManager.ERROR, "Cannot create URL for "+base+resource+". "+ex.toString());   //NOI18N
                        continue;
                    }
                }
                return null;
            }

        }
    }

    private static class OpenProjectAction extends AbstractAction implements ContextAwareAction {

        static final OpenProjectAction SINGLETON = new OpenProjectAction();

        private OpenProjectAction() {}

        public @Override void actionPerformed(ActionEvent e) {
            assert false;
        }

        public @Override Action createContextAwareInstance(final Lookup context) {
            return new AbstractAction(BTN_Open_Project()) {
                public @Override void actionPerformed(ActionEvent e) {
                    Set<Project> projects = new HashSet<Project>();
                    for (Artifact art : context.lookupAll(Artifact.class)) {
                        File f = art.getFile();
                        if (f != null) {
                            Project p = FileOwnerQuery.getOwner(org.openide.util.Utilities.toURI(f));
                            if (p != null) {
                                projects.add(p);
                            }
                        }
                    }
                    OpenProjects.getDefault().open(projects.toArray(new NbMavenProjectImpl[projects.size()]), false, true);
                }
            };
        }
    }

}
