/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.visualweb.propertyeditors;

import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.WeakHashMap;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignProject;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A custom property editor for URL properties. URLs may be edited by hand in a
 * combo box, which makes current URLs found elsewhere in the project as
 * alternative selections. A URL to a project resource or page may be constructed
 * by choosing a target from a tree of target nodes. In this case, the editor
 * will generate a "context relative" URL (a URL that begins with '/').
 *
 * @author gjmurphy
 */
public class UrlPropertyPanel extends PropertyPanelBase {
    
    protected static ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle");
    
    /**
     * A map of projects to the most recently examined file path for that
     * project.
     */
    protected static WeakHashMap directoryMap = new WeakHashMap();
    
    /**
     * A default file filter used to hide project files that aren't deployed (NB
     * backup files), and project files that cannot be targeted (JSP page fragments
     * and files that are in the WEB-INF directory).
     */
    static FileFilter defaultFileFilter = new FileFilter() {
        public boolean accept(File file) {
            String fileName = file.getName();
            if (fileName == null)
                return false;
            if (file.isDirectory() && fileName.equals("WEB-INF")) //NOI18N
                return false;
            if (fileName.endsWith(".jspf") || fileName.endsWith("~")) //NOI18N
                return false;
            return true;
        }
    };
    
    UrlPropertyEditor editor;
    DesignContext designContext;
    ResourceNode rootTreeNode;
    DefaultTreeModel treeModel;
    
    
    /** Creates new form UrlPropertyPanel */
    public UrlPropertyPanel(UrlPropertyEditor editor) {
        super(editor);
        this.editor = editor;
        DesignBean designBean = editor.getDesignProperty().getDesignBean();
        DesignProperty designProperty = editor.getDesignProperty();
        this.designContext = designBean.getDesignContext();
        this.rootTreeNode = generateTargetTree(designContext);
        this.treeModel = new DefaultTreeModel(rootTreeNode);
        initComponents();
        // Resource trees are generally small, so by default, expand all directories
        // at the project level
        for (int i = 0; i < rootTreeNode.getChildCount(); i++) {
            TreeNode n = rootTreeNode.getChildAt(i);
            resourceTree.expandPath(new TreePath(treeModel.getPathToRoot(n)));
        }
        // Insert into combo box URLs found in this property on all other instances
        // of this component in context
        DesignBean[] beans = designContext.getBeansOfType(designBean.getInstance().getClass());
        if (designProperty.getPropertyDescriptor() != null) {
            if (beans != null && beans.length > 0) {
                String propertyName = designProperty.getPropertyDescriptor().getName();
                for (int i = 0; i < beans.length; i++) {
                    String url = (String) beans[i].getProperty(propertyName).getValue();
                    if (url != null)
                        this.urlComboBox.addItem(url);
                }
                
            }
        }
        // If URL property already set, update display
        if (editor.getValue() != null) {
            String url = UrlPropertyEditor.decodeUrl(editor.getValue().toString());
            this.urlComboBox.setSelectedItem(url);
            // If url has no protocol, it points to a project resource, so ensure
            // that the resource is visible in the resource tree
            if (url.indexOf(":") < 0) {
                TreePath path = urlToTreePath(url, this.rootTreeNode);
                this.resourceTree.makeVisible(path);
                this.resourceTree.setSelectionPath(path);
            }
        } else {
            this.urlComboBox.setSelectedItem("");
        }
        
    }
    
    public Object getPropertyValue() throws IllegalArgumentException {
        return UrlPropertyEditor.encodeUrl((String) this.urlComboBox.getSelectedItem());
    }
    
    protected File getLastDirectory() {
        Object key = this.editor.getDesignProperty().getDesignBean().getDesignContext().getProject();
        return (File) UrlPropertyPanel.directoryMap.get(key);
    }
    
