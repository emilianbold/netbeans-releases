package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
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
import java.util.Vector;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import javax.management.ObjectName;

import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.BaseResourceNode;
import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject;

public class ListServerInstances extends JPanel implements WizardConstants{
                
    final static java.util.ResourceBundle bundle = NbBundle.getBundle(ListServerInstances.class);
    
    Component initialFocusOwner = null;
    String title;
    Hashtable servers = new Hashtable();
            
    public ListServerInstances(String _title, SunResourceDataObject dObj, String type, InstanceProperties targetName) {
        title = _title;
        initComponents(dObj, type, targetName); 
        //HelpCtx.setHelpIDString(this, "S1_register.html");//NOI18N
    }
    
    private void initComponents(final SunResourceDataObject resourceObj, final String resType, InstanceProperties targetName) {
        Vector names = new Vector();
        if(targetName == null){
            showInvalidServerError();
            return;
        }  
        try{
            SunDeploymentManagerInterface dm = (SunDeploymentManagerInterface)targetName.getDeploymentManager();
            String servName = targetName.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);  
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
        JLabel rsNameLabel = new JLabel(resourceObj.getName());
        
        //Now displaying only default server instance
        //serverListCB = new JComboBox(names);
        serverListCB = new JTextField();
        serverListCB.setText(" " + names.get(0).toString()); //NOI18N
        
        serverListCB.setEditable(false);
        serverListCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_select_server"));  //NOI18N
        serverListCB.getAccessibleContext().setAccessibleDescription(bundle.getString( "LBL_select_server"));  //NOI18N
        
        JLabel selectLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(selectLabel,bundle.getString("LBL_select_server"));  //NOI18N
        selectLabel.setLabelFor(serverListCB);
        
        msgArea = new JTextArea(4, 1);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setEditable(false);
        msgArea.setBackground(p.getBackground());
        
        applyButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(applyButton,bundle.getString("LBL_Register"));  //NOI18N
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButton.setEnabled(false);
                doRegistration(resourceObj, resType); 
                applyButton.setEnabled(true);
            }
        });
        
        closeButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(closeButton,bundle.getString("LBL_Close"));  //NOI18N
        
        p.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc.fill = gbc1.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth =  3;
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
    public void doRegistration(SunResourceDataObject resourceObj, String resType) {
        String serverNm = ((String) serverListCB.getText()).trim();
        try{
            msgArea.setText(bundle.getString("Msg_RegDS")); //NOI18N
            setTopManagerStatus(bundle.getString( "Msg_RegDS"));//NOI18N
            Resources res = getResourceGraph(resourceObj);
            SunDeploymentManagerInterface sunDm = getSunDm(serverNm);
            if(res != null){
                ResourceUtils.register(res, sunDm, false, resType);  
                setTopManagerStatus(bundle.getString( "Msg_RegDone"));//NOI18N
                msgArea.setText(bundle.getString( "Msg_RegDone")); //NOI18N
            }
        }catch(Exception ex){
            String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
            msgArea.setText(errorMsg);
            setTopManagerStatus(errorMsg);
        }
    }
       
    private void setTopManagerStatus(String msg){
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);
    }
    
    private Resources getResourceGraph(SunResourceDataObject resourceObj){
        BaseResourceNode resNode = (BaseResourceNode)resourceObj.getNodeDelegate();
        return resNode.getBeanGraph();
    }
       
    private SunDeploymentManagerInterface getSunDm(String serverName){
        SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface)servers.get(serverName);
        return eightDM;
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

