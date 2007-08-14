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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.wsitconf.api;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.wsitconf.*;
import org.netbeans.modules.websvc.wsitconf.ui.service.ServiceTopComponent;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public final class WSITConfigProvider extends Object {

    private static final Logger logger = Logger.getLogger(WSITEditor.class.getName());

    private static WSITConfigProvider instance;
  
    private WSITConfigProvider() { }

    public static synchronized WSITConfigProvider getDefault() {
        if (instance == null) {
            instance = new WSITConfigProvider();
        }
        return instance;
    }

     /**
     * Returns a WSIT configuration editor, then you may call same methods as WS Attributes editor api does,
     * which means WSEditor.createWSEditorComponent(Node node, JaxWsModel jaxWsModel). This call returns JComponent 
     * with WSIT config UI.
     * This method only returns dialog - there are no OK/Cancel buttons provided - thus it's required that a 
     * caller of this method makes sure appropriate actions are taken on wsdlModel and undomanager for Cancel/Save actions
     */
    public final JComponent getWSITServiceConfig(WSDLModel wsdlModel, UndoManager undoManager, Collection<Binding> bindings, Node node) {
        final ServiceTopComponent stc = new ServiceTopComponent(wsdlModel, undoManager, bindings, node);
        return stc;
    }
    
    /**
     * Should be invoked with same parameters as WSEditor calls are invoked. Returns false if WSIT is not supported,
     * is switched off, an error happened, or WSIT security features are switched off.
     * Is here to enable other parties (e.g. AccessManager) to detect if WSIT is configured, because combinations 
     * don't work well together.
     * @param node 
     * @param jaxWsModel 
     * @return 
     */
    public final boolean isWsitSecurityEnabled(Node node, JaxWsModel jaxWsModel) {
        
        //is it a client node?
        Client client = node.getLookup().lookup(Client.class);
        //is it a service node?
        Service service = node.getLookup().lookup(Service.class);
        
        Project p = null;
        if (jaxWsModel != null) {
            p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
        }

        if (p != null) {
            if (Util.isWsitSupported(p)) {
                try {
                    WSDLModel wsdlModel = WSITModelSupport.getModel(node, jaxWsModel, null, false, null);
                    if (wsdlModel != null) {
                        if (client != null) { //it's a client
                            JAXWSClientSupport wscs = JAXWSClientSupport.getJaxWsClientSupport(p.getProjectDirectory());
                            if (wscs != null) {
                                WSDLModel serviceWsdlModel = WSITModelSupport.getServiceModelForClient(wscs, client);
                                Collection<Binding> bindings = serviceWsdlModel.getDefinitions().getBindings();
                                for (Binding b : bindings) {
                                    if (SecurityPolicyModelHelper.isSecurityEnabled(b)) {
                                        return true;
                                    }
                                }
                            }
                        } else if (service != null) {
                            Collection<Binding> bindings = wsdlModel.getDefinitions().getBindings();
                            for (Binding b : bindings) {
                                if (SecurityPolicyModelHelper.isSecurityEnabled(b)) {
                                    return true;
                                }
                            }
                        }
                    }
                } catch(Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }
            }
        }
        return false;        
    }

    /**
     * Should be invoked with same parameters as WSEditor calls are invoked. Returns false if WSIT is not supported,
     * is switched off, or an error happened.
     * Is here to enable other parties (e.g. AccessManager) to detect if WSIT is configured, because combinations 
     * don't work well together.
     * @param node 
     * @param jaxWsModel 
     * @return 
     */
    public final boolean isWsitEnabled(Node node, JaxWsModel jaxWsModel) {
        
        Project p = null;
        if (jaxWsModel != null) {
            p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
        }

        if (p != null) {
            if (Util.isWsitSupported(p)) {
                try {
                    WSDLModel wsdlModel = WSITModelSupport.getModel(node, jaxWsModel, null, false, null);
                    if (wsdlModel != null) {
                        return true;
                    }
                } catch(Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }
            }
        }
        return false;        
    }
    
}
