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
 * DataSourcePropertiesPanel.java
 *
 * Created on March 10, 2004, 12:13 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/** 
 *  
 */
public class EjbDataSourcePropertiesPanel extends javax.swing.JPanel 
{
    private EjbGroup ejbGrp;
    
    public EjbDataSourcePropertiesPanel() {
        initComponents();
    }
    
    public void clear(){
        nameTextField.setText("");
        containerTypeTextField.setText("");
        serverHostTextField.setText("");
        iiopPortTextField.setText("");
    }
    
    public void setDataSourceProperties( EjbGroup group )
    {
        this.ejbGrp = group;
        
        nameTextField.setText( group.getName() );
        containerTypeTextField.setText( group.getAppServerVendor() );
        serverHostTextField.setText( group.getServerHost() );
        iiopPortTextField.setText( Integer.toString( group.getIIOPPort() ) );
    }
     
    
    public boolean saveChange()
    {
        StringBuffer msg = new StringBuffer();
        if( !validateData( msg ) )
        {
            NotifyDescriptor d = new NotifyDescriptor.Message( msg.toString(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify( d );
            return false;
        }
        else
        {
            ejbGrp.setName( getName() );
            ejbGrp.setServerHost( getServerHost() );
            ejbGrp.setIIOPPort( Integer.parseInt( getIIOPPort() ) );
            
            return true;
        }
    }
    
    public String getName()
    {
        // Workaround for 113538 Export/Import EJB feature doesn't work on Linux
        if (nameTextField == null) {
            return "";
        }
        return nameTextField.getText().trim();
    }
    
    public String getServerHost()
    {
        return serverHostTextField.getText().trim();
    }
    
    public String getIIOPPort()
    {
        return iiopPortTextField.getText();
    }
    
    private boolean validateData( StringBuffer errorMsg )
    {
        // Make sure the user didn't change anything to be invalid
        
        boolean valid = true;
        
        if( getName() == null || getName().length() == 0 )
        {
            if( valid )
            {
                nameTextField.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(EjbDataSourcePropertiesPanel.class, "EMPTY_GROUP_NAME") );
            errorMsg.append( "\n" );
        }
        
        if( getServerHost() == null || getServerHost().length() == 0 )
        {
            if( valid )
            {
                serverHostTextField.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(EjbDataSourcePropertiesPanel.class, "EMPTY_SERVER_HOST") );
            errorMsg.append( "\n" );
        }
        else if( getServerHost().indexOf( ' ' ) != -1  )
        {
            // Can not contain spaces
            if( valid )
            {
                serverHostTextField.requestFocus();
                serverHostTextField.selectAll();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(EjbDataSourcePropertiesPanel.class, "SPACES_IN_SERVER_HOST", "\'" + getServerHost() + "\'") );
            errorMsg.append( "\n" );
        }
        
        if( getIIOPPort() == null || getIIOPPort().length() == 0 )
        {
            if( valid )
            {
                iiopPortTextField.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(EjbDataSourcePropertiesPanel.class, "EMPTY_IIOP_PORT") );
            errorMsg.append( "\n" );
        }
        else
        {
            // Make it is a number
            try
            {
                int portNum = Integer.parseInt( getIIOPPort() );
            }
            catch( NumberFormatException ex )
            {
                if( valid )
                {
                    iiopPortTextField.requestFocus();
                    iiopPortTextField.selectAll();
                    valid = false;
                }
                
                errorMsg.append( NbBundle.getMessage(EjbDataSourcePropertiesPanel.class, "IIOP_PORT_NOT_NUMBER") );
                errorMsg.append( "\n" );
            }
        }
        
        return valid;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        iiopPortLabel = new javax.swing.JLabel();
        iiopPortTextField = new javax.swing.JTextField();
        containerTypeLabel = new javax.swing.JLabel();
        containerTypeTextField = new javax.swing.JTextField();
        serverHostLabel = new javax.swing.JLabel();
        serverHostTextField = new javax.swing.JTextField();
        fillPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(EjbDataSourcePropertiesPanel.class, "EJB_GROUP_NAME_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(nameLabel, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle"); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("EJB_GROUP_NAME_DESC")); // NOI18N

        nameTextField.setMinimumSize(new java.awt.Dimension(250, 20));
        nameTextField.setPreferredSize(null);
        nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameTextFieldFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(nameTextField, gridBagConstraints);
        nameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("EJB_GROUP_NAME_DESC")); // NOI18N

        iiopPortLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        iiopPortLabel.setLabelFor(iiopPortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(iiopPortLabel, org.openide.util.NbBundle.getMessage(EjbDataSourcePropertiesPanel.class, "IIOP_PORT_LABEL_R")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(iiopPortLabel, gridBagConstraints);
        iiopPortLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("IIOP_PORT_DESC")); // NOI18N

        iiopPortTextField.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        add(iiopPortTextField, gridBagConstraints);
        iiopPortTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("IIOP_PORT_DESC")); // NOI18N

        containerTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        containerTypeLabel.setLabelFor(containerTypeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(containerTypeLabel, org.openide.util.NbBundle.getMessage(EjbDataSourcePropertiesPanel.class, "APP_SERVER_LABEL2")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(containerTypeLabel, gridBagConstraints);
        containerTypeLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("APP_SERVER_DESC")); // NOI18N

        containerTypeTextField.setEditable(false);
        containerTypeTextField.setMinimumSize(new java.awt.Dimension(250, 20));
        containerTypeTextField.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 0);
        add(containerTypeTextField, gridBagConstraints);
        containerTypeTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("APP_SERVER_DESC")); // NOI18N

        serverHostLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        serverHostLabel.setLabelFor(serverHostTextField);
        org.openide.awt.Mnemonics.setLocalizedText(serverHostLabel, org.openide.util.NbBundle.getMessage(EjbDataSourcePropertiesPanel.class, "SERVER_HOST_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(serverHostLabel, gridBagConstraints);
        serverHostLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("SERVER_HOST_DESC")); // NOI18N

        serverHostTextField.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 0);
        add(serverHostTextField, gridBagConstraints);
        serverHostTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("SERVER_HOST_DESC")); // NOI18N

        fillPanel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void nameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameTextFieldFocusLost
        
    }//GEN-LAST:event_nameTextFieldFocusLost
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel containerTypeLabel;
    private javax.swing.JTextField containerTypeTextField;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JLabel iiopPortLabel;
    private javax.swing.JTextField iiopPortTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel serverHostLabel;
    private javax.swing.JTextField serverHostTextField;
    // End of variables declaration//GEN-END:variables
    
}
