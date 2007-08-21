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
 * SourcesPanel.java
 *
 * Created on April 8, 2004, 1:39 PM
 */
package org.netbeans.modules.mobility.project.ui.wizard.imports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.modules.mobility.project.ui.wizard.ProjectPanel;
import org.netbeans.modules.mobility.project.ui.wizard.Utils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Kaspar
 */
public class SourcesPanel extends javax.swing.JPanel implements DocumentListener {
    
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(500, 340);
    
    public static final String SOURCES_LOCATION = "SourcesLocation"; // NOI18N
    public static final String JAD_LOCATION = "JadLocation"; // NOI18N

    private static final String JAD="jad";
    private boolean updateJad;
    
    /** Creates new form SourcesPanel */
    public SourcesPanel() {
        initComponents();
        initAccessibility();
        updateJad = true;
        tJad.getDocument().addDocumentListener(this);
    }
    
    public synchronized void addListener(final DocumentListener listener) {
        tLocation.getDocument().addDocumentListener(listener);
        tJad.getDocument().addDocumentListener(listener);
    }
    
    public synchronized void removeListener(final DocumentListener listener) {
        tLocation.getDocument().removeDocumentListener(listener);
        tJad.getDocument().removeDocumentListener(listener);
    }
    
    public synchronized void readData(final TemplateWizard object) {
        Object tmp;
        tmp = object.getProperty(SOURCES_LOCATION);
        if (tmp != null) {
            tLocation.setText((String) tmp);
        } else {
            final FileObject fo = Templates.getExistingSourcesFolder(object);
            final File f = fo == null ? null : FileUtil.toFile(fo);
            tLocation.setText(f == null ? "" : f.getAbsolutePath()); // NOI18N
        }
        tmp = object.getProperty(JAD_LOCATION);
        final boolean tmpUpdateJad = updateJad;
        tJad.setText((tmp != null) ? (String) tmp : ""); // NOI18N
        updateJad = tmpUpdateJad;
    }
    
    public synchronized void storeData(final TemplateWizard object) {
        final String location = tLocation.getText();
        object.putProperty(SOURCES_LOCATION, location);
        object.putProperty(JAD_LOCATION, tJad.getText());
        final String name = new File(location).getName();
        object.putProperty(ProjectPanel.PROJECT_NAME, "Imported Project" + (name != null ? " - " + name+'1' : "1")); // NOI18N
        
        String detectedConfiguration = null;
        String detectedProfile = null;
        try {
            final Map<String,String> map = new HashMap<String,String>();
            final File jadFile = new File(tJad.getText());
            J2MEProjectGenerator.loadJadManifest(map, jadFile);
            detectedConfiguration = map.get("MicroEdition-Configuration"); // NOI18N
            detectedProfile = map.get("MicroEdition-Profile"); // NOI18N
        } catch (IOException e) {
        }
        object.putProperty(PlatformSelectionPanel.REQUIRED_CONFIGURATION, detectedConfiguration);
        object.putProperty(PlatformSelectionPanel.REQUIRED_PROFILE, detectedProfile);
    }
        
    public String getLocationText() {
        return tLocation.getText();
    }
    
    public String getJadLocation() {
        return tJad.getText();
    }
    
    public synchronized void setJadLocation(final String jadLocation) {
        final boolean tmp = updateJad;
        tJad.setText(jadLocation);
        updateJad = tmp;
    }
    
    public boolean hasUpdateJad() {
        return updateJad;
    }
    
