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

package org.netbeans.upgrade.systemoptions;

import java.util.Map;


class DefaultResult implements Result {
    private Map<String, String> m;
    private String instanceName;
    private String moduleName;    
    DefaultResult(String instanceName, Map<String, String> m) {
        this.instanceName = instanceName;
        this.m = m;
    }
    public String getProperty(final String propName) {
        return m.get(propName);
    }
    
    public String[] getPropertyNames() {
        return m.keySet().toArray(new String[m.size()]);
    }
    
    public String getInstanceName() {
        return instanceName;
    }
    public String getModuleName() {
        return moduleName;
    }    
    public void setModuleName(String aModuleName) {
        moduleName = aModuleName;
    }        
}    