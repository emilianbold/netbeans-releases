/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.*;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

import org.openide.*;
import org.openide.explorer.*;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.*;
import org.openide.explorer.view.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

/**
 * Component that displays an explorer that displays only certain
 * nodes. Similar to the node selector (retrieved from the TopManager)
 * but arranged a bit differently, plus allows the user to set the
 * currently selected node.
 * @author Joe Warzecha
 */
public class DataObjectListView extends DataObjectPanel implements PropertyChangeListener {
    
    final static int DEFAULT_INSET = 10;
    
    private JFileChooser chooser;
    
    private File rootFile;
    
    /** We must keep created filtered root node because rootNode can be reset any time
     * eg. when setOkButtonEnabled() is called and we need filtered root node to traverse
     * node hierarchy up. */
    private Node filteredRootNode;
    
    public DataObjectListView (PropertyEditorSupport my, PropertyEnv env) {
        super(my, env);
    }
    
    public void addNotify() {
        completeInitialization();
        super.addNotify();
    }
    
    private boolean initialized=false;
    
    /** Called from addNotify. */
    @SuppressWarnings("deprecation")
    private void completeInitialization() {
        if (initialized) {
            //Do not re-initialize if the dialog has already been used,
            //otherwise we will end up listening to the wrong thing and
            //the OK button will never be enabled
            return;
        }
        if (insets != null) {
            setBorder(new EmptyBorder(insets));
        } else {
            setBorder(new EmptyBorder(12, 12, 0, 11));
        }
        setLayout(new BorderLayout(0, 2));
        
        //TODO Is it possible to set any label for JFileChooser?
        /*if (subTitle != null) {
            JLabel l = new JLabel(subTitle);
            l.setLabelFor(reposTree);
            add(l, BorderLayout.NORTH);
        }*/
        
        filteredRootNode = rootNode;
        if (filteredRootNode == null) {
            if (dataFilter != null) {
                if (folderFilter != null) {
                    DataFilter dFilter = new DataFilter() {
                        public boolean acceptDataObject(DataObject obj) {
                            if (folderFilter.acceptDataObject(obj)) {
                                return true;
                            }
                            return dataFilter.acceptDataObject(obj);
                        }
                    };
                    filteredRootNode = RepositoryNodeFactory.getDefault().repository(dFilter);
                } else {
                    filteredRootNode = RepositoryNodeFactory.getDefault().repository(dataFilter);
                }
            } else {
                if (folderFilter != null) {
                    filteredRootNode = RepositoryNodeFactory.getDefault().repository(folderFilter);
                } else {
                    filteredRootNode = RepositoryNodeFactory.getDefault().repository(DataFilter.ALL);
                }
            }
        }

        if (nodeFilter != null) {
            FilteredChildren children = 
                new FilteredChildren(filteredRootNode, nodeFilter, dataFilter);
            FilterNode n = new FilterNode(filteredRootNode, children);
            filteredRootNode = n;
        }
        
        if (rootObject != null) {
            Node n = findNodeForObj(filteredRootNode, rootObject);
            if (n != null) {
                NodeAcceptor naccep = nodeFilter;
                if (naccep == null) {
                    naccep = new NodeAcceptor() {
                        public boolean acceptNodes(Node [] nodes) {
                            return false;
                        }
                    };
                }
                FilteredChildren children =
                    new FilteredChildren(n, naccep, dataFilter);
                FilterNode filtNode = new FilterNode(n, children);
                filteredRootNode = filtNode;
            }
        }
        
        rootFile = new NodeFile(getFileName(filteredRootNode), filteredRootNode);
        
        //Create instance AFTER root file is set!!!
        chooser = new NodeFileChooser(rootFile, new NodeFileSystemView());
        FileEditor.hackFileChooser(chooser);
        //We must initialize it after JFileChooser is created
        if (description != null) {
            setDescription(description);
        } else {
            //Set default value
            setDescription(NbBundle.getMessage(DataObjectListView.class, "ACSD_DataObjectPanel"));
        }
        
        chooser.setControlButtonsAreShown(false);
        chooser.setMultiSelectionEnabled(multiSelection);
        chooser.setFileSelectionMode(selectionMode);
        chooser.addPropertyChangeListener(this);
        
        //set initial selection
        if (dObj != null) {
            String path = findPathTo(filteredRootNode, dObj);
            chooser.setCurrentDirectory(new NodeFile(path, dObj.getNodeDelegate()));
        }
        
        add(chooser, BorderLayout.CENTER);

        if (multiSelection) {
            DataObject [] dObjArr = getDataObjects();
            if ((dataFilter != null) && (dObjArr != null)) {
                boolean b = false;
                for (int i = 0; i < dObjArr.length; i++) {
                    if (dataFilter.acceptDataObject(dObjArr[i])) {
                        b = true;
                        break;
                    }
                }
                setOkButtonEnabled(b);
            } else {
                setOkButtonEnabled(dObjArr != null);
            }
        } else {
            if ((dataFilter != null) && (getDataObject() != null)) {
                setOkButtonEnabled(
                    dataFilter.acceptDataObject(getDataObject())); 
            } else {
                setOkButtonEnabled(getDataObject() != null);
            }
        }
        initialized=true;
    }
    
