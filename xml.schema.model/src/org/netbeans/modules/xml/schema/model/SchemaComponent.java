/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.DocumentComponent;
import org.netbeans.modules.xml.xam.GlobalReference;

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
    SchemaModel getSchemaModel();
    
    /**
     * @return schema component 'id' attribute if presents, null otherwise.
     */
    String getId();
    
    /**
     * Set the schema component 'id' attribute value.
     */
    void setId(String id);
    
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
     * @param target the target schema component
     * @param type actual type of the target
     * @return the global reference.
     */
    <T extends ReferenceableSchemaComponent> GlobalReference<T> createReferenceTo(T referenced, Class<T> type);
}
