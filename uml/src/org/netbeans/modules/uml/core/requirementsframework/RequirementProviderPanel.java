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


package org.netbeans.modules.uml.core.requirementsframework;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sheryl
 */
public class RequirementProviderPanel extends javax.swing.JPanel
{
	
	private IRequirementsManager m_Manager = null;
	
	/** Creates new form RequirementProviderPanel */
	public RequirementProviderPanel(IRequirementsManager manager) 
	{
		initComponents();
		m_Manager = manager;
		IRequirementsProvider[] descriptors = manager.getAddIns();
		DefaultComboBoxModel model = new DefaultComboBoxModel(descriptors);
		jComboBox1.setModel(model);
		ProviderCellRenderer renderer = new ProviderCellRenderer();
		jComboBox1.setRenderer(renderer);
		updateState();
	}
	
	public void process()
	{	
		IRequirementsProvider provider = (IRequirementsProvider)jComboBox1.getSelectedItem();
			
		try
		{
			IRequirementSource source = provider.displaySources();
			if(source != null)
			{
				if(m_Manager instanceof IRequirementsManager)
				{
				   IRequirementsManager reqManager = (IRequirementsManager)m_Manager;
				   reqManager.processSource(source);
				}
			}
		}
        catch(RequirementsException ev)
		{
			UMLMessagingHelper helper = new UMLMessagingHelper();
			helper.sendExceptionMessage(ev);
		}
	}
	
	private IRequirementsManager getManager()
	{
		return m_Manager;
	}
	
	private void updateState()
	{
		IRequirementsProvider provider = (IRequirementsProvider)jComboBox1.getSelectedItem();
		jTextArea1.setText(provider.getDescription());
	}
	
	private static class ProviderCellRenderer extends JLabel implements ListCellRenderer  {
      
        public ProviderCellRenderer() {
            setOpaque(true);
        }
         
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
                    
            if ( value instanceof IRequirementsProvider ) {
                setText(((IRequirementsProvider)value).getDisplayName());
            }
            else {
                setText( value == null ? " " : value.toString () ); // NOI18N
            }
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
             
            }
            return this;                    
        }
        
    }
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jComboBox1 = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });

        jComboBox1.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/core/requirementsframework/Bundle").getString("ACSN_Requirement_Provider_Name"));
        jComboBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RequirementProviderPanel.class, "ACSD_Requirement_Provider_Name"));
        jComboBox1.getAccessibleContext().setAccessibleParent(this);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);
        jTextArea1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RequirementProviderPanel.class, "ACSN_Requirement_Provider_Description"));
        jTextArea1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RequirementProviderPanel.class, "ACSD_Requirement_Provider_Description"));

        jLabel1.setLabelFor(jComboBox1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RequirementProviderPanel.class, "LBL_RequirementProviderPanel_Providers"));
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RequirementProviderPanel.class, "ACSN_Requirement_Provider_Name"));
        jLabel1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/core/requirementsframework/Bundle").getString("ACSD_Requirement_Provider_Name"));
        jLabel1.getAccessibleContext().setAccessibleParent(this);

        jLabel2.setLabelFor(jTextArea1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RequirementProviderPanel.class, "LBL_RequirementProviderPanel_ProviderDescritpion"));
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RequirementProviderPanel.class, "ACSN_Requirement_Provider_Description"));
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RequirementProviderPanel.class, "ACSD_Requirement_Provider_Description"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                    .add(jComboBox1, 0, 349, Short.MAX_VALUE)
                    .add(jLabel1)
                    .add(jLabel2))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
		updateState();
	}//GEN-LAST:event_jComboBox1ItemStateChanged
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
	
}
