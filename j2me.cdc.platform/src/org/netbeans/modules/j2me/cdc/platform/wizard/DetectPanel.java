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

package org.netbeans.modules.j2me.cdc.platform.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformConfigurator;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * This Panel launches autoconfiguration during the New J2SE Platform sequence.
 * The UI views properties of the platform, reacts to the end of detection by
 * updating itself. It triggers the detection task when the button is pressed.
 * The inner class WizardPanel acts as a controller, reacts to the UI completness
 * (jdk name filled in) and autoconfig result (passed successfully) - and manages
 * Next/Finish button (valid state) according to those.
 *
 * @author Svata Dedic
 */
public class DetectPanel extends javax.swing.JPanel {

    private ArrayList<ChangeListener> listeners;
    
    /**
     * Creates a detect panel
     * start the task and update on its completion
     * @param p the platform being customized.
     */
    public DetectPanel(FileObject installed, CDCPlatformDetector detector ) {
        initComponents();
        sourcesList.setModel(new DefaultListModel());
        javadocList.setModel(new DefaultListModel());        
        postInitComponents ();
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, //NOI18N
            new String[] {
                NbBundle.getMessage(DetectPanel.class,"TITLE_PlatformName"), //NOI18N
        });
        this.setName (NbBundle.getMessage(DetectPanel.class,"TITLE_PlatformName")); //NOI18N

