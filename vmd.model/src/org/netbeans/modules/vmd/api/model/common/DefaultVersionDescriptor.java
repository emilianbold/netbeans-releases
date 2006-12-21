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
package org.netbeans.modules.vmd.api.model.common;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;

import java.util.*;

/**
 * This class is a default implementation of the version descriptor interface. This implementation deletes the component
 * when it is incompatible with new abilities. The class can be extended and convertion methods could be overriden.
 *
 * @author David Kaspar
 */
public abstract class DefaultVersionDescriptor implements VersionDescriptor {

    private static final VersionDescriptor FOREVER_COMPATIBLE = new VersionDescriptor() {
        public boolean isCompatibleWith (Collection<String> abilities) {
            return true;
        }

        public Set<String> getPreliminaryConvertMessages (DesignComponent component, Collection<String> oldAbilities, Collection<String> newAbilities) {
            return null;
        }

        public void convertComponent (DesignComponent component, Collection<String> oldAbilities, Collection<String> newAbilities) {
        }
    };

    private Kind kind;
    private String[] abilities;

    /**
     * The kind of the abilities checking.
     */
    public enum Kind {

        /**
         * If any ability of the version descriptor exists then version is resolved as compatible.
         */
        ANY,

        /**
         * Only when all abilities of the version descriptor exist then version is resolved as compatible.
         */
        ALL
    }

    /**
     * Creates a abstract implementation of default version descriptor.
     * @param kind the kind used for checking abilities
     * @param abilities the abilities of the version descriptor
     */
    public DefaultVersionDescriptor (Kind kind, String[] abilities) {
        assert kind != null  &&  abilities != null;
        this.kind = kind;
        this.abilities = abilities;
    }

    /**
     * Returns whether this version descriptor is compatible with abilities based on the kind and the abilities of the version descriptor.
     * @param abilities the collection of abilities
     * @return true, if compatible
     */
    public final boolean isCompatibleWith (String[] abilities) {
        if (kind == Kind.ALL) {
            return Arrays.asList (abilities).containsAll (Arrays.asList (this.abilities));
        } else {
            List<String> abilitiesList = Arrays.asList (abilities);
            for (String ability : this.abilities) {
                if (abilitiesList.contains (ability))
                    return true;
            }
            return false;
        }
    }

    /**
     * Return a set of warning/error messages for notifying an user about conversion changes.
     * The check is based on isCompatibleWith method and in the case that it is not compatible, it returns a message
     * about a component removing.
     * <p>
     * This method is called before convertComponent method is called on any component in a document.
     * @param component the component
     * @param oldAbilities the collection of old abilities
     * @param newAbilities the collection of new abilities
     * @return a set of messages
     */
    // TODO - change this signature
    public Set<String> getPreliminaryConvertMessages (DesignComponent component, Collection<String> oldAbilities, Collection<String> newAbilities) {
        if (isCompatibleWith (newAbilities))
            return null;
        String htmlDisplayName = InfoPresenter.getHtmlDisplayName (component);
        if (htmlDisplayName == null)
            htmlDisplayName = "#" + component.getComponentID ();
        return Collections.singleton ("Component " + htmlDisplayName + " is going to be removed because it is incompatible with the new project abilities.");
    }

    /**
     * Creates a version descriptor that is is always compatible.
     * @return the "forever" version descriptor
     */
    public static VersionDescriptor createForeverCompatibleVersionDescriptor () {
        return FOREVER_COMPATIBLE;
    }

}
