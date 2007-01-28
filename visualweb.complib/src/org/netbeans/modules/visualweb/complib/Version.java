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

package org.netbeans.modules.visualweb.complib;

/**
 * Represents a generic Version object
 *
 * @author Edwin Goei
 */
public class Version implements Comparable<Version> {

    private int major;

    private int minor;

    private int micro;

    /**
     * @param major
     * @param minor
     * @param micro
     */
    public Version(int major, int minor, int micro) {
        if (major < 0 || minor < 0 || micro < 0) {
            throw new IllegalArgumentException(
                    "No arguments must be less than 0");
        }
        this.major = major;
        this.minor = minor;
        this.micro = micro;
    }

    /**
     * Format: "major.minor[.micro]", for example "2.1.3". Major and minor parts
     * are required but micro is optional.
     *
     * @param versionString
     */
    public Version(String versionString) {
        String[] parts = versionString.split("\\.");
        try {
            major = Integer.parseInt(parts[0]);
            if (major < 0) {
                throw new IllegalArgumentException(
                        "Bad version format, required major version < 0: "
                                + versionString);
            }
            minor = Integer.parseInt(parts[1]);
            if (minor < 0) {
                throw new IllegalArgumentException(
                        "Bad version format, required minor version < 0: "
                                + versionString);
            }

            if (parts.length > 2) {
                micro = Integer.parseInt(parts[2]);
                if (micro < 0) {
                    throw new IllegalArgumentException(
                            "Bad version format, optional micro version < 0: "
                                    + versionString);
                }
            } else {
                // Make version "X.Y" equal "X.Y.0" for any valid X or Y
                micro = 0;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad version format "
                    + versionString);
        }
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Version) {
            Version anotherVersion = (Version) anObject;
            if (getMajor() == anotherVersion.getMajor()
                    && getMinor() == anotherVersion.getMinor()) {
                return (getMicro() == anotherVersion.getMicro()) ? true : false;
            }
            return false;
        }
        return false;
    }

    public int hashCode() {
        return getMajor() + getMinor() + getMicro();
    }

    /**
     * Version as a String with an optional micro value if set.
     */
    public String toString() {
        String val = major + "." + minor;
        if (micro != 0) {
            val += "." + micro;
        }
        return val;
    }

    public int getMajor() {
        return major;
    }

    public int getMicro() {
        return micro;
    }

    public int getMinor() {
        return minor;
    }

    public int compareTo(Version o) {
        int sgn = new Integer(major).compareTo(o.major);
        if (sgn != 0) {
            return sgn;
        }

        sgn = new Integer(minor).compareTo(o.minor);
        if (sgn != 0) {
            return sgn;
        }

        return new Integer(micro).compareTo(o.micro);
    }
}
