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

import java.util.Collection;
import java.util.Set;

/**
 * This inteface defines a version compatibility and convertions.
 *
 * @author David Kaspar
 */
// TODO - partial semantic clash with Versionable interface
public interface VersionDescriptor {

    /**
     * Return whether this version descriptor is compatible with abilities.
     * @param abilities the collection of abilities
     * @return true, if compatible
     */
    public boolean isCompatibleWith (Collection<String> abilities);

    /**
     * Return a set of warning/error messages for notifying an user about conversion changes.
     * This method is called before convertComponent method is called on any component in a document.
     * @param component the component
     * @param oldAbilities the collection of old abilities
     * @param newAbilities the collection of new abilities
     * @return a set of messages
     */
    // TODO - change this signature
    public Set<String> getPreliminaryConvertMessages (DesignComponent component, Collection<String> oldAbilities, Collection<String> newAbilities);

    /**
     * Convert a component.
     * This method is called after getPreliminaryConvertMessages method is called on all components in a document.
     * @param component the component
     * @param oldAbilities the collection of old abilities
     * @param newAbilities the collection of new abilities
     */
    // TODO - how to deal with deleting of unusable components
    public void convertComponent (DesignComponent component, Collection<String> oldAbilities, Collection<String> newAbilities);

}
