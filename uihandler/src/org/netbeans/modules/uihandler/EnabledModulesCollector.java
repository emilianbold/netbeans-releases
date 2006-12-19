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

package org.netbeans.modules.uihandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Implementation of deactivator with sends one log with a list
 * of enabled modules.
 *
 */
public class EnabledModulesCollector implements Deactivated {
    private List<ModuleInfo> previouslyEnabled = Collections.emptyList();
    private List<ModuleInfo> previouslyDisabled = Collections.emptyList();
    
    
    /** Creates a new instance of EnabledModulesCollector */
    public EnabledModulesCollector() {
    }

    public void deactivated(Logger uiLogger) {
        List<ModuleInfo> enabled = new ArrayList<ModuleInfo>();
        List<ModuleInfo> disabled = new ArrayList<ModuleInfo>();
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.isEnabled()) {
                enabled.add(m);
            } else {
                disabled.add(m);
            }
        }

        List<ModuleInfo> newEnabled = new ArrayList<ModuleInfo>(enabled);
        newEnabled.removeAll(previouslyEnabled);
        List<ModuleInfo> newDisabled = new ArrayList<ModuleInfo>(disabled);
        newDisabled.removeAll(previouslyDisabled);
        
        if (!newEnabled.isEmpty()) {
            LogRecord rec = new LogRecord(Level.CONFIG, "UI_ENABLED_MODULES");
            String[] enabledNames = new String[newEnabled.size()];
            int i = 0;
            for (ModuleInfo m : newEnabled) {
                enabledNames[i++] = m.getCodeName();
            }
            rec.setParameters(enabledNames);
            rec.setLoggerName(uiLogger.getName());
            rec.setResourceBundle(NbBundle.getBundle(EnabledModulesCollector.class));
            uiLogger.log(rec);
        }
        if (!newDisabled.isEmpty()) {
            LogRecord rec = new LogRecord(Level.CONFIG, "UI_DISABLED_MODULES");
            String[] disabledNames = new String[newDisabled.size()];
            int i = 0;
            for (ModuleInfo m : newDisabled) {
                disabledNames[i++] = m.getCodeName();
            }
            rec.setParameters(disabledNames);
            rec.setLoggerName(uiLogger.getName());
            rec.setResourceBundle(NbBundle.getBundle(EnabledModulesCollector.class));
            uiLogger.log(rec);
        }
        
        previouslyEnabled = enabled;
        previouslyDisabled = disabled;
    }
    
}
