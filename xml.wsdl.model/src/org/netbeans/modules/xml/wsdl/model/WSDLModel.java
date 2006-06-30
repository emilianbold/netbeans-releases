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

package org.netbeans.modules.xml.wsdl.model;

import org.netbeans.modules.xml.xam.DocumentModel;

/**
 *
 * @author rico
 *  This interface represents an instance of a wsdl model. A wsdl model is
 * bound to a single file.
 */
public interface WSDLModel extends DocumentModel<WSDLComponent> {
    /**
     * @return WSDL model root component 'definitions'
     */
    Definitions getDefinitions();

    /**
     * @return WSDL component factory.
     */
    WSDLComponentFactory getFactory();

    /**
     * Returns WSDLModel having specified target namespace.
     * The search recursively includes all imported models.
     * @param namespaceURL the target namespace to search for model
     * @return WSDL model if found; else null.
     */
    WSDLModel findWSDLModel(String namespaceURL);
    
    /**
     * Find named WSDL component by name and type within current model.
     * @param name target component attribute value of 'name'
     * @param type type of target component
     * @return WSDL component of specified type and name; null if not found.
     */
    <T extends ReferenceableWSDLComponent> T findComponentByName(String name, Class<T> type);
    
    /**
     * Find named WSDL component by name and type.
     * @param name target component attribute value of 'name'
     * @param type type of target component
     * @param global if true lookup also find in imported models, else only locally in this model.
     * @return WSDL component of specified type and name; null if not found.
     */
    <T extends ReferenceableWSDLComponent> T findComponentByName(String name, Class<T> type, boolean global);
}
