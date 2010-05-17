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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.jaxrpc.nodes;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.table.TableModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.spi.support.MessageHandlerPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
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
                    handlerList, false, serviceName);
            String title = NbBundle.getMessage(ConfigureHandlerCookieImpl.class,"TTL_MessageHandlerPanel");
            DialogDescriptor dialogDesc = new DialogDescriptor(panel, title, true,
                    new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    if(evt.getSource() == NotifyDescriptor.OK_OPTION) {
                        
                        if(!panel.isChanged()) return;
                        
                        try{
                            //refresh handlers
                            ServiceRefHandler[] handlers = serviceRef.getHandler();
                            for(int j = 0; j < handlers.length; j++){
                                ServiceRefHandler handler = handlers[j];
                                String clsName = handler.getHandlerClass();
                                serviceRef.removeHandler(handler);
                            }
                            TableModel tableModel = panel.getHandlerTableModel();
                            
                            //add  handlers
                            for (int i = 0 ; i < tableModel.getRowCount(); i++){
                                String className = (String)tableModel.getValueAt(i, 0);
                                ServiceRefHandler handler = (ServiceRefHandler)rootDD.createBean("ServiceRefHandler");
                                handler.setHandlerName(className);
                                handler.setHandlerClass(className);
                                serviceRef.addHandler(handler);
                            }
                            rootDD.write(ddFO);
                        }catch(ClassNotFoundException e){
                            ErrorManager.getDefault().notify(e);
                        } catch(IOException e){
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                }
            });
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
            dialog.getAccessibleContext().setAccessibleDescription(dialog.getTitle());
            dialog.setVisible(true);
        }catch(IOException e){
            ErrorManager.getDefault().notify(e);
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
