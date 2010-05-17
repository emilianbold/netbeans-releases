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

package org.netbeans.modules.uml.project.ui.customizer;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.project.UMLProject;

import org.openide.util.Utilities;



/**
 *
 * @author  Mike Frisino
 */
public class PanelUmlImports extends JPanel
        implements PropertyChangeListener {


	// TODO - improve the rendering so that the end user sees a more friendly
	// value in the table instead of the current "object.toString" value.
	// We still need the object, so don't throw out the object in favor of 
	// a string. You will have to either add a string column or add a custom
	// cell renderer to render the object as a user friendly string.
	
	
	// TODO - decide whether we want to allow users to explicity add and/or 
	// remove imports. If we have the implicit import add, it may make the
	// explicit action unnecessary and just a possible trap.
	// Even without the explicit add/remove, we still need this panel even
	// if it is a read only feature.
	
	// TODO - implement the "implicit" import add whenever developer drags
	// modeling element from one project to another
	// though that work will not  be done in this class. 
	
	// TODO - decide whether we want to use this "file system" style project
	// chooser or the "open projects only " chooser that we currently use in 
	// the Java Project affiliation. This is moot if we remove the "Add" button
	
 
    
     /** Creates new form PanelUmlImports */
    public PanelUmlImports(
           UMLProjectProperties uiProperties) {
        
        this.uiProperties = uiProperties;

        initComponents();  
        // we decided we don't want to show this button
        // leaving it in for testing purposes only
        addUMLProjectButton.setVisible(false);         
        imports.setModel(uiProperties.umlProjectImportsModel);

                
        jScrollPane1.getViewport().setBackground( imports.getBackground() );
        imports.getTableHeader().setReorderingAllowed(false);
        TableColumn col = imports.getTableHeader().getColumnModel().getColumn(0);
        col.setCellRenderer(new ProjectCellRender());
        
    }
    
    public void propertyChange (PropertyChangeEvent event) {
		
		// TODO - do we need to do anything here? Bulletproofing?
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        addUMLProjectButton = new javax.swing.JButton();
        importedUMLProjectsPanel = new javax.swing.JPanel();
        importedUMLProjectLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        imports = new javax.swing.JTable();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName("");
        getAccessibleContext().setAccessibleDescription("");
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/project/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addUMLProjectButton, bundle.getString("LBL_AddUmlLProjectAction")); // NOI18N
        addUMLProjectButton.setActionCommand("BROWSE");
        addUMLProjectButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                handleAddUmlImport(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(addUMLProjectButton, gridBagConstraints);
        addUMLProjectButton.getAccessibleContext().setAccessibleName("");
        addUMLProjectButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BrowseUmlProjectButton")); // NOI18N

        importedUMLProjectsPanel.setLayout(new java.awt.BorderLayout());

        importedUMLProjectsPanel.setPreferredSize(new java.awt.Dimension(200, 200));
        importedUMLProjectLabel.setLabelFor(imports);
        org.openide.awt.Mnemonics.setLocalizedText(importedUMLProjectLabel, bundle.getString("LBL_ImportedUmlProjectsLabel")); // NOI18N
        importedUMLProjectsPanel.add(importedUMLProjectLabel, java.awt.BorderLayout.NORTH);
        importedUMLProjectLabel.getAccessibleContext().setAccessibleName("");
        importedUMLProjectLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ImportedUmlProjectsLabel")); // NOI18N

        jScrollPane1.setBorder(null);
        imports.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String []
            {
                "UML Project", "Label"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean []
            {
                false, false
            };

            public Class getColumnClass(int columnIndex)
            {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        imports.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        jScrollPane1.setViewportView(imports);
        imports.getAccessibleContext().setAccessibleName("");
        imports.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelUmlImports.class, "ACSD_ImportedProjectTable")); // NOI18N

        importedUMLProjectsPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane1.getAccessibleContext().setAccessibleName("");
        jScrollPane1.getAccessibleContext().setAccessibleDescription("");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(importedUMLProjectsPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

	private void handleAddUmlImport(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_handleAddUmlImport
// TODO add your handling code here:
		

			AntArtifactChooser.ArtifactItem artifactItems[] = 
			AntArtifactChooser.showDialog(UMLProject.ARTIFACT_TYPE_UML_PROJ, 
			uiProperties.getProject(), this);			


			if (artifactItems != null) {
				int[] newSelection = UMLImportsUiSupport.addArtifacts( 
					uiProperties.umlProjectImportsModel, artifactItems);
			}
		
        
	}//GEN-LAST:event_handleAddUmlImport
    
   public class ProjectCellRender extends DefaultTableCellRenderer
   {
       protected void setValue(Object value)
       {
           if (value != null)
           {
               setText(value.toString());
               Icon image = ImageUtil.instance().getIcon("uml-project.png"); // NOI18N

               if (image != null)
                   setIcon(image);
           }
       }
   }
   
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addUMLProjectButton;
    private javax.swing.JLabel importedUMLProjectLabel;
    private javax.swing.JPanel importedUMLProjectsPanel;
    private javax.swing.JTable imports;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    

    
    private boolean valid;
    private final UMLProjectProperties uiProperties;    
}

