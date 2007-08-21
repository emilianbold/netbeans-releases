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
 * WtkPanel.java
 *
 * Created on April 8, 2004, 1:39 PM
 */
package org.netbeans.modules.mobility.project.ui.wizard.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.modules.mobility.project.ui.wizard.ProjectPanel;
import org.netbeans.modules.mobility.project.ui.wizard.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;

/**
 *
 * @author  David Kaspar
 */
public class WtkPanel extends javax.swing.JPanel {
    
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(500, 340);
    
    public static final String WTK_LOCATION = "WtkLocation"; // NOI18N
    public static final String APP_LOCATION = "AppLocation"; // NOI18N
    
    private static String DEFAULT_WTK_LOCATION = null;
    
    private DefaultListModel listModel;
    
    /** Creates new form WtkPanel */
    public WtkPanel() {
        initComponents();
        initAccessibility();
        lApps.setModel(listModel = new DefaultListModel());
        lApps.setCellRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(@SuppressWarnings("unused")
			final JList list, final Object value, @SuppressWarnings("unused")
			final int index, final boolean isSelected, final boolean cellHasFocus) {
                final ListItem item = (ListItem) value;
                final JLabel label = new JLabel("<html><b>" + item.toString() + "</b><br>" + item.getPath()); // NOI18N
                label.setOpaque(true);
                label.setBackground(UIManager.getDefaults().getColor(isSelected ? "List.selectionBackground" : "List.background")); // NOI18N
                if (cellHasFocus) {
                    final JPanel panel = new JPanel();
                    panel.setBackground(UIManager.getDefaults().getColor(isSelected ? "List.selectionBackground" : "List.background")); // NOI18N
                    panel.setLayout(new GridBagLayout());
                    panel.add(label, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
                    panel.setBorder(new LineBorder(UIManager.getDefaults().getColor("Button.focus"), 1)); // NOI18N
                    return panel;
                }
                label.setBorder(new EmptyBorder(6, 6, 6, 6));
                return label;
            }
            
        });
    }
    
    public void addListener(final DocumentListener documentListener, final ListSelectionListener listSelectionListener) {
        tLocation.getDocument().addDocumentListener(documentListener);
        lApps.addListSelectionListener(listSelectionListener);
    }
    
    public void removeListener(final DocumentListener documentListener, final ListSelectionListener listSelectionListener) {
        tLocation.getDocument().removeDocumentListener(documentListener);
        lApps.removeListSelectionListener(listSelectionListener);
    }
    
    /**
     * Returns the default instalation directories of known WTKs installed on the system. The order is from the latest to
     * to the olders version. Last version checked for is WTK 2.3 - which at the time of writting didn't yet exist :).
     */
    public static String getWTKInstalationDirs() {
        final JavaPlatform plat[] = JavaPlatformManager.getDefault().getPlatforms(null,  new Specification(J2MEPlatform.SPECIFICATION_NAME, null, null));
        for (int i=0; i<plat.length; i++) {
            if (plat[i] instanceof J2MEPlatform) {
                final String name = ((J2MEPlatform)plat[i]).getName().toLowerCase();
                if ((name.indexOf("wireless") >= 0 && name.indexOf("toolkit") >= 0) || name.indexOf("wtk") >= 0) {
                    final String path = ((J2MEPlatform)plat[i]).getHomePath();
                    if (new File(path, "apps").isDirectory()) return path; //NOI18N
                }
            }
        }
        
        final String[] unixInstallDirs = {"WTK2.3", "WTK2.2", "WTK2.1", "WTK2.0", "WTK104"}; //NOI18N
        final String[] winInstallDirs = {"WTK23", "WTK22", "WTK21", "WTK20", "WTK104"}; //NOI18N
        String[] installDirs;
        String baseDir;
        
        if (Utilities.isUnix()) {
            baseDir = System.getProperty("user.home", "") + System.getProperty("file.separator"); //NOI18N
            installDirs = unixInstallDirs;
        } else {
            baseDir = "c:\\"; // NOI18N
            installDirs = winInstallDirs;
        }
        
        for (int i = 0; i < installDirs.length; i++) {
            final String wtkDir = baseDir + installDirs[i];
            if (new File(wtkDir).isDirectory())
                return wtkDir;
        }
        return null;
    }
    
    public void readData(final TemplateWizard object) {
        String tmp;
        tmp = (String) object.getProperty(WTK_LOCATION);
        if (tmp == null)
            tmp = DEFAULT_WTK_LOCATION;
        if (tmp == null)
            tmp = getWTKInstalationDirs();
        if (tmp == null)
            tmp = System.getProperty("user.home", ""); // NOI18N
        tLocation.setText(tmp); // NOI18N
        tmp = (String) object.getProperty(APP_LOCATION);
        if (tmp != null) for (int a = 0; a < listModel.getSize(); a ++) {
            final ListItem item = (ListItem) listModel.getElementAt(a);
            if (tmp.equals(item.getPath())) {
                lApps.setSelectedIndex(a);
                break;
            }
        }
    }
    
    public void storeData(final TemplateWizard object) {
        object.putProperty(WTK_LOCATION, tLocation.getText());
        DEFAULT_WTK_LOCATION = tLocation.getText();
        final ListItem item = (ListItem) lApps.getSelectedValue();
        final String app = (item != null) ? item.getPath() : null;
        object.putProperty(APP_LOCATION, app);
        String name = null;
        if (item != null)
            name = item.getName();
        else if (app != null)
            name = new File(app).getName();
        object.putProperty(ProjectPanel.PROJECT_NAME, "Imported Project" + (name != null ? " - " + name+'1' : "1")); // NOI18N
        
        String detectedConfiguration = null;
        String detectedProfile = null;
        try {
            final Map<String,String> map = new HashMap<String,String>();
            J2MEProjectGenerator.loadJadAndManifest(map, J2MEProjectGenerator.findWtkJadFile(app), J2MEProjectGenerator.findWtkManifestFile(app));
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
    
    public void setAppList(final ArrayList<ListItem> items) {
        listModel.clear();
        for (int a = 0; a < items.size(); a ++)
            listModel.addElement(items.get(a));
        if (items.size() > 0)
            lApps.setSelectedIndex(0);
    }
    
    public int getAppCount() {
        return listModel.size();
    }
    
    public ListItem getSelectedApp() {
        return (ListItem) lApps.getSelectedValue();
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
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lApps = new javax.swing.JList();

        setName(org.openide.util.NbBundle.getMessage(WtkPanel.class, "TITLE_Wtk")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WtkPanel.class, "LBL_Wtk_Info")); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setPreferredSize(new java.awt.Dimension(400, 72));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(tLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WtkPanel.class, "LBL_Wtk_Location")); // NOI18N
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
        tLocation.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WtkPanel.class, "ACSD_WTK_Location")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bBrowse, org.openide.util.NbBundle.getMessage(WtkPanel.class, "LBL_Wtk_Browse")); // NOI18N
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
        bBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WtkPanel.class, "ACSD_WTK_Browse")); // NOI18N

        jLabel4.setLabelFor(lApps);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(WtkPanel.class, "LBL_Wtk_DetectedApps")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 6);
        add(jLabel4, gridBagConstraints);

        lApps.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(lApps);
        lApps.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WtkPanel.class, "ACSD_WTK_Apps")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(WtkPanel.class, "ACSN_Wtk"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WtkPanel.class, "ACSD_Wtk"));
    }
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    private void bBrowseActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBrowseActionPerformed
        final String folder = Utils.browseFolder(this, tLocation.getText(), NbBundle.getMessage(WtkPanel.class, "TITLE_Wtk_Location")); // NOI18N
        if (folder != null)
            tLocation.setText(folder);
    }//GEN-LAST:event_bBrowseActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList lApps;
    private javax.swing.JTextField tLocation;
    // End of variables declaration//GEN-END:variables
    
    static class ListItem {
        
        String name;
        String desc;
        String path;
        
        public ListItem(String name, String desc, String path) {
            this.name = name;
            this.desc = desc;
            this.path = path;
        }
        
        public String getName() {
            return name;
        }
        
        public String getPath() {
            return path;
        }
        
        public String toString() {
            if (desc != null)
                return name + " - " + desc; // NOI18N
            return name;
        }
        
    }
    
    static class WizardPanel implements TemplateWizard.FinishablePanel, DocumentListener, ListSelectionListener {
        
        WtkPanel component;
        TemplateWizard wizard;
        Collection<ChangeListener> listeners = new ArrayList<ChangeListener>();
        boolean valid = false;
        
        public void addChangeListener(final javax.swing.event.ChangeListener changeListener) {
            listeners.add(changeListener);
        }
        
        public void removeChangeListener(final javax.swing.event.ChangeListener changeListener) {
            listeners.remove(changeListener);
        }
        
        public java.awt.Component getComponent() {
            if (component == null) {
                component = new WtkPanel();
                component.addListener(this, this);
                checkValid();
            }
            return component;
        }
        
        public org.openide.util.HelpCtx getHelp() {
            return new HelpCtx(WtkPanel.class);
        }
        
        public boolean isFinishPanel() {
            return false;
        }
        
        public void showError(final String message) {
            if (wizard != null)
                wizard.putProperty("WizardPanel_errorMessage", message); // NOI18N
        }
        
        public boolean isValid() {
            boolean valid;
            File f;
            final WtkPanel panel = (WtkPanel) getComponent();
            
            f = new File(panel.getLocationText());
            valid = f.exists();
            if (! valid) {
                showError(NbBundle.getMessage(WtkPanel.class, "ERR_Wtk_InvalidLocation")); // NOI18N
                return false;
            }
            
            final int count = panel.getAppCount();
            if (count <= 0) {
                showError(NbBundle.getMessage(WtkPanel.class, "ERR_Wtk_NoAppFound")); // NOI18N
                return false;
            }
            final ListItem app = panel.getSelectedApp();
            if (app == null) {
                showError(NbBundle.getMessage(WtkPanel.class, "ERR_Wtk_NoAppSelected")); // NOI18N
                return false;
            }
            f = new File(app.path + File.separator + "src").getAbsoluteFile();//NOI18N
            f = f.isDirectory() ? FileUtil.normalizeFile(f) : null;
            final FileObject srcRoot = f == null ? null : FileUtil.toFileObject(f);
            final Project other = srcRoot == null ? null : FileOwnerQuery.getOwner(srcRoot);
            if (other != null && Arrays.asList(OpenProjects.getDefault().getOpenProjects()).contains(other)) {
                final ProjectInformation pi = other.getLookup().lookup(ProjectInformation.class);
                final String name = pi == null ? other.getProjectDirectory().getPath() : pi.getDisplayName();
                showError(NbBundle.getMessage(SourcesPanel.class, "WARN_WTK_SourcesClashing", name)); // NOI18N
            } else {
                showError(null);
            }
            return true;
        }
        
        public void readSettings(final Object obj) {
            wizard = (TemplateWizard) obj;
            ((WtkPanel) getComponent()).readData(wizard);
        }
        
        public void storeSettings(final Object obj) {
            wizard = (TemplateWizard) obj;
            ((WtkPanel) getComponent()).storeData(wizard);
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
        
        void checkValid() {
            if (isValid() != valid) {
                valid ^= true;
                fireStateChange();
            }
        }
        
        public ListItem getWtkProjectDirectory(final File project) {
            if (! project.exists()  ||  ! project.isDirectory()  ||  ! project.canRead())
                return null;
            String name = project.getName();
            String desc = null;
            boolean isValid = false;
            final File bin = new File(project, "bin"); // NOI18N
            final File[] files = bin.listFiles();
            File file = Utils.findSubFile(files, project.getName() + ".jad"); // NOI18N
            if (file == null)
                file = Utils.findAnyFile(files, "jad"); // NOI18N
            if (file != null  &&  file.exists()  &&  file.isFile()) {
                isValid = true;
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), J2MEProjectGenerator.DEFAULT_ENCODING));
                    for (;;) {
                        final String line = reader.readLine();
                        if (line == null)
                            break;
                        final int i = line.indexOf(':');
                        if (i < 0)
                            continue;
                        final String property = line.substring(0, i).trim();
                        if ("MIDlet-Name".equals(property)) // NOI18N
                            name =line.substring(i + 1).trim();
                        if ("MIDlet-Description".equals(property)) // NOI18N
                            desc = line.substring(i + 1).trim();
                    }
                } catch (IOException e) {
                    if (reader != null) try { reader.close(); } catch (IOException ee) {}
                }
            }
            return isValid ? new ListItem(name, desc, project.getAbsolutePath()) : null;
        }
        
        public void updateApps() {
            final WtkPanel panel = (WtkPanel) getComponent();
            final ArrayList<ListItem> list = new ArrayList<ListItem>();
            final String location = panel.getLocationText();
            final File wtk = new File(location);
            if (wtk.exists()  &&  wtk.isDirectory()) {
                final File apps = new File(wtk, "apps"); // NOI18N
                if (apps.exists()  &&  apps.isDirectory()  &&  wtk.canRead()) {
                    final File[] files = apps.listFiles();
                    if (files != null) for (int a = 0; a < files.length; a ++) {
                        final ListItem item = getWtkProjectDirectory(files[a]);
                        if (item != null)
                            list.add(item);
                    }
                }
            }
            if (list.size() <= 0) {
                final ListItem item = getWtkProjectDirectory(wtk);
                if (item != null)
                    list.add(item);
            }
            panel.setAppList(list);
        }
        
        public void changedUpdate(@SuppressWarnings("unused")
		final javax.swing.event.DocumentEvent e) {
            updateApps();
            checkValid();
        }
        
        public void insertUpdate(@SuppressWarnings("unused")
		final javax.swing.event.DocumentEvent e) {
            updateApps();
            checkValid();
        }
        
        public void removeUpdate(@SuppressWarnings("unused")
		final javax.swing.event.DocumentEvent e) {
            updateApps();
            checkValid();
        }
        
        public void valueChanged(@SuppressWarnings("unused")
		final javax.swing.event.ListSelectionEvent e) {
            checkValid();
        }
        
    }
    
}
