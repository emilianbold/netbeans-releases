/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConfigCustomizerPanel.java
 *
 * Created on November 28, 2002, 11:16 AM
 */
package org.netbeans.modules.testtools;

import javax.swing.tree.*;
import org.openide.DialogDescriptor;
import javax.swing.event.*;
import org.openide.nodes.Node;
import org.openide.nodes.BeanNode;
import java.beans.IntrospectionException;
import java.io.InputStream;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class ConfigCustomizerPanel extends javax.swing.JPanel implements ChangeListener {
    
    private static ImageIcon loadImage(String path) {
        try {
            InputStream in = ConfigCustomizerPanel.class.getClassLoader().getResourceAsStream("org/netbeans/modules/testtools/resources/"+path); // NO I18N
            byte b[] = new byte[in.available()];
            in.read(b);
            in.close();
            return new ImageIcon(b);
        } catch (Exception e) {
            System.out.println(path);
        }
        return null;
    }
        
    static final ImageIcon unknownIcon = loadImage("UnknownIcon.gif"); // NOI18N
    static final ImageIcon configIcon = loadImage("ConfigIcon.gif"); // NOI18N
    static final ImageIcon configErrIcon = loadImage("ConfigErr.gif"); // NOI18N
    static final ImageIcon testbagIcon = loadImage("TestBagIcon.gif"); // NOI18N
    static final ImageIcon testbagErrIcon = loadImage("TestBagErr.gif"); // NOI18N
    static final ImageIcon propertyIcon = loadImage("PropertyIcon.gif"); // NOI18N
    static final ImageIcon propertyErrIcon = loadImage("PropertyErr.gif"); // NOI18N
    static final ImageIcon testsetIcon = loadImage("TestSetIcon.gif"); // NOI18N
    static final ImageIcon testsetErrIcon = loadImage("TestSetErr.gif"); // NOI18N
    static final ImageIcon patternsetIcon = loadImage("PatternSetIcon.gif"); // NOI18N
    static final ImageIcon includeIcon = loadImage("IncludeIcon.gif"); // NOI18N
    static final ImageIcon includeErrIcon = loadImage("IncludeErr.gif"); // NOI18N
    static final ImageIcon excludeIcon = loadImage("ExcludeIcon.gif"); // NOI18N
    static final ImageIcon excludeErrIcon = loadImage("ExcludeErr.gif"); // NOI18N
    static final ImageIcon executorIcon = loadImage("ExecutorIcon.gif"); // NOI18N
    static final ImageIcon executorErrIcon = loadImage("ExecutorErr.gif"); // NOI18N
    static final ImageIcon compilerIcon = loadImage("CompilerIcon.gif"); // NOI18N
    static final ImageIcon compilerErrIcon = loadImage("CompilerErr.gif"); // NOI18N
    static final ImageIcon resultIcon = loadImage("ResultsProcessorIcon.gif"); // NOI18N
    static final ImageIcon resultErrIcon = loadImage("ResultsProcessorErr.gif"); // NOI18N
    
    Collection nodes;
    JPopupMenu popup;

    static class MyCellRenderer extends DefaultTreeCellRenderer {
        public MyCellRenderer() {
            super();
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value instanceof Unknown) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,hasFocus);
                String msg=((Unknown)value).errorMessage();
                Icon i=null;
                if (msg.length()==0) {
                    setToolTipText(null);
                    i=((Unknown)value).getIcon();
                } else {
                    if (msg.endsWith(", ")) msg=msg.substring(0, msg.length()-2);
                    setToolTipText(msg);
                    i=((Unknown)value).getErrorIcon();
                }
                if (i!=null) setIcon(i);
            } else super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,hasFocus);
            return this;
        }

    }
    
    /** Creates new form ComponentsEditorPanel
     * @param gen ComponentGenerator instance */
    public ConfigCustomizerPanel(XMLDataObject dob) throws IOException, SAXException {
        TreeNode rootNode=createNode(dob.getDocument().getDocumentElement());
        initComponents();
        MyCellRenderer rend = new MyCellRenderer();
        tree.setCellRenderer(rend);
        tree.getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION); 
        tree.setModel(new DefaultTreeModel(rootNode));
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                nodeChanged(tree.getSelectionPaths());
            }
        });
    }
    
    void nodeChanged(TreePath paths[]) {
        if (paths==null) {
            propertySheet.setNodes(new Node[0]);
        } else try {
            Node nodes[]=new Node[paths.length];
            for (int i=0; i<paths.length; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
                nodes[i]=new BeanNode(node);
            }
            propertySheet.setNodes(nodes);
        } catch (IntrospectionException ex) {
            propertySheet.setNodes(new Node[0]);
        }
    }        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        splitPane = new javax.swing.JSplitPane();
        scrollPane = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        propertySheet = new org.openide.explorer.propertysheet.PropertySheet();

        setLayout(new java.awt.BorderLayout());

        splitPane.setDividerLocation(415);
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(0.5);
        splitPane.setPreferredSize(new java.awt.Dimension(800, 400));
        scrollPane.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigCustomizerPanel.class, "TTT_ComponentsTree", new Object[] {}));
        tree.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigCustomizerPanel.class, "TTT_ComponentsTree", new Object[] {}));
        tree.setShowsRootHandles(true);
        tree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                treeKeyReleased(evt);
            }
        });

        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
        });

        scrollPane.setViewportView(tree);
        tree.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfigCustomizerPanel.class, "LBL_ComponentTree", new Object[] {}));

        splitPane.setLeftComponent(scrollPane);

        propertySheet.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigCustomizerPanel.class, "TTT_Properties", new Object[] {}));
        propertySheet.setDisplayWritableOnly(true);
        splitPane.setRightComponent(propertySheet);
        propertySheet.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfigCustomizerPanel.class, "LBL_Properties", new Object[] {}));

        add(splitPane, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void treeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeKeyReleased
        if ((evt.getKeyCode()==KeyEvent.VK_DELETE)&&(evt.getModifiers()==0)&&(tree.getSelectionCount()>0)&&!tree.isRowSelected(0)) {
            DeleteActionPerformed();
        }
    }//GEN-LAST:event_treeKeyReleased

    private void treeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseClicked
        if ((evt.getModifiers()==evt.BUTTON3_MASK)&&(tree.getSelectionCount()>0)) {
            if (tree.getSelectionCount()==1) {
                popup=((Unknown)tree.getSelectionPath().getLastPathComponent()).getPopupMenu();
            } else {
                popup=new JPopupMenu();
            }
            if (!tree.isRowSelected(0)) {
                JMenuItem del=new JMenuItem("Delete");
                del.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
                del.addActionListener(new java.awt.event.ActionListener() { // NOI18N
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        DeleteActionPerformed();
                    }
                });
                popup.add(del);
            }
            popup.show(tree,evt.getX(),evt.getY());
        } else if (popup!=null) {
            popup.setVisible(false);
        }
    }//GEN-LAST:event_treeMouseClicked
    
    void DeleteActionPerformed() {
        TreePath paths[]=tree.getSelectionPaths();
        for (int i=0; paths!=null&&i<paths.length; i++) {
            ((Unknown)paths[i].getLastPathComponent()).delete();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTree tree;
    private javax.swing.JScrollPane scrollPane;
    private org.openide.explorer.propertysheet.PropertySheet propertySheet;
    // End of variables declaration//GEN-END:variables
    
    /** implementation of StateListener
     * @param changeEvent ChangeEvent */    
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        DefaultTreeModel model=(DefaultTreeModel)tree.getModel();
        model.nodeChanged((Unknown)changeEvent.getSource());
        nodeChanged(tree.getSelectionPaths());
    }
    
    public Unknown createNode(Element e) {
        String name=e.getNodeName();
        if (name.equals("mconfig")) return new Config(e);
        if (name.equals("testbag")) return new TestBag(e);
        if (name.equals("testproperty")) return new Property(e);
        if (name.equals("testset")) return new TestSet(e);
        if (name.equals("patternset")) return new PatternSet(e);
        if (name.equals("include")) return new Include(e);
        if (name.equals("exclude")) return new Exclude(e);
        if (name.equals("executor")) return new Executor(e);
        if (name.equals("compiler")) return new Compiler(e);
        if (name.equals("resultsprocessor")) return new ResultsProcessor(e);
        return new Unknown(e);
    }
    
    public Element createElement(Element parent, String action) {
        if (action.equals("Add TestBag")) return parent.getOwnerDocument().createElement("testbag");
        if (action.equals("Add Property")) return parent.getOwnerDocument().createElement("testproperty");
        if (action.equals("Add TestSet")) return parent.getOwnerDocument().createElement("testset");
        if (action.equals("Add PatternSet")) return parent.getOwnerDocument().createElement("patternset");
        if (action.equals("Add Include")) return parent.getOwnerDocument().createElement("include");
        if (action.equals("Add Exclude")) return parent.getOwnerDocument().createElement("exclude");
        if (action.equals("Add Executor")) return parent.getOwnerDocument().createElement("executor");
        if (action.equals("Add Compiler")) return parent.getOwnerDocument().createElement("compiler");
        if (action.equals("Add ResultProcessor")) return parent.getOwnerDocument().createElement("resultsprocessor");
        return null;
    }
    
    public class Unknown extends DefaultMutableTreeNode implements ActionListener {
        public Unknown(Element e) {
            super(e);
            NodeList list=e.getChildNodes();
            for (int i=0; i<list.getLength(); i++) {
                if (list.item(i) instanceof Element)
                    add(createNode((Element)list.item(i)));
            }
        }
        protected String className() {
            String cln=getClass().getName();
            return cln.substring(cln.lastIndexOf('$')+1);
        }
        protected String getAttribute(String name) {
            return ((Element)getUserObject()).getAttribute(name);
        }
        protected void setAttribute(String name, String value) {
            if (value==null || value.length()<1) {
                ((Element)getUserObject()).removeAttribute(name);
            } else {
                ((Element)getUserObject()).setAttribute(name, value);
            }
            ((DefaultTreeModel)tree.getModel()).nodeChanged(this);
        }
        public String toString() {
            return className()+" "+getAttribute("name");
        }
        public void delete() {
            Element e=((Element)getUserObject());
            e.getParentNode().removeChild(e);
            ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(this);
        }
        private JPopupMenu popupMenu;
        public JPopupMenu getPopupMenu() {
            popupMenu=null;
            createPopupMenu();
            return popupMenu;
        }
        protected void createPopupMenu() {
            popupMenu=new JPopupMenu();
        }
        protected void addMenuItem(String action) {
            if (popupMenu==null) popupMenu=new JPopupMenu();
            JMenuItem it=new JMenuItem("Add "+action);
            it.addActionListener(this);
            popupMenu.add(it);
        }
        public void actionPerformed(ActionEvent ae) {
            Element el=(Element)getUserObject();
            Unknown node=createNode((Element)el.appendChild(createElement(el, ae.getActionCommand())));
            ((DefaultTreeModel)tree.getModel()).insertNodeInto(node, this, getChildCount());
        }
        public String errorMessage() {
            return "unknown element type";
        }
        protected boolean containsChild(Class clazz) {
            Enumeration ch=children();
            while (ch.hasMoreElements())
             if (clazz.equals(ch.nextElement().getClass())) return true;
            return false;
        }
        public Icon getIcon() {
            return unknownIcon;
        }
        public Icon getErrorIcon() {
            return unknownIcon;
        }
    }

    public class TestBag extends Include {
        public TestBag(Element e) {
            super(e);
        }
        public String getTestAttributes() {
            return getAttribute("testattribs");
        }
        public void setTestAttributes(String value) {
            setAttribute("testattribs", value);
        }
        public String getExecutor() {
            return getAttribute("executor");
        }
        public void setExecutor(String value) {
            setAttribute("executor", value);
        }
        public String getCompiler() {
            return getAttribute("compiler");
        }
        public void setCompiler(String value) {
            setAttribute("compiler", value);
        }
        public String getResultsProcessor() {
            return getAttribute("resultsprocessor");
        }
        public void setResultsProcessor(String value) {
            setAttribute("resultsprocessor", value);
        }
        public String getPriority() {
            return getAttribute("prio");
        }
        public void setPriority(String value) {
            Integer.parseInt(value);
            setAttribute("prio", value);
        }
        protected void createPopupMenu() {
            addMenuItem("Property");
            addMenuItem("TestSet");
        }
        private boolean findElement(String id, String name) {
            Document d=((Element)getUserObject()).getOwnerDocument();
            NodeList nl=d.getElementsByTagName(id);
            for (int i=0; i<nl.getLength(); i++)
                if (((Element)nl.item(i)).getAttribute("name").equals(name)) return true;
            return false;
        }
        
        public String errorMessage() {
            return super.errorMessage()+(getTestAttributes().length()==0?"testAttributes are required, ":"")+(containsChild(TestSet.class)?"":"TestSet element is required, ")+
            (findElement("executor",getExecutor())?"":"wrong Executor name, ")+(getCompiler().length()==0||findElement("compiler",getCompiler())?"":"wrong Compiler name, ")+(findElement("resultsprocessor",getResultsProcessor())?"":"wrong ResultsProcessor name");
        }
        public Icon getIcon() {
            return testbagIcon;
        }
        public Icon getErrorIcon() {
            return testbagErrIcon;
        }
    }
    
    public class Property extends Include {
        public Property(Element e) {
            super(e);
        }
        public String getValue() {
            return getAttribute("value");
        }
        public void setValue(String value) {
            setAttribute("value", value);
        }
        public String errorMessage() {
            return super.errorMessage()+(getValue().length()==0?"value attribute is required":"");
        }
        public Icon getIcon() {
            return propertyIcon;
        }
        public Icon getErrorIcon() {
            return propertyErrIcon;
        }
    }
    
    public class TestSet extends Unknown {
        public TestSet(Element e) {
            super(e);
        }
        public String getDirectory() {
            return getAttribute("dir");
        }
        public void setDirectory(String value) {
            setAttribute("dir", value);
        }
        public String toString() {
            return className()+" "+getDirectory();
        }
        protected void createPopupMenu() {
            addMenuItem("PatternSet");
        }
        public String errorMessage() {
            return getDirectory().length()==0?"directory attribute is required":"";
        }
        public Icon getIcon() {
            return testsetIcon;
        }
        public Icon getErrorIcon() {
            return testsetErrIcon;
        }
    }
    
    public class PatternSet extends Unknown {
        public PatternSet(Element e) {
            super(e);
        }
        public String toString() {
            return className();
        }
        protected void createPopupMenu() {
            addMenuItem("Include");
            addMenuItem("Exclude");
        }
        public String errorMessage() {
            return "";
        }
        public Icon getIcon() {
            return patternsetIcon;
        }
        public Icon getErrorIcon() {
            return patternsetIcon;
        }
    }
    
    public class Include extends Unknown {
        public Include(Element e) {
            super(e);
        }
        public String getName() {
            return getAttribute("name");
        }
        public void setName(String value) {
            setAttribute("name", value);
        }
        public String errorMessage() {
            return getName().length()==0?"name is required, ":"";
        }
        public Icon getIcon() {
            return includeIcon;
        }
        public Icon getErrorIcon() {
            return includeErrIcon;
        }
    }
    
    public class Exclude extends Include {
        public Exclude(Element e) {
            super(e);
        }
        public Icon getIcon() {
            return excludeIcon;
        }
        public Icon getErrorIcon() {
            return excludeErrIcon;
        }
    }
    
    public class Config extends Include {
        public Config(Element e) {
            super(e);
        }
        protected void createPopupMenu() {
            addMenuItem("TestBag");
            addMenuItem("Executor");
            addMenuItem("Compiler");
            addMenuItem("ResultProcessor");
        }
        public String errorMessage() {
            return super.errorMessage()+(containsChild(TestBag.class)?"":"TestBag element is required, ")+(containsChild(Executor.class)?"":"Executor element is required, ")+(containsChild(ResultsProcessor.class)?"":"ResultsProcessor element is required");
        }
        public Icon getIcon() {
            return configIcon;
        }
        public Icon getErrorIcon() {
            return configErrIcon;
        }
    }
    
    public class Executor extends Include {
        public Executor(Element e) {
            super(e);
        }
        public String getAntFile() {
            return getAttribute("antfile");
        }
        public void setAntFile(String value) {
            setAttribute("antfile", value);
        }
        public String getTarget() {
            return getAttribute("target");
        }
        public void setTarget(String value) {
            setAttribute("target", value);
        }
        public String getDirectory() {
            return getAttribute("dir");
        }
        public void setDirectory(String value) {
            setAttribute("dir", value);
        }
        public boolean isDefault() {
            String s=getAttribute("default");
            return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equals("1");
        }
        public void setDefault(boolean value) {
            setAttribute("default", value?"yes":"no");
        }
        public Icon getIcon() {
            return executorIcon;
        }
        public Icon getErrorIcon() {
            return executorErrIcon;
        }
    }

    public class Compiler extends Executor {
        public Compiler(Element e) {
            super(e);
        }
        public Icon getIcon() {
            return compilerIcon;
        }
        public Icon getErrorIcon() {
            return compilerErrIcon;
        }
    }    

    public class ResultsProcessor extends Executor {
        public ResultsProcessor(Element e) {
            super(e);
        }
        public Icon getIcon() {
            return resultIcon;
        }
        public Icon getErrorIcon() {
            return resultErrIcon;
        }
    }    
    
    public static class UnknownBeanInfo extends SimpleBeanInfo {

        /** returns Bean Info of ancestor class
         * @return BeanInfo */    
        public BeanInfo[] getAdditionalBeanInfo () {
            return new BeanInfo[0];
        }

        /** returns Property Descriptors of ConfigDataLoader properties
         * @return array of PropertyDescriptor */    
        public PropertyDescriptor[] getPropertyDescriptors() {
            return new PropertyDescriptor[0];
        }
        
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        XMLDataObject dob=(XMLDataObject)DataObject.find(Repository.getDefault().findResource("cfg-unit.xml"));
        JDialog d=new JDialog();
        d.getContentPane().add(new ConfigCustomizerPanel(dob));
        d.show();
    }    
}
