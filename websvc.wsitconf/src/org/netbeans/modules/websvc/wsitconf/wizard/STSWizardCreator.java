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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wizard;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.util.GenerationUtils;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class STSWizardCreator {
    public static final String MEX_NAME = "com.sun.xml.ws.mex.server.MEXEndpoint";

    protected static final int JSE_PROJECT_TYPE = 0;
    protected static final int WEB_PROJECT_TYPE = 1;
    protected static final int EJB_PROJECT_TYPE = 2;

    private static final String SERVLET_NAME = "ServletName";
    private static final String SERVLET_CLASS = "ServletClass";
    private static final String URL_PATTERN = "UrlPattern";
    
    private int projectType;

    private Project project;
    private WizardDescriptor wiz;

    public boolean jwsdpSupported, wsitSupported, jsr109Supported, jsr109oldSupported;

    private static final Logger logger = Logger.getLogger(STSWizardCreator.class.getName());
    
    public STSWizardCreator(Project project, WizardDescriptor wiz) {
        this.project = project;
        this.wiz = wiz;
    }
    
    public STSWizardCreator(Project project) {
        this.project = project;
    }
    
    public void createSTS() {
        final ProgressHandle handle = ProgressHandleFactory.createHandle( 
                NbBundle.getMessage(STSWizardCreator.class, "TXT_StsGeneration")); //NOI18N

        initProjectInfo(project);
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    handle.start(100);
                    generateWsFromWsdl15(handle);
                } catch (Exception e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if(message != null) {
                        logger.log(Level.INFO, null, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } else {
                        logger.log(Level.INFO, null, e);
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }
    
    private void initProjectInfo(Project project) {
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
            String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if (serverInstance != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                if (j2eePlatform != null) {
                    jwsdpSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP);
                    wsitSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT);
                    jsr109Supported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
                    jsr109oldSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE);
                }
            }
        }
        
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        EjbJar em = EjbJar.getEjbJar(project.getProjectDirectory());
        if (em != null)
            projectType = EJB_PROJECT_TYPE;
        else if (wm != null)
            projectType = WEB_PROJECT_TYPE;
        else
            projectType = JSE_PROJECT_TYPE;
    }
    
    private void generateWsFromWsdl15(final ProgressHandle handle) throws Exception {
        String wsdlFilePath = (String) wiz.getProperty(WizardProperties.WSDL_FILE_PATH);
        File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
        //convert to URI first to take care of spaces
        final URL wsdlURL = normalizedWsdlFilePath.toURI().toURL();
        final WsdlService service = (WsdlService) wiz.getProperty(WizardProperties.WSDL_SERVICE);
        if (service==null) {
            handle.finish();
            return;
        } else {
            final WsdlPort port = (WsdlPort) wiz.getProperty(WizardProperties.WSDL_PORT);
            //String portJavaName = port.getJavaName();   
            WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(WizardProperties.WSDL_MODELER);
            // don't set the packageName for modeler (use the default one generated from target Namespace)
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    WsdlService service1 = model.getServiceByName(service.getName());
                    WsdlPort port1 = service1.getPortByName(port.getName());
                    port1.setSOAPVersion(port.getSOAPVersion());
                    FileObject targetFolder = Templates.getTargetFolder(wiz);
                    String targetName = Templates.getTargetName(wiz);
                    try {
                        generateProviderImplClass(project, targetFolder, targetName, service1, port1, wsdlURL);
                        handle.finish();
                    } catch (Exception ex) {
                        handle.finish();
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }
    
    public void generateProviderImplClass(Project project, FileObject targetFolder,
            String targetName, final WsdlService service, final WsdlPort port, URL wsdlURL) throws Exception {
        initProjectInfo(project);
        
        String serviceID = service.getName();
        
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
            
        FileObject implClassFo = GenerationUtils.createClass(targetFolder, targetName, null);
        ClassPath classPath = ClassPath.getClassPath(implClassFo, ClassPath.SOURCE);            
        String serviceImplPath = classPath.getResourceName(implClassFo, '.', false);
        String portJavaName = port.getJavaName();
        String artifactsPckg = portJavaName.substring(0, portJavaName.lastIndexOf('.'));

        serviceID = jaxWsSupport.addService(targetName, serviceImplPath, wsdlURL.toString(), service.getName(), port.getName(), artifactsPckg, jsr109Supported && Util.isJavaEE5orHigher(project));
        final String wsdlLocation = jaxWsSupport.getWsdlLocation(serviceID);
                       
        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                if (genUtils!=null) {     
                    TreeMaker make = workingCopy.getTreeMaker();
                    ClassTree javaClass = genUtils.getClassTree();
                    ClassTree modifiedClass;
                    
                    // add implementation clause
                    TypeElement provider = workingCopy.getElements().getTypeElement("javax.xml.ws.Provider"); //NOI18N
                    TypeElement source = workingCopy.getElements().getTypeElement("javax.xml.transform.Source"); //NOI18N
                    TypeElement baseStsImpl = workingCopy.getElements().getTypeElement("com.sun.xml.ws.security.trust.sts.BaseSTSImpl"); //NOI18N
                    TypeElement msgContext = workingCopy.getElements().getTypeElement("javax.xml.ws.handler.MessageContext"); //NOI18N
                    TypeElement resource = workingCopy.getElements().getTypeElement("javax.annotation.Resource"); //NOI18N
                    TypeElement wsContext = workingCopy.getElements().getTypeElement("javax.xml.ws.WebServiceContext"); //NOI18N
                    TypeElement WSAn = workingCopy.getElements().getTypeElement("javax.xml.ws.WebServiceProvider"); //NOI18N
                    
                    // create parameters
                    List<AnnotationTree> annotations = new ArrayList<AnnotationTree>();
                    AnnotationTree resourceAnnotation = make.Annotation(
                        make.QualIdent(resource), 
                        Collections.<ExpressionTree>emptyList()
                    );
                    annotations.add(resourceAnnotation);
                    
                    List<VariableTree> classField = new ArrayList<VariableTree>();
                    // final ObjectOutput arg0
                    classField.add(make.Variable(
                            make.Modifiers(
                                Collections.<Modifier>emptySet(),
                                annotations
                            ),
                            "context", // name
                            make.QualIdent(wsContext), // parameter type
                            null // initializer - does not make sense in parameters.
                    ));
                    
                    modifiedClass = genUtils.addClassFields(javaClass, classField);
                    
                    ParameterizedTypeTree t = make.ParameterizedType(make.QualIdent(provider), 
                            Collections.singletonList(make.QualIdent(source)) );
                    modifiedClass = make.addClassImplementsClause(modifiedClass, t);
                    modifiedClass = make.setExtends(modifiedClass, make.QualIdent(baseStsImpl));
                    
                    //add @WebServiceProvider annotation
                    List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                    attrs.add(
                        make.Assignment(make.Identifier("serviceName"), make.Literal(service.getName()))); //NOI18N
                    attrs.add(
                        make.Assignment(make.Identifier("portName"), make.Literal(port.getName()))); //NOI18N
                    attrs.add(
                        make.Assignment(make.Identifier("targetNamespace"), make.Literal(port.getNamespaceURI()))); //NOI18N
                    attrs.add(
                        make.Assignment(make.Identifier("wsdlLocation"), make.Literal(wsdlLocation))); //NOI18N
                    AnnotationTree WSAnnotation = make.Annotation(
                        make.QualIdent(WSAn), 
                        attrs
                    );
                    modifiedClass = genUtils.addAnnotation(modifiedClass, WSAnnotation);
                                        
                    //add @WebServiceProvider annotation
                    TypeElement modeAn = workingCopy.getElements().getTypeElement("javax.xml.ws.ServiceMode"); //NOI18N
                    List<ExpressionTree> attrsM = new ArrayList<ExpressionTree>();

                    TypeElement te = workingCopy.getElements().getTypeElement("javax.xml.ws.Service.Mode");
                    
                    ExpressionTree mstree = make.MemberSelect(make.QualIdent(te), "PAYLOAD");
                    
                    attrsM.add(
                        make.Assignment(make.Identifier("value"), mstree)); //NOI18N
                    AnnotationTree modeAnnot = make.Annotation(
                        make.QualIdent(modeAn), 
                        attrsM
                    );
                    modifiedClass = genUtils.addAnnotation(modifiedClass, modeAnnot);

                    // add @Stateless annotation
                    if (projectType == EJB_PROJECT_TYPE) {//EJB project
                        TypeElement StatelessAn = workingCopy.getElements().getTypeElement("javax.ejb.Stateless"); //NOI18N                   
                        AnnotationTree StatelessAnnotation = make.Annotation(
                            make.QualIdent(StatelessAn), 
                            Collections.<ExpressionTree>emptyList()
                        );
                        modifiedClass = genUtils.addAnnotation(modifiedClass, StatelessAnnotation);
                    }

                    // create parameters
                    List<VariableTree> params = new ArrayList<VariableTree>();
                    // final ObjectOutput arg0
                    params.add(make.Variable(
                            make.Modifiers(
                                Collections.<Modifier>emptySet(),
                                Collections.<AnnotationTree>emptyList()
                            ),
                            "rstElement", // name
                            make.QualIdent(source), // parameter type
                            null // initializer - does not make sense in parameters.
                    ));

                    // create method
                    ModifiersTree methodModifiers = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC),
                        Collections.<AnnotationTree>emptyList()
                    );
                    
                    List<ExpressionTree> exc = new ArrayList<ExpressionTree>();
                    
                    MethodTree method = make.Method(
                            methodModifiers, // public
                            "invoke", // operation name
                            make.QualIdent(source), // return type 
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            params,
                            exc, // throws 
                            "{ return super.invoke(rstElement); }", // body text
                            null // default value - not applicable here, used by annotations
                    );
                    modifiedClass =  make.addClassMember(modifiedClass, method); 
                    
                    // create method
                    ModifiersTree msgContextModifiers = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PROTECTED),
                        Collections.<AnnotationTree>emptyList()
                    );
                    
                    List<ExpressionTree> excMsg = new ArrayList<ExpressionTree>();
                    
                    MethodTree methodMsgContext = make.Method(
                            msgContextModifiers, // public
                            "getMessageContext", // operation name
                            make.QualIdent(msgContext), // return type 
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            Collections.<VariableTree>emptyList(),
                            excMsg, // throws 
                            "{ MessageContext msgCtx = context.getMessageContext();\nreturn msgCtx; }", // body text
                            null // default value - not applicable here, used by annotations
                    );
                    modifiedClass =  make.addClassMember(modifiedClass, methodMsgContext);                     
                    
                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }

            public void cancel() { 
            }
        };
        
        targetSource.runModificationTask(task).commit();
            
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            try {
                WebApp wApp = DDProvider.getDefault ().getDDRoot(wm.getDeploymentDescriptor());                    
                Servlet servlet = Util.getServlet(wApp, serviceImplPath);
                if (servlet == null) {      //NOI18N
                    try {
                        servlet = (Servlet)wApp.addBean("Servlet",              //NOI18N
                                new String[]{SERVLET_NAME,SERVLET_CLASS},    
                                new Object[]{targetName,serviceImplPath},SERVLET_NAME);
                        servlet.setLoadOnStartup(new java.math.BigInteger("0"));               //NOI18N
                        wApp.addBean("ServletMapping", new String[]{SERVLET_NAME,URL_PATTERN}, //NOI18N
                                new Object[]{targetName, "/" + targetName},SERVLET_NAME);      //NOI18N
                        try {
                            servlet = (Servlet)wApp.addBean("Servlet",              //NOI18N
                                    new String[]{SERVLET_NAME,SERVLET_CLASS},    
                                    new Object[]{MEX_NAME,MEX_NAME},SERVLET_NAME);
                            servlet.setLoadOnStartup(new java.math.BigInteger("0"));     //NOI18N
                            } catch (NameAlreadyUsedException ex) {
                            // do nothing, this is ok - there should be only one instance of this
                        }
                        wApp.addBean("ServletMapping", new String[]{SERVLET_NAME,URL_PATTERN}, //NOI18N
                                new Object[]{MEX_NAME, "/" + targetName + "/mex"},URL_PATTERN);  //NOI18N
                        wApp.write(wm.getDeploymentDescriptor());
                    } catch (NameAlreadyUsedException ex) {
                        ex.printStackTrace();
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        //open in the editor
        DataObject dobj = DataObject.find(implClassFo);
        openFileInEditor(dobj);
    }

    private static void openFileInEditor(DataObject dobj){
        final EditorCookie ec = dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }
        
}
