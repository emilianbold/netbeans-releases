/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
