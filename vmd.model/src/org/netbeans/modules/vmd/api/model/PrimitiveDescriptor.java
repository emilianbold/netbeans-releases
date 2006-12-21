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
 * This interface is used for describing (de)serialization of a design-time primitives (for TypeID.Kind == PRIMITIVE).
 * For registering the implemenation see PrimitiveDescriptorFactory interface.
 * <p>
 * This descriptor is used just for (de)serialization of value with TypeID.Kind == PRIMITIVE.
 *
 * @author David Kaspar
 */
public interface PrimitiveDescriptor {

    /**
     * Returns an encoded string value of an original value.
     * @param value the original value
     * @return the encoded string value
     */
    public String serialize (Object value);

    /**
     * Returns a decoded value from an encoded string value.
     * @param serialized the encoded string value
     * @return the decoded value
     */
    public Object deserialize (String serialized);

    /**
     * Checks whether an object is valid value.
     * @param object the object to validate
     * @return true if valid
     */
    public boolean isValidInstance (Object object);

}
