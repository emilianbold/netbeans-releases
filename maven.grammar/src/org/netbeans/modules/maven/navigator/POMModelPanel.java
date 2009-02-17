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

package org.netbeans.modules.maven.navigator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.build.model.ModelLineage;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.model.pom.CiManagement;
import org.netbeans.modules.maven.model.pom.Contributor;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.Developer;
import org.netbeans.modules.maven.model.pom.IssueManagement;
import org.netbeans.modules.maven.model.pom.License;
import org.netbeans.modules.maven.model.pom.MailingList;
import org.netbeans.modules.maven.model.pom.Organization;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.model.pom.Scm;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
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

/**
 *
 * @author  mkleint
 */
public class POMModelPanel extends javax.swing.JPanel implements ExplorerManager.Provider, Runnable {

    private static final String NAVIGATOR_SHOW_UNDEFINED = "navigator.showUndefined"; //NOi18N
    private transient ExplorerManager explorerManager = new ExplorerManager();
    
    private BeanTreeView treeView;
    private DataObject current;
    private FileChangeAdapter adapter = new FileChangeAdapter(){
            @Override
            public void fileChanged(FileEvent fe) {
                showWaitNode();
                RequestProcessor.getDefault().post(POMModelPanel.this);
            }
        };
    private TapPanel filtersPanel;

    private boolean filterIncludeUndefined;

    /** Creates new form POMInheritancePanel */
    public POMModelPanel() {
        initComponents();
        filterIncludeUndefined = NbPreferences.forModule(POMModelPanel.class).getBoolean(NAVIGATOR_SHOW_UNDEFINED, false);

        treeView = (BeanTreeView)jScrollPane1;
        // filters
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(POMModelPanel.class, "TIP_TapPanel", keyText)); //NOI18N

        JComponent buttons = createFilterButtons();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N

        add(filtersPanel, BorderLayout.SOUTH);

    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    void navigate(DataObject d) {
        if (current != null) {
            current.getPrimaryFile().removeFileChangeListener(adapter);
        }
        current = d;
        current.getPrimaryFile().addFileChangeListener(adapter);
        showWaitNode();
        RequestProcessor.getDefault().post(this);
    }
    
