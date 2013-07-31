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

import java.awt.Cursor;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.websvc.core.webservices.ui.DeleteWsDialog;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.actions.*;
import org.openide.util.HelpCtx;
import javax.swing.Action;
import java.awt.Image;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.websvc.jaxrpc.actions.AddOperationAction;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.openide.filesystems.FileLock;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.jaxrpc.actions.WSRegisterCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.core.webservices.ui.panels.EnterWSDLUrlPanel;
import org.openide.DialogDescriptor;
import org.openide.util.Lookup;
import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
import java.awt.Dialog;
import java.awt.datatransfer.Transferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.websvc.spi.support.ConfigureHandlerAction;
import org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.spi.support.MessageHandlerPanel;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.ListModel;
import javax.swing.table.TableModel;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.websvc.core.WebServiceReference;
import org.netbeans.modules.websvc.core.WebServiceTransferable;
import org.netbeans.modules.websvc.core.WsWsdlCookie;
import org.netbeans.modules.websvc.jaxrpc.actions.RegenerateFromWsdlAction;
import org.netbeans.modules.websvc.jaxrpc.actions.RegenerateFromWsdlCookie;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

public class WebServiceNode extends AbstractNode implements WSRegisterCookie, WsWsdlCookie,
        ConfigureHandlerCookie, OpenCookie, RegenerateFromWsdlCookie{
    Webservices webServices;
    WebserviceDescription webServiceDescription;
    FileObject srcRoot;
    FileObject implClass;
    WebServicesSupport wsSupport;
    String wsName;
    Project project;
    
    public WebServiceNode(Webservices webServices, WebserviceDescription webServiceDescription, FileObject srcRoot, FileObject implBean) {
        this(new InstanceContent(), webServices, webServiceDescription, srcRoot, implBean);
    }
    
    private WebServiceNode(InstanceContent content, Webservices webServices, WebserviceDescription webServiceDescription, FileObject srcRoot, FileObject implClass) {
        super(new WebServiceChildren(webServiceDescription, srcRoot, implClass), new AbstractLookup(content));
        this.webServices = webServices;
        this.webServiceDescription = webServiceDescription;
        this.srcRoot = srcRoot;
        this.implClass=implClass;
        this.wsSupport = WebServicesSupport.getWebServicesSupport(srcRoot);
        project = FileOwnerQuery.getOwner(srcRoot);
        wsName = webServiceDescription.getWebserviceDescriptionName();
        setDisplayName(wsName);
        setName(wsName);
        content.add(this);
        content.add(implClass);
        setValue("wsdl-url",getWsdlURL());
    }
    
    public Image getIcon(int type){
        return ImageUtilities.loadImage("org/netbeans/modules/websvc/core/webservices/ui/resources/webservice.png");
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public void open() {
        OpenCookie oc = getOpenCookie();
        if (oc != null) {
            oc.open();
        }
    }
    
    public WebServicesSupport getWebServicesSupport(){
        return wsSupport;
    }
    
    private OpenCookie getOpenCookie() {
        OpenCookie oc = null;
        if (implClass != null) {
            try {
                DataObject d = DataObject.find(implClass);
                oc = (OpenCookie)d.getCookie(OpenCookie.class);
            } catch (DataObjectNotFoundException de) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
        }
        return oc;
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    public Node.Cookie getCookie(Class type){
        // if(type == EditWSAttributesCookie.class){
        //     return new EditWSAttributesCookieImpl(this, null);
        //}
        return super.getCookie(type);
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        ArrayList<Action> actions = new ArrayList<Action>(Arrays.asList(
            SystemAction.get(OpenAction.class),
            null,
            SystemAction.get(AddOperationAction.class),
            null,
            SystemAction.get(RegenerateFromWsdlAction.class),
            null,
            SystemAction.get(ConfigureHandlerAction.class),
            //null,
            //SystemAction.get(WSEditAttributesAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            null,
            SystemAction.get(PropertiesAction.class)));
        addFromLayers(actions, "WebServices/Services/Actions");
        return actions.toArray(new Action[actions.size()]);
    }
    
    private void addFromLayers(ArrayList<Action> actions, String path) {
        Lookup look = Lookups.forPath(path);
        for (Object next : look.lookupAll(Object.class)) {
            if (next instanceof Action) {
                actions.add((Action) next);
            } else if (next instanceof javax.swing.JSeparator) {
                actions.add(null);
            }
        }
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // Handle deleting:
    public boolean canDestroy() {
        return true;
    }
    
    private void deleteFile(FileObject f) {
        FileLock lock = null;
        try {
            lock = f.lock();
            if (f.isFolder()) {
                DataFolder folder = DataFolder.findFolder(f);
                // save all opened files
                if (folder!=null) {
                    DataObject[] children = folder.getChildren();
                    for (int i=0;i<children.length;i++) {
                        SaveCookie save = (SaveCookie)children[i].getCookie(SaveCookie.class);
                        if (save!=null) save.save();
                    }
                }
            }
            f.delete(lock);
        } catch(java.io.IOException e) {
            NotifyDescriptor ndd =
                    new NotifyDescriptor.Message(NbBundle.getMessage(this.getClass(), "MSG_Unable_Delete_File", f.getNameExt()),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        } finally {
            if(lock != null) {
                lock.releaseLock();
            }
        }
    }
    
    private void deleteConfigFile() {
        ClassPathProvider cpp = (ClassPathProvider)project.getLookup().lookup(ClassPathProvider.class);
        assert cpp != null;
        ClassPath classPath = cpp.findClassPath(srcRoot, ClassPath.SOURCE);
        String configFileName = getPackageName()+ "/" + wsName + "-config" + ".xml";
        FileObject configFO = classPath.findResource(configFileName);
        if(configFO != null) {
            deleteFile(configFO);
        }
    }
    
    private void deleteDDFile() {
        FileObject wsdd = wsSupport.getWebservicesDD();
        if(wsdd != null) {
            deleteFile(wsdd);
        }
    }
    
    public void destroy() throws java.io.IOException {
        super.destroy();
        String wsdlName = getWsdlName();
        FileObject wsdlFile = getWsdlFile(wsdlName);
        FileObject mappingFile = getMappingFile(wsdlName);
        String packageName=getPackageName();
        String deleteOptions = DeleteWsDialog.open(getDisplayName(),packageName.replace('/','.'),(wsdlFile==null?null:wsdlName));
        if (!deleteOptions.equals(DeleteWsDialog.DELETE_NOTHING)) {
            boolean deletePackage=false;
            boolean deleteWsdl=false;
            if (deleteOptions.equals(DeleteWsDialog.DELETE_ALL)) {
                deletePackage=true;
                deleteWsdl=true;
            } else if (deleteOptions.equals(DeleteWsDialog.DELETE_PACKAGE)) deletePackage=true;
            else if (deleteOptions.equals(DeleteWsDialog.DELETE_WSDL)) deleteWsdl=true;
            //delete the config file
            deleteConfigFile();
            //remove entry from webservices.xml
            webServices.removeWebserviceDescription(webServiceDescription);
            //need to write everytime to remove the node
            webServices.write(wsSupport.getWebservicesDD());
            //remove entry in module DD and project files
            wsSupport.removeServiceEntry(getLinkName());
            wsSupport.removeProjectEntries(wsName);
            //if there are no more web services, delete webservices.xml
            if(webServices.sizeWebserviceDescription() == 0) {
                deleteDDFile();
            }
            if (deletePackage) { // remove the package where WS was generated
                FileObject wsPackage = srcRoot.getFileObject(packageName);
                if (wsPackage!=null) {
                    FileObject parent = wsPackage.getParent();
                    deleteFile(wsPackage);
                    //remove also the empty packages upwards
                    while (parent!=srcRoot && parent.getChildren().length==0) {
                        FileObject fileToDelete=parent;
                        parent = parent.getParent();
                        deleteFile(fileToDelete);
                    }
                }
            }
            if (deleteWsdl) {
                if (wsdlFile!=null) {
                    deleteFile(wsdlFile);
                }
                if (mappingFile!=null) {
                    deleteFile(mappingFile);
                }
            }
        }
        
    }
    
    private String getLinkName() {
        PortComponent portComponent = webServiceDescription.getPortComponent(0); //assume one port per ws
        ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
        String link =serviceImplBean.getServletLink();
        if(link == null) {
            link = serviceImplBean.getEjbLink();
        }
        return link;
    }
    
    //Need a better way to get the package, maybe pass it(???)
    private String getPackageName() {
        String implClassName = FileUtil.getRelativePath(srcRoot,implClass);
        int index = implClassName.lastIndexOf("/");
        return implClassName.substring(0, index);
    }
        
    private String getDefaultWSDLUrl(){
        J2eeModuleProvider provider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties instanceProperties = provider.getInstanceProperties();
        String portNumber = instanceProperties.getProperty(InstanceProperties.HTTP_PORT_NUMBER);
        if(portNumber == null || portNumber.equals("")) {
            portNumber = "8080";
        }
        
        // getting hostName
        String serverUrl = instanceProperties.getProperty(InstanceProperties.URL_ATTR);
        String hostName="localhost"; //NOI18N
        if (serverUrl!=null && serverUrl.indexOf("::")>0) { //NOI18N
            int index1 = serverUrl.indexOf("::"); //NOI18N
            int index2 = serverUrl.lastIndexOf(":"); //NOI18N
            if (index2>index1+2) hostName = serverUrl.substring(index1+2,index2);
        }
        
        String contextRoot = "webservice";//NO18N
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        String wsURI = wsName;
        if(J2eeModule.Type.WAR.equals(moduleType)) {
            J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
            WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(srcRoot);
            FileObject ddFolder = wsSupport.getWsDDFolder();
            if (ddFolder!=null) {
                FileObject webXmlFo = ddFolder.getFileObject("web.xml"); //NOI18N
                if (webXmlFo!=null) {
                    wsURI = findUriForWS(webXmlFo,wsName);
                    if (wsURI.startsWith("/")) wsURI=wsURI.substring(1); //NOI18N
                }
                
            }
            
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                // TODO context path could not be read, the user should be notified about it
            }
            if(contextRoot != null && contextRoot.startsWith("/")){
                contextRoot = contextRoot.substring(1);
            }
        } else if(J2eeModule.Type.EJB.equals(moduleType)) {
            contextRoot = "";//NO18N for now, we need to find the real value (see bug...57034 and 52265)
        }
        
        return "http://"+hostName+":" + portNumber +"/" + (contextRoot != null && !contextRoot.equals("") ? contextRoot + "/" : "") + wsURI + "?WSDL";
    }
    
    private String findUriForWS(FileObject webXmlFo, String wsName) {
        try {
            WebApp webApp = org.netbeans.modules.j2ee.dd.api.web.DDProvider.getDefault().getDDRoot(webXmlFo);
            if (webApp!=null) {
                ServletMapping[] maps = webApp.getServletMapping();
                // servletName = "WSServlet_"+wsName
                String servletName = WebServicesConstants.WebServiceServlet_PREFIX+wsName;
                for (int i=0;i<maps.length;i++) {
                    if (servletName.equals(maps[i].getServletName())) {
                        return ((ServletMapping25)maps[i]).getUrlPatterns()[0];
                    }
                }
            }
        } catch (IOException ex) {}
        return wsName;
    }
    
    private String getWsdlName() {
        String wsdlFile = webServiceDescription.getWsdlFile();
        if (wsdlFile!=null) {
            int ind = wsdlFile.lastIndexOf("/"); //NOI10N
            if (ind>=0) return wsdlFile.substring(ind+1);
        }
        return wsdlFile;
    }
    
    private FileObject getWsdlFile(String wsdlName) {
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(srcRoot);
        FileObject wsdlFolder = wsSupport.getWsDDFolder().getFileObject("wsdl");//NOI18N
        FileObject wsdlFO=null;
        if (wsdlFolder!=null) {
            if (wsdlName!=null) {
                wsdlFO=wsdlFolder.getFileObject(wsdlName);
            }
        }
        return wsdlFO;
    }
    
    private FileObject getMappingFile(String wsdlName) {
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(srcRoot);
        FileObject ddFolder = wsSupport.getWsDDFolder();
        FileObject mappingFO=null;
        if (ddFolder!=null) {
            if (wsdlName!=null) {
                String wsdlShortName = wsdlName;
                if (wsdlName.endsWith(".wsdl")) wsdlShortName = wsdlName.substring(0,wsdlName.length()-5);
                mappingFO=ddFolder.getFileObject(wsdlShortName+"-mapping.xml"); //NOI18N
            }
        }
        return mappingFO;
    }
    
    /*
     * Add the web service to the web service registry
     */
    public void registerWebService() {
        final EnterWSDLUrlPanel panel = new EnterWSDLUrlPanel(getDefaultWSDLUrl());
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, NbBundle.getMessage(WebServiceNode.class, "Enter_WSDL_Url"), true,
                new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                if(evt.getSource() == NotifyDescriptor.OK_OPTION) {
                    
                    RequestProcessor.getDefault().post(new Runnable(){
                        public void run(){
                            URL url = null;
                            try{
                                url  = new URL(panel.getSelectedWSDLUrl());
                            }catch(Exception e){
                                throw new RuntimeException(e.getMessage());
                            }
                            WebServicesRegistryView registryView = (WebServicesRegistryView)Lookup.getDefault().
                                    lookup(WebServicesRegistryView.class);
                            TopComponent currentComponent = TopComponent.getRegistry().getActivated();
                            currentComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            try{
                                boolean success = registryView.registerService(url, true);
                                if(!success) {
                                    NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(WebServiceNode.class,
                                            "MSG_UNABLE_TO_REGISTER_WS"), NotifyDescriptor.ERROR_MESSAGE);
                                    DialogDisplayer.getDefault().notify(d);
                                }
                            }finally{
                                currentComponent.setCursor(null);
                            }
                        }
                    });
                }
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WebServiceNode.class, "Enter_WSDL_Url_Desc"));
        dialog.setVisible(true);
    }
    
    private boolean isNewHandler(String className, PortComponent portComponent){
        PortComponentHandler[] handlers = portComponent.getHandler();
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
    public void configureHandler() {
        final PortComponent portComponent = webServiceDescription.getPortComponent(0);
        PortComponentHandler[] handlers = portComponent.getHandler();
        ArrayList handlerList = new ArrayList();
        for(int j = 0; j < handlers.length; j++) {
            handlerList.add(handlers[j].getHandlerClass());
        }
        final MessageHandlerPanel panel = new MessageHandlerPanel(project, handlerList, false, wsName);
        String title = NbBundle.getMessage(WebServiceNode.class,"TTL_MessageHandlerPanel");
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title, true,
                new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                if(evt.getSource() == NotifyDescriptor.OK_OPTION) {
                    
                    if(!panel.isChanged()) return;
                    
                    //refresh handlers
                    PortComponentHandler[] handlers = portComponent.getHandler();
                    for(int j = 0; j < handlers.length; j++){
                        PortComponentHandler handler = handlers[j];
                        String clsName = handler.getHandlerClass();
                        portComponent.removeHandler(handler);
                    }
                    TableModel tableModel = panel.getHandlerTableModel();
                    try{
                        //add handlers
                        for(int i = 0; i < tableModel.getRowCount(); i++){
                            String className = (String)tableModel.getValueAt(i, 0);
                            PortComponentHandler handler = (PortComponentHandler)webServices.createBean("PortComponentHandler");
                            handler.setHandlerName(className);
                            handler.setHandlerClass(className);
                            portComponent.addHandler(handler);
                        }
                        
                        webServices.write(wsSupport.getWebservicesDD());
                        //configuration.write(configFO);
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
    }
    
    /**
     * Adds possibility to display custom delete dialog
     */
    public Object getValue(String attributeName) {
        Object retValue;
        if (attributeName.equals("customDelete")) { //NOI18N
            retValue = Boolean.TRUE;
        } else {
            retValue = super.getValue(attributeName);
        }
        return retValue;
    }
    
    public String getWsdlURL() {
        return getDefaultWSDLUrl();
    }
    
    public void regenerate() {
        NotifyDescriptor.Confirmation notifyDesc =
                new NotifyDescriptor.Confirmation(NbBundle.getMessage(WebServiceNode.class, "MSG_CONFIRM_REFRESH_IMPL" ),
                NotifyDescriptor.YES_NO_OPTION);
        DialogDisplayer.getDefault().notify(notifyDesc);
        if(notifyDesc.getValue() == NotifyDescriptor.NO_OPTION) {
            return;
        }
        final ProgressHandle handle = ProgressHandleFactory.createHandle
                ( NbBundle.getMessage(WebServiceNode.class, "TXT_Regenerating"));
        handle.start(100);
        handle.switchToIndeterminate();
        Runnable r = new Runnable(){
            public void run(){
                try{
                    runWscompileTarget();
                }finally{
                    handle.finish();
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }
    
    public Transferable clipboardCopy() throws IOException {
        URL url = new URL(getWsdlURL());
        boolean connectionOK=false;
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection)connection;
                try {
                    httpConnection.setRequestMethod("GET"); //NOI18N
                    httpConnection.connect();
                    if (HttpURLConnection.HTTP_OK == httpConnection.getResponseCode())
                        connectionOK=true;
                } catch (java.net.ConnectException ex) {
                    //TODO: throw exception here?
                    url = null;
                } finally {
                    if (httpConnection!=null)
                        httpConnection.disconnect();
                }
                if(!connectionOK){
                    //TODO: throw exception here?
                    url = null;
                }
            }
        } catch (IOException ex) {
            //TODO: throw exception here?
            url = null;
        }
        return new WebServiceTransferable(new WebServiceReference(url , wsName, project.getProjectDirectory().getName()));
    }
    
    private void runWscompileTarget(){
        FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        try {
            ExecutorTask wscompileTask =
                    ActionUtils.runTarget(buildImplFo,new String[]{wsName + "_wscompile"},null); //NOI18N
            wscompileTask.waitFinished();
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ex.getLocalizedMessage());
        } catch (IllegalArgumentException ex) {
            ErrorManager.getDefault().log(ex.getLocalizedMessage());
            
        }
    }
}
