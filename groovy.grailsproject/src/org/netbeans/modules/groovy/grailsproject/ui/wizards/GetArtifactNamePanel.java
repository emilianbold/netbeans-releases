/*
 * NewGrailsProjectPanel.java
 *
 * Created on October 1, 2007, 2:49 PM
 */

package org.netbeans.modules.groovy.grailsproject.ui.wizards;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.NbBundle;
import java.io.File;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;


/**
 *
 * @author  schmidtm
 */
public class GetArtifactNamePanel extends WizardSettingsPanel implements DocumentListener {
    GetArtifactNameStep parentStep;
    GrailsProject project;
    String baseDir;
    private String fileName = "";
    String suffix = "";
    
    SourceCategory cat;
        
    boolean valid(WizardDescriptor settings) {
        
        if (!Utilities.isJavaIdentifier(classNameTextField.getText().trim())) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(NewGrailsProjectWizardIterator.class,
                "GetArtifactNamePanel.NotValidIdentifier"));
            return false;
        }
        if (new File(createdFileTextField.getText().trim()).exists()) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(NewGrailsProjectWizardIterator.class,
                "GetArtifactNamePanel.FileAlreadyExists"));
            return false;
        }
        return true;
    }
    
    void read (WizardDescriptor d) {

    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    void store( WizardDescriptor d ) {
        d.putProperty("projectFolder", projectTextField.getText().trim()); // NOI18N
    }
    
    /** Creates new form NewGrailsProjectPanel */
    public GetArtifactNamePanel(GetArtifactNameStep parentStep, SourceCategory cat) {
        this.parentStep = parentStep;
        this.cat = cat;
        
        initComponents();

        String subDirName = "<unknown>";
        String dirPrefix  = "";
        String className = "";
        FileObject preselectedFolder = parentStep.project.getProjectDirectory().getFileObject(cat.getRelativePath());
        
        switch(cat){
            case GRAILSAPP_DOMAIN:
                setName(NbBundle.getMessage(GetArtifactNamePanel.class,"WIZARD_TITLE_DOMAIN")); // NOI18N
                setTitle(NbBundle.getMessage(GetArtifactNamePanel.class,"TXT_NewDomain"));
                subDirName = "domain";
                dirPrefix = "grails-app" + File.separatorChar;
                className = uniqueName("NewElement", suffix, preselectedFolder);
                break;
            case GRAILSAPP_CONTROLLERS:
                setName(NbBundle.getMessage(GetArtifactNamePanel.class,"WIZARD_TITLE_CONTROLLERS")); // NOI18N
                setTitle(NbBundle.getMessage(GetArtifactNamePanel.class,"TXT_NewController"));
                subDirName = "controllers";
                dirPrefix = "grails-app" + File.separatorChar;
                suffix = "Controller";
                className = uniqueName("NewElement", suffix, preselectedFolder);
                break;
            case GRAILSAPP_SERVICES:
                setName(NbBundle.getMessage(GetArtifactNamePanel.class,"WIZARD_TITLE_SERVICES")); // NOI18N
                setTitle(NbBundle.getMessage(GetArtifactNamePanel.class,"TXT_NewService"));
                subDirName = "services";
                dirPrefix = "grails-app" + File.separatorChar;
                suffix = "Service";
                className = uniqueName("NewElement", suffix, preselectedFolder);
                break; 
            case GRAILSAPP_VIEWS:
                setName(NbBundle.getMessage(GetArtifactNamePanel.class,"WIZARD_TITLE_VIEWS")); // NOI18N
                setTitle(NbBundle.getMessage(GetArtifactNamePanel.class,"TXT_NewView"));
                subDirName = "views";
                dirPrefix = "grails-app" + File.separatorChar;
                className = uniqueName("NewElement", suffix, preselectedFolder);
                break;    
            case GRAILSAPP_TAGLIB:
                setName(NbBundle.getMessage(GetArtifactNamePanel.class,"WIZARD_TITLE_TAGLIB")); // NOI18N
                setTitle(NbBundle.getMessage(GetArtifactNamePanel.class,"TXT_NewTaglib"));
                subDirName = "taglib";
                dirPrefix = "grails-app" + File.separatorChar;
                suffix = "TagLib";
                className = uniqueName("NewElement", suffix, preselectedFolder);
                break;    
            case TEST_INTEGRATION:
                setName(NbBundle.getMessage(GetArtifactNamePanel.class,"WIZARD_TITLE_INTEGRATION")); // NOI18N
                setTitle(NbBundle.getMessage(GetArtifactNamePanel.class,"TXT_NewIntegrationTest"));
                subDirName = "integration";
                dirPrefix = "test" + File.separatorChar;
                suffix = "Tests";
                className = uniqueName("NewElement", suffix, preselectedFolder);
                break;
            case TEST_UNIT:
                setName(NbBundle.getMessage(GetArtifactNamePanel.class,"WIZARD_TITLE_UNIT")); // NOI18N
                setTitle(NbBundle.getMessage(GetArtifactNamePanel.class,"TXT_NewUnitTest"));
                subDirName = "unit";
                dirPrefix = "test" + File.separatorChar;
                suffix = "Tests";
                className = uniqueName("NewElement", suffix, preselectedFolder);
                break;
            case SCRIPTS:
                setName(NbBundle.getMessage(GetArtifactNamePanel.class,"WIZARD_TITLE_SCRIPTS")); // NOI18N
                setTitle(NbBundle.getMessage(GetArtifactNamePanel.class,"TXT_NewScript"));
                subDirName = "scripts";
                className = uniqueName("NewElement", suffix, preselectedFolder);
                break;    
            }
        
        // populate the panel with some stuff
        
        project = parentStep.getGrailsProject();

        projectTextField.setText(project.getProjectDirectory().getName());
        
        baseDir =   FileUtil.getFileDisplayName(project.getProjectDirectory()) + 
                    File.separatorChar + dirPrefix + subDirName;
        
        createdFileTextField.setText(baseDir + File.separatorChar );
        
        // register event listeners to auto-update some fields.

        classNameTextField.getDocument().addDocumentListener( this );
        classNameTextField.setText(className);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        classNameLabel = new javax.swing.JLabel();
        projectLabel = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();
        projectTextField = new javax.swing.JTextField();
        createdFileLabel = new javax.swing.JLabel();
        createdFileTextField = new javax.swing.JTextField();

        classNameLabel.setDisplayedMnemonic('N');
        classNameLabel.setLabelFor(classNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classNameLabel, org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetDomainClassNamePanel.projectNameLabel.text")); // NOI18N

        projectLabel.setDisplayedMnemonic('P');
        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetDomainClassNamePanel.projectFolderLabel.text")); // NOI18N

        classNameTextField.setText(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetDomainClassNamePanel.projectNameTextField.text")); // NOI18N

        projectTextField.setEditable(false);
        projectTextField.setText(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.projectTextField.text")); // NOI18N

        createdFileLabel.setDisplayedMnemonic('F');
        createdFileLabel.setLabelFor(createdFileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFileLabel, org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.createdFileLabel.text")); // NOI18N

        createdFileTextField.setEditable(false);
        createdFileTextField.setText(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.createdFileTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(classNameLabel)
                    .add(createdFileLabel)
                    .add(projectLabel))
                .add(23, 23, 23)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(createdFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(classNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(projectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(classNameLabel)
                    .add(classNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(projectTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdFileLabel)
                    .add(createdFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        classNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.classNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.projectLabel.AccessibleContext.accessibleDescription")); // NOI18N
        classNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.classNameTextField.AccessibleContext.accessibleName")); // NOI18N
        classNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.classNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.projectTextField.AccessibleContext.accessibleName")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.projectTextField.AccessibleContext.accessibleDescription")); // NOI18N
        createdFileLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.createdFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        createdFileTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.createdFileTextField.AccessibleContext.accessibleName")); // NOI18N
        createdFileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.createdFileTextField.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetArtifactNamePanel.class, "GetArtifactNamePanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classNameLabel;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JLabel createdFileLabel;
    private javax.swing.JTextField createdFileTextField;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    // End of variables declaration//GEN-END:variables


    private void setTitle(String title) {
        putClientProperty("NewFileWizard_Title", title); // NOI18N
        getAccessibleContext().setAccessibleName(title);
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateTexts( e ) ;
    }

    public void removeUpdate(DocumentEvent e) {
        updateTexts( e ) ;
    }

    public void changedUpdate(DocumentEvent e) {
        updateTexts( e ) ;
    }
    
    /** Handles changes in the Project name and project directory
     */
    private void updateTexts( DocumentEvent e ) {
        
        Document doc = e.getDocument();
                
        if ( doc == classNameTextField.getDocument() ) {
            
            String artifactName = classNameTextField.getText().trim();
            // there can be package name as part of the artifact name,
            // we don't have package chooser in this wizard
            if (artifactName.indexOf('.') != -1) {
                artifactName = artifactName.replace('.', File.separatorChar);
            }
            
            fileName = baseDir + File.separatorChar + artifactName + suffix + ".groovy";
            createdFileTextField.setText(fileName);
            projectTextField.setText(project.getProjectDirectory().getName());
            
            parentStep.fireChangeEvent();
            
        }                
  
    }

    public String getArtifactName(){
        return classNameTextField.getText().trim();
    }

    public String getFileName() {
        return fileName;
    }

    private static String uniqueName(final String baseName, String suffix, FileObject preselectedFolder) {
        String activeName = baseName;
        if (preselectedFolder != null) {
            int index = 0;
            while (true) {
                FileObject _tmp = preselectedFolder.getFileObject(activeName + suffix, "groovy");    //NOI18N
                if (_tmp == null) {
                    break;
                }
                activeName = baseName + ++index;
            }
        }
        return activeName;
    }

}
