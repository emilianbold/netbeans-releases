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

package org.netbeans.modules.j2me.cdc.project;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** Browses and allows to choose a project's main class.
 *
 * @author  Jiri Rechtacek
 */
public class MainClassChooser extends JPanel {

    protected ChangeListener changeListener;
    private String dialogSubtitle = null;
    protected List<String> possibleMainClasses;
    private FileObject sourcesRoot;
    protected boolean onlyMain;
    protected String mainClass;
    protected Map<String,String> executionModes;
    protected String specialExecFqnXlet;
    protected String specialExecFqnApplet;        
    
    
    /** Creates new form MainClassChooser */
    public MainClassChooser (FileObject sourcesRoot, Map<String,String> executionModes) {
        this (sourcesRoot, null, false, executionModes);
    }

    public MainClassChooser (FileObject sourcesRoot, String subtitle, boolean onlyMain, Map<String,String> executionModes) {
        dialogSubtitle = subtitle;
        this.sourcesRoot = sourcesRoot;
        this.onlyMain = onlyMain;
        this.executionModes = executionModes;
        initComponents();
        initClassesView (sourcesRoot);
        if (onlyMain)
            onlymainLabel.setText(NbBundle.getMessage(MainClassChooser.class, "MSG_OnlyMainAllowed"));
        
        specialExecFqnXlet = (executionModes != null) ? executionModes.get(CDCPlatform.PROP_EXEC_XLET)  : null;
        specialExecFqnApplet = (executionModes != null) ? executionModes.get(CDCPlatform.PROP_EXEC_APPLET)  : null;        
        
    }
    
