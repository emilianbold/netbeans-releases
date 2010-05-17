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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xslt.tmap.model.spi;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;

/**
 * This interface will be implemented by services that search
 * avaliable models.
 *
 * This is designed as service for possibility to use ant commandline tasks.
 * One can implement this interface and this implementation
 * will search models in location that needs for ant tasks.
 *
 * @author Vitaly Bychkov
 */
public interface ExternalModelRetriever {

    /**
     * Accessor to list of availible WSDL models.
     * @param model TMap model for which WSDL models will be searched.
     * @param namespace Namespace of searched WSDL model.
     * @return Collection of found WSDL models. 
     */
    Collection<WSDLModel> getWSDLModels( TMapModel model,String namespace );
}
