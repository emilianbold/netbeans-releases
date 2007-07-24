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

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.general.RootCD;

/**
 * @author David Kaspar
 */
public final class MidpVersionable {

    public static final Versionable MIDP = new Versionable() {

        public boolean isCompatibleWith (Versionable versionable) {
            return versionable == MIDP  ||  versionable == MIDP_1  ||  versionable == MIDP_2;
        }

        public boolean isAvailable (DesignDocument document) {
            if (document == null)
                return false;
            DesignComponent root = document.getRootComponent ();
            if (root == null)
                return false;
            PropertyValue version = root.readProperty (RootCD.PROP_VERSION);
            String string = MidpTypes.getString (version);
            return string != null  &&  string.startsWith (RootCD.VALUE_MIDP_PREFIX);
        }

    };

    public static final Versionable MIDP_1 = new Versionable() {

        public boolean isCompatibleWith (Versionable versionable) {
            return versionable == MIDP  ||  versionable == MIDP_1;
        }

        public boolean isAvailable (DesignDocument document) {
            if (document == null)
                return false;
            DesignComponent root = document.getRootComponent ();
            if (root == null)
                return false;
            PropertyValue version = root.readProperty (RootCD.PROP_VERSION);
            String string = MidpTypes.getString (version);
            return RootCD.VALUE_MIDP_1_0.equals (string);
        }

    };

    public static final Versionable MIDP_2 = new Versionable () {

        public boolean isCompatibleWith (Versionable versionable) {
            return versionable == MIDP  ||  versionable == MIDP_2;
        }

        public boolean isAvailable (DesignDocument document) {
            if (document == null)
                return false;
            DesignComponent root = document.getRootComponent ();
            if (root == null)
                return false;
            PropertyValue version = root.readProperty (RootCD.PROP_VERSION);
            String string = MidpTypes.getString (version);
            return RootCD.VALUE_MIDP_2_0.equals (string);
        }

    };

}
