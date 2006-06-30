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
