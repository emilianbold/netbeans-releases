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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ServiceRefGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;


/**
 * @author Peter Williams
 */
public class WebServiceView extends DDSectionNodeView {

    private final Set<WebserviceDescription> webServiceCache = new HashSet<WebserviceDescription>();

    public WebServiceView(SunDescriptorDataObject dataObject) {
        super(dataObject);
        
        if(!(rootDD instanceof SunWebApp || rootDD instanceof SunEjbJar || rootDD instanceof SunApplicationClient)) {
            throw new IllegalArgumentException("Data object is not a root that contains webservice elements (" + rootDD + ")");
        }
        
        // web apps and ejb jars support web services.
        boolean hasWebServices = (rootDD instanceof SunWebApp || rootDD instanceof SunEjbJar);
        // ejb jars show the clients under the ejb node so hide them here.
        boolean hasGlobalClients = (rootDD instanceof SunWebApp || rootDD instanceof SunApplicationClient);
        
        LinkedList<SectionNode> children = new LinkedList<SectionNode>();
        if(hasWebServices) {
            children.add(new WebServiceGroupNode(this, rootDD, version));
        }
        if(hasGlobalClients) {
            children.add(new ServiceRefGroupNode(this, rootDD, version));
        }
        
        setChildren(children);
    }
    
}