    private void initClassesView (final FileObject sourcesRoot) {
        possibleMainClasses = null;
        jMainClassList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        jMainClassList.setListData (getWarmupList ());
        jMainClassList.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent evt) {
                checkSelectionOptions();
                if (changeListener != null){
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
                possibleMainClasses = CDCProjectUtil.getMainClasses (new FileObject[] {sourcesRoot}, executionModes);
                if (possibleMainClasses.isEmpty ()) {                    
                    SwingUtilities.invokeLater( new Runnable () {
                        public void run () {
                            jMainClassList.setListData (new String[] { NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_NO_CLASSES_NODE") } ); // NOI18N
                        }
                    });                    
                } else {
                    final String[] arr = possibleMainClasses.toArray (new String[0]);
                    // #46861, sort name of classes
                    Arrays.sort (arr);
                    SwingUtilities.invokeLater(new Runnable () {
                        public void run () {
                            if (onlyMain){ //only execution using main is allowed (tests)
                                List<String> l = new ArrayList<String>(possibleMainClasses);
                                for (Iterator<String> it = l.iterator(); it.hasNext();) {
                                    String elem = it.next();
                                    if (!CDCProjectUtil.isMainClass(elem, sourcesRoot) || (executionModes != null && executionModes.containsKey(CDCPlatform.PROP_EXEC_MAIN))){
                                        it.remove();
                                    }   
                                }
                                final String[] arr = l.toArray (new String[0]);
                                if (arr.length == 0){
                                    jMainClassList.setListData (new String[] { NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_NO_CLASSES_NODE") } ); // NOI18N
                                    return;
                                }
                                Arrays.sort (arr);
                                jMainClassList.setListData (arr);
                                jMainClassList.setSelectedIndex (0);
                                return;
                            } 
                            boolean xlet = false;
                            if (!possibleMainClasses.isEmpty()){
                                for (String elem : possibleMainClasses) {
                                    if (CDCProjectUtil.isXletClass(elem, sourcesRoot, specialExecFqnXlet)){
                                        xlet = true;
                                        break;
                                    }
                                }
                                if ( xlet && mainClass != null && mainClass.indexOf(';') != -1 ){ //only multiple xlets execution is selected and there is at least one xlet
                                    String[] xlets = updateListView(true);
                                    StringTokenizer st = new StringTokenizer(mainClass, ";");
                                    List<Integer> indicies = new ArrayList<Integer>();
                                    while (st.hasMoreTokens()){
                                        String token = st.nextToken();
                                        for (int i = 0; i < xlets.length; i++){
                                            if (token.equals(xlets[i])){
                                                indicies.add(new Integer(i));
                                            }
                                        }
                                    }
                                    int[] sel = new int[indicies.size()];
                                    for (int i = 0; i < sel.length; i++){
                                        sel[i] = indicies.get(i).intValue();
                                    }
                                    multipleXlets.setSelected(sel.length > 1);
                                    jMainClassList.setListData(xlets);
                                    jMainClassList.setSelectedIndices(sel.length != 0 ? sel : new int[]{0});
                                } else {
                                    jMainClassList.setListData(updateListView(false));
                                    if (mainClass != null && isValidMainClassName(mainClass)) { //have valid seletion
                                        jMainClassList.setSelectedValue(mainClass, true); 
                                    }
                                    else {
                                        jMainClassList.setSelectedIndex(0); //otherwise
                                    }
                                }
                                checkSelectionOptions(); //update options                                
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
    
    protected void checkSelectionOptions() {
        if (!multipleXlets.isSelected()){
            String mainClass = getSelectedMainClass();
            boolean isMain = CDCProjectUtil.isMainClass(mainClass, sourcesRoot);
            boolean isXlet = CDCProjectUtil.isXletClass(mainClass, sourcesRoot, specialExecFqnXlet);
            boolean isApplet = CDCProjectUtil.isAppletClass(mainClass, sourcesRoot, specialExecFqnApplet);
            MainClassChooser.this.mainExecution.setEnabled(isMain && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_MAIN)));
            MainClassChooser.this.xletExecution.setEnabled(isXlet && !onlyMain && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_XLET)));
            MainClassChooser.this.appletExecution.setEnabled(isApplet && !onlyMain && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_APPLET)));
            MainClassChooser.this.multipleXlets.setEnabled(isXlet && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_XLET)));
            if (isApplet && MainClassChooser.this.appletExecution.isSelected()) //block the case, when applet exec is already selected
                return;
            if (isXlet && MainClassChooser.this.xletExecution.isSelected()) //block the case, when xlet exec is already selected
                return;
            if (isMain || onlyMain){
                MainClassChooser.this.mainExecution.setSelected(isMain);
            } else if (isXlet){
                MainClassChooser.this.xletExecution.setSelected(isXlet);
            } else if (isApplet){
                MainClassChooser.this.appletExecution.setSelected(isApplet);
            }
        } else {
            MainClassChooser.this.mainExecution.setEnabled(false);
            MainClassChooser.this.xletExecution.setEnabled(true);
            MainClassChooser.this.appletExecution.setEnabled(false);
            MainClassChooser.this.xletExecution.setSelected(true);
        }
    }
    
    private Object[] getWarmupList () {
        return new Object[] {NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_WARMUP_MESSAGE")}; // NOI18N
    }
    
    protected boolean isValidMainClassName (Object value) {
        return (possibleMainClasses != null) && (possibleMainClasses.contains (value));
    }


    /** Returns the selected main class.
     *
     * @return name of class or null if no class with the main method is selected
     */    
    public String getSelectedMainClass () {
        if (isValidMainClassName (jMainClassList.getSelectedValue ()) && !multipleXlets.isSelected()) {
            return (String)jMainClassList.getSelectedValue ();
        } else if (multipleXlets.isSelected()) {
            Object[] classes = jMainClassList.getSelectedValues();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < classes.length; i++) {
                if (isValidMainClassName (classes[i])){
                    sb.append(classes[i]);
                    sb.append(';');
                }                
            }
            
            return sb.length() != 0 ? sb.toString().substring(0, sb.length()-1) : null;            
        } else {    
            return null;
        }
    }
    
    public void setSelectedMainClass(String mainClass){
        this.mainClass = mainClass;
    }
    
    public boolean isXletExecution(){
        return xletExecution.isSelected();
    }
    
    public void setXletExecution(boolean selected){
        xletExecution.setSelected(selected);
    }

    boolean isAppletExecution() {
        return appletExecution.isSelected();
    }

    public void setAppletExecution(boolean selected){
        appletExecution.setSelected(selected);
    }

    public synchronized void addChangeListener (ChangeListener l) {
        changeListener = l;
    }
    
    public synchronized void removeChangeListener (ChangeListener l) {
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
        return CDCProjectUtil.hasMainMethod (classFO);
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMainClassList = new javax.swing.JList();
        mainExecution = new javax.swing.JRadioButton();
        xletExecution = new javax.swing.JRadioButton();
        appletExecution = new javax.swing.JRadioButton();
        multipleXlets = new javax.swing.JCheckBox();
        onlymainLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(380, 300));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("AD_MainClassChooser"));
        jLabel1.setLabelFor(jMainClassList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("CTL_AvaialableMainClasses"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 2, 12);
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 200));
        jScrollPane1.setViewportView(jMainClassList);
        jMainClassList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooser.class,"ACSN_jMainClassList"));
        jMainClassList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("ACSD_jMainClassList"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(jScrollPane1, gridBagConstraints);

        buttonGroup1.add(mainExecution);
        mainExecution.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mainExecution, org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("LBL_RunAsMain"));
        mainExecution.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainExecution.setEnabled(false);
        mainExecution.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(mainExecution, gridBagConstraints);
        mainExecution.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooser.class,"ACSN_CustomizerRun_Main"));
        mainExecution.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooser.class,"ACSD_CustomizerRun_Main"));

        buttonGroup1.add(xletExecution);
        org.openide.awt.Mnemonics.setLocalizedText(xletExecution, org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("LBL_RunAsXlet"));
        xletExecution.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        xletExecution.setEnabled(false);
        xletExecution.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(xletExecution, gridBagConstraints);
        xletExecution.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooser.class,"ACSN_CustomizerRun_Xlet"));
        xletExecution.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooser.class,"ACSD_CustomizerRun_Xlet"));

        buttonGroup1.add(appletExecution);
        org.openide.awt.Mnemonics.setLocalizedText(appletExecution, org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("LBL_RunAsApplet"));
        appletExecution.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        appletExecution.setEnabled(false);
        appletExecution.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(appletExecution, gridBagConstraints);
        appletExecution.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooser.class,"ACSN_CustomizerRun_Applet"));
        appletExecution.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooser.class,"ACSD_CustomizerRun_Applet"));

        org.openide.awt.Mnemonics.setLocalizedText(multipleXlets, org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("LBL_AllowMultipleXlets"));
        multipleXlets.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        multipleXlets.setEnabled(false);
        multipleXlets.setMargin(new java.awt.Insets(0, 0, 0, 0));
        multipleXlets.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                multipleXletsActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(multipleXlets, gridBagConstraints);
        multipleXlets.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooser.class,"ACSN_CustomizerRun_AllowMultiple"));
        multipleXlets.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooser.class,"ACSD_CustomizerRun_AllowMultiple"));

        onlymainLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(onlymainLabel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void multipleXletsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multipleXletsActionPerformed
        boolean xlets = multipleXlets.isSelected();
        String[] arr = updateListView(xlets);
        jMainClassList.setListData (arr);
        jMainClassList.setSelectedIndex (0);
        checkSelectionOptions();
    }//GEN-LAST:event_multipleXletsActionPerformed

    protected String[] updateListView(final boolean onlyXlets) {
        if (onlyXlets){
            jMainClassList.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            List<String> l = new ArrayList<String>(possibleMainClasses);
            for (Iterator<String> it = l.iterator(); it.hasNext();) {
                String elem = it.next();
                if (!CDCProjectUtil.isXletClass(elem, sourcesRoot, specialExecFqnXlet)){
                    it.remove();
                }
            }
 
            final String[] arr = l.toArray (new String[0]);
            // #46861, sort name of classes
            Arrays.sort (arr);
            return arr;
        }
        jMainClassList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        final String[] arr = possibleMainClasses.toArray (new String[0]);
        Arrays.sort (arr);
        return arr;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton appletExecution;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jMainClassList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton mainExecution;
    private javax.swing.JCheckBox multipleXlets;
    private javax.swing.JLabel onlymainLabel;
    private javax.swing.JRadioButton xletExecution;
    // End of variables declaration//GEN-END:variables

}
