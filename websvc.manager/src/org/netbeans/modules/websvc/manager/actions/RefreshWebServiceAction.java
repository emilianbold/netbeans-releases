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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.manager.actions;

import java.io.IOException;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that refreshes a web service from its original wsdl location.
 * 
 * @author quynguyen
 */
public class RefreshWebServiceAction extends NodeAction {
    protected boolean enable(Node[] nodes) {
        if(nodes != null &&
        nodes.length != 0) {
            for (int i = 0; i < nodes.length; i++) {
                WebServiceData data = nodes[i].getLookup().lookup(WebServiceData.class);
                if (data != null && !data.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILING)) {
                    return true;
                }
            }
            
            return false;
        } else {
            return false;
        }
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/websvc/manager/resources/MyActionIcon.gif"; // NOI18N
    }
    
    public String getName() {
        return NbBundle.getMessage(RefreshWebServiceAction.class, "REFRESH");
    }
    
    protected void performAction(Node[] nodes) {
        
        if(null != nodes && nodes.length > 0) {
            final Ref<Boolean> errorNotify = new Ref<Boolean>(Boolean.FALSE);
            String msg = NbBundle.getMessage(RefreshWebServiceAction.class, "WS_REFRESH");
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
            Object response = DialogDisplayer.getDefault().notify(d);
            if(null != response && response.equals(NotifyDescriptor.YES_OPTION)) {
                for(int ii = 0; ii < nodes.length; ii++) {
                    final WebServiceData wsData = nodes[ii].getLookup().lookup(WebServiceData.class);
                    
                    if(wsData != null) {
                        Runnable refreshTask = new Runnable() {
                            public void run() {
                                try {
                                    if (!wsData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILING)) {
                                        WebServiceManager.getInstance().refreshWebService(wsData);
                                    }
                                } catch (IOException ioe) {
                                    wsData.setResolved(false);
                                    
                                    if (!errorNotify.reference.booleanValue()) {
                                        errorNotify.reference = Boolean.TRUE;
                                        String message = NbBundle.getMessage(WebServiceManager.class, "WS_WSDL_XML_ERROR");
                                        NotifyDescriptor d = new NotifyDescriptor.Message(message);
                                        DialogDisplayer.getDefault().notify(d);
                                    }
                                }
                            }
                        };
                        
                        WebServiceManager.getInstance().getRequestProcessor().post(refreshTask);
                    }
                }
            }
        }
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    private static final class Ref<T> {
        public T reference;
        
        public Ref(T ref) {
            this.reference = ref;
        }
    }
}
