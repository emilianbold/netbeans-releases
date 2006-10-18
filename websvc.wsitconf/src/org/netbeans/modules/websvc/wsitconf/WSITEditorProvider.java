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
package org.netbeans.modules.websvc.wsitconf;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditorProvider;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.WSITEditor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class WSITEditorProvider implements WSEditorProvider {
    
    /**
     * Creates a new instance of WSITEditorProvider
     */
    public WSITEditorProvider () {
    }

    public WSEditor createWSEditor() {
        return new WSITEditor();
    }

    public boolean enable(Node node) {
        //is it a client node?
        Client client = (Client)node.getLookup().lookup(Client.class);
        //is it a service node?
        Service service = (Service)node.getLookup().lookup(Service.class);
        
        Project p = null;
        FileObject srcRoot = (FileObject)node.getLookup().lookup(FileObject.class);
        if (srcRoot != null) {
            p = FileOwnerQuery.getOwner(srcRoot);
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot lookup source root from node: " + node);  //NOI18N
        }
        
        if (client != null){ //its a client
            if (p != null) {
                JAXWSClientSupport wscs = JAXWSClientSupport.getJaxWsClientSupport(p.getProjectDirectory());
                if (wscs != null) {
                    return WSITEditor.isWsitSupported(p);
                }
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                                              "WS Client support not found for: " +
                                              p.getProjectDirectory());
            }
        } else if (service != null) {
            if (p != null) {
                JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(p.getProjectDirectory());
                if (wss != null) {
                    return WSITEditor.isWsitSupported(p);
                }
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                                              "WS support not found for: " +
                                              p.getProjectDirectory());
            }
        }
        return false;
    }
}
