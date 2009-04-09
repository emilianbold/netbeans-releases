/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.maven.jaxws.nodes;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.jaxws.MavenModelUtils;
import org.netbeans.modules.maven.jaxws.ServerType;
import org.netbeans.modules.maven.jaxws.WSStackUtils;
import org.netbeans.modules.maven.jaxws.WSUtils;
import org.netbeans.modules.maven.jaxws._RetoucheUtil;
import org.netbeans.modules.maven.jaxws.actions.AddOperationAction;
import org.netbeans.modules.maven.jaxws.actions.WSEditAttributesAction;
import org.netbeans.modules.maven.jaxws.actions.WsTesterPageAction;
import org.netbeans.modules.maven.jaxws.wseditor.EditWSAttributesCookieImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Handler;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider;
import org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.spi.support.ConfigureHandlerAction;
import org.netbeans.modules.websvc.spi.support.MessageHandlerPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

public class JaxWsNode extends AbstractNode implements ConfigureHandlerCookie {

    JaxWsService service;
    FileObject srcRoot;
    private FileObject implBeanClass;
    InstanceContent content;
    Project project;

    public JaxWsNode(JaxWsService service, FileObject srcRoot, FileObject implBeanClass) {
        this(service, srcRoot, implBeanClass, new InstanceContent());
    }

    private JaxWsNode(JaxWsService service, FileObject srcRoot, FileObject implBeanClass, InstanceContent content) {
        super(new JaxWsChildren(service, srcRoot, implBeanClass), new AbstractLookup(content));
        this.service = service;
        this.srcRoot = srcRoot;
        this.content = content;
        this.implBeanClass = implBeanClass;
        project = FileOwnerQuery.getOwner(srcRoot);
        String serviceName = service.getServiceName();
        setName(serviceName);
        content.add(this);
        content.add(service);
        content.add(implBeanClass);
        OpenCookie cookie = new OpenCookie() {

            public void open() {
                OpenCookie oc = getOpenCookie();
                if (oc != null) {
                    oc.open();
                }
            }
        };
        content.add(cookie);
        setServiceUrl();
        content.add(new EditWSAttributesCookieImpl(this));
        attachFileChangeListener();

    }

