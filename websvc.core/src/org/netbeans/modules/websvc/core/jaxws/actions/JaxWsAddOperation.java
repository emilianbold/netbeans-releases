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

package org.netbeans.modules.websvc.core.jaxws.actions;

import org.netbeans.modules.websvc.core._RetoucheUtil;
import org.netbeans.modules.websvc.core.AddWsOperationHelper;
import org.netbeans.modules.websvc.core.AddOperationCookie;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.util.RequestProcessor;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/** JaxWsAddOperation.java
 * Created on December 12, 2006, 4:36 PM
 *
 * @author mkuchtiak
 */
public class JaxWsAddOperation implements AddOperationCookie {
    private FileObject implClassFo;
    private Service service;
    
    /** Creates a new instance of JaxWsAddOperation */
    public JaxWsAddOperation(FileObject implClassFo) {
        this.implClassFo=implClassFo;
        service = getService();
    }
    
    public void addOperation(final FileObject implementationClass) {
        final AddWsOperationHelper strategy = new AddWsOperationHelper(
                NbBundle.getMessage(AddWsOperationHelper.class, "LBL_OperationAction"));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    String className = _RetoucheUtil.getMainClassName(implementationClass);
                    if (className != null) {
                        strategy.addMethod(implementationClass, className);
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        });
    }
    
    public boolean isEnabledInEditor(FileObject implClass) {
        return isJaxWsImplementationClass() && !isFromWSDL();
    }
    
    private boolean isJaxWsImplementationClass() {
        return service != null;
    }
  
    private Service getService(){
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(implClassFo);
        if (jaxWsSupport!=null) {
            List services = jaxWsSupport.getServices();
            for (int i=0;i<services.size();i++) {
                Service serv = (Service)services.get(i);
                if (serv.getWsdlUrl()==null) {
                    String implClass = serv.getImplementationClass();
                    if (implClass.equals(getPackageName(implClassFo))) {
                        return serv;
                    }
                }
            }
        }
        return null;
    }
    
    private boolean isFromWSDL() {
        if(service != null){
            return service.getWsdlUrl()!=null;
        }
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
    
}
