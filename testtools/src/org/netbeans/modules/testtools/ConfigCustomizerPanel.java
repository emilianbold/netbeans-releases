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
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.io.ReaderInputStream;
import org.openide.xml.EntityCatalog;
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
        } catch (IOException ioe) {
            fail(ioe);
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
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value instanceof Unknown) {
                super.getTreeCellRendererComponent(tree, value.toString().length()==0?"<html><i>&lt;noname&gt;":value, sel, expanded, leaf, row,hasFocus);
                String msg=((Unknown)value).errorMessage();
                Icon i=msg.length()>0?((Unknown)value).getErrorIcon():((Unknown)value).getIcon();
                msg=((Unknown)value).className()+"<br>"+msg;
                if (msg.endsWith("<br>")) msg=msg.substring(0, msg.length()-3);
                setToolTipText("<html>"+msg);
                if (i!=null) {
                    setIcon(i);
                }
            } else super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,hasFocus);
            return this;
        }
    }
    static class MyCellEditor extends DefaultTreeCellEditor {
        public MyCellEditor(JTree tree) {
            super(tree, (MyCellRenderer)tree.getCellRenderer());
        }
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row) {
            if (value instanceof Unknown) {
                String msg=((Unknown)value).errorMessage();
                Icon i=(msg.length()==0)?((Unknown)value).getIcon():((Unknown)value).getErrorIcon();
                if (i!=null) {
                    if(leaf)
                        renderer.setLeafIcon(i);
                    else if(expanded)
                        renderer.setOpenIcon(i);
                    else
                        renderer.setClosedIcon(i);
                }
            }
            return super.getTreeCellEditorComponent(tree, value, sel, expanded, leaf, row);
        }
        public boolean isCellEditable(EventObject event) {
            if (event instanceof MouseEvent) {
                Object o=this.tree.getPathForLocation(((MouseEvent)event).getX(), ((MouseEvent)event).getY()).getLastPathComponent();
                return  ((o instanceof Include)||(o instanceof TestSet))&&super.isCellEditable(event);
            }
            return super.isCellEditable(event);
        }
    }
    
    DataObject dob;
    Document doc;
    
    private static void fail(Throwable t) {
        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, t);
    }
    
    /** Creates new form ComponentsEditorPanel
     * @param gen ComponentGenerator instance */
    public ConfigCustomizerPanel(XMLDataObject dob) {
//        try {
            this.dob=dob;
            this.doc=((AntProjectCookie)(dob.getCookie(AntProjectCookie.class))).getDocument();
//            StyledDocument stdoc=((EditorCookie)dob.getCookie(EditorCookie.class)).openDocument();
//            DocumentBuilder builder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            builder.setEntityResolver(EntityCatalog.getDefault());
//            this.doc=builder.parse(new ReaderInputStream(new StringReader(stdoc.getText(0, stdoc.getLength()))));
//            this.doc=dob.getDocument();
            TreeNode rootNode=createNode(doc.getDocumentElement());
            initComponents();
            MyCellRenderer rend = new MyCellRenderer();
            tree.setCellRenderer(rend);
            tree.setCellEditor(new MyCellEditor(tree));
            tree.getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION); 
            tree.setModel(new DefaultTreeModel(rootNode));
            tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    nodeChanged(tree.getSelectionPaths());
                }
            });
            for (int i=0; i<tree.getRowCount(); i++) tree.expandRow(i);
            tree.setEditable(true);