    private static String findPathTo(Node rootNode, DataObject dobj) {
        Stack<DataObject> st = new Stack<DataObject>();
        DataObject o = dobj;

        while (o != null) {
            st.push(o);
            o = o.getFolder();
        }
        
        Children children = rootNode.getChildren();
        Node n = null;
        while (n == null && !st.isEmpty()) {
            o = st.pop();
            n = children.findChild(o.getNodeDelegate().getName());
            
            if (n == null) {
                Node [] nodes = children.getNodes(true);
                for (int i = 0; (i < nodes.length) && (n == null); i++) {
                    DataObject oo = (DataObject) nodes [i].getCookie(DataObject.class);
                    if ((oo != null) && oo == o) {
                        n = nodes [i];
                    }
                }
            }
        }
        String path = getFileName(rootNode);
        if (n != null) {
            path += File.separator + getFileName(n);

            while (!st.isEmpty()) {
                Node nn = st.pop().getNodeDelegate();
                path += File.separator + getFileName(nn);
            }
        }
        
        return path;
    }
    
    /**
     * Tries to retrieve unique file name from Node -> DataObject -> PrimaryFile
     * if possible. Used to set file name for NodeFile representing node in JFileChooser.
     */
    private static String getFileName (Node n) {
        DataObject dObj = (DataObject) n.getCookie(DataObject.class);
        if (dObj != null) {
            FileObject pf = dObj.getPrimaryFile();
            if (pf.isRoot()) {
                return n.getDisplayName();
            } else {
                return pf.getNameExt();
            }
        } else {
            return n.getDisplayName();
        }
    }
    
    /**
     * Sets description of the panel.
     *
     * @param desc Desciption of the panel.
     */
    public void setDescription(String desc) {
        this.description = desc;
        getAccessibleContext().setAccessibleDescription(desc);
        if (chooser != null) {
            chooser.getAccessibleContext().setAccessibleDescription(desc);
        }
    }
    
