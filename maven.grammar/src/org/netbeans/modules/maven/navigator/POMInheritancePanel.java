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
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.build.model.ModelLineage;
import org.apache.maven.project.build.model.ModelLineageIterator;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.actions.EditAction;
import org.openide.cookies.EditCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author  mkleint
 */
public class POMInheritancePanel extends javax.swing.JPanel implements ExplorerManager.Provider, Runnable {
    private transient ExplorerManager explorerManager = new ExplorerManager();
    
    private BeanTreeView treeView;
    private DataObject current;
    private FileChangeAdapter adapter = new FileChangeAdapter(){
            @Override
            public void fileChanged(FileEvent fe) {
                showWaitNode();
                RequestProcessor.getDefault().post(POMInheritancePanel.this);
            }
        };

    /** Creates new form POMInheritancePanel */
    public POMInheritancePanel() {
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
        //#164852 somehow a folder dataobject slipped in, test mimetype to avoid that.
        // the root cause of the problem is unknown though
        if (current != null && "text/x-maven-pom+xml".equals(current.getPrimaryFile().getMIMEType())) { //NOI18N
            File file = FileUtil.toFile(current.getPrimaryFile());
            // can be null for stuff in jars?
            if (file != null) {
                try {
                    ModelLineage lin = EmbedderFactory.createModelLineage(file, EmbedderFactory.getOnlineEmbedder(), false);
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
    
    private static class PomChildren extends Children.Keys<ModelLineage> {

        public PomChildren(ModelLineage lineage) {
            setKeys(new ModelLineage[] {lineage});
        }
        
        @Override
        protected Node[] createNodes(ModelLineage key) {
            ModelLineageIterator it = key.reversedLineageIterator();
            List<POMNode> nds = new ArrayList<POMNode>();
            String parentVersion = null;
            while (it.hasNext()) {
                it.next();
                Model mdl = it.getModel();
                File fl = FileUtil.normalizeFile(it.getPOMFile());
                FileObject fo = FileUtil.toFileObject(fl);
                InstanceContent ic = new InstanceContent();
                if (fo != null) {
                    try {
                        DataObject dobj = DataObject.find(ROUtil.checkPOMFileObjectReadOnly(fo, fl));
                        if (dobj != null) {
                            ic.add(dobj);
                            EditCookie ec = dobj.getLookup().lookup(EditCookie.class);
                            if (ec != null) {
                                ic.add(ec);
                            }
                        }
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
                nds.add(0, new POMNode(fl, mdl, new AbstractLookup(ic), parentVersion));
                if (mdl.getVersion() != null) {
                    parentVersion = mdl.getVersion();
                }
            }
            return nds.toArray(new Node[0]);
        }
        
    }

    
    private static class POMNode extends AbstractNode {
        
        private Image icon = ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/Maven2Icon.gif"); // NOI18N
        private boolean readonly = false;
        private POMNode(File key, Model mdl, Lookup lkp, String parentVersion) {
            super( Children.LEAF, lkp);
            String version = mdl.getVersion();
            if (version == null) {
                version = parentVersion;
            }
            setDisplayName(NbBundle.getMessage(POMInheritancePanel.class, "TITLE_PomNode", mdl.getArtifactId(), version));
            if (key.getName().endsWith("pom")) { //NOI18N
                //coming from repository
                readonly = true;
            }
            setShortDescription(key.getAbsolutePath());
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                new MyEditAction()
            };
        }

        @Override
        public Action getPreferredAction() {
            return EditAction.get(EditAction.class);
        }

        @Override
        public String getHtmlDisplayName() {
            if (readonly) {
                return NbBundle.getMessage(POMInheritancePanel.class, "HTML_TITLE_PomNode", getDisplayName());
            }
            return null;
        }
        
        @Override
        public Image getIcon(int type) {
             return icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        private class MyEditAction extends AbstractAction {
            
            public MyEditAction() {
                putValue(NAME, NbBundle.getMessage(POMInheritancePanel.class, "ACTION_Edit"));
                setEnabled(true);
            }
            
            public void actionPerformed(ActionEvent e) {
                EditCookie ec = POMNode.this.getLookup().lookup(EditCookie.class);
                if (ec != null) {
                    ec.edit();
                }
            }
        }
    }
}
