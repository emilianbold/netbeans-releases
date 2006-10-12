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

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;

/**
 *
 * @author rico
 *  This interface represents an instance of a wsdl model. A wsdl model is
 * bound to a single file.
 */
public abstract class WSDLModel extends AbstractDocumentModel<WSDLComponent> implements Referenceable {
    
    protected WSDLModel(ModelSource source) {
        super(source);
    }
    
    /**
     * @return WSDL model root component 'definitions'
     */
    public abstract Definitions getDefinitions();
    
    /**
     * @return WSDL component factory.
     */
    public abstract WSDLComponentFactory getFactory();
    
    /**
     * Search from all imported WSDL models those with specified target namespace.
     * @param namespaceURI the target namespace to search for model
     * @return list WSDL models or empty list if none found.
     */
    public abstract List<WSDLModel> findWSDLModel(String namespaceURI);
    
    /**
     * Search for all schemas visible from imported/included/redefined in the 
     * schema extensibility elements.  Schema model imported through wsdl:import
     * are also in the search.
     * @param namespaceURI the target namespace to search for model
     * @return list of schema match the give namespace.
     */
    public abstract List<Schema> findSchemas(String namespaceURI);
    
    /**
     * Find named WSDL component by name and type within current model.
     * @param name local name of target component
     * @param type type of target component
     * @return WSDL component of specified type and name; null if not found.
     */
    public abstract <T extends ReferenceableWSDLComponent> T findComponentByName(String name, Class<T> type);
    
    /**
     * Find named WSDL component by QName and type.
     * @param name QName of the target component.
     * @param type type of target component
     * @return WSDL component of specified type and name; null if not found.
     */
    public abstract <T extends ReferenceableWSDLComponent> T findComponentByName(QName name, Class<T> type);
}
