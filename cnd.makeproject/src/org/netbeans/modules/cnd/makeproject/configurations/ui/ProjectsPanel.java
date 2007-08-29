/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class ProjectsPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener {
    private Project project;
    private MakeConfiguration conf;
    private MyListEditorPanel myListEditorPanel;
    private String baseDir;
    private PropertyEditorSupport editor;

    private JButton addProjectButton;
    private JButton addStandardLibraryButton;
    private JButton addLibraryButton;
    private JButton addLibraryFileButton;
    private JButton addLibraryOption;
    
    public ProjectsPanel(Project project, MakeConfiguration conf, String baseDir, Object[] data, PropertyEditorSupport editor, PropertyEnv env) {
	this.project = project;
	this.conf = conf;
	this.baseDir = baseDir;
        this.editor = editor;
        initComponents();
	//
	addProjectButton = new JButton(getString("ADD_PROJECTS_BUTTON_TXT")); // NOI18N
	addProjectButton.setToolTipText(getString("ADD_PROJECTS_BUTTON_TT")); // NOI18N
	addProjectButton.setMnemonic(getString("ADD_PROJECTS_BUTTON_MN").charAt(0)); // NOI18N
        
	JButton[] extraButtons = new JButton[] {addProjectButton};
	myListEditorPanel = new MyListEditorPanel(data, extraButtons);
        addProjectButton.addActionListener(new AddProjectButtonAction());
	//
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(myListEditorPanel, gridBagConstraints);
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        setPreferredSize(new java.awt.Dimension(700, 350));
        
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
    }

    public void setInstructionsText(String txt) {
	instructionsTextArea.setText(txt);
    }

    public void setListData(Object[] data){
	myListEditorPanel.setListData(data);
    }

    public Vector getListData() {
	return myListEditorPanel.getListData();
    }

    private Object getPropertyValue() throws IllegalStateException {
	return getListData();
    }
        
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("RequiredProjects"); // NOI18N
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        outerPanel = new javax.swing.JPanel();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(323, 223));
        outerPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(outerPanel, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 12);
        add(instructionPanel, gridBagConstraints);
    }
    
    private class MyListEditorPanel extends TableEditorPanel {
	public MyListEditorPanel(Object[] objects, JButton[] extraButtons) {
	    super(objects, extraButtons, baseDir);
	    getAddButton().setVisible(false);
	    getCopyButton().setVisible(false);
	    getEditButton().setVisible(false);
	    getDefaultButton().setVisible(false);
	}

	public String getListLabelText() {
	    return getString("PROJECTS_TXT");
	}
	public char getListLabelMnemonic() {
	    return getString("PROJECTS_MN").charAt(0);
	}
    }

    class AddProjectButtonAction implements java.awt.event.ActionListener {
	public void actionPerformed(java.awt.event.ActionEvent evt) {
	    MakeArtifact[] artifacts = MakeArtifactChooser.showDialog(MakeArtifactChooser.ArtifactType.PROJECT, project, myListEditorPanel);
	    if (artifacts != null) {
		for (int i = 0; i < artifacts.length; i++) {
		    String location;
		    String workingdir;
		    if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
			location = IpeUtils.toAbsoluteOrRelativePath(baseDir, artifacts[i].getProjectLocation());
			workingdir = IpeUtils.toAbsoluteOrRelativePath(baseDir, artifacts[i].getWorkingDirectory());
		    }
		    else if (PathPanel.getMode() == PathPanel.REL) {
			location = IpeUtils.toRelativePath(baseDir, artifacts[i].getProjectLocation());
			workingdir = IpeUtils.toRelativePath(baseDir, artifacts[i].getWorkingDirectory());
		    }
		    else {
			location = artifacts[i].getProjectLocation();
			workingdir = artifacts[i].getWorkingDirectory();
		    }
		    location = FilePathAdaptor.normalize(location);
		    workingdir = FilePathAdaptor.normalize(workingdir);
		    artifacts[i].setProjectLocation(location);
		    artifacts[i].setWorkingDirectory(workingdir);
                    artifacts[i].setBuild(false);
		    myListEditorPanel.addObjectAction(new LibraryItem.ProjectItem(artifacts[i]));
		}
	    }
	}
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JPanel outerPanel;
    // End of variables declaration//GEN-END:variables
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(ProjectsPanel.class);
	}
	return bundle.getString(s);
    }
}
