/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.upgrade.systemoptions;

import java.util.*;

/**
 * Imports CVS root settings: external SSH command
 * 
 * @author Maros Sandor
 */
public class CvsSettingsProcessor extends PropertyProcessor {

    private final String FIELD_SEPARATOR = "<~>";
    
    public CvsSettingsProcessor() {
        super("org.netbeans.modules.versioning.system.cvss.settings.CvsRootSettings.PersistentMap");
    }

    void processPropertyImpl(String propertyName, Object value) {
        if ("rootsMap".equals(propertyName)) { // NOI18N
            List mapData = ((SerParser.ObjectWrapper) value).data;
            int n = 0;
            int idx = 3;
            if (mapData.size() > 3) {
                for (;;) {
                    if (idx + 2 > mapData.size()) break;
                    String root = (String) mapData.get(idx);
                    List rootData = ((SerParser.ObjectWrapper) mapData.get(idx + 1)).data;
                    try {
                        List extSettingsData = ((SerParser.ObjectWrapper) ((SerParser.NameValue) rootData.get(0)).value).data;
                        Boolean extRememberPassword = (Boolean) ((SerParser.NameValue) extSettingsData.get(0)).value;
                        Boolean extUseInternalSSH = (Boolean) ((SerParser.NameValue) extSettingsData.get(1)).value;
                        String extCommand = (String) ((SerParser.NameValue) extSettingsData.get(2)).value;
                        String extPassword = (String) ((SerParser.NameValue) extSettingsData.get(3)).value;
                        String setting = root + FIELD_SEPARATOR + extUseInternalSSH + FIELD_SEPARATOR + extRememberPassword + FIELD_SEPARATOR + extCommand;
                        if (extPassword != null && !extPassword.equals("null")) setting += FIELD_SEPARATOR + extPassword; 
                        addProperty("cvsRootSettings" + "." + n, setting);
                        n++;
                    } catch (Exception e) {
                        // the setting is not there => nothing to import
                    }
                    idx += 2;
                }
            }
        }  else {
            throw new IllegalStateException();
        }
    }
}
