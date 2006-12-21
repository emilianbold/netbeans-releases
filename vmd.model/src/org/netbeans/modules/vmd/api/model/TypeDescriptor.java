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
 * This immutable class describes the type hierarchy used in component descriptor.
 * <p>
 * It holds information about super type id, this type id, instantiate ability, derive ability.
 *
 * @author David Kaspar
 */
public final class TypeDescriptor {

    private final TypeID superType;
    private final TypeID thisType;
    private final boolean canInstantiate;
    private final boolean canDerive;

    /**
     * Creates a new type descriptor used in component descriptor.
     * @param superType the type id of super/parent component descriptor
     * @param thisType the type id of this component descriptor
     * @param canInstantiate true if this component descriptor can be instantiate (a new component could be created from this component descriptor)
     * @param canDerive true if the component descriptor is not final (other component descriptor could take this one as its super component descriptor)
     */
    public TypeDescriptor (TypeID superType, TypeID thisType, boolean canInstantiate, boolean canDerive) {
        assert thisType != null;
        this.superType = superType;
        this.thisType = thisType;
        this.canInstantiate = canInstantiate;
        this.canDerive = canDerive;
    }

    /**
     * Returns a type id of a super component descriptor.
     * @return the super type id
     */
    public TypeID getSuperType () {
        return superType;
    }

    /**
     * Returns a type id of this component descriptor.
     * @return this type id
     */
    public TypeID getThisType () {
        return thisType;
    }

    /**
     * Returns an instantiate ability.
     * @return true if a new component could be created from this component descriptor
     */
    public boolean isCanInstantiate () {
        return canInstantiate;
    }

    /**
     * Returns a derive ability.
     * @return true if other component descriptor can use this one as their super.
     */
    public boolean isCanDerive () {
        return canDerive;
    }

}
