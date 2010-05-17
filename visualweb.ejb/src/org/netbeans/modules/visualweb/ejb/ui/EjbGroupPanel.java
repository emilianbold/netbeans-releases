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
 * JPanel.java
 *
 * Created on May 5, 2004, 11:06 AM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import javax.swing.event.DocumentEvent;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbContainerVendor;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import java.io.File;
import java.util.*;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;
import org.openide.modules.InstalledFileLocator;

/**
 * This class defines the panel for the user to input the Ejb Group data.
 *
 * @author  cao
 */
public class EjbGroupPanel extends javax.swing.JPanel      
{
    // Pointing to the <INSTALL_DIR>/samples/ejb/client-jars dir by default
    private static File DEFAULT_CURRENT_JAR_DIR_FILE = InstalledFileLocator.getDefault().locate("samples/ejb/client-jars", null, false ); // NOI18N
    
    private boolean isNewCreation = true;
    private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public EjbGroupPanel(EjbGroup group)
    {
        initComponents();
        setName( NbBundle.getMessage(EjbGroupPanel.class, "ADD_EJB_GROUP" ) );
        
        containerTypeCombo.setModel( new javax.swing.DefaultComboBoxModel( EjbContainerVendor.getContainerTypeNames() ) );
        ClientJarFileListModel listModel = new ClientJarFileListModel();
        
        groupNameTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                for (ChangeListener listener : listeners) {
                    listener.stateChanged(new ChangeEvent(EjbGroupPanel.this));
                }
            }

            public void removeUpdate(DocumentEvent e) {
                for (ChangeListener listener : listeners) {
                    listener.stateChanged(new ChangeEvent(EjbGroupPanel.this));
                }
            }

