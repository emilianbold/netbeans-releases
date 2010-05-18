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
package org.netbeans.modules.vmd.api.model.common;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.openide.util.NbBundle;

import java.util.*;

/**
 * This class is a default implementation of the version descriptor interface. This implementation deletes the component
 * when it is incompatible with new abilities. The class can be extended and convertion methods could be overriden.
 *
 * @author David Kaspar
 */
public abstract class DefaultVersionDescriptor implements VersionDescriptor {

    private static final VersionDescriptor FOREVER_COMPATIBLE = new VersionDescriptor() {

        public boolean isCompatibleWith (VersionDescriptor descriptor) {
            return true;
        }

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
            htmlDisplayName = "#" + component.getComponentID (); // NOI18N
        return Collections.singleton (NbBundle.getMessage (DefaultVersionDescriptor.class, "MSG_Convertion_RemoveComponent", htmlDisplayName)); // NOI18N
    }

    /**
     * Creates a version descriptor that is is always compatible.
     * @return the "forever" version descriptor
     */
    public static VersionDescriptor createForeverCompatibleVersionDescriptor () {
        return FOREVER_COMPATIBLE;
    }

}
