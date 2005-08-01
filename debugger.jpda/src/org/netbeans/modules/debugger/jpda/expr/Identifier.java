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

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ObjectReference;

/**
 * A simple bean encapsulating information needed to resolve a given name (or identifier). The identifier may
 * be resolved in an instance, type or local context.
 *
 * @author Maros Sandor
 */
class Identifier {

    ReferenceType   typeContext;
    ObjectReference instanceContext;
    String          superQualifier;
    String          identifier;
    boolean         localContext;

    Identifier(boolean localContext, ObjectReference instanceContext, String identifier) {
        this.localContext = localContext;
        this.instanceContext = instanceContext;
        if (instanceContext != null) {
            this.typeContext = instanceContext.referenceType();
        }
        this.identifier = identifier;
    }

    Identifier(boolean localContext, ObjectReference objectReference, ReferenceType typeContext, String identifier) {
        this.localContext = localContext;
        this.instanceContext = objectReference;
        this.typeContext = typeContext;
        this.identifier = identifier;
    }

    Identifier(boolean localContext, ReferenceType typeContext, String identifier) {
        this.localContext = localContext;
        this.typeContext = typeContext;
        this.identifier = identifier;
    }

    Identifier(ObjectReference objectReference, String identifier, String superQualifier) {
        this.instanceContext = objectReference;
        this.typeContext = instanceContext.referenceType();
        this.identifier = identifier;
        this.superQualifier = superQualifier;
    }

    Identifier(ReferenceType typeContext, String identifier) {
        this.typeContext = typeContext;
        this.identifier = identifier;
    }
}
