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

package org.netbeans.modules.websvc.design.view.widget;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.*;

/**
 *
 * @author Ajit Bhate
 */
public class OperationsWidget extends Widget{
    
    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param label 
     */
    public OperationsWidget(Scene scene, Service service) {
        super(scene);
        createContent(service);
    }
    
    private void createContent(Service service) {
        try {
            String wsdlUrlStr = service.getWsdlUrl();
            if(wsdlUrlStr==null) return;
            URL wsdlUrl = new URL(wsdlUrlStr);
            if(wsdlUrl==null) return;
            WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlUrl);
            if(modeler==null) return;
            WsdlModel model = modeler.getWsdlModel();
            if(model==null) return;
            WsdlService wsdlService = model.getServiceByName(service.getName());
            if (wsdlService==null) return;
            for(WsdlPort port:wsdlService.getPorts()) {
                for(WsdlOperation operation:port.getOperations()) {
                    addChild(new OperationWidget(getScene(),operation));
                }
            }
        } catch(MalformedURLException e) {
        }
    }
}
