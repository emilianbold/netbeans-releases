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
 * ImportCDCProjectPanel.java
 *
 * Created on April 8, 2004, 1:39 PM
 */
package org.netbeans.modules.j2me.cdc.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.modules.mobility.project.ui.wizard.ProjectPanel;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * 
 */
public class ImportCDCProjectPanel extends javax.swing.JPanel implements DocumentListener {
    
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(500, 340);
    static final String PROJECT_LOCATION = "ImportProjectLocation"; // NOI18N
    private static String prjType;
    private static String location;
    /**
     * Creates new form ImportCDCProjectPanel
     */
    private ImportCDCProjectPanel(String type) {
        prjType=type;
        initComponents();
        initAccessibility();
    }
    
    public synchronized void addListener(final DocumentListener listener) {
        tLocation.getDocument().addDocumentListener(listener);
    }
    
    public synchronized void removeListener(final DocumentListener listener) {
        tLocation.getDocument().removeDocumentListener(listener);
    }
    
    public synchronized void readData(final TemplateWizard object) {
        Object tmp;
        tmp = object.getProperty(PROJECT_LOCATION);
        if (tmp != null) {
            tLocation.setText((String) tmp);
        } else {
            final FileObject fo = Templates.getExistingSourcesFolder(object);
            final File f = fo == null ? null : FileUtil.toFile(fo);
            tLocation.setText(f == null ? "" : f.getAbsolutePath()); // NOI18N
        }
    }
    
    public synchronized void storeData(final TemplateWizard object) {
        final String location = tLocation.getText();
        object.putProperty(PROJECT_LOCATION, location);
        final String name = new File(location).getName();
        object.putProperty(ProjectPanel.PROJECT_NAME, "Imported Project" + (name != null ? " - " + name+'1' : "1")); // NOI18N
        
        String detectedConfiguration = null;
        String detectedProfile = null;
        final Map<String,String> map = new HashMap<String,String>();
        detectedConfiguration = map.get("MicroEdition-Configuration"); // NOI18N
        detectedProfile = map.get("MicroEdition-Profile"); // NOI18N

        object.putProperty(PlatformSelectionPanel.REQUIRED_CONFIGURATION, detectedConfiguration);
        object.putProperty(PlatformSelectionPanel.REQUIRED_PROFILE, detectedProfile);
    }
        
    public String getLocationText() {
        return tLocation.getText();
    }
        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tLocation = new javax.swing.JTextField();
        bBrowse = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();

