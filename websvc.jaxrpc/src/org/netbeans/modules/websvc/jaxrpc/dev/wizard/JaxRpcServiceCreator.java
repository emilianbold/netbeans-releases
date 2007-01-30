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

package org.netbeans.modules.websvc.jaxrpc.dev.wizard;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport;
import org.netbeans.modules.websvc.core.ServiceCreator;
import org.netbeans.modules.websvc.core.dev.wizard.NewWebServiceWizardIterator;
import org.netbeans.modules.websvc.core.dev.wizard.ProjectInfo;
import org.netbeans.modules.websvc.core.dev.wizard.WizardProperties;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Liu
 */
public class JaxRpcServiceCreator implements ServiceCreator {
    
    private Project project;
    private ProjectInfo projectInfo;
    private WizardDescriptor wiz;
    private String wsName;
    private int serviceType;
    private int projectType;
    
    /** Creates a new instance of JaxRpcServiceCreator */
    public JaxRpcServiceCreator(Project project, ProjectInfo projectInfo, WizardDescriptor wiz) {
        this.project = project;
        this.projectInfo = projectInfo;
        this.wiz = wiz;
    }
    
    public void createService() throws IOException {
        System.out.println("create JaxRpc Service");
        
        serviceType = ((Integer) wiz.getProperty(WizardProperties.WEB_SERVICE_TYPE)).intValue();
        projectType = projectInfo.getProjectType();
        
        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle( NbBundle.getMessage(JaxRpcServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N
        handle.start(100);
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    generateWebService(handle);
                } catch (Exception e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if(message != null) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
        
    }
    
    public void createServiceFromWsdl() throws IOException {
        System.out.println("create JaxRpc Service from wsdl");
    }
    
    private void generateWebService(ProgressHandle handle) throws Exception {
        FileObject pkg = Templates.getTargetFolder(wiz);
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(pkg);
        assert wsSupport != null;
        
        wsName = getUniqueJaxrpcName(wsSupport, Templates.getTargetName(wiz));
        
        WebServiceGenerator generator = new WebServiceGenerator(wsSupport, wsName, pkg, project);
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_GEN_SEI_AND_IMPL"), 50); //NOI18N
        generator.generateWebService();
        
        URI targetNS = null;
        URI typeNS = null;
        try {
            targetNS = generator.getTargetNS();
            typeNS = generator.getDefaultTypeNS(wsName); //Need to get from user
        } catch(java.net.URISyntaxException e) {
            String mes = NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_INVALID_URL_SYNTAX"); //NOI18N
            throw new Exception(mes);
        }
        //Create config file
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_CREATING_WSCOMPILE_ARTIFACTS")); //NOI18N
        String servantClassName = generator.getServantClassName();
        String seiClassName = generator.getSEIClassName();
        FileObject configFile = null;
        configFile = generator.generateConfigFile(seiClassName, servantClassName, targetNS, typeNS);
        handle.progress(70);
        
        //Add web service entries to the project's property file, project file
        wsSupport.addServiceImpl(wsName, configFile, false);
        handle.progress(90);
        
        //Add web service entries to the module's DD
        wsSupport.addServiceEntriesToDD(wsName, seiClassName, servantClassName);
        
        //Add webservice entry in webservices.xml
        handle.progress(NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_ADDING_DD_ENTRIES")); //NOI18N
        String portTypeName = null;
        generator.addWebServiceEntry(seiClassName, portTypeName, targetNS);
        
        handle.finish();
    }
    
    private String getUniqueJaxrpcName(WebServicesSupport wsSupport, String origName){
        List<WsCompileEditorSupport.ServiceSettings> webServices = wsSupport.getServices();
        List<String> serviceNames = new ArrayList<String>(webServices.size());
        for(WsCompileEditorSupport.ServiceSettings service: webServices){
            serviceNames.add(service.getServiceName());
        }
        return uniqueWSName(origName, serviceNames);
    }
    
    private String uniqueWSName(final String origName, List<String> names ){
        int uniquifier = 0;
        String truename = origName;
        while(names.contains(truename)){
            truename = origName + String.valueOf(++uniquifier);
        }
        return truename;
    }
}
