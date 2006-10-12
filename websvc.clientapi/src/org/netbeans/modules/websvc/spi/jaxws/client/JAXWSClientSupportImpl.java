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

package org.netbeans.modules.websvc.spi.jaxws.client;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Peter Williams
 */
public interface JAXWSClientSupportImpl {
    
    public static final String XML_RESOURCES_FOLDER="xml-resources"; //NOI18N
    public static final String CLIENTS_LOCAL_FOLDER="web-service-references"; //NOI18N
    public static final String CATALOG_FILE="catalog.xml"; //NOI18N
    
    
    /** returns ERL string for local wsdl copy downloaded from wsdlUrl
     * 
     */
    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109);
    
    public FileObject getWsdlFolder(boolean create) throws IOException;
    
    /**
     *  return folder for local wsdl artifacts
     */
    public FileObject getLocalWsdlFolderForClient(String clientName, boolean createFolder);
    
    /**
     *  return folder for local wsdl bindings
     */
    public FileObject getBindingsFolderForClient(String clientName, boolean createFolder);

    public void removeServiceClient(String serviceName);
    
    public List getServiceClients();

    public URL getCatalog();
    
    public String getServiceRefName(Node clientNode);

}
