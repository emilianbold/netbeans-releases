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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.src.SourceElement;
import org.openide.util.NbBundle;



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
        jMainClassList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        jMainClassList.setListData (getAllMainClasses (sourcesRoot));
        jMainClassList.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent evt) {
                if (changeListener != null) {
                    changeListener.stateChanged (new ChangeEvent (evt));
                }
            }
        });
    }
    
    // XXX temporary obtain the main classes in project's sources
    // should be used some query to java sources
    private Object[] getAllMainClasses (FileObject sourcesRoot) {
        Set result = new HashSet ();
        Enumeration en = sourcesRoot.getChildren (true);
        while (en.hasMoreElements ()) {
            FileObject fo = (FileObject)en.nextElement ();
            if (hasMainMethod (fo)) {
                try {
                    DataObject classDo = DataObject.find (fo);
                    result.add (getMainMethod (classDo.getCookie (SourceCookie.class), null));
                } catch (DataObjectNotFoundException ex) {
                    // alreary checked, must passes
                    assert false : fo;
                }
            }
        }
        return result.toArray ();
    }


    /** Returns the selected main class, other types of classes doesn't return.
     *
     * @return name of class or null if no class with the main method is selected
     */    
    public String getSelectedMainClass () {
        return (String)jMainClassList.getSelectedValue ();
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
    
    /** Checks if given file object contains the main method.
     *
     * @param classFO file object represents java 
     * @return false if parameter is null or doesn't contain SourceCookie
     * or SourceCookie doesn't contain the main method
     */    
    public static boolean hasMainMethod (FileObject classFO) {
        if (classFO == null) {
            return false;
        }
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
        jMainClassList = new javax.swing.JList();

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
        jScrollPane1.setViewportView(jMainClassList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 12);
        add(jScrollPane1, gridBagConstraints);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jMainClassList;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

//    // Maybe useless renderer (fit if wanted to reneder Icons)
//    private static final class MainClassRenderer extends DefaultListCellRenderer {
//        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//            String displayName;
//            if (value instanceof String) {
//                displayName = (String) value;
//            } else {
//                displayName = value.toString ();
//            }
//            return super.getListCellRendererComponent (list, displayName, index, isSelected, cellHasFocus);
//        }
//    }

}
