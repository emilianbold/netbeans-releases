/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

import java.util.List;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.DocumentComponent;
import org.netbeans.modules.xml.xam.GlobalReference;

/**
 *
 * @author rico
 * Base interface of all WSDL components
 */
public interface WSDLComponent extends DocumentComponent<WSDLComponent> {
    public static final String DOCUMENTATION_PROPERTY = "documentation"; //NOI18N
    public static final String EXTENSIBILITY_ELEMENT_PROPERTY = "extensibilityElement";

    void accept(WSDLVisitor visitor);
    
    void setDocumentation(Documentation doc);
    Documentation getDocumentation();
    
    WSDLModel getWSDLModel();
    
    /**
     * Creates a global reference to the given target WSDL component.
     * @param target the target WSDLComponent
     * @param type actual type of the target
     * @return the global reference.
     */
    <T extends ReferenceableWSDLComponent> GlobalReference<T> createReferenceTo(T target, Class<T> type);
    
    /**
     * Creates a GlobalReference to a Schema component
     * @param target The schema component that is being referenced.
     * @param type Class object of the schema component
     */
    <T extends ReferenceableSchemaComponent> GlobalReference<T> 
            createSchemaReference(T target, Class<T> type);

    void addExtensibilityElement(ExtensibilityElement ee);
    void removeExtensibilityElement(ExtensibilityElement ee);
    List<ExtensibilityElement> getExtensibilityElements();
    
    <T extends ExtensibilityElement> List<T> getExtensibilityElements(Class<T> type);
}
