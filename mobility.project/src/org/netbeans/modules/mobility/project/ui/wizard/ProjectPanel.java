/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * ProjectPanel.java
 *
 * Created on April 8, 2004, 2:03 PM
 */
package org.netbeans.modules.mobility.project.ui.wizard;

import java.awt.Component;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class ProjectPanel extends javax.swing.JPanel {
    
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(500, 340);
    
    public static final String PROJECT_NAME = "ProjectName"; // NOI18N
    public static final String PROJECTS_HOME = "ProjectsHome"; // NOI18N
    public static final String PROJECT_LOCATION = "ProjectLocation"; // NOI18N
    public static final String PROJECT_CREATE_MIDLET = "CreateMidlet"; // NOI18N
    public static final String PROJECT_COPY_SOURCES = "CopySources"; //NOI18N

    /** path length limitation for Windows OS */
    private static final int WINDOWS_MAX_PATH_LENGTH = 255;
    
    /** Creates new form ProjectPanel */
    public ProjectPanel(boolean showCreateMIDlet, boolean showCopySources) {
        initComponents();
        initAccessibility();
        cCreateMIDlet.setVisible(showCreateMIDlet);
        jLabel4.setVisible(showCopySources);
        jRadioCopySrc.setVisible(showCopySources);
        jRadioEmptySrc.setVisible(showCopySources);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        tName.requestFocus();
    }
    
    public void addListeners(final DocumentListener documentListener, final ItemListener itemListener) {
        tName.getDocument().addDocumentListener(documentListener);
        tHome.getDocument().addDocumentListener(documentListener);
        cCreateMIDlet.addItemListener(itemListener);
    }
    
    public void removeListeners(final DocumentListener documentListener, final ItemListener itemListener) {
        tName.getDocument().removeDocumentListener(documentListener);
        tHome.getDocument().removeDocumentListener(documentListener);
        cCreateMIDlet.removeItemListener(itemListener);
    }
    
    private static String camelize(final String input) {
        final StringBuffer output = new StringBuffer();
        boolean upper = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == ' ') {
                upper = true;
                continue;
            }
            if (upper) {
                c = Character.toUpperCase(c);
                upper = false;
            }
            output.append(c);
        }
        return output.toString();
    }
    
    public void readData(final WizardDescriptor object) {
        String tmp;
        Boolean b;
        
        tmp = (String) object.getProperty(PROJECT_NAME);
        if (tmp == null) {
            tmp = "My Project"; // NOI18N
        }
        tName.setText(camelize(tmp));
        
        File location = (File) object.getProperty(PROJECT_LOCATION);
        if (location != null){
            tCreated.setText(location.getAbsolutePath());
        } else {
            final File home = ProjectChooser.getProjectsFolder();
            tHome.setText(home != null ? home.getAbsolutePath() : System.getProperty("user.home", "")); // NOI18N

            if (testIfProjectNameExists()) {
                String name = getProjectName();
                if (name.endsWith("1")) { // NOI18N
                    name = name.substring(0, name.length() - 1);
                }
                int i = 2;
                for (;;) {
                    tName.setText(name + i); // NOI18N
                    if (! testIfProjectNameExists()) {
                        break;
                    }
                    i ++;
                }
            }
            tName.selectAll();
        }

        b = (Boolean) object.getProperty(Utils.IS_EMBEDDED);
        boolean embedded = b == null ? false : b.booleanValue();
        if(embedded) {
            org.openide.awt.Mnemonics.setLocalizedText(cCreateMIDlet, org.openide.util.NbBundle.getMessage(ProjectPanel.class, "LBL_Project_CreateIMlet")); // NOI18N
        }
        b = (Boolean) object.getProperty(PROJECT_CREATE_MIDLET);
        cCreateMIDlet.setSelected(b == null ? true : b.booleanValue());
        
        b = (Boolean) object.getProperty(PROJECT_COPY_SOURCES);
        jRadioEmptySrc.setSelected(b == null ? true : !b.booleanValue());
        jRadioCopySrc.setSelected(b == null ? false : b.booleanValue());
    }
    
    private boolean testIfProjectNameExists() {
        boolean valid;
        final File home = new File(getProjectsHome());
        valid = home.exists() && home.isDirectory() && home.canWrite();
        if (! valid) {
            return false;
        }
        
        return new File(home, getProjectName()).exists();
    }
    
    public void storeData(final WizardDescriptor object) {
        object.putProperty(PROJECT_NAME, tName.getText().trim());
        object.putProperty(PROJECTS_HOME, tHome.getText());
        object.putProperty(PROJECT_LOCATION, new File(tCreated.getText()).getAbsoluteFile());
        object.putProperty(PROJECT_CREATE_MIDLET, cCreateMIDlet.isVisible()  &&  cCreateMIDlet.isSelected());
        object.putProperty(PROJECT_COPY_SOURCES, jRadioCopySrc.isVisible()  &&  jRadioCopySrc.isSelected());
    }
    
    public String getProjectName() {
        return tName.getText().trim();
    }
    
    public String getProjectsHome() {
        return tHome.getText();
    }
    
    public void setCreated(final String created) {
        tCreated.setText(created);
    }
    
    public String getCreated() {
        return tCreated.getText();
    }
    
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
        tName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tHome = new javax.swing.JTextField();
        bBrowse = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        tCreated = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        cCreateMIDlet = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jRadioEmptySrc = new javax.swing.JRadioButton();
        jRadioCopySrc = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();

        setName(org.openide.util.NbBundle.getMessage(ProjectPanel.class, "TITLE_Project")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(tName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ProjectPanel.class, "LBL_Project_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 6, 6);
        add(tName, gridBagConstraints);

        jLabel2.setLabelFor(tHome);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ProjectPanel.class, "LBL_Projects_Home")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 6, 6);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 6, 6);
        add(tHome, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bBrowse, org.openide.util.NbBundle.getMessage(ProjectPanel.class, "LBL_Project_Browse")); // NOI18N
        bBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 6, 0);
        add(bBrowse, gridBagConstraints);

        jLabel3.setLabelFor(tCreated);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ProjectPanel.class, "LBL_Project_Created")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 6, 6);
        add(jLabel3, gridBagConstraints);

        tCreated.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 6, 6);
        add(tCreated, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jSeparator1, gridBagConstraints);

        cCreateMIDlet.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cCreateMIDlet, org.openide.util.NbBundle.getMessage(ProjectPanel.class, "LBL_Project_CreateMIDlet")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 6, 0);
        add(cCreateMIDlet, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(ProjectPanel.class, "LBL_ProjectSources")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 6);
        add(jLabel4, gridBagConstraints);

        buttonGroup1.add(jRadioEmptySrc);
        jRadioEmptySrc.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioEmptySrc, NbBundle.getMessage(ProjectPanel.class, "LBL_EmptySources")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jRadioEmptySrc, gridBagConstraints);

        buttonGroup1.add(jRadioCopySrc);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioCopySrc, NbBundle.getMessage(ProjectPanel.class, "LBL_CopySources")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 6, 0);
        add(jRadioCopySrc, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(ProjectPanel.class, "ACSN_Project"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectPanel.class, "ACSD_Project"));
    }
    
    @Override
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    private void bBrowseActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBrowseActionPerformed
        final String folder = Utils.browseFolder(this, tHome.getText(), NbBundle.getMessage(ProjectPanel.class, "TITLE_Project_Home"));
        if (folder != null) {
            tHome.setText(folder);
        }
    }//GEN-LAST:event_bBrowseActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowse;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox cCreateMIDlet;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioCopySrc;
    private javax.swing.JRadioButton jRadioEmptySrc;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField tCreated;
    private javax.swing.JTextField tHome;
    private javax.swing.JTextField tName;
    // End of variables declaration//GEN-END:variables
    
    public static class WizardPanel implements TemplateWizard.FinishablePanel, DocumentListener, ItemListener {
        
        ProjectPanel component;
        TemplateWizard wizard;
        private boolean embedded;
        Collection<ChangeListener> listeners = new ArrayList<ChangeListener>();
        boolean valid = false;
        boolean showCreateMIDlet;
        boolean showCopySources;
        
        public WizardPanel(boolean showCreateMIDlet) {
            this(showCreateMIDlet, false);
        }
        public WizardPanel(boolean showCreateMIDlet, boolean showCopySources) {
            this.showCreateMIDlet = showCreateMIDlet;
            this.showCopySources = showCopySources;
        }
        
        @Override
        public void addChangeListener(final javax.swing.event.ChangeListener changeListener) {
            listeners.add(changeListener);
        }
        
        @Override
        public void removeChangeListener(final javax.swing.event.ChangeListener changeListener) {
            listeners.remove(changeListener);
        }
        
        @Override
        public java.awt.Component getComponent() {
            if (component == null) {
                component = new ProjectPanel(showCreateMIDlet, showCopySources); // NOI18N
                component.addListeners(this, this);
                checkValid();
            }
            return component;
        }
        
        @Override
        public org.openide.util.HelpCtx getHelp() {
            return new HelpCtx(ProjectPanel.class.getName() + (embedded ? "Embedded" : "") ); // NOI18N
        }
        
        @Override
        public boolean isFinishPanel() {
            return true;
        }
        
        public void showError(final String message) {
            if (wizard != null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            }
        }
        
        private boolean isLatin1(final String s) {
            for (int i=0; i<s.length(); i++) {
                final char c = s.charAt(i);
                if (c < 32 || c > 127) {
                    return false;
                }
            }
            return true;
        }
        
        private File getCanonicalFile(final File f) {
            try {
                return f.getCanonicalFile();
            } catch (IOException ioe) {}
            return null;
        }
        
        @Override
        public boolean isValid() {
            String projectName = component.getProjectName();
            if (isIllegalName(projectName)) {
                showError(NbBundle.getMessage(ProjectPanel.class, "ERR_Project_InvalidProjectsName")); // NOI18N
                return false;
            }
            final File home = getCanonicalFile(new File(component.getProjectsHome()).getAbsoluteFile());
            if (home == null) {
                showError(NbBundle.getMessage(ProjectPanel.class, "ERR_Project_InvalidProjectsHome")); // NOI18N
                return false;
                
            }
            final File destFolder = getCanonicalFile(new File(component.getCreated()).getAbsoluteFile());
            if (destFolder == null) {
                showError(NbBundle.getMessage(ProjectPanel.class, "ERR_Project_InvalidProjectsName")); // NOI18N
                return false;
            }
            File projLoc = destFolder;
            while (projLoc != null && !projLoc.exists()) {
                projLoc = projLoc.getParentFile();
            }
            if (projLoc == null || !projLoc.canWrite()) {
                showError(NbBundle.getMessage(ProjectPanel.class, "ERR_Project_ProjectFolderReadOnly")); // NOI18N
                return false;
            }
            projLoc = FileUtil.normalizeFile(projLoc);
            if (FileUtil.toFileObject(projLoc) == null) {
                showError(NbBundle.getMessage(ProjectPanel.class, "ERR_Project_InvalidProjectsHome")); // NOI18N
                return false;
            }
            final File[] kids = destFolder.listFiles();
            if ( destFolder.exists() && kids != null && kids.length > 0) {
                // Folder exists and is not empty
                showError(NbBundle.getMessage(ProjectPanel.class, "ERR_Project_ProjectAlreadyExists")); // NOI18N
                return false;
            }
            if (!isLatin1(component.getProjectName())) {
                showError(NbBundle.getMessage(ProjectPanel.class, "WARN_Project_InvalidCharacters")); // NOI18N
            } else if (Utilities.isWindows()
                    && component.getCreated().length() > WINDOWS_MAX_PATH_LENGTH) {
                showError(NbBundle.getMessage(ProjectPanel.class, "WARN_Project_PathLengthExceeding")); // NOI18N
            } else {
                showError(null);
            }
            return true;
        }
        
        @Override
        public void readSettings(final Object obj) {
            wizard = (TemplateWizard) obj;
            ((ProjectPanel) getComponent()).readData(wizard);
            embedded = wizard.getProperty(Utils.IS_EMBEDDED) == null ? false : (Boolean)wizard.getProperty(Utils.IS_EMBEDDED);
            Component component = getComponent();
            Object substitute = ((JComponent)component).getClientProperty ("NewProjectWizard_Title"); // NOI18N
            if (substitute != null) {
                wizard.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
            }
        }
        
        @Override
        public void storeSettings(final Object obj) {
            wizard = (TemplateWizard) obj;
            ((ProjectPanel) getComponent()).storeData(wizard);
        }
        
        void fireStateChange() {
            ChangeListener[] ll;
            synchronized (this) {
                if (listeners.isEmpty()) {
                    return;
                }
                ll = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            final ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < ll.length; i++) {
                ll[i].stateChanged(ev);
            }
        }
        
        void checkValid() {
            updateCreatedFolderPath();
            if (isValid() != valid) {
                valid ^= true;
                fireStateChange();
            }
        }

        public void updateCreatedFolderPath(){
            String projectName = component.getProjectName();
            String projectFolder = component.getProjectsHome();
            String projFolderPath = FileUtil.normalizeFile(new File(projectFolder)).getAbsolutePath();
            if (projFolderPath.endsWith(File.separator)) {
                component.setCreated(projFolderPath + projectName);
            } else {
                component.setCreated(projFolderPath + File.separator + projectName);
            }
        }
        
        @Override
        public void changedUpdate(@SuppressWarnings("unused")
		final javax.swing.event.DocumentEvent e) {
            checkValid();
        }
        
        @Override
        public void insertUpdate(@SuppressWarnings("unused")
		final javax.swing.event.DocumentEvent e) {
            checkValid();
        }
        
        @Override
        public void removeUpdate(@SuppressWarnings("unused")
		final javax.swing.event.DocumentEvent e) {
            checkValid();
        }
        
        @Override
        public void itemStateChanged(@SuppressWarnings("unused")
		final java.awt.event.ItemEvent e) {
        }
        
    }
    
    static boolean isIllegalName(final String name) {
        return name.length() == 0      || 
            name.indexOf('/')  >= 0 ||        //NOI18N
            name.indexOf('\\') >= 0 ||        //NOI18N
            name.indexOf(':')  >= 0 ||        //NOI18N
            name.indexOf("\"") >= 0 ||        //NOI18N
            name.indexOf('<')  >= 0 ||        //NOI18N
            name.indexOf('>')  >= 0;          //NOI18N
    }

}
