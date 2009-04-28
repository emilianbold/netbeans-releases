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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ide.ergonomics.fod;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/** Feature On Demand Unit Provider.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=UpdateProvider.class)
public class FoDUpdateUnitProvider implements UpdateProvider {
    private static final Logger log = Logger.getLogger (FoDUpdateUnitProvider.class.getName ()); // NOI18N

    public FoDUpdateUnitProvider() {
    }

    public String getName () {
        return "fod-provider";
    }

    public String getDisplayName () {
        return null;
    }


    public String getDescription() {
        return null;
    }

    public CATEGORY getCategory() {
        return CATEGORY.STANDARD;
    }

    public Map<String, UpdateItem> getUpdateItems () throws IOException {
        Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
        Collection<? extends ModuleInfo> all = Lookup.getDefault().lookupAll(ModuleInfo.class);
        Set<ModuleInfo> notYetProcessed = new HashSet<ModuleInfo>(all);
        FEATURES: for (FeatureInfo fi : FeatureManager.features()) {
            if (!fi.isPresent()) {
                continue;
            }
            if (registerFeature(
                all, fi.getCodeNames(),
                fi.getFeatureCodeNameBase(), res, notYetProcessed
            )) {
                continue;
            }
        }
        Set<String> rest = new HashSet<String>();
        String restPilot = null;
        for (ModuleInfo mi : notYetProcessed) {
            if (showInAU(mi)) {
                if (restPilot == null) {
                    restPilot = mi.getCodeNameBase();
                }
                rest.add(mi.getCodeNameBase());
            }
        }
        registerFeature(all, rest, restPilot, res, notYetProcessed);

        return res;
    }
    
    public boolean refresh (boolean force) throws IOException {
        return true;
    }

    private boolean registerFeature(
        Collection<? extends ModuleInfo> allModules,
        Set<String> codeNames, String prefCnb, Map<String, UpdateItem> res,
        Set<ModuleInfo> processed
    ) {
        Set<String> justKits = new HashSet<String>();
        justKits.addAll(codeNames);
        String name = "fod." + prefCnb; // NOI18N
        ModuleInfo preferred = null;
        StringBuffer description = new StringBuffer();
        for (ModuleInfo mi : allModules) {
            if (prefCnb != null && prefCnb.equals(mi.getCodeNameBase())) {
                preferred = mi;
            }
            if (justKits.contains(mi.getCodeNameBase())) {
                processed.remove(mi);
                if (showInAU(mi)) {
                    StringBuilder sb = new StringBuilder();
                    Object desc = mi.getLocalizedAttribute("OpenIDE-Module-Long-Description"); // NOI18N
                    if (!(desc instanceof String)) {
                        desc = mi.getLocalizedAttribute("OpenIDE-Module-Short-Description"); // NOI18N
                    }
                    if (desc instanceof String) {
                        sb.append("<h5>").append(mi.getDisplayName()).append("</h5>"); // NOI18N
                        sb.append(desc);
                        sb.append("\n"); // NOI18N
                    }
                    if (preferred == mi) {
                        description.insert(0, sb);
                    } else {
                        description.append(sb);
                    }
                    continue;
                }
                justKits.remove(mi.getCodeNameBase());
            }
        }
        if (preferred == null) {
            return true;
        }
        Object featureName = preferred.getLocalizedAttribute("OpenIDE-Module-Display-Category"); // NOI18N
        if (!(name instanceof String)) {
            featureName = preferred.getDisplayName();
        }
        UpdateItem feature = UpdateItem.createFeature(name, preferred.getSpecificationVersion().toString(), justKits, (String) featureName, description.toString(), null);
        res.put(name, feature);
        return false;
    }

    private static boolean showInAU(ModuleInfo mi) {
        return "true".equals(mi.getAttribute("AutoUpdate-Show-In-Client")); // NOI18N
    }
}
