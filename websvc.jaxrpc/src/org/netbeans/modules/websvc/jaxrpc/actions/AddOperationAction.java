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
package org.netbeans.modules.websvc.jaxrpc.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
// Retouche
//import javax.jmi.reflect.JmiException;
//import org.netbeans.jmi.javamodel.ClassMember;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.JavaModelPackage;
//import org.netbeans.jmi.javamodel.Method;
//import org.netbeans.jmi.javamodel.Resource;
//import org.netbeans.jmi.javamodel.Type;
//import org.netbeans.jmi.javamodel.TypeReference;
//import org.netbeans.modules.j2ee.common.JMIUtils;
//import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
//import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
//import org.netbeans.modules.javacore.api.JavaModel;
//import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
//import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsClassesCookie;
//import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsCookieFactory;
//import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
//import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
//import org.openide.ErrorManager;
import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import java.lang.reflect.Modifier;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;

public class AddOperationAction extends CookieAction {
    //private Service service;
    public String getName() {
        return NbBundle.getMessage(AddOperationAction.class, "LBL_OperationAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(AddOperationAction.class);
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {WebServiceClassesCookie.class};
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes.length == 1 &&
// Retouche
//                ( activatedNodes[0].getLookup().lookup(ClassMember.class) != null || JMIUtils.getClassMemberFromNode(activatedNodes[0])!=null ) &&
//                JMIUtils.getJavaClassFromNode(activatedNodes[0]) != null &&
                (isJaxWsImplementationClass(activatedNodes[0]) || 
                (isWsImplBeanOrInterface(activatedNodes[0]) && !isFromWSDL(activatedNodes[0])));
    }
    
    private boolean isWsImplBeanOrInterface(Node node) {
// Retouche
//        JavaClass ce = JMIUtils.getJavaClassFromNode(node);
//        Resource r = ce.getResource();
//        FileObject f = JavaModel.getFileObject(r);
//        if (f != null) {
//
//            WebserviceDescription wsDesc = WebServiceCookieFactory.findWSDescriptionFromClass(ce, f);
//            if (wsDesc != null) {
//                return true;
//            }
//        }
        return false;
    }

    private boolean isJaxWsImplementationClass(Node node) {
// Retouche
//         JavaClass ce = JMIUtils.getJavaClassFromNode(node);
//         Resource r = ce.getResource();
//         FileObject fo = JavaModel.getFileObject(r);
//         JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(fo);
//         if (jaxWsSupport!=null) {
//             List services = jaxWsSupport.getServices();
//             for (int i=0;i<services.size();i++) {
//                 Service serv = (Service)services.get(i);
//                 if (serv.getWsdlUrl()==null) {
//                     String implClass = serv.getImplementationClass();
//                     if (implClass.equals(ce.getName())) {
//                         service=serv;
//                         return true;
//                     }
//                 }
//             }
//         }
//         service=null;
         return false;
    }
    
    private boolean isFromWSDL(Node node) {
// Retouche
//        JavaClass ce = JMIUtils.getJavaClassFromNode(node);
//        Resource r = ce.getResource();
//        FileObject f = JavaModel.getFileObject(r);
//        if (f != null) {
//            WebserviceDescription wsDesc = WebServiceCookieFactory.findWSDescriptionFromClass(ce, f);
//            if (wsDesc != null) {
//                String wsName = wsDesc.getWebserviceDescriptionName();
//                WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(f);
//                assert wsSupport != null;
//                return wsSupport.isFromWSDL(wsName);
//            }
//        }
        return false;
    }
    
    protected void performAction(Node[] activatedNodes) {
// Retouche        
//        JavaMetamodel.getManager().waitScanFinished();
//        
//        Node.Cookie cookie=null;
//        if (service!=null) {
//            cookie = JaxWsCookieFactory.getJaxWsClassesCookie(service,JMIUtils.getJavaClassFromNode(activatedNodes[0]));
//        } else {
//            cookie = WebServiceCookieFactory.getWebServiceClassesCookie(JMIUtils.getJavaClassFromNode(activatedNodes[0]));
//        }
//        if (cookie == null) return;
//
//        Method m = null;
//        JavaClass javaClass = JMIUtils.getJavaClassFromNode(activatedNodes[0]);
//
//        if ((javaClass != null) && (javaClass.isValid())) {
//            JMIUtils.beginJmiTransaction();
//            try {
//                m = JMIUtils.createMethod(javaClass);
//                m.setModifiers(Modifier.PUBLIC);
//                m.setName(NbBundle.getMessage(AddOperationAction.class, "TXT_DefaultOperationName")); //NOI18N
//                // sets 'String' as a default return value for method. 
//                // method.setType(type) can't be used here since it would set 
//                // return value to 'java.lang.String' (#61178)
//                TypeReference tr = 
//                        ((JavaModelPackage)javaClass.refImmediatePackage()).getMultipartId().createMultipartId("String", null, null);
//                m.setTypeName(tr);
//                
//            } catch (JmiException e) {
//                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.toString());
//                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.getElementInError().toString());
//                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.getObjectInError().toString());
//            } finally {
//                JMIUtils.endJmiTransaction();
//            }
//            if (m == null) {
//                return;
//            }
//
//            MethodCustomizer mc = MethodCollectorFactory.operationCollector(m);
//            final NotifyDescriptor nd = new NotifyDescriptor(mc,  NbBundle.getMessage(AddOperationAction.class, "TTL_AddOperation"),
//                    NotifyDescriptor.OK_CANCEL_OPTION,
//                    NotifyDescriptor.PLAIN_MESSAGE,
//                    null, null
//                    );
//            mc.addPropertyChangeListener(new PropertyChangeListener() {
//                public void propertyChange(PropertyChangeEvent evt) {
//                    if (evt.getPropertyName().equals(MethodCustomizer.OK_ENABLED)) {
//                        Object newvalue = evt.getNewValue();
//                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
//                            nd.setValid(((Boolean)newvalue).booleanValue());
//                        }
//                    }
//                }
//            });
//            Object rv = DialogDisplayer.getDefault().notify(nd);
//            mc.isOK(); // apply possible changes in dialog fields
//            if (rv == NotifyDescriptor.OK_OPTION) {
//                if (cookie instanceof JaxWsClassesCookie)
//                    ((JaxWsClassesCookie)cookie).addOperation(m);
//                else 
//                    ((WebServiceClassesCookie)cookie).addOperation(m);
//            }
//
//        }

    }
}