    protected void setLastDirectory(File dir) {
        Object key = this.editor.getDesignProperty().getDesignBean().getDesignContext().getProject();
        UrlPropertyPanel.directoryMap.put(key, dir);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        radioButtonGroup = new javax.swing.ButtonGroup();
        resourceScrollPane = new javax.swing.JScrollPane();
        resourceTree = new javax.swing.JTree(treeModel);
        addButton = new javax.swing.JButton();
        urlPanel = new javax.swing.JPanel();
        urlLabel = new javax.swing.JLabel();
        urlComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        resourceScrollPane.setMinimumSize(new java.awt.Dimension(120, 80));
        resourceTree.setCellRenderer(new NodeRenderer());
        resourceTree.setMinimumSize(new java.awt.Dimension(120, 80));
        resourceTree.setRootVisible(false);
        resourceTree.setShowsRootHandles(true);
        resourceTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeValueChanged(evt);
            }
        });

        resourceScrollPane.setViewportView(resourceTree);
        resourceTree.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("UrlPropertyPanel.resourceTree.AccessibleName"));
        resourceTree.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("UrlPropertyPanel.resourceTree.AccessibleDescription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 11, 11);
        add(resourceScrollPane, gridBagConstraints);

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("UrlPropertyPanel.add.label.mnemonic").charAt(0));
        addButton.setText(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("UrlPropertyPanel.add.label"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 7);
        add(addButton, gridBagConstraints);

        urlPanel.setLayout(new java.awt.GridBagLayout());

        urlLabel.setText(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("UrlPropertyPanel.url.label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        urlPanel.add(urlLabel, gridBagConstraints);

        urlComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        urlPanel.add(urlComboBox, gridBagConstraints);
        urlComboBox.getAccessibleContext().setAccessibleName("");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(7, 11, 0, 7);
        add(urlPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void treeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeValueChanged
        TreePath path = evt.getNewLeadSelectionPath();
        // If user has selected a node that equates to a valid URL, generate it,
        // otherwise do nothing.
        if (path != null) {
            ResourceNode n = (ResourceNode) path.getPathComponent(path.getPathCount() - 1);
            if (n instanceof FileNode && !((FileNode) n).getFile().isDirectory())
                urlComboBox.setSelectedItem(n.getUrlPathString());
            else if (n instanceof TargetNode)
                urlComboBox.setSelectedItem(n.getUrlPathString());
        }
    }//GEN-LAST:event_treeValueChanged
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        final JFileChooser fileChooser = new JFileChooser();
        UrlFileFilter fileFilter = this.editor.getFileFilter();
        if (fileFilter != null)
            fileChooser.addChoosableFileFilter(fileFilter);
        File lastDir = getLastDirectory();
        if (lastDir != null)
            fileChooser.setCurrentDirectory(lastDir);
        String approveMessage =
                resourceBundle.getString("UrlPropertyPanel.add.label"); //NOI18N
        if (fileChooser.showDialog(this, approveMessage) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file == null || !file.exists())
                return;
            try {
                DesignProject project = this.designContext.getProject();
                project.addResource(file.toURI().toURL(),
                        new URI("web/resources/" + UrlPropertyEditor.encodeUrl(file.getName())));
                String url = "/resources/" + file.getName();
                ResourceNode resourcesNode = getNode("/resources", this.rootTreeNode);
                if (resourcesNode == null) {
                    File rootFile = ((FileNode) this.rootTreeNode).getFile();
                    resourcesNode = new FileNode(this.rootTreeNode, new File(rootFile, "/resources"));
                    this.rootTreeNode.add(resourcesNode);
                }
                FileNode childNode = new FileNode(resourcesNode, file);
                this.treeModel.insertNodeInto(childNode, resourcesNode, resourcesNode.getChildCount());
                this.resourceTree.setSelectionPath(urlToTreePath(url, this.rootTreeNode));
                this.urlComboBox.setSelectedItem(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        setLastDirectory(fileChooser.getCurrentDirectory());
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.ButtonGroup radioButtonGroup;
    private javax.swing.JScrollPane resourceScrollPane;
    private javax.swing.JTree resourceTree;
    private javax.swing.JComboBox urlComboBox;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JPanel urlPanel;
    // End of variables declaration//GEN-END:variables
    
    
    protected TreePath urlToTreePath(String url, TreeNode treeNode) {
        ResourceNode resourceNode = getNode(url, treeNode);
        if (resourceNode == null) {
            return new TreePath(new TreeNode[] {treeNode});
        }
        ArrayList nodeList = new ArrayList();
        while (resourceNode != null) {
            nodeList.add(0, resourceNode);
            resourceNode = (ResourceNode) resourceNode.getParent();
        }
        return new TreePath(nodeList.toArray());
    }
    
    protected ResourceNode getNode(String url, TreeNode treeNode) {
        Stack nodeStack = new Stack();
        nodeStack.push(treeNode);
        ResourceNode resourceNode = null;
        while (!nodeStack.isEmpty() && resourceNode == null) {
            ResourceNode node = (ResourceNode) nodeStack.pop();
            if (url.equals(node.getUrlPathString()))
                resourceNode = node;
            if (node.getChildCount() > 0)
                nodeStack.addAll(Collections.list(node.children()));
        }
        return resourceNode;
    }
    
    ResourceNode generateTargetTree(DesignContext designContext) {
        // Generate a map of page file names to page nodes. Each page node will
        // contain children nodes for all URL targets on the page, whether
        // the targets are rendered by components or encoded directly in the JSP.
        HashMap pagesMap = new HashMap(16);
        
        DesignContext[] contexts = designContext.getProject().getDesignContexts();
        //DesignContext[] contexts = getDesignContexts (editor.getDesignProperty().getDesignBean());
        
        for (int i = 0; i < contexts.length; i++) {
            DesignBean pageBean = contexts[i].getRootContainer();
            if (pageBean != null && pageBean.getInstance() instanceof UIViewRoot) {
                PageNode pageNode = new PageNode(null, pageBean);
                // To the page node, add a node for every JSF component that, when
                // rendered, generates a URL target
                DesignBean beans[] = contexts[i].getBeans();
                for (int j = 0; j < beans.length; j++) {
                    Object instance = beans[j].getInstance();
                    if (instance == null) {
                        // XXX #144220 Logging illegal state.
                        info(new IllegalStateException("There was returned null instance from design bean, designBean=" + beans[j])); // NOI18N
                    } else if (UIComponent.class.isAssignableFrom(instance.getClass()) && editor.isTargetComponent((UIComponent) instance)) {
                        TargetNode target = new ComponentTargetNode(pageNode, beans[j]);
                        pageNode.add(target);
                    }
                }
                // To the page node, add a node for every HTML target anchor found
                // in the page's JSP document
                if (pageBean.getChildBeanCount() > 0 && pageBean.getChildBeans()[0] instanceof MarkupDesignBean) {
                    Element pageElement = ((MarkupDesignBean) pageBean.getChildBeans()[0]).getElement();
                    NodeList anchorList = pageElement.getElementsByTagName("a");
                    for (int j = 0; j < anchorList.getLength(); j++) {
                        Element element = (Element)anchorList.item(j);
                        if (element.hasAttribute("name")) {
                            TargetNode target = new ElementTargetNode(pageNode, element);
                            pageNode.add(target);
                        }
                    }
                }
                try {
                    File pageFileDir = new File(contexts[i].resolveResource(".").toURI());
                    File pageFile = new File(pageFileDir, pageBean.getInstanceName() + ".jsp");
                    pagesMap.put(pageFile, pageNode);
                } catch (URISyntaxException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        // Create target tree, where a target is an available project resource.
        // A stack is used to descend recursively through the project resource
        // file hierarchy.
        FileNode rootNode = new FileNode();
        try {
            DesignProject project = this.designContext.getProject();
            File file = project.getResourceFile(new URI("web")); //NOI18N
            FileFilter fileFilter = this.defaultFileFilter;
            UrlFileFilter urlFileFilter = this.editor.getFileFilter();
            if (urlFileFilter != null)
                fileFilter = urlFileFilter.getIOFileFilter();
            rootNode.setFile(file);
            Stack nodeStack = new Stack();
            nodeStack.push(rootNode);
            while (!nodeStack.isEmpty()) {
                FileNode node = (FileNode) nodeStack.pop();
                file = node.getFile();
                if (file.isDirectory()) {
                    File[] childFiles = file.listFiles(fileFilter);
                    for (int i = 0; i < childFiles.length; i++) {
                        File childFile = childFiles[i];
                        if (pagesMap.containsKey(childFile)) {
                            PageNode childNode = (PageNode) pagesMap.get(childFile);
                            childNode.setParent(node);
                            childNode.setFile(childFile);
                            node.add(childNode);
                        } else {
                            FileNode childNode = new FileNode(node, childFile);
                            node.add(childNode);
                            nodeStack.push(childNode);
                        }
                    }
                }
            }
        } catch (URISyntaxException e){
        }
        return rootNode;
    }
    
    // For performance improvement. No need to get all the contexts in the project
    private DesignContext[] getDesignContexts(DesignBean designBean){
        DesignProject designProject = designBean.getDesignContext().getProject();
        DesignContext[] contexts;
        if (designProject instanceof FacesDesignProject) {
            contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                "request",
                "session",
                "application"
            });
        } else {
            contexts = new DesignContext[0];
        }
        DesignContext[] designContexts = new DesignContext[contexts.length + 1];
        designContexts[0] = designBean.getDesignContext();
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }
    
    static class ResourceNode implements MutableTreeNode {
        
        ArrayList children;
        MutableTreeNode parentNode;
        
        ResourceNode() {
            this.children = new ArrayList();
        }
        
        ResourceNode(MutableTreeNode parentNode) {
            this.children = new ArrayList();
            this.parentNode = parentNode;
        }
        
        public TreeNode getChildAt(int index) {
            return (TreeNode) children.get(index);
        }
        
        void add(MutableTreeNode node) {
            children.add(node);
        }
        
        public void insert(MutableTreeNode child, int index) {
            this.children.add(index, child);
        }
        
        void clear() {
            children.clear();
        }
        
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }
        
        public boolean isLeaf() {
            return getChildCount() == 0 ? true : false;
        }
        
        public TreeNode getParent() {
            return parentNode;
        }
        
        public void setParent(MutableTreeNode node) {
            this.parentNode = node;
        }
        
        public int getChildCount() {
            return children.size();
        }
        
        public boolean getAllowsChildren() {
            return true;
        }
        
        public Enumeration children() {
            return Collections.enumeration(children);
        }
        
        char getPathSepChar() {
            return '/';
        }
        
        public String toString() {
            return null;
        }
        
        private String urlString;
        
        String getUrlString() {
            return this.urlString;
        }
        
        void setUrlString(String urlString) {
            this.urlString = urlString;
        }
        
        String getUrlPathString() {
            if (getUrlString() == null)
                return null;
            if (getParent() == null)
                return getPathSepChar() + getUrlString();
            String parentPathString = ((ResourceNode) getParent()).getUrlPathString();
            if (parentPathString == null)
                return getPathSepChar() + getUrlString();
            return parentPathString + getPathSepChar() + getUrlString();
        }
        
        public void remove(MutableTreeNode node) {
            this.children.remove(node);
        }
        
        public void remove(int index) {
            this.children.remove(index);
        }
        
        public void removeFromParent() {
            this.parentNode = null;
        }
        
        public void setUserObject(Object object) {
        }
        
    }
    
    static class FileNode extends ResourceNode {
        
        String suffix;
        
        FileNode() {
            super();
        }
        
        FileNode(MutableTreeNode parentNode) {
            super(parentNode);
        }
        
        FileNode(MutableTreeNode parentNode, File file) {
            super(parentNode);
            this.setFile(file);
            super.setUrlString(file.getName());
        }
        
        private File file;
        
        File getFile() {
            return this.file;
        }
        
        void setFile(File file) {
            this.file = file;
            String fileName = this.file.getName();
            if (!file.isDirectory()) {
                int i = fileName.lastIndexOf('.');
                suffix = null;
                if (i >= 0)
                    suffix = fileName.substring(i + 1);
            }
        }
        
        String getSuffix() {
            return suffix;
        }
        
        public String toString() {
            return this.file.getName();
        }
        
    }
    
    
    static class PageNode extends FileNode {
        
        DesignBean pageBean;
        
        PageNode(MutableTreeNode parentNode, DesignBean pageBean) {
            super(parentNode);
            this.pageBean = pageBean;
            super.setUrlString(pageBean.getInstanceName() + ".jsp");
        }
        
        public String toString() {
            return this.pageBean.getInstanceName();
        }
        
        String getUrlPathString() {
            return "/faces" + super.getUrlPathString();
        }
        
    }
    
    
    static abstract class TargetNode extends ResourceNode {
        
        TargetNode(MutableTreeNode parentNode) {
            super(parentNode);
        }
        
        char getPathSepChar() {
            return '#';
        }
        
    }
    
    
    static class ElementTargetNode extends TargetNode {
        
        Element element;
        
        ElementTargetNode(MutableTreeNode parentNode, Element element) {
            super(parentNode);
            this.element = element;
            super.setUrlString(element.getAttribute("name"));
        }
        
        Element getElement() {
            return element;
        }
        
        public String toString() {
            return element.getAttribute("name");
        }
        
    }
    
    
    static class ComponentTargetNode extends TargetNode {
        
        DesignBean componentBean;
        
        ComponentTargetNode(MutableTreeNode parentNode, DesignBean componentBean) {
            super(parentNode);
            this.componentBean = componentBean;
            super.setUrlString(((UIComponent) componentBean.getInstance()).getId());
        }
        
        DesignBean getComponentDesignBean() {
            return componentBean;
        }
        
        public String toString() {
            return componentBean.getInstanceName();
        }
        
    }
    
    static class NodeRenderer extends DefaultTreeCellRenderer {
        
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof ComponentTargetNode) {
                setIcon(new ImageIcon(((ComponentTargetNode)value).getComponentDesignBean().getBeanInfo().getIcon(BeanInfo.ICON_COLOR_16x16)));
            } else if (value instanceof FileNode) {
                try {
                    FileObject fileObject = FileUtil.toFileObject(((FileNode) value).getFile());
                    Image fileImage = DataObject.find(fileObject).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                    setIcon(new ImageIcon(fileImage));
                } catch (DataObjectNotFoundException e) {
                }
            }
            return this;
        }
        
    }

    private static void info(Exception ex) {
        Logger.getLogger(UrlPropertyPanel.class.getName()).log(Level.INFO, null, ex);
    }
}
