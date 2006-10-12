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
/*
 * CustomizationWSEditorProvider.java
 *
 * Created on February 17, 2006, 11:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.core.ui;

import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditorProvider;
import org.openide.nodes.Node;

/**
 *
 * @author Roderico Cruz
 */
public class CustomizationWSEditorProvider
        implements WSEditorProvider{
    
    /** Creates a new instance of CustomWSAttributeEditorProvider */
    public CustomizationWSEditorProvider() {
    }
    
    public WSEditor createWSEditor() {
        return new CustomizationWSEditor();
    }
    
    public boolean enable(Node node) {
        Client client = (Client)node.getLookup().lookup(Client.class);
        if(client != null){
            return true;
        } else{
            Service service = (Service)node.getLookup().lookup(Service.class);
            if(service != null){
                return (service.getWsdlUrl() != null);
            }
        }
        return false;
    }
    
}
