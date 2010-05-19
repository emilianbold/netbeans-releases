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

package org.netbeans.modules.bpel.model.api;

import java.util.List;
import org.netbeans.modules.bpel.model.api.events.VetoException;

/**
 * This type is extended by other component types to allow elements and
 * attributes from other namespaces to be added.
 * <p>
 * Java class for tExtensibleElements complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 *  &lt;xsd:complexType name="tExtensibleElements">
 *       &lt;xsd:annotation>
 *           &lt;xsd:documentation>
 *               This type is extended by other component types to allow elements and attributes from
 *               other namespaces to be added at the modeled places.
 *           &lt;/xsd:documentation>
 *       &lt;/xsd:annotation>
 *       &lt;xsd:sequence>
 *           &lt;xsd:element ref="documentation" minOccurs="0" maxOccurs="unbounded"/>
 *           &lt;xsd:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *       &lt;/xsd:sequence>
 *       &lt;xsd:anyAttribute namespace="##other" processContents="lax"/>
 *   &lt;/xsd:complexType>
 * </pre>
 *
 * TODO: rename to ExtensibleElement
 */
public interface ExtensibleElements extends BpelContainer {

    /**
     * @return Documentation cildren.
     */
    Documentation[] getDocumentations();

    /**
     * Returns <code>i</code>-th Documentation element.
     * 
     * @param i Index in Documentation children array.
     * @return <code>i</code>-th Documentation child.
     */
    Documentation getDocumentation( int i );

    /**
     * Removes <code>i</code>-th Documentation child.
     * 
     * @param i Index in children array.
     */
    void removeDocumentation( int i );

    /**
     * Set new <code>documentation</code> element to the <code>i</code>-th
     * position.
     * 
     * @param documentation New Documentation child
     * @param i Index in children array.
     */
    void setDocumentation( Documentation documentation, int i );

    /**
     * Insert new <code>documentation</code> element to the <code>i</code>
     * position.
     * 
     * @param documentation
     *            New Documentation child
     * @param i Index in children array.
     */
    void insertDocumentation( Documentation documentation, int i );

    /**
     * Set new Documentation array .
     * 
     * @param documentations New array.
     */
    void setDocumentations( Documentation[] documentations );

    /**
     * Adds new <code>documentation</code> in the end of children list.
     * 
     * @param documentation New documentation element.
     */
    void addDocumentation( Documentation documentation );

    /**
     * @return size of children Documentation elements.
     */
    int sizeOfDocumentations();

    /**
     *
     */
    String getDocumentation();

    /**
     *
     */
    void setDocumentation(String documentation) throws VetoException;
    
    /**
     *
     */
    void removeDocumentation() throws VetoException;

    /**
     * Add extension entity to the end of children list with specified
     * type.
     * @param <T> Class of entity.
     * @param type extension entity class.
     * @param entity New child extension entity.
     */
    <T extends ExtensionEntity> void addExtensionEntity(Class<T> type, T entity );
    
    /**
     * @return All extension children.
     */
    List<ExtensionEntity> getExtensionChildren();
    
}
