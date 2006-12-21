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
package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;

import java.util.Collection;
import java.util.Set;

/**
 * @author David Kaspar
 */
public final class MidpVersionDescriptor implements VersionDescriptor {

    public static final VersionDescriptor FOREVER = new MidpVersionDescriptor (); // TODO
    public static final VersionDescriptor MIDP = new MidpVersionDescriptor (); // TODO
    public static final VersionDescriptor MIDP_2 = new MidpVersionDescriptor (); // TODO

    private MidpVersionDescriptor () {
    }

    public boolean isCompatibleWith (Collection<String> abilities) {
        return false; // TODO
    }

    // TODO - change this signature
    public Set<String> getPreliminaryConvertMessages (DesignComponent component, Collection<String> oldAbilities, Collection<String> newAbilities) {
        return null; // TODO
    }

    // TODO - how to deal with deleting of unusable components
    public void convertComponent (DesignComponent component, Collection<String> oldAbilities, Collection<String> newAbilities) {
        // TODO
    }

}
