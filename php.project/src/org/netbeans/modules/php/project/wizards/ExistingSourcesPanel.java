/*
 * SourcesPanelVisual.java
 *
 * Created on 21 Август 2007 г., 16:17
 */

package org.netbeans.modules.php.project.wizards;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.ui.SourceRootsUi;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  avk
 */
public class ExistingSourcesPanel extends javax.swing.JPanel 
        implements PropertyChangeListener
{

    private static final String TIP_FULL_SOURCE_PATH = "TIP_SourcePath"; // NOI18N
    private static final String LBL_SELECT_SOURCE_FOLDER = "LBL_Select_Source_Folder_Title"; // NOI18N
    private static final String BROWSE = "BROWSE"; // NOI18N

    public static final String PROP_SOURCE_ROOT  = "sourceRootDir";    // NOI18N
    
    /** Creates new form SourcesPanelVisual */
    ExistingSourcesPanel(PhpProjectConfigurePanel panel) {
        myPanel = panel;

        initComponents();

        init(panel);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PanelProjectLocationVisual.PROP_PROJECT_DIR)){
            String oldDir = (String)evt.getOldValue();
            String newDir = (String)evt.getNewValue();
            File srcRoot = getSourceRootWithProjectChange(oldDir, newDir);
            viewSourcesRoot(srcRoot);
        }
    }
    
    boolean dataIsValid(WizardDescriptor wizardDescriptor) {
        return validate(wizardDescriptor);
    }

    void store(WizardDescriptor descriptor) {
        if (myLastUsedSourceDir != null) {
            descriptor.putProperty(NewPhpProjectWizardIterator.SOURCE_ROOT, myLastUsedSourceDir);
        }
    }

    void read(WizardDescriptor settings) {
        
        myProjectDir = getProjectDir(settings);
        
        File srcRoot = (File) settings.getProperty(NewPhpProjectWizardIterator.SOURCE_ROOT);
        if (srcRoot == null){
            srcRoot = getDefaultSourceRoot(myProjectDir);
        }

        viewSourcesRoot(srcRoot);
    }

    private void init(PhpProjectConfigurePanel panel) {
        myPanel = panel;
        
        // Register listener on the textFields to make the automatic updates
        myListener = new Listener();
        mySourceFolder.getDocument().addDocumentListener(myListener);

        //text field is not editable. But we set it's BG color to look as it is editable.
        // To show that it can ber changed (at least using button)
        mySourceFolder.setBackground(getTextFieldBgColor());
    }
        
    private boolean validate(WizardDescriptor wizardDescriptor) {
        if (!validateSourceRoot(wizardDescriptor)) {
            return false;
        }
        wizardDescriptor.putProperty(
                NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, "");
        return true;
    }

    private boolean validateSourceRoot(WizardDescriptor wizardDescriptor) {
        // TODO validate
        return true;
    }

    PhpProjectConfigurePanel getPanel() {
        return myPanel;
    }
    
    private File getDefaultSourceRoot(File projectDir){
        String srcPath = getPanel().getDefaultSourceRoot();
        File src = new File(srcPath);
        if (src.isAbsolute()){
            return src;
        } else if (srcPath.equals(NewPhpProjectWizardIterator.CURRENT_FOLDER_PATTERN)){
            return projectDir;
        } else {
            return new File(projectDir, srcPath);
        }
    }

    private File getSourceRootWithProjectChange(
            String oldProjectDir, String newProjectDir)
    {
        
        String oldSourceDir = null;
        if (myLastUsedSourceDir != null){
            oldSourceDir = myLastUsedSourceDir.getAbsolutePath();
        }
        
        if (oldSourceDir == null) {
            return new File(newProjectDir);
        } 
        else if (oldSourceDir.equals(oldProjectDir)) {
            return new File(newProjectDir);
        } 
        else if (oldSourceDir.startsWith(oldProjectDir)) {
            File oldProject = new File(oldProjectDir);
            String oldDir = oldProject.getAbsolutePath();
            
            String relativeSourceDir = oldSourceDir.substring(oldDir.length()+1);
            File newProject = new File(newProjectDir);
            
            return new File(newProject, relativeSourceDir);
        }
        return myLastUsedSourceDir;
        
    }
    
    private File getProjectDir(WizardDescriptor settings) {
        File projectDir = (File) settings.
                getProperty(NewPhpProjectWizardIterator.PROJECT_DIR);
        if (projectDir == null) {
            File projectLocation = getPanel().getProjectLocation(settings);
            String projectName = getPanel().getProjectName(settings);
            projectDir = new File(projectLocation, projectName);
        }
        return projectDir;
    }
    
    private void viewSourcesRoot(File sourceRoot) {
        
        File oldSourceDir = myLastUsedSourceDir;
        myLastUsedSourceDir = sourceRoot;
        
        if (sourceRoot == null) {
            mySourceFolder.setText("");
            return;
        }
        String sourcePath = sourceRoot.getAbsolutePath();

        
            mySourceFolder.setText(sourcePath);
            /*
        if (projectPath.equalsIgnoreCase(sourcePath)) {
            mySourceFolder.setText(sourcePath);
            //mySourceFolder.setText("."); // NOI18N
        } else if (sourcePath.startsWith(projectPath + File.separator)) {
            String name = sourcePath.substring(projectPath.length() + 1);
            mySourceFolder.setText(name);
        } else {
            mySourceFolder.setText(sourcePath);
        }
             */

        String message = NbBundle.getMessage(ExistingSourcesPanel.class, TIP_FULL_SOURCE_PATH);
        String tip = MessageFormat.format(message, sourcePath);
        mySourceFolder.setToolTipText(tip);
        
        Logger.getLogger(getClass().getName()).info("SRC change: "+oldSourceDir+" -> "+myLastUsedSourceDir);
        firePropertyChange(PROP_SOURCE_ROOT, oldSourceDir, myLastUsedSourceDir);
        
    }

    private String getMessage(String key, Object... args) {
        String message = null;
        if (args.length > 0) {
            message = MessageFormat.format(NbBundle.getMessage(ExistingSourcesPanel.class, key), args);
        } else {
            message = NbBundle.getMessage(ExistingSourcesPanel.class, key);
        }
        return message;
    }

    private void performUpdate(DocumentEvent event) {
        getPanel().fireChangeEvent(); // Notify that the panel changed
    }

    private class Listener implements DocumentListener {

        public void changedUpdate(DocumentEvent event) {
            performUpdate(event);
        }

        public void insertUpdate(DocumentEvent event) {
            performUpdate(event);
        }

        public void removeUpdate(DocumentEvent event) {
            performUpdate(event);
        }
    }

    private Color getTextFieldBgColor(){
        JTextField tf = new JTextField();
        tf.setEditable(true);
        tf.setEnabled(true);
        return tf.getBackground();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mySourceFolderLabel = new javax.swing.JLabel();
        mySourceFolder = new javax.swing.JTextField();
        myBrowse = new javax.swing.JButton();

        mySourceFolderLabel.setLabelFor(mySourceFolder);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/project/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(mySourceFolderLabel, bundle.getString("LBL_Source_Folder")); // NOI18N

        mySourceFolder.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(myBrowse, org.openide.util.NbBundle.getMessage(ExistingSourcesPanel.class, "LBL_Browse_Btn")); // NOI18N
        myBrowse.setActionCommand(BROWSE);
        myBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myBrowsedoBrowse(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(mySourceFolderLabel)
                .add(18, 18, 18)
                .add(mySourceFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myBrowse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mySourceFolderLabel)
                    .add(mySourceFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myBrowse))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        mySourceFolderLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExistingSourcesPanel.class, "A11_Source_Folder_Lbl")); // NOI18N
        mySourceFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExistingSourcesPanel.class, "A11_Source_Folder")); // NOI18N
        myBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExistingSourcesPanel.class, "A11_Browse_Btn")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void myBrowsedoBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myBrowsedoBrowse
        String command = evt.getActionCommand();

        if (BROWSE.equals(command)) {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle(getMessage(LBL_SELECT_SOURCE_FOLDER));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        File projectDir = myProjectDir;
        File curDir = null;
        if (myLastUsedSourceDir != null) {
            curDir = myLastUsedSourceDir;
        }
        if (curDir == null) {
            curDir = projectDir;
        }
        if (curDir != null) {
            chooser.setCurrentDirectory(curDir);
        }

        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File sourceDir = chooser.getSelectedFile();
            if (sourceDir != null) {
                File normSourceDir = FileUtil.normalizeFile(sourceDir);
                File normProjectDir = FileUtil.normalizeFile(projectDir);
                if (SourceRootsUi.isRootNotOccupied(normSourceDir, normProjectDir)) {
                    viewSourcesRoot(normSourceDir);
                } else {
                    SourceRootsUi.showSourceUsedDialog(normSourceDir);
                }
            }
        }
            
            
            getPanel().fireChangeEvent();
        }
    }//GEN-LAST:event_myBrowsedoBrowse


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton myBrowse;
    private javax.swing.JTextField mySourceFolder;
    private javax.swing.JLabel mySourceFolderLabel;
    // End of variables declaration//GEN-END:variables
    private PhpProjectConfigurePanel myPanel;
    private DocumentListener myListener;
    private File myProjectDir;
    private File myLastUsedSourceDir;

}
