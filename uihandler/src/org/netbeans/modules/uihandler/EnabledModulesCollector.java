/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Implementation of deactivator with sends one log with a list
 * of enabled modules.
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.uihandler.api.Deactivated.class)
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
                SpecificationVersion specVersion = m.getSpecificationVersion();
                if (specVersion != null){
                    enabledNames[i++]  = m.getCodeName() + " [" + specVersion.toString() + "]";
                }else{
                    enabledNames[i++] = m.getCodeName();
                }
            }
            rec.setParameters(enabledNames);
            rec.setLoggerName(uiLogger.getName());
            rec.setResourceBundle(NbBundle.getBundle(EnabledModulesCollector.class));
            rec.setResourceBundleName(EnabledModulesCollector.class.getPackage().getName()+".Bundle");
            uiLogger.log(rec);
        }
        if (!newDisabled.isEmpty()) {
            LogRecord rec = new LogRecord(Level.CONFIG, "UI_DISABLED_MODULES");
            String[] disabledNames = new String[newDisabled.size()];
            int i = 0;
            for (ModuleInfo m : newDisabled) {
                SpecificationVersion specVersion = m.getSpecificationVersion();
                if (specVersion != null){
                    disabledNames[i++]   = m.getCodeName() + " [" + specVersion.toString() + "]";
                }else{
                    disabledNames[i++] = m.getCodeName();
                }
            }
            rec.setParameters(disabledNames);
            rec.setLoggerName(uiLogger.getName());
            rec.setResourceBundle(NbBundle.getBundle(EnabledModulesCollector.class));
            rec.setResourceBundleName(EnabledModulesCollector.class.getPackage().getName()+".Bundle");
            uiLogger.log(rec);
        }
        
        previouslyEnabled = enabled;
        previouslyDisabled = disabled;
    }
    
}
