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

package org.netbeans.modules.j2ee.dd.impl.webservices.annotation;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;

/**
 *
 * @author mkuchtiak
 */
public class WebservicesMetadataImpl implements WebservicesMetadata {

    private final Webservices root;

    public WebservicesMetadataImpl(Webservices root) {
        this.root = root;
    }

    public Webservices getRoot() {
        return root;
    }
    
    public WebserviceDescription findWebserviceByName(String wsName) {
        WebserviceDescription[] webServices = root.getWebserviceDescription();
        if (webServices != null) {
            for (WebserviceDescription ws : webServices) {
                if (wsName.equals(ws.getWebserviceDescriptionName())) {
                    return ws;
                }
            }
        }
        return null;
    }
}
