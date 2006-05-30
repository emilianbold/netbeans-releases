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

package org.netbeans.bluej;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.ClassIndex;
import org.netbeans.modules.javacore.JMManager;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** Browses and allows to choose a project's main class.
 *
 * @author  Jiri Rechtacek
 * @author Milos Kleint copied from j2se project type to bluej one
 */
public class MainClassChooser extends JPanel {

    private ChangeListener changeListener;
    private String dialogSubtitle = null;
    private List/*<String>*/ possibleMainClasses;

    private String selectedClass;
            
    /** Creates new form MainClassChooser */
    public MainClassChooser (FileObject[] sourcesRoots) {
        this (sourcesRoots, null);
    }

    public MainClassChooser (FileObject[] sourcesRoots, String subtitle) {
        dialogSubtitle = subtitle;
        initComponents();
        initClassesView (sourcesRoots);
    }
    
    private void initClassesView (final FileObject[] sourcesRoots) {
        possibleMainClasses = null;
        jMainClassList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        jMainClassList.setListData (getWarmupList ());
        jMainClassList.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent evt) {
                if (changeListener != null) {
                    changeListener.stateChanged (new ChangeEvent (evt));
                }
            }
        });
        // support for double click to finish dialog with selected class
        jMainClassList.addMouseListener (new MouseListener () {
            public void mouseClicked (MouseEvent e) {
                if (MouseUtils.isDoubleClick (e)) {
                    if (getSelectedMainClass () != null) {
                        if (changeListener != null) {
                            changeListener.stateChanged (new ChangeEvent (e));
                        }
                    }
                }
            }
            public void mousePressed (MouseEvent e) {}
            public void mouseReleased (MouseEvent e) {}
            public void mouseEntered (MouseEvent e) {}
            public void mouseExited (MouseEvent e) {}
        });
        
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                possibleMainClasses = getMainClasses (sourcesRoots, true);
                if (possibleMainClasses.isEmpty ()) {                    
                    SwingUtilities.invokeLater( new Runnable () {
                        public void run () {
                            jMainClassList.setListData (new String[] { NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_NO_CLASSES_NODE") } ); // NOI18N
                        }
                    });                    
                } else {
                    final Object[] arr = possibleMainClasses.toArray ();
                    // #46861, sort name of classes
                    Arrays.sort (arr);
                    SwingUtilities.invokeLater(new Runnable () {
                        public void run () {
                            jMainClassList.setListData (arr);
                            if (selectedClass != null) {
                                jMainClassList.setSelectedValue(selectedClass, true);
                            } else {
                                jMainClassList.setSelectedIndex(0);
                            }
                        }
                    });                    
                }
            }
        });
        
        if (dialogSubtitle != null) {
            Mnemonics.setLocalizedText (jLabel1, dialogSubtitle);
        }
    }
    
    public static List/*String*/ getMainClasses (FileObject[] roots, boolean wait) {
        if (wait) {
            JMManager.getManager().waitScanFinished();
        }
        List result = new ArrayList ();
        for (int i=0; i<roots.length; i++) {
            getMainClasses(roots[i], result);
        }
        return result;
    }
    
    /** Returns list of FQN of classes contains the main method.
     * 
     * @param root the root of source to start find
     * @param addInto list of names of classes, e.g, [sample.project1.Hello, sample.project.app.MainApp]
     */
    private static void getMainClasses (FileObject root, List/*<String>*/ addInto) {
        JavaModel.getJavaRepository ().beginTrans (false);
        try {
            JavaModelPackage mofPackage = JavaModel.getJavaExtent(root);
            ClassIndex index = ClassIndex.getIndex (mofPackage);
            //Resource[] res = index.findResourcesForIdentifier ("main"); // NOI18N
            Collection col = index.findResourcesForIdent ("main"); // NOI18N
            Object[] arr = col.toArray ();

            if (arr == null) {
                // no main classes
                return;
            }

            for (int i = 0; i < arr.length; i++) {
                Resource res = (Resource)arr[i];
                Iterator mainIt=res.getMain().iterator();
                
                while (mainIt.hasNext()) {
                    JavaClass jcls=(JavaClass)mainIt.next();
                    
                    addInto.add(jcls.getName());
                }
            }
        } finally {
            JavaModel.getJavaRepository ().endTrans (false);
        }        
    }
    
    
    private Object[] getWarmupList () {        
        return JMManager.getManager().isScanInProgress() ?
            new Object[] {NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_SCANNING_MESSAGE")}:
            new Object[] {NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_WARMUP_MESSAGE")}; // NOI18N
    }
    
    private boolean isValidMainClassName (Object value) {
        return (possibleMainClasses != null) && (possibleMainClasses.contains (value));
    }


    /** Returns the selected main class.
     *
     * @return name of class or null if no class with the main method is selected
     */    
    public String getSelectedMainClass () {
        if (isValidMainClassName (jMainClassList.getSelectedValue ())) {
            return (String)jMainClassList.getSelectedValue ();
        } else {
            return null;
        }
    }
    
    public String getArguments() {
        return txtArguments.getText();
    }
    
    public void addChangeListener (ChangeListener l) {
        changeListener = l;
    }
    
    public void removeChangeListener (ChangeListener l) {
        changeListener = null;
    }
    
    // Used only from unit tests to suppress check of main method. If value
    // is different from null it will be returned instead.
    public static Boolean unitTestingSupport_hasMainMethodResult = null;
    
    /** Checks if given file object contains the main method.
     *
     * @param classFO file object represents java 
     * @return false if parameter is null or doesn't contain SourceCookie
     * or SourceCookie doesn't contain the main method
     */    
    public static boolean hasMainMethod (FileObject classFO) {
        return BluejActionProvider.hasMainMethod (classFO);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMainClassList = new javax.swing.JList();
        lblArguments = new javax.swing.JLabel();
        txtArguments = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(380, 300));
        getAccessibleContext().setAccessibleDescription(null);
        jLabel1.setLabelFor(jMainClassList);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 200));
        jScrollPane1.setViewportView(jMainClassList);
        jMainClassList.getAccessibleContext().setAccessibleDescription(null);

        lblArguments.setText(org.openide.util.NbBundle.getMessage(MainClassChooser.class, "LBL_Run_Arguments"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(lblArguments)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtArguments, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, Short.MAX_VALUE)
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblArguments)
                    .add(txtArguments, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(61, 61, 61))
        );
    }// </editor-fold>//GEN-END:initComponents

    
 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jMainClassList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblArguments;
    private javax.swing.JTextField txtArguments;
    // End of variables declaration//GEN-END:variables

    

    // Maybe useless renderer (fit if wanted to reneder Icons) // XXX
//    private static final class MainClassRenderer extends DefaultListCellRenderer {
//        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//            String displayName;
//            if (value instanceof String) {
//                displayName = (String) value;
//            } if (value instanceof FileObject) {
//                displayName = ((FileObject)value).getName ();
//            } else {
//                displayName = value.toString ();
//            }
//            return super.getListCellRendererComponent (list, displayName, index, isSelected, cellHasFocus);
//        }
//    }
//

    void setSelectedMainClass(String clazz) {
        this.selectedClass = clazz;
    }

    void setArguments(String args) {
        txtArguments.setText(args);
    }
}
