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

package org.netbeans.modules.autoupdate.pluginimporter.libinstaller;

import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressHandle;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.autoupdate.pluginimporter.libinstaller.Bundle.*;

/**
 * Defines JUnit 3.x/4.x libraries by downloading their defining modules.
 */
public class JUnitLibraryInstaller {
    private static final String JUNIT_LIB = "org.netbeans.modules.junitlib"; // NOI18N
    private static final String JUNIT_MODULE = "org.netbeans.modules.junit"; // NOI18N
    private static final Logger LOG = Logger.getLogger(JUnitLibraryInstaller.class.getName());

    @Messages({
        "download_title=Install JUnit Library",
        "download_question=Do you wish to download and install JUnit testing library now? Doing so is recommended for Java development, but JUnit is not distributed with NetBeans.",
        "accept_button=Download and Install JUnit",
        "download_handle=Downloading JUnit",
        "validate_handle=Installing JUnit",
        "install_handle=Installing JUnit"
    })
    
    public static void install(boolean silent) {
        Map<String, UpdateUnit> modules = findModules(JUNIT_LIB, JUNIT_MODULE);
        if (modules.size() < 2) { // UC haven't downloaded yet
            // May be first start, when no update lists have yet been downloaded.
            LOG.finer("May be first start, when no update lists have yet been downloaded.");
            for (UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(true)) {
                try {
                    p.refresh(null, true);
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "While refreshing " + p + " thrown " + ex, ex);
                }
            }
            modules = findModules(JUNIT_LIB, JUNIT_MODULE);
        }
        // check if JUnit module installed
        UpdateUnit jUnitMod = modules.get(JUNIT_MODULE);
        if (jUnitMod == null) {
            LOG.fine("No " + JUNIT_MODULE + " found. Probably a network problem.");
            return ;
        }
        if (jUnitMod.getInstalled() == null) {
            LOG.fine(JUNIT_MODULE + " didn't installed, no needed to install " + JUNIT_LIB);
            return ;
        }
        // check if JUnit library available
        UpdateUnit jUnitLib = modules.get(JUNIT_LIB);
        if (jUnitLib == null) {
            LOG.fine("No " + JUNIT_LIB + " found. Probably a network problem.");
            return ;
        }
        if (jUnitLib.getInstalled() != null) {
            LOG.fine(JUNIT_LIB + " already installed, no needed to install again.");
            return ;
        }
        assert ! jUnitLib.getAvailableUpdates().isEmpty() : "Updates found for " + jUnitLib;
        if (jUnitLib.getAvailableUpdates().isEmpty()) {
            LOG.info("No updates found for " + jUnitLib);
            return ;
        }
        // make install container
        OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
        UpdateElement jUnitElement = jUnitLib.getAvailableUpdates().get(0);
        if (!oc.canBeAdded(jUnitLib, jUnitElement)) {
            LOG.info("Could not add " + jUnitElement + " to updates");
            return ;
        }
        for (UpdateElement req : oc.add(jUnitElement).getRequiredElements()) {
            oc.add(req);
        }
        if (silent) {
            try {
                // download
                LOG.fine("Try to download " + jUnitElement);
                ProgressHandle downloadHandle = ProgressHandleFactory.createHandle (download_handle());
                Validator validator = oc.getSupport().doDownload(downloadHandle, true);
                // install
                ProgressHandle validateHandle = ProgressHandleFactory.createHandle (validate_handle());
                Installer installer = oc.getSupport().doValidate(validator, validateHandle);
                LOG.fine("Try to install " + jUnitElement);
                ProgressHandle installHandle = ProgressHandleFactory.createHandle (install_handle());
                Restarter restarter = oc.getSupport().doInstall(installer, installHandle);
                assert restarter == null : "Not need to restart while installing " + jUnitLib;
                LOG.fine("Done " + jUnitElement);
            } catch (OperationException ex) {
                LOG.log(Level.INFO, "While installing " + jUnitLib + " thrown " + ex, ex);
            }
        } else {
            Confirmation question = new NotifyDescriptor.Confirmation(
                                            download_question(),
                                            download_title(),
                                            NotifyDescriptor.OK_CANCEL_OPTION);
            question.setOptions(new Object[] {accept_button(), NotifyDescriptor.CANCEL_OPTION});
            if (DialogDisplayer.getDefault().notify(question) == accept_button()) {
                if (! PluginManager.openInstallWizard(oc)) {
                    LOG.info("user canceled JUnit install wizard");
                }
            } else {
                LOG.info("user denied JUnit installation");
            }
        }
    }

    private static Map<String, UpdateUnit> findModules(String... codeNames) {
        Collection<String> names = Arrays.asList(codeNames);
        Map<String, UpdateUnit> res = new HashMap<String, UpdateUnit>();
        for (UpdateUnit unit : UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE)) {
            if (names.contains(unit.getCodeName())) {
                res.put(unit.getCodeName(), unit);
                if (res.size() == names.size()) {
                    return res;
                }
            }
        }
        return res;
    }

}
