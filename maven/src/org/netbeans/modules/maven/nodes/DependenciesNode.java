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

import hidden.org.codehaus.plexus.util.StringUtils;
import java.awt.Image;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import javax.swing.JMenuItem;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.UIManager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
    
    DependenciesNode(DependenciesChildren childs, NbMavenProjectImpl mavproject, int type) {
        super(childs, Lookups.fixed(mavproject));
        setName("Dependencies" + type); //NOI18N
        this.type = type;
        switch (type) {
            case TYPE_COMPILE : setDisplayName(NbBundle.getMessage(DependenciesNode.class, "LBL_Libraries")); break;
            case TYPE_TEST : setDisplayName(NbBundle.getMessage(DependenciesNode.class, "LBL_Test_Libraries")); break;
            case TYPE_RUNTIME : setDisplayName(NbBundle.getMessage(DependenciesNode.class, "LBL_Runtime_Libraries")); break;
            default : setDisplayName(NbBundle.getMessage(DependenciesNode.class, "LBL_Libraries")); break;
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

        int nonCPcount = 0;

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
            Lookup look = Lookups.fixed(new Object[] {
                wr.getArtifact(),
                wr.getDependency(),
                project
            });
            return new Node[] { new DependencyNode(look, true) };
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
        
        @SuppressWarnings("unchecked") // a lot of calls to maven lists..
        int regenerateKeys() {
            TreeSet<DependencyWrapper> lst = new TreeSet<DependencyWrapper>(new DependenciesComparator());
            MavenProject mp = project.getOriginalMavenProject();
            if (type == TYPE_COMPILE) {
                lst.addAll(create(mp.getCompileDependencies(), mp.getArtifacts()));
            }
            if (type == TYPE_TEST) {
                lst.addAll(create(mp.getTestDependencies(), mp.getArtifacts()));
                int cnt = nonCPcount;
                nonCPcount = 0;
                lst.removeAll(create(mp.getCompileDependencies(), mp.getArtifacts()));
                lst.removeAll(create(mp.getRuntimeDependencies(), mp.getArtifacts()));
                nonCPcount = cnt - nonCPcount;
                if (nonCPcount == 0) {
                    lst.remove(NULL);
                } else {
                    lst.add(NULL);
                }
            }
            if (type == TYPE_RUNTIME) {
                lst.addAll(create(mp.getRuntimeDependencies(), mp.getArtifacts()));
                int cnt = nonCPcount;
                nonCPcount = 0;
                lst.removeAll(create(mp.getCompileDependencies(), mp.getArtifacts()));
                nonCPcount = cnt - nonCPcount;
                if (nonCPcount == 0) {
                    lst.remove(NULL);
                } else {
                    lst.add(NULL);
                }
            }
            setKeys(lst);
            return lst.size();
        }
        
        private Set<DependencyWrapper> create(Collection<Dependency> deps, Collection<Artifact> arts) {
            boolean nonCP = showNonClasspath() || showNonCP;
            int nonCPCount = 0;
            TreeSet<DependencyWrapper> lst = new TreeSet<DependencyWrapper>(new DependenciesComparator());
            for (Dependency d : deps) {
                boolean added = false;
                for (Artifact a : arts) {
                    if (a.getGroupId().equals(d.getGroupId()) &&
                          a.getArtifactId().equals(d.getArtifactId()) &&
                          StringUtils.equals(a.getClassifier(), d.getClassifier())) {
                        if (nonCP || a.getArtifactHandler().isAddedToClasspath()) {
                            lst.add(new DependencyWrapper(a, d));
                        } else {
                            nonCPCount = nonCPCount + 1;
                        }
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    System.out.println("not found artifact for " + d);
                }
            }
            if (nonCPCount > 0) {
                lst.add(NULL);
            }
            this.nonCPcount = nonCPCount;
            return lst;
        }

        public void preferenceChange(PreferenceChangeEvent evt) {
            if (SHOW_NONCLASSPATH_DEPENDENCIES.equals(evt.getKey())) {
                regenerateKeys();
            }
        }
    }
    
    private static final DependencyWrapper NULL = new DependencyWrapper(null, null);
    
    private static class DependencyWrapper {

        private Artifact artifact;
        private Dependency dependency;

        public DependencyWrapper(Artifact artifact, Dependency dependency) {
            this.artifact = artifact;
            this.dependency = dependency;
        }

        public Artifact getArtifact() {
            return artifact;
        }

        public Dependency getDependency() {
            return dependency;
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
    
    private class AddDependencyAction extends AbstractAction {
        public AddDependencyAction() {
            putValue(Action.NAME, NbBundle.getMessage(DependenciesNode.class, "BTN_Add_Library"));
        }

        public void actionPerformed(ActionEvent event) {
            AddDependencyPanel pnl = new AddDependencyPanel(project.getOriginalMavenProject(), project);
            String typeString = type == TYPE_RUNTIME ? "runtime" : (type == TYPE_TEST ? "test" : "compile"); //NOI18N
            pnl.setSelectedScope(typeString);
        
            pnl.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DependenciesNode.class, "TIT_Add_Library"));
            DialogDescriptor dd = new DialogDescriptor(pnl, NbBundle.getMessage(DependenciesNode.class, "TIT_Add_Library"));
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
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        project.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource();
                    }
                });
            }
        }
    }
    
 
    private class DownloadJavadocSrcAction extends AbstractAction {
        private boolean javadoc;
        public DownloadJavadocSrcAction(boolean javadoc) {
            putValue(Action.NAME, javadoc ? org.openide.util.NbBundle.getMessage(DependenciesNode.class, "LBL_Download_Javadoc") : org.openide.util.NbBundle.getMessage(DependenciesNode.class, "LBL_Download__Sources"));
            this.javadoc = javadoc;
        }
        
        public void actionPerformed(ActionEvent evnt) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
                    Node[] nds = getChildren().getNodes();
                    ProgressContributor[] contribs = new ProgressContributor[nds.length];
                    for (int i = 0; i < nds.length; i++) {
                        contribs[i] = AggregateProgressFactory.createProgressContributor("multi-" + i); //NOI18N
                    }
                    String label = javadoc ? NbBundle.getMessage(DependenciesNode.class, "Progress_Javadoc") : NbBundle.getMessage(DependenciesNode.class, "Progress_Source");
                    AggregateProgressHandle handle = AggregateProgressFactory.createHandle(label, 
                            contribs, null, null);
                    handle.start();
                    try {
                    ProgressTransferListener.setAggregateHandle(handle);
                    for (int i = 0; i < nds.length; i++) {
                        if (nds[i] instanceof DependencyNode) {
                            DependencyNode nd = (DependencyNode)nds[i];
                            if (javadoc && !nd.hasJavadocInRepository()) {
                                nd.downloadJavadocSources(online, contribs[i], javadoc);
                            } else if (!javadoc && !nd.hasSourceInRepository()) {
                                nd.downloadJavadocSources(online, contribs[i], javadoc);
                            } else {
                                contribs[i].finish();
                            }
                        }
                    }
                    } finally {
                        handle.finish();
                        ProgressTransferListener.clearAggregateHandle();
                    }
                }
            });
        }
    }  

    public static class ResolveDepsAction extends AbstractAction {
        private Project project;
        public ResolveDepsAction(Project prj) {
            putValue(Action.NAME, NbBundle.getMessage(DependenciesNode.class, "LBL_Download"));
            project = prj;
        }
        
        public void actionPerformed(ActionEvent evnt) {
            setEnabled(false);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    project.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource();
                }
            });
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
    
    private static class ShowClasspathDepsAction extends AbstractAction implements Presenter.Popup {

        public ShowClasspathDepsAction() {
//            String s = showNonClasspath() ?
//                NbBundle.getMessage(DependenciesNode.class, "LBL_HideNonClasspath") :
            String s = NbBundle.getMessage(DependenciesNode.class, "LBL_ShowNonClasspath");
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

    private class ShowManagedStateAction extends AbstractAction implements Presenter.Popup {

        public ShowManagedStateAction() {
            String s = NbBundle.getMessage(DependenciesNode.class, "LBL_ShowManagedState");
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

        @SuppressWarnings("unchecked")
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
        
        NonCPNode(int count, DependenciesChildren parent) {
            super(Children.LEAF);
            this.parent = parent;
            if (count == 1) {
                setDisplayName(NbBundle.getMessage(DependenciesNode.class, "LBL_NonCPCount1"));
            } else {
                setDisplayName(NbBundle.getMessage(DependenciesNode.class, "LBL_NonCPCount2", count));
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

    private static class ExpandAction extends AbstractAction {
        private DependenciesChildren parent;

        public ExpandAction(DependenciesChildren parent) {
            this.parent = parent;
            putValue(Action.NAME, NbBundle.getMessage(DependenciesNode.class, "LBL_Expand"));
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

