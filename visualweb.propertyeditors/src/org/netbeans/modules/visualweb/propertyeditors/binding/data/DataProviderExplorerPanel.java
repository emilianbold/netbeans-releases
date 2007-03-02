/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors.binding.data;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
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
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("ServerNavigator");
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