    private boolean isWebProject() {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            Object moduleType = provider.getJ2eeModule().getModuleType();
            if (J2eeModule.WAR.equals(moduleType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return service.getServiceName();
    }

    @Override
    public String getShortDescription() {
        return getWsdlURL();
    }

    private static final String WAITING_BADGE = "org/netbeans/modules/maven/jaxws/resources/waiting.png"; // NOI18N
    private static final String ERROR_BADGE = "org/netbeans/modules/maven/jaxws/resources/error-badge.gif"; //NOI18N
    private static final String SERVICE_BADGE = "org/netbeans/modules/maven/jaxws/resources/XMLServiceDataIcon.png"; //NOI18N
    private java.awt.Image cachedWaitingBadge;
    private java.awt.Image cachedErrorBadge;
    private java.awt.Image cachedServiceBadge;

    @Override
    public java.awt.Image getIcon(int type) {
        return getServiceImage();
    }

    private java.awt.Image getServiceImage() {
        if (cachedServiceBadge == null) {
            cachedServiceBadge = ImageUtilities.loadImage(SERVICE_BADGE);
        }
        return cachedServiceBadge;
    }

    private java.awt.Image getErrorBadge() {
        if (cachedErrorBadge == null) {
            cachedErrorBadge = ImageUtilities.loadImage(ERROR_BADGE);
        }
        return cachedErrorBadge;
    }

    private java.awt.Image getWaitingBadge() {
        if (cachedWaitingBadge == null) {
            cachedWaitingBadge = ImageUtilities.loadImage(WAITING_BADGE);
        }
        return cachedWaitingBadge;
    }

    void changeIcon() {
        fireIconChange();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    private DataObject getDataObject() {
        FileObject f = getImplBean();
        if (f != null) {
            try {
                return DataObject.find(f);
            } catch (DataObjectNotFoundException de) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
        }
        return null;
    }

    private OpenCookie getOpenCookie() {
        OpenCookie oc = null;
        FileObject f = getImplBean();
        if (f != null) {
            try {
                DataObject d = DataObject.find(f);
                oc = d.getCookie(OpenCookie.class);
            } catch (DataObjectNotFoundException de) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
        }
        return oc;
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }

    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        DataObject dobj = getCookie(DataObject.class);
        ArrayList<Action> actions = new ArrayList<Action>(Arrays.asList(
                SystemAction.get(OpenAction.class),
//                SystemAction.get(JaxWsRefreshAction.class),
//                null,
                SystemAction.get(AddOperationAction.class),
                null,
                SystemAction.get(WsTesterPageAction.class),
//                null,
                SystemAction.get(WSEditAttributesAction.class),
                SystemAction.get(ConfigureHandlerAction.class),
//                null,
//                SystemAction.get(JaxWsGenWSDLAction.class),
//                null,
//                SystemAction.get(ConvertToRestAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                null,
                SystemAction.get(PropertiesAction.class)));
        addFromLayers(actions, "WebServices/Services/Actions");
        return actions.toArray(new Action[actions.size()]);
    }

    private void addFromLayers(List<Action> actions, String path) {
        Lookup look = Lookups.forPath(path);
        for (Object next : look.lookupAll(Object.class)) {
            if (next instanceof Action) {
                actions.add((Action) next);
            } else if (next instanceof javax.swing.JSeparator) {
                actions.add(null);
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    // Handle deleting:
    @Override
    public boolean canDestroy() {
        return true;
    }

    /**
     * get URL for Web Service WSDL file
     */
    private String getWebServiceURL() {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        Deployment.getDefault().getServerInstance(provider.getServerInstanceID());
        String serverInstanceID = provider.getServerInstanceID();
        if (serverInstanceID == null || WSStackUtils.DEVNULL.equals(serverInstanceID)) {
            Logger.getLogger(JaxWsNode.class.getName()).log(Level.INFO, "Can not detect target J2EE server"); //NOI18N
            return "";
        }
        // getting port and host name
        ServerInstance serverInstance = Deployment.getDefault().getServerInstance(serverInstanceID);
        String portNumber = "8080"; //NOI18N
        String hostName = "localhost"; //NOI18N
        try {
            ServerInstance.Descriptor instanceDescriptor = serverInstance.getDescriptor();
            if (instanceDescriptor != null) {
                int port = instanceDescriptor.getHttpPort();
                portNumber = port == 0 ? "8080" : String.valueOf(port); //NOI18N
                String hstName = instanceDescriptor.getHostname();
                if (hstName != null) {
                    hostName = hstName;
                }
            } else {
                // using the old way to obtain port name and host name
                // should be removed if ServerInstance.Descriptor is implemented in server plugins
                InstanceProperties instanceProperties = provider.getInstanceProperties();
                if (instanceProperties == null) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(JaxWsNode.class, "MSG_MissingServer"), NotifyDescriptor.ERROR_MESSAGE));
                    return "";
                } else {
                    portNumber = getPortNumber(instanceProperties);
                    hostName = getHostName(instanceProperties);
                }
            }
        } catch (InstanceRemovedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Removed ServerInstance", ex); //NOI18N
        }

        String contextRoot = null;
        Object moduleType = provider.getJ2eeModule().getModuleType();
        // need to compute from annotations
        String wsURI = null;

        WSStackUtils stackUtils = new WSStackUtils(project);
//        boolean isJsr109Supported = stackUtils.isJsr109Supported();
//        if (J2eeModule.WAR.equals(moduleType) && ServerType.JBOSS == stackUtils.getServerType()) {
//            // JBoss type
//            try {
//                wsURI = getUriFromDD(moduleType);
//            } catch (UnsupportedEncodingException ex) {
//                // this shouldn't happen'
//            }
//        } else if (J2eeModule.EJB.equals(moduleType) && ServerType.JBOSS == stackUtils.getServerType()) {
//            // JBoss type
//            wsURI = getNameFromPackageName(service.getImplementationClass());
//        } else if (isJsr109Supported && Util.isJavaEE5orHigher(project) || JaxWsUtils.isEjbJavaEE5orHigher(project)) {
            try {
                wsURI = getServiceUri(moduleType);
            } catch (UnsupportedEncodingException ex) {
                // this shouldn't happen'
            }
//        } else {
//            // non jsr109 type (Tomcat)
//            try {
//                wsURI = getNonJsr109Uri(moduleType);
//            } catch (UnsupportedEncodingException ex) {
//                // this shouldn't happen'
//            }
//        }
        if (J2eeModule.WAR.equals(moduleType)) {
            J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                // TODO the context root value could not be read, let the user know about it
            }
            if (contextRoot != null && contextRoot.startsWith("/")) {
                //NOI18N
                contextRoot = contextRoot.substring(1);
            }
            if ( (contextRoot == null || contextRoot.length() == 0) &&
                    ServerType.GLASSFISH == stackUtils.getServerType()) {
                NbMavenProject nbMaven = project.getLookup().lookup(NbMavenProject.class);
                if (nbMaven != null) {
                    StringBuffer buf = new StringBuffer();
                    MavenProject mavenProject = nbMaven.getMavenProject();
                    String groupId = mavenProject.getGroupId();
                    String artifactId = mavenProject.getArtifactId();
                    String packaging = mavenProject.getPackaging();
                    String version = mavenProject.getVersion();
                    if (groupId != null && artifactId !=null && packaging != null && version != null) {
                        contextRoot = groupId+"_"+artifactId+"_"+packaging+"_"+version; // NOI18N
                    }
                }
            }
        }
//            else if (J2eeModule.EJB.equals(moduleType) && ServerType.JBOSS == stackUtils.getServerType()) {
//            // JBoss type
//            contextRoot = project.getProjectDirectory().getName();
//        }

        return "http://" + hostName + ":" + portNumber + "/" + (contextRoot != null && !contextRoot.equals("") ? contextRoot + "/" : "") + wsURI; //NOI18N
    }

//    private String getNonJsr109Uri(Object moduleType) throws UnsupportedEncodingException {
//        if (J2eeModule.WAR.equals(moduleType)) {
//            WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
//            FileObject webInfFo = webModule.getWebInf();
//            if (webInfFo != null) {
//                FileObject sunJaxwsFo = webInfFo.getFileObject("sun-jaxws", "xml"); //NOI18N
//                if (sunJaxwsFo != null) {
//                    try {
//                        Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunJaxwsFo);
//                        if (endpoints != null) {
//                            String urlPattern = findUrlPattern(endpoints, service.getImplementationClass());
//                            if (urlPattern != null) {
//                                return URLEncoder.encode(urlPattern, "UTF-8"); //NOI18N
//                            }
//                        }
//                    } catch (IOException ex) {
//                        ErrorManager.getDefault().log(ex.getLocalizedMessage());
//                    }
//                }
//            }
//        }
//        return URLEncoder.encode(getNameFromPackageName(service.getImplementationClass()), "UTF-8"); //NOI18N
//    }

//    private String getUriFromDD(Object moduleType) throws UnsupportedEncodingException {
//        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
//        if (webModule != null) {
//            FileObject ddFo = webModule.getDeploymentDescriptor();
//            if (ddFo != null) {
//                try {
//                    WebApp webApp = DDProvider.getDefault().getDDRoot(ddFo);
//                    if (webApp != null) {
//                        String urlPattern = findUrlPattern(webApp, service.getImplementationClass());
//                        if (urlPattern != null) {
//                            return URLEncoder.encode(urlPattern, "UTF-8"); //NOI18N
//                        }
//                    }
//                } catch (IOException ex) {
//                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
//                }
//            }
//        }
//        return URLEncoder.encode(getNameFromPackageName(service.getImplementationClass()), "UTF-8"); //NOI18N
//    }

//    private String findUrlPattern(Endpoints endpoints, String implementationClass) {
//        Endpoint[] endp = endpoints.getEndpoints();
//        for (int i = 0; i < endp.length; i++) {
//            if (implementationClass.equals(endp[i].getImplementation())) {
//                String urlPattern = endp[i].getUrlPattern();
//                if (urlPattern != null) {
//                    return urlPattern.startsWith("/") ? urlPattern.substring(1) : urlPattern; //NOI18N
//                }
//            }
//        }
//        return null;
//    }

    private String findUrlPattern(WebApp webApp, String implementationClass) {
        for (Servlet servlet : webApp.getServlet()) {
            if (implementationClass.equals(servlet.getServletClass())) {
                String servletName = servlet.getServletName();
                if (servletName != null) {
                    for (ServletMapping servletMapping : webApp.getServletMapping()) {
                        if (servletName.equals(servletMapping.getServletName())) {
                            String urlPattern = servletMapping.getUrlPattern();
                            return urlPattern.startsWith("/") ? urlPattern.substring(1) : urlPattern; //NOI18N
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getServiceUri(final Object moduleType) throws UnsupportedEncodingException {
        final String[] serviceName = new String[1];
        final String[] name = new String[1];
        final boolean[] isProvider = {false};
        JavaSource javaSource = getImplBeanJavaSource();
        if (javaSource != null) {
            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                    TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
                    if (typeElement != null && wsElement != null) {
                        boolean foundWsAnnotation = resolveServiceUrl(moduleType, controller, typeElement, wsElement, serviceName, name);
                        if (!foundWsAnnotation) {
                            TypeElement wsProviderElement = controller.getElements().getTypeElement("javax.xml.ws.WebServiceProvider"); //NOI18N
                            List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
                            for (AnnotationMirror anMirror : annotations) {
                                if (controller.getTypes().isSameType(wsProviderElement.asType(), anMirror.getAnnotationType())) {
                                    isProvider[0] = true;
                                }
                            }
                        }
                    }
                }

                public void cancel() {
                }
            };
            try {
                javaSource.runUserActionTask(task, true);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }

        String qualifiedImplClassName = service.getImplementationClass();
        String implClassName = getNameFromPackageName(qualifiedImplClassName);
        if (serviceName[0] == null) {
            serviceName[0] = URLEncoder.encode(implClassName + "Service", "UTF-8"); //NOI18N
        }
        if (J2eeModule.WAR.equals(moduleType)) {
            return serviceName[0];
        } else if (J2eeModule.EJB.equals(moduleType)) {
            if (name[0] == null) {
                if (isProvider[0]) {
                    //per JSR 109, use qualified impl class name for EJB
                    name[0] = qualifiedImplClassName;
                } else {
                    name[0] = implClassName;
                }
                name[0] = URLEncoder.encode(name[0], "UTF-8"); //NOI18N
            }
            return serviceName[0] + "/" + name[0];
        } else {
            return serviceName[0];
        }
    }

    private boolean resolveServiceUrl(Object moduleType, CompilationController controller, TypeElement targetElement, TypeElement wsElement, String[] serviceName, String[] name) throws IOException {
        boolean foundWsAnnotation = false;
        List<? extends AnnotationMirror> annotations = targetElement.getAnnotationMirrors();
        for (AnnotationMirror anMirror : annotations) {
            if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                foundWsAnnotation = true;
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : expressions.entrySet()) {
                    if (entry.getKey().getSimpleName().contentEquals("serviceName")) {
                        serviceName[0] = (String) expressions.get(entry.getKey()).getValue();
                        if (serviceName[0] != null) {
                            serviceName[0] = URLEncoder.encode(serviceName[0], "UTF-8"); //NOI18N
                        }
                    } else if (entry.getKey().getSimpleName().contentEquals("name")) {
                        name[0] = (String) expressions.get(entry.getKey()).getValue();
                        if (name[0] != null) {
                            name[0] = URLEncoder.encode(name[0], "UTF-8");
                        }
                    }
                    if (serviceName[0] != null && name[0] != null) {
                        break;
                    }
                }
                break;
            } // end if
        } // end for
        return foundWsAnnotation;
    }

    private String getNameFromPackageName(String packageName) {
        int index = packageName.lastIndexOf("."); //NOI18N
        return index >= 0 ? packageName.substring(index + 1) : packageName;
    }

    public String getWsdlURL() {
        String wsdlUrl = getWebServiceURL();
        return wsdlUrl.length() == 0 ? "" : wsdlUrl + "?wsdl"; //NOI18N
    }

    /**
     * get URL for Web Service Tester Page
     */
    public String getTesterPageURL() {
        WSStackUtils stackUtils = new WSStackUtils(project);
        if (ServerType.GLASSFISH == stackUtils.getServerType() || ServerType.GLASSFISH_V3 == stackUtils.getServerType() ) {
            return getWebServiceURL() + "?Tester"; //NOI18N
        } else {
            return getWebServiceURL(); //NOI18N
        }
    }

    @Override
    public void destroy() throws java.io.IOException {

        if (service.getLocalWsdl() != null) {
            // remove execution from pom file
            final FileObject wsdlFileObject = getLocalWsdl();
            if (wsdlFileObject != null) {
                ModelOperation<POMModel> oper = new ModelOperation<POMModel>() {
                    public void performOperation(POMModel model) {
                        MavenModelUtils.removeWsimportExecution(model, wsdlFileObject.getName());
                    }
                };
                FileObject pom = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                Utilities.performPOMModelOperations(pom, Collections.singletonList(oper));
            }
            // remove stale file
            try {
                removeStaleFile(wsdlFileObject.getName());
            } catch (IOException ex) {
                Logger.getLogger(JaxWsClientNode.class.getName()).log(
                        Level.FINE, "Cannot remove stale file", ex); //NOI18N
            }
        }

        WSUtils.removeImplClass(project, service.getImplementationClass());
    }

    private FileObject getImplBean() {
        String implBean = service.getImplementationClass();
        if (implBean != null) {
            return srcRoot.getFileObject(implBean.replace('.', '/') + ".java");
        }
        return null;
    }

    private FileObject getLocalWsdl() {
        JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(implBeanClass);
        if (jaxWsSupport != null) {
            FileObject localWsdlocalFolder = jaxWsSupport.getWsdlFolder(false);
            if (localWsdlocalFolder!=null) {
                String relativePath = service.getLocalWsdl();
                if (relativePath != null) {
                    return localWsdlocalFolder.getFileObject(relativePath);
                }
            }
        }
        return null;
    }

    private JavaSource getImplBeanJavaSource() {
        FileObject implBean = getImplBean();
        if (implBean != null) {
            return JavaSource.forFileObject(implBean);
        }
        return null;
    }

    /**
     * Adds possibility to display custom delete dialog
     */
//    @Override
//    public Object getValue(String attributeName) {
//        Object retValue;
//        if (attributeName.equals("customDelete")) {
//            //NOI18N
//            retValue = Boolean.TRUE;
//        } else {
//            retValue = super.getValue(attributeName);
//        }
//        return retValue;
//    }

    /**
     * Implementation of the ConfigureHandlerCookie
     */
//    public void configureHandler() {
//        FileObject implBeanFo = getImplBean();
//        List<String> handlerClasses = new ArrayList<String>();
//        FileObject handlerFO = null;
//        HandlerChains handlerChains = null;
//        //obtain the handler config file, if any from annotation in implbean
//        final String[] handlerFileName = new String[1];
//        final boolean[] isNew = new boolean[]{true};
//        JavaSource implBeanJavaSrc = JavaSource.forFileObject(implBeanFo);
//        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
//
//            public void run(CompilationController controller) throws IOException {
//                controller.toPhase(Phase.ELEMENTS_RESOLVED);
//                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
//                AnnotationMirror handlerAnnotation = getAnnotation(controller, typeElement, "javax.jws.HandlerChain"); //NOI18N
//                if (handlerAnnotation != null) {
//                    isNew[0] = false;
//                    Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = handlerAnnotation.getElementValues();
//                    for (ExecutableElement ex : expressions.keySet()) {
//                        if (ex.getSimpleName().contentEquals("file")) {   //NOI18N
//                            handlerFileName[0] = (String) expressions.get(ex).getValue();
//                            break;
//                        }
//                    }
//                }
//            }
//
//            public void cancel() {
//            }
//        };
//        try {
//            implBeanJavaSrc.runUserActionTask(task, true);
//        } catch (IOException ex) {
//            ErrorManager.getDefault().notify(ex);
//        }
//
//        if (!isNew[0] && handlerFileName[0] != null) {
//            try {
//                // look for handlerFile
//                FileObject parent = implBeanFo.getParent();
//                File parentFile = FileUtil.toFile(parent);
//
//                File file = new File(parentFile, handlerFileName[0]);
//                if (file.exists()) {
//                    file = file.getCanonicalFile();
//                    handlerFO = FileUtil.toFileObject(file);
//                }
//                if (handlerFO != null) {
//                    try {
//                        handlerChains = HandlerChainsProvider.getDefault().getHandlerChains(handlerFO);
//                    } catch (Exception e) {
//                        ErrorManager.getDefault().notify(e);
//                        return; //TODO handle this
//                    }
//                    HandlerChain[] handlerChainArray = handlerChains.getHandlerChains();
//                    //there is always only one, so get the first one
//                    HandlerChain chain = handlerChainArray[0];
//                    Handler[] handlers = chain.getHandlers();
//                    for (int i = 0; i < handlers.length; i++) {
//                        handlerClasses.add(handlers[i].getHandlerClass());
//                    }
//                } else {
//                    //unable to find the handler file, display a warning
//                    NotifyDescriptor.Message dialogDesc = new NotifyDescriptor.Message(NbBundle.getMessage(JaxWsNode.class, "MSG_HANDLER_FILE_NOT_FOUND", handlerFileName), NotifyDescriptor.INFORMATION_MESSAGE);
//                    DialogDisplayer.getDefault().notify(dialogDesc);
//                }
//            } catch (IOException ex) {
//                ErrorManager.getDefault().notify(ex);
//            }
//        }
//        final MessageHandlerPanel panel = new MessageHandlerPanel(project, handlerClasses, true, service.getName());
//        String title = NbBundle.getMessage(JaxWsNode.class, "TTL_MessageHandlerPanel");
//        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title);
//        dialogDesc.setButtonListener(new HandlerButtonListener(panel, handlerChains, handlerFO, implBeanFo, service, isNew[0]));
//        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
//        dialog.getAccessibleContext().setAccessibleDescription(dialog.getTitle());
//        dialog.setVisible(true);
//    }

//    static AnnotationMirror getAnnotation(CompilationController controller, TypeElement typeElement, String annotationType) {
//        TypeElement anElement = controller.getElements().getTypeElement(annotationType);
//        if (anElement != null) {
//            List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
//            for (AnnotationMirror annotation : annotations) {
//                if (controller.getTypes().isSameType(anElement.asType(), annotation.getAnnotationType())) {
//                    return annotation;
//                }
//            }
//        }
//        return null;
//    }

//    void refreshImplClass() {
//        if (implBeanClass != null) {
//            content.remove(implBeanClass);
//        }
//        implBeanClass = getImplBean();
//        content.add(implBeanClass);
//    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public boolean canCut() {
        return true;
    }

//    @Override
//    public Transferable clipboardCopy() throws IOException {
//        URL url = new URL(getWsdlURL());
//        boolean connectionOK = false;
//        try {
//            URLConnection connection = url.openConnection();
//            if (connection instanceof HttpURLConnection) {
//                HttpURLConnection httpConnection = (HttpURLConnection) connection;
//                try {
//                    httpConnection.setRequestMethod("GET"); //NOI18N
//                    httpConnection.connect();
//                    if (HttpURLConnection.HTTP_OK == httpConnection.getResponseCode()) {
//                        connectionOK = true;
//                    }
//                } catch (java.net.ConnectException ex) {
//                    //TODO: throw exception here?
//                    url = null;
//                } finally {
//                    if (httpConnection != null) {
//                        httpConnection.disconnect();
//                    }
//                }
//                if (!connectionOK) {
//                    //TODO: throw exception here?
//                    url = null;
//                }
//            }
//        } catch (IOException ex) {
//            //TODO: throw exception here?
//            url = null;
//        }
//
//        return new WebServiceTransferable(new WebServiceReference(url, service.getWsdlUrl() != null ? service.getServiceName() : service.getName(), project.getProjectDirectory().getName()));
//    }

//    private class RefreshServiceImpl implements JaxWsRefreshCookie {
//
//        /**
//         * refresh service information obtained from wsdl (when wsdl file was changed)
//         */
//        public void refreshService(boolean downloadWsdl) {
//            if (downloadWsdl) {
//                String result = RefreshWsDialog.open(downloadWsdl, service.getImplementationClass(), service.getWsdlUrl());
//                if (RefreshWsDialog.CLOSE.equals(result)) {
//                    return;
//                }
//                if (result.startsWith(RefreshWsDialog.DO_ALL)) {
//                    ((JaxWsChildren) getChildren()).refreshKeys(true, true, result.substring(1));
//                } else if (result.startsWith(RefreshWsDialog.DOWNLOAD_WSDL)) {
//                    ((JaxWsChildren) getChildren()).refreshKeys(true, false, result.substring(1));
//                } else if (RefreshWsDialog.REGENERATE_IMPL_CLASS.equals(result)) {
//                    ((JaxWsChildren) getChildren()).refreshKeys(false, true, null);
//                } else {
//                    ((JaxWsChildren) getChildren()).refreshKeys(false, false, null);
//                }
//            } else {
//                String result = RefreshWsDialog.openWithOKButtonOnly(downloadWsdl, service.getImplementationClass(), service.getWsdlUrl());
//                if (RefreshWsDialog.REGENERATE_IMPL_CLASS.equals(result)) {
//                    ((JaxWsChildren) getChildren()).refreshKeys(false, true, null);
//                } else {
//                    ((JaxWsChildren) getChildren()).refreshKeys(false, false, null);
//                }
//            }
//        }
//    }

    /** Old way to obtain port number from server instance (using instance properties)
     * 
     * @param instanceProperties
     * @return port number
     */
    private String getPortNumber(InstanceProperties instanceProperties) {
        String portNumber = instanceProperties.getProperty(InstanceProperties.HTTP_PORT_NUMBER);
        if (portNumber == null || portNumber.equals("")) { //NOI18N
            return "8080"; //NOI18N
        } else {
            return portNumber;
        }
    }

    /** Old way to obtain host name from server instance (using instance properties)
     * 
     * @param instanceProperties
     * @return host name
     */
    private String getHostName(InstanceProperties instanceProperties) {
        String serverUrl = instanceProperties.getProperty(InstanceProperties.URL_ATTR);
        String hostName = "localhost"; //NOI18N
        if (serverUrl != null && serverUrl.indexOf("::") > 0) { //NOI18N
            //NOI18N
            int index1 = serverUrl.indexOf("::"); //NOI18N
            int index2 = serverUrl.lastIndexOf(":"); //NOI18N
            if (index2 > index1 + 2) {
                hostName = serverUrl.substring(index1 + 2, index2);
            }
        }
        return hostName;
    }
    
    void setServiceUrl() {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                JaxWsNode.this.setValue("wsdl-url", getWsdlURL());
            }
        });        
    }
    
