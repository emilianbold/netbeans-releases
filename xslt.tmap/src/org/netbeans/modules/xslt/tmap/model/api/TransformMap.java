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
package org.netbeans.modules.xslt.tmap.model.api;

import java.util.List;
import org.netbeans.modules.xslt.tmap.model.api.events.VetoException;
import org.netbeans.modules.xslt.tmap.model.impl.TMapComponents;

/**
 * 
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface TransformMap extends TMapComponent {

    TMapComponents TYPE = TMapComponents.TRANSFORM_MAP;

    /**
     * targetNamespace attribute name.
     */
    String TARGET_NAMESPACE = "targetNamespace"; // NOI18N

    /**
     * Gets the value of the targetNamespace property.
     * 
     * @return possible object is {@link String }
     */
    String getTargetNamespace();

    /**
     * Sets the value of the targetNamespace property.
     * 
     * @param value
     *            allowed object is {@link String }
     * @throws VetoException {@link VetoException}
     *             will be thrown if <code>value</code> if not acceptable as
     *             targetNamespace attribute here.
     */
    void setTargetNamespace(String value) throws VetoException;

    /**
     * @return imports children for this stylesheet.
     * Note that resulting collection is unmodifiable. 
     */
    List<Import> getImports();
    
    /**
     * Add new import <code>impt</code> element at <code>position</code>. 
     * @param impt new import element.
     * @param position position for new element.
     */
    void addImport(Import impt);

    /**
     * Append new import element.
     * @param impt new import child element for appending.
     */
    void addImport(Import impt, int position);
      
    /**
     * Removes existing <code>impt</code> import child element.
     * @param impt import child element.
     */
    void removeImport(Import impt);

    /**
     * @return size of "imports" children.
     */
    int getSizeOfImports();
    
    List<Service> getServices();

    void removeService(Service service);

    void addService(Service service);

    int getSizeOfServices();
}
