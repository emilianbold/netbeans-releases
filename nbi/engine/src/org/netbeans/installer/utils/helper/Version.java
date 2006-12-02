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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Kirill Sorokin
 */
public class Version {
    private long major;
    private long minor;
    private long micro;
    private long update;
    private long build;
    
    private String string;
    
    public Version(String versionString) {
        if (versionString.matches("([0-9]+[\\._\\-]+)*[0-9]+")) {
            String[] split = versionString.split("[\\._\\-]+"); //NOI18N
            
            if (split.length > 0) {
                major = new Long(split[0]);
            }
            if (split.length > 1) {
                minor = new Long(split[1]);
            }
            if (split.length > 2) {
                micro = new Long(split[2]);
            }
            if (split.length > 3) {
                update = new Long(split[3]);
            }
            if (split.length > 4) {
                build = new Long(split[4]);
            }
        } else {
            string = versionString;
        }
    }
    
    public boolean equals(Version version) {
        if (string == null) {
            return ((major == version.getMajor()) && (minor == version.getMinor()) && (micro == version.getMicro()) && (update == version.getUpdate() && (build == version.getBuild()))) ? true : false;
        } else {
            return string.equals(version.toString());
        }
    }
    
    public boolean newerThan(Version version) {
        if (string == null) {
            if (major > version.getMajor()) {
                return true;
            } else if (major == version.getMajor()) {
                if (minor > version.getMinor()) {
                    return true;
                } else if (minor == version.getMinor()) {
                    if (micro > version.getMicro()) {
                        return true;
                    } else if (micro == version.getMicro()) {
                        if (update > version.getUpdate()) {
                            return true;
                        } else if (update == version.getBuild()) {
                            if (build > version.getBuild()) {
                                return true;
                            }
                        }
                    }
                }
            }
        } else {
            if (string.compareTo(version.toString()) > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean newerOrEquals(Version version) {
        if (newerThan(version) || equals(version)) {
            return true;
        }
        
        return false;
    }
    
    public boolean olderThan(Version version) {
        if (!newerOrEquals(version)) {
            return true;
        }
        
        return false;
    }
    
    public boolean olderOrEquals(Version version) {
        if (!newerThan(version)) {
            return true;
        }
        
        return false;
    }
    
    public long getMajor() {
        return major;
    }
    
    public long getMinor() {
        return minor;
    }
    
    public long getMicro() {
        return micro;
    }
    
    public long getUpdate() {
        return update;
    }
    
    public long getBuild() {
        return build;
    }
    
    public String toString() {
        if (string == null) {
            return "" + major + "." + minor + "." + micro + "." + update + "." + build;
        } else {
            return string;
        }
    }
    
    public String toMajor() {
        return "" + major;
    }
    
    public String toMinor() {
        return "" + major + "." + minor;
    }
    
    public String toMicro() {
        return "" + major + "." + minor + "." + micro;
    }
    
    public String toJdkStyle() {
        return "" + major + "." + minor + "." + micro + (update != 0 ? "_" + (update < 10 ? "0" + update : update) : "");
    }
}
