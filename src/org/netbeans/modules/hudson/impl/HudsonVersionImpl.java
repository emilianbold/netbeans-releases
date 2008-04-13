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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.hudson.api.HudsonVersion;

/**
 * Implementation of the HudsonVersion
 *
 * @author Michal Mocnak
 */
public class HudsonVersionImpl implements HudsonVersion {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\b.*");
    
    private final int major;
    private final int minor;
    
    public HudsonVersionImpl(String version) {
        Matcher m = VERSION_PATTERN.matcher(version);
        if(!m.matches())
            throw new IllegalArgumentException(version);
        this.major = Integer.parseInt(m.group(1));
        this.minor = Integer.parseInt(m.group(2));
    }
    
    public int getMajorVersion() {
        return major;
    }
    
    public int getMinorVersion() {
        return minor;
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