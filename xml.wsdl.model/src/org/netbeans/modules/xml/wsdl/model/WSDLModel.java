/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