            public void changedUpdate(DocumentEvent e) {
                for (ChangeListener listener : listeners) {
                    listener.stateChanged(new ChangeEvent(EjbGroupPanel.this));
                }
            }
            
        });
        
        if( group == null )
        {
            String initName = EjbDataModel.getInstance().getAUniqueName( "DeployedEjbApp" ); // I18N???
            groupNameTextField.setText( initName );
        }
        else
        {
            isNewCreation = false;
            
            groupNameTextField.setText( group.getName() );
            serverHostTextField.setText( group.getServerHost() );
            iiopPortTextField.setText( Integer.toString( group.getIIOPPort() ) );
            containerTypeCombo.setSelectedItem( group.getAppServerVendor() );
            ddLocTextField.setText( group.getDDLocationFile() );
            
            // Populate the client jar list
            for( Iterator iter = group.getClientJarFiles().iterator(); iter.hasNext(); )
            {
                listModel.addJarFile( (String)iter.next() );
            }
            
            removeClientJarButton.setEnabled( true );
        }
        
        clientJarsList.setModel( listModel );
    }
    
    public EjbGroupPanel() 
    {   
        this( null );
    }
    
    public String getGroupName()
    {
        return groupNameTextField.getText().trim();
    }
    
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
    
    public ArrayList getClientJars()
    {
        //return clientJarTextField.getText().trim();
        ArrayList fileNames = new ArrayList();
        for( int i = 0; i < clientJarsList.getModel().getSize(); i ++ )
        {
            fileNames.add( clientJarsList.getModel().getElementAt( i ) );
        }
        
        return fileNames;
    }
    
    public String getDDLocationFile()
    {
        if( ddLocTextField.getText() != null && ddLocTextField.getText().trim().length() != 0 )
            return ddLocTextField.getText().trim();
        else
            return null;
    }
    
    public String getContainerType()
    {
        return (String)containerTypeCombo.getSelectedItem();
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
            
            errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "EMPTY_GROUP_NAME") );
            errorMsg.append( "\n" );
        }
        // Check uniqueness if modifying an existing group
        else if( !isNewCreation && EjbDataModel.getInstance().getEjbGroup( getGroupName() ) != null )
        {
            if( valid )
            {
                groupNameTextField.requestFocus();
                groupNameTextField.selectAll();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "NAME_NOT_UNIQUE", "\'" + getGroupName() + "\'") );
            errorMsg.append( "\n" );
        }
        
        if( getContainerType() == null || getContainerType().length() == 0 )
        {
            if( valid )
            {
                containerTypeCombo.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "EMPTY_APP_SERVER") );
            errorMsg.append( "\n" );
        }
        
        if( getServerHost() == null || getServerHost().length() == 0 )
        {
            if( valid )
            {
                serverHostTextField.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "EMPTY_SERVER_HOST") );
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
            
            errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "SPACES_IN_SERVER_HOST", "\'" + getServerHost() + "\'" ) );
            errorMsg.append( "\n" );
        }
        
        if( getIIOPPort() == null || getIIOPPort().length() == 0 )
        {
            if( valid )
            {
                iiopPortTextField.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "EMPTY_IIOP_PORT") );
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
                
                errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "IIOP_PORT_NOT_NUMBER") );
                errorMsg.append( "\n" );
            }
        }
        
        if( getClientJars() == null || getClientJars().size() == 0 )
        {
            if( valid )
            {
                clientJarsList.requestFocus();
                valid = false;
            }
            
            errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "EMPTY_CLIENT_JAR") );
            errorMsg.append( "\n" );
        }
        else
        {
            // Make sure they are existed
            for( Iterator iter = getClientJars().iterator(); iter.hasNext(); )
            {
                String jar = (String)iter.next();
                if( !(new File(jar)).exists() )
                {
                    if( valid ) 
                    {
                        clientJarsList.requestFocus();
                        valid = false;
                    }
                    
                    errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "CLIENT_JAR_NOT_EXIST", jar) );
                    errorMsg.append( "\n" );
                }
            }
        }
        
        // Make sure the dd location file is existed if the user has specified one
        if( getDDLocationFile() != null && getDDLocationFile().length() != 0 ) {
            if( !(new File(getDDLocationFile())).exists() ) 
            {
                if( valid ) 
                {
                    ddLocTextField.requestFocus();
                    ddLocTextField.selectAll();
                    valid = false;
                }
            
                errorMsg.append( NbBundle.getMessage(AddEjbGroupDialog.class, "DD_FILE_NOT_EXIST", getDDLocationFile()) );
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
        containerTypeLabel = new javax.swing.JLabel();
        containerTypeCombo = new javax.swing.JComboBox();
        serverHostLabel = new javax.swing.JLabel();
        serverHostTextField = new javax.swing.JTextField();
        iiopPortLabel = new javax.swing.JLabel();
        iiopPortTextField = new javax.swing.JTextField();
        ddLocLabel1 = new javax.swing.JLabel();
        ddLocLabel2 = new javax.swing.JLabel();
        ddLocTextField = new javax.swing.JTextField();
        ddLocButton = new javax.swing.JButton();
        clientJarsLabel = new javax.swing.JLabel();
        clientJarScrollPane = new javax.swing.JScrollPane();
        clientJarsList = new javax.swing.JList();
        cleintJarButtonPanel = new javax.swing.JPanel();
        addClientJarButton = new javax.swing.JButton();
        removeClientJarButton = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(510, 292));
        setLayout(new java.awt.GridBagLayout());

        groupNameLabel.setLabelFor(groupNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(groupNameLabel, org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "EJB_GROUP_NAME_LABEL")); // NOI18N
        groupNameLabel.setPreferredSize(new java.awt.Dimension(20, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 23;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(groupNameLabel, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle"); // NOI18N
        groupNameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("EJB_GROUP_NAME_DESC")); // NOI18N

        groupNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupNameTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 159;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 2, 0);
        add(groupNameTextField, gridBagConstraints);
        groupNameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("EJB_GROUP_NAME_DESC")); // NOI18N

        containerTypeLabel.setLabelFor(containerTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(containerTypeLabel, org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "APP_SERVER_LABEL1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(containerTypeLabel, gridBagConstraints);
        containerTypeLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("APP_SERVER_DESC")); // NOI18N

        containerTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                containerTypeComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 159;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(containerTypeCombo, gridBagConstraints);
        containerTypeCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("APP_SERVER_DESC")); // NOI18N

        serverHostLabel.setLabelFor(serverHostTextField);
        org.openide.awt.Mnemonics.setLocalizedText(serverHostLabel, org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "SERVER_HOST_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 49;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(serverHostLabel, gridBagConstraints);
        serverHostLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("SERVER_HOST_DESC")); // NOI18N

        serverHostTextField.setText("localhost");
        serverHostTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverHostTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 159;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(serverHostTextField, gridBagConstraints);
        serverHostTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("SERVER_HOST_DESC")); // NOI18N

        iiopPortLabel.setLabelFor(iiopPortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(iiopPortLabel, org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "IIOP_PORT_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 36;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(iiopPortLabel, gridBagConstraints);
        iiopPortLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("IIOP_PORT_DESC")); // NOI18N

        iiopPortTextField.setText("3700");
        iiopPortTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iiopPortTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 159;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(iiopPortTextField, gridBagConstraints);
        iiopPortTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("IIOP_PORT_DESC")); // NOI18N

        ddLocLabel1.setLabelFor(ddLocTextField);
        org.openide.awt.Mnemonics.setLocalizedText(ddLocLabel1, org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "DEPLOYMENT_DESCRIPTOR_LOCATION_LABEL1")); // NOI18N
        ddLocLabel1.setDoubleBuffered(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(ddLocLabel1, gridBagConstraints);
        ddLocLabel1.getAccessibleContext().setAccessibleDescription(bundle.getString("DEPLOYMENT_DESCRIPTOR_LOCATION_DESC")); // NOI18N

        ddLocLabel2.setText(bundle.getString("DEPLOYMENT_DESCRIPTOR_LOCATION_LABEL2")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(ddLocLabel2, gridBagConstraints);

        ddLocTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ddLocTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(ddLocTextField, gridBagConstraints);
        ddLocTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("DEPLOYMENT_DESCRIPTOR_LOCATION_DESC")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ddLocButton, org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "BROWSE_DD_BUTTON_LABEL")); // NOI18N
        ddLocButton.setActionCommand("Select");
        ddLocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ddLocButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 10);
        add(ddLocButton, gridBagConstraints);
        ddLocButton.getAccessibleContext().setAccessibleDescription(bundle.getString("BROWSE_DD_BUTTON_DESC")); // NOI18N

        clientJarsLabel.setLabelFor(clientJarsList);
        org.openide.awt.Mnemonics.setLocalizedText(clientJarsLabel, org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "CLIENT_JAR_FILE_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 53;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(clientJarsLabel, gridBagConstraints);
        clientJarsLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIENT_JAR_DESC")); // NOI18N

        clientJarScrollPane.setMinimumSize(new java.awt.Dimension(260, 60));
        clientJarScrollPane.setPreferredSize(new java.awt.Dimension(260, 60));

        clientJarsList.setMaximumSize(new java.awt.Dimension(500, 500));
        clientJarsList.setMinimumSize(new java.awt.Dimension(260, 132));
        clientJarsList.setPreferredSize(new java.awt.Dimension(100, 50));
        clientJarScrollPane.setViewportView(clientJarsList);
        clientJarsList.getAccessibleContext().setAccessibleDescription(bundle.getString("CLIENT_JAR_DESC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(clientJarScrollPane, gridBagConstraints);

        cleintJarButtonPanel.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        org.openide.awt.Mnemonics.setLocalizedText(addClientJarButton, org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "ADD_CLIENT_JAR_BUTTON_LABEL")); // NOI18N
        addClientJarButton.setActionCommand("Add");
        addClientJarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClientJarButtonActionPerformed(evt);
            }
        });
        cleintJarButtonPanel.add(addClientJarButton);
        addClientJarButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ADD_CLIENT_JAR_BUTTON_DESC")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeClientJarButton, org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "REMOVE_CLIENT_JAR_BUTTON_LABEL")); // NOI18N
        removeClientJarButton.setEnabled(false);
        removeClientJarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClientJarButtonActionPerformed(evt);
            }
        });
        cleintJarButtonPanel.add(removeClientJarButton);
        removeClientJarButton.getAccessibleContext().setAccessibleDescription(bundle.getString("REMOVE_CLIENT_JAR_BUTTON_DESC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 10);
        add(cleintJarButtonPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "ADD_EJB_GROUP")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EjbGroupPanel.class, "ADD_EJB_GROUP")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void removeClientJarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClientJarButtonActionPerformed
        Object[] selectedFiles = clientJarsList.getSelectedValues();
        for( int i = 0; i < selectedFiles.length; i ++ )
        {
            ((ClientJarFileListModel)clientJarsList.getModel()).removeElement( selectedFiles[i] );
        }
        
        // Enable the remove button if there are jars in the list
        if( clientJarsList.getModel().getSize() > 0 )
            removeClientJarButton.setEnabled( true );
        else
            removeClientJarButton.setEnabled( false );
        
    }//GEN-LAST:event_removeClientJarButtonActionPerformed

    private void addClientJarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClientJarButtonActionPerformed
        // Pop up FileChooser to let the user select a .ear or .jar file where
        // the EJB deployment descriptors are contained
        
        JFileChooser clientJarFileChooser = org.netbeans.modules.visualweb.extension.openide.awt.JFileChooser_RAVE.getJFileChooser();
        clientJarFileChooser.setMultiSelectionEnabled( true );
        clientJarFileChooser.setFileFilter( new JarFileFilter( true ) );
        clientJarFileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        
        // Set the current directory -- WHERE ???
        File curDir =  DEFAULT_CURRENT_JAR_DIR_FILE;
        
        clientJarFileChooser.setCurrentDirectory( curDir );
        
        int returnVal = clientJarFileChooser.showOpenDialog( this );
        
        if( returnVal == JFileChooser.APPROVE_OPTION ) 
        {
            File[] files = clientJarFileChooser.getSelectedFiles();
            
            for( int i = 0; i < files.length; i ++ )
            {
                // Add the selected file to the client jars list
                ClientJarFileListModel listModel = (ClientJarFileListModel)clientJarsList.getModel();
                listModel.addJarFile( files[i].getPath() );
                
                // Default dir to the last selection
                if( i == 0 )
                    DEFAULT_CURRENT_JAR_DIR_FILE = files[i].getParentFile();
            }
        } 
        
        // Enable the remove button if there are jars in the list
        if( clientJarsList.getModel().getSize() > 0 )
            removeClientJarButton.setEnabled( true );
        else
            removeClientJarButton.setEnabled( false );
    }//GEN-LAST:event_addClientJarButtonActionPerformed

    private void ddLocButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ddLocButtonActionPerformed
        // Pop up FileChooser to let the user select a .ear or .jar file where
        // the EJB deployment descriptors are contained
        
        JFileChooser ddFileChooser = new JFileChooser();
        ddFileChooser.setMultiSelectionEnabled( false );
        ddFileChooser.setFileFilter( new JarFileFilter( false ) );
        ddFileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        
        // TODO what should the defualt current directory be?
        
        // Set the current directory
        // If the user has a client file specified already, then we'll
        // start from the that directory. Otherwise, use the default 
        // directory
        File curDir = null;
        if( getDDLocationFile() != null && getDDLocationFile().length() != 0 )
            curDir = new File( getDDLocationFile() );
        
        if( curDir == null )
            curDir = DEFAULT_CURRENT_JAR_DIR_FILE;
        
        ddFileChooser.setCurrentDirectory( curDir );
        
        int returnVal = ddFileChooser.showOpenDialog( this );
        
        if( returnVal == JFileChooser.APPROVE_OPTION ) 
        {
            File file = ddFileChooser.getSelectedFile();
            ddLocTextField.setText( file.getAbsolutePath() );
            
            // Default dir to the last selection
            DEFAULT_CURRENT_JAR_DIR_FILE = file.getParentFile();
        } 
    }//GEN-LAST:event_ddLocButtonActionPerformed

    private void ddLocTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ddLocTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ddLocTextFieldActionPerformed

    private void iiopPortTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iiopPortTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_iiopPortTextFieldActionPerformed

    private void serverHostTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverHostTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_serverHostTextFieldActionPerformed

    private void containerTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_containerTypeComboActionPerformed
        // Display different default port for different app server
        JComboBox cb = (JComboBox)evt.getSource();
        String serverType = (String)cb.getSelectedItem();
        int defaultPort = EjbContainerVendor.getDefaultPort( serverType );
        iiopPortTextField.setText( Integer.toString(defaultPort ) );
        
    }//GEN-LAST:event_containerTypeComboActionPerformed

    private void groupNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_groupNameTextFieldActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClientJarButton;
    private javax.swing.JPanel cleintJarButtonPanel;
    private javax.swing.JScrollPane clientJarScrollPane;
    private javax.swing.JLabel clientJarsLabel;
    private javax.swing.JList clientJarsList;
    private javax.swing.JComboBox containerTypeCombo;
    private javax.swing.JLabel containerTypeLabel;
    private javax.swing.JButton ddLocButton;
    private javax.swing.JLabel ddLocLabel1;
    private javax.swing.JLabel ddLocLabel2;
    private javax.swing.JTextField ddLocTextField;
    private javax.swing.JLabel groupNameLabel;
    private javax.swing.JTextField groupNameTextField;
    private javax.swing.JLabel iiopPortLabel;
    private javax.swing.JTextField iiopPortTextField;
    private javax.swing.JButton removeClientJarButton;
    private javax.swing.JLabel serverHostLabel;
    private javax.swing.JTextField serverHostTextField;
    // End of variables declaration//GEN-END:variables
    
}

