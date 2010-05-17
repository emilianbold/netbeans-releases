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

/*
 * PromotePatternPanel.java
 *
 * Created on October 30, 2006, 4:30 PM
 */

package org.netbeans.modules.uml.designpattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sheryl
 */
public class PromotePatternPanel extends javax.swing.JPanel {
	
	/** Creates new form PromotePatternPanel */
	public PromotePatternPanel(IDesignPatternManager manager,
			IDesignPatternDetails details) {
		m_Manager = manager;
		m_Details = details;
		initComponents();
		populateProjects();
	}
	
	
	private void populateProjects()
	{
		List<String> projects = new ArrayList<String>();
		IWorkspace pWorkspace = DesignPatternUtilities.getDesignPatternCatalogWorkspace();
		if (pWorkspace != null)
		{
			ETList <IWSProject> pProjects = pWorkspace.getWSProjects();
			if (pProjects != null)
			{	
				int numOfProjects = pProjects.size();
				for (int x = 0; x < numOfProjects; x++)
				{
					IWSProject pProject = pProjects.get(x);
					if (pProject != null)
					{
						// get their names, but only if they are user defined, not our shipped
						String name = pProject.getName();
					  
						  // if we are promoting, then we only want to display user defined
						  String file = pProject.getLocation();
						  String xsExtension = FileSysManip.getExtension( file );
						  if (xsExtension != null && (!(xsExtension.equals(FileExtensions.PATTERN_EXT_NODOT))))
						  {
							  if (name != null && name.length() > 0)
							  {
								  projects.add(name);
							  }
						  }	
					}
				}
				Collections.sort(projects);	
			}
		}
		DefaultComboBoxModel model = new DefaultComboBoxModel(projects.toArray());
		m_ProjectCombo.setModel(model);
		if (projects.size()>0)
		{
			m_ProjectCombo.setSelectedIndex(0);
			updateNamespaces();
		}
		else
		{
			model = new DefaultComboBoxModel();
			m_NamespaceCombo.setModel(model);
		}
	}
	
	
	private void updateNamespaces()
	{
		IProject pProject=null;
		String sName = (String) m_ProjectCombo.getSelectedItem();
		if (sName != null)
		{
			// get our application
			IApplication pApp = ProductHelper.getApplication();
			if (pApp != null)
			{
				if (pApp != null)
				{
					IWorkspace pUserWork = DesignPatternUtilities.getDesignPatternCatalogWorkspace();
					if (pUserWork != null)
					{
						pProject = pApp.openProject(pUserWork, sName);
						if (pProject == null)
						{
							errorMsg.setText(NbBundle.getMessage(
									PromotePatternPanel.class, "IDS_OPENPROJECTFAILED", sName));
						}
					}
					
					m_Project = pProject;		
					List<String> namespaces = new ArrayList<String>();
					// get the package element names
					String pattern = "//UML:Element.ownedElement/UML:Package";
					ETList<String> names = DesignPatternUtilities.getElementNames(pProject, pattern, false);
					if (names != null)
					{
						// add a blank to the combo so that the user can blank it out if
						// necessary, since we will be defaulting it for them
						namespaces.add(" ");
						// loop through the results and add them to the list box
						for (int x = 0; x < names.size(); x++)
						{
							String str = names.get(x);
							namespaces.add(str);
						}
						Collections.sort(namespaces);	
						DefaultComboBoxModel model = new DefaultComboBoxModel(namespaces.toArray());
						m_NamespaceCombo.setModel(model);
						m_NamespaceCombo.setSelectedIndex(0);
					}
				}
			}
		}
	}
	
	
	protected IDesignPatternDetails getDesignPatterDetail()
	{
		m_Details.setProject(m_Project);
		m_Details.setNamespace(DesignPatternUtilities.getSelectedNamespace(m_NamespaceCombo, m_Project));
		m_Details.setRemoveOnPromote(m_RemoveCheck.isSelected());
		return m_Details;
	}
	
	protected void promote()
	{
		m_Manager.promotePattern(getDesignPatterDetail());
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        m_ProjectCombo = new javax.swing.JComboBox();
        m_NamespaceCombo = new javax.swing.JComboBox();
        m_RemoveCheck = new javax.swing.JCheckBox();
        errorMsg = new javax.swing.JLabel();

        jLabel1.setLabelFor(m_ProjectCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PromotePatternPanel.class, "IDS_PROJECT"));
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PromotePatternPanel.class, "ACSD_PROJECT_COMBOBOX"));

        jLabel2.setLabelFor(m_NamespaceCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PromotePatternPanel.class, "IDS_NAMESPACE"));
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PromotePatternPanel.class, "ACSD_Namespace"));

        m_ProjectCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        m_ProjectCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                m_ProjectComboItemStateChanged(evt);
            }
        });

        m_NamespaceCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(m_RemoveCheck, org.openide.util.NbBundle.getMessage(PromotePatternPanel.class, "IDS_REMOVEFROMPROJECT"));
        m_RemoveCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        m_RemoveCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));
        m_RemoveCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PromotePatternPanel.class, "ACSD_remove_from_project"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(errorMsg))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(15, 15, 15)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(m_NamespaceCombo, 0, 194, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                    .add(m_ProjectCombo, 0, 194, Short.MAX_VALUE)))))
                    .add(m_RemoveCheck))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(m_ProjectCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(m_NamespaceCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 18, Short.MAX_VALUE)
                .add(m_RemoveCheck)
                .add(19, 19, 19)
                .add(errorMsg)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void m_ProjectComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_m_ProjectComboItemStateChanged
		updateNamespaces();
	}//GEN-LAST:event_m_ProjectComboItemStateChanged
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorMsg;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox m_NamespaceCombo;
    private javax.swing.JComboBox m_ProjectCombo;
    private javax.swing.JCheckBox m_RemoveCheck;
    // End of variables declaration//GEN-END:variables
	
	private IProject m_Project = null;
	private IDesignPatternManager m_Manager = null;
	private IDesignPatternDetails m_Details  = null;
}
