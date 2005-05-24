/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

import java.awt.Insets;
import java.awt.Dialog;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import java.text.MessageFormat;

import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;

import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

public class ListServerInstances extends JPanel implements WizardConstants{
                
    final static java.util.ResourceBundle bundle = NbBundle.getBundle(ListServerInstances.class);
    
    Component initialFocusOwner = null;
    String title;
    
    Hashtable servers = new Hashtable();
    
    public ListServerInstances(String _title, Property[] props, String resType, String resFileName, InstanceProperties targetName) {
        title = _title;
        initComponents(props, resType, resFileName, targetName);
//        HelpCtx.setHelpIDString(this, "S1_register.html");//NOI18N
    }
    
    public ListServerInstances(String _title, Property[] props, String resType, String resFileName) {
        title = _title;
        initComponents(props, resType, resFileName, null);
//        HelpCtx.setHelpIDString(this, "S1_register.html");//NOI18N
    }
    
    private void initComponents(final Property[] props, final String resType, String resFileName, InstanceProperties targetName) {
        Vector names = new Vector();
        if(targetName == null){
            showInvalidServerError();
            return;
        }  
        try{
            SunDeploymentManagerInterface dm = (SunDeploymentManagerInterface)targetName.getDeploymentManager();
            String servName = dm.getHost() + ":" + dm.getPort();  //NOI18N
            names.addElement(servName);
            servers.put(servName, dm);
        }catch(java.lang.ClassCastException cce){
            showInvalidServerError();
            return;
        }        

        JPanel p = new JPanel();
        
        JLabel nameLabel = new JLabel(bundle.getString("LBL_resource_name"));  //NOI18N
        //FIXME:this should show the name of the resource which may not be same as the name of the node
        //get property from node 
        JLabel rsNameLabel = new JLabel(resFileName);
        
        //Now displaying only default server instance
        //serverListCB = new JComboBox(names);
        serverListCB = new JTextField();
        serverListCB.setText(names.get(0).toString());
        
        serverListCB.setEditable(false);
        serverListCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_select_server"));  //NOI18N
        serverListCB.getAccessibleContext().setAccessibleDescription(bundle.getString( "LBL_select_server"));  //NOI18N
        
        JLabel selectLabel = new JLabel(bundle.getString("LBL_select_server"));  //NOI18N
        selectLabel.setDisplayedMnemonic(bundle.getString("LBL_select_server_Mnemonic").charAt(0));  //NOI18N
        selectLabel.setLabelFor(serverListCB);
        
        msgArea = new JTextArea(2, 1);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setEditable(false);
        msgArea.setBackground(p.getBackground());
        
        applyButton = new JButton(bundle.getString("LBL_Register"));  //NOI18N
        applyButton.setMnemonic(bundle.getString( "LBL_Register_Mnemonic").charAt(0));  //NOI18N
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButton.setEnabled(false);
                doRegistration(props, resType);
                applyButton.setEnabled(true);
            }
        });
        
        closeButton = new JButton(bundle.getString("LBL_Close"));  //NOI18N
        closeButton.setMnemonic(bundle.getString( "LBL_Close_Mnemonic").charAt(0));  //NOI18N
        
        p.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc.fill = gbc1.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth =  1;
        gbc.gridheight =  1;
        
        
        gbc1.anchor = GridBagConstraints.EAST;
        gbc1.gridwidth = GridBagConstraints.REMAINDER;
        
        gbc.insets = new Insets(12, 12, 10, 0);
        gbc1.insets = new Insets(12, 12, 10, 12);
        
        p.add(nameLabel, gbc);
        p.add(rsNameLabel, gbc1);
        
        gbc.insets = new Insets(0, 12, 10, 0);
        gbc1.insets = new Insets(0, 12, 10, 12);
        
        p.add(selectLabel, gbc);
        p.add(serverListCB, gbc1);
        
        
        gbc1.gridheight=2;
        gbc1.weighty = 0.5;
        p.add(new JLabel(""), gbc1); //dummy  //NOI18N
        
        gbc.insets = new Insets(0, 12, 10, 12);
        gbc.gridwidth =4;
        gbc.weightx = 0.5;
        p.add(new JScrollPane(msgArea), gbc);
        
        this.setLayout(new BorderLayout());
        this.add(p, "Center");  //NOI18N
        
