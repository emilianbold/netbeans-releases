/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