/**
 * Filter file selection so only .jar files are shown
 */
class JarFileFilter extends FileFilter {

        public final static String JAR_EXT = "jar"; // NOI18N
        public final static String EAR_EXT = "ear"; // NOI18N
        
        private boolean jarOnly = true;
        
        public JarFileFilter( boolean jarOnly )
        {
            this.jarOnly = jarOnly;
        }

        /** Allow directories and jar files
         */
        public boolean accept( File file ) 
        {
            if( file.isDirectory() ) 
            {
                return true;
            }

            String extension = getExtension( file );
            if (extension != null) {
                if( jarOnly && extension.equalsIgnoreCase( JAR_EXT ) ) 
                {
                    return true;
                } 
                else if( !jarOnly && (extension.equalsIgnoreCase( JAR_EXT ) || extension.equalsIgnoreCase( EAR_EXT ) ) )
                {
                    return true;
                }
                else
                    return false;
            }

            return false;
        }

        public String getExtension( File file ) 
        {
            String ext = null;
            String fileName = file.getName();
            int index = fileName.lastIndexOf( '.' );

            if( index > 0 &&  index < fileName.length() - 1 ) 
            {
                ext = fileName.substring(index+1).toLowerCase();
            }
            return ext;
        }

        /** The description of this filter */
        public String getDescription() {
            if( jarOnly )
                return "JAR Files (.jar)";
            else
                return "EAR or JAR Files (.ear or .jar)";
        }
    }
