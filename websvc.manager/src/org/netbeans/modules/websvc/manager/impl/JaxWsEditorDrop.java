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

package org.netbeans.modules.websvc.manager.impl;

import java.awt.datatransfer.Transferable;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.jaxws.nodes.OperationNode;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsCodeGenerator;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer.Method;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer.Port;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.openide.ErrorManager;
import org.openide.text.ActiveEditorDrop;

/** JaxWsEditorDrop
 *
 * @author Ayub Khan
 */
public class JaxWsEditorDrop implements ActiveEditorDrop {
    
    JaxWsTransferManager manager;
    private Transferable transferable;
    
    public JaxWsEditorDrop(JaxWsTransferManager manager) {
        this.manager=manager;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        Object mimeType = targetComponent.getDocument().getProperty("mimeType"); //NOI18N
        if (mimeType!=null && ("text/x-java".equals(mimeType) || "text/x-jsp".equals(mimeType) )) { //NOI18N
            
            try {
                boolean enableDnD=false;
                //Node clientNode = manager.getParentNode().getParentNode().getParentNode();
                //FileObject srcRoot = (FileObject)clientNode.getLookup().lookup(FileObject.class);
                //Project clientProject = FileOwnerQuery.getOwner(srcRoot);
                Project targetProject = FileOwnerQuery.getOwner(
                    NbEditorUtilities.getFileObject(targetComponent.getDocument()));
                Method method = 
                    (Method) transferable.getTransferData(WebServiceMetaDataTransfer.METHOD_FLAVOR);
                WebServiceData d = method.getWebServiceData();
                WsdlService service = d.getWsdlService();
                WsdlPort port = service.getPortByName(method.getPortName());
                WsdlOperation operation = method.getOperation();
                String wsdlUrl = d.getURL();
                Document document = targetComponent.getDocument();
                int pos = targetComponent.getCaret().getDot();
                
                JaxWsCodeGenerator.insertMethod(document, pos, service, port, operation, wsdlUrl);
                
            } catch (Exception ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
        }
        return false;
    }

    void setTransferable(Transferable transferable) {
        this.transferable = transferable;
    }
    
}