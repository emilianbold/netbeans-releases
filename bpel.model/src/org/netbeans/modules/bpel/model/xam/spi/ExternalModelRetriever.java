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

/**
 *
 */
package org.netbeans.modules.bpel.model.xam.spi;

import java.util.Collection;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;


/**
 * @author ads
 *
 * This interface will be implemented by services that search
 * avaliable models.
 *
 * This is designed as service for possibility to write unit tests.
 * One can implement this interface and this implementation
 * will search models in location that needs for unit tests.
 */
public interface ExternalModelRetriever {

    /**
     * Accessor to list of availible WSDL models.
     * @param model Bpel model for which WSDL models will be searched.
     * @param namespace Namespace of searched WSDL model.
     * @return Collection of found WSDL models. 
     */
    Collection<WSDLModel> getWSDLModels( BpelModel model , String namespace );
    
    /**
     * Accessor to list of availible Schema models.
     * @param model Bpel model for which Schema models will be searched.
     * @param namespace Namespace of searched  Schema model.
     * @return Collection of found Schema models. 
     */
    Collection<SchemaModel> getSchemaModels( BpelModel model , String namespace );
}
