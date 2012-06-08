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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
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
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * root node for dependencies in project's view.
 * @author  Milos Kleint
 */
public class DependenciesNode extends AbstractNode {
    enum Type {COMPILE, TEST, RUNTIME, /** any scope */NONCP}
    public static final String PREF_DEPENDENCIES_UI = "org/netbeans/modules/maven/dependencies/ui"; //NOI18N
    
    private final DependenciesSet dependencies;

    @Messages({
        "LBL_Libraries=Dependencies",
        "LBL_Test_Libraries=Test Dependencies",
        "LBL_Runtime_Libraries=Runtime Dependencies",
        "LBL_non_cp_libraries=Non-classpath Dependencies"
    })
    DependenciesNode(DependenciesSet dependencies) {
        super(Children.create(new DependenciesChildren(dependencies), true), Lookups.fixed(dependencies.project));
        this.dependencies = dependencies;
        setName("Dependencies" + dependencies.type); //NOI18N
        switch (dependencies.type) {
            case COMPILE : setDisplayName(LBL_Libraries()); break;
            case TEST : setDisplayName(LBL_Test_Libraries()); break;
            case RUNTIME : setDisplayName(LBL_Runtime_Libraries()); break;
            default : setDisplayName(LBL_non_cp_libraries()); break;
        }
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
        toRet.add(new ResolveDepsAction(dependencies.project));
        toRet.add(new DownloadJavadocSrcAction(true));
        toRet.add(new DownloadJavadocSrcAction(false));
        toRet.addAll(Utilities.actionsForPath("Projects/org-netbeans-modules-maven/DependenciesActions")); //NOI18N
        toRet.add(null);
        toRet.add(new DependencyNode.ShowManagedStateAction());
        return toRet.toArray(new Action[toRet.size()]);
    }
    
    private static class DependenciesChildren extends ChildFactory<DependencyWrapper> implements ChangeListener {

        private final DependenciesSet dependencies;

        @SuppressWarnings("LeakingThisInConstructor")
        DependenciesChildren(DependenciesSet dependencies) {
            this.dependencies = dependencies;
            dependencies.addChangeListener(WeakListeners.change(this, dependencies));
        }
        
        @Override
        protected Node createNodeForKey(DependencyWrapper wr) {
            Artifact art = wr.getArtifact();
            if (art.getFile() == null) { // #140253
                Node n = new AbstractNode(Children.LEAF);
                n.setName("No such artifact: " + art); // XXX I18N
                return n;
            }
            return new DependencyNode(dependencies.project, art, true);
        }

        @Override public void stateChanged(ChangeEvent e) {
            refresh(false);
        }
        
        @Override protected boolean createKeys(List<DependencyWrapper> toPopulate) {
            toPopulate.addAll(dependencies.list());
            return true;
        }

    }

    static final class DependenciesSet implements PropertyChangeListener {

        private NbMavenProjectImpl project;
        private final Type type;
        private final ChangeSupport cs = new ChangeSupport(this);

        @SuppressWarnings("LeakingThisInConstructor")
        DependenciesSet(NbMavenProjectImpl project, Type type) {
            this.project = project;
            this.type = type;
            NbMavenProject nbmp = project.getProjectWatcher();
            nbmp.addPropertyChangeListener(WeakListeners.propertyChange(this, nbmp));
        }

        Collection<DependencyWrapper> list() {
            TreeSet<DependencyWrapper> lst = new TreeSet<DependencyWrapper>(new DependenciesComparator());
            MavenProject mp = project.getOriginalMavenProject();
            Set<Artifact> arts = mp.getArtifacts();
            switch (type) {
            case COMPILE:
                create(lst, arts, Artifact.SCOPE_COMPILE, Artifact.SCOPE_PROVIDED, Artifact.SCOPE_SYSTEM);
                break;
            case TEST:
                create(lst, arts, Artifact.SCOPE_TEST);
                break;
            case RUNTIME:
                create(lst, arts, Artifact.SCOPE_RUNTIME);
                break;
            default:
                for (Artifact a : arts) {
                    if (!a.getArtifactHandler().isAddedToClasspath()) {
                        lst.add(new DependencyWrapper(a));
                    }
                }
            }
            return lst;
        }

        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                cs.fireChange();
            }
        }

        private void create(Set<DependencyWrapper> lst, Collection<Artifact> arts, String... scopes) {
            for (Artifact a : arts) {
                if (!Arrays.asList(scopes).contains(a.getScope())) {
                    continue;
                }
                if (a.getArtifactHandler().isAddedToClasspath()) {
                    lst.add(new DependencyWrapper(a));
                }
            }
        }

    }
    
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
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DependencyWrapper other = (DependencyWrapper) obj;
            if (!artifact.equals(other.artifact)) {
                return false;
            }
            if (!artifact.getDependencyTrail().equals(other.artifact.getDependencyTrail())) {
                return false;
            }
            if (!artifact.getFile().equals(other.artifact.getFile())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + artifact.hashCode() + artifact.getDependencyTrail().hashCode();
            hash = 31 * hash + artifact.getFile().hashCode();
            return hash;
        }
        
    }
    
    @SuppressWarnings("serial")
    private class AddDependencyAction extends AbstractAction {

        @Messages("BTN_Add_Library=Add Dependency...")
        AddDependencyAction() {
            putValue(Action.NAME, BTN_Add_Library());
        }

        @Override public void actionPerformed(ActionEvent event) {
            String typeString = dependencies.type == Type.RUNTIME ? "runtime" : (dependencies.type == Type.TEST ? "test" : "compile"); //NOI18N
            final String[] data = AddDependencyPanel.show(dependencies.project, true, typeString);
            if (data != null) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        ModelUtils.addDependency(dependencies.project.getProjectDirectory().getFileObject("pom.xml")/*NOI18N*/,
                               data[0], data[1], data[2], data[4], data[3], data[5], false);
                        dependencies.project.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource(false);
                    }
                });
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
                @Override
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
                            AtomicBoolean cancel = ProgressTransferListener.activeListener().cancel;
                            if (cancel != null && cancel.get()) {
                                return;
                            }
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

    @SuppressWarnings("serial")
    private static class ResolveDepsAction extends AbstractAction {

        private Project project;

        @Messages("LBL_Download=Download Declared Dependencies")
        ResolveDepsAction(Project prj) {
            putValue(Action.NAME, LBL_Download());
            project = prj;
        }
        
        @Override
        public void actionPerformed(ActionEvent evnt) {
            setEnabled(false);
            project.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource(false);
        }
    }
    
    private static class DependenciesComparator implements Comparator<DependencyWrapper> {

        @Override
        public int compare(DependencyWrapper art1, DependencyWrapper art2) {
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
        Image base;
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
    
    static Preferences prefs() {
        return NbPreferences.root().node(PREF_DEPENDENCIES_UI);
    }

}
