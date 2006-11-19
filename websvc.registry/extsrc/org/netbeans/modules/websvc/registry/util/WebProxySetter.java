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

package org.netbeans.modules.websvc.registry.util;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author  Winston Prakash
 */
public class WebProxySetter {
    
    private final Integer MANUAL_SET_PROXY = new Integer(2);
    private final String PROXY_HOST = "http.proxyHost"; // NOI18N
    private final String PROXY_PORT = "http.proxyPort"; // NOI18N
    
    private static WebProxySetter defaultInstance = new WebProxySetter();
    
    // Use Preferences instead of IDESettings for proxy config etc.
    // will be properly persisted in NbPreferences.
    private static Preferences proxySettingsNode;
    
    private WebProxySetter() {
        proxySettingsNode = NbPreferences.root ().node ("/org/netbeans/core");
        assert proxySettingsNode != null;
    }
    
    public static WebProxySetter getInstance(){
        return defaultInstance;
    }
    
    /** Gets Proxy Host */
    public String getProxyHost() {
        return proxySettingsNode.get ("proxyHttpHost", "");
    }

    /** Gets Proxy Port */
    public String getProxyPort() {
        return proxySettingsNode.get ("proxyHttpPort", "");
   }

    /** Sets the whole proxy configuration */
    public void setProxyConfiguration(String host, String port ) {
        proxySettingsNode.putInt ("proxyType", MANUAL_SET_PROXY.intValue ());
        proxySettingsNode.put ("proxyHttpHost", host);
        proxySettingsNode.put ("proxyHttpPort", port);
        // ??? Is it need anymore?
        System.setProperty(PROXY_HOST, host);
        System.setProperty(PROXY_PORT, port);
    }
}