    public void run() {
        if (current != null) {
            File file = FileUtil.toFile(current.getPrimaryFile());
            // can be null for stuff in jars?
            if (file != null) {
                try {
                    ModelLineage lin = EmbedderFactory.createModelLineage(file, EmbedderFactory.createOnlineEmbedder(), false);
                    @SuppressWarnings("unchecked")
                    Iterator<File> it = lin.fileIterator();
                    List<POMModel> mdls = new ArrayList<POMModel>();


                    while (it.hasNext()) {
                        File pom = it.next();
                        FileUtil.refreshFor(pom);
                        FileObject fo = FileUtil.toFileObject(pom);
                        if (fo != null) {
                            ModelSource ms = org.netbeans.modules.maven.model.Utilities.createModelSource(fo);
                            POMModel mdl = POMModelFactory.getDefault().getModel(ms);
                            if (mdl != null) {
                                mdls.add(0, mdl);
                            } else {
                                System.out.println("no model for " + pom);
                            }
                        } else {
                            System.out.println("no fileobject for " + pom);
                        }
                    }
                    final Children ch = new PomChildren(mdls);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           treeView.setRootVisible(false);
                           explorerManager.setRootContext(new AbstractNode(ch));
                        } 
                    });
                } catch (ProjectBuildingException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.FINE, "Error reading model lineage", ex);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           treeView.setRootVisible(true);
                           explorerManager.setRootContext(createErrorNode());
                        }
                    });
                }
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                       treeView.setRootVisible(false);
                       explorerManager.setRootContext(createEmptyNode());
                    } 
                });
            }
        }
    }

    /**
     * 
     */
    void release() {
        if (current != null) {
            current.getPrimaryFile().removeFileChangeListener(adapter);
        }
        current = null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               treeView.setRootVisible(false);
               explorerManager.setRootContext(createEmptyNode());
            } 
        });
    }

    /**
     * 
     */
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               treeView.setRootVisible(true);
               explorerManager.setRootContext(createWaitNode());
            } 
        });
    }

    private JComponent createFilterButtons() {
        Box box = new Box(BoxLayout.X_AXIS);
        box.setBorder(new EmptyBorder(1, 2, 3, 5));

            // configure toolbar
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL) {
            @Override
            protected void paintComponent(Graphics g) {
            }
        };
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            JToggleButton tg1 = new JToggleButton(new ShowUndefinedAction());
            tg1.setSelected(filterIncludeUndefined);
            toolbar.add(tg1);
            Dimension space = new Dimension(3, 0);
            toolbar.addSeparator(space);

            box.add(toolbar);
            return box;

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
    private static Node createWaitNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setIconBaseWithExtension("org/netbeans/modules/maven/navigator/wait.gif");
        an.setDisplayName(NbBundle.getMessage(POMInheritancePanel.class, "LBL_Wait"));
        return an;
    }

    private static Node createEmptyNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        return an;
    }

    private static Node createErrorNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setDisplayName(NbBundle.getMessage(POMInheritancePanel.class, "LBL_Error"));
        return an;
    }

    protected void addSingleFieldNode(List<POMModel> key, String[] vals, String dispName, List<Node> nds) {
        if (!filterIncludeUndefined || definesValue(vals)) {
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, dispName, vals));
        }
    }

    private void addObjectNode(List<POMModel> key, List sMan, Children sueManagementChildren, String displayName, List<Node> nds) {
        if (!filterIncludeUndefined || definesValue(sMan.toArray())) {
            nds.add(new ObjectNode(Lookup.EMPTY, sueManagementChildren, key, displayName, sMan));
        }
    }

    private void addListNode(List<POMModel> key, List<List> sMan, ChildrenCreator chc, String displayName, List<Node> nds) {
        if (!filterIncludeUndefined || definesValue(sMan.toArray())) {
            nds.add(new ListNode(Lookup.EMPTY, chc, key, displayName, sMan));
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="POM Children">
    private class PomChildren extends Children.Keys<Object> {
        private List<POMModel> lin;
        public PomChildren(List<POMModel> lineage) {
            setKeys(new Object[] {lineage} );
            this.lin = lineage;
        }

        public void reshow() {
            this.refreshKey(lin);
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            @SuppressWarnings("unchecked")
            List<POMModel> mods = (List<POMModel>) key;
            Project[] models = new Project[mods.size()];
            int index = 0;
            for (POMModel md : mods) {
                models[index] = md.getProject();
                index++;
            }

            List<Node> nds = new ArrayList<Node>();
            addSingleFieldNode(mods, getStringValue(models, "getModelVersion", Project.class), "Model Version", nds);
            addSingleFieldNode(mods, getStringValue(models, "getGroupId", Project.class), "GroupId", nds);
            addSingleFieldNode(mods, getStringValue(models, "getArtifactId", Project.class), "ArtifactId", nds);
            addSingleFieldNode(mods, getStringValue(models, "getPackaging", Project.class), "Packaging", nds);
            addSingleFieldNode(mods, getStringValue(models, "getName", Project.class), "Name", nds);
            addSingleFieldNode(mods, getStringValue(models, "getVersion", Project.class), "Version", nds);
            addSingleFieldNode(mods, getStringValue(models, "getDescription", Project.class), "Description", nds);
            addSingleFieldNode(mods, getStringValue(models, "getURL", Project.class), "Url", nds);
            addSingleFieldNode(mods, getStringValue(models, "getInceptionYear", Project.class), "Inception Year", nds);

            @SuppressWarnings("unchecked")
            List<IssueManagement> issMan = getValue(models, "getIssueManagement", Project.class);
            addObjectNode(mods, issMan, new IssueManagementChildren(issMan, mods), "IssueManagement", nds);

            @SuppressWarnings("unchecked")
            List<CiManagement> ciMan = getValue(models, "getCiManagement", Project.class);
            addObjectNode(mods, ciMan, new CiManagementChildren(ciMan, mods), "CiManagement", nds);

            @SuppressWarnings("unchecked")
            List<Scm> scm = getValue(models, "getScm", Project.class);
            addObjectNode(mods, scm, new ScmChildren(scm, mods), "Scm", nds);

            @SuppressWarnings("unchecked")
            List<Organization> org = getValue(models, "getOrganization", Project.class);
            addObjectNode(mods, org, new OrgChildren(org, mods), "Organization", nds);

            @SuppressWarnings("unchecked")
            List<List> mailingLists = getValue(models, "getMailingLists", Project.class);
            addListNode(mods, mailingLists, new ChildrenCreator() {
                public Children createChildren(List value, List<POMModel> lineage) {
                    @SuppressWarnings("unchecked")
                    List<MailingList> lst = value;
                    return new MailingListChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getName", MailingList.class);
                    return name.length > 0 ? name[0] : "Mailing List";
                }
            }, "Mailing Lists", nds);

            @SuppressWarnings("unchecked")
            List<List> developers = getValue(models, "getDevelopers", Project.class);
            addListNode(mods, developers, new ChildrenCreator() {
                public Children createChildren(List value, List<POMModel> lineage) {
                    @SuppressWarnings("unchecked")
                    List<Developer> lst = value;
                    return new DeveloperChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getName", Developer.class);
                    String[] id = getStringValue(new Object[] {value}, "getId", Developer.class);
                    return name.length > 0 ? name[0] : (id.length > 0 ? id[0] : "Developer");
                }
            }, "Developers", nds);

            @SuppressWarnings("unchecked")
            List<List> contributors = getValue(models, "getContributors", Project.class);
            addListNode(mods, contributors, new ChildrenCreator() {
                public Children createChildren(List value, List<POMModel> lineage) {
                    @SuppressWarnings("unchecked")
                    List<Contributor> lst = value;
                    return new ContributorChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getName", Contributor.class);
                    return name.length > 0 ? name[0] : "Contributor";
                }
            }, "Contributors", nds);

            @SuppressWarnings("unchecked")
            List<List> licenses = getValue(models, "getLicenses", Project.class);
            addListNode(mods, licenses, new ChildrenCreator() {
                public Children createChildren(List value, List<POMModel> lineage) {
                    @SuppressWarnings("unchecked")
                    List<License> lst = value;
                    return new LicenseChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getName", License.class);
                    return name.length > 0 ? name[0] : "License";
                }
            }, "Licenses", nds);

            @SuppressWarnings("unchecked")
            List<List> dependencies = getValue(models, "getDependencies", Project.class);
            addListNode(mods, dependencies, new ChildrenCreator2() {
                public Children createChildren(List value, List<POMModel> lineage) {
                    @SuppressWarnings("unchecked")
                    List<Dependency> lst = value;
                    return new DependencyChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getArtifactId", Dependency.class);
                    return name.length > 0 ? name[0] : "Dependency";
                }

                public boolean objectsEqual(Object value1, Object value2) {
                    Dependency d1 = (Dependency)value1;
                    Dependency d2 = (Dependency)value2;
                    String grId1 = d1.getGroupId();
                    String grId2 = d2.getGroupId();
                    String artId1 = d1.getArtifactId();
                    String artId2 = d2.getArtifactId();
                    return (grId1 + ":" + artId1).equals(grId2 + ":" + artId2);
                }
            }, "Dependencies", nds);

            @SuppressWarnings("unchecked")
            List<List> repositories = getValue(models, "getRepositories", Project.class);
            addListNode(mods, repositories, new ChildrenCreator2() {
                public Children createChildren(List value, List<POMModel> lineage) {
                    @SuppressWarnings("unchecked")
                    List<Repository> lst = value;
                    return new RepositoryChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getId", Repository.class);
                    return name.length > 0 ? name[0] : "Repository";
                }

                public boolean objectsEqual(Object value1, Object value2) {
                    Repository d1 = (Repository)value1;
                    Repository d2 = (Repository)value2;
                    return d1.getId() != null && d1.getId().equals(d2.getId());
                }
            }, "Repositories", nds);

            @SuppressWarnings("unchecked")
            List<Properties> props = getValue(models, "getProperties", Project.class);
            addObjectNode(mods, props, new PropsChildren(props, mods), "Properties", nds);
            return nds.toArray(new Node[0]);
        }

        
    }
    // </editor-fold>

    private static interface ChildrenCreator {
        Children createChildren(List value, List<POMModel> lineage);
        String createName(Object value);
    }

    private static interface ChildrenCreator2 extends ChildrenCreator {
        boolean objectsEqual(Object value1, Object value2);
    }

    // <editor-fold defaultstate="collapsed" desc="IssueManagement Children">
    private static class IssueManagementChildren extends Children.Keys<List<IssueManagement>> {
        private List<POMModel> lineage;
        public IssueManagementChildren(List<IssueManagement> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<IssueManagement> key) {
            IssueManagement[] models = key.toArray(new IssueManagement[key.size()]);

            List<SingleFieldNode> nds = new ArrayList<SingleFieldNode>();
            String[] vals = getStringValue(models, "getSystem", IssueManagement.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "System", vals));
            vals = getStringValue(models, "getUrl", IssueManagement.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Url", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="CIManagement Children">
    private static class CiManagementChildren extends Children.Keys<List<CiManagement>> {
        private List<POMModel> lineage;
        public CiManagementChildren(List<CiManagement> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<CiManagement> key) {
            CiManagement[] models = key.toArray(new CiManagement[key.size()]);

            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getSystem", CiManagement.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "System", vals));
            vals = getStringValue(models, "getUrl", CiManagement.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Url", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Scm Children">
    private static class ScmChildren extends Children.Keys<List<Scm>> {
        private List<POMModel> lineage;
        public ScmChildren(List<Scm> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<Scm> key) {
            Scm[] models = key.toArray(new Scm[key.size()]);
            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getConnection", Scm.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Connection", vals));
            vals = getStringValue(models, "getDeveloperConnection", Scm.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Developer Connection", vals));
            vals = getStringValue(models, "getTag", Scm.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Tag", vals));
            vals = getStringValue(models, "getUrl", Scm.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Url", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Organization Children">
    private static class OrgChildren extends Children.Keys<List<Organization>> {
        private List<POMModel> lineage;
        public OrgChildren(List<Organization> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<Organization> key) {
            Organization[] models = key.toArray(new Organization[key.size()]);
            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getName", Organization.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Name", vals));
            vals = getStringValue(models, "getUrl", Organization.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Url", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Properties Children">
    private static class PropsChildren extends Children.Keys<List<Properties>> {
        private List<POMModel> lineage;
        public PropsChildren(List<Properties> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<Properties> key) {
            Properties[] models = key.toArray(new Properties[key.size()]);
            List<Node> nds = new ArrayList<Node>();
            java.util.Map<String, List<String>> properties = getPropertyValues(models);
            for (java.util.Map.Entry<String, List<String>> entry : properties.entrySet()) {
                String[] vals = entry.getValue().toArray(new String[0]);
                nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, entry.getKey(), vals));
            }
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Mailing List Children">
    private static class MailingListChildren extends Children.Keys<List<MailingList>> {
        private List<POMModel> lineage;
        public MailingListChildren(List<MailingList> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<MailingList> key) {
            MailingList[] models = key.toArray(new MailingList[key.size()]);
            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getName", MailingList.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Name", vals));
            vals = getStringValue(models, "getSubscribe", MailingList.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Subscribe", vals));
            vals = getStringValue(models, "getUnsubscribe", MailingList.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Unsubscribe", vals));
            vals = getStringValue(models, "getPost", MailingList.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Post", vals));
            vals = getStringValue(models, "getArchive", MailingList.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Archive", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Developers Children">
    private static class DeveloperChildren extends Children.Keys<List<Developer>> {
        private List<POMModel> lineage;
        public DeveloperChildren(List<Developer> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<Developer> key) {
            Developer[] models = key.toArray(new Developer[key.size()]);
            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getId", Developer.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Id", vals));
            vals = getStringValue(models, "getName", Developer.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Name", vals));
            vals = getStringValue(models, "getEmail", Developer.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Email", vals));
            vals = getStringValue(models, "getUrl", Developer.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Url", vals));
            vals = getStringValue(models, "getOrganization", Developer.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Organization", vals));
            vals = getStringValue(models, "getOrganizationUrl", Developer.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Organization Url", vals));
            vals = getStringValue(models, "getTimezone", Developer.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Timezone", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Contributors Children">
    private static class ContributorChildren extends Children.Keys<List<Contributor>> {
        private List<POMModel> lineage;
        public ContributorChildren(List<Contributor> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<Contributor> key) {
            Contributor[] models = key.toArray(new Contributor[key.size()]);
            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getName", Contributor.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Name", vals));
            vals = getStringValue(models, "getEmail", Contributor.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Email", vals));
            vals = getStringValue(models, "getUrl", Contributor.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Url", vals));
            vals = getStringValue(models, "getOrganization", Contributor.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Organization", vals));
            vals = getStringValue(models, "getOrganizationUrl", Contributor.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Organization Url", vals));
            vals = getStringValue(models, "getTimezone", Contributor.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Timezone", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="License Children">
    private static class LicenseChildren extends Children.Keys<List<License>> {
        private List<POMModel> lineage;
        public LicenseChildren(List<License> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<License> key) {
            License[] models = key.toArray(new License[key.size()]);
            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getName", License.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Name", vals));
            vals = getStringValue(models, "getUrl", License.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Url", vals));
            vals = getStringValue(models, "getDistribution", License.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Distribution", vals));
            vals = getStringValue(models, "getComments", License.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Comments", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dependency Children">
    private static class DependencyChildren extends Children.Keys<List<Dependency>> {
        private List<POMModel> lineage;
        public DependencyChildren(List<Dependency> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<Dependency> key) {
            Dependency[] models = key.toArray(new Dependency[key.size()]);
            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getGroupId", Dependency.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "GroupId", vals));
            vals = getStringValue(models, "getArtifactId", Dependency.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "ArtifactId", vals));
            vals = getStringValue(models, "getVersion", Dependency.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Version", vals));
            vals = getStringValue(models, "getType", Dependency.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Type", vals));
            vals = getStringValue(models, "getScope", Dependency.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Scope", vals));
            vals = getStringValue(models, "getClassifier", Dependency.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Classifier", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Repository Children">
    private static class RepositoryChildren extends Children.Keys<List<Repository>> {
        private List<POMModel> lineage;
        public RepositoryChildren(List<Repository> list, List<POMModel> lin) {
            setKeys(new List[] {list});
            lineage = lin;
        }

        @Override
        protected Node[] createNodes(List<Repository> key) {
            Repository[] models = key.toArray(new Repository[key.size()]);
            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getId", Repository.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Id", vals));
            vals = getStringValue(models, "getName", Repository.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Name", vals));
            vals = getStringValue(models, "getUrl", Repository.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Url", vals));
            return nds.toArray(new Node[0]);
        }
    }
    // </editor-fold>

    static Map<String, List<String>> getPropertyValues(Properties[] models) {
        TreeMap<String, List<String>> toRet = new TreeMap<String, List<String>>();
        int nulls = 0;
        for (Properties prop : models) {
            for (Object keyProp : prop.getProperties().keySet()) {
                String k = (String) keyProp;
                List<String> vals = toRet.get(k);
                if (vals == null) {
                    vals = new ArrayList<String>();
                    toRet.put(k, vals);
                }
                if (vals.size() < nulls) {
                    vals.addAll(Arrays.asList(new String[nulls - vals.size()]));
                }
                vals.add(prop.getProperty(k));
            }
            nulls = nulls + 1;
        }
        for (List<String> vals : toRet.values()) {
            if (vals.size() < models.length) {
                vals.addAll(Arrays.asList(new String[models.length - vals.size()]));
            }
        }
        return toRet;
    }

    private static String[] getStringValue(Object[] models, String getter, Class modelClazz) {
        String[] toRet = new String[models.length];
        Method meth = null;
        try {
            meth = modelClazz.getMethod(getter);
        } catch (Exception ex) {
            //ignore
        }
        assert meth != null : "Model doesn't have a getter named '" + getter + "'";
        int i = 0;
        for (Object model : models) {
            Object obj = null;
            if (model != null) {
                try {
                    obj = meth.invoke(model);
                } catch (Exception ex) {
                    //ignore
                }
            }
            assert obj == null || obj instanceof String : "Wrong return type " + obj.getClass() + " for " + getter;
            toRet[i] = (String)obj;
            i = i + 1;
        }
        return toRet;
    }

    private static List getValue(Object[] models, String getter, Class modelClazz) {
        List toRet = new ArrayList();
        Method meth = null;
        try {
            meth = modelClazz.getMethod(getter);
        } catch (Exception ex) {
            //ignore
        }
        assert meth != null : "Model " + modelClazz.getSimpleName() + " doesn't have a getter named '" + getter + "'";
        for (Object model : models) {
            Object obj = null;
            if (model != null) {
                try {
                    obj = meth.invoke(model);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (obj instanceof Collection && ((Collection)obj).size() == 0) {
                //ignore empty arrays, model getters just return tem when not defined om pom.
                obj = null;
            }
            toRet.add(obj);
        }
        return toRet;
    }


    /**
     * returns true if the value is defined in current pom. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static boolean isValueDefinedInCurrent(Object[] values) {
        return values[0] != null;
    }

    /**
     * returns true if the value is defined in current pom
     * and one of the parent poms as well.
     */
    static boolean overridesParentValue(Object[] values) {
        if (values.length <= 1) {
            return false;
        }
        boolean curr = values[0] != null;
        boolean par = false;
        for (int i = 1; i < values.length; i++) {
            if (values[i] != null) {
                par = true;
                break;
            }
        }
        return curr && par;

    }

    /**
     * returns true if the value is defined in in any pom. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static boolean definesValue(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return true;
            }
        }
        return false;
    }


    /**
     * gets the first defined value from the list. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static String getValidValue(String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return values[i];
            }
        }
        return null;
    }
    
    private static class SingleFieldNode extends AbstractNode {
        
        private Image icon = ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/Maven2Icon.gif"); // NOI18N
        private List<POMModel> lineage;
        private String key;
        private String[] values;
        private SingleFieldNode(Lookup lkp, Children children, List<POMModel> lineage, String key, String[] values) {
            super( children, lkp);
            setName(key);
            this.key = key;
            this.values = values;
            this.lineage = lineage;
        }

        @Override
        public String getHtmlDisplayName() {
            String dispVal = getValidValue(values);
            if (dispVal == null) {
                dispVal = "&lt;Undefined&gt;";
            }
            boolean override = overridesParentValue(values);
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !isValueDefinedInCurrent(values);
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";

            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " : " + dispVal +
                    overrideEnd + inheritedEnd;
            return message;
        }
        
        @Override
        public Image getIcon(int type) {
             return icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
    }

    private static class ObjectNode extends AbstractNode {

        private Image icon = ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/Maven2Icon.gif"); // NOI18N
        private String key;
        private List<POMModel> lineage;
        private List values;
        private ObjectNode(Lookup lkp, Children children, List<POMModel> lineage, String key, List values) {
            super( definesValue(values.toArray()) ? children : Children.LEAF, lkp);
            setName(key);
            this.key = key;
            this.values = values;
            this.lineage = lineage;
        }

        @Override
        public String getHtmlDisplayName() {
            String dispVal = definesValue(values.toArray()) ? "" : "&lt;Undefined&gt;";
            boolean override = overridesParentValue(values.toArray());
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !isValueDefinedInCurrent(values.toArray());
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";

            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " " + dispVal +
                    overrideEnd + inheritedEnd;

            return message;
        }

        @Override
        public Image getIcon(int type) {
             return icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    private static class ListNode extends AbstractNode {

        private Image icon = ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/Maven2Icon.gif"); // NOI18N
        private String key;
        private List<POMModel> lineage;
        private List values;

        private ListNode(Lookup lkp, ChildrenCreator childrenCreator, List<POMModel> lineage, String name, List<List> values) {
            super( definesValue(values.toArray()) ?
                (childrenCreator instanceof ChildrenCreator2 ?
                    createMergeListChildren((ChildrenCreator2)childrenCreator, lineage, values) :
                    createOverrideListChildren(childrenCreator, lineage, values))
                : Children.LEAF, lkp);
            setName(name);
            this.key = name;
            this.values = values;
            this.lineage = lineage;
        }

        @Override
        public String getHtmlDisplayName() {
            //TODO - this needs different markings..
            String dispVal = definesValue(values.toArray()) ? "" : "&lt;Undefined&gt;";
            boolean override = overridesParentValue(values.toArray());
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !isValueDefinedInCurrent(values.toArray()) && definesValue(values.toArray());
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";
            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " " + dispVal +
                    overrideEnd + inheritedEnd;
            return message;
        }

        @Override
        public Image getIcon(int type) {
             return icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    private static Children createOverrideListChildren(ChildrenCreator subs, List<POMModel> key, List<List> values) {
        Children toRet = new Children.Array();
        int count = 0;
        for (List lst : values) {
            if (lst != null && lst.size() > 0) {
                for (Object o : lst) {
                    List objectList = new ArrayList(Collections.nCopies(count, null));
                    objectList.add(o);
                    toRet.add(new Node[] {
                        new ObjectNode(Lookup.EMPTY, subs.createChildren(objectList, key), key, subs.createName(o), objectList)
                    });
                }
                break;
            }
            count = count + 1;
        }

        return toRet;
    }

    private static Children createMergeListChildren(ChildrenCreator2 subs, List<POMModel> key, List<List> values) {
        Children toRet = new Children.Array();
        HashMap<Object, List> content = new HashMap<Object, List>();
        List order = new ArrayList();

        int count = 0;
        for (List lst : values) {
            if (lst != null && lst.size() > 0) {
                for (Object o : lst) {
                    processObjectList(o, content, count, subs);
                            new ArrayList(Collections.nCopies(count, null));
                }
            }
            count = count + 1;
        }
        for (Map.Entry<Object, List> entry : content.entrySet()) {
            toRet.add(new Node[] {
                new ObjectNode(Lookup.EMPTY, subs.createChildren(entry.getValue(), key), key, subs.createName(entry.getKey()), entry.getValue())
            });
        }

        return toRet;
    }

    private static List processObjectList(Object o, Map<Object, List> content, int count, ChildrenCreator2 subs) {
        List lst = null;
        for (Object known : content.keySet()) {
            if (subs.objectsEqual(o, known)) {
                lst = content.get(known);
                break;
            }
        }
        if (lst == null) {
            lst = new ArrayList();
            content.put(o, lst);
        }
        if (lst.size() < count) {
            lst.addAll(new ArrayList(Collections.nCopies(count - lst.size(), null)));
        }
        lst.add(o);
        return lst;
    }

    private class ShowUndefinedAction extends AbstractAction {

        public ShowUndefinedAction() {
            putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/filterHideFields.gif")));
            putValue(SHORT_DESCRIPTION, "Show only POM elements defined in at least one place.");
        }


        public void actionPerformed(ActionEvent e) {
            filterIncludeUndefined = !filterIncludeUndefined;
            NbPreferences.forModule(POMModelPanel.class).putBoolean( NAVIGATOR_SHOW_UNDEFINED, filterIncludeUndefined);

            PomChildren keys = (PomChildren) explorerManager.getRootContext().getChildren();
            keys.reshow();
        }
        
    }
}

