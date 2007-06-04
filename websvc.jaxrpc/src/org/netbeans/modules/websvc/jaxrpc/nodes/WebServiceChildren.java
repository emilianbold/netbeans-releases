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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.io.IOException;
import javax.swing.SwingUtilities;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;

import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.ErrorManager;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import static org.netbeans.api.java.source.JavaSource.Phase;

import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.common.source.SourceUtils;

public class WebServiceChildren extends Children.Keys {
    
    private static final java.awt.Image OPERATION_BADGE =
        org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/core/webservices/ui/resources/wsoperation.png" ); //NOI18N
    
    private WebserviceDescription webServiceDescription;
    private FileObject implClass;
    private FileObject srcRoot;
    
    public WebServiceChildren(WebserviceDescription webServiceDescription, FileObject srcRoot, FileObject implClass) {
        super();
        this.webServiceDescription = webServiceDescription;
        this.srcRoot = srcRoot;
        this.implClass = implClass;
    }
    
    protected Node[] createNodes(Object key) {
        if(key instanceof ExecutableElement) {
            final ExecutableElement method = (ExecutableElement)key;
            Node n = new AbstractNode(Children.LEAF) {

                @java.lang.Override
                public java.awt.Image getIcon(int type) {
                    return OPERATION_BADGE;
                }

                @Override
                public String getDisplayName() {
                    return method.getSimpleName().toString();
                }
            };
            
            return new Node[]{n};
        }
        return new Node[0];
    }

    private boolean isFromWsdl() {
        //TODO
        return false;
    }
    
    protected void addNotify() {
        super.addNotify();
        if (isFromWsdl()) {
            //TODO
        } else {
            updateKeys();
        }
    }

