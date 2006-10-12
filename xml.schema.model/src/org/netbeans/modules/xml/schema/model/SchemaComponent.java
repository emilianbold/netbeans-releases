/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.model;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This interface represents a common interface shared by all schema elements.
 * @author Chris Webster
 */
public interface SchemaComponent extends DocumentComponent<SchemaComponent> {

    // TODO Should there be a resolve capability and expose uri for references
    public static final String ANNOTATION_PROPERTY = "annotation";
    public static final String ID_PROPERTY = "id";
    
    /**
     * @return the schema model this component belongs to.
     */
    SchemaModel getModel();
    
    /**
     * @return schema component 'id' attribute if presents, null otherwise.
     */
    String getId();
    
    /**
     * Set the schema component 'id' attribute value.
     */
    void setId(String id);
    
    /**
     * Returns value of an attribute defined in a certain namespace.
     */
    String getAnyAttribute(QName attributeName);
    
    /**
     * Sets value of an attribute defined in a certain namespace.
     * Propery change event will be fired with property name using attribute local name.
     */
    void setAnyAttribute(QName attributeName, String value);
    
    /**
     **/
    public Annotation getAnnotation();
    
    /**
     **/
    public void setAnnotation(Annotation annotation);
    
    /**
     * Visitor providing
     */
    void accept(SchemaVisitor visitor);
    
    /**
     * @return true if the elements are from the same schema model.
     */
    boolean fromSameModel(SchemaComponent other);
    
    /**
     * Returns the type of the component in terms of the schema model interfaces
     *
     */
    Class<? extends SchemaComponent> getComponentType();
	
    /**
     * Creates a global reference to the given target Schema component.
     * @param referenced the schema component being referenced.
     * @param type actual type of the target
     * @return the reference.
     */
    <T extends ReferenceableSchemaComponent> NamedComponentReference<T> createReferenceTo(T referenced, Class<T> type);
}
