/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.cookies.SourceCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.view.TreeView;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.src.SourceElement;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;



/** Browse and allow to chooser a project's main class.
 *
 * @author  Jiri Rechtacek
 */
public class MainClassChooser extends JPanel {

    private static final Node NO_CLASSES_NODE = new AbstractNode (Children.LEAF);
    private ChangeListener changeListener;
            
    /** Creates new form MainClassChooser */
    public MainClassChooser (FileObject sourcesRoot) {
        initComponents();
        initClassesView (sourcesRoot);
        NO_CLASSES_NODE.setName (NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_NO_CLASSES_NODE")); // NOI18N
    }
    
    private void initClassesView (FileObject sourcesRoot) {
        ClassesViewPanel view = (ClassesViewPanel) classesView;
        TreeView treeView = (TreeView) classesTree;
        
        Children ch = null;
        if (sourcesRoot != null) {
            ch = PackageView.createPackageView (sourcesRoot);
        }

        if (ch == null || ch.getNodes (true).length == 0) {
            ch = new Children.Array ();
            ch.add (new Node[] { NO_CLASSES_NODE.cloneNode () });
        }

        Node root = new AbstractNode (ch);
        treeView.setRootVisible (false);
        treeView.setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);

        view.getExplorerManager ().setRootContext (root);
        view.getExplorerManager ().addPropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange(PropertyChangeEvent evt) {
                if (changeListener != null) {
                    changeListener.stateChanged (new ChangeEvent (evt));
                }
            }
        });
        treeView.expandNode (root);
    }


    /** Returns the selected main class, other types of classes doesn't return.
     *
     * @return name of class or null if no class with the main method is selected
     */    
    public String getSelectedMainClass () {
        
        Node[] nodes = ((ClassesViewPanel)classesView).getExplorerManager ().getSelectedNodes ();

        // check if this java object has main class
        if (nodes.length == 1) {
            // check if it's main class
            return getMainMethod (nodes[0].getCookie (SourceCookie.class), nodes[0].getName ());
        }
        
        return null;
    }
    
    public void addChangeListener (ChangeListener l) {
        changeListener = l;
    }
    
    private static String getMainMethod (Object obj, String expectedName) {
        if (obj == null || !(obj instanceof SourceCookie)) {
            return null;
        }
        SourceCookie cookie = (SourceCookie) obj;
        // check the main class
        String fullName = null;
        SourceElement source = cookie.getSource ();
        ClassElement[] classes = source.getClasses();
        boolean hasMain = false;
        for (int i = 0; i < classes.length; i++) {
          if (expectedName == null || classes[i].getName().getName().equals (expectedName)) {
            if (classes[i].hasMainMethod()) {
                hasMain = true;
                fullName = classes[i].getName ().getFullName ();
                break;
            }
          }
        }
        if (hasMain) {
            return fullName;
        }
        return null;
    }
    
    // temporary method
    public static boolean hasMainMethod (FileObject classFO) {
        try {
            DataObject classDO = DataObject.find (classFO);
            return getMainMethod (classDO.getCookie (SourceCookie.class), null) != null;
        } catch (DataObjectNotFoundException ex) {
            // can ignore it, classFO could be wrongly set
            return false;
        }
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        classesView = new ClassesViewPanel ();
        classesTree = new BeanTreeView ();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("CTL_AvaialableMainClasses"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 200));
        classesView.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        classesView.add(classesTree, gridBagConstraints);

        jScrollPane1.setViewportView(classesView);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane classesTree;
    private javax.swing.JPanel classesView;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables


    private final class ClassesViewPanel extends JPanel implements Provider/*, Lookup.Provider*/ {
        private ExplorerManager manager = new ExplorerManager ();
        private TopComponent comp = new TopComponent ();
        
        public ExplorerManager getExplorerManager () {
            return manager;
        }
        
//        public Lookup getLookup () {
//            return comp.getLookup ();
//        }
//        
    }

}
