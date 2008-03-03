/*
 * HibernateMappingWizardPanel.java
 *
 * Created on February 5, 2008, 12:23 PM
 */
package org.netbeans.modules.hibernate.wizards;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.util.NbBundle;
import org.netbeans.modules.hibernate.loaders.cfg.multiview.Util;
import org.netbeans.modules.hibernate.loaders.cfg.multiview.BrowseFolders;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  gowri
 */
public class HibernateMappingWizardPanel extends javax.swing.JPanel {

    private Project project;

    /** Creates new form HibernateMappingWizardPanel */
    public HibernateMappingWizardPanel(Project project) {
        this.project = project;
        initComponents();
        String[] configFiles = getConfigFilesFromProject(project);
        this.cmbResource.setModel(new DefaultComboBoxModel(configFiles));        
        
        this.browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    org.netbeans.api.project.SourceGroup[] groups = getJavaSourceGroups();
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String className = Util.getResourcePath(groups,fo);
                            txtClassName.setText(className);
                        }
                    } catch (java.io.IOException ex) {}          
            }
        });
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "LBL_HibernateMappingPanel_Name");
    }
    
    public String getClassName() {
        return txtClassName.getText();
    }
    
    private SourceGroup[] getJavaSourceGroups() throws java.io.IOException {        
        if (project==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(project);
        return sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }
    
    // Gets the list of Config files from HibernateEnvironment.
    public String[] getConfigFilesFromProject(Project project) {
        ArrayList<String> configFiles = new ArrayList<String>();
        org.netbeans.api.project.Project enclosingProject = project;
        org.netbeans.modules.hibernate.service.HibernateEnvironment env = enclosingProject.getLookup().lookup(org.netbeans.modules.hibernate.service.HibernateEnvironment.class);
        ArrayList <FileObject> configFileObjects = env.getAllHibernateConfigFileObjects(enclosingProject);
        for (FileObject fo : configFileObjects) {
            configFiles.add(fo.getName());
        }
        return configFiles.toArray(new String[]{});
    }
  

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtClassName = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        cmbResource = new javax.swing.JComboBox();

        setName(org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "LBL_HibernateMappingPanel_Name")); // NOI18N

        jLabel1.setLabelFor(txtClassName);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "HibernateMappingWizardPanel.jLabel1.text")); // NOI18N

        txtClassName.setColumns(30);
        txtClassName.setText(org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "HibernateMappingWizardPanel.txtClassName.text")); // NOI18N

        browseButton.setText(org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "HibernateMappingWizardPanel.browseButton.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(HibernateMappingWizardPanel.class, "HibernateMappingWizardPanel.jLabel2.text")); // NOI18N

        cmbResource.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtClassName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cmbResource, 0, 376, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(browseButton)
                    .add(txtClassName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(cmbResource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox cmbResource;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField txtClassName;
    // End of variables declaration//GEN-END:variables
}
