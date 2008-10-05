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

import java.awt.Image;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Developer;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Scm;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.build.model.ModelLineage;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  mkleint
 */
public class POMModelPanel extends javax.swing.JPanel implements ExplorerManager.Provider, Runnable {
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

    /** Creates new form POMInheritancePanel */
    public POMModelPanel() {
        initComponents();
        treeView = (BeanTreeView)jScrollPane1;
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
                    final Children ch = new PomChildren(lin);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new BeanTreeView();

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
        );
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
    
    // <editor-fold defaultstate="collapsed" desc="POM Children">
    private static class PomChildren extends Children.Keys<ModelLineage> {
        public PomChildren(ModelLineage lineage) {
            setKeys(new ModelLineage[] {lineage});
        }
        
        @Override
        protected Node[] createNodes(ModelLineage key) {
            @SuppressWarnings("unchecked")
            List<Model> mods = key.getModelsInDescendingOrder();
            Collections.reverse(mods);
            Model[] models = mods.toArray(new Model[mods.size()]);

            List<Node> nds = new ArrayList<Node>();
            String[] vals = getStringValue(models, "getModelVersion", Model.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, "Model Version", vals));
            vals = getStringValue(models, "getGroupId", Model.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, "GroupId", vals));
            vals = getStringValue(models, "getArtifactId", Model.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, "ArtifactId", vals));
            vals = getStringValue(models, "getPackaging", Model.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, "Packaging", vals));
            vals = getStringValue(models, "getName", Model.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, "Name", vals));
            vals = getStringValue(models, "getVersion", Model.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, "Version", vals));
            vals = getStringValue(models, "getDescription", Model.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, "Description", vals));
            vals = getStringValue(models, "getUrl", Model.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, "Url", vals));
            vals = getStringValue(models, "getInceptionYear", Model.class);
            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, "Inception Year", vals));
            @SuppressWarnings("unchecked")
            List<IssueManagement> issMan = getValue(models, "getIssueManagement", Model.class);
            nds.add(new ObjectNode(Lookup.EMPTY, new IssueManagementChildren(issMan, key), key, "IssueManagement", issMan));
            @SuppressWarnings("unchecked")
            List<CiManagement> ciMan = getValue(models, "getCiManagement", Model.class);
            nds.add(new ObjectNode(Lookup.EMPTY, new CiManagementChildren(ciMan, key), key, "CiManagement", ciMan));
            @SuppressWarnings("unchecked")
            List<Scm> scm = getValue(models, "getScm", Model.class);
            nds.add(new ObjectNode(Lookup.EMPTY, new ScmChildren(scm, key), key, "Scm", scm));
            @SuppressWarnings("unchecked")
            List<Organization> org = getValue(models, "getOrganization", Model.class);
            nds.add(new ObjectNode(Lookup.EMPTY, new OrgChildren(org, key), key, "Organization", org));

            @SuppressWarnings("unchecked")
            List<List> mailingLists = getValue(models, "getMailingLists", Model.class);
            nds.add(new ListNode(Lookup.EMPTY, new ChildrenCreator() {
                public Children createChildren(List value, ModelLineage lineage) {
                    @SuppressWarnings("unchecked")
                    List<MailingList> lst = value;
                    return new MailingListChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getName", MailingList.class);
                    return name.length > 0 ? name[0] : "Mailing List";
                }
            }, key, "Mailing Lists", mailingLists));

            @SuppressWarnings("unchecked")
            List<List> developers = getValue(models, "getDevelopers", Model.class);
            nds.add(new ListNode(Lookup.EMPTY, new ChildrenCreator() {
                public Children createChildren(List value, ModelLineage lineage) {
                    @SuppressWarnings("unchecked")
                    List<Developer> lst = value;
                    return new DeveloperChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getName", Developer.class);
                    String[] id = getStringValue(new Object[] {value}, "getId", Developer.class);
                    return name.length > 0 ? name[0] : (id.length > 0 ? id[0] : "Developer");
                }
            }, key, "Developers", developers));

            @SuppressWarnings("unchecked")
            List<List> contributors = getValue(models, "getContributors", Model.class);
            nds.add(new ListNode(Lookup.EMPTY, new ChildrenCreator() {
                public Children createChildren(List value, ModelLineage lineage) {
                    @SuppressWarnings("unchecked")
                    List<Contributor> lst = value;
                    return new ContributorChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getName", Contributor.class);
                    return name.length > 0 ? name[0] : "Contributor";
                }
            }, key, "Contributors", contributors));

            @SuppressWarnings("unchecked")
            List<List> licenses = getValue(models, "getLicenses", Model.class);
            nds.add(new ListNode(Lookup.EMPTY, new ChildrenCreator() {
                public Children createChildren(List value, ModelLineage lineage) {
                    @SuppressWarnings("unchecked")
                    List<License> lst = value;
                    return new LicenseChildren(lst, lineage);
                }

                public String createName(Object value) {
                    String[] name = getStringValue(new Object[] {value}, "getName", License.class);
                    return name.length > 0 ? name[0] : "License";
                }
            }, key, "Licenses", licenses));

            List<Properties> props = getValue(models, "getProperties", Model.class);
            nds.add(new ObjectNode(Lookup.EMPTY, new PropsChildren(props, key), key, "Properties", props));
            return nds.toArray(new Node[0]);
        }
        
    }
    // </editor-fold>

    private static interface ChildrenCreator {
        Children createChildren(List value, ModelLineage lineage);
        String createName(Object value);
    }

    // <editor-fold defaultstate="collapsed" desc="IssueManagement Children">
    private static class IssueManagementChildren extends Children.Keys<List<IssueManagement>> {
        private ModelLineage lineage;
        public IssueManagementChildren(List<IssueManagement> list, ModelLineage lin) {
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
        private ModelLineage lineage;
        public CiManagementChildren(List<CiManagement> list, ModelLineage lin) {
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
        private ModelLineage lineage;
        public ScmChildren(List<Scm> list, ModelLineage lin) {
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
        private ModelLineage lineage;
        public OrgChildren(List<Organization> list, ModelLineage lin) {
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
        private ModelLineage lineage;
        public PropsChildren(List<Properties> list, ModelLineage lin) {
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
        private ModelLineage lineage;
        public MailingListChildren(List<MailingList> list, ModelLineage lin) {
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
        private ModelLineage lineage;
        public DeveloperChildren(List<Developer> list, ModelLineage lin) {
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
        private ModelLineage lineage;
        public ContributorChildren(List<Contributor> list, ModelLineage lin) {
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
        private ModelLineage lineage;
        public LicenseChildren(List<License> list, ModelLineage lin) {
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

    static Map<String, List<String>> getPropertyValues(Properties[] models) {
        TreeMap<String, List<String>> toRet = new TreeMap<String, List<String>>();
        int nulls = 0;
        for (Properties prop : models) {
            for (Object keyProp : prop.keySet()) {
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
            meth = Model.class.getMethod(getter);
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
        private ModelLineage lineage;
        private String key;
        private String[] values;
        private SingleFieldNode(Lookup lkp, Children children, ModelLineage lineage, String key, String[] values) {
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
        private ModelLineage lineage;
        private List values;
        private ObjectNode(Lookup lkp, Children children, ModelLineage lineage, String key, List values) {
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
        private ModelLineage lineage;
        private List values;

        private ListNode(Lookup lkp, ChildrenCreator childrenCreator, ModelLineage lineage, String name, List<List> values) {
            super( definesValue(values.toArray()) ? createOverrideListChildren(childrenCreator, lineage, values) : Children.LEAF, lkp);
            setName(name);
            this.key = name;
            this.values = values;
            this.lineage = lineage;
        }

        @Override
        public String getHtmlDisplayName() {
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

    private static Children createOverrideListChildren(ChildrenCreator subs, ModelLineage key, List<List> values) {
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

//    private static Children createMergeListChildren(ChildrenCreator subs, ModelLineage key, List<List> values) {
//        Children toRet = new Children.Array();
//        for (List lst : values) {
//            if (lst != null && lst.size() > 0) {
//                for (Object o : lst) {
//                    toRet.add(new Node[] {
//                        new ObjectNode(Lookup.EMPTY, subs.createChildren(o, key), key, subs.createName(o), Collections.singletonList(o))
//                    });
//                }
//                break;
//            }
//        }
//
//        return toRet;
//    }

}