    public synchronized void changedUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        updateJad = false;
    }
    
    public synchronized void insertUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        updateJad = false;
    }
    
    public synchronized void removeUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        updateJad = false;
    }
    
    protected boolean isJadDocumentEvent(final DocumentEvent e) {
        return e.getDocument() == tJad.getDocument();
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
        jLabel2 = new javax.swing.JLabel();
        tLocation = new javax.swing.JTextField();
        bBrowse = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        tJad = new javax.swing.JTextField();
        bBrowseJad = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();

        setName(org.openide.util.NbBundle.getMessage(SourcesPanel.class, "TITLE_Sources")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "LBL_Sources_Info")); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setPreferredSize(new java.awt.Dimension(400, 54));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(tLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "LBL_Sources_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 6);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        add(tLocation, gridBagConstraints);
        tLocation.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourcesPanel.class, "ACSD_Sources_Location")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bBrowse, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "LBL_Sources_Browse")); // NOI18N
        bBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 0);
        add(bBrowse, gridBagConstraints);
        bBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourcesPanel.class, "ACSD_Sources_Browse1")); // NOI18N

        jLabel3.setLabelFor(tJad);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "LBL_Sources_Jad")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 6);
        add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        add(tJad, gridBagConstraints);
        tJad.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourcesPanel.class, "ACSD_Sources_JAD")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bBrowseJad, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "LBL_Sources_BrowseJad")); // NOI18N
        bBrowseJad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBrowseJadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 0);
        add(bBrowseJad, gridBagConstraints);
        bBrowseJad.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourcesPanel.class, "ACSD_Sources_Browse2")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourcesPanel.class, "ACSN_Sources"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourcesPanel.class, "ACSD_Sources"));
    }
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    private void bBrowseJadActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBrowseJadActionPerformed
        String location = tJad.getText();
        if (location == null  ||  "".equals(location)) // NOI18N
            location = tLocation.getText();
        if (location == null  ||  "".equals(location)) // NOI18N
            location = System.getProperty("user.home", ""); // NOI18N
        final String jad = Utils.browseFilter(this, location, NbBundle.getMessage(SourcesPanel.class, "TITLE_Sources_JadManifestLocation"), JFileChooser.FILES_ONLY, new FileFilter() { // NOI18N
            public boolean accept(File f) {
                if (! f.exists()  ||  ! f.canRead())
                    return false;
                if (f.isDirectory())
                    return true;
                if (! f.isFile())
                    return false;
                String ext = f.getName();
                int i = ext.lastIndexOf('.');
                if (i >= 0)
                    ext = ext.substring(i + 1);
                ext = ext.toLowerCase();
                return JAD.equals(ext)  ||  "mf".equals(ext); // NOI18N
            }
            public String getDescription() {
                return NbBundle.getMessage(SourcesPanel.class,"LBL_Sources_JadManifestFilter"); // NOI18N
            }
        });
        if (jad != null)
            tJad.setText(jad);
    }//GEN-LAST:event_bBrowseJadActionPerformed
    
    private void bBrowseActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBrowseActionPerformed
        String location = tLocation.getText();
        if (location == null  ||  "".equals(location)) // NOI18N
            location = System.getProperty("user.home", ""); // NOI18N
        final String folder = Utils.browseFolder(this, location, NbBundle.getMessage(SourcesPanel.class, "TITLE_Sources_Location")); // NOI18N
        if (folder != null)
            tLocation.setText(folder);
    }//GEN-LAST:event_bBrowseActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowse;
    private javax.swing.JButton bBrowseJad;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField tJad;
    private javax.swing.JTextField tLocation;
    // End of variables declaration//GEN-END:variables
    
    static class WizardPanel implements TemplateWizard.FinishablePanel, DocumentListener {
        
        SourcesPanel component;
        TemplateWizard wizard;
        Collection<ChangeListener> listeners = new ArrayList<ChangeListener>();
        boolean valid = false;
        
        public void addChangeListener(final ChangeListener changeListener) {
            listeners.add(changeListener);
        }
        
        public void removeChangeListener(final ChangeListener changeListener) {
            listeners.remove(changeListener);
        }
        
        public java.awt.Component getComponent() {
            if (component == null) {
                // !!! use unified workdir
                component = new SourcesPanel();
                component.addListener(this);
                checkValid(true);
            }
            return component;
        }
        
        public org.openide.util.HelpCtx getHelp() {
            return new HelpCtx(SourcesPanel.class);
        }
        
        public boolean isFinishPanel() {
            return false;
        }
        
        public void showError(final String message) {
            if (wizard != null)
                wizard.putProperty("WizardPanel_errorMessage", message); // NOI18N
        }
        
        public boolean isValid() {
            return isValid(false);
        }
        
        public boolean isValid(final boolean allowJadUpdate) {
            boolean valid;
            File f;
            
            f = FileUtil.normalizeFile(new File(component.getLocationText()).getAbsoluteFile());
            valid = component.getLocationText().length()>0 && f != null && f.exists()  &&  f.isDirectory();
            if (! valid) {
                showError(NbBundle.getMessage(SourcesPanel.class, "ERR_Sources_InvalidLocation")); // NOI18N
                return false;
            }
            final FileObject srcRoot = FileUtil.toFileObject(f);
            if (allowJadUpdate  &&  component.hasUpdateJad()) {
                f = findJadManifest(f);
                component.removeListener(this);
                component.setJadLocation(f != null ? f.getAbsolutePath() : ""); // NOI18N
                component.addListener(this);
            }
            
            final String jadLocation = component.getJadLocation();
            if (! "".equals(jadLocation)) {
                f = new File(jadLocation);
                valid = f.exists()  &&  f.isFile()  &&  f.canRead();
                
                if (! valid) {
                    showError(NbBundle.getMessage(SourcesPanel.class, component.hasUpdateJad() ? "ERR_Sources_InvalidAutoJadLocation" : "ERR_Sources_InvalidJadLocation")); // NOI18N
                    return false;
                }
            }
            final Project other = srcRoot == null ? null : FileOwnerQuery.getOwner(srcRoot);
            if (other != null && Arrays.asList(OpenProjects.getDefault().getOpenProjects()).contains(other)) {
                final ProjectInformation pi = other.getLookup().lookup(ProjectInformation.class);
                final String name = pi == null ? other.getProjectDirectory().getPath() : pi.getDisplayName();
                showError(NbBundle.getMessage(SourcesPanel.class, "WARN_Sources_Clashing", name)); // NOI18N
            } else {
                showError(null);
            }
            return true;
        }
        
        private File findJadManifest(final File dir) {
            File res;
            final File[] dirFiles = dir.listFiles();
            
            res = Utils.findSubFile(dirFiles, dir.getName() + ".jad"); // NOI18N
            if (res != null)
                return res;
            res = Utils.findAnyFile(dirFiles, JAD); // NOI18N
            if (res != null)
                return res;
            res = Utils.findSubFile(dirFiles, "manifest.mf"); // NOI18N
            if (res != null)
                return res;
            
            final File metaDir = Utils.findSubFile(dirFiles, "META-INF"); // NOI18N
            if (metaDir != null  &&  metaDir.isDirectory()) {
                final File[] metaFiles = metaDir.listFiles();
                
                res = Utils.findSubFile(metaFiles, "manifest.mf"); // NOI18N
                if (res != null)
                    return res;
                res = Utils.findAnyFile(metaFiles, "mf"); // NOI18N
                if (res != null)
                    return res;
                res = Utils.findSubFile(metaFiles, JAD); // NOI18N
                if (res != null)
                    return res;
            }
            
            res = Utils.findAnyFile(dirFiles, "mf"); // NOI18N
            if (res != null)
                return res;
            
            final File parent = dir.getParentFile();
            if (parent != null) {
                final File[] parentFiles = parent.listFiles();
                
                res = Utils.findSubFile(parentFiles, dir.getName() + ".jad"); // NOI18N
                if (res != null)
                    return res;
                res = Utils.findAnyFile(parentFiles, JAD); // NOI18N
                if (res != null)
                    return res;
                res = Utils.findSubFile(parentFiles, "manifest.mf"); // NOI18N
                if (res != null)
                    return res;
                
                res = Utils.findAnyFile(parentFiles, "mf"); // NOI18N
                if (res != null)
                    return res;
            }
            
            return null;
        }
        
        public void readSettings(final Object obj) {
            wizard = (TemplateWizard) obj;
            ((SourcesPanel) getComponent()).readData(wizard);
        }
        
        public void storeSettings(final Object obj) {
            wizard = (TemplateWizard) obj;
            ((SourcesPanel) getComponent()).storeData(wizard);
        }
        
        void fireStateChange() {
            ChangeListener[] ll;
            synchronized (this) {
                if (listeners.isEmpty())
                    return;
                ll = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            final ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < ll.length; i++)
                ll[i].stateChanged(ev);
        }
        
        void checkValid(final boolean allowJadUpdate) {
            if (isValid(allowJadUpdate) != valid) {
                valid ^= true;
                fireStateChange();
            }
        }
        
        public void changedUpdate(final javax.swing.event.DocumentEvent e) {
            checkValid(! component.isJadDocumentEvent(e));
        }
        
        public void insertUpdate(final javax.swing.event.DocumentEvent e) {
            checkValid(!component.isJadDocumentEvent(e));
        }
        
        public void removeUpdate(final javax.swing.event.DocumentEvent e) {
            checkValid(!component.isJadDocumentEvent(e));
        }
        
    }
    
}