    private void updateKeys() {
        if (isFromWsdl()) {
            List keys = new ArrayList();
            //TODO
            setKeys(keys);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final List keys = new ArrayList();
                    if (implClass != null) {
                        JavaSource javaSource = JavaSource.forFileObject(implClass);
                        if (javaSource!=null) {
                            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                                public void run(CompilationController controller) throws IOException {
                                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                                    SourceUtils srcUtils = SourceUtils.newInstance(controller);
                                    if (srcUtils!=null) {
                                        // find WS operations as all public methods
                                        List<ExecutableElement> publicMethods = getPublicMethods(controller, srcUtils.getTypeElement());
                                        keys.addAll(publicMethods);
                                    }
                                }
                                
                                public void cancel() {}
                            };
                            try {
                                javaSource.runUserActionTask(task, true);
                            } catch (IOException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                    }
                    setKeys(keys);
                }
            });
        }
    }
    
    private List<ExecutableElement> getPublicMethods(CompilationController controller, TypeElement classElement) throws IOException {
        List<? extends Element> members = classElement.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(members);
        List<ExecutableElement> publicMethods = new ArrayList<ExecutableElement>();
        for (ExecutableElement method:methods) {
            Set<Modifier> modifiers = method.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)) {
                publicMethods.add(method);
            }
        }
        return publicMethods;
    }
    
    // Retouche
    //    public ComponentMethodViewStrategy createViewStrategy() {
    //        WSComponentMethodViewStrategy strategy = WSComponentMethodViewStrategy.instance();
    //        return strategy;
    //    }
    //
    //    protected JavaClass getImplBean() {
    //        return getImplBeanClass(webServiceDescription);
    //    }
    //
    //    protected Collection getInterfaces() {
    //        Set set = new HashSet();
    //        set.add(getServiceEndpointInterface(webServiceDescription));
    //        return set;
    //    }
    //
    //    private JavaClass getServiceEndpointInterface(WebserviceDescription webServiceDescription){
    //        PortComponent portComponent = webServiceDescription.getPortComponent(0);
    //        String sei = portComponent.getServiceEndpointInterface();
    //        if(sei != null) {
    //            sei = sei.trim(); // IZ 56889: must trim white space, if any, before using this information.
    //        }
    //        return JMIUtils.findClass(sei);
    //    }
    //
    //    private JavaClass getImplBeanClass(WebserviceDescription webServiceDescription) {
    //        PortComponent portComponent = webServiceDescription.getPortComponent(0); //assume one port per ws
    //        ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
    //        String link =serviceImplBean.getServletLink();
    //        if(link == null) {
    //            link = serviceImplBean.getEjbLink();
    //        }
    //        if(link != null) {
    //            link = link.trim(); // Related to IZ 56889: must trim white space, if any, before using this information.
    //        }
    //        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(srcRoot);
    //        String implBean = wsSupport.getImplementationBean(link);
    //        if(implBean != null) {
    //            return JMIUtils.findClass(implBean);
    //        }
    //        return null;
    //    }
    //
    //    public static class WSComponentMethodViewStrategy implements ComponentMethodViewStrategy {
    //      //  private Image NOT_OPERATION_BADGE = Utilities.loadImage("org/openide/src/resources/error.gif");
    //        private static WSComponentMethodViewStrategy wsmvStrategy;
    //        private WSComponentMethodViewStrategy(){
    //        }
    //
    //        public static WSComponentMethodViewStrategy instance(){
    //            if(wsmvStrategy == null){
    //                wsmvStrategy = new WSComponentMethodViewStrategy();
    //            }
    //            return wsmvStrategy;
    //        }
    //        public Image getBadge(Method method, Collection interfaces){
    //
    //       /* no need to badge this, it sometimes not a sign for bad operation see 55679    Set paramTypes = new HashSet();
    //            //FIX-ME:Need a better way to find out if method is in SEI
    //            MethodParameter[] parameters = method.getParameters();
    //            for(int i = 0; i < parameters.length; i++){
    //                paramTypes.add(parameters[i].getType());
    //            }
    //            Iterator iter  = interfaces.iterator();
    //            while(iter.hasNext()){
    //                ClassElement intf = (ClassElement)iter.next();
    //                if(intf.getMethod(method.getName(), (Type[])paramTypes.toArray(new Type[paramTypes.size()])) == null){
    //                    return NOT_OPERATION_BADGE;
    //                }
    //
    //            }*/
    //
    //            return null;
    //        }
    //        public void deleteImplMethod(Method m, JavaClass implClass, Collection interfaces) throws IOException{
    //            //delete method in the SEI
    //            Iterator iter = interfaces.iterator();
    //            while (iter.hasNext()){
    //                JavaClass intf = (JavaClass)iter.next();
    //                try {
    //                    intf.getContents().remove(m);
    //                } catch (JmiException e) {
    //                    throw new IOException(e.getMessage());
    //                }
    //            }
    //            //delete method from Impl class
    //            Method[] methods = JMIUtils.getMethods(implClass);
    //            for(int i = 0; i < methods.length; i++){
    //                Method method = methods[i];
    //                if (JMIUtils.equalMethods(m, method)) {
    //                    try {
    //                        implClass.getContents().remove(method);
    //                        break;
    //                    } catch (JmiException e) {
    //                        throw new IOException(e.getMessage());
    //                    }
    //                }
    //            }
    //        }
    //
    //        public OpenCookie getOpenCookie(Method m, JavaClass implClass, Collection interfaces) {
    //            Method[] methods = JMIUtils.getMethods(implClass);
    //            for(int i = 0; i < methods.length; i++) {
    //                Method method = methods[i];
    //                if (JMIUtils.equalMethods(m, method)) {
    //                    return (OpenCookie)JMIUtils.getCookie(method, OpenCookie.class);
    //                }
    //            }
    //            return null;
    //        }
    //
    //        public Image getIcon(Method me, Collection interfaces) {
    //            return Utilities.loadImage("org/openide/src/resources/methodPublic.gif");
    //        }
    //
    //    }
    
}
