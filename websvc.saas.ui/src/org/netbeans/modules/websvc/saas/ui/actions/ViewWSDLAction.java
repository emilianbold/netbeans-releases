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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.saas.ui.actions;

import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasPort;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author  nam
 */
public class ViewWSDLAction extends NodeAction {
    
    /** Creates a new instance of ViewWSDLAction */
    public ViewWSDLAction() {
    }
    
    protected boolean enable(Node[] nodes) {
        WsdlSaas saas = getWsdlSaas(nodes);
        if (saas != null) {
            return saas.getState() == Saas.State.RESOLVED;
        }
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage(ViewWSDLAction.class, "VIEW_WSDL");
    }
    
    private WsdlSaas getWsdlSaas(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return null;
        }

        WsdlSaas saas = nodes[0].getLookup().lookup(WsdlSaas.class);
        if (saas == null) {
            WsdlSaasPort port = nodes[0].getLookup().lookup(WsdlSaasPort.class);
            if (port != null) {
                saas = port.getParentSaas();
            }
        }
        if (saas == null) {
            WsdlSaasMethod method = nodes[0].getLookup().lookup(WsdlSaasMethod.class);
            if (method != null) {
                saas = method.getSaas();
            }
        }
        return saas;
    }
    
    protected void performAction(Node[] nodes) {
        WsdlSaas saas = getWsdlSaas(nodes);
        if (saas == null) {
            throw new IllegalArgumentException("No nodes assoaciated WsdlSaas in lookup.");
        }
        if (saas.getState() != Saas.State.RESOLVED) {
            throw new IllegalStateException("WsdlSaas is not in resolved state.");
        }

        String location = saas.getWsdlData().getWsdlFile();
        FileObject wsdlFileObject = saas.getLocalWsdlFile();

        if (null == wsdlFileObject) {
            String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND", location);
            NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        //TODO: open in read-only mode
        try {
            DataObject wsdlDataObject = DataObject.find(wsdlFileObject);
            EditorCookie editorCookie = wsdlDataObject.getCookie(EditorCookie.class);
            editorCookie.open();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    public boolean asynchronous() {
        return true;
    }
}