/*
        } catch (IOException ioe) {
            fail(ioe);
        } catch (SAXException saxe) {
            fail(saxe);
        } catch (ParserConfigurationException pce) {
            fail(pce);
        } catch (BadLocationException ble) {
            fail(ble);
        }
 */
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
            TreePath paths[]=tree.getSelectionPaths();
            for (int i=0; i<paths.length; i++)
                ((Unknown)paths[i].getLastPathComponent()).delete();
        }
    }//GEN-LAST:event_treeKeyReleased

    private void treeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseClicked
        TreePath tp;
        if ((evt.getModifiers()==evt.BUTTON3_MASK)&&((tp=tree.getPathForLocation(evt.getX(), evt.getY()))!=null)) {
            if (!tree.isPathSelected(tp)) {
                tree.clearSelection();
                tree.addSelectionPath(tp);
            }
            if (tree.getSelectionCount()==1) {
                Unknown u=(Unknown)tree.getSelectionPath().getLastPathComponent();
                popup=u.getPopupMenu();
                if ((u instanceof Include)||(u instanceof TestSet)) {
                    if (popup.getComponentCount()>0) popup.add(new JSeparator());
                    popup.add(new JMenuItem("Rename")).addActionListener(u);
                }
            } else {
                popup=new JPopupMenu();
            }
            if (!tree.isRowSelected(0)) {
                JMenuItem del=new JMenuItem("Delete");
                del.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
                TreePath paths[]=tree.getSelectionPaths();
                for (int i=0; i<paths.length; i++)
                    del.addActionListener((Unknown)paths[i].getLastPathComponent());
                if (popup.getComponentCount()>0) popup.add(new JSeparator());
                popup.add(del);
            }
            popup.show(tree,evt.getX(),evt.getY());
        } else if (popup!=null) {
            popup.setVisible(false);
        }
    }//GEN-LAST:event_treeMouseClicked
    
   
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
/*    
    protected void setModified() {
        try {
            EditorCookie cookie=(EditorCookie)dob.getCookie(EditorCookie.class);
            final StringWriter sw=new StringWriter();
            XMLSerializer ser=new XMLSerializer(sw, new OutputFormat("xml","UTF-8",true)); // NOI18N
            ser.serialize(doc);
            sw.close();
            final StyledDocument stdoc=cookie.openDocument();
            NbDocument.runAtomicAsUser(stdoc, new Runnable() {
                public void run() {
                    try {
                        stdoc.remove(0, stdoc.getLength());
                        stdoc.insertString(0, sw.toString(), null);
                    } catch (BadLocationException e) {
                        if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                            e.printStackTrace();
                    }
                }
            });
            dob.setModified(true);
        } catch (IOException ioe) {
            fail(ioe);
        } catch (BadLocationException ble) {
            fail(ble);
        }
    }
*/    
    public class Unknown extends DefaultMutableTreeNode implements ActionListener {
        public Unknown(Element e) {
            super(e);
            NodeList list=e.getChildNodes();
            for (int i=0; i<list.getLength(); i++) {
                if (list.item(i) instanceof Element)
                    add(createNode((Element)list.item(i)));
            }
        }
        public String className() {
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
//            setModified();
        }
        public String toString() {
            //return className()+" "+getAttribute("name");
            return getAttribute("name");
        }
        public void delete() {
            Element e=((Element)getUserObject());
            e.getParentNode().removeChild(e);
            ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(this);
//            setModified();
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
        protected void addMenuItem(String action, Icon icon) {
            if (popupMenu==null) popupMenu=new JPopupMenu();
            JMenuItem it=new JMenuItem("Add "+action, icon);
            it.addActionListener(this);
            popupMenu.add(it);
        }
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().startsWith("Add ")) {
                Element el=(Element)getUserObject();
                Unknown node=createNode((Element)el.appendChild(createElement(el, ae.getActionCommand())));
                ((DefaultTreeModel)tree.getModel()).insertNodeInto(node, this, getChildCount());
//                setModified();
            } else if (ae.getActionCommand().equals("Delete")) {
                delete();
            } else if (ae.getActionCommand().equals("Rename")) {
                System.out.println("rename");
                tree.startEditingAtPath(tree.getSelectionPath());
            }
        }
        public String errorMessage() {
            return "unknown element type: "+((Element)getUserObject()).getNodeName();
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
        public void setUserObject(Object o) {
            setObject(o);
        }
        protected void setObject(Object o) {}
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
            addMenuItem("Property", propertyIcon);
            addMenuItem("TestSet", testsetIcon);
        }
        private boolean findElement(String id, String name) {
            Document d=((Element)getUserObject()).getOwnerDocument();
            NodeList nl=d.getElementsByTagName(id);
            for (int i=0; i<nl.getLength(); i++)
                if (((Element)nl.item(i)).getAttribute("name").equals(name)) return true;
            return false;
        }
        
        public String errorMessage() {
            return super.errorMessage()+(getTestAttributes().length()==0?"testAttributes are required<br>":"")+(containsChild(TestSet.class)?"":"TestSet element is required<br>")+
            (findElement("executor",getExecutor())?"":"wrong Executor name<br>")+(getCompiler().length()==0||findElement("compiler",getCompiler())?"":"wrong Compiler name<br>")+(getResultsProcessor().length()==0||findElement("resultsprocessor",getResultsProcessor())?"":"wrong ResultsProcessor name");
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
            //return className()+" "+getDirectory();
            return getDirectory();
        }
        protected void createPopupMenu() {
            addMenuItem("PatternSet", patternsetIcon);
        }
        public String errorMessage() {
            return toString().length()==0?"directory attribute is required":"";
        }
        public Icon getIcon() {
            return testsetIcon;
        }
        public Icon getErrorIcon() {
            return testsetErrIcon;
        }
        protected void setObject(Object o) {
            setAttribute("dir", o.toString());
            nodeChanged(tree.getSelectionPaths());
        }
    }
    
    public class PatternSet extends Unknown {
        
        public PatternSet(Element e) {
            super(e);
        }
        public String toString() {
            //return className();
            return "<html><i>PatternSet";
        }
        protected void createPopupMenu() {
            addMenuItem("Include", includeIcon);
            addMenuItem("Exclude", excludeIcon);
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
            return getName().length()==0?"name is required<br>":"";
        }
        public Icon getIcon() {
            return includeIcon;
        }
        public Icon getErrorIcon() {
            return includeErrIcon;
        }
        protected void setObject(Object o) {
            setName(o.toString());
            nodeChanged(tree.getSelectionPaths());
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
            addMenuItem("TestBag", testbagIcon);
            addMenuItem("Executor", executorIcon);
            addMenuItem("Compiler", compilerIcon);
            addMenuItem("ResultProcessor", resultIcon);
        }
        public String errorMessage() {
            return super.errorMessage()+(containsChild(TestBag.class)?"":"TestBag element is required<br>")+(containsChild(Executor.class)?"":"Executor element is required<br>")+(containsChild(ResultsProcessor.class)?"":"ResultsProcessor element is required");
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
        XMLDataObject dob=(XMLDataObject)DataObject.find(Repository.getDefault().findResource("results/htmlresults/cfg-unit.xml"));
        JDialog d=new JDialog();
        d.getContentPane().add(new ConfigCustomizerPanel(dob));
        d.show();
    }    
}
