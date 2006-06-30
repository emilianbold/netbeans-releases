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
package org.netbeans.api.java.platform;

import org.openide.modules.SpecificationVersion;

/**
 * Represents profile installed in the Java SDK
 */
public class Profile {

    private String name;
    private SpecificationVersion version;

    /**
     * Creates new Profile
     * @param name of the profile, e.g. MIDP
     * @param version of the profile, e.g. 1.0
     */
    public Profile (String name, SpecificationVersion version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Returns the name of the profile
     * @return String
     */
    public final String getName () {
        return this.name;
    }

    /**
     * Returns the version of the profile
     * @return String
     */
    public final SpecificationVersion getVersion () {
        return this.version;
    }


    public int hashCode () {
        int hc = 0;
        if (name != null)
            hc = name.hashCode() << 16;
        if (version != null)
            hc += version.hashCode();
        return hc;
    }

    public boolean equals (Object other) {
        if (other instanceof Profile) {
            Profile op = (Profile) other;
            return this.name == null ? op.name == null : this.name.equals(op.name) &&
                   this.version == null ? op.version == null : this.version.equals (op.version);
        }
        else
            return false;
    }

    public String toString () {
        String str;
        str = this.name == null ? "" : this.name;
        str += " " + this.version == null ? "" : this.version.toString(); // NOI18N
        return str;
    }
}
