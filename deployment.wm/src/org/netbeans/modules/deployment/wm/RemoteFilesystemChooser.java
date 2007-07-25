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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * RemoteFilesystemChooser.java
 *
 * Created on February 26, 2007, 4:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.deployment.wm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import org.netbeans.mobility.activesync.ActiveSyncException;
import org.netbeans.mobility.activesync.ActiveSyncOps;
import org.netbeans.mobility.activesync.DeviceConnectedListener;
import org.netbeans.mobility.activesync.RemoteFile;
//todo import org.netbeans.modules.java.ui.nodes.SourceNodes;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author suchys
 */
final class RemoteFilesystemChooser extends JPanel implements ExplorerManager.Provider {
    
    private static String currentFolder;
    private String selectedFolder;
            
    private static boolean foldersOnly;
    private ExplorerManager em;
    private static final ImageIcon CDC_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/deployment/wm/resources/cdcProject.png")); // NOI18N
    private static final String FOLDER_ICON_BASE = "org/openide/loaders/defaultFolder"; //NOI18N
     
    private PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())){
                    if (getExplorerManager().getSelectedNodes().length == 1){
                        RemoteFile remote = (RemoteFile) getExplorerManager().getSelectedNodes()[0].getLookup().lookup(RemoteFile.class);
                        if (remote == null){
                            RemoteFilesystemChooser.this.firePropertyChange(NotifyDescriptor.PROP_VALID, true, false);
                            return;
                        }
                        if (foldersOnly && !remote.isDirectory()){
                            RemoteFilesystemChooser.this.firePropertyChange(NotifyDescriptor.PROP_VALID, true, false);
                            return;
                        } else if (!foldersOnly && remote.isDirectory()){
                            RemoteFilesystemChooser.this.firePropertyChange(NotifyDescriptor.PROP_VALID, true, false);
                            return;
                        }
                        String[] path = NodeOp.createPath(getExplorerManager().getSelectedNodes()[0], getExplorerManager().getRootContext());
                        StringBuffer sb = new StringBuffer("\\"); //NOI18N
                        for (int i = 0; i < path.length; i++){
                            sb.append(path[i]);
                            if (i+1 < path.length){
                                sb.append("\\"); //NOI18N
                            }
                        }
                        selectedFolder = sb.toString();
                        RemoteFilesystemChooser.this.firePropertyChange(NotifyDescriptor.PROP_VALID, false, true);
                    } else {
                        RemoteFilesystemChooser.this.firePropertyChange(NotifyDescriptor.PROP_VALID, true, false);
                    }
                }
            }
        };
        
    private ConnectionListener connectionListener;
        
    /** Creates a new instance of RemoteFilesystemChooser */
    public RemoteFilesystemChooser(String curentFolder, final boolean foldersOnly) {
        this.currentFolder = currentFolder;
        this.foldersOnly = foldersOnly;
        this.selectedFolder = curentFolder;
        this.setPreferredSize(new Dimension(400, 500));
        setLayout(new BorderLayout());
        try {
            if (ActiveSyncOps.isAvailable()){
                if (!ActiveSyncOps.getDefault().isDeviceConnected()) {
                    createView(NbBundle.getMessage(RemoteFilesystemChooser.class, "ERROR_DeviceDisconnected")); //NOI18N
                } else {
                    createView(null);
                }
            } else {
                createView(NbBundle.getMessage(RemoteFilesystemChooser.class, "ERROR_ActiveSyntNotInstalled")); //NOI18N
            }
        } catch (ActiveSyncException ex) {
                createView(NbBundle.getMessage(RemoteFilesystemChooser.class, "ERROR_ActiveSyncGeneralError")); //NOI18N
        }
    }
        
    public ExplorerManager getExplorerManager() {
        if (em == null){
            em = new ExplorerManager();
        }
        return em;
    }
    
    public String getSelectedFile(){
        return selectedFolder;
    }
    
    public void addNotify(){
        super.addNotify();
        try {
            if (ActiveSyncOps.getDefault() != null)
                ActiveSyncOps.getDefault().addConnectionListener(connectionListener = new ConnectionListener());
        } catch (ActiveSyncException ex) {
        }
        
    }
    
    public void removeNotify(){
        try {
            if (ActiveSyncOps.getDefault() != null)
                ActiveSyncOps.getDefault().removeConnectionListener(connectionListener);
        } catch (ActiveSyncException ex) {
        }
        super.removeNotify();
    }
    
    private void createView(final String message){
        if ( SwingUtilities.isEventDispatchThread() ){
            if (message == null){
                createFileView();
            } else {
                createErrorView(message);
            }
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createView(message);
                }
            });
        }    
    }

    private void createFileView(){
        setVisible(false);
        removeAll();
        //check the availability if active sync here, if not, put some warning panel here
        BeanTreeView btw = new BeanTreeView();
        btw.setBorder( new LineBorder( Color.BLACK, 1, true ));
        add(btw, BorderLayout.CENTER);
        try {
            getExplorerManager().setRootContext(new AbstractNode(new RemoteFileSystemChildren(ActiveSyncOps.getDefault().getRootFilesystems()[0])){
                public String getName(){
                    return "\\"; //NOI18N
                }
                
                public Image getIcon(int type){
                    return CDC_PROJECT_ICON.getImage();
                }
                
                public Image getOpenedIcon(int i) {
                    return getIcon(i);
                }
            });
        } catch (ActiveSyncException ex) {
            createView(NbBundle.getMessage(RemoteFilesystemChooser.class, "ERROR_ActiveSyncGeneralError")); //NOI18N
        }
        //how to with lazy inicialization?
//        try {
//            Node path = NodeOp.findPath(getExplorerManager().getRootContext(), new StringTokenizer(curentFolder, "/\\")); //NOI18N
//            getExplorerManager().setSelectedNodes(new Node[]{path});
//        } catch (NodeNotFoundException ex) {
//            ex.printStackTrace();
//        } catch (PropertyVetoException ex) {
//            ex.printStackTrace();
//        }
        getExplorerManager().addPropertyChangeListener(pcl);        
        invalidate();
        setVisible(true);
    }
    
    private void createErrorView(String error){
        setVisible(false);
        removeAll();
        getExplorerManager().removePropertyChangeListener(pcl);     
        try {
            getExplorerManager().setRootContext(new AbstractNode(Children.LEAF)); //??
        } catch (Exception e){e.printStackTrace();};
        RemoteFilesystemChooser.this.firePropertyChange(NotifyDescriptor.PROP_VALID, true, false);
        JLabel errorLabel = new JLabel(error);
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        add(errorLabel, BorderLayout.CENTER);
        invalidate();
        setVisible(true);
        
    }
    
    private static class RemoteFileSystemNode extends AbstractNode {
        private RemoteFile file;
                
        RemoteFileSystemNode(RemoteFile file, Children children){
            super(children, Lookups.singleton (file));
            this.file = file;
            setIconBase(FOLDER_ICON_BASE);                       
        }
        
        public String getName(){
            return file.getName();
        }

        /** Overrides folder icon to search for icon in UIManager table for
         * BeanInfo.ICON_COLOR_16x16 type, to allow for different icons
         * across Look and Feels.
         * Keeps possibility of icon annotations.
         */
        public Image getIcon (int type) {
            Image img = null;
            if (type == BeanInfo.ICON_COLOR_16x16) {
                // search for proper folder icon installed by core/windows module
                img = (Image)UIManager.get("Nb.Explorer.Folder.icon"); //NOI18N
            }
            if (img == null) {
                img = super.getIcon(type);
            } 
            return img;
        }
        
        /** Overrides folder icon to search for icon in UIManager table for
         * BeanInfo.ICON_COLOR_16x16 type, to allow for different icons
         * across Look and Feels.
         * Keeps possibility of icon annotations.
         */
        public Image getOpenedIcon (int type) {
            Image img = null;
            if (type == BeanInfo.ICON_COLOR_16x16) {
                // search for proper folder icon installed by core/windows module
                img = (Image)UIManager.get("Nb.Explorer.Folder.openedIcon");
            }
            if (img == null) {
                img = super.getOpenedIcon(type);
            } 
            return img;
        }
    }
    
    private static class RemoteFileSystemChildren extends Children.Keys {
        private RemoteFile folder;
        
        RemoteFileSystemChildren(RemoteFile folder){
            this.folder = folder;
        }
        
        protected Node[] createNodes(Object object) {
            if (object instanceof RemoteFile){
                try {
                    RemoteFile file = (RemoteFile) object;
                    if (foldersOnly){
                        if (!file.isDirectory()){
                            return new AbstractNode[]{};
                        }
                    }
                    return new AbstractNode[] {
                        file.isDirectory() ? 
                        new RemoteFileSystemNode(file, new RemoteFileSystemChildren(file)) : 
                        new RemoteFileSystemNode(file, Children.LEAF)};
                } catch (Exception ex) {
                    return new AbstractNode[]{}; //error here
                }
            } else {
                Node waitNode = new AbstractNode(Children.LEAF);//todo SourceNodes.getExplorerFactory().createWaitNode(); 
                waitNode.setName(NbBundle.getMessage(RemoteFilesystemChooser.class, "NodeWait")); //NOI18N
                return new Node[]{ waitNode };
            }
        }

        protected void addNotify() {
            super.addNotify();
            setKeys(new Object[]{new Object()}); //do a static object
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        ActiveSyncOps activeSync = ActiveSyncOps.getDefault();
                        RemoteFile[] files = activeSync.listFiles(folder);
                        //sort here by folders first, then by names
                        Arrays.sort(files, REMOTE_FILE_COMPARATOR);
                        setKeys(files);            
                    } catch (ActiveSyncException ex) {
                    }            
                }
            });
        }

        protected void removeNotify() {
            super.removeNotify();
            setKeys(Collections.EMPTY_LIST);
        }        
    }
    
    private static Comparator REMOTE_FILE_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            RemoteFile rf1 = (RemoteFile)o1;
            RemoteFile rf2 = (RemoteFile)o2;
            if ( (rf1.isDirectory() && rf2.isDirectory()) || (!rf1.isDirectory() && !rf2.isDirectory())){
                return rf1.getName().compareTo(rf2.getName());
            } else {
                return !rf1.isDirectory() && rf2.isDirectory() ? 1 : -1;
            }
        }
    };
    
    private class ConnectionListener implements DeviceConnectedListener {
        public void onDeviceConnected(boolean connected) {
            if (connected){
                createView(null);
            } else {
                createView(NbBundle.getMessage(RemoteFilesystemChooser.class, "ERROR_DeviceDisconnected")); //NOI18N
                RemoteFilesystemChooser.this.firePropertyChange(NotifyDescriptor.PROP_VALID, true, false);                
            }
        }        
    }    
}
