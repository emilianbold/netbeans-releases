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
package org.netbeans.modules.websvc.axis2.actions;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class GenerateWsdlAction extends NodeAction  {
    
    public String getName() {
        return NbBundle.getMessage(GenerateWsdlAction.class, "LBL_GenerateWsdlAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
        
    @Override
    protected boolean asynchronous() {
        return true;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes==null || activatedNodes.length != 1) return false;
        Service service = activatedNodes[0].getLookup().lookup(Service.class);        
        if (service == null || service.getGenerateWsdl() == null) return false;
        else return true;
    }
    
    protected void performAction(Node[] activatedNodes) {
        boolean wsdlAlreadyExists = false;

        Service service = activatedNodes[0].getLookup().lookup(Service.class);
        FileObject srcRoot = activatedNodes[0].getLookup().lookup(FileObject.class);
        Project prj = FileOwnerQuery.getOwner(srcRoot);
        FileObject projectDir = prj.getProjectDirectory();
        String serviceName = service.getNameAttr();
        if (projectDir.getFileObject("xml-resources/axis2/META-INF/"+serviceName+".wsdl") != null) { //NOI18N
            wsdlAlreadyExists = true;
        }
        if (wsdlAlreadyExists) {
            String message = NbBundle.getMessage(GenerateWsdlAction.class, "TXT_ConfirmWsdlReplace", serviceName);
            NotifyDescriptor dialog = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(dialog);
            if (dialog.getValue() == NotifyDescriptor.YES_OPTION) {
                AxisUtils.runTargets(projectDir, new String[]{"java2wsdl-clean-"+serviceName,"java2wsdl-"+serviceName }); //NOI18N
            }
        } else {
            AxisUtils.runTargets(projectDir, new String[]{"java2wsdl-"+serviceName }); //NOI18N
        }
    }

}