//        HelpCtx.setHelpIDString(this, "S1_register.html");//NOI18N
        
        Object options[] = {applyButton, closeButton};
        
        DialogDescriptor dd = new DialogDescriptor(this, title, true, options, applyButton, DialogDescriptor.BOTTOM_ALIGN, HelpCtx.findHelp(this), null);
        
        dd.setClosingOptions(new Object[] {closeButton});
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setLocation(350, 350);
        
        dialog.pack();
        dialog.setVisible(true);
        
        dialog.getAccessibleContext().setAccessibleName(bundle.getString("DSC_ListServers"));//NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_ListServers"));//NOI18N
        
    }
    
    
    //Handles case when Register is called from xml file. No checks for registered CP or JDBC
    public void doRegistration(Property[] props, String resType) {
        //String serverNm = (String) serverListCB.getSelectedItem();
        String serverNm = (String) serverListCB.getText();
        try{
            msgArea.setText(bundle.getString("Msg_RegDS")); //NOI18N
            setTopManagerStatus(bundle.getString( "Msg_RegDS"));//NOI18N
            if(resType.equals(__JdbcConnectionPool)){
                registerConnectionPool(props, resType, serverNm);
            }else if(resType.equals(__JdbcResource)){
                registerDataSource(props, resType, serverNm);
            }else if(resType.equals(__PersistenceManagerFactoryResource)){
                registerPersistenceManager(props, resType, serverNm);
            }else if(resType.equals(__MailResource)){
                registerMailSession(props, resType, serverNm);
            }else if(resType.equals(__JmsResource)){
                registerJMS(props, resType, serverNm);
            }
            setTopManagerStatus(bundle.getString( "Msg_RegDone"));//NOI18N
            msgArea.setText(bundle.getString( "Msg_RegDone")); //NOI18N
        }catch(Exception ex){
            String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
            msgArea.setText(errorMsg);
            setTopManagerStatus(errorMsg);
        }
    }
    
    public void registerConnectionPool(Property[] props, String resType, String serverName) throws Exception{
        //FIXME: last param needs to be target ?? servername??
        Object[] params = new Object[]{ResourceUtils.getAttributes(props, __JdbcConnectionPool), ResourceUtils.getProperties(props), null};
        String[] signature = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};  //NOI18N
        String operName = NbBundle.getMessage(ListServerInstances.class, "CreateCP"); //NOI18N
        
        createResource(operName, params, signature, getManagementObject(serverName));
    }
    
    public void registerDataSource(Property[] props, String resType, String serverName) throws Exception{
        //FIXME: last param needs to be target ?? servername??
        Object[] params = new Object[]{ResourceUtils.getAttributes(props, __JdbcResource), ResourceUtils.getProperties(props), null};
        String[] signature = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};  //NOI18N
        String operName = NbBundle.getMessage(ListServerInstances.class, "CreateDS"); //NOI18N
        
        createResource(operName, params, signature, getManagementObject(serverName));
    }
    
    public void registerPersistenceManager(Property[] props, String resType, String serverName) throws Exception{
        //FIXME: last param needs to be target ?? servername??
        Object[] params = new Object[]{ResourceUtils.getAttributes(props, __PersistenceManagerFactoryResource), ResourceUtils.getProperties(props), null};
        String[] signature = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};  //NOI18N
        String operName = NbBundle.getMessage(ListServerInstances.class, "CreatePMF"); //NOI18N
        
        createResource(operName, params, signature, getManagementObject(serverName));
    }
    
    public void registerMailSession(Property[] props, String resType, String serverName) throws Exception{
        //FIXME: last param needs to be target ?? servername??
        Object[] params = new Object[]{ResourceUtils.getAttributes(props, __MailResource), ResourceUtils.getProperties(props), null};
        String[] signature = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};  //NOI18N
        String operName = NbBundle.getMessage(ListServerInstances.class, "CreateMail"); //NOI18N
        
        createResource(operName, params, signature, getManagementObject(serverName));
    }
    
    public void registerJMS(Property[] props, String resType, String serverName) throws Exception{
        //FIXME: last param needs to be target ?? servername??
        Object[] params = new Object[]{ResourceUtils.getAttributes(props, __JmsResource), ResourceUtils.getProperties(props), null};
        String[] signature = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};  //NOI18N
        String operName = NbBundle.getMessage(ListServerInstances.class, "CreateJMS"); //NOI18N
        
        createResource(operName, params, signature, getManagementObject(serverName));
    }
    
    private void setTopManagerStatus(String msg){
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);
    }
    
    private ServerInterface getManagementObject(String serverName){
        SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface)servers.get(serverName);
        ServerInterface mejb = eightDM.getManagement(); 
        return mejb;
    }
    
     static final String MAP_RESOURCES = "com.sun.appserv:type=resources,category=config";//NOI18N
   private void createResource(String operName, Object[] params, String[] signature, ServerInterface mejb) throws Exception{
        try{
            ObjectName objName = new ObjectName(MAP_RESOURCES);
            mejb.invoke(objName, operName, params, signature);
        }catch(Exception ex){
            throw new Exception(ex.getLocalizedMessage());
        }
    }
    
    private void showInvalidServerError(){
        NotifyDescriptor d = new NotifyDescriptor.Message(bundle.getString("Msg_invalid_server"), NotifyDescriptor.INFORMATION_MESSAGE);
        d.setTitle(bundle.getString("Title_invalid_server"));
        DialogDisplayer.getDefault().notify(d);
        setTopManagerStatus(bundle.getString("Msg_invalid_server"));//NOI18N
    }
    
    private JButton applyButton;
    private JButton closeButton;
    private JTextArea msgArea;
    //private JComboBox serverListCB ;
    private JTextField serverListCB ;
}

