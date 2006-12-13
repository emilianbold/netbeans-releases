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
 * ServiceInformationSupport.java
 *
 * Created on May 2, 2006, 2:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.jaxrpc;

import org.netbeans.modules.websvc.wsdl.config.ServiceInformationImpl;
import org.openide.loaders.DataObject;

/**
 *
 * @author rico
 */
public class ServiceInformationSupport {
    private static ServiceInformationSupport serviceInfoSupport;
    
    /** Creates a new instance of ServiceInformationSupport */
    private ServiceInformationSupport() {
    }
    
    public static ServiceInformationSupport getDefault(){
        if(serviceInfoSupport == null){
            serviceInfoSupport = new ServiceInformationSupport();
        }  
        return serviceInfoSupport;
    }
    
    public ServiceInformation getServiceInformation(DataObject dataObject){
        ServiceInformation serviceInfo = 
                (ServiceInformation)dataObject.getCookie(ServiceInformation.class);
        if(serviceInfo != null){
            return serviceInfo;
        }
        return new ServiceInformationImpl(dataObject);
    }
    
}
