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
package org.netbeans.modules.vmd.api.model;

/**
 * Register an implementation of this interface into global lookup (META-INF/services/org.netbeans.modules.vmd.api.model.PrimitiveDescriptorFactory file).
 * Registered factories are called for creating PrimitiveDescriptors for a specified string ids.
 * <p>
 * This descriptor is used just for (de)serialization of value with TypeID.Kind == PRIMITIVE.
 *
 * @author David Kaspar
 */
public interface PrimitiveDescriptorFactory {

    /**
     * The project type which the primitive descriptor factory is related to.
     * @return the related project type
     */
    public String getProjectType ();

    /**
     * Factory method for creating PrimitiveDescriptors. It is called while a document is (de)serialized.
     * @param string the string part of TypeID
     * @return the primitive descriptor
     */
    public PrimitiveDescriptor getDescriptorForTypeIDString (String string);

}
