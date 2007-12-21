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
package org.netbeans.modules.bpel.core.helper.api;

import java.net.URI;
import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;

/**
 * Helper to get access to resources related to a business process
 * @author Praveen
 * @author ads
 */
public interface BusinessProcessHelper {

    /**
     * Get a collection on WSDL files in the project.
     * @return Collection of fileobjects.
     */       
    public Collection<FileObject> getWSDLFilesInProject();

    /**
     * Get a collection on Schema files in the project.
     * @return Collection of fileobjects.
     */       
    public Collection<FileObject> getSchemaFilesInProject();
    
    /**
     * Get a collection on WSDL model from the URI.
     * @return WSDLModel.
     */       
    public WSDLModel getWSDLModelFromUri(URI uri);
    
    /**
     * Get the object model for the WSDL associated with this business process
     * @return WSDL Object Model
     */
    public WSDLModel getWSDLModel();

    /**
     * Get the absolute path for the WSDL associated with this business process
     * @return WSDL Object Model
     */    
    public String getWSDLFile();
    
    /**
     * Get the URI for the WSDL associated with this business process
     * @return WSDL file URI
     */    
    public URI getWSDLFileUri();    
}
