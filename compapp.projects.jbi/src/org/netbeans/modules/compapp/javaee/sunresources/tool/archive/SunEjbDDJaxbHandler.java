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

package org.netbeans.modules.compapp.javaee.sunresources.tool.archive;

import java.util.Iterator;

import org.netbeans.modules.compapp.javaee.sunresources.generated.sunejb30.*;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode;
import org.openide.util.NbBundle;

/**
 * @author echou
 *
 */
public class SunEjbDDJaxbHandler {

    private SunEjbJar root;
    
    public SunEjbDDJaxbHandler(Object root) throws Exception {
        if (root instanceof SunEjbJar) {
            this.root = (SunEjbJar) root;
        } else {
            throw new Exception(
                    NbBundle.getMessage(SunEjbDDJaxbHandler.class, "EXC_bad_jaxbroot", root.getClass()));
        }
    }
    
    public String findJndiByEjbName(String ejbName) {
        for (Iterator<Ejb> ejbIter = root.getEnterpriseBeans().getEjb().iterator();
            ejbIter.hasNext(); ) {
            Ejb ejb = ejbIter.next();
            if (ejb.getEjbName().equals(ejbName)) {
                return ejb.getJndiName();
            }
        }
        return null;
    }
    
    public String resolveResRef(String ejbName, String resRefName) {
        for (Iterator<Ejb> ejbIter = root.getEnterpriseBeans().getEjb().iterator();
            ejbIter.hasNext(); ) {
            Ejb ejb = ejbIter.next();
            for (Iterator<ResourceRef> resRefIter = ejb.getResourceRef().iterator();
                resRefIter.hasNext(); ) {
                ResourceRef resRef = resRefIter.next();
                if (resRef.getResRefName().equals(resRefName)) {
                    return resRef.getJndiName();
                }
            }
        }
        return null;
    }

    public String resolveMsgDestRef(String ejbName, String msgDestRefLink) {
        for (Iterator<MessageDestination> msgDestIter = root.getEnterpriseBeans().getMessageDestination().iterator(); 
            msgDestIter.hasNext(); ) {
            MessageDestination msgDest = msgDestIter.next();
            if (msgDest.getMessageDestinationName().equals(msgDestRefLink)) {
                return msgDest.getJndiName();
            }
        }
        return null;
    }
    
    public void resolveWebservice(CMapNode node, String ejbName, String serviceEndpoint, 
            WebservicesDDJaxbHandler webservicesDD) {
        for (Iterator<Ejb> ejbIter = root.getEnterpriseBeans().getEjb().iterator();
            ejbIter.hasNext(); ) {
            Ejb ejb = ejbIter.next();
            for (Iterator<WebserviceEndpoint> wsEndpointIter = ejb.getWebserviceEndpoint().iterator();
                wsEndpointIter.hasNext(); ) {
                WebserviceEndpoint wsEndpoint = wsEndpointIter.next();
                String portCompName = wsEndpoint.getPortComponentName();
                // resolve using webservice.xml
                if (webservicesDD != null) {
                    webservicesDD.resolvePortCompName(node, portCompName);
                }
            }
        }
    }
}
