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
package org.netbeans.modules.websvc.core.webservices.action;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.AddOperationCookie;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

/** Editor action that opens the Add Operation dialog and adds new methods to
 *  the service
 */
public class AddOperationEditorAction extends NodeAction {
    private Service service;
    public String getName() {
        return NbBundle.getMessage(AddOperationEditorAction.class, "LBL_AddOperationEditorAction");
    }
    
    public HelpCtx getHelpCtx() {
        // If you will provide context help then use:  
        return HelpCtx.DEFAULT_HELP;
  }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            FileObject fo = getFileObjectFromNode(activatedNodes[0]);
            if (fo!=null) {
                AddOperationCookie cookie = WebServiceActionProvider.getAddOperationAction(fo);
                return cookie!=null && activatedNodes.length == 1 &&
// Retouche
//                  ( activatedNodes[0].getLookup().lookup(ClassMember.class) != null || JMIUtils.getClassMemberFromNode(activatedNodes[0])!=null ) &&
//                  JMIUtils.getJavaClassFromNode(activatedNodes[0]) != null &&
                    (isJaxWsImplementationClass(activatedNodes[0]) /*|| (isWsImplBeanOrInterface(activatedNodes[0]) */
                    && !isFromWSDL(activatedNodes[0]));
            }
        }
        return false;
    }
    
    
    private FileObject getFileObjectFromNode(Node n) {
        DataObject dObj = (DataObject)n.getCookie(DataObject.class);
        if (dObj!=null) return dObj.getPrimaryFile();
        else return null;
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
        FileObject fo = getFileObjectFromNode(node);
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(fo);
        if (jaxWsSupport!=null) {
            List services = jaxWsSupport.getServices();
            for (int i=0;i<services.size();i++) {
                Service serv = (Service)services.get(i);
                if (serv.getWsdlUrl()==null) {
                    String implClass = serv.getImplementationClass();
                    if (implClass.equals(getPackageName(fo))) {
                        service=serv;
                        return true;
                    }
                }
            }
        }
        service=null;
        return false;
    }
    
    private boolean isFromWSDL(Node node) {
        if (service!=null) {
            return service.getWsdlUrl()!=null;
        }
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
    
    private String getPackageName(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        Sources sources = (Sources)project.getLookup().lookup(Sources.class);
        if (sources!=null) {
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (groups!=null) {
                List<FileObject> roots = new ArrayList<FileObject>();
                for (SourceGroup group: groups) {   
                    FileObject rootFolder = group.getRootFolder();
                    if (FileUtil.isParentOf(rootFolder, fo)) {
                        String relativePath = FileUtil.getRelativePath(rootFolder, fo).replace('/', '.');
                        return (relativePath.endsWith(".java")? //NOI18N
                            relativePath.substring(0,relativePath.length()-5):
                            relativePath);
                    }
                }
            }   
        }
        return null;
    }
    
    protected void performAction(Node[] activatedNodes) {
            FileObject fo = getFileObjectFromNode(activatedNodes[0]);
            AddOperationCookie cookie = WebServiceActionProvider.getAddOperationAction(fo);
            cookie.addOperation(fo);
    }
}
