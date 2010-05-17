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
 * ModifyEJBsInsides.java
 *
 * Created on September 9, 2004, 8:50 AM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.openide.util.NbBundle;

/**
 * This class defines the panel for the user to input the Ejb Group data.
 *
 * @author  cao
 */
public class ModifyEjbGroupPanel extends javax.swing.JPanel 
{
    // Remember the originals so that we can check whehter they are changed
    private String orgName;
    private String orgHost;
    private int orgPort;
    
    public ModifyEjbGroupPanel(EjbGroup group)
    {
        initComponents();
        
        orgName = group.getName();
        orgHost = group.getServerHost();
        orgPort = group.getIIOPPort();
        
        groupNameTextField.setText( group.getName() );
        serverHostTextField.setText( group.getServerHost() );
        iiopPortTextField.setText( Integer.toString( group.getIIOPPort() ) );
    }
    
    public ModifyEjbGroupPanel() 
    {   
        this( null );
    }
    
    public String getGroupName()
    {
        return groupNameTextField.getText().trim();
    }
    
    public String getServerHost()
    {
        return serverHostTextField.getText().trim();
    }
    
    public String getIIOPPort()
    {
        return iiopPortTextField.getText().trim();
    }
    
    public boolean validateData( StringBuffer errorMsg )
    {
        // Make sure all the required fields are not empty and valid
        
        boolean valid = true;
        
        if( getGroupName() == null || getGroupName().length() == 0 )
        {
            if( valid )
            {
                groupNameTextField.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(ModifyEjbGroupPanel.class, "EMPTY_GROUP_NAME") );
            errorMsg.append( "\n" );
        }
        // Make sure the user modifies to a unique name
        else if( !orgName.equals( getGroupName() ) &&
                 EjbDataModel.getInstance().getEjbGroup( getGroupName() ) != null )
        {
            if( valid )
            {
                groupNameTextField.requestFocus();
                groupNameTextField.selectAll();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(ModifyEjbGroupPanel.class, "NAME_NOT_UNIQUE", "\'" + getGroupName() + "\'" ) );
            errorMsg.append( "\n" );
        }
        
        if( getServerHost() == null || getServerHost().length() == 0 )
        {
            if( valid )
            {
                serverHostTextField.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(ModifyEjbGroupPanel.class, "EMPTY_SERVER_HOST") );
            errorMsg.append( "\n" );
        }
        // Make sure the server host is still valid
        else if( !orgHost.equals( getServerHost() ) &&
                 getServerHost().indexOf( ' ' ) != -1  )
        {
            // Can not contain spaces
            if( valid )
            {
                serverHostTextField.requestFocus();
                serverHostTextField.selectAll();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(ModifyEjbGroupPanel.class, "SPACES_IN_SERVER_HOST", "\'" + getServerHost() + "\'") );
            errorMsg.append( "\n" );
        }
        
        if( getIIOPPort() == null || getIIOPPort().length() == 0 )
        {
            if( valid )
            {
                iiopPortTextField.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(ModifyEjbGroupPanel.class, "EMPTY_IIOP_PORT") );
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
                
                errorMsg.append( NbBundle.getMessage(ModifyEjbGroupPanel.class, "IIOP_PORT_NOT_NUMBER") );
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

        groupNameLabel = new javax.swing.JLabel();
        groupNameTextField = new javax.swing.JTextField();
        serverHostLabel = new javax.swing.JLabel();
        serverHostTextField = new javax.swing.JTextField();
        iiopPortTextField = new javax.swing.JTextField();
        iiopPortLabel = new javax.swing.JLabel();
        paddingPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(350, 120));
        setLayout(new java.awt.GridBagLayout());

        groupNameLabel.setLabelFor(groupNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(groupNameLabel, org.openide.util.NbBundle.getMessage(ModifyEjbGroupPanel.class, "EJB_GROUP_NAME_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(groupNameLabel, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle"); // NOI18N
        groupNameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("EJB_GROUP_NAME_DESC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        add(groupNameTextField, gridBagConstraints);
        groupNameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("EJB_GROUP_NAME_DESC")); // NOI18N

        serverHostLabel.setLabelFor(serverHostTextField);
        org.openide.awt.Mnemonics.setLocalizedText(serverHostLabel, org.openide.util.NbBundle.getMessage(ModifyEjbGroupPanel.class, "SERVER_HOST_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(serverHostLabel, gridBagConstraints);
        serverHostLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("SERVER_HOST_DESC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        add(serverHostTextField, gridBagConstraints);
        serverHostTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("SERVER_HOST_DESC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 12);
        add(iiopPortTextField, gridBagConstraints);
        iiopPortTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("IIOP_PORT_DESC")); // NOI18N

        iiopPortLabel.setLabelFor(iiopPortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(iiopPortLabel, org.openide.util.NbBundle.getMessage(ModifyEjbGroupPanel.class, "IIOP_PORT_LABEL_R")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
        add(iiopPortLabel, gridBagConstraints);
        iiopPortLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("IIOP_PORT_DESC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(paddingPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(bundle.getString("MODIFY_EJB_GROUP")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("MODIFY_EJB_GROUP")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel groupNameLabel;
    private javax.swing.JTextField groupNameTextField;
    private javax.swing.JLabel iiopPortLabel;
    private javax.swing.JTextField iiopPortTextField;
    private javax.swing.JPanel paddingPanel;
    private javax.swing.JLabel serverHostLabel;
    private javax.swing.JTextField serverHostTextField;
    // End of variables declaration//GEN-END:variables
    
}
