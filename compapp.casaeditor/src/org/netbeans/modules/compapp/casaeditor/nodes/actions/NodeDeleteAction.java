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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.ConnectionState;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.openide.util.NbBundle;

/**
 *
 * @author Josh Sandusky
 */
public class NodeDeleteAction extends NodeAbstractAction {
    
    
    public NodeDeleteAction(CasaNode node) {
        super(NbBundle.getMessage(NodeDeleteAction.class, "NAME_Delete"), node);        // NOI18N
    }
    
    
    public void actionPerformed(ActionEvent e) {
        List widgetsToDelete = new ArrayList();
        Object data = getData();
        if (data != null) {
            widgetsToDelete.add(data);
        }
        delete(getModel(), widgetsToDelete);
    }
    
    public static void delete(CasaWrapperModel model, List objectsToDelete) {
        DeleteVisitorCasa deleterCasa = new DeleteVisitorCasa(model);
        for (Object object : objectsToDelete) {
            ((CasaComponent) object).accept(deleterCasa);
        }
    }
    
    
    private static class DeleteVisitorCasa extends CasaComponentVisitor.Default {
        
        private CasaWrapperModel mModel;
        
        public DeleteVisitorCasa(CasaWrapperModel model) {
            mModel = model;
        }
        
        public void visit(CasaConnection connection) {
            // Ensure the connection is not already deleted.
            String state = connection.getState();
            if (
                    connection.isInDocumentModel() && 
                    !ConnectionState.DELETED.getState().equals(state)) {
                mModel.removeConnection(connection);
            }
        }
        
        public void visit(CasaConsumes consumes) {
            // Ensure the endpoint is not already deleted.
            if (consumes.isInDocumentModel()) {
                mModel.removeEndpoint(consumes, true);
            }
        }
        
        public void visit(CasaProvides provides) {
            // Ensure the endpoint is not already deleted.
            if (provides.isInDocumentModel()) {
                mModel.removeEndpoint(provides, true);
            }
        }
        
        public void visit(CasaPort port) {
            mModel.removeCasaPort(port, true);
        }
        
        public void visit(CasaServiceEngineServiceUnit su) {
            mModel.removeServiceEngineServiceUnit(su); 
        }
    }
}