    protected void fireShortDescriptionChange() {
        setShortDescription(getWsdlURL());
    }

    private void attachFileChangeListener() {
        implBeanClass.addFileChangeListener(new FileChangeAdapter() {

            @Override
            public void fileChanged(final FileEvent fe) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String oldServiceName = service.getServiceName();
                        final String[] newServiceName = new String[1];
                        JavaSource javaSource = JavaSource.forFileObject(fe.getFile());
                        if (javaSource!=null) {

                            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                                public void run(CompilationController controller) throws IOException {
                                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                                    TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                                    if (typeElement!=null) {
                                        // check service name
                                        newServiceName[0] = getServiceName(controller, typeElement);
                                    }
                                }
                                public void cancel() {}
                            };

                            try {
                                javaSource.runUserActionTask(task, true);
                            } catch (IOException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }

                            if (newServiceName[0] == null) {
                                newServiceName[0] = fe.getFile().getName()+"Service"; // defaultName
                            }

                            if (!newServiceName[0].equals(oldServiceName)) {
                                FileObject implBean = getImplBean();
                                if (getImplBean() != null) { // check if service wasn't removed already
                                    service.setServiceName(newServiceName[0]);
                                    fireDisplayNameChange(oldServiceName, newServiceName[0]);
                                    fireNameChange(oldServiceName, newServiceName[0]);
                                    fireShortDescriptionChange();

                                    // replace nonJSR109 entries
                                    if (!WSUtils.isJsr109Supported(project)) {
                                        JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(implBean);
                                        FileObject ddFolder = jaxWsSupport.getDeploymentDescriptorFolder();
                                        if (ddFolder != null) {
                                            try {
                                                WSUtils.replaceSunJaxWsEntries(ddFolder, oldServiceName, newServiceName[0]);
                                            } catch (IOException ex) {
                                                Logger.getLogger(JaxWsNode.class.getName()).log(Level.WARNING,
                                                        "Cannot modify endpoint in sun-jaxws.xml file", ex); //NOI18N
                                            }
                                        }
                                        try {
                                            WSUtils.replaceServiceEntriesFromDD(project, oldServiceName, newServiceName[0]);
                                        } catch (IOException ex) {
                                            Logger.getLogger(JaxWsNode.class.getName()).log(Level.WARNING,
                                                    "Cannot modify web.xml file", ex); //NOI18N
                                        }
                                    }
                                }
                            }

                        }
                    }
                });
            }

        });
    }

    private String getServiceName(CompilationController controller, TypeElement classElement) {
         // check service name
        TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
        if (wsElement != null) {
            List<? extends AnnotationMirror> annotations = classElement.getAnnotationMirrors();

            for (AnnotationMirror anMirror : annotations) {
                if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                    java.util.Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                    for (java.util.Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : expressions.entrySet()) {
                        if (entry.getKey().getSimpleName().contentEquals("serviceName")) { //NOI18N
                            return (String)expressions.get(entry.getKey()).getValue();
                        }
                    }
                }
            }
        }
        return null;
    }

    public void configureHandler() {
        FileObject implBeanFo = getImplBean();
        List<String> handlerClasses = new ArrayList<String>();
        FileObject handlerFO = null;
        HandlerChains handlerChains = null;
        //obtain the handler config file, if any from annotation in implbean
        final String[] handlerFileName = new String[1];
        final boolean[] isNew = new boolean[]{true};
        JavaSource implBeanJavaSrc = JavaSource.forFileObject(implBeanFo);
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                AnnotationMirror handlerAnnotation = _RetoucheUtil.getAnnotation(controller, typeElement, "javax.jws.HandlerChain"); //NOI18N
                if (handlerAnnotation != null) {
                    isNew[0] = false;
                    Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = handlerAnnotation.getElementValues();
                    for (ExecutableElement ex : expressions.keySet()) {
                        if (ex.getSimpleName().contentEquals("file")) {   //NOI18N
                            handlerFileName[0] = (String) expressions.get(ex).getValue();
                            break;
                        }
                    }
                }
            }

            public void cancel() {
            }
        };
        try {
            implBeanJavaSrc.runUserActionTask(task, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        if (!isNew[0] && handlerFileName[0] != null) {
            try {
                // look for handlerFile
                FileObject parent = implBeanFo.getParent();
                File parentFile = FileUtil.toFile(parent);

                File file = new File(parentFile, handlerFileName[0]);
                if (file.exists()) {
                    file = file.getCanonicalFile();
                    handlerFO = FileUtil.toFileObject(file);
                }
                if (handlerFO != null) {
                    try {
                        handlerChains = HandlerChainsProvider.getDefault().getHandlerChains(handlerFO);
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(e);
                        return; //TODO handle this
                    }
                    HandlerChain[] handlerChainArray = handlerChains.getHandlerChains();
                    //there is always only one, so get the first one
                    HandlerChain chain = handlerChainArray[0];
                    Handler[] handlers = chain.getHandlers();
                    for (int i = 0; i < handlers.length; i++) {
                        handlerClasses.add(handlers[i].getHandlerClass());
                    }
                } else {
                    //unable to find the handler file, display a warning
                    NotifyDescriptor.Message dialogDesc = new NotifyDescriptor.Message(NbBundle.getMessage(JaxWsNode.class, "MSG_HANDLER_FILE_NOT_FOUND", handlerFileName), NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(dialogDesc);
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        final MessageHandlerPanel panel = new MessageHandlerPanel(project, handlerClasses, true, service.getServiceName());
        String title = NbBundle.getMessage(JaxWsNode.class, "TTL_MessageHandlerPanel");
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title);
        dialogDesc.setButtonListener(new HandlerButtonListener(panel, handlerChains, handlerFO, implBeanFo, service, isNew[0]));
        DialogDisplayer.getDefault().notify(dialogDesc);
//        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
//        dialog.getAccessibleContext().setAccessibleDescription(dialog.getTitle());
//        dialog.setVisible(true);
    }

    private void removeStaleFile(String name) throws IOException {
        FileObject staleFile = project.getProjectDirectory().getFileObject("target/jaxws/stale/"+name+".stale");
        if (staleFile != null) {
            staleFile.delete();
        }
    }

}
