/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.visualweb.propertyeditors.binding.data;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;



/**
 * This panel provides a cloned view of DataSources, WebServices and EJB nodes
 * in the Server Navigator.
 *
 * @author  Winston Prakash
 */
public class DataProviderExplorerPanel extends JPanel implements ExplorerManager.Provider{

    private final TreeView view = new BeanTreeView();
    private final ServernavChildren children = new ServernavChildren();

    TopComponent tc = WindowManager.getDefault().findTopComponent("serverNavigator");

    public DataProviderExplorerPanel() {
        view.setDropTarget(false);
        view.setDragSource(false);
        view.setRootVisible(false);

        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);
        getExplorerManager().setRootContext(new AbstractNode(children));
        initialize();
    }

    /**
     * Initialization the explorer panel.
     */
    public void initialize(){
        FileObject fo = FileUtil.getConfigFile("ServerNavigator");
        if(fo != null){
            DataFolder folder = DataFolder.findFolder(fo);
            final ServerNavigatorFolder servNavFolder = new ServerNavigatorFolder(folder);
            servNavFolder.recreate();
            servNavFolder.instanceFinished();
            fo.addFileChangeListener(new org.openide.filesystems.FileChangeAdapter() {
                public void fileDeleted(org.openide.filesystems.FileEvent evt) {
                    servNavFolder.recreate();
                }

                public void fileDataCreated(org.openide.filesystems.FileEvent evt) {
                    servNavFolder.recreate();
                }
            });
        }
        tc.requestActive();
    }

    public HelpCtx getHelpCtx() {
        Node[] selNodes = getExplorerManager().getSelectedNodes();
        if( selNodes != null && selNodes.length == 1 )
            return selNodes[0].getHelpCtx();
        else
            return HelpCtx.DEFAULT_HELP;
    }

    ExplorerManager  explorerManager = new ExplorerManager();

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }


    /**
     * This class is used to add the nodes specified in the layer file's
     * <code>AddDataProviderDialog</code> folder.
     */
    private final class ServerNavigatorFolder extends FolderInstance {

        public ServerNavigatorFolder(final DataFolder folder) {
            super(folder);
        }
        /**
         * Updates the <code>AddDataProviderDialog</code> with nodes specified in the children of
         *  AddDataProviderDialog folder in the layer file.
         */
        protected Object createInstance(InstanceCookie[] cookies) throws IOException, ClassNotFoundException {
            final List nodes = new ArrayList();
            for(int i=0; i< cookies.length; i++){
                try {
                    Object obj = cookies[i].instanceCreate();
                    if (obj instanceof Node) {
                        Node node =  ((Node) obj).cloneNode();
                        if(node.getName().equals(NbBundle.getMessage(DataProviderExplorerPanel.class,"DATA_SOURCES"))){ // NO_I18N
                                //|| node.getName().equals(NbBundle.getMessage(DataProviderExplorerPanel.class,"ENTERPRISE_JAVA_BEANS")) // NO_I18N
                                //|| node.getName().equals(NbBundle.getMessage(DataProviderExplorerPanel.class,"Web_Services"))){ // NO_I18N
                            nodes.add(node);
                            view.expandNode(node);
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DataProviderExplorerPanel.this.children.updateKeys(nodes);
                }
            });
            return nodes;
        }
    }
    
    
    private class ServernavChildren extends org.openide.nodes.Children.Keys {
        protected Node[] createNodes(Object key) {
            if(key instanceof Node) {
                final Node node = (Node)key;
                
                // XXX Trick to expand the node
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if(view != null) {
                            view.expandNode(node);
                        }
                    }
                });
                return new Node[] {node};
            }
            
            return new Node[0];
        }
        
        protected void removeNotify() {
            setKeys(java.util.Collections.EMPTY_SET);
        }
        
        public void updateKeys(java.util.Collection keys) {
            setKeys(keys);
        }
    }
}
