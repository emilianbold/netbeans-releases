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

package org.netbeans.modules.websvc.manager.impl;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsCodeGenerator;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer.Method;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.util.WebServiceLibReferenceHelper;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
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
                Project targetProject = FileOwnerQuery.getOwner(
                    NbEditorUtilities.getFileObject(targetComponent.getDocument()));                
                Method method = 
                    (Method) transferable.getTransferData(WebServiceMetaDataTransfer.METHOD_FLAVOR);
                WebServiceData d = method.getWebServiceData();
                
                //copy client jars
                List<String> jars = new ArrayList<String>();
                jars.add(WebServiceManager.WEBSVC_HOME + File.separator + 
                        WsdlUtil.getServiceDirName(d.getOriginalWsdlUrl()) + File.separator + WebServiceData.JAX_WS + File.separator + 
                        d.getName() + ".jar");
                WebServiceLibReferenceHelper.addArchiveRefsToProject(
                        targetProject, jars);              

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