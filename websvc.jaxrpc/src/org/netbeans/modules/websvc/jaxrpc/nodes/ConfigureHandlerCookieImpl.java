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

package org.netbeans.modules.websvc.jaxrpc.nodes;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.core.webservices.action.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.core.webservices.ui.panels.MessageHandlerPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class ConfigureHandlerCookieImpl implements ConfigureHandlerCookie {
    private String serviceName;
    private Project project;
    private FileObject srcRoot;
    private WebServicesClientSupport clientSupport;
    
    public ConfigureHandlerCookieImpl(String serviceName, Project project, WebServicesClientSupport clientSupport, FileObject srcRoot) {
        this.serviceName = serviceName;
        this.project = project;
        this.clientSupport = clientSupport;
        this.srcRoot = srcRoot;
    }
    
    
    
    public void configureHandler() {
      try{  
        final FileObject ddFO = clientSupport.getDeploymentDescriptor();
        if(ddFO == null) return;
        final RootInterface rootDD = DDProvider.getDefault().getDDRoot(ddFO);
        String ddServiceName = "service/" + serviceName;
        final ServiceRef serviceRef = (ServiceRef) rootDD.findBeanByName("ServiceRef", "ServiceRefName", ddServiceName); // NOI18N
        if (serviceRef==null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ConfigureHandlerCookieImpl.class, "ERR_NotJSR109Client"),NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        ServiceRefHandler[] handlers = serviceRef.getHandler();
        ArrayList handlerList = new ArrayList();
        for(int j = 0; j < handlers.length; j++) {
            handlerList.add(handlers[j].getHandlerClass());
        }
        
        final MessageHandlerPanel panel = new MessageHandlerPanel(project,
        (String[])handlerList.toArray(new String[handlerList.size()]), false, serviceName);
        String title = NbBundle.getMessage(ConfigureHandlerCookieImpl.class,"TTL_MessageHandlerPanel");
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title, true,
        new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                if(evt.getSource() == NotifyDescriptor.OK_OPTION) {
                    DefaultListModel listModel = panel.getListModel();
                    //DefaultTableModel tblModel = panel.getTableModel();
                    try{
                        //add new handlers
                        for (int i = 0 ; i < listModel.getSize(); i++){
                            String className = (String)listModel.getElementAt(i);
                            if(isNewHandler(className, serviceRef)){
                                ServiceRefHandler handler = (ServiceRefHandler)rootDD.createBean("ServiceRefHandler");
                                handler.setHandlerName(className);
                                handler.setHandlerClass(className);
                                serviceRef.addHandler(handler);
                            }
                        }
                        //remove handlers that have been deleted
                        ServiceRefHandler[] handlers = serviceRef.getHandler();
                        for(int j = 0; j < handlers.length; j++){
                            ServiceRefHandler handler = handlers[j];
                            String clsName = handler.getHandlerClass();
                            if(!isInModel(clsName, listModel)){
                                serviceRef.removeHandler(handler);
                            }
                        }
                        rootDD.write(ddFO);
                    }catch(ClassNotFoundException e){
                        throw new RuntimeException(e.getMessage());
                    }
                    catch(IOException e){
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.setVisible(true);
      }catch(IOException e){
           throw new RuntimeException(e.getMessage());
      }
        
    }
    
    private boolean isNewHandler(String className, ServiceRef serviceRef){
        ServiceRefHandler[] handlers = serviceRef.getHandler();
        for(int i = 0; i < handlers.length; i++){
            if(handlers[i].getHandlerClass().equals(className)){
                return false;
            }
        }
        return true;
    }
    
    private boolean isInModel(String className, ListModel model){
        for(int i = 0; i < model.getSize(); i++){
            String cls = (String)model.getElementAt(i);
            if(className.equals(cls)){
                return true;
            }
        }
        return false;
    }
    
}
