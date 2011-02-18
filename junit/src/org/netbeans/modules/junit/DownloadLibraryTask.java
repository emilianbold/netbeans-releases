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

package org.netbeans.modules.junit;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport.LibraryDefiner;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.junit.Bundle.*;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * #195123: prompt to load JUnit during startup.
 */
@ServiceProvider(service=Runnable.class, path="WarmUp")
public class DownloadLibraryTask implements Runnable {

    private static final String LIB_NAME = "junit_4";
    private static final String KEY = "tried.to.download.junit";

    @Messages({
        "download_title=Install JUnit Library",
        "download_question=Do you wish to download and install the JUnit testing library? Doing so is recommended for Java development, but it is not distributed with NetBeans."
    })
    public @Override void run() {
        Preferences p = NbPreferences.forModule(DownloadLibraryTask.class);
        if (p.getBoolean(KEY, false)) {
            // Only check once (i.e. on first start for a fresh user dir).
            return;
        }
        p.putBoolean(KEY, true);
        if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(download_question(), download_title(), NotifyDescriptor.OK_CANCEL_OPTION)) == NotifyDescriptor.OK_OPTION) {
            perhapsDownload();
        }
    }

    public static void perhapsDownload() {
        if (LibraryManager.getDefault().getLibrary(LIB_NAME) == null) {
            for (LibraryDefiner definer : Lookup.getDefault().lookupAll(LibraryDefiner.class)) {
                Callable<Library> c = definer.missingLibrary(LIB_NAME);
                if (c != null) {
                    try {
                        c.call();
                    } catch (Exception x) {
                        Logger.getLogger(DownloadLibraryTask.class.getName()).log(Level.INFO, "Could not install JUnit library", x);
                    }
                }
            }
        }
    }

}
