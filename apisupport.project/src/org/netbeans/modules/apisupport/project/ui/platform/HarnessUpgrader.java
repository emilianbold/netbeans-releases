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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Offers to upgrade old harnesses to the new version.
 * @author Jesse Glick
 * @see "issue #71630"
 */
class HarnessUpgrader {
    
    private HarnessUpgrader() {}
    
    public static void checkForUpgrade() {
        if (ModuleUISettings.getDefault().getHarnessesUpgraded()) {
            return;
        }
        ModuleUISettings.getDefault().setHarnessesUpgraded(true);
        final Set/*<NbPlatform>*/ toUpgrade = new HashSet();
        Iterator it = NbPlatform.getPlatforms().iterator();
        while (it.hasNext()) {
            NbPlatform p = (NbPlatform) it.next();
            if (p.isDefault() && !p.isValid()) {
                continue;
            }
            if (p.getHarnessVersion() >= NbPlatform.HARNESS_VERSION_50u1) {
                continue;
            }
            if (!p.getHarnessLocation().equals(p.getBundledHarnessLocation())) {
                // Somehow custom, forget it.
                continue;
            }
            toUpgrade.add(p);
        }
        if (!toUpgrade.isEmpty()) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    promptForUpgrade(toUpgrade);
                }
            });
        }
    }
    
    private static void promptForUpgrade(Set/*<NbPlatform>*/ platforms) {
        if (UIUtil.showAcceptCancelDialog(
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.title"),
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.text"),
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.upgrade"),
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.skip"),
                NotifyDescriptor.QUESTION_MESSAGE)) {
            try {
                doUpgrade(platforms);
            } catch (IOException e) {
                Util.err.notify(e);
            }
        }
    }
    
    private static void doUpgrade(Set/*<NbPlatform>*/ platforms) throws IOException {
        File defaultHarness = NbPlatform.getDefaultPlatform().getHarnessLocation();
        Iterator it = platforms.iterator();
        while (it.hasNext()) {
            NbPlatform p = (NbPlatform) it.next();
            p.setHarnessLocation(defaultHarness);
        }
    }
    
}