    /** Finds node by path from root node. It can return null if node is not found.
     */
    private Node findNode (String path) {
        //Find node corresponding to given path
        Node n = filteredRootNode;
        String p = path;
        String fileName;
        int ind = p.indexOf(File.separatorChar);
        if (ind != -1) {
            fileName = p.substring(0, ind);
            p = p.substring(ind + 1);
        } else {
            fileName = p;
        }
        fileName = fileName.replace('#',File.separatorChar);
        
        //Root node must correspond to root file
        /*if (!fileName.equals(n.getDisplayName())) {
            System.out.println("########### ERROR folder name and node display name does not match #########");
            System.out.println("fileName:" + fileName
            + " nodeName:" + n.getDisplayName());
        }*/

        while (ind != -1) {
            Node [] nodes = n.getChildren().getNodes(true);
            ind = p.indexOf(File.separatorChar);
            if (ind != -1) {
                fileName = p.substring(0, ind);
                p = p.substring(ind + 1);
            } else {
                fileName = p;
            }
            fileName = fileName.replace('#',File.separatorChar);
            //Find node with the same name
            for (int i = 0; i < nodes.length; i++) {
                if (fileName.equals(getFileName(nodes[i]))) {
                    n = nodes[i];
                    break;
                }
            }
        }
        
        //Check if node was found
        if (!fileName.equals(getFileName(n))) {
            return null;
        }
        
        return n;
    }
    
    /** Creates node by path from root node. It either returns existing node as
     * findNode does or creates instance of FakeNode. Created node is NOT added
     * to node hierarchy.
     */
    private Node createNode (String path) {
        //Find node corresponding to given path
        Node n = filteredRootNode;
        Node parent = null;
        String p = path;
        String fileName;
        int ind = p.indexOf(File.separatorChar);
        if (ind != -1) {
            fileName = p.substring(0, ind);
            p = p.substring(ind + 1);
        } else {
            fileName = p;
        }
        fileName = fileName.replace('#',File.separatorChar);
        
        //Root node must correspond to root file
        /*if (!fileName.equals(n.getDisplayName())) {
            System.out.println("########### ERROR folder name and node display name does not match #########");
            System.out.println("fileName:" + fileName
            + " nodeName:" + n.getDisplayName());
        }*/

        while (ind != -1) {
            Node [] nodes = n.getChildren().getNodes(true);
            parent = n;
            ind = p.indexOf(File.separatorChar);
            if (ind != -1) {
                fileName = p.substring(0, ind);
                p = p.substring(ind + 1);
            } else {
                fileName = p;
            }
            fileName = fileName.replace('#',File.separatorChar);
            //Find node with the same name
            for (int i = 0; i < nodes.length; i++) {
                if (fileName.equals(getFileName(nodes[i]))) {
                    n = nodes[i];
                    break;
                }
            }
        }
        
        if (!fileName.equals(getFileName(n))) {
            //Create new node
            n = new FakeNode(Children.LEAF);
            n.setDisplayName(fileName.replace('#',File.separatorChar));
        }
        
        /*if (parent != null) {
            System.out.println("ADD NODE TO PARENT");
            parent.getChildren().add(new Node [] { n });
        }*/
        
        return n;
    }
    
    /**
     * Return the currently selected DataObject. 
     * @return The currently selected DataObject or null if there is no node seleted
     */
    public DataObject getDataObject() {
        DataObject retValue = null;
        if (!multiSelection) {
            File f = chooser.getSelectedFile();
            if (f instanceof NodeFile) {
                Node n = ((NodeFile) f).getNode();
                if (n != null) {
                    retValue = (DataObject) n.getCookie(DataObject.class);
                }
            }
        }
        return retValue;
    }
    
    /**
     * Return the currently selected Node. 
     * @return The currently selected Node or null if there is no node seleted
     */
    public Node getNode() {
        Node retValue = null;
        if (!multiSelection) {
            File f = chooser.getSelectedFile();
            if (f instanceof NodeFile) {
                retValue = ((NodeFile) f).getNode();
            }
        }
        return retValue;
    }
    
    /**
     * Return the currently selected DataObject. 
     * @return The currently selected DataObject or null if there is no node seleted
     */
    public DataObject [] getDataObjects () {
        DataObject [] retValue = null;
        if (multiSelection) {
            File [] f = chooser.getSelectedFiles();
            retValue = new DataObject [f.length];
            for (int i = 0; i < f.length; i++) {
                if (f[i] instanceof NodeFile) {
                    Node n = ((NodeFile) f[i]).getNode();
                    if (n != null) {
                        retValue[i] = (DataObject) n.getCookie(DataObject.class);
                    }
                }
            }
        }
        return retValue;
    }
    
