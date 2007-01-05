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

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Vidhya Narayanan
 * @author rico
 *
 * Represents global references. Provides additional information for referenced elements
 * such as its broken state and its changeability.
 */
public class GlobalReferenceImpl<T extends ReferenceableSchemaComponent> extends AbstractNamedComponentReference<T>
        implements NamedComponentReference<T> {
    
    //factory uses this
    public GlobalReferenceImpl(T target, Class<T> cType, SchemaComponentImpl parent) {
        super(target, cType, parent);
    }
    
    //used by resolve methods
    public GlobalReferenceImpl(Class<T> classType, SchemaComponentImpl parent, String refString){
        super(classType, parent, refString);
    }

    public T get() {
        if (getReferenced() == null) {
            String namespace = getQName().getNamespaceURI();
            namespace = namespace.length() == 0 ? null : namespace;
            String localName = getLocalName();
            T target = ((SchemaComponentImpl)getParent()).getModel().resolve(namespace, localName, getType());
            setReferenced(target);
        }
        return getReferenced();
    }
    
    public SchemaComponentImpl getParent() {
        return (SchemaComponentImpl) super.getParent();
    }
    
    public String getEffectiveNamespace() {
        return getParent().getModel().getEffectiveNamespace(get());
    }
}
