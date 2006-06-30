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
