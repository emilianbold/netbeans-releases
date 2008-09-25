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

package org.netbeans.modules.maven.nodes;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.NbArtifact;
import org.netbeans.modules.maven.embedder.writer.WriterUtils;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import hidden.org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.EditAction;
import org.openide.actions.FindAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * node representing a dependency
 * @author  Milos Kleint 
 */
public class DependencyNode extends AbstractNode {
    private static final String JAVADOC_BADGE_ICON = "org/netbeans/modules/maven/DependencyJavadocIncluded.png"; //NOI18N
    private static final String MISSING_BADGE_ICON = "org/netbeans/modules/maven/ResourceNotIncluded.gif"; //NOI18N
    private static final String SOURCE_BADGE_ICON = "org/netbeans/modules/maven/DependencySrcIncluded.png"; //NOI18N

    private Artifact art;
    private NbMavenProjectImpl project;
    private boolean longLiving;
    private PropertyChangeListener listener;
    private ChangeListener listener2;
    
    private static String toolTipJavadoc = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(JAVADOC_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_JavadocBadge");//NOI18N
    private static String toolTipSource = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(SOURCE_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_SourceBadge");//NOI18N
    private static String toolTipMissing = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(MISSING_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_MissingBadge");//NOI18N
    

    public static Children createChildren(Lookup look, boolean longLiving) {
        if (!longLiving) {
            return Children.LEAF;
        }
        Artifact art = look.lookup(Artifact.class);
        if (art.getFile() != null) {//#135463
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(art.getFile()));
            if (fo != null && FileUtil.isArchiveFile(fo)) {
                return new JarContentFilterChildren(PackageView.createPackageView(new ArtifactSourceGroup(art)));
            }
        }
        return Children.LEAF;
    }

    /**
     *@param lookup - expects instance of NbMavenProjectImpl, Artifact
     */
    public DependencyNode(Lookup lookup, boolean isLongLiving) {
        super(createChildren(lookup, isLongLiving), lookup);
//        super(isLongLiving ? new DependencyChildren(lookup) : Children.LEAF, lookup);
        project = lookup.lookup(NbMavenProjectImpl.class);
        art = lookup.lookup(Artifact.class);
        assert art != null;
        if (art.getFile() == null) {
            throw new IllegalStateException("Artifact " + art.getId() + " is not resolved and is missing the file association in local repository. Please report at issue #140253."); //NOI18N
        }
        longLiving = isLongLiving;
        if (longLiving) {
            listener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                        refreshNode();
                    }
                }
            };
            NbMavenProject.addPropertyChangeListener(project, WeakListeners.propertyChange(listener, this));
            listener2 = new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    refreshNode();
                }
            };
            //TODO check if this one is a performance bottleneck.
            MavenFileOwnerQueryImpl.getInstance().addChangeListener(
                    WeakListeners.change(listener2,
                    MavenFileOwnerQueryImpl.getInstance()));
        }
        setDisplayName(createName());
        setIconBase();
    }

    /**
     * public because of the property sheet
     */
    public boolean isTransitive() {
        List trail = art.getDependencyTrail();
        return trail != null && trail.size() > 2;
    }

    private void setIconBase() {
        if (longLiving && isDependencyProjectOpen() && isTransitive()) {
            setIconBaseWithExtension("org/netbeans/modules/maven/TransitiveMaven2Icon.gif"); //NOI18N
        } else if (longLiving && isDependencyProjectOpen()) {
            setIconBaseWithExtension("org/netbeans/modules/maven/Maven2Icon.gif"); //NOI18N
        } else if (isTransitive()) {
            if (isAddedToCP()) {
                setIconBaseWithExtension("org/netbeans/modules/maven/TransitiveDependencyIcon.png"); //NOI18N
            } else {
                setIconBaseWithExtension("org/netbeans/modules/maven/TransitiveArtifactIcon.png"); //NOI18N
            }
        } else if (isAddedToCP()) { //NOI18N
            setIconBaseWithExtension("org/netbeans/modules/maven/DependencyIcon.png"); //NOI18N
        } else {
            setIconBaseWithExtension("org/netbeans/modules/maven/ArtifactIcon.png"); //NOI18N
        }
    }

    private boolean isAddedToCP() {
        return art.getArtifactHandler().isAddedToClasspath();
    }

    boolean isDependencyProjectOpen() {
        if ( Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
            return false;
        }
        URI uri = art.getFile().toURI();
//        URI  rootUri = project.getRepositoryRoot().getURL().toURI();
//        URI uri = rootUri.create(rootUri.toString() + "/" + project.getArtifactRelativeRepositoryPath(art));
        Project depPrj = MavenFileOwnerQueryImpl.getInstance().getOwner(uri);
        return depPrj != null;
    }

    
    public void refreshNode() {
        setDisplayName(createName());
        setIconBase();
        fireIconChange();
        fireDisplayNameChange(null, getDisplayName());
//        if (longLiving) {
//            ((DependencyChildren)getChildren()).doRefresh();
//        }
        //#142784
        if (longLiving) {
            if (Children.LEAF == getChildren()) {
                Children childs = createChildren(getLookup(), true);
                if (childs != Children.LEAF) {
                    setChildren(childs);
                }
            }
        }
    }

    @Override
    public String getHtmlDisplayName() {
        String version = ""; //NOI18N
        if (ArtifactUtils.isSnapshot(art.getVersion()) && art.getVersion().indexOf("SNAPSHOT") < 0) { //NOI18N
            version = " <b>[" + art.getVersion() + "]</b>"; //NOI18N
        }
        return "<html>" + getDisplayName() + version + ("compile".equalsIgnoreCase(art.getScope()) ? "" : "  <i>[" + art.getScope() + "]</i>") + "</html>"; // - not sure if shall ne translated..
    }

    private String createName() {
        if (art instanceof NbArtifact) {
            NbArtifact nb = (NbArtifact)art;
            if (nb.isFakedSystemDependency()) {
                return nb.getNonFakedFile().getName();
            }
            if (nb.isFakedPomDependency()) {
                return nb.getNonFakedFile().getName();
            }
        }
        return art.getFile().getName();
    }

    @Override
    public Action[] getActions(boolean context) {
        Collection<Action> acts = new ArrayList<Action>();
        if (isAddedToCP()) {
            InstallLocalArtifactAction act = new InstallLocalArtifactAction();
            acts.add(act);
            if (!isLocal()) {
                act.setEnabled(true);
            }
        }

//        acts.add(new EditAction());
//        acts.add(RemoveDepAction.get(RemoveDepAction.class));
//        acts.add(new DownloadJavadocAndSourcesAction());
        if (isAddedToCP() && !hasJavadocInRepository()) {
            acts.add(new InstallLocalJavadocAction());
        }
        if (isAddedToCP() && !hasSourceInRepository()) {
            acts.add(new InstallLocalSourcesAction());
        }
        if (isTransitive()) {
            acts.add(new ExcludeTransitiveAction());
        } else {
            acts.add(new RemoveDependencyAction());
        }
        acts.add(null);
        acts.add(CommonArtifactActions.createFindUsages(art));
        acts.add(null);
        acts.add(CommonArtifactActions.createViewJavadocAction(art));
        acts.add(CommonArtifactActions.createViewProjectHomeAction(art));
        acts.add(CommonArtifactActions.createViewBugTrackerAction(art));
        acts.add(CommonArtifactActions.createSCMActions(art));
        acts.add(null);
        acts.add(PropertiesAction.get(PropertiesAction.class));
        return acts.toArray(new Action[acts.size()]);
    }

    @Override
    public boolean canDestroy() {
        return true;
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
        if (art instanceof NbArtifact) {
            NbArtifact nb = (NbArtifact) art;
            if (nb.isFakedSystemDependency()) {
                return false;
            }
        }
        return art.getFile().exists();
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

    void downloadJavadocSources(MavenEmbedder online, ProgressContributor progress, boolean isjavadoc) {
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
        Image retValue;
        retValue = super.getIcon(param);
        if (isLocal()) {
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
            return retValue;
        } else {
            Image ann = ImageUtilities.loadImage(MISSING_BADGE_ICON); //NOI18N
            ann = ImageUtilities.addToolTipToImage(ann, toolTipMissing);
            return ImageUtilities.mergeImages(retValue, ann, 0, 0);//NOI18N
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        Image retValue;
        retValue = super.getOpenedIcon(type);
        if (isLocal()) {
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
            return retValue;
        } else {
            Image ann = ImageUtilities.loadImage(MISSING_BADGE_ICON); //NOI18N
            ann = ImageUtilities.addToolTipToImage(ann, toolTipMissing);
            return ImageUtilities.mergeImages(retValue, ann, 0, 0);//NOI18N
        }
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
//            RequestProcessor.getDefault().post(this);
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

    private class RemoveDependencyAction extends AbstractAction {

        public RemoveDependencyAction() {
            putValue(Action.NAME, org.openide.util.NbBundle.getMessage(DependencyNode.class, "BTN_Remove_Dependency"));
        }

        public void actionPerformed(ActionEvent event) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(DependencyNode.class, "MSG_Remove_Dependency", art.getGroupId() + ":" + art.getArtifactId()), NbBundle.getMessage(DependencyNode.class, "TIT_Remove_Dependency")); //NOI18N
            nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret != NotifyDescriptor.YES_OPTION) {
                return;
            }

            MavenProject mproject = project.getOriginalMavenProject();
            boolean found = false;
            while (mproject != null) {
                if (mproject.getDependencies() != null) {
                    Iterator it = mproject.getDependencies().iterator();
                    while (it.hasNext()) {
                        Dependency dep = (Dependency) it.next();
                        if (art.getArtifactId().equals(dep.getArtifactId())
                                && art.getGroupId().equals(dep.getGroupId())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (found) {
                    break;
                }
                mproject = mproject.getParent();
            }
            if (mproject == null) {
                //how come..
                StatusDisplayer.getDefault().setStatusText(org.openide.util.NbBundle.getMessage(DependencyNode.class, "ERR_Cannot_Locate_Dep"));
                return;
            }
            if (mproject != project.getOriginalMavenProject()) {
                //TODO warn that we are to modify the parent pom.
                nd = new NotifyDescriptor.Confirmation(org.openide.util.NbBundle.getMessage(DependencyNode.class, "MSG_Located_In_Parent"), org.openide.util.NbBundle.getMessage(DependencyNode.class, "TIT_Located_In_Parent"));
                nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
                ret = DialogDisplayer.getDefault().notify(nd);
                if (ret != NotifyDescriptor.YES_OPTION) {
                    return;
                }
            }
            try {
                File fil = mproject.getFile();
                Model model = EmbedderFactory.getProjectEmbedder().readModel(fil);
                Iterator it = model.getDependencies().iterator();
                while (it.hasNext()) {
                    Dependency dep = (Dependency) it.next();
                    if (art.getArtifactId().equals(dep.getArtifactId()) && art.getGroupId().equals(dep.getGroupId())) {
                        model.removeDependency(dep);
                        break;
                    }
                }
                WriterUtils.writePomModel(FileUtil.toFileObject(project.getPOMFile()), model);
                NbMavenProject.fireMavenProjectReload(project);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (XmlPullParserException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class ExcludeTransitiveAction extends AbstractAction {

        public ExcludeTransitiveAction() {
            putValue(Action.NAME, org.openide.util.NbBundle.getMessage(DependencyNode.class, "BTN_Exclude_Dependency"));
        }

        public void actionPerformed(ActionEvent event) {
            try {
                List trail = art.getDependencyTrail();
                String str = (String) trail.get(1);
                StringTokenizer tok = new StringTokenizer(str, ":"); //NOI18N
                String groupId = tok.nextToken();
                String artifactId = tok.nextToken();
                File fil = DependencyNode.this.project.getPOMFile();
                Model model = EmbedderFactory.getProjectEmbedder().readModel(fil);
                Dependency dep = null;
                if (model.getDependencies() != null) {
                    Iterator it = model.getDependencies().iterator();
                    while (it.hasNext()) {
                        Dependency dependency = (Dependency) it.next();
                        if (groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId())) {
                            dep = dependency;
                            break;
                        }
                    }
                }
                if (dep == null) {
                    // now check the active profiles for the dependency..
                    List profileNames = new ArrayList();
                    Iterator it = project.getOriginalMavenProject().getActiveProfiles().iterator();
                    while (it.hasNext()) {
                        Profile prof = (Profile) it.next();
                        profileNames.add(prof.getId());
                    }
                    it = model.getProfiles().iterator();
                    while (it.hasNext()) {
                        Profile profile = (Profile) it.next();
                        if (profileNames.contains(profile.getId())) {
                            List lst = profile.getDependencies();
                            if (lst != null) {
                                Iterator it2 = lst.iterator();
                                while (it2.hasNext()) {
                                    Dependency dependency = (Dependency) it2.next();
                                    if (groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId())) {
                                        dep = dependency;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (dep == null) {
                    dep = new Dependency();
                    dep.setArtifactId(artifactId);
                    dep.setGroupId(groupId);
                    dep.setType(tok.nextToken());
                    dep.setVersion(tok.nextToken());
                    model.addDependency(dep);
                }
                Exclusion exclude = new Exclusion();
                exclude.setArtifactId(art.getArtifactId());
                exclude.setGroupId(art.getGroupId());
                dep.addExclusion(exclude);
                WriterUtils.writePomModel(FileUtil.toFileObject(project.getPOMFile()), model);
                NbMavenProject.fireMavenProjectReload(project);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (XmlPullParserException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class InstallLocalArtifactAction extends AbstractAction {

        public InstallLocalArtifactAction() {
            putValue(Action.NAME, org.openide.util.NbBundle.getMessage(DependencyNode.class, "BTN_Manually_install"));
        }

        public void actionPerformed(ActionEvent event) {
            File fil = InstallPanel.showInstallDialog(art);
            if (fil != null) {
                InstallPanel.runInstallGoal(project, fil, art);
            }
        }
    }

    private class InstallLocalJavadocAction extends AbstractAction implements Runnable {

        private File source;

        public InstallLocalJavadocAction() {
            putValue(Action.NAME, org.openide.util.NbBundle.getMessage(DependencyNode.class, "BTN_Add_javadoc"));
        }

        public void actionPerformed(ActionEvent event) {
            source = InstallDocSourcePanel.showInstallDialog(true);
            if (source != null) {
                RequestProcessor.getDefault().post(this);
            }
        }

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

    private class InstallLocalSourcesAction extends AbstractAction implements Runnable {

        private File source;

        public InstallLocalSourcesAction() {
            putValue(Action.NAME, org.openide.util.NbBundle.getMessage(DependencyNode.class, "BTN_Add_sources"));
        }

        public void actionPerformed(ActionEvent event) {
            source = InstallDocSourcePanel.showInstallDialog(false);
            if (source != null) {
                RequestProcessor.getDefault().post(this);
            }
        }

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

        public FileObject getRootFolder() {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(art.getFile()));
            if (fo != null) {
                return FileUtil.getArchiveRoot(fo);
            }
            return null;
        }

        public String getName() {
            return art.getId();
        }

        public String getDisplayName() {
            return art.getId();
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

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
            DataObject dobj = getOriginal().getLookup().lookup(DataObject.class);
            List<Action> result = new ArrayList<Action>();
            Action[] superActions = super.getActions(false);
            boolean hasOpen = false;
            for (int i = 0; i < superActions.length; i++) {
                if ((superActions[i] instanceof OpenAction || superActions[i] instanceof EditAction)
                        && !dobj.getClass().getName().contains("ClassDataObject")) {//NOI18N #148053
                    result.add(superActions[i]);
                    hasOpen = true;
                }
                if (dobj != null && dobj.getPrimaryFile().isFolder() && superActions[i] instanceof FindAction) {
                    result.add(superActions[i]);
                }
            }
            if (!hasOpen) { //necessary? maybe just keep around for all..
                result.add(new OpenSrcAction(false));
            }
            result.add(new OpenSrcAction(true));

            return result.toArray(new Action[result.size()]);
        }

        @Override
        public Action getPreferredAction() {
            return new OpenSrcAction(false);
        }

        private class OpenSrcAction extends AbstractAction {

            private boolean javadoc;

            private OpenSrcAction(boolean javadoc) {
                this.javadoc = javadoc;
                if (javadoc) {
                    putValue(Action.NAME, NbBundle.getMessage(DependencyNode.class, "BTN_View_Javadoc"));
                } else {
                    putValue(NAME, NbBundle.getMessage(DependencyNode.class, "BTN_View_Source"));
                }
            }

            public void actionPerformed(ActionEvent e) {
                DataObject dobj = getOriginal().getLookup().lookup(DataObject.class);
                if (dobj != null && (javadoc || !dobj.getPrimaryFile().isFolder())) {
                    try {
                        FileObject fil = dobj.getPrimaryFile();
                        FileObject jar = FileUtil.getArchiveFile(fil);
                        FileObject root = FileUtil.getArchiveRoot(jar);
                        String rel = FileUtil.getRelativePath(root, fil);
                        if (rel.endsWith(".class")) { //NOI18N
                            rel = rel.replaceAll("class$", javadoc ? "html" : "java"); //NOI18N
                        }
                        if (javadoc) {
                            JavadocForBinaryQuery.Result res = JavadocForBinaryQuery.findJavadoc(root.getURL());
                            if (fil.isFolder()) {
                                rel = rel + "/package-summary.html"; //NOI18N
                            }
                            URL javadocUrl = findJavadoc(rel, res.getRoots());
                            if (javadocUrl != null) {
                                HtmlBrowser.URLDisplayer.getDefault().showURL(javadocUrl);
                            } else {
                                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DependencyNode.class, "ERR_No_Javadoc_Found", fil.getPath()));
                            }
                            return;
                        } else {
                            SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(root.getURL());
                            for (FileObject srcRoot : res.getRoots()) {
                                FileObject src = srcRoot.getFileObject(rel);
                                if (src != null) {
                                    DataObject dobj2 = DataObject.find(src);
                                    if (tryOpen(dobj2)) {
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    //applies to  show source only..
                    tryOpen(dobj);
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

            private boolean tryOpen(DataObject dobj2) {
                EditCookie ec = dobj2.getLookup().lookup(EditCookie.class);
                if (ec != null) {
                    ec.edit();
                    return true;
                } else {
                    OpenCookie oc = dobj2.getLookup().lookup(OpenCookie.class);
                    if (oc != null) {
                        oc.open();
                        return true;
                    }
                }
                return false;
            }
        }
    }
}