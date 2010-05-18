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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.uml.reporting.wizard;

import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.project.ProjectCellRenderer;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.UMLProject;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sheryl
 */
public class ReportLocationPanel extends WizardPanelBase implements WizardDescriptor.FinishablePanel {
	
	private Project selected;
	private File folder;
	private WizardDescriptor wizardDescriptor;
	
	/**
	 * Creates new form ReportLocationPanel
	 */
	public ReportLocationPanel() {
		initComponents();
		DocumentListener docListener=
			new DocumentListener()
			{
				public void changedUpdate(DocumentEvent e)
				{
				}

				public void insertUpdate(DocumentEvent e)
				{
					checkValidity();
				}

				public void removeUpdate(DocumentEvent e)
				{
					checkValidity();
				}
			};

		locationTextField.getDocument().addDocumentListener(docListener);
		initValue();
	}
	
	
	public boolean isFinishPanel()
	{
		return true;
	}
	
	
	public String getName()
	{
		return NbBundle.getMessage(ReportLocationPanel.class, "TITLE_ReportLocationPanel_WizardTitle");
	}
	
	
	public void initValue()
	{
		Project[] umlProjects = ProjectUtil.getOpenUMLProjects();
		Project[] selectedProjects = ProjectUtil.getSelectedProjects(UMLProject.class);
		if (selectedProjects.length > 0)
			selected = selectedProjects[0];
		else if (umlProjects.length > 0)
			selected = umlProjects[0];
		DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( umlProjects );
		projectComboBox.setModel( projectsModel );
		projectComboBox.setRenderer(new ProjectCellRenderer());
		projectComboBox.setSelectedItem(selected);
	}
	
	
	public Project getSelectedProject()
	{
		return (Project)projectComboBox.getSelectedItem();
	}
	
	
	private void setReportFolder(File f)
	{
		this.folder = f;
		locationTextField.setText(f.getAbsolutePath());
		setValid(true);
	}
	
	
	public File getReportFolder()
	{
		return new File (locationTextField.getText());
	}
	
	
	private void checkValidity()
	{
		String location = locationTextField.getText().trim();
		File dir = new File(location);
		
		wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
		if (dir.exists() && !dir.isDirectory())
		{
			setValid(false);
			wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
					NbBundle.getMessage(ReportLocationPanel.class, 
					"MSG_ReportLocationPanel_InvalidFolder"));
		}
		else if (dir.exists() && dir.listFiles()!=null && dir.listFiles().length>0 )
		{
			setValid(true);
			wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
					NbBundle.getMessage(ReportLocationPanel.class, 
					"MSG_ReportLocationPanel_FolderExists"));
		}
	}
	
	
	public void readSettings(Object object)
	{
		ReportWizardSettings settings = (ReportWizardSettings)object;
		wizardDescriptor = settings.getWizardDescriptor();
		Project p = settings.getProject();
		folder = settings.getReportFolder();
		if (p!=null)
			projectComboBox.setSelectedItem(p);
		if (folder!=null)
			locationTextField.setText(folder.getAbsolutePath());
		else
		{
			FileObject fo = ((Project)projectComboBox.getSelectedItem()).getProjectDirectory();
			String path = FileUtil.toFile(fo.getParent()).getAbsolutePath();
		
			locationTextField.setText(path);
		}
	}
	
	
	public void storeSettings(Object object)
	{
		ReportWizardSettings settings = (ReportWizardSettings)object;
		
		settings.setProject(getSelectedProject());
		settings.setReportFolder(getReportFolder());		
	}
	
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        ProjectLbl = new javax.swing.JLabel();
        projectComboBox = new javax.swing.JComboBox();
        locationLbl = new javax.swing.JLabel();
        locationTextField = new javax.swing.JTextField();
        browseBtn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        msgPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(180, 175));
        setPreferredSize(new java.awt.Dimension(180, 175));
        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(jPanel1, gridBagConstraints);

        ProjectLbl.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/reporting/wizard/Bundle").getString("LBL_ReportLocationPanel_Project"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(ProjectLbl, gridBagConstraints);

        projectComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        add(projectComboBox, gridBagConstraints);

        locationLbl.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/reporting/wizard/Bundle").getString("LBL_ReportLocationPanel_ReportLocation"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(locationLbl, gridBagConstraints);

        locationTextField.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        add(locationTextField, gridBagConstraints);

        browseBtn.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/reporting/wizard/Bundle").getString("CTL_ReportLocationPanel"));
        browseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(browseBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jSeparator1, gridBagConstraints);

        msgPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(msgPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

	private void browseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseBtnActionPerformed
		JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);
		
        if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION) {  
			
			setReportFolder(fc.getSelectedFile());
        }
	}//GEN-LAST:event_browseBtnActionPerformed
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ProjectLbl;
    private javax.swing.JButton browseBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel locationLbl;
    private javax.swing.JTextField locationTextField;
    private javax.swing.JPanel msgPanel;
    private javax.swing.JComboBox projectComboBox;
    // End of variables declaration//GEN-END:variables
	
}
