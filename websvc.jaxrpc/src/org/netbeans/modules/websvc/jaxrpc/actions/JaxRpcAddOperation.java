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

package org.netbeans.modules.websvc.jaxrpc.actions;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import org.netbeans.modules.websvc.core.AddWsOperationHelper;
import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.DDProvider;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.openide.util.Lookup;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/** JaxWsAddOperation.java
 * Created on December 12, 2006, 4:36 PM
 *
 * @author mkuchtiak
 */
public class JaxRpcAddOperation implements AddOperationCookie {

    private static final String Remote_Exception = "java.rmi.RemoteException"; //NOI18N
    private FileObject implementationClass;

    /** Creates a new instance of JaxWsAddOperation */
    public JaxRpcAddOperation(FileObject implementationClass) {
        this.implementationClass = implementationClass;
    }

    public void addOperation() {
        WebserviceDescription wsDesc = findWSDescriptionFromClass(implementationClass);
        if (wsDesc == null)
            return; //this will never happen
        FileObject implBeanClass;
        FileObject seiClass;
        if (!isSEIClass(implementationClass, wsDesc)) {
            implBeanClass = implementationClass;
            seiClass = getSEIClass(wsDesc,implementationClass);
        } else {
            implBeanClass = getImplementationBean(wsDesc, implementationClass);
            if(implBeanClass==null)
                return; // this should never happen
            seiClass = implementationClass;
        }
        AddWsOperationHelper strategy = new AddWsOperationHelper(NbBundle.getMessage(JaxRpcAddOperation.class, "LBL_AddOperationAction"), false);
        try {
            String className = getMainClassName(implBeanClass);
            if (className != null) {
                MethodModel methodModel = strategy.getMethodModel(implBeanClass, className);
                if (methodModel!=null && seiClass != null) {
                    addMethodToSEI(methodModel, seiClass);
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    @Override
    public boolean isEnabledInEditor(Lookup nodeLookup) {
        return isWsImplBeanOrInterface(implementationClass);
    }
    
    public static String getMainClassName(final FileObject classFO) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(classFO);
        final String[] result = new String[1];
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement classEl = SourceUtils.getPublicTopLevelElement(controller);
                if (classEl != null) {
                    result[0] = classEl.getQualifiedName().toString();
                }
            }
        }, true);
        return result[0];
    }

    private boolean hasRemoteException(final List<String> exceptions) {
        for (String exception : exceptions) {
            if (exception.equals(Remote_Exception)) {
                return true;
            }
        }
        return false;
    }

    private void addMethodToSEI(final MethodModel methodModel, FileObject seiFo) {
        final JavaSource targetSource = JavaSource.forFileObject(seiFo);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    List<String> exceptions = new ArrayList<String>();
                    exceptions.addAll(methodModel.getExceptions());
                    if (!hasRemoteException(exceptions)) {
                        exceptions.add(Remote_Exception); //all SEI methods must throw RemoteException
                    }
                    MethodModel seiMethodModel = MethodModel.create(methodModel.getName(), methodModel.getReturnType(), null, methodModel.getParameters(), exceptions, methodModel.getModifiers()); //create an SEI version of MethodModel, no method body
                    MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, seiMethodModel);
                    ClassTree modifiedClass = make.addClassMember(javaClass, methodTree);
                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }

            public void cancel() {
            }
        };
        try {
            targetSource.runModificationTask(modificationTask).commit();
            DataObject dataObject = DataObject.find(seiFo);
            if (dataObject != null) {
                SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private FileObject getSEIClass(WebserviceDescription wsDesc, FileObject implClass) {
        PortComponent[] ports = wsDesc.getPortComponent();
        if (ports.length > 0) {
            //in the Jaxrpc support, we assume only one port
            PortComponent portComponent = ports[0];
            String sei = portComponent.getServiceEndpointInterface();
            sei = sei.replace(".", "/") + ".java";
            Project project = FileOwnerQuery.getOwner(implClass);
            for (SourceGroup srcGroup:ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                FileObject seiFO = srcGroup.getRootFolder().getFileObject(sei);
                if (seiFO != null) {
                    return seiFO;
                }
            }
        }
        return null;
    }

    private FileObject getImplementationBean(WebserviceDescription wsDesc, FileObject seiClass) {
        PortComponent[] ports = wsDesc.getPortComponent();
        if (ports.length > 0) {
            //in the Jaxrpc support, we assume only one port
            PortComponent portComponent = ports[0];
            ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
            String link = serviceImplBean.getServletLink();
            if (link == null) {
                link = serviceImplBean.getEjbLink();
            }
            String implBean = WebServicesSupport.getWebServicesSupport(seiClass).getImplementationBean(link);
            implBean = implBean.replace(".", "/") + ".java";
            Project project = FileOwnerQuery.getOwner(seiClass);
            for (SourceGroup srcGroup:ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                FileObject implFO = srcGroup.getRootFolder().getFileObject(implBean);
                if (implFO != null) {
                    return implFO;
                }
            }
        }
        return null;
    }

    private boolean isSEIClass(FileObject fileObject, WebserviceDescription wsDesc) {
        ClassPath classPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
        String implClassPath = classPath.getResourceName(fileObject, '.', false);
        PortComponent portComponent = wsDesc.getPortComponent(0);
        // first check the interface
        String wsSEI = portComponent.getServiceEndpointInterface();
        if ((wsSEI != null) && (implClassPath.endsWith(wsSEI))) {
            return true;
        }
        return false;
    }

    public static boolean isWsImplBeanOrInterface(FileObject implClassFo) {
        WebserviceDescription wsDesc = findWSDescriptionFromClass(implClassFo);
        if (wsDesc != null) {
            WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(implClassFo);
            if (wsSupport != null) {
                return !wsSupport.isFromWSDL(wsDesc.getWebserviceDescriptionName());
            }
        }
        return false;
    }

    static WebserviceDescription findWSDescriptionFromClass(FileObject implClassFO) {
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(implClassFO);
        ClassPath classPath = ClassPath.getClassPath(implClassFO, ClassPath.SOURCE);
        String implClassPath = null;
        if(classPath==null || (implClassPath = classPath.getResourceName(implClassFO, '.', false))==null) {
            return null;
        }
        if (wsSupport != null) {
            DDProvider wsDDProvider = DDProvider.getDefault();
            Webservices webServices = null;
            try {
                webServices = wsDDProvider.getDDRoot(wsSupport.getWebservicesDD());
            } catch(java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            if(webServices != null) {
                WebserviceDescription[] wsDescriptions = webServices.getWebserviceDescription();
                for (int i = 0; i < wsDescriptions.length; i++) {
                    WebserviceDescription wsDescription = wsDescriptions[i];
                    PortComponent portComponent = wsDescription.getPortComponent(0);
                    // first check the interface
                    String wsSEI = portComponent.getServiceEndpointInterface();
                    if ((wsSEI != null) && (implClassPath.endsWith(wsSEI))) {
                        return wsDescription;
                    }
                    // then the implementation bean
                    ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
                    String link = serviceImplBean.getServletLink();
                    if (link == null) {
                        link = serviceImplBean.getEjbLink();
                    }
                    String implBean = wsSupport.getImplementationBean(link);
                    if (implBean!=null && implClassPath.endsWith(implBean)) {
                        return wsDescription;
                    }
                }
            }
        }
        return null;
    }
    
}
