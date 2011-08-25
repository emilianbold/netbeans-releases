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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * root node for dependencies in project's view.
 * @author  Milos Kleint
 */
public class DependenciesNode extends AbstractNode {
    static final int TYPE_COMPILE = 0;
    static final int TYPE_TEST = 1;
    static final int TYPE_RUNTIME = 2;
    private static final String SHOW_NONCLASSPATH_DEPENDENCIES = "show.nonclasspath.dependencies"; //NOI18N
    private static final String SHOW_MANAGED_DEPENDENCIES = "show.managed.dependencies"; //NOI18N
    public static final String PREF_DEPENDENCIES_UI = "org/netbeans/modules/maven/dependencies/ui"; //NOI18N
    
    private NbMavenProjectImpl project;
    private int type;

    @Messages({
        "LBL_Libraries=Dependencies",
        "LBL_Test_Libraries=Test Dependencies",
        "LBL_Runtime_Libraries=Runtime Dependencies"
    })
    DependenciesNode(DependenciesChildren childs, NbMavenProjectImpl mavproject, int type) {
        super(childs, Lookups.fixed(mavproject));
        setName("Dependencies" + type); //NOI18N
        this.type = type;
        switch (type) {
            case TYPE_COMPILE : setDisplayName(LBL_Libraries()); break;
            case TYPE_TEST : setDisplayName(LBL_Test_Libraries()); break;
            case TYPE_RUNTIME : setDisplayName(LBL_Runtime_Libraries()); break;
            default : setDisplayName(LBL_Libraries()); break;
        }
        project = mavproject;
        setIconBaseWithExtension("org/netbeans/modules/maven/defaultFolder.gif"); //NOI18N
    }
    
    @Override
    public Image getIcon(int param) {
        Image retValue = ImageUtilities.mergeImages(getTreeFolderIcon(false),
                ImageUtilities.loadImage("org/netbeans/modules/maven/libraries-badge.png"), //NOI18N
                8, 8);
        return retValue;
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        Image retValue = ImageUtilities.mergeImages(getTreeFolderIcon(true),
                ImageUtilities.loadImage("org/netbeans/modules/maven/libraries-badge.png"), //NOI18N
                8, 8);
        return retValue;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> toRet = new ArrayList<Action>();
        toRet.add(new AddDependencyAction());
        toRet.add(null);
        toRet.add(new ResolveDepsAction(project));
        toRet.add(new DownloadJavadocSrcAction(true));
        toRet.add(new DownloadJavadocSrcAction(false));
        toRet.addAll(Utilities.actionsForPath("Projects/org-netbeans-modules-maven/DependenciesActions")); //NOI18N
        toRet.add(null);
        toRet.add(new ShowClasspathDepsAction());
        toRet.add(new ShowManagedStateAction());
        return toRet.toArray(new Action[toRet.size()]);
    }
    
    static class DependenciesChildren extends Children.Keys<DependencyWrapper> implements PropertyChangeListener, PreferenceChangeListener {
        private NbMavenProjectImpl project;
        private int type;
        boolean showNonCP = false;
        private int nonCPcount;


        public DependenciesChildren(NbMavenProjectImpl proj, int type) {
            super();
            project = proj;
            this.type = type;
        }
        
        public void showNonCP() {
            showNonCP = true;
            regenerateKeys();
            refresh();
            refreshKey(NULL);
        }
        
        protected Node[] createNodes(DependencyWrapper wr) {
            if (wr == NULL) {
                return new Node[] {new NonCPNode(nonCPcount, this)};
            }
            Artifact art = wr.getArtifact();
            if (art.getFile() == null) { // #140253
                Node n = new AbstractNode(Children.LEAF);
                n.setName("No such artifact: " + art); // XXX I18N
                return new Node[] {n};
            }
            return new Node[] {new DependencyNode(project, art, true)};
        }

