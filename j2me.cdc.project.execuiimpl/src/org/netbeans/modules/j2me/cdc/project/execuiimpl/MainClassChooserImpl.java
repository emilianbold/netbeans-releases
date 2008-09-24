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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2me.cdc.project.execuiimpl;

import org.netbeans.modules.j2me.cdc.project.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.project.execui.MainClassChooser;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/** Browses and allows to choose a project's main class.
 *
 * @author  Jiri Rechtacek
 */
public class MainClassChooserImpl extends MainClassChooser {

    protected ChangeListener changeListener;
    private String dialogSubtitle = null;
    protected List<String> possibleMainClasses;
    private FileObject sourcesRoot;
    private String bcp;
    protected boolean onlyMain;
    protected String mainClass;
    protected Map<String,String> executionModes;
    protected String specialExecFqnXlet;
    protected String specialExecFqnApplet;        
    
    
    /** Creates new form MainClassChooser */
    public MainClassChooserImpl () {
        initComponents();
    }

    @Override
    public void inicialize(FileObject sourceRoot, Map<String, String> executionModes, String bootcp) {
        dialogSubtitle = null;
        this.sourcesRoot = sourceRoot;
        this.onlyMain = false;
        this.executionModes = executionModes;
        initClassesView (sourcesRoot);
        if (onlyMain)
            onlymainLabel.setText(NbBundle.getMessage(MainClassChooserImpl.class, "MSG_OnlyMainAllowed"));
        bcp=bootcp;
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
                possibleMainClasses = getMainClasses (new FileObject[] {sourcesRoot}, executionModes,bcp);
                if (possibleMainClasses.isEmpty ()) {                    
                    SwingUtilities.invokeLater( new Runnable () {
                        public void run () {
                            jMainClassList.setListData (new String[] { NbBundle.getMessage (MainClassChooserImpl.class, "LBL_ChooseMainClass_NO_CLASSES_NODE")  }  ); // NOI18N
                        }
                    });                    
                } else {
                    String[] arr = possibleMainClasses.toArray (new String[0]);
                    // #46861, sort name of classes
                    Arrays.sort (arr);
                    
                            if (onlyMain){ //only execution using main is allowed (tests)
                                final List<String> l = new ArrayList<String>(possibleMainClasses);
                                for (Iterator<String> it = l.iterator(); it.hasNext();) {
                                    String elem = it.next();
                                    if (!isMainClass(elem, sourcesRoot) || (executionModes != null && executionModes.containsKey(CDCPlatform.PROP_EXEC_MAIN))){
                                        it.remove();
                                    }   
                                }
                                SwingUtilities.invokeLater(new Runnable () {
                                public void run () {
                                    String[] arr = l.toArray (new String[0]);
                                    if (arr.length == 0){
                                        jMainClassList.setListData (new String[] { NbBundle.getMessage (MainClassChooserImpl.class, "LBL_ChooseMainClass_NO_CLASSES_NODE")  }  ); // NOI18N
                                        return;
                                    }
                                    Arrays.sort (arr);
                                    jMainClassList.setListData (arr);
                                    jMainClassList.setSelectedIndex (0);
                                }});
                                return;
                            } 
                            boolean xlet = false;
                            if (!possibleMainClasses.isEmpty()){
                                for (String elem : possibleMainClasses) {
                                    if (isXletClass(elem, sourcesRoot, specialExecFqnXlet)){
                                        xlet = true;
                                        break;
                                    }
                                }
                                if ( xlet && mainClass != null && mainClass.indexOf(';') != -1 ){ //only multiple xlets execution is selected and there is at least one xlet
                                    final String[] xlets = updateListView(true);
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
                                    final int[] sel = new int[indicies.size()];
                                    for (int i = 0; i < sel.length; i++){
                                        sel[i] = indicies.get(i).intValue();
                                    }
                                    SwingUtilities.invokeLater(new Runnable () {
                                        public void run () {
                                            multipleXlets.setSelected(sel.length > 1);
                                            jMainClassList.setListData(xlets);
                                            jMainClassList.setSelectedIndices(sel.length != 0 ? sel : new int[]{0});
                                    }});
                                } else {
                                    SwingUtilities.invokeLater(new Runnable () {
                                        public void run () {
                                            jMainClassList.setListData(updateListView(false));
                                            if (mainClass != null && isValidMainClassName(mainClass)) { //have valid seletion
                                                jMainClassList.setSelectedValue(mainClass, true); 
                                            }
                                            else {
                                                jMainClassList.setSelectedIndex(0); //otherwise
                                            }
                                     }});
                                }
                                checkSelectionOptions(); //update options                                
                            }
                }
            }
        });
        
        if (dialogSubtitle != null) {
            Mnemonics.setLocalizedText (jLabel1, dialogSubtitle);
        }
    }
    
    protected void checkSelectionOptions() {
        if (!multipleXlets.isSelected()){
            String tmpMainClass = getSelectedMainClass();
            if (tmpMainClass == null)
                tmpMainClass=this.mainClass;
            final String mainClass=tmpMainClass;
            final boolean isMain[]={false};
            final boolean isXlet[]={false};
            final boolean isApplet[]={false};
            Task task=RequestProcessor.getDefault().post(new Runnable()
            {
                public void run()
                {
                    isMain[0] = isMainClass(mainClass, sourcesRoot);
                    isXlet[0] = isXletClass(mainClass, sourcesRoot, specialExecFqnXlet);
                    isApplet[0] = isAppletClass(mainClass, sourcesRoot, specialExecFqnApplet);
                }
            });
            task.waitFinished();
            MainClassChooserImpl.this.mainExecution.setEnabled(isMain[0] && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_MAIN)));
            MainClassChooserImpl.this.xletExecution.setEnabled(isXlet[0] && !onlyMain && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_XLET)));
            MainClassChooserImpl.this.appletExecution.setEnabled(isApplet[0] && !onlyMain && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_APPLET)));
            MainClassChooserImpl.this.multipleXlets.setEnabled(isXlet[0] && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_XLET)));
            if (isApplet[0] && MainClassChooserImpl.this.appletExecution.isSelected()) //block the case, when applet exec is already selected
                return;
            if (isXlet[0] && MainClassChooserImpl.this.xletExecution.isSelected()) //block the case, when xlet exec is already selected
                return;
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    if (isMain[0] || onlyMain){
                        MainClassChooserImpl.this.mainExecution.setSelected(isMain[0]);
                    } else if (isXlet[0]){
                        MainClassChooserImpl.this.xletExecution.setSelected(isXlet[0]);
                    } else if (isApplet[0]){
                        MainClassChooserImpl.this.appletExecution.setSelected(isApplet[0]);
                    }
            }});
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    MainClassChooserImpl.this.mainExecution.setEnabled(false);
                    MainClassChooserImpl.this.xletExecution.setEnabled(true);
                    MainClassChooserImpl.this.appletExecution.setEnabled(false);
                    MainClassChooserImpl.this.xletExecution.setSelected(true);
            }});
        }
    }
    
    private Object[] getWarmupList () {
        return new Object[] {NbBundle.getMessage (MainClassChooserImpl.class, "LBL_ChooseMainClass_WARMUP_MESSAGE")}; // NOI18N
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

    public boolean isAppletExecution() {
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
        

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
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

        setPreferredSize(new java.awt.Dimension(380, 300));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(jMainClassList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(MainClassChooserImpl.class).getString("CTL_AvaialableMainClasses")); // NOI18N
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
        jMainClassList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooserImpl.class,"ACSN_jMainClassList")); // NOI18N
        jMainClassList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(MainClassChooserImpl.class).getString("ACSD_jMainClassList")); // NOI18N

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
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/cdc/project/execuiimpl/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(mainExecution, bundle.getString("LBL_RunAsMain")); // NOI18N
        mainExecution.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainExecution.setEnabled(false);
        mainExecution.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(mainExecution, gridBagConstraints);
        mainExecution.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooserImpl.class,"ACSN_CustomizerRun_Main")); // NOI18N
        mainExecution.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooserImpl.class,"ACSD_CustomizerRun_Main")); // NOI18N

        buttonGroup1.add(xletExecution);
        org.openide.awt.Mnemonics.setLocalizedText(xletExecution, bundle.getString("LBL_RunAsXlet")); // NOI18N
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
        xletExecution.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooserImpl.class,"ACSN_CustomizerRun_Xlet")); // NOI18N
        xletExecution.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooserImpl.class,"ACSD_CustomizerRun_Xlet")); // NOI18N

        buttonGroup1.add(appletExecution);
        org.openide.awt.Mnemonics.setLocalizedText(appletExecution, bundle.getString("LBL_RunAsApplet")); // NOI18N
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
        appletExecution.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooserImpl.class,"ACSN_CustomizerRun_Applet")); // NOI18N
        appletExecution.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooserImpl.class,"ACSD_CustomizerRun_Applet")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(multipleXlets, bundle.getString("LBL_AllowMultipleXlets")); // NOI18N
        multipleXlets.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        multipleXlets.setEnabled(false);
        multipleXlets.setMargin(new java.awt.Insets(0, 0, 0, 0));
        multipleXlets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multipleXletsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(multipleXlets, gridBagConstraints);
        multipleXlets.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainClassChooserImpl.class,"ACSN_CustomizerRun_AllowMultiple")); // NOI18N
        multipleXlets.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MainClassChooserImpl.class,"ACSD_CustomizerRun_AllowMultiple")); // NOI18N

        onlymainLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(onlymainLabel, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(MainClassChooserImpl.class).getString("AD_MainClassChooser")); // NOI18N
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
                if (!isXletClass(elem, sourcesRoot, specialExecFqnXlet)){
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


    /** Check if the given file object represents a source with the main method.
     * 
     * @param fo source
     * @return true if the source contains the main method
     */
    public static boolean hasMainMethod(FileObject fo) {
        // support for unit testing
        if (fo == null) {
            // ??? maybe better should be thrown IAE
            return false;
        }
        return !SourceUtils.getMainClasses(fo).isEmpty();
    }

    /** Returns list of FQN of classes contains the main method.
     * 
     * @param roots the classpath roots of source to start find
     * @return list of names of classes, e.g, [sample.project1.Hello, sample.project.app.MainApp]
     */
    public static List<String> getMainClasses (FileObject[] roots, Map<String,String> executionModes,String bootcp) {
        List<String> result = new ArrayList<String> ();
        for (FileObject fo : roots) {
            getMainClasses(fo, result, executionModes,bootcp);
        }
        return result;
    }
    
    /** Returns list of FQN of classes contains the main method.
     * 
     * @param root the root of source to start find
     * @param addInto list of names of classes, e.g, [sample.project1.Hello, sample.project.app.MainApp]
     */
    private static void getMainClasses (final FileObject root, final List<String> addInto, final Map<String,String> executionModes, final String bootcp) {       
        final String specialXletFqn = (executionModes != null) ? executionModes.get(CDCPlatform.PROP_EXEC_XLET)  : null;
        final String specialAppletFqn = (executionModes != null) ? executionModes.get(CDCPlatform.PROP_EXEC_APPLET)  : null;
        
        //We must get acuall (choosen in the customizer bootclasspath so we can't usee ClassPath.Boot
        ClassPath bcp=null;
        if (bootcp != null)
        {
            String[] items = PropertyUtils.tokenizePath(bootcp);
            if (items.length >0)
            {
                FileObject bcpRoots[]=new FileObject[items.length];
                int i=0;
                for (String item : items) {
                    FileObject fo=FileUtil.toFileObject(FileUtil.normalizeFile(new File(item)));
                    if (FileUtil.isArchiveFile(fo))
                        bcpRoots[i++]=FileUtil.getArchiveRoot(fo);
                    else
                        bcpRoots[i++]=fo;
                }

                bcp=ClassPathSupport.createClassPath(bcpRoots);
            }
        }
        else
            bcp=ClassPath.getClassPath (root, ClassPath.BOOT);  //Single compilation unit
        
        final ClassPath boot = bcp;
        final ClassPath rtm2 = ClassPath.getClassPath (root, ClassPath.EXECUTE);  //Single compilation unit'
        final ClassPath rtm1 = ClassPath.getClassPath (root, ClassPath.COMPILE);
        final ClassPath rtm  = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(new ClassPath[] { rtm1, rtm2 } );
        
        
        
        /* Here is the trick to include not build dependent projects */
        ArrayList<ClassPath> srcRoots=new ArrayList<ClassPath>();
        final ArrayList<FileObject> srcRootsFO=new ArrayList<FileObject>();
        ClassPath path=ClassPath.getClassPath (root, ClassPath.SOURCE);
        srcRoots.add(path);
        for (ClassPath.Entry entry : path.entries())
            srcRootsFO.add(entry.getRoot());
        Library libs[]=LibraryManager.getDefault().getLibraries();
        HashSet<URL> libSet=new HashSet<URL>();
        for (Library lib : libs)
        {
            try {
                List<URL> url=lib.getContent("src");
                libSet.addAll(url);
            } catch (IllegalArgumentException iae){} //bug in web ui libary? Does not provide emtoy set?
        }
        HashSet<URL> entrySet=new HashSet<URL>();
        for (Entry e: rtm2.entries())
        {
            Result res=null;
            try {
                res=SourceForBinaryQuery.findSourceRoots(e.getURL());
            } catch(Exception ex) {}
            FileObject[] roots=res.getRoots();
            for ( FileObject r : roots)
            {
                path=ClassPath.getClassPath(r,ClassPath.SOURCE);
                entrySet.clear();
                for (ClassPath.Entry entry : path.entries())
                {
                    entrySet.add(entry.getURL());
                }
                entrySet.removeAll(libSet);
                if (!srcRoots.contains(path) && entrySet.size()>0 )
                {
                    srcRoots.add(path);
                    for (ClassPath.Entry entry : path.entries())
                            srcRootsFO.add(entry.getRoot());
                }
            }
        }
        
        final ClassPath src = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(srcRoots.toArray(new ClassPath[srcRoots.size()]));                
         
        final ClasspathInfo cpInfo = ClasspathInfo.create(boot, rtm, src);
        
        JavaSource js = JavaSource.create(cpInfo);
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                HashSet<SearchKind> sk=new HashSet<SearchKind>();
                HashSet<SearchScope> ss=new HashSet<SearchScope>();
                CompilationController control; 
                
                Collection<ElementHandle<TypeElement>> addChildren(ClassIndex index,Collection<ElementHandle<TypeElement>> elems)
                {
                    Collection<ElementHandle<TypeElement>> elcl=new ArrayList<ElementHandle<TypeElement>>();
                    for (ElementHandle<TypeElement> elem : elems )
                    {
                        Collection<ElementHandle<TypeElement>> newEl=index.getElements(elem, sk, ss);
                        if (newEl.size()!=0)
                        {                            
                            elcl.addAll(addChildren(index,newEl));
                        }
                        elcl.add(elem);
                    }
                    return elcl;
                }
                
                public void run(CompilationController control) throws Exception {
                    control.toPhase(Phase.RESOLVED);
                    TypeElement xlet = control.getElements().getTypeElement(specialXletFqn != null ? specialXletFqn : "javax.microedition.xlet.Xlet");
                    TypeElement applet = control.getElements().getTypeElement(specialAppletFqn != null ? specialAppletFqn : "java.applet.Applet");

                    sk.add(SearchKind.IMPLEMENTORS);
                    ss.add(SearchScope.SOURCE);
                    ss.add(SearchScope.DEPENDENCIES);
                    Collection<ElementHandle<TypeElement>> arr = new ArrayList<ElementHandle<TypeElement>>();
                    if (executionModes == null || (executionModes != null && executionModes.containsKey(CDCPlatform.PROP_EXEC_MAIN))){
                        arr = SourceUtils.getMainClasses(srcRootsFO.toArray(new FileObject[srcRootsFO.size()])); // NOI18N
                    }
                    
                    Collection<ElementHandle<TypeElement>> exec=new ArrayList<ElementHandle<TypeElement>>();
                    Types types=control.getTypes();                    
                    if (xlet !=null && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_XLET)))
                        exec.add(ElementHandle.create(xlet));
                    
                    if (applet != null && (executionModes == null || executionModes.containsKey(CDCPlatform.PROP_EXEC_APPLET)))
                        exec.add(ElementHandle.create(applet));

                    if (arr == null && exec.size() == 0) {
                        // no main classes
                        return;
                    }
                    ClasspathInfo newInfo = ClasspathInfo.create(rtm1, rtm1, src);
                    ClassIndex index=newInfo.getClassIndex();
                    arr.addAll(addChildren(index,exec));
                    arr.removeAll(exec);
                    
                    for (ElementHandle<TypeElement> res : arr ){
                        TypeElement elem=res.resolve(control);
                        if (elem==null)
                        {
                            continue;
                        }
                        addInto.add(elem.getQualifiedName().toString());
                    }
                }
                
                
                public void cancel() {}
                
                },true);
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /** Returns if the given class name exists under the sources root and
     * it's a main class.
     * 
     * @param className FQN of class
     * @param root roots of sources
     * @return true if the class name exists and it's a main class
     */
    public static boolean isMainClass(String className, FileObject root) {
        ClassPath boot = ClassPath.getClassPath (root, ClassPath.BOOT);  //Single compilation unit
        ClassPath rtm2  = ClassPath.getClassPath (root, ClassPath.EXECUTE);  //Single compilation unit'
        ClassPath rtm1 = ClassPath.getClassPath (root, ClassPath.COMPILE);
        ClassPath rtm  = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(new ClassPath[] { rtm1, rtm2 } );
        ClassPath clp = ClassPath.getClassPath (root, ClassPath.SOURCE);        
        
        ClasspathInfo cpInfo = ClasspathInfo.create(boot,rtm,clp);
        return SourceUtils.isMainClass (className, cpInfo);        
    }
    
    private static boolean isSubclass(final String className, final String baseClassName, final FileObject root)
    {
        final Boolean[] result = new Boolean[]{false};
        if (className == null) {
            return result[0];
        }

        if (MainClassChooserImpl.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooserImpl.unitTestingSupport_hasMainMethodResult;
        }

        ClassPath boot = ClassPath.getClassPath(root, ClassPath.BOOT); //Single compilation unit
        ClassPath rtm2 = ClassPath.getClassPath(root, ClassPath.EXECUTE); //Single compilation unit'
        ClassPath rtm1 = ClassPath.getClassPath(root, ClassPath.COMPILE);
        ClassPath rtm = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(new ClassPath[]{rtm1, rtm2});
        ClassPath clp = ClassPath.getClassPath(root, ClassPath.SOURCE);

        ClasspathInfo cpInfo = ClasspathInfo.create(boot, rtm, clp);
        JavaSource js = JavaSource.create(cpInfo);
        try {

            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void run(CompilationController control) throws Exception {
                    TypeElement type = control.getElements().getTypeElement(baseClassName);
                    if (type == null) {
                        return;
                    }

                    TypeElement xtype = control.getElements().getTypeElement(className);
                    if (xtype == null) {
                        return;
                    }
                    Types types = control.getTypes();
                    result[0] = types.isSubtype(types.erasure(xtype.asType()), types.erasure(type.asType()));
                }

                public void cancel() {
                }
            }, true);
        } catch (IOException ioe) {
        }
        
        return result[0];
    }

    /** Returns if the given class name exists under the sources root and
     * it's a Xlet class.
     * 
     * @param className FQN of class
     * @param root roots of sources
     * @return true if the class name exists and it's a Xlet class
     */
    public static boolean isXletClass (final String className, FileObject root, final String specialXletFqn) {        
        return isSubclass(className, specialXletFqn != null ? specialXletFqn : "javax.microedition.xlet.Xlet",root);
    }
    
    /** Returns if the given class name exists under the sources root and
     * it's a Applet class.
     * 
     * @param className FQN of class
     * @param roots roots of sources
     * @return true if the class name exists and it's a Xlet class
     */
    public static boolean isAppletClass (final String className, final FileObject root, final String specialAppletFqn) {
        return isSubclass(className, specialAppletFqn != null ? specialAppletFqn : "java.applet.Applet",root);
    }
}
