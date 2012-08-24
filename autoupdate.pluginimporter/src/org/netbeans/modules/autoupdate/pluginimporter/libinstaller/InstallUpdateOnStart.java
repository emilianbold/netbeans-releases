/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.autoupdate.pluginimporter.libinstaller;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.modules.OnStart;

@OnStart
public class InstallUpdateOnStart implements Runnable {

    private static final Logger LOG = Logger.getLogger(InstallUpdateOnStart.class.getName());
    
    public @Override
    void run() {
        // only if IDE is running in silent mode
        if ("true".equalsIgnoreCase(System.getProperty("netbeans.close"))) { // NOI18N
            LOG.fine("Call Check for updates...");
            try {
                checkAndInstallUpdates();
            } catch (OperationException ex) {
                LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
            LOG.fine("Call Install JUnit...");
            new InstallLibraryTask().run();
        }
    }

    private void checkAndInstallUpdates() throws OperationException {
        Collection<UpdateUnit> updates = new HashSet<UpdateUnit>();

        // The first start, when no update lists have yet been downloaded.
        LOG.finer("The first start, when no update lists have yet been downloaded.");
        for (UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(true)) {
            try {
                p.refresh(null, true);
            } catch (IOException ex) {
                LOG.log(Level.INFO, "While refreshing " + p + " thrown " + ex, ex);
            }
        }
        
        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);

        // check for updates
        for (UpdateUnit unit : units) {
            if (unit.getInstalled() != null) {
                if (! unit.getAvailableUpdates().isEmpty()) {
                    updates.add(unit);
                }
            }
        }

        LOG.info("Found updates: " + updates);
        if (! updates.isEmpty()) {
            // make install container
            OperationContainer<OperationSupport> oc = OperationContainer.createForDirectUpdate();
            for (UpdateUnit unit : updates) {
                assert ! unit.getAvailableUpdates().isEmpty() : "Available updates found for " + unit;
                if (oc.canBeAdded(unit, unit.getAvailableUpdates().get(0))) {
                    LOG.fine("  ... update " + unit.getInstalled() + " -> " + unit.getAvailableUpdates().get(0));
                    OperationInfo<OperationSupport> info = oc.add(unit.getAvailableUpdates().get(0));
                    if (info != null) {
                        Set<UpdateElement> requiredElements = info.getRequiredElements();
                        LOG.fine("      ... add required elements: " + requiredElements);
                        oc.add(requiredElements);
                    }
                }
            }
            assert oc.listInvalid().isEmpty() : "listInvalid elements is empty.";
            assert ! oc.listAll().isEmpty() : "listAll elements is not empty";
            if (oc.listInvalid().isEmpty() && ! oc.listAll().isEmpty()) {
                LOG.fine("Try to invoke the installation...");
                Restarter restart = oc.getSupport().doOperation(ProgressHandleFactory.createHandle (InstallUpdateOnStart.class.getName()));
                LOG.info("... Restarter found? " + (restart != null));
                assert restart == null;
                if (restart != null) {
                    LOG.fine("Try to restart...");
                    oc.getSupport().doRestart(restart, ProgressHandleFactory.createHandle (InstallUpdateOnStart.class.getName()));
                }
            }
            LOG.fine("Update done.");
        }
    }
    
}