        CDCPlatformConfigurator configurator = detector.getConfigurator(installed);
        if (configurator != null ){
            JPanel panel = configurator.getConfigurationTools();
            panel.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    fireChange();
                }
            });
            jTabbedPane1.insertTab(NbBundle.getMessage(DetectPanel.class, "TXT_Configuration"), null, panel, null, 0);
        }
    }

    public void addNotify() {
        super.addNotify();        
    }    

    private void postInitComponents () {
        this.jdkName.getDocument().addDocumentListener (new DocumentListener () {

            public void insertUpdate(DocumentEvent e) {
                handleNameChange ();
            }

            public void removeUpdate(DocumentEvent e) {
                handleNameChange ();
            }

            public void changedUpdate(DocumentEvent e) {
                handleNameChange ();
            }
        });

        // Fix for IZ#163462 - Too many problems in the "Add Java Platform"/"Java ME CDC..." wizard
        sourcesList.getSelectionModel().addListSelectionListener( 
                new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e) {
                removeSourceButton.setEnabled(sourcesList.getSelectedValue()!= null);
            }
        });

        javadocList.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e) {
                removeJavadocButton.setEnabled(javadocList.getSelectedValue()!= null);
            }
        });
    }

    protected void handleNameChange () {
        this.fireChange();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        jdkName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourcesList = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        addSourceButton = new javax.swing.JButton();
        removeSourceButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        javadocList = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        addJavadocButton = new javax.swing.JButton();
        removeJavadocButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("AD_DetectPanel"));
        jLabel3.setLabelFor(jdkName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getBundle(DetectPanel.class).getString("LBL_DetailsPanel_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jdkName, gridBagConstraints);
        jdkName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(DetectPanel.class).getString("AD_PlatformName"));

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.GridBagLayout());

        sourcesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(sourcesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        jPanel4.add(jScrollPane1, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridLayout(2, 0, 6, 6));

        org.openide.awt.Mnemonics.setLocalizedText(addSourceButton, org.openide.util.NbBundle.getMessage(DetectPanel.class, "TXT_AddSources"));
        addSourceButton.setEnabled(false);
        addSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSourceButtonActionPerformed(evt);
            }
        });

        jPanel6.add(addSourceButton);

        org.openide.awt.Mnemonics.setLocalizedText(removeSourceButton, org.openide.util.NbBundle.getMessage(DetectPanel.class, "TXT_RemoveSources"));
        removeSourceButton.setEnabled(false);
        removeSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSourceButtonActionPerformed(evt);
            }
        });

        jPanel6.add(removeSourceButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel4.add(jPanel6, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DetectPanel.class, "TXT_Sources"), jPanel4);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        javadocList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        javadocList.setEnabled(false);
        jScrollPane2.setViewportView(javadocList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        jPanel5.add(jScrollPane2, gridBagConstraints);

        jPanel7.setLayout(new java.awt.GridLayout(2, 0, 0, 6));

        org.openide.awt.Mnemonics.setLocalizedText(addJavadocButton, org.openide.util.NbBundle.getMessage(DetectPanel.class, "TXT_AddJavadoc"));
        addJavadocButton.setEnabled(false);
        addJavadocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJavadocButtonActionPerformed(evt);
            }
        });

        jPanel7.add(addJavadocButton);

        org.openide.awt.Mnemonics.setLocalizedText(removeJavadocButton, org.openide.util.NbBundle.getMessage(DetectPanel.class, "TXT_RemoveJavadoc"));
        removeJavadocButton.setEnabled(false);
        removeJavadocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeJavadocButtonActionPerformed(evt);
            }
        });

        jPanel7.add(removeJavadocButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel5.add(jPanel7, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DetectPanel.class, "TXT_JavaDoc"), jPanel5);

        jPanel1.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(jPanel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void removeJavadocButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeJavadocButtonActionPerformed
        int index = javadocList.getSelectedIndex();    
        ((DefaultListModel)javadocList.getModel()).remove(index);
        int size = javadocList.getModel().getSize();
        if (size != 0){
            javadocList.setSelectedIndex(0);
        }
        //removeJavadocButton.setEnabled(size != 0);
    }//GEN-LAST:event_removeJavadocButtonActionPerformed

    private void addJavadocButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJavadocButtonActionPerformed
        addResource("javadoc"); //NOI18N
    }//GEN-LAST:event_addJavadocButtonActionPerformed

    private void addSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourceButtonActionPerformed
        addResource("src"); //NOI18N
    }//GEN-LAST:event_addSourceButtonActionPerformed

    private void removeSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSourceButtonActionPerformed
        int index = sourcesList.getSelectedIndex();    
        ((DefaultListModel)sourcesList.getModel()).remove(index);
        int size = sourcesList.getModel().getSize();
        if (size != 0){
            sourcesList.setSelectedIndex(0);
        }
        //removeSourceButton.setEnabled(size != 0);

    }//GEN-LAST:event_removeSourceButtonActionPerformed

    public final synchronized void addChangeListener (ChangeListener listener) {
        if (this.listeners == null)
            this.listeners = new ArrayList<ChangeListener> ();
        this.listeners.add (listener);
    }

    public final synchronized void removeChangeListener (ChangeListener listener) {
        if (this.listeners == null)
            return;
        this.listeners.remove (listener);
    }

    public String getPlatformName() {
	    return jdkName.getText().trim();
    }
    
    String getSources () {
        StringBuffer sb = new StringBuffer();
        int size = sourcesList.getModel().getSize();
        for (int i = 0; i < size; i++) {
            sb.append(sourcesList.getModel().getElementAt(i));
            if ( i+1 < size ){
                sb.append(';');
            }
        }
        String val = sb.toString();
        return val.length() == 0 ? null : val;
    }

    void setSources (String sources) {
        ((DefaultListModel)sourcesList.getModel()).removeAllElements();
        if (sources == null){
            return;
        }
        StringTokenizer st = new StringTokenizer(sources, ";");
        while(st.hasMoreTokens()){
            ((DefaultListModel)sourcesList.getModel()).addElement(st.nextToken());
        }
        //removeSourceButton.setEnabled(sourcesList.getModel().getSize() != 0);
    }

    void setSources (ClassPath sources) {
        String srcPath = null;
        if (sources.entries().size()>0) {
            URL folderRoot = ((ClassPath.Entry)sources.entries().get(0)).getURL();
            if ("jar".equals(folderRoot.getProtocol())) {   //NOI18N
                folderRoot = FileUtil.getArchiveFile (folderRoot);
            }
            srcPath = new File(URI.create(folderRoot.toExternalForm())).getAbsolutePath();
        }

        setSources (srcPath);
    }

    String getJavadoc () {
        StringBuffer sb = new StringBuffer();
        int size = javadocList.getModel().getSize();
        for (int i = 0; i < size; i++) {
            sb.append(javadocList.getModel().getElementAt(i));
            if ( i+1 < size ){
                sb.append(';');
            }
        }
        String val = sb.toString();
        return val.length() == 0 ? null : val;
    }

    void setJavadoc (String jdoc) {
        ((DefaultListModel)javadocList.getModel()).removeAllElements();
        if (jdoc == null){
            return;
        }
        StringTokenizer st = new StringTokenizer(jdoc, ";");
        while(st.hasMoreTokens()){
            ((DefaultListModel)javadocList.getModel()).addElement(st.nextToken());
        }
        //removeJavadocButton.setEnabled(javadocList.getModel().getSize() != 0);
    }

    void setJavadoc (List jdocFolders) {
        ((DefaultListModel)javadocList.getModel()).removeAllElements();
        if (jdocFolders == null){
            return;
        }
        Iterator it = jdocFolders.iterator();
        while(it.hasNext()){
            ((DefaultListModel)javadocList.getModel()).addElement(FileUtil.toFile((FileObject)it.next()).getAbsolutePath());
        }
        //removeJavadocButton.setEnabled(javadocList.getModel().getSize() != 0);
    }

    protected final void fireChange () {
        Iterator it = null;
        synchronized (this) {
            if (this.listeners == null)
                return;
            it = ((ArrayList)this.listeners.clone()).iterator();
        }
        ChangeEvent event = new ChangeEvent (this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(event);
        }
    }

    /**
     * Updates static information from the detected platform's properties
     */
    void updateData(String platfromDisplayName) {
        // if the name is empty, fill something in:
        if ("".equals(jdkName.getText())) {
            jdkName.setText(platfromDisplayName);
            this.jdkName.selectAll();
        }
    }    
    
    private void addResource(String volumeType) {
        // TODO add your handling code here:
        DefaultListModel model = null;
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setAcceptAllFileFilterUsed(false);
        if (volumeType.equalsIgnoreCase("javadoc")) {     //NOI18N
            chooser.setDialogTitle(NbBundle.getMessage(DetectPanel.class,"TXT_OpenJavadoc"));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter(new SimpleFileFilter(NbBundle.getMessage(
                    DetectPanel.class,"TXT_SelectJavadoc"),new String[] {"ZIP","JAR"}));     //NOI18N
            chooser.setApproveButtonText(NbBundle.getMessage(DetectPanel.class,"CTL_SelectJD"));
            chooser.setApproveButtonMnemonic(NbBundle.getMessage(DetectPanel.class,"MNE_SelectJD").charAt(0));
            model = (DefaultListModel) javadocList.getModel();
        } else if (volumeType.equalsIgnoreCase("src")) {         //NOI18N
            chooser.setDialogTitle(NbBundle.getMessage(DetectPanel.class,"TXT_OpenSources"));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter(new SimpleFileFilter(NbBundle.getMessage(
                    DetectPanel.class,"TXT_SelectSources"),new String[] {"ZIP","JAR"}));     //NOI18N
            chooser.setApproveButtonText(NbBundle.getMessage(DetectPanel.class,"CTL_SelectSRC"));
            chooser.setApproveButtonMnemonic(NbBundle.getMessage(DetectPanel.class,"MNE_SelectSRC").charAt(0));
            model = (DefaultListModel) sourcesList.getModel();
        }
        if (lastFolder != null) {
            chooser.setCurrentDirectory(lastFolder);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            lastFolder = chooser.getCurrentDirectory();
            if (chooser.isMultiSelectionEnabled()) {
                addFiles(chooser.getSelectedFiles(), model);
            } else {
                addFiles(new File[] {chooser.getSelectedFile()}, model);
            }
        }
    }

    private void addFiles (File[] files, DefaultListModel model) {
        for (File f : files ) {
            //XXX: JFileChooser workaround (JDK bug #5075580), double click on folder returns wrong file
            // E.g. for /foo/src it returns /foo/src/src
            // Try to convert it back by removing last invalid name component
            if (!f.exists()) {
                File parent = f.getParentFile();
                if (parent != null && f.getName().equals(parent.getName()) && parent.exists()) {
                    f = parent;
                }
            }
            model.addElement(f.toString());
        }
    }

    private static class SimpleFileFilter extends FileFilter {
        
        private String description;
        private Collection extensions;
        
        
        public SimpleFileFilter(String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }
        
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index==name.length()-1)
                return false;
            String extension = name.substring(index+1).toUpperCase();
            return this.extensions.contains(extension);
        }
        
        public String getDescription() {
            return this.description;
        }
    }
    
    private static File lastFolder = null;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addJavadocButton;
    private javax.swing.JButton addSourceButton;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList javadocList;
    private javax.swing.JTextField jdkName;
    private javax.swing.JButton removeJavadocButton;
    private javax.swing.JButton removeSourceButton;
    private javax.swing.JList sourcesList;
    // End of variables declaration//GEN-END:variables

    /**
     * Controller for the outer class: manages wizard panel's valid state
     * according to the user's input and detection state.
     */
    static class WizardPanel implements WizardDescriptor.Panel, TaskListener, ChangeListener {
        protected DetectPanel         component;
        private RequestProcessor.Task task;
        private final CDCWizardIterator  iterator;
        private Collection<ChangeListener> changeList = new ArrayList<ChangeListener>();
        protected boolean             detected;
        private boolean             valid;
        private boolean             configured;        
        private WizardDescriptor    wiz;
        protected CDCPlatform platform;
        protected FileObject installedFolder;
        protected CDCPlatformDetector detector;
        
        /**
         * @param type @see CDCPlatformImpl.java
         */        
        WizardPanel(CDCWizardIterator iterator, CDCPlatformDetector detector) {
	    this.iterator = iterator;
            this.detector = detector;
        }

        public void addChangeListener(ChangeListener l) {
            changeList.add(l);
        }

        public java.awt.Component getComponent() {
            if (component == null) {
                installedFolder = iterator.getInstallFolder();
                component = new DetectPanel(installedFolder, detector);
                component.addChangeListener (this);
                configured = isConfigured(); //NOI18N
                if (configured){
                    task = RequestProcessor.getDefault().create(detectPlatformTask);
                    task.addTaskListener(this);
                } else {
                    setValid(false);
                }
            }
            return component;
        }

        void setValid(boolean v) {
            if (v == valid)
                return;
            valid = v;
            fireStateChange();
        }

        public HelpCtx getHelp() {
            return new HelpCtx ("cdc.detectPanel"); //NOI18N
        }

        public boolean isValid() {
            return valid;
        }

        public void readSettings(Object settings) {
            this.wiz = (WizardDescriptor) settings;
            JavaPlatform platform = this.iterator.getPlatform();
            String srcPath = null;
            String jdocPath = null;
            if (platform != null){
                ClassPath src = platform.getSourceFolders();
                if (src.entries().size()>0) {
                    URL folderRoot = ((ClassPath.Entry)src.entries().get(0)).getURL();
                    if ("jar".equals(folderRoot.getProtocol())) {   //NOI18N
                        folderRoot = FileUtil.getArchiveFile (folderRoot);
                    }
                    srcPath = new File(URI.create(folderRoot.toExternalForm())).getAbsolutePath();
                }
                List<URL> jdoc = platform.getJavadocFolders();
                if (jdoc.size()>0) {
                    URL folderRoot = jdoc.get(0);
                    if ("jar".equals(folderRoot.getProtocol())) {
                        folderRoot = FileUtil.getArchiveFile (folderRoot);
                    }
                    jdocPath = new File (URI.create(folderRoot.toExternalForm())).getAbsolutePath();
                }
                this.component.setSources (srcPath);
                this.component.setJavadoc (jdocPath);
            }
            this.component.jdkName.setEditable(false);
            if (platform == null && task != null){
                task.schedule(0);
            } else {
                checkValid();
                fireStateChange();
            }
        }

        void fireStateChange() {
            ChangeListener[] ll;
            synchronized (this) {
                if (changeList.isEmpty())
                    return;
                ll = changeList.toArray(new ChangeListener[0]);
            }
            ChangeEvent ev = new ChangeEvent(this);
            for (ChangeListener l : ll)
                l.stateChanged(ev);
        }

        public void removeChangeListener(ChangeListener l) {
            changeList.remove(l);
        }

	/**
	 Updates the Platform's display name with the one the user
	 has entered. Stores user-customized display name into the Platform.
	 */
        public void storeSettings(Object settings) {
            if (isValid()) {                
                CDCPlatform p = platform;
                String name = component.getPlatformName();
                platform.setDisplayName (name);
                String antName = createAntName (name);
                p.setAntName (antName);
                List<PathResourceImplementation> src = new ArrayList<PathResourceImplementation>();
                List<URL> jdoc = new ArrayList<URL> ();
                String srcPath = this.component.getSources();
                if (srcPath!=null) {
                    File f = new File (srcPath);
                    try {
                        URL url = f.toURI().toURL();
                        if (FileUtil.isArchiveFile(url)) {
                            url = FileUtil.getArchiveRoot(url);
                            FileObject fo = URLMapper.findFileObject(url);
                            if (fo != null) {
                                fo = fo.getFileObject("src");   //NOI18N
                                if (fo != null) {
                                    url = fo.getURL();
                                }
                            }
                            src.add (ClassPathSupport.createResource(url));
                        }
                        else {
                            src.add (ClassPathSupport.createResource(url));
                        }
                    } catch (MalformedURLException mue) {
                        ErrorManager.getDefault().notify (mue);
                    }
                    catch (FileStateInvalidException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
                String jdocPath = this.component.getJavadoc();
                if (jdocPath!=null) {
                    StringTokenizer st = new StringTokenizer(jdocPath, ";");
                    while ( st.hasMoreTokens() ){
                        File f = new File (st.nextToken());
                        try {
                            URL url = f.toURI().toURL();
                            if (FileUtil.isArchiveFile(url)) {
                                jdoc.add (FileUtil.getArchiveRoot(url));
                            }
                            else {
                                jdoc.add (url);
                            }
                        } catch (MalformedURLException mue) {
                            ErrorManager.getDefault().notify (mue);
                        }
                    }
                }
                p.setSourceFolders (ClassPathSupport.createClassPath(src));
                p.setJavadocFolders (jdoc);
                this.iterator.setPlatform(p);
            }
        }

        /**
         * Revalidates the Wizard Panel
         */
        public void taskFinished(Task task) {
            SwingUtilities.invokeLater( new Runnable () {
                public void run () {
                    detected = detectPlatformTask.isValid();
                    if (detected)
                    {
                        component.setJavadoc (platform.getJavadocFolders());
                        component.setSources(platform.getSourceFolders());
                        component.updateData (platform.getDisplayName());
                        component.jdkName.setEditable(true);

                        component.addSourceButton.setEnabled(true);
                        component.addJavadocButton.setEnabled(true);                    
                        component.sourcesList.setEnabled(true);
                        component.javadocList.setEnabled(true);                        
                    }
                    checkValid ();
                }
            });            
        }


        public void stateChanged(ChangeEvent e) {
             this.checkValid();
        }

        protected void checkValid () {
            this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, "");   //NO18N                                                               //NOI18N
            
            boolean v = true;
            boolean usedDisplayName = false;
            String name = this.component.getPlatformName ();            
            boolean b = isConfigured();
            if (b && !configured){
                task = RequestProcessor.getDefault().create(detectPlatformTask);
                task.addTaskListener(this);
                task.schedule(0);
                configured = b;
            }
            if (!configured){
                CDCPlatformConfigurator configurator = detector.getConfigurator(installedFolder);
                if ( configurator != null ){
                this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, configurator.getInfo()); //NOI18N
                } else { 
                    this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(DetectPanel.class,"ERROR_PlatformNotSet"));         //NOI18N
                }
                v = false;
            } else {
                boolean validDisplayName = name.length() > 0;
                if (!detected) {
                    this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,NbBundle.getMessage(DetectPanel.class,"ERROR_NoSDKRegistry"));         //NOI18N
                }
                else if (!validDisplayName) {
                    this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,NbBundle.getMessage(DetectPanel.class,"ERROR_InvalidDisplayName"));    //NOI18N
                }
                else {
                    JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
                    for (int i=0; i<platforms.length; i++) {
                        if (name.equals (platforms[i].getDisplayName())) {
                            usedDisplayName = true;
                            this.wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,NbBundle.getMessage(DetectPanel.class,"ERROR_UsedDisplayName"));    //NOI18N
                            break;
                        }
                    }
                }
                v = detected && validDisplayName && !usedDisplayName;
            }
            setValid(v);            
        }

        private static String createAntName (String name) {
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException ();
            }                        
            String antName = PropertyUtils.getUsablePropertyName(name);            
            if (platformExists (antName)) {
                String baseName = antName;
                int index = 1;
                antName = baseName + Integer.toString (index);
                while (platformExists (antName)) {
                    index ++;
                    antName = baseName + Integer.toString (index);
                }
            }
            return antName;
        }
        
        private static boolean platformExists (String antName) {
            JavaPlatformManager mgr = JavaPlatformManager.getDefault();
            JavaPlatform[] platforms = mgr.getInstalledPlatforms();
            for (int i=0; i < platforms.length; i++) {
                if (platforms[i] instanceof CDCPlatform) {
                    String val = ((CDCPlatform)platforms[i]).getAntName();
                    if (antName.equals(val)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        private boolean isConfigured(){
            CDCPlatformConfigurator configurator = detector.getConfigurator(installedFolder);
            if (configurator == null){
                return true;
            }
            return configurator.isConfigured();
        }        
        
        protected DetectPlatformTask detectPlatformTask = new DetectPlatformTask();
        
        private class DetectPlatformTask implements Runnable {
            private boolean valid = false;
            public boolean isValid (){
                return valid;
            }
            public void run() {
                try {
                    WizardPanel.this.platform = detector.detectPlatform(installedFolder);
                    valid = WizardPanel.this.platform == null ? false : true;
                } catch (IOException ex) {
                    valid = false;
                }                
            }
        }
    }    
}
