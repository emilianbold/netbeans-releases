/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */


package org.netbeans.installer.utils.helper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Lipin
 */
public class JavaCompatibleProperties {
    private String minVersion;
    private String maxVersion;
    private String vendor;
    private String osName;
    private String osArch;
    
    public JavaCompatibleProperties() {        
    }
    public JavaCompatibleProperties(String minVersion, String maxVersion, String vendor,  String osName, String osArch) {        
        setMinVersion(minVersion);
        setMaxVersion(maxVersion);
        setVendor(vendor);
        setOsName(osName);
        setOsArch(osArch);
    }
    public String getMinVersion() {
        return minVersion;
    }
    
    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }
    
    public String getMaxVersion() {
        return maxVersion;
    }
    
    public void setMaxVersion(String maxVersion) {
        this.maxVersion = maxVersion;
    }
    
    public String getVendor() {
        return vendor;
    }
    
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    
    public String getOsName() {
        return osName;
    }
    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }
    public String toString() {
        String all = "";
        if(minVersion!=null) {
            all += "<min version=" + minVersion + "> ";
        }
        if(maxVersion!=null) {
            all += "<max version=" + maxVersion + "> ";
        }
        if(vendor!=null) {
            all += "<vendor=" + vendor + "> ";
        }        
        if(osArch!=null) {
            all += "<os arch=" + osArch + "> ";
        }
        if(osName!=null) {
            all += "<os name=" + osName + "> ";
        }
        return all.trim();
    }
}
