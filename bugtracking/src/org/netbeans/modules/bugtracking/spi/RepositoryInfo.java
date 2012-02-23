/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.spi;

import java.util.*;
import java.util.prefs.Preferences;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;

/**
 *
 * @author Tomas Stupka
 */
public final class RepositoryInfo {
    
    static {
        SPIAccessorImpl.createAccesor();
    }
        
    private static final String DELIMITER         = "<=>";                      // NOI18N    
    
    private final Map<String, String> map = new HashMap<String, String>();
    
    private static final String PROPERTY_ID = "id";                             // NOI18N    
    private static final String PROPERTY_CONNECTOR_ID = "connectorId";          // NOI18N    
    private static final String PROPERTY_URL = "url";                           // NOI18N    
    private static final String PROPERTY_DISPLAY_NAME = "displayName";          // NOI18N    
    private static final String PROPERTY_TOOLTIP = "tooltip";                   // NOI18N    
    private static final String PROPERTY_USERNAME = "username";                 // NOI18N    
    private static final String PROPERTY_HTTP_USERNAME = "httpUsername";        // NOI18N    

    private RepositoryInfo(Map<String, String> properties) {
        this.map.putAll(properties);
    }

    public RepositoryInfo(String id, String connectorId, String url, String displayName, String tooltip, String user, String httpUser, char[] password, char[] httpPassword) {
        map.put(PROPERTY_ID, id);
        map.put(PROPERTY_CONNECTOR_ID, connectorId);
        map.put(PROPERTY_DISPLAY_NAME, displayName);
        map.put(PROPERTY_TOOLTIP, tooltip);
        map.put(PROPERTY_URL, url);
        map.put(PROPERTY_USERNAME, user);
        map.put(PROPERTY_HTTP_USERNAME, httpUser);
        storePasswords(password, httpPassword);
    }

    public String getDisplayName() {
        return map.get(PROPERTY_DISPLAY_NAME);
    }

    public char[] getHttpPassword() {
        if(isNbRepository()) {
            return new char[0];
        } else {
            return BugtrackingUtil.readPassword(null, "http", getHttpUsername(), getUrl()); // NOI18N
        }
    }

    public String getId() {
        return map.get(PROPERTY_ID);
    }
    
    public String getConnectorId() {
        return map.get(PROPERTY_CONNECTOR_ID);
    }
    
    public String getUrl() {
        return map.get(PROPERTY_URL);
    }

    public char[] getPassword() {
        if(isNbRepository()) {
            return BugtrackingUtil.getNBPassword();
        } else {
            return BugtrackingUtil.readPassword(null, null, getUsername(), getUrl());
        }
    }

    public String getTooltip() {
        return map.get(PROPERTY_TOOLTIP);
    }

    public String getUsername() {
        return map.get(PROPERTY_USERNAME);
    }

    public String getHttpUsername() {
        return map.get(PROPERTY_HTTP_USERNAME);
    }
    
    public String getValue(String key) {
        return map.get(key);
    }
    
    public void putValue(String key, String value) {
        map.put(key, value);
    }

    static RepositoryInfo read(Preferences preferences, String key) {
        String str = preferences.get(key, "");                                  // NOI18N    
        if(str.equals("")) {                                                    // NOI18N    
            return null;
        }
        Map<String, String> m = fromString(str);
        return new RepositoryInfo(m);
    }
    
    void store(Preferences preferences, String key) {
        boolean isNetbeans = isNbRepository();
        preferences.put(key, getStringValue(isNetbeans));
        if(isNetbeans) {
            BugtrackingUtil.saveNBUsername(getUsername());
        }
    }
    
    private String getStringValue(boolean dropUser) {
        List<String> l = new ArrayList<String>(map.keySet());
        Collections.sort(l);
        StringBuilder sb = new StringBuilder();
        sb.append(PROPERTY_ID);
        sb.append(DELIMITER);
        sb.append(map.get(PROPERTY_ID));
        for (String key : l) {
            if(!key.equals(PROPERTY_ID)) {
                sb.append(DELIMITER);
                sb.append(key);
                sb.append(DELIMITER);
                if(!(dropUser && key.equals(PROPERTY_USERNAME))) {
                    sb.append(map.get(key));
                }
            }
        }
        return sb.toString();
    }

    private static Map<String, String> fromString(String string) {
        String[] values = string.split(DELIMITER);
        Map<String, String> m = new HashMap<String, String>(); 
        for (int i = 0; i < values.length; i = i + 2) {
            String key = values[i];
            String value = i < values.length - 1 ? values[i + 1] : "";
            m.put(key, value);
        }
        return m;
    }   
    
    private void storePasswords(char[] password, char[] httpPassword) throws MissingResourceException {
        if(isNbRepository()) {
            BugtrackingUtil.saveNBPassword(password);
        } else {
            BugtrackingUtil.savePassword(password, null, getUsername(), getUrl());
            BugtrackingUtil.savePassword(httpPassword, "http", getHttpUsername(), getUrl()); // NOI18N
        }
    }

    private boolean isNbRepository() {
        String url = map.get(PROPERTY_URL);
        return url != null ? BugtrackingUtil.isNbRepository(url) : false;
    }
}
