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
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.propertyeditors.resolver;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;

/**
 * Defines a resolver responsible for determining a property editor suitable for
 * editing a property, as described by a property descriptor. Implementing classes
 * may use any information in the property descriptor to determine an appropriate
 * editor.
 *
 * <p>Typically, the IDE will provide at least one implementation. Implementations
 * are made available using the service provider interface. Within the module or
 * library JAR, the file
 * {@code META-INF/services/com.sun.rave.propertyeditors.resolver.PropertyEditorResolver}
 * contains the name of the Java class that implements {@link PropertyEditorResolver}.
 * Each implementation discovered during the service provider lookup will be asked,
 * in turn, to provide an editor. The first editor returned is used. If an 
 * implementation is provided via a module, it may be given a numbered
 * priority, which will guarantee that it is invoked before any resolvers supplied by
 * the IDE.
 */
public interface PropertyEditorResolver {
    
    /**
     * Returns a property editor suitable for editing the property described by
     * the property descriptor specified. If the resolver does not know of a suitable
     * editor, returns null.
     */
    public PropertyEditor getEditor(PropertyDescriptor propertyDescriptor);
    
}
