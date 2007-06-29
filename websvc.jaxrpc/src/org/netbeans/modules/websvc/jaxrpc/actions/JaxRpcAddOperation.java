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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import org.netbeans.modules.websvc.core._RetoucheUtil;
import org.netbeans.modules.websvc.core.AddWsOperationHelper;
import org.netbeans.modules.websvc.core.AddOperationCookie;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import static org.netbeans.api.java.source.JavaSource.Phase;
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
    
    private final static String Remote_Exception = "java.rmi.RemoteException";  //NOI18N
    
    /** Creates a new instance of JaxWsAddOperation */
    public JaxRpcAddOperation() {
    }
    
    public void addOperation(FileObject implementationClass) {
        AddWsOperationHelper strategy = new AddWsOperationHelper(
                NbBundle.getMessage(AddWsOperationHelper.class, "LBL_OperationAction"), false);
        try {
            String className = _RetoucheUtil.getMainClassName(implementationClass);
            if (className != null) {
                MethodModel methodModel = strategy.getMethodModel(implementationClass, className);
                if(methodModel != null){
                    addMethodToSEI(methodModel, implementationClass);
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    public boolean isEnabledInEditor(FileObject implClass) {
        return isWsImplBeanOrInterface(implClass);
    }
    
    private boolean hasRemoteException(final List<String> exceptions){
        for(String exception : exceptions){
            if(exception.equals(Remote_Exception)){
                return true;
            }
        }
        return false;
    }
    
    private void addMethodToSEI(final MethodModel methodModel, FileObject implClassFo){
        if(methodModel == null) return;
        FileObject seiFo = getSEIClass(implClassFo);
        if(seiFo != null){
            final JavaSource targetSource = JavaSource.forFileObject(seiFo);
            final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(Phase.RESOLVED);
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    if (genUtils!=null) {
                        ClassTree javaClass = genUtils.getClassTree();
                        TreeMaker make = workingCopy.getTreeMaker();
                        List<String> exceptions = new ArrayList<String>();
                        exceptions.addAll(methodModel.getExceptions());
                        if(!hasRemoteException(exceptions)){
                            exceptions.add(Remote_Exception);  //all SEI methods must throw RemoteException
                        }
                        MethodModel seiMethodModel = MethodModel.create(methodModel.getName(), methodModel.getReturnType(), null,
                                methodModel.getParameters(), exceptions,methodModel.getModifiers());  //create an SEI version of MethodModel, no method body
                        MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, seiMethodModel);
                        ClassTree modifiedClass = make.addClassMember(javaClass,methodTree);
                        workingCopy.rewrite(javaClass, modifiedClass);
                    }
                }
                public void cancel() {}
            };
            try {
                targetSource.runModificationTask(modificationTask).commit();
                DataObject dataObject = DataObject.find(seiFo);
                if (dataObject!=null) {
                    SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
                    if (cookie!=null) cookie.save();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    private FileObject getSEIClass(FileObject implClass){
        PortComponent portComponent = null;
        WebserviceDescription wsDesc = AddOperationAction.findWSDescriptionFromClass(implClass);
        if (wsDesc != null) {
            PortComponent[] ports = wsDesc.getPortComponent();
            if(ports.length > 0){
                //in the Jaxrpc support, we assume only one port
                portComponent = ports[0];
                String sei = portComponent.getServiceEndpointInterface();
                Project project = FileOwnerQuery.getOwner(implClass);
                SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                        JavaProjectConstants.SOURCES_TYPE_JAVA);
                ClassPath classPath = null;
                sei = sei.replace(".", "/") + ".java";
                for(int i = 0 ; i < sourceGroups.length; i++){
                    classPath = ClassPath.getClassPath(sourceGroups[i].getRootFolder(),ClassPath.SOURCE);
                    FileObject seiFO = classPath.findResource(sei);
                    if(seiFO != null){
                        return seiFO;
                    }
                }
            }
        }
        return null;
    }
    
    private boolean isWsImplBeanOrInterface(FileObject implClassFo) {
        WebserviceDescription wsDesc = AddOperationAction.findWSDescriptionFromClass(implClassFo);
        if (wsDesc != null) {
            WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(implClassFo);
            if(wsSupport != null){
                return !wsSupport.isFromWSDL(wsDesc.getWebserviceDescriptionName());
            }
        }
        return false;
    }
    
    
}
