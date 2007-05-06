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

package org.netbeans.modules.hudson.impl;

import org.netbeans.modules.hudson.api.HudsonVersion;

/**
 * Implementation of the HudsonVersion
 *
 * @author Michal Mocnak
 */
public class HudsonVersionImpl implements HudsonVersion {
    
    public static final HudsonVersionImpl SUPPORTED_VERSION = new HudsonVersionImpl("1.101");
    
    private int major;
    private int minor;
    
    public HudsonVersionImpl(String version) {
        this.major = parseMajor(version);
        this.minor = parseMinor(version);
    }
    
    public int getMajorVersion() {
        return major;
    }
    
    public int getMinorVersion() {
        return minor;
    }
    
    private int parseMajor(String version) {
        int result = 0;
        
        try {
            // Get major version as string
            String s = version.substring(0, version.indexOf("."));
            
            // Convert to integer
            result = Integer.parseInt(s);
        } catch (IndexOutOfBoundsException e) {
            // Nothing
        } catch (NumberFormatException e) {
            // Nothing
        } catch (NullPointerException e) {
            // Nothing
        }
        
        return result;
    }
    
    private int parseMinor(String version) {
        int result = 0;
        
        try {
            // Get minor version as string
            String s = (version.indexOf("-") == -1) ? version.substring(version.indexOf(".") + 1) :
                version.substring(version.indexOf(".") + 1, version.indexOf("-"));
            
            // Convert to integer
            result = Integer.parseInt(s);
        } catch (IndexOutOfBoundsException e) {
            // Nothing
        } catch (NumberFormatException e) {
            // Nothing
        } catch (NullPointerException e) {
            // Nothing
        }
        
        return result;
    }
    
    public String toString() {
        return getMajorVersion() + "." + getMinorVersion();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof HudsonVersionImpl))
            return false;
        
        HudsonVersionImpl oV = (HudsonVersionImpl) o;
        
        return getMajorVersion() == oV.getMajorVersion() && getMinorVersion() == oV.getMinorVersion();
    }
    
    public int compareTo(HudsonVersion o) {
        if (this.equals(o))
            return 0;
        
        return (getMajorVersion() < o.getMajorVersion()) ? -1 :
            (getMajorVersion() > o.getMajorVersion()) ? 1 :
                (getMinorVersion() < o.getMinorVersion()) ? -1 :
                    (getMinorVersion() > o.getMinorVersion()) ? 1 : 0;
    }
}