        Node getParentNode() {
            return getNode();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                //was refreshed by the NodeList already..
                regenerateKeys();
                refresh();
                refreshKey(NULL);
            }
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            NbMavenProject.addPropertyChangeListener(project, this);
            Preferences prefs = NbPreferences.root().node(PREF_DEPENDENCIES_UI); //NOI18N
            prefs.addPreferenceChangeListener(this);
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<DependencyWrapper>emptyList());
            NbMavenProject.removePropertyChangeListener(project, this);
            Preferences prefs = NbPreferences.root().node(PREF_DEPENDENCIES_UI); //NOI18N
            prefs.removePreferenceChangeListener(this);
            super.removeNotify();
        }
        
        int regenerateKeys() {
            TreeSet<DependencyWrapper> lst = new TreeSet<DependencyWrapper>(new DependenciesComparator());
            MavenProject mp = project.getOriginalMavenProject();
            Set<Artifact> arts = mp.getArtifacts();
            if (type == TYPE_COMPILE) {
                Tuple t = create(arts, Artifact.SCOPE_COMPILE, Artifact.SCOPE_PROVIDED, Artifact.SCOPE_SYSTEM);
                lst.addAll(t.wrappers);
                nonCPcount = t.nonCpCount;
                if (t.nonCpCount > 0) {
                    lst.add(NULL);
                }
            }
            if (type == TYPE_TEST) {
                Tuple t = create(arts, Artifact.SCOPE_TEST);
                lst.addAll(t.wrappers);
                nonCPcount = t.nonCpCount;
                if (t.nonCpCount <= 0) {
                    lst.remove(NULL);
                } else {
                    lst.add(NULL);
                }
            }
            if (type == TYPE_RUNTIME) {
                Tuple t = create(arts, Artifact.SCOPE_RUNTIME);
                lst.addAll(t.wrappers);
                nonCPcount = t.nonCpCount;
                if (t.nonCpCount <= 0) {
                    lst.remove(NULL);
                } else {
                    lst.add(NULL);
                }
            }
            setKeys(lst);
            return lst.size();
        }
        
        private class Tuple {
            Set<DependencyWrapper> wrappers;
            int nonCpCount;

            private Tuple(TreeSet<DependencyWrapper> lst, int nonCPCount) {
                wrappers = lst;
                nonCpCount = nonCPCount;
            }
        }

        private void fixFile(Artifact a) {
            if (a.getFile() == null) {
                ArtifactRepository local = project.getEmbedder().getLocalRepository();
                String path = local.pathOf(a);
                if (!path.endsWith('.' + a.getType())) {
                    // XXX why does this happen? just for fake artifacts
                    path += '.' + a.getType();
                }
                File f = new File(local.getBasedir(), path);
                a.setFile(f);
            }
        }

        private Tuple create(Collection<Artifact> arts, String... scopes) {
            boolean nonCP = showNonClasspath() || showNonCP;
            int nonCPCount = 0;
            TreeSet<DependencyWrapper> lst = new TreeSet<DependencyWrapper>(new DependenciesComparator());
            for (Artifact a : arts) {
                if (!Arrays.asList(scopes).contains(a.getScope())) {
                    continue;
                }
                fixFile(a); // will be null if *any* dependency artifacts are missing, for some reason
                if (nonCP || a.getArtifactHandler().isAddedToClasspath()) {
                    lst.add(new DependencyWrapper(a));
                } else {
                    nonCPCount = nonCPCount + 1;
                }
            }
            return new Tuple(lst, nonCPCount);
        }

        public void preferenceChange(PreferenceChangeEvent evt) {
            if (SHOW_NONCLASSPATH_DEPENDENCIES.equals(evt.getKey())) {
                regenerateKeys();
            }
        }
    }
    
    private static final DependencyWrapper NULL = new DependencyWrapper(null);
    
    private static class DependencyWrapper {

        private Artifact artifact;

        public DependencyWrapper(Artifact artifact) {
            this.artifact = artifact;
        }

        public Artifact getArtifact() {
            return artifact;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == NULL && obj == NULL) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DependencyWrapper other = (DependencyWrapper) obj;
            if (this.artifact != other.artifact && (this.artifact == null || !this.artifact.equals(other.artifact))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            if (this == NULL) {
                return hash;
            }
            hash = 23 * hash + (this.artifact != null ? this.artifact.hashCode() : 0);
            return hash;
        }
        
    }
    
    @SuppressWarnings("serial")
    private class AddDependencyAction extends AbstractAction {

        @Messages("BTN_Add_Library=Add Dependency...")
        AddDependencyAction() {
            putValue(Action.NAME, BTN_Add_Library());
        }

        @Messages("TIT_Add_Library=Add Dependency")
        public void actionPerformed(ActionEvent event) {
            AddDependencyPanel pnl = new AddDependencyPanel(project.getOriginalMavenProject(), true, project);
            String typeString = type == TYPE_RUNTIME ? "runtime" : (type == TYPE_TEST ? "test" : "compile"); //NOI18N
            pnl.setSelectedScope(typeString);
        
            pnl.getAccessibleContext().setAccessibleDescription(TIT_Add_Library());
            DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Add_Library());
            dd.setClosingOptions(new Object[] {
                pnl.getOkButton(),
                DialogDescriptor.CANCEL_OPTION
            });
            dd.setOptions(new Object[] {
                pnl.getOkButton(),
                DialogDescriptor.CANCEL_OPTION
            });
            pnl.attachDialogDisplayer(dd);
            Object ret = DialogDisplayer.getDefault().notify(dd);
            if (pnl.getOkButton() == ret) {
                String version = pnl.getVersion();
                if (version != null && version.trim().length() == 0) {
                    version = null;
                }
                ModelUtils.addDependency(project.getProjectDirectory().getFileObject("pom.xml")/*NOI18N*/,
                       pnl.getGroupId(), pnl.getArtifactId(), version,
                       null, pnl.getScope(), null,false);
                project.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource(false);
            }
        }
    }
    
 
    private static final RequestProcessor RP = new RequestProcessor(DependenciesNode.class);
    @SuppressWarnings("serial")
    private class DownloadJavadocSrcAction extends AbstractAction {

        private boolean javadoc;
        
        @Messages({
            "LBL_Download_Javadoc=Download Javadoc",
            "LBL_Download__Sources=Download Sources"
        })
        DownloadJavadocSrcAction(boolean javadoc) {
            putValue(Action.NAME, javadoc ? LBL_Download_Javadoc() : LBL_Download__Sources());
            this.javadoc = javadoc;
        }
        
        @Messages({
            "Progress_Javadoc=Downloading Javadoc",
            "Progress_Source=Downloading Sources"
        })
        @Override public void actionPerformed(ActionEvent evnt) {
            RP.post(new Runnable() {
                public void run() {
                    Node[] nds = getChildren().getNodes();
                    ProgressContributor[] contribs = new ProgressContributor[nds.length];
                    for (int i = 0; i < nds.length; i++) {
                        contribs[i] = AggregateProgressFactory.createProgressContributor("multi-" + i); //NOI18N
                    }
                    String label = javadoc ? Progress_Javadoc() : Progress_Source();
                    AggregateProgressHandle handle = AggregateProgressFactory.createHandle(label, 
                            contribs, ProgressTransferListener.cancellable(), null);
                    handle.start();
                    try {
                    ProgressTransferListener.setAggregateHandle(handle);
                    for (int i = 0; i < nds.length; i++) {
                        if (nds[i] instanceof DependencyNode) {
                            DependencyNode nd = (DependencyNode)nds[i];
                            if (javadoc && !nd.hasJavadocInRepository()) {
                                nd.downloadJavadocSources(contribs[i], javadoc);
                            } else if (!javadoc && !nd.hasSourceInRepository()) {
                                nd.downloadJavadocSources(contribs[i], javadoc);
                            } else {
                                contribs[i].finish();
                            }
                        }
                    }
                    } catch (ThreadDeath d) { // download interrupted
                    } finally {
                        handle.finish();
                        ProgressTransferListener.clearAggregateHandle();
                    }
                }
            });
        }
    }  

    @SuppressWarnings("serial")
    private static class ResolveDepsAction extends AbstractAction {

        private Project project;

        @Messages("LBL_Download=Download Declared Dependencies")
        ResolveDepsAction(Project prj) {
            putValue(Action.NAME, LBL_Download());
            project = prj;
        }
        
        public void actionPerformed(ActionEvent evnt) {
            setEnabled(false);
            project.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource(false);
        }
    }
    
    private static boolean showNonClasspath() {
        Preferences prefs = NbPreferences.root().node(PREF_DEPENDENCIES_UI); //NOI18N
        boolean b = prefs.getBoolean(SHOW_NONCLASSPATH_DEPENDENCIES, false); //NOI18N
        return b;
    }

    static boolean showManagedState() {
        Preferences prefs = NbPreferences.root().node(PREF_DEPENDENCIES_UI); //NOI18N
        boolean b = prefs.getBoolean(SHOW_MANAGED_DEPENDENCIES, false); //NOI18N
        return b;
    }
    
    @SuppressWarnings("serial")
    private static class ShowClasspathDepsAction extends AbstractAction implements Presenter.Popup {

        @Messages("LBL_ShowNonClasspath=Always show non-classpath dependencies")
        ShowClasspathDepsAction() {
            String s = LBL_ShowNonClasspath();
            putValue(Action.NAME, s);
        }

        public void actionPerformed(ActionEvent e) {
            boolean b = showNonClasspath();
            Preferences prefs = NbPreferences.root().node(PREF_DEPENDENCIES_UI); //NOI18N
            prefs.putBoolean(SHOW_NONCLASSPATH_DEPENDENCIES, !b); //NOI18N
            try {
                prefs.flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public JMenuItem getPopupPresenter() {
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(this);
            mi.setSelected(showNonClasspath());
            return mi;
        }
        
    }

    @SuppressWarnings("serial")
    private class ShowManagedStateAction extends AbstractAction implements Presenter.Popup {

        @Messages("LBL_ShowManagedState=Show Managed State for dependencies")
        ShowManagedStateAction() {
            String s = LBL_ShowManagedState();
            putValue(Action.NAME, s);
        }

        public void actionPerformed(ActionEvent e) {
            boolean b = showManagedState();
            Preferences prefs = NbPreferences.root().node(PREF_DEPENDENCIES_UI); //NOI18N
            prefs.putBoolean(SHOW_MANAGED_DEPENDENCIES, !b); //NOI18N
            try {
                prefs.flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
            for (Node nd : getChildren().getNodes(true)) {
                if (nd instanceof DependencyNode) {
                    ((DependencyNode)nd).refreshNode();
                }
            }
        }

        public JMenuItem getPopupPresenter() {
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(this);
            mi.setSelected(showManagedState());
            return mi;
        }

    }

    
    private static class DependenciesComparator implements Comparator<DependencyWrapper> {

        public int compare(DependencyWrapper art1, DependencyWrapper art2) {
            if (art1 == NULL && art2 == NULL) {
                return 0;
            }
            if (art1 == NULL) {
                return -1;
            }
            if (art2 == NULL) {
                return 1;
            }
            boolean transitive1 = art1.getArtifact().getDependencyTrail().size() > 2;
            boolean transitive2 = art2.getArtifact().getDependencyTrail().size() > 2;
            if (transitive1 && !transitive2) {
                return 1;
            }
            if (!transitive1 && transitive2)  {
                return -1;
            }
            int ret = art1.getArtifact().getArtifactId().compareTo(art2.getArtifact().getArtifactId());
            if (ret != 0) {
                return ret;
            }
            return art1.getArtifact().compareTo(art2.getArtifact());
        }
        
    }
    
    private static class NonCPNode extends AbstractNode {
        private DependenciesChildren parent;
        
        @Messages({
            "LBL_NonCPCount1=There is 1 non-classpath dependency.",
            "LBL_NonCPCount2=There are {0} non-classpath dependencies."
        })
        NonCPNode(int count, DependenciesChildren parent) {
            super(Children.LEAF);
            this.parent = parent;
            if (count == 1) {
                setDisplayName(LBL_NonCPCount1());
            } else {
                setDisplayName(LBL_NonCPCount2(count));
            }
        }

        @Override
        public Action getPreferredAction() {
            return new ExpandAction(parent);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                new ExpandAction(parent)
            };
        }
    }

    @SuppressWarnings("serial")
    private static class ExpandAction extends AbstractAction {
        private DependenciesChildren parent;

        @Messages("LBL_Expand=Expand dependencies")
        ExpandAction(DependenciesChildren parent) {
            this.parent = parent;
            putValue(Action.NAME, LBL_Expand());
        }

        public void actionPerformed(ActionEvent e) {
            parent.showNonCP();
        }

    }
    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    private static final String ICON_PATH = "org/netbeans/modules/maven/defaultFolder.gif"; // NOI18N
    private static final String OPENED_ICON_PATH = "org/netbeans/modules/maven/defaultFolderOpen.gif"; // NOI18N
    
    /**
     * Returns default folder icon as {@link java.awt.Image}. Never returns
     * <code>null</code>.
     *
     * @param opened wheter closed or opened icon should be returned.
     * 
     * copied from apisupport/project
     */
    public static Image getTreeFolderIcon(boolean opened) {
        Image base = null;
        Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
        if (baseIcon != null) {
            base = ImageUtilities.icon2Image(baseIcon);
        } else {
            base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263
            if (base == null) { // fallback to our owns
                base = ImageUtilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, true);
            }
        }
        assert base != null;
        return base;
    }
    
}

