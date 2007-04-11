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

package org.netbeans.api.java.source;

import java.util.EventObject;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;

/**
 * Event used to notify the {@link ClassIndexListener} about
 * a change of declared types in the {@link ClassIndex}
 * @author Tomas Zezula
 */
public final class TypesEvent extends EventObject {
    
    private final Iterable<? extends ElementHandle<TypeElement>> types;
    
    TypesEvent (final ClassIndex source, final Iterable<? extends ElementHandle<TypeElement>> types) {
        super (source);
        assert types != null;
        this.types = types;
    }
    
    /**
     * Returns the affected declared types.
     * @return an {@link Iterable} of {@link TypeElement} handles
     */
    public Iterable<? extends ElementHandle<TypeElement>> getTypes () {
        return this.types;
    }
    
    @Override
    public String toString () {
        return String.format("TypesEvent [%s]", this.types.toString());
    }
    
}
