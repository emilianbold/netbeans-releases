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
        FEATURES: for (FeatureInfo fi : FeatureManager.features()) {
            if (!fi.isPresent()) {
                continue;
            }
            String prefCnb = fi.getFeatureCodeNameBase();
            if (prefCnb == null) {
                continue;
            }
            Set<String> justKits = new HashSet<String>();
            justKits.addAll(fi.getCodeNames());
            String name = "fod." + prefCnb; // NOI18N
            ModuleInfo preferred = null;
            Collection<? extends ModuleInfo> allModules = Lookup.getDefault().lookupAll(ModuleInfo.class);
            for (ModuleInfo mi : allModules) {
                if (prefCnb.equals(mi.getCodeNameBase())) {
                    preferred = mi;
                }
                if ("true".equals(mi.getAttribute("AutoUpdate-Show-In-Client"))) { // NOI18N
                    continue;
                }
                justKits.remove(mi.getCodeNameBase());
            }
            if (preferred == null) {
                FoDFileSystem.LOG.warning("For cluster " + fi.clusterName + " there is prefCnb " + prefCnb +
                        " but the module is not found " + preferred + "\nList of modules is: ");
                FeatureManager.dumpModules(Level.INFO, Level.INFO);
                continue;
            }
            Object desc = preferred.getLocalizedAttribute("OpenIDE-Module-Long-Description"); // NOI18N
            if (!(desc instanceof String)) {
                desc = preferred.getLocalizedAttribute("OpenIDE-Module-Short-Description"); // NOI18N
            }
            if (!(desc instanceof String)) {
                desc = preferred.getDisplayName();
            }

            UpdateItem feature = UpdateItem.createFeature(
                name,
                preferred.getSpecificationVersion().toString(),
                justKits,
                preferred.getDisplayName(),
                (String)desc,
                null
            );
            res.put(name, feature);
        }
        return res;
    }
    
    public boolean refresh (boolean force) throws IOException {
        return true;
    }

}