    /**
     * Return the currently selected Node. 
     * @return The currently selected Node or null if there is no node seleted
     */
    public Node [] getNodes () {
        Node [] retValue = null;
        if (multiSelection) {
            File [] f = chooser.getSelectedFiles();
            retValue = new Node [f.length];
            for (int i = 0; i < f.length; i++) {
                if (f[i] instanceof NodeFile) {
                    retValue[i] = ((NodeFile) f[i]).getNode();
                }
            }
        }
        return retValue;
    }
    
    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does not contain a valid property value
     *           (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        if (multiSelection) {
            return getDataObjects();
        } else {
            return getDataObject();
        }
    }
    
    /** Property change listaner attached to the JFileChooser chooser. */
    public void propertyChange(PropertyChangeEvent e) {
        if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(e.getPropertyName()) ||
            JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName())) {
            File[] selFiles = (File[]) chooser.getSelectedFiles();
            if (selFiles == null) {
                return;
            }

            if ((selFiles.length == 0) && (chooser.getSelectedFile() != null)) {
                selFiles = new File[] { chooser.getSelectedFile() };
            }
                        
            Node [] nodes = new Node [selFiles.length];
            for (int i = 0; i < selFiles.length; i++) {
                if (selFiles[i] instanceof NodeFile) {
                    //Get node directly
                    nodes[i] = ((NodeFile) selFiles[i]).getNode();
                } else {
                    //Try to find node by path
                    nodes[i] = findNode(selFiles[i].getPath());
                }
            }
            
            ArrayList<DataObject> dObjList = new ArrayList<DataObject>(selFiles.length);
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] != null) {
                    DataObject dObj = (DataObject) nodes[i].getCookie(DataObject.class);
                    if (dObj != null) {
                        if (dataFilter != null) {
                            if (dataFilter.acceptDataObject(dObj)) {
                                dObjList.add(dObj);
                            }
                        } else {
                            dObjList.add(dObj);
                        }
                    }
                }
            }
            
            DataObject [] dObjArray = dObjList.toArray(new DataObject[dObjList.size()]);
            boolean enableOK = false;
            if (dObjArray.length > 0) {
                enableOK = true;
            } else {
                enableOK = false;
            }
            if (multiSelection) {
                myEditor.setValue(dObjArray);
            } else {
                if (dObjArray.length > 0) {
                    myEditor.setValue(dObjArray[0]);
                } else {
                    myEditor.setValue(null);
                }
            }
            setOkButtonEnabled(enableOK);
        } else if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName())) {
        }
    }
    
    /** Fake node used to create NodeFile for nonexisting node. JFileChooser calls
     * FileSystemView.createFileObject() when renaming existing file.
     */
    private static class FakeNode extends AbstractNode {
        
        public FakeNode (Children children) {
            super(children);
        }
    }
    
    /** Used by JFileChooser to display File instances from our fake
     * file system representing node hierarchy.
     */
    private class NodeFile extends File {
        private Node n;
        
        NodeFile (String path, Node n) {
            super(path);
            this.n = n;
        }
        
        NodeFile (File parent, String child, Node n) {
            super(parent,child);
            this.n = n;
        }
        
        public boolean canRead() {
            return true;
        }
        
        public boolean canWrite() {
            return true;
        }
        
        public boolean renameTo (File dest) {
            DataObject dObj = (DataObject) n.getCookie(DataObject.class);
            if (dObj != null) {
                try {
                    dObj.rename(dest.getName());
                } catch (IOException exc) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
        
        public File[] listFiles () {
            Node [] nodes = n.getChildren().getNodes(true);
            NodeFile [] files = new NodeFile[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                String name = getFileName(nodes[i]);
                name = name.replace(File.separatorChar,'#');
                files[i] = new NodeFile(getPath() + File.separator + name, nodes[i]);
            }
            return files;
        }
        
        public String getName () {
            if (n != null) {
                return n.getDisplayName();
            } else {
                return super.getName();
            }
        }
        
        public String getParent () {
            String p = super.getParent();
            return p;
        }
        
        public File getParentFile () {
            String p = this.getParent();
            if (p == null) {
                return null;
            }
            if (n == null) {
                return null;
            }
            Node parent = findNode(p);
            if (parent == null) {
                return null;
            }
            return new NodeFile(p, parent);
        }
        
        public boolean exists () {
            Node n = findNode(getPath());
            if (n != null) {
                if (n instanceof FakeNode) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
        
        public boolean isAbsolute () {
            String p = getPath();
            int ind = p.indexOf(File.separatorChar);
            if (ind != -1) {
                //Get root of path
                p = p.substring(0, ind);
            }
            p = p.replace('#',File.separatorChar);
            if (p.equals(getFileName(filteredRootNode))) {
                return true;
            } else {
                return false;
            }
        }
        
        public boolean isDirectory () {
            if (n == null) {
                return false;
            }
            /*Node [] nodes = n.getChildren().getNodes(true);
            System.out.println("NodeFile.isDirectory sz:" + nodes.length);
            if (nodes.length > 0) {
                System.out.println("NodeFile.isDirectory LEAVE 2 true f:" + this);
                return true;
            } else {
                System.out.println("NodeFile.isDirectory LEAVE 2 false f:" + this);
                return false;
            }*/
            DataObject dObj = (DataObject) n.getCookie(DataObject.class);
            if (dObj != null) {
                if (dObj instanceof DataFolder) {
                    return true;
                } else {
                    return false;
                }
            } else {
                //Always root??
                return true;
            }
        }
        
        public boolean isFile () {
            if (n == null) {
                return true;
            }
            /*Node [] nodes = n.getChildren().getNodes(true);
            System.out.println("NodeFile.isFile sz:" + nodes.length);
            if (nodes.length > 0) {
                System.out.println("NodeFile.isFile LEAVE 2 false f:" + this);
                return false;
            } else {
                System.out.println("NodeFile.isFile LEAVE 3 true f:" + this);
                return true;
            }*/
            DataObject dObj = (DataObject) n.getCookie(DataObject.class);
            if (dObj != null) {
                if (dObj instanceof DataFolder) {
                    return false;
                } else {
                    return true;
                }
            } else {
                //Always root??
                return false;
            }
        }
        
        public Icon getIcon () {
            Icon icon = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            return icon;
        }
        
        public String getAbsolutePath() {
            return getPath();
        }
        
        public File getAbsoluteFile() {
            return new NodeFile(getAbsolutePath(), n);
        }
        
        public String getCanonicalPath() throws IOException {
            return getPath();
        }
        
        public File getCanonicalFile() throws IOException {
            return new NodeFile(getCanonicalPath(), n);
        }
        
        public Node getNode () {
            return n;
        }
        
    }
    
    /** Used by JFileChooser to display File instances from our fake
     * file system representing node hierarchy.
     */
    private class NodeFileView extends FileView {
        
        NodeFileView () {
            super();
        }
        
        public String getName (File f) {
            if (f instanceof NodeFile) {
                return f.getName();
            } else {
                //Try to locate corresponding node by path
                Node n = findNode(f.getPath());
                if (n != null) {
                    return n.getDisplayName();
                } else {
                    return null;
                }
            }
        }
        
        public Icon getIcon (File f) {
            if (f instanceof NodeFile) {
                return ((NodeFile) f).getIcon();
            } else {
                //Try to locate corresponding node by path
                Node n = findNode(f.getPath());
                if (n != null) {
                    Icon icon = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
                    return icon;
                } else {
                    return null;
                }
            }
        }
    }
    
    /** Used by JFileChooser to display File instances from our fake
     * file system representing node hierarchy.
     */
    private class NodeFileSystemView extends FileSystemView {
        private final String newFolderString =
                UIManager.getString("FileChooser.other.newFolder"); // NOI18N
        
        NodeFileSystemView () {
            super();
        }
        
        /**
         * Determines if the given file is a root partition or drive.
         */
        public boolean isRoot(File f) {
            return rootFile.equals(f);
        }
        
        /** Creates a new folder with a default folder name.
         *
         */
        public File createNewFolder(File containingDir) throws IOException {
            String path = containingDir.getPath() + File.separator + newFolderString;
            Node n = findNode(path);
            if (n != null) {
                NodeFile folder = new NodeFile(path, n);
                return folder;
            } else {
                Node parent = findNode(containingDir.getPath());
                if (parent == null) {
                    return null;
                }
                DataObject dObj = (DataObject) parent.getCookie(DataObject.class);
                if (dObj != null) {
                    if (dObj instanceof DataFolder) {
                        DataFolder.create((DataFolder) dObj, newFolderString);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
                n = createNode(path);
                NodeFile folder = new NodeFile(path, n);
                return folder;
            }
        }
        
        public File createFileObject(File dir, String filename) {
            filename = filename.replace(File.separatorChar,'#');
            //Find node corresponding to given path
            String path = dir.getPath() + File.separator + filename;
            Node n = findNode(path);
            if (n == null) {
                n = createNode(path);
            }
            NodeFile file = new NodeFile(path, n);
            return file;
        }

        /**
         * Returns a File object constructed from the given path string.
         */
        public File createFileObject(String path) {
            //Find node corresponding to given path
            Node n = findNode(path);
            if (n == null) {
                n = createNode(path);
            }
            NodeFile file = new NodeFile(path, n);
            return file;
        }
        
        /**
         * Returns whether a file is hidden or not.
         */
        public boolean isHiddenFile(File f) {
            return false;
        }
        
        /**
         * Returns all root partitians on this system. For example, on Windows,
         * this would be the A: through Z: drives.
         */
        public File[] getRoots() {
            return new NodeFile [] { (NodeFile) rootFile };
        }
        
        public File getHomeDirectory() {
            return rootFile;
        }
        
        public File[] getFiles (File dir, boolean useFileHiding) {
            if (dir instanceof NodeFile) {
                return dir.listFiles();
            } else {
                return super.getFiles(dir, useFileHiding);
            }
        }
        
        public File getParentDirectory (File dir) {
            if (dir != null) {
                File f = createFileObject(dir.getPath());
                File parent = f.getParentFile();
                return parent;
            }
            return null;
        }
        
        public String getSystemDisplayName (File f) {
            return f.getName();
        }
        
    }
    
    /** Extended JFileChooser. We have to overwrite some methods because
     * UI implementation creates its own instances of java.io.File -> it causes
     * trouble with our fake filesystem for example java.io.File.exists() returns
     * false when our fake path is provided. */
    public class NodeFileChooser extends JFileChooser {
        
        NodeFileChooser (File currentDirectory, FileSystemView fsv) {
            super(currentDirectory, fsv);
        }
        
        protected void setup(FileSystemView view) {
            // MCF Bug 4972092
            // If the  FileView is not set, the calls to getIcon will trigger
            // null pointer exceptions. And getIcon is called indirectly from 
            // the super version of JFileChooser.setup
            setFileView(new DataObjectListView.NodeFileView());
            super.setup(view);
        }
        
        public void setCurrentDirectory (File dir) {
            if ((DataObjectListView.this != null) && (dir != null) && !(dir instanceof NodeFile)) {
                Node n = findNode(dir.getPath());
                dir = new NodeFile(dir.getPath(), n);
            }
            super.setCurrentDirectory(dir);
        }
        
    }
}
