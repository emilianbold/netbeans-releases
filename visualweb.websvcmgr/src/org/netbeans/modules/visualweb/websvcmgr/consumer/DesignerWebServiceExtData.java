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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.websvcmgr.consumer;

import java.util.Map;

/**
 *
 * @author quynguyen
 */
public class DesignerWebServiceExtData {
    
    private Map<String, Map<String, String>> portToDataProviderMap;
    private Map<String, String> portToProxyBeanNameMap;
    
    public DesignerWebServiceExtData() {
    }
    
    public Map<String, Map<String, String>> getPortToDataProviderMap() {
        return portToDataProviderMap;
    }
    
    public void setPortToDataProviderMap(Map<String, Map<String, String>> portToDataProviderMap) {
        this.portToDataProviderMap = portToDataProviderMap;
    }
    
    public Map<String, String> getPortToProxyBeanNameMap() {
        return portToProxyBeanNameMap;
    }
    
    public void setPortToProxyBeanNameMap(Map<String, String> portToProxyBeanNameMap) {
        this.portToProxyBeanNameMap = portToProxyBeanNameMap;
    }
}
