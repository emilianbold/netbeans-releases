/*
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.explorer;

import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.QueueTool;

import org.netbeans.jemmy.operators.Operator;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;

import javax.swing.border.BevelBorder;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.jemmy.util.Dumper;


/**
 * An application allowing to explore a Java GUI application.
 * Could be executed by command: <br>
 * <pre>
 * java "application java options" \
 *   org.netbeans.jemmy.explorer.GUIBrowser \
 *   "application main class" \
 *   "application parameters"
 * </pre>
 * or from java code by <code>GUIBrowser.showBrowser()</code> method using.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class GUIBrowser extends JFrame{
    
    private static String WINDOWS_TAB = "Subwindows";
    private static String COMPONENTS_TAB = "Hierarchy";
    private static String PROPERTIES_TAB = "Properties";
    private static String REFLECTION_TAB = "Reflection";
    private static String EVENT_TAB = "Events";
    private static String IMAGE_TAB = "Image";
    
    boolean exit;
    PropertyDialog propDialog;
    RootNode root;
    QueueTool qt;
    JTextField refreshDelay;
    JTree mainTree;
    JLabel status;
    JButton viewButton;
    JButton expandButton;
    JSplitPane split;
    ByteArrayOutputStream dumpData;
    PrintWriter dumpWriter;
    
    private GUIBrowser(boolean exitNecessary) {
        super("GUI Browser");
        
        dumpData = new ByteArrayOutputStream();
        dumpWriter = new PrintWriter(new OutputStreamWriter(dumpData));
                
        exit = exitNecessary;
        propDialog = new PropertyDialog(this);
        qt = new QueueTool();
        qt.setOutput(TestOut.getNullOutput());
        root = new RootNode();
        
        mainTree = new JTree(root.getWindowModel());
        mainTree.setCellRenderer(new WindowRenderer());
        mainTree.setEditable(false);
        
        refreshDelay = new JTextField(3);
        refreshDelay.setText("0");
        
        viewButton = new JButton("View");
        viewButton.setEnabled(false);
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ComponentBrowser(getOwnr(),
                        (ComponentNode)mainTree.getSelectionPath().
                        getLastPathComponent()).
                        show();
            }
        });
        
        expandButton = new JButton("Expand All");
        expandButton.setEnabled(false);
        expandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                expandAll(mainTree,
                        mainTree.getSelectionPath());
            }
        });
        
        JButton refreshButton = new JButton("Reload in ...");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reload(new Integer(refreshDelay.getText()).intValue());
            }
        });
        
        JButton dumpButton = new JButton("Dump");
        dumpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser =
                        new JFileChooser(System.getProperty("user.home"));
                chooser.addChoosableFileFilter(new FileFilter() {
                    public boolean accept(File f) {
                        return(f.getName().endsWith(".xml"));
                    }
                    public String getDescription() {
                        return("xml files");
                    }
                });
                chooser.showSaveDialog(GUIBrowser.this);
                try {
                    File file = chooser.getSelectedFile();
                    OutputStream output = new FileOutputStream(file);
                    output.write(dumpData.toByteArray());
                } catch(IOException ee) {
                    ee.printStackTrace();
                }
            }
        });
        
        JPanel refreshPane = new JPanel();
        refreshPane.add(viewButton);
        refreshPane.add(expandButton);
        refreshPane.add(new JLabel(""));
        refreshPane.add(new JLabel(""));
        refreshPane.add(new JLabel(""));
        refreshPane.add(new JLabel(""));
        refreshPane.add(new JLabel(""));
        refreshPane.add(new JLabel(""));
        refreshPane.add(refreshButton);
        refreshPane.add(refreshDelay);
        refreshPane.add(new JLabel("seconds     "));
        refreshPane.add(dumpButton);
        
        split = createUnderPane(mainTree);
        
        JPanel nonStatusPane = new JPanel();
        nonStatusPane.setLayout(new BorderLayout());
        nonStatusPane.add(refreshPane, BorderLayout.SOUTH);
        nonStatusPane.add(split, BorderLayout.CENTER);
        
        split.setDividerLocation(0.8);
        
        status = new JLabel("Reloaded");
        
        JPanel statusPane = new JPanel();
        statusPane.setLayout(new BorderLayout());
        statusPane.add(status, BorderLayout.CENTER);
        statusPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        
        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new BorderLayout());
        bottomPane.add(statusPane, BorderLayout.NORTH);
        bottomPane.add(statusPane, BorderLayout.CENTER);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(bottomPane, BorderLayout.SOUTH);
        getContentPane().add(nonStatusPane, BorderLayout.CENTER);
        
        Component[] cpss = {viewButton, expandButton};
        mainTree.addTreeSelectionListener(new SelectionManager(cpss));
        
        addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent e) {}
            public void windowClosed(WindowEvent e) {
                setVisible(false);
                if(exit) {
                    System.exit(0);
                }
            }
            public void windowClosing(WindowEvent e) {}
            public void windowDeactivated(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowOpened(WindowEvent e) {}
        });
        addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {}
            public void componentMoved(ComponentEvent e) {}
            public void componentResized(ComponentEvent e) {
                split.setDividerLocation(0.8);
            }
            public void componentShown(ComponentEvent e) {}
        });
        
        setSize(800, 400);
    }
    
    boolean shown = false;
    public void show() {
        super.show();
        viewButton.setEnabled(false);
        if(!shown) {
            split.setDividerLocation(0.8);
            shown = true;
        }
    }
    
    /**
     * Specifies a status text.
     * @param st a status text.
     */
    public void setStatus(String st) {
        status.setText(st);
    }
    
    /**
     * Method to invoke GUIBrowser from java code.
     */
    public static void showBrowser() {
        showBrowser(new String[0], false);
    }
    
    /**
     * Method to invoke GUIBrowser as java application.
     * @param argv Argument array. If not empty, first element should be<br>
     * main class of an aplication to be browsed.<br>
     * Other elements are application parameters.
     */
    public static void main(String[] argv) {
        showBrowser(argv, true);
    }
    
    private static void showBrowser(String[] argv, boolean exitNecessary) {
        if(argv.length >= 1) {
            String[] newArgv = new String[argv.length -1];
            for(int i = 1; i < argv.length; i++) {
                newArgv[i-1] = argv[i];
            }
            try {
                new ClassReference(argv[0]).startApplication(newArgv);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        new GUIBrowser(exitNecessary).show();
    }
    
    private void reload(final int delay) {
        viewButton.setEnabled(false);
        expandButton.setEnabled(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    for(int i = delay - 1; i >= 0 ; i--) {
                        setStatus("Reloading after " + Integer.toString(i) + " second");
                        Thread.sleep(1000);
                    }
                    setStatus("Reloading ...");
                    Dumper.dumpAll(dumpWriter);
                    qt.lock();
                    root = new RootNode();
                    qt.unlock();
                    mainTree.setModel(root.getWindowModel());
                    setStatus("Reloaded");
                } catch(InterruptedException e) {}
            }
        }).start();
    }
    
    private JFrame getOwnr() {
        return(this);
    }
    
    private class ClassNode {
        Class clzz;
        protected ClassNode() {
            clzz = null;
        }
        public ClassNode(Class clzz) {
            this.clzz = clzz;
        }
        public ClassNode[] getSuperClasses() {
            int count = 0;
            Class parent = clzz;
            while((parent = parent.getSuperclass()) != null) {
                count++;
            }
            Class[] interfaces = clzz.getInterfaces();
            ClassNode[] result = new ClassNode[count + interfaces.length + 1];
            result[0] = new SuperClassNode(clzz);
            count = 1;
            parent = clzz;
            while((parent = parent.getSuperclass()) != null) {
                result[count] = new SuperClassNode(parent);
                count++;
            }
            for(int i = count; i < count + interfaces.length; i++) {
                result[i] = new InterfaceNode(interfaces[i - count]);
            }
            return(result);
        }
        public String toString() {
            if(clzz.isArray()) {
                return(clzz.getComponentType().getName() + "[]");
            } else {
                return(clzz.getName());
            }
        }
        public TreeModel getMethodsModel() {
            return(new ClassModel(this));
        }
        public ClassNode[] getSubNodes() {
            Vector res = new Vector();
            Field[] fields = clzz.getFields();
            Arrays.sort(fields, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return(((Field)o1).getName().compareTo(((Field)o2).getName()));
                }
            });
            Method[] mtds = clzz.getMethods();
            Arrays.sort(mtds, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return(((Method)o1).getName().compareTo(((Method)o2).getName()));
                }
            });
            for(int i = 0; i < fields.length; i++) {
                if(fields[i].getDeclaringClass().getName().equals(clzz.getName())) {
                    res.add(new FieldNode(fields[i]));
                }
            }
            for(int i = 0; i < mtds.length; i++) {
                if(mtds[i].getDeclaringClass().getName().equals(clzz.getName())) {
                    res.add(new MethodNode(mtds[i]));
                }
            }
            ClassNode[] result = new ClassNode[res.size()];
            for(int i = 0; i < result.length; i++) {
                result[i] = (ClassNode)res.get(i);
            }
            return(result);
        }
    }
    private class SuperClassNode extends ClassNode {
        protected SuperClassNode() {
            super();
        }
        public SuperClassNode(Class clzz) {
            super(clzz);
        }
        public String toString() {
            return("Class: " + super.toString());
        }
    }
    private class InterfaceNode extends ClassNode {
        protected InterfaceNode() {
            super();
        }
        public InterfaceNode(Class clzz) {
            super(clzz);
        }
        public String toString() {
            return("Interfac: " + super.toString());
        }
    }
    private class FieldNode extends ClassNode {
        Field field;
        public FieldNode(Field fld) {
            super(fld.getType());
            field = fld;
        }
        public String toString() {
            return("Field: " +
                    Modifier.toString(field.getModifiers()) + " " +
                    super.toString() + " " +
                    field.getName());
        }
    }
    private class MethodNode extends ClassNode {
        Method method;
        public MethodNode(Method mtd) {
            super(mtd.getReturnType());
            method = mtd;
        }
        public String toString() {
            return("Method: " +
                    Modifier.toString(method.getModifiers()) + " " +
                    super.toString() + " " +
                    method.getName());
            
        }
        public ClassNode[] getParameters() {
            Class[] ptps = method.getParameterTypes();
            ClassNode[] result = new ClassNode[ptps.length];
            for(int i = 0; i < ptps.length; i++) {
                result[i] = new ClassNode(ptps[i]);
            }
            return(result);
        }
    }
    private class ComponentNode extends ClassNode {
        protected Hashtable props;
        protected String clss = "";
        protected String compToString = "";
        Component comp;
        ComponentImageProvider image;
        protected int x, y, w, h;
        protected ComponentNode() {
            props = new Hashtable();
        }
        public ComponentNode(Component comp) {
            super(comp.getClass());
            props = Operator.createOperator(comp).getDump();
            clss = comp.getClass().getName();
            compToString = comp.toString();
            this.comp = comp;
            x = comp.getLocationOnScreen().x;
            y = comp.getLocationOnScreen().y;
            w = comp.getWidth();
            h = comp.getHeight();
        }
        public String toString() {
            return(clss);
        }
        public Hashtable getProperties() {
            return(props);
        }
        public String getToString() {
            return(compToString);
        }
        public void setComponentImageProvider(ComponentImageProvider provider) {
            image = provider;
        }
        public BufferedImage getImage() {
            if(image != null) {
                return(image.getImage(x, y, w, h));
            } else {
                return(null);
            }
        }
    }
    private class ContainerNode extends ComponentNode {
        protected ComponentNode[] comps;
        protected ContainerNode() {
            super();
            comps = new ComponentNode[0];
        }
        public ContainerNode(Container comp) {
            super(comp);
            Component[] cmps = comp.getComponents();
            Vector wwns = new Vector();
            for(int i = 0; i < cmps.length; i++) {
                if(cmps[i] != null && (propDialog.showAll || cmps[i].isVisible())) {
                    if(cmps[i] instanceof Container) {
                        wwns.add(new ContainerNode((Container)cmps[i]));
                    } else {
                        wwns.add(new ComponentNode(cmps[i]));
                    }
                }
            }
            comps = new ComponentNode[wwns.size()];
            for(int i = 0; i < wwns.size(); i++) {
                comps[i] = (ComponentNode)wwns.get(i);
            }
        }
        public ComponentNode[] getComponents() {
            return(comps);
        }
        public int getComponentIndex(ComponentNode comp) {
            for(int i = 0; i < comps.length; i++) {
                if(comps[i].equals(comp)) {
                    return(i);
                }
            }
            return(-1);
        }
        public void setComponentImageProvider(ComponentImageProvider provider) {
            super.setComponentImageProvider(provider);
            for(int i = 0; i < comps.length; i++) {
                comps[i].setComponentImageProvider(provider);
            }
        }
        public ComponentModel getComponentModel() {
            return(new ComponentModel(this));
        }
    }
    private class WindowNode extends ContainerNode {
        protected WindowNode[] wins;
        String title;
        protected WindowNode() {
            super();
            wins = new WindowNode[0];
            title = "All Frames";
            clss = "";
        }
        public WindowNode(Window win) {
            super(win);
            Window[] wns = win.getOwnedWindows();
            Vector wwns = new Vector();
            for(int i = 0; i < wns.length; i++) {
                if(propDialog.showAll || wns[i].isVisible()) {
                    wwns.add(new WindowNode(wns[i]));
                }
            }
            wins = new WindowNode[wwns.size()];
            for(int i = 0; i < wwns.size(); i++) {
                wins[i] = (WindowNode)wwns.get(i);
            }
            title = win.toString();
            clss = win.getClass().getName();
            BufferedImage image = null;
            try {
                image = new Robot().
                        createScreenCapture(new Rectangle(win.getLocationOnScreen(),
                        win.getSize()));
            } catch(AWTException e) {
                e.printStackTrace();
            }
            setComponentImageProvider(new ComponentImageProvider(image, x, y));
        }
        public WindowNode[] getWindows() {
            return(wins);
        }
        public int getWindowIndex(WindowNode node) {
            for(int i = 0; i < wins.length; i++) {
                if(wins[i].equals(node)) {
                    return(i);
                }
            }
            return(-1);
        }
        public WindowModel getWindowModel() {
            return(new WindowModel(this));
        }
        public String toString() {
            return(clss + " \"" + title + "\"");
        }
    }
    
    private class RootNode extends WindowNode {
        public RootNode() {
            super();
            Window[] wns = Frame.getFrames();
            wins = new WindowNode[wns.length];
            int count = 0;
            for(int i = 0; i < wns.length; i++) {
                if(propDialog.showAll || wns[i].isVisible()) {
                    count++;
                }
            }
            wins = new WindowNode[count];
            count = 0;
            for(int i = 0; i < wns.length; i++) {
                if(propDialog.showAll || wns[i].isVisible()) {
                    wins[count] = new WindowNode(wns[i]);
                    count++;
                }
            }
        }
    }
    private class ClassModel implements TreeModel {
        ClassNode clsn;
        ClassModel(ClassNode clsn) {
            this.clsn = clsn;
        }
        public void addTreeModelListener(TreeModelListener l) {}
        public Object getChild(Object parent, int index) {
            if(parent == clsn) {
                return(clsn.getSuperClasses()[index]);
            } else if(parent instanceof SuperClassNode ||
                    parent instanceof InterfaceNode) {
                return(((ClassNode)parent).getSubNodes()[index]);
            } else if(parent instanceof MethodNode) {
                return(((MethodNode)parent).getParameters()[index]);
            }
            return(null);
        }
        public int getChildCount(Object parent) {
            if(parent == clsn) {
                return(clsn.getSuperClasses().length);
            } else if(parent instanceof SuperClassNode ||
                    parent instanceof InterfaceNode) {
                return(((ClassNode)parent).getSubNodes().length);
            } else if(parent instanceof MethodNode) {
                return(((MethodNode)parent).getParameters().length);
            }
            return(0);
        }
        public int getIndexOfChild(Object parent, Object child) {
            if(parent == clsn ||
                    parent instanceof MethodNode ||
                    parent instanceof SuperClassNode ||
                    parent instanceof InterfaceNode) {
                Object[] children;
                if(parent instanceof SuperClassNode ||
                        parent instanceof InterfaceNode) {
                    children = ((ClassNode)parent).getSuperClasses();
                } else if(parent instanceof MethodNode) {
                    children = ((MethodNode)parent).getParameters();
                } else {
                    children = clsn.getSuperClasses();
                }
                for(int i = 0; i < children.length; i++) {
                    if(children.equals(child)) {
                        return(i);
                    }
                }
            }
            return(0);
        }
        public Object getRoot() {
            return(clsn);
        }
        public boolean isLeaf(Object node) {
            return(getChildCount(node) == 0);
        }
        public void removeTreeModelListener(TreeModelListener l) {}
        public void valueForPathChanged(TreePath path, Object newValue) {}
    }
    private class WindowModel implements TreeModel {
        WindowNode win;
        WindowModel(WindowNode win) {
            this.win = win;
        }
        public void addTreeModelListener(TreeModelListener l) {}
        public Object getChild(Object parent, int index) {
            return(((WindowNode)parent).getWindows()[index]);
        }
        public int getChildCount(Object parent) {
            return(((WindowNode)parent).getWindows().length);
        }
        public int getIndexOfChild(Object parent, Object child) {
            return(((WindowNode)parent).getWindowIndex((WindowNode)child));
        }
        public Object getRoot() {
            return(win);
        }
        public boolean isLeaf(Object node) {
            return(((WindowNode)node).getWindows().length == 0);
        }
        public void removeTreeModelListener(TreeModelListener l) {}
        public void valueForPathChanged(TreePath path, Object newValue) {}
    }
    private class ComponentModel implements TreeModel {
        ContainerNode cont;
        ComponentModel(ContainerNode cont) {
            this.cont = cont;
        }
        public void addTreeModelListener(TreeModelListener l) {}
        public Object getChild(Object parent, int index) {
            if(parent instanceof ContainerNode) {
                return(((ContainerNode)parent).getComponents()[index]);
            } else {
                return("");
            }
        }
        public int getChildCount(Object parent) {
            if(parent instanceof ContainerNode) {
                return(((ContainerNode)parent).getComponents().length);
            } else {
                return(0);
            }
        }
        public int getIndexOfChild(Object parent, Object child) {
            if(parent instanceof ContainerNode) {
                return(((ContainerNode)parent).getComponentIndex((ComponentNode)child));
            } else {
                return(-1);
            }
        }
        public Object getRoot() {
            return(cont);
        }
        public boolean isLeaf(Object node) {
            if(node instanceof ContainerNode) {
                return(((ContainerNode)node).getComponents().length == 0);
            } else {
                return(true);
            }
        }
        public void removeTreeModelListener(TreeModelListener l) {}
        public void valueForPathChanged(TreePath path, Object newValue) {}
    }
    
    private class WindowRenderer implements TreeCellRenderer, ListCellRenderer {
        public WindowRenderer() {
        }
        public Component getTreeCellRendererComponent(JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
            return(get(value, selected));
        }
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            return(get(value, isSelected));
        }
        private Component get(Object value, boolean selected) {
            Component result = new JLabel(((ClassNode)value).toString());
            if(selected) {
                result.setBackground(Color.blue);
                result.setForeground(Color.white);
                JPanel resPane = new JPanel();
                resPane.setLayout(new BorderLayout());
                resPane.add(result, BorderLayout.CENTER);
                return(resPane);
            } else {
                return(result);
            }
        }
    }
    
    private class PropertyDialog extends JDialog {
        public boolean showAll = false;
        JComboBox visibleCombo;
        JCheckBox showToString;
        JCheckBox showReflection;
        JCheckBox showEvents;
        DefaultListModel viewTabs;
        JList orderList;
        JButton up;
        JButton down;
        Properties props;
        File propFile;
        public PropertyDialog(JFrame owner) {
            super(owner, "Properties", true);
            
            propFile = new File(System.getProperty("user.home") +
                    System.getProperty("file.separator") +
                    ".guibrowser");
            
            props = new Properties();
            
            String[] cpItems = {"Showing", "All"};
            visibleCombo = new JComboBox(cpItems);
            visibleCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showAll = (visibleCombo.getSelectedIndex() == 1);
                }
            });
            
            JPanel visiblePane = new JPanel();
            visiblePane.add(new JLabel("Show "));
            visiblePane.add(visibleCombo);
            
            showToString = new JCheckBox("Show toString() method result");
            showToString.setSelected(true);
            
            JPanel compTreePane = new JPanel();
            compTreePane.add(visiblePane);
            
            viewTabs = new DefaultListModel();
            viewTabs.addElement(WINDOWS_TAB);
            viewTabs.addElement(COMPONENTS_TAB);
            viewTabs.addElement(PROPERTIES_TAB);
            viewTabs.addElement(REFLECTION_TAB);
            viewTabs.addElement(IMAGE_TAB);
            
            orderList = new JList(viewTabs);
            orderList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    up.setEnabled(orderList.getSelectedIndex() > -1);
                    down.setEnabled(orderList.getSelectedIndex() > -1);
                }
            });
            
            showReflection = new JCheckBox("Show reflection tab");
            showReflection.setSelected(true);
            showReflection.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(showReflection.isSelected()) {
                        viewTabs.addElement(REFLECTION_TAB);
                    } else {
                        viewTabs.remove(viewTabs.indexOf(REFLECTION_TAB));
                    }
                    up.setEnabled(orderList.getSelectedIndex() > -1);
                    down.setEnabled(orderList.getSelectedIndex() > -1);
                }
            });
            
            showEvents = new JCheckBox("Show event tab");
            showEvents.setSelected(true);
            showEvents.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(showEvents.isSelected()) {
                        viewTabs.addElement(EVENT_TAB);
                    } else {
                        viewTabs.remove(viewTabs.indexOf(EVENT_TAB));
                    }
                    up.setEnabled(orderList.getSelectedIndex() > -1);
                    down.setEnabled(orderList.getSelectedIndex() > -1);
                }
            });
            
            up = new JButton("Move Up");
            up.setEnabled(false);
            up.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = orderList.getSelectedIndex();
                    if(index > 0) {
                        viewTabs.add(index - 1,
                                viewTabs.remove(index));
                        orderList.setSelectedIndex(index - 1);
                    }
                }
            });
            
            down = new JButton("Move Down");
            down.setEnabled(false);
            down.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = orderList.getSelectedIndex();
                    if(index < viewTabs.size() - 1) {
                        viewTabs.add(index + 1,
                                viewTabs.remove(index));
                        orderList.setSelectedIndex(index + 1);
                    }
                }
            });
            
            JPanel movePane = new JPanel();
            movePane.add(showEvents);
            movePane.add(showReflection);
            movePane.add(up);
            movePane.add(down);
            
            JPanel compViewPane = new JPanel();
            compViewPane.setLayout(new BorderLayout());
            compViewPane.add(new JLabel("Tab order:"), BorderLayout.NORTH);
            compViewPane.add(movePane, BorderLayout.SOUTH);
            compViewPane.add(orderList, BorderLayout.CENTER);
            
            JTabbedPane tbpn = new JTabbedPane();
            tbpn.add("Component Tree", compTreePane);
            tbpn.add("Component View", compViewPane);
            
            JButton okBUtton = new JButton("OK");
            okBUtton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    save();
                    setVisible(false);
                }
            });
            
            JButton cnBUtton = new JButton("Cancel");
            cnBUtton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    load();
                }
            });
            
            JPanel pn = new JPanel();
            pn.add(okBUtton);
            pn.add(cnBUtton);
            
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(pn, BorderLayout.SOUTH);
            getContentPane().add(tbpn, BorderLayout.CENTER);
            
            load();
            
            setSize(400, 400);
        }
        
        private void save() {
            try {
                props.setProperty("guibrowser.showall",
                        showAll ? "on" : "off");
                for(int i = 0; i < viewTabs.size(); i++) {
                    props.setProperty("guibrowser.viewpage_" + Integer.toString(i),
                            (String)viewTabs.elementAt(i));
                }
                props.store(new FileOutputStream(propFile),
                        "Jemmy GUIBrowser");
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        
        private void load() {
            if(propFile.exists()) {
                try {
                    props.load(new FileInputStream(propFile));
                    showAll =
                            props.getProperty("guibrowser.showall") == null ||
                            props.getProperty("guibrowser.showall").equals("") ||
                            props.getProperty("guibrowser.showall").equals("on");
                    visibleCombo.setSelectedIndex(showAll ? 1 : 0);
                    if( props.getProperty("guibrowser.viewpage_0") != null &&
                            !props.getProperty("guibrowser.viewpage_0").equals("")) {
                        viewTabs.removeAllElements();
                        viewTabs.addElement(props.getProperty("guibrowser.viewpage_0"));
                        viewTabs.addElement(props.getProperty("guibrowser.viewpage_1"));
                        viewTabs.addElement(props.getProperty("guibrowser.viewpage_2"));
                        if( props.getProperty("guibrowser.viewpage_3") != null &&
                                !props.getProperty("guibrowser.viewpage_3").equals("")) {
                            viewTabs.addElement(props.getProperty("guibrowser.viewpage_3"));
                        }
                        if( props.getProperty("guibrowser.viewpage_4") != null &&
                                !props.getProperty("guibrowser.viewpage_4").equals("")) {
                            viewTabs.addElement(props.getProperty("guibrowser.viewpage_4"));
                        }
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
                showReflection.setSelected(viewTabs.indexOf(REFLECTION_TAB) > -1);
                showEvents.setSelected(viewTabs.indexOf(EVENT_TAB) > -1);
            }
        }
    }
    
    private class ComponentBrowser extends JFrame {
        
        JTree winTree;
        JTree componentTree;
        JTree methodTree;
        ClassNode compNode;
        JTabbedPane tbd;
        JList fList;
        JButton viewButton;
        JButton expandButton;
        JSplitPane winSplit = null;
        JSplitPane componentSplit = null;
        WindowRenderer renderer;
        SelectionManager selManager;
        JList eventList;
        ListListener listListener;
        DefaultListModel eventModel;
        JCheckBox mouseEvents;
        JCheckBox mouseMotionEvents;
        JCheckBox keyEvents;
        
        public ComponentBrowser(JFrame owner, ClassNode componentNode) {
            super("Component " + componentNode.toString());
            fill(componentNode);
        }
        
        public ComponentBrowser(JDialog owner, ClassNode componentNode) {
            super("Component " + componentNode.toString());
            fill(componentNode);
        }
        
        private void fill(ClassNode componentNode) {
            compNode = componentNode;
            
            viewButton = new JButton("View");
            viewButton.setEnabled(false);
            viewButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new ComponentBrowser(getOwnr(),
                            getSelectedNode()).
                            show();
                }
            });
            
            expandButton = new JButton("Expand All");
            expandButton.setEnabled(false);
            expandButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    expandAll(getSelectedTree(),
                            getSelectionPath());
                }
            });
            
            JPanel buttonPane = new JPanel();
            buttonPane.add(viewButton);
            buttonPane.add(expandButton);
            
            Component[] cpss = {viewButton, expandButton};
            selManager = new SelectionManager(cpss);
            renderer = new WindowRenderer();
            
            tbd = new JTabbedPane();
            
            for(int i = 0; i < propDialog.viewTabs.size(); i++) {
                String next = (String)propDialog.viewTabs.elementAt(i);
                if       (next.equals(PROPERTIES_TAB)) {
                    addPropertiesTab();
                } else if(next.equals(WINDOWS_TAB)) {
                    addWindowTab();
                } else if(next.equals(COMPONENTS_TAB)) {
                    addComponentTab();
                } else if(next.equals(REFLECTION_TAB)) {
                    addReflectionTab();
                } else if(next.equals(EVENT_TAB)) {
                    addEventTab();
                } else if(next.equals(IMAGE_TAB)) {
                    addImageTab();
                }
            }
            
            tbd.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    viewButton.setEnabled(getSelectedNode() != null);
                }
            });
            
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            getContentPane().add(tbd, BorderLayout.CENTER);
            
            addComponentListener(new ComponentListener() {
                public void componentHidden(ComponentEvent e) {}
                public void componentMoved(ComponentEvent e) {}
                public void componentResized(ComponentEvent e) {
                    if(winSplit != null) {
                        winSplit.setDividerLocation(0.8);
                    }
                    if(componentSplit != null) {
                        componentSplit.setDividerLocation(0.8);
                    }
                }
                public void componentShown(ComponentEvent e) {}
            });
            
            setSize(800, 400);
        }
        
        private void addImageTab() {
            BufferedImage image = null;
            if(compNode instanceof ComponentNode) {
                image = ((ComponentNode)compNode).getImage();
            }
            if(image != null) {
                JPanel imagePane = new ImagePane(image);
                imagePane.prepareImage(image, imagePane);
                JScrollPane pane = new JScrollPane(imagePane);
                tbd.add(IMAGE_TAB, pane);
            }
        }
        
        private void addWindowTab() {
            if(compNode instanceof WindowNode &&
                    ((WindowNode)compNode).getWindows().length > 0) {
                winTree = new JTree(((WindowNode)compNode).getWindowModel());
                winTree.setCellRenderer(renderer);
                winTree.setEditable(false);
                winTree.addTreeSelectionListener(selManager);
                winSplit = createUnderPane(winTree);
                tbd.add(WINDOWS_TAB, winSplit);
            }
            
        }
        
        private void addComponentTab() {
            if(compNode instanceof ContainerNode &&
                    ((ContainerNode)compNode).getComponents().length > 0) {
                componentTree = new JTree(((ContainerNode)compNode).getComponentModel());
                componentTree.setCellRenderer(renderer);
                componentTree.setEditable(false);
                componentTree.addTreeSelectionListener(selManager);
                componentSplit = createUnderPane(componentTree);
                tbd.add(COMPONENTS_TAB, componentSplit);
            }
            
        }
        
        private void addReflectionTab() {
            methodTree = new JTree(compNode.getMethodsModel());
            methodTree.setCellRenderer(renderer);
            methodTree.setEditable(false);
            methodTree.addTreeSelectionListener(selManager);
            tbd.add(REFLECTION_TAB, new JScrollPane(methodTree));
        }
        
        private void addPropertiesTab() {
            if(compNode instanceof ComponentNode) {
                Hashtable props = ((ContainerNode)compNode).getProperties();
                if(props.size() > 0) {
                    JTable propTable = new JTable(new MyModel(props));
                    propTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                    tbd.add(PROPERTIES_TAB, new JScrollPane(propTable));
                    /*
                    propTable.addMouseListener(new MouseListener() {
                        public void mouseClicked(MouseEvent e) {
                            new ComponentBrowser(getOwnr(),
                                    getSelectedNode()).
                                    show();
                        }
                        public void mouseExited(MouseEvent e) {
                        }
                        public void mouseEntered(MouseEvent e) {
                        }
                        public void mousePressed(MouseEvent e) {
                        }
                        public void mouseReleased(MouseEvent e) {
                        }
                    });
                     */
                }
            }
        }
        
        private void addEventTab() {
            if(compNode instanceof ComponentNode) {
                eventModel = new DefaultListModel();
                eventList = new JList(eventModel);
                listListener = new ListListener(eventModel, ((ComponentNode)compNode).comp);
                mouseEvents = new JCheckBox("Mouse events");
                mouseEvents.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(mouseEvents.isSelected()) {
                            listListener.addMouseListener();
                        } else {
                            listListener.removeMouseListener();
                        }
                    }
                });
                mouseMotionEvents = new JCheckBox("Mouse motion events");
                mouseMotionEvents.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(mouseMotionEvents.isSelected()) {
                            listListener.addMouseMotionListener();
                        } else {
                            listListener.removeMouseMotionListener();
                        }
                    }
                });
                keyEvents = new JCheckBox("Key events");
                keyEvents.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(keyEvents.isSelected()) {
                            listListener.addKeyListener();
                        } else {
                            listListener.removeKeyListener();
                        }
                    }
                });
                JButton clear = new JButton("Clear list");
                clear.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        eventModel.removeAllElements();
                    }
                });
                JPanel checkPane = new JPanel();
                checkPane.add(mouseEvents);
                checkPane.add(mouseMotionEvents);
                checkPane.add(keyEvents);
                checkPane.add(clear);
                JPanel subPane = new JPanel();
                subPane.setLayout(new BorderLayout());
                subPane.add(checkPane, BorderLayout.SOUTH);
                subPane.add(new JScrollPane(eventList), BorderLayout.CENTER);
                tbd.add(EVENT_TAB, subPane);
            }
        }
        
        
        private JFrame getOwnr() {
            return(this);
        }
        
        private JTree getSelectedTree() {
            String title = tbd.getTitleAt(tbd.getSelectedIndex());
            if(title.equals(WINDOWS_TAB)) {
                return(winTree);
            } else if(title.equals(COMPONENTS_TAB)) {
                return(componentTree);
            } else if(title.equals(REFLECTION_TAB)) {
                return(methodTree);
            }
            return(null);
        }
        
        private TreePath getSelectionPath() {
            JTree tree = getSelectedTree();
            if(tree != null) {
                return(tree.getSelectionPath());
            } else {
                return(null);
            }
        }
        
        private ClassNode getSelectedNode() {
            TreePath path = getSelectionPath();
            if(path != null) {
                return((ClassNode)path.getLastPathComponent());
            } else  {
                return(null);
            }
        }
        
    }
    
    private class SelectionManager implements TreeSelectionListener, ListSelectionListener {
        Component[] comps;
        public SelectionManager(Component[] comps) {
            this.comps = comps;
        }
        public void valueChanged(TreeSelectionEvent e) {
            for(int i = 0; i < comps.length; i++) {
                comps[i].setEnabled(e.getPath() != null);
            }
        }
        public void valueChanged(ListSelectionEvent e) {
            for(int i = 0; i < comps.length; i++) {
                comps[i].setEnabled(e.getFirstIndex() != -1);
            }
        }
    }
    
    private void expandAll(JTree tree, TreePath path) {
        tree.expandPath(path);
        TreeModel model = tree.getModel();
        Object lastComponent = path.getLastPathComponent();
        for(int i = 0; i < model.getChildCount(lastComponent); i++) {
            expandAll(tree,
                    path.pathByAddingChild(model.getChild(lastComponent, i)));
        }
    }
    
    private class ToStringListener implements TreeSelectionListener{
        JTextArea area;
        public ToStringListener(JTextArea area) {
            this.area = area;
        }
        public void valueChanged(TreeSelectionEvent e) {
            if(e.getPath() != null &&
                    e.getPath().getLastPathComponent() instanceof ComponentNode) {
                area.setText("toString(): " +
                        ((ComponentNode)e.getPath().getLastPathComponent()).
                        getToString());
            } else {
                area.setText("");
            }
        }
    }
    
    private JSplitPane createUnderPane(JTree tree) {
        JTextArea toStringArea = new JTextArea();
        toStringArea.setLineWrap(true);
        toStringArea.setEditable(false);
        tree.addTreeSelectionListener(new ToStringListener(toStringArea));
        JSplitPane result = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tree),
                new JScrollPane(toStringArea));
        result.setOneTouchExpandable(true);
        result.setDividerSize(8);
        result.setDividerLocation(0.8);
        return(result);
    }
    
    private class MyModel extends DefaultTableModel {
        public MyModel(Hashtable props) {
            super();
            Object[] keys = props.keySet().toArray();
            if(keys.length > 0) {
                addColumn("Name");
                addColumn("Value");
                setNumRows(keys.length);
                for(int i = 0; i < keys.length; i++) {
                    setValueAt(keys[i].toString(), i, 0);
                    setValueAt(props.get(keys[i]).toString(), i, 1);
                }
                String[] titles = {"Name", "Value"};
                Collections.sort(getDataVector(), new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return(((Vector)o1).get(0).toString().compareTo(((Vector)o2).get(0).toString()));
                    }
                    public boolean equals(Object obj) {
                        return(false);
                    }
                });
            }
        }
        public boolean isCellEditable(int x, int y) {
            return(false);
        }
    }
    
    private class ListListener extends TrialListenerManager {
        DefaultListModel model;
        public ListListener(DefaultListModel m, Component comp) {
            super(comp);
            model = m;
        }
        void printEvent(AWTEvent e) {
            model.addElement(e);
        }
    }
    
    private class ImagePane extends JPanel {
        BufferedImage image;
        public ImagePane(BufferedImage image) {
            super();
            this.image = image;
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }
        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }
    }
    
    private class ComponentImageProvider {
        BufferedImage image;
        int x;
        int y;
        public ComponentImageProvider(BufferedImage image, int x, int y) {
            this.image = image;
            this.x = x;
            this.y = y;
        }
        public BufferedImage getImage(int x, int y, int w, int h) {
            /*
            BufferedImage newImage = image.getSubimage(0, 0, image.getWidth(), image.getHeight());
            Graphics g = newImage.getGraphics();
            g.setColor(Color.RED);
            g.drawRect(x - this.x, y - this.y, w, h);
            return(newImage);
             */
            return(image.getSubimage(x - this.x, y - this.y, w, h));
        }
    }
}
