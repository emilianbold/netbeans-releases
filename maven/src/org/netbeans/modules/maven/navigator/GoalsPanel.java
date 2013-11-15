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

package org.netbeans.modules.maven.navigator;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.spi.IconResources;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 *
 * @author  mkleint
 */
public class GoalsPanel extends javax.swing.JPanel implements ExplorerManager.Provider, Runnable {

    private final transient ExplorerManager explorerManager = new ExplorerManager();
    
    private final BeanTreeView treeView;
    private NbMavenProject current;
    private Project currentP;
    
    private final PropertyChangeListener pchadapter = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                showWaitNode();
                RequestProcessor.getDefault().post(GoalsPanel.this);
            }
        }
    };

    /** Creates new form POMInheritancePanel */
    public GoalsPanel() {
        initComponents();
        treeView = (BeanTreeView)jScrollPane1;
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    void navigate(DataObject d) {
        if (current != null) {
            current.removePropertyChangeListener(pchadapter);
        }
        NbMavenProject n = null;

        FileObject f = d.getPrimaryFile();
        if (!f.isFolder()) {
            f = f.getParent();
        }
        Project p = null;
        try {
            p = ProjectManager.getDefault().findProject(f);
            if (p != null) {
                n = p.getLookup().lookup(NbMavenProject.class);
            }
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            //Exceptions.printStackTrace(ex);
        }

        if (n == null) {
            release();
            return;
        }
         
        current = n;
        currentP = p;
        current.addPropertyChangeListener(pchadapter);
        showWaitNode();
        RequestProcessor.getDefault().post(this);
    }
    
    @Override
    public void run() {
        //#164852 somehow a folder dataobject slipped in, test mimetype to avoid that.
        // the root cause of the problem is unknown though
        if (currentP != null ) { //NOI18N
         
            NbMavenProject mpp = currentP.getLookup().lookup(NbMavenProject.class);
            if (mpp != null) {
                final Children ch = Children.create(new PluginChildren(currentP), true);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        treeView.setRootVisible(false);
                        explorerManager.setRootContext(new AbstractNode(ch));
                        treeView.expandAll();
                    }
                });
                return;
            }


        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                treeView.setRootVisible(false);
                explorerManager.setRootContext(createEmptyNode());
            }
        });
    }

    /**
     * 
     */
    void release() {
        if (current != null) {
            current.removePropertyChangeListener(pchadapter);
        }
        current = null;
        currentP = null;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
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
            @Override
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
    @Messages("LBL_Wait=Please Wait...")
    private static Node createWaitNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setIconBaseWithExtension("org/netbeans/modules/maven/navigator/wait.gif");
        an.setDisplayName(Bundle.LBL_Wait());
        return an;
    }

    private static Node createEmptyNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        return an;
    }

    private static class PluginChildren extends ChildFactory<Mojo> {
        private final Project prj;

        PluginChildren(Project prj) {
            this.prj = prj;
        }

        protected @Override
        boolean createKeys(List<Mojo> toPopulate) {
            Set<Mojo> goals = new TreeSet<Mojo>();
            MavenProject mp = prj.getLookup().lookup(NbMavenProject.class).getMavenProject();
            for (Artifact p : mp.getPluginArtifacts()) {
                EmbedderFactory.getProjectEmbedder().getLifecyclePhases();
                try {

                    EmbedderFactory.getOnlineEmbedder().resolve(p, mp.getPluginArtifactRepositories(), EmbedderFactory.getOnlineEmbedder().getLocalRepository());
                    Document d = loadPluginXml(p.getFile());
                    if (d != null) {
                        Element root = d.getDocumentElement();
                        Element mojos = XMLUtil.findElement(root, "mojos", null);
                        if (mojos == null) {
                            LOG.log(Level.WARNING, "no mojos in {0}", p.getFile());
                            continue;
                        }
                        Element goalPrefix = XMLUtil.findElement(root, "goalPrefix", null);
                        if (goalPrefix == null) {
                            LOG.log(Level.WARNING, "no goalPrefix in {0}", p.getFile());
                            continue;
                        }

                        for (Element mojo : XMLUtil.findSubElements(mojos)) {
                            if (!mojo.getTagName().equals("mojo")) {
                                continue;
                            }
                            Element goal = XMLUtil.findElement(mojo, "goal", null);
                            if (goal == null) {
                                LOG.log(Level.WARNING, "mojo missing goal in {0}", p.getFile());
                                continue;
                            }
                            String goalString = XMLUtil.findText(goal).trim();
                            List<Param> params = new ArrayList<Param>();
                            Element parameters = XMLUtil.findElement(mojo, "parameters", null);
                            if (parameters != null) {
                                for (Element param : XMLUtil.findSubElements(parameters)) {
                                    if (!param.getTagName().equals("parameter")) {
                                        continue;
                                    }
                                    Element nameEl = XMLUtil.findElement(param, "name", null);
                                    Element editableEl = XMLUtil.findElement(param, "editable", null);
                                    Element requiredEl = XMLUtil.findElement(param, "required", null);
                                    if (nameEl != null && requiredEl != null && editableEl != null && "true".equals(XMLUtil.findText(editableEl))) {
                                        String r = XMLUtil.findText(requiredEl);
                                        Param par = new Param(XMLUtil.findText(nameEl), "true".equals(r));
                                        params.add(par);
                                    }
                                }
                            }
                            Element config = XMLUtil.findElement(mojo, "configuration", null);
                            if (config != null) {
                                for (Param par : params) {
                                    Element pconfEl = XMLUtil.findElement(config, par.parameterName, null);
                                    if (pconfEl != null) {
                                        Attr attr = pconfEl.getAttributeNode("default-value");
                                        if (attr != null) {
                                            par.defValue = attr.getValue();
                                        }
                                        String val = XMLUtil.findText(pconfEl);
                                        if (val != null && val.startsWith("${") && val.endsWith("}")) {
                                            par.property = val.substring(2, val.length() - 1);
                                        }
                                    }
                                    Plugin pl = mp.getPlugin(Plugin.constructKey(p.getGroupId(), p.getArtifactId()));
                                    if (pl != null) {
                                        Xpp3Dom c = (Xpp3Dom) pl.getConfiguration();
                                        if (c != null) {
                                            par.parameterInModel = c.getChild(par.parameterName) != null;
                                        }
                                    }
                                    if (par.property != null) {
                                        par.propertyInModel = mp.getProperties().getProperty(par.property) != null;
                                    }
                                }
                            }
                            goals.add(new Mojo(XMLUtil.findText(goalPrefix).trim(), goalString, p, params));
                        }

                    }
                } catch (ArtifactResolutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ArtifactNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            toPopulate.addAll(goals);
            return true;
        }

        protected @Override
        Node createNodeForKey(Mojo mdl) {
            return new MojoNode(mdl, prj);
        }
    }

    private static class Mojo implements Comparable<Mojo> {

        final Artifact a;
        final String goal;
        final String prefix;
        final List<Param> parameters;

        public Mojo(String prefix, String goal, Artifact a, List<Param> parameters) {
            this.a = a;
            this.goal = goal;
            this.prefix = prefix;
            this.parameters = parameters;
        }
        
        List<Param> getNotSetParams() {
            List<Param> toRet = new ArrayList<Param>();
            for (Param p : parameters) {
                if (p.required && !p.parameterInModel && (p.property == null || !p.propertyInModel)) {
                    toRet.add(p);
                }
            }
            return toRet;
        }

        @Override
        public int compareTo(Mojo o) {
            int res = prefix.compareTo(o.prefix);
            if (res == 0) {
                res = goal.compareTo(o.goal);
            }
            return res;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + (this.goal != null ? this.goal.hashCode() : 0);
            hash = 17 * hash + (this.prefix != null ? this.prefix.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Mojo other = (Mojo) obj;
            if ((this.goal == null) ? (other.goal != null) : !this.goal.equals(other.goal)) {
                return false;
            }
            if ((this.prefix == null) ? (other.prefix != null) : !this.prefix.equals(other.prefix)) {
                return false;
            }
            return true;
        }
        
    }
    
    private static final class Param {
        final String parameterName;
        final boolean required;
        String property;
        String defValue;
        boolean parameterInModel = false; //calculated from presence of parameterName in <configuration> and property in <properties>
        boolean propertyInModel = false;

        public Param(String parameterName, boolean required) {
            this.parameterName = parameterName;
            this.required = required;
        }
        
    }
    
     private static class MojoNode extends AbstractNode {
        
 
        private final Mojo mojo;
        private final Project project;
        private MojoNode(@NonNull Mojo mojo, Project p) {
            super(Children.LEAF);
            setDisplayName(mojo.goal);
            setShortDescription("<html>Plugin:" + mojo.a.getId() + "<br/>Goal:" + mojo.goal + "<br/>Prefix:" + mojo.prefix);
            this.mojo = mojo;
            this.project = p;
        }

        @Override
        public Action[] getActions(boolean context) {
            NetbeansActionMapping mapp = new NetbeansActionMapping();
            for (Param p : mojo.getNotSetParams()) {
                if (p.property != null) {
                    mapp.addProperty(p.property, "");
                }
            }
            mapp.setGoals(Collections.singletonList(mojo.a.getGroupId() + ":" + mojo.a.getArtifactId() + ":" + mojo.a.getVersion() + ":" + mojo.goal));
            Action a = ActionProviderImpl.createCustomMavenAction(mojo.prefix + ":" + mojo.goal, mapp, true, Lookup.EMPTY, project);
            a.putValue(Action.NAME, "Execute Mojo With Modifiers...");
            return new Action[] {
                new RunGoalAction(mojo, project),
                a
            };
        }

        @Override
        public String getHtmlDisplayName() {
            return "<html>" /*<font color='!controlShadow'>"*/ + mojo.prefix /*+"</font>"*/ + " <b>" +  mojo.goal + "</b></html>";
        }

        @Override
        public Action getPreferredAction() {
            return new RunGoalAction(mojo, project);
        }

        @Override
        public Image getIcon(int type) {
             return ImageUtilities.loadImage(IconResources.MOJO_ICON);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }
    private static final Logger LOG = Logger.getLogger(GoalsPanel.class.getName());
     
    private static @CheckForNull Document loadPluginXml(File jar) {
        if (!jar.isFile() || !jar.getName().endsWith(".jar")) {
            return null;
        }
        LOG.log(Level.FINER, "parsing plugin.xml from {0}", jar);
            try {
            return XMLUtil.parse(new InputSource("jar:" + Utilities.toURI(jar) + "!/META-INF/maven/plugin.xml"), false, false, XMLUtil.defaultErrorHandler(), null);
        } catch (Exception x) {
            LOG.log(Level.FINE, "could not parse " + jar, x.toString());
            return null;
        }
    }

    private static class RunGoalAction extends AbstractAction {
        private final Mojo mojo;
        private final Project project;

        public RunGoalAction(Mojo mojo, Project prj) {
            this.mojo = mojo;
            this.project = prj;
            putValue(Action.NAME, "Execute Mojo");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RunConfig config = RunUtils.createRunConfig(FileUtil.toFile(project.getProjectDirectory()), project, mojo.prefix + ":" + mojo.goal, 
                    Collections.singletonList(mojo.a.getGroupId() + ":" + mojo.a.getArtifactId() + ":" + mojo.a.getVersion() + ":" + mojo.goal));
            //TODO run in RP
            M2ConfigProvider prof = project.getLookup().lookup(M2ConfigProvider.class);
            M2Configuration m2c = prof.getActiveConfiguration(); //TODO in mutex
            if (m2c != null) {
                config.addProperties(m2c.getProperties());
                config.setActivatedProfiles(m2c.getActivatedProfiles());
            }
            RunUtils.run(config);
        }

        
    }
}