        setName(org.openide.util.NbBundle.getMessage(ImportCDCProjectPanel.class, "TITLE_Project",prjType.equals(ImportCDCProjectWizardIterator.CDC55)?ImportCDCProjectWizardIterator.CDC55TYPE:ImportCDCProjectWizardIterator.CDCTOOLKITTYPE)); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ImportCDCProjectPanel.class, "LBL_Import_Info",prjType.equals(ImportCDCProjectWizardIterator.CDC55)?ImportCDCProjectWizardIterator.CDC55TYPE:ImportCDCProjectWizardIterator.CDCTOOLKITTYPE)); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImportCDCProjectPanel.class, "ASCD_Import_Info",prjType.equals(ImportCDCProjectWizardIterator.CDC55)?ImportCDCProjectWizardIterator.CDC55TYPE:ImportCDCProjectWizardIterator.CDCTOOLKITTYPE)); // NOI18N

        jLabel2.setLabelFor(tLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ImportCDCProjectPanel.class, "LBL_Project_Location")); // NOI18N
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
        tLocation.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportCDCProjectPanel.class, "ACSD_Project_Location",prjType.equals(ImportCDCProjectWizardIterator.CDC55)?ImportCDCProjectWizardIterator.CDC55TYPE:ImportCDCProjectWizardIterator.CDCTOOLKITTYPE)); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bBrowse, org.openide.util.NbBundle.getMessage(ImportCDCProjectPanel.class, "LBL_Project_Browse")); // NOI18N
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
        bBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportCDCProjectPanel.class, "ACSD_Project_Browse1")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(ImportCDCProjectPanel.class, "ACSN_Project",prjType.equals(ImportCDCProjectWizardIterator.CDC55)?ImportCDCProjectWizardIterator.CDC55TYPE:ImportCDCProjectWizardIterator.CDCTOOLKITTYPE));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportCDCProjectPanel.class, "ACSD_Project",prjType.equals(ImportCDCProjectWizardIterator.CDC55)?ImportCDCProjectWizardIterator.CDC55TYPE:ImportCDCProjectWizardIterator.CDCTOOLKITTYPE));
    }
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
        
    private void bBrowseActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBrowseActionPerformed
	//GEN-HEADEREND:event_bBrowseActionPerformed
        if (location == null)
            location = tLocation.getText();                                            
        if (location == null  ||  "".equals(location)){ // NOI18N
            location = System.getProperty("user.home", ""); // NOI18N
        } else {
            File f = new File(location);
            if (!f.exists() || !f.isDirectory())
                location = System.getProperty("user.home", ""); // NOI18N
        }
        File origLoc = ProjectChooser.getProjectsFolder();
        ProjectChooser.setProjectsFolder(new File(location));
        JFileChooser ch=createProjectChooser();
        if  (JFileChooser.APPROVE_OPTION == ch.showOpenDialog( WindowManager.getDefault().getMainWindow() ))
        {
            String folder=ch.getSelectedFile().getAbsolutePath();
            if (folder != null)
            {
                tLocation.setText(folder);
                location = new File(folder).getParent();
            }
        }
        ProjectChooser.setProjectsFolder(origLoc);
    }                                       

    public void insertUpdate(DocumentEvent e)                                       
    {
    }

    public void removeUpdate(DocumentEvent e)//GEN-LAST:event_bBrowseActionPerformed
    {
    }

    public void changedUpdate(DocumentEvent e)
    {
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField tLocation;
    // End of variables declaration//GEN-END:variables
    
    static class WizardPanel implements TemplateWizard.FinishablePanel, DocumentListener {
        
        ImportCDCProjectPanel component;
        TemplateWizard wizard;
        Collection<ChangeListener> listeners = new ArrayList<ChangeListener>();
        boolean valid = false;
        final String prjType;
        
        public WizardPanel(String type)
        {
            prjType=type;
        }
        
        public void addChangeListener(final ChangeListener changeListener) {
            listeners.add(changeListener);
        }
        
        public void removeChangeListener(final ChangeListener changeListener) {
            listeners.remove(changeListener);
        }
        
        public java.awt.Component getComponent() {
            if (component == null) {
                // !!! use unified workdir
                component = new ImportCDCProjectPanel(prjType);
                component.addListener(this);
                checkValid();
            }
            return component;
        }
        
        public org.openide.util.HelpCtx getHelp() {
            return new HelpCtx(ImportCDCProjectPanel.class);
        }
        
        public boolean isFinishPanel() {
            return false;
        }
        
        public void showError(final String message) {
            if (wizard != null)
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
        }
        
        public boolean isValid() {
            boolean valid;
            File f;
            
            f = FileUtil.normalizeFile(new File(component.getLocationText()).getAbsoluteFile());
            valid = component.getLocationText().length()>0 && f != null && f.exists()  &&  f.isDirectory() && isOldProject(FileUtil.toFileObject(f), ImportCDCProjectPanel.prjType);
            if (! valid) {
                showError(NbBundle.getMessage(ImportCDCProjectPanel.class, "ERR_Project_InvalidLocation")); // NOI18N
                return false;
            }
            showError(null);
            return true;
        }
        
        
        public void readSettings(final Object obj) {
            wizard = (TemplateWizard) obj;
            ((ImportCDCProjectPanel) getComponent()).readData(wizard);
            Component component = getComponent();
            Object substitute = ((JComponent)component).getClientProperty ("NewProjectWizard_Title"); // NOI18N
            if (substitute != null) {
                wizard.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
            }            
        }
        
        public void storeSettings(final Object obj) {
            wizard = (TemplateWizard) obj;
            ((ImportCDCProjectPanel) getComponent()).storeData(wizard);
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
        
        public void changedUpdate(final javax.swing.event.DocumentEvent e) {
            checkValid();
        }
        
        public void insertUpdate(final javax.swing.event.DocumentEvent e) {
            checkValid();
        }
        
        public void removeUpdate(final javax.swing.event.DocumentEvent e) {
            checkValid();
        }
        
    }
    
    
    private static boolean isOldProject(FileObject fo, String type)
    {
        if (fo  == null || !fo.isValid()) return false;
        FileObject xml=fo.getFileObject("nbproject/project.xml");
        FileObject prop=fo.getFileObject("nbproject/project.properties");
        if (xml != null && prop != null)
        {
            File file=FileUtil.toFile(xml);
            try
            {
                Document doc = XMLUtil.parse(new InputSource(file.toURI().toString()),
                        false, true, null, null);
                NodeList nl=doc.getElementsByTagNameNS("http://www.netbeans.org/ns/project/1","type");
                return nl.item(0).getTextContent().equals(type);
            } catch(Exception ex){};
        }
        return false;
    }

    private static class ProjectFileView extends FileView {

        final private FileSystemView fsv;
        private static final Icon BADGE = ImageUtilities.loadImageIcon("org/netbeans/modules/j2me/cdc/project/resources/projectBadge.gif", false); // NOI18N
        private static final Icon EMPTY = ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/empty.gif", false); // NOI18N
        private Icon lastOriginal;
        private Icon lastMerged;
        
        public ProjectFileView( FileSystemView fs ) {
            this.fsv=fs;
        }

        public Icon getIcon(File _f) {
            //return original.getIcon(f);
            
            if (!_f.exists()) {
                // Can happen when a file was deleted on disk while project
                // dialog was still open. In that case, throws an exception
                // repeatedly from FSV.gSI during repaint.
                return null;
            }
            File f = FileUtil.normalizeFile(_f);
            Icon or = fsv.getSystemIcon(f);
            if (or == null) {
                // L&F (e.g. GTK) did not specify any icon.
                or = EMPTY;
            }
            FileObject fo=FileUtil.toFileObject(f);
            
            if ( fo != null && ImportCDCProjectPanel.isOldProject(fo,ImportCDCProjectPanel.prjType)) {
                
                if ( or.equals( lastOriginal ) ) {
                    return lastMerged;
                }
                lastOriginal = or;
                lastMerged = new MergedIcon(or, BADGE, -1, -1);
                return lastMerged;
            }
            else {
                return or;
            }
        }
    }    
    
    private static class MergedIcon implements Icon {

        private Icon icon1;
        private Icon icon2;
        private int xMerge;
        private int yMerge;

        MergedIcon( Icon icon1, Icon icon2, int xMerge, int yMerge ) {

            this.icon1 = icon1;
            this.icon2 = icon2;

            if ( xMerge == -1 ) {
                xMerge = icon1.getIconWidth() - icon2.getIconWidth();
            }

            if ( yMerge == -1 ) {
                yMerge = icon1.getIconHeight() - icon2.getIconHeight();
            }

            this.xMerge = xMerge;
            this.yMerge = yMerge;
        }

        public int getIconHeight() {
            return Math.max( icon1.getIconHeight(), yMerge + icon2.getIconHeight() );
        }

        public int getIconWidth() {
            return Math.max( icon1.getIconWidth(), yMerge + icon2.getIconWidth() );
        }

        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            icon1.paintIcon( c, g, x, y );
            icon2.paintIcon( c, g, x + xMerge, y + yMerge );
        }
    }
    
     /** Factory method for project chooser
     */
    public static JFileChooser createProjectChooser() {

        ProjectManager.getDefault().clearNonProjectCache(); // #41882
        
        JFileChooser chooser = new ProjectFileChooser();
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

        if ("GTK".equals(javax.swing.UIManager.getLookAndFeel().getID())) { // NOI18N
            // see BugTraq #5027268
            chooser.putClientProperty("GTKFileChooser.showDirectoryIcons", Boolean.TRUE); // NOI18N
            //chooser.putClientProperty("GTKFileChooser.showFileIcons", Boolean.TRUE); // NOI18N
        }
       
        //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
        chooser.setAcceptAllFileFilterUsed( false );
        chooser.setFileFilter( ProjectDirFilter.INSTANCE );

        File currDir = ProjectChooser.getProjectsFolder();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, currDir);
        chooser.setFileView( new ProjectFileView( chooser.getFileSystemView() ) );

        return chooser;
    }
    
    private static class ProjectFileChooser extends JFileChooser {
        
            public void approveSelection() {
                File dir = FileUtil.normalizeFile(getSelectedFile());
                if ( dir != null && ImportCDCProjectPanel.isOldProject( FileUtil.toFileObject(dir),ImportCDCProjectPanel.prjType) ) {
                    super.approveSelection();
                }
                else {
                    setCurrentDirectory( dir );
                }
            }
    }
    
    
    private static class ProjectDirFilter extends FileFilter {

        private static final FileFilter INSTANCE = new ProjectDirFilter( );

        public boolean accept( File f ) {

            if ( f.isDirectory() ) {
                return true; // Directory selected
            }

            return false;
        }

        public String getDescription() {
            return "";
        }

    }

}
