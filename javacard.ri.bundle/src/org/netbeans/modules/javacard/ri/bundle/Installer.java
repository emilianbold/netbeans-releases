/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.ri.bundle;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.JavacardPlatformLocator;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class Installer extends ModuleInstall implements Runnable {
    private static final String ATTR = "isRiBundle302"; //NOI18N
    private static final String[] SUPERSEDED_ATTRS = new String[] { "isRiBundle" }; //NOI18N
    private static final String PLATFORM_DIRECTORY_NAME = "JCDK3.0.2_ConnectedEdition"; //NOI18N

    @Override
    public void restored() {
        //No need to block startup
        RequestProcessor.getDefault().post(this, 2000, Thread.MIN_PRIORITY);
    }

    public void run() {
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Installer.class,
                "MSG_CHECKING_RUNTIME")); //NOI18N
        final FileObject platformsFolder = Utils.sfsFolderForRegisteredJavaPlatforms();
        for (FileObject fo : platformsFolder.getChildren()) {
            if (Boolean.TRUE.equals(fo.getAttribute(ATTR))) {
                return;
            }
            //Try to clean up out-of-date or unsupported platforms and their 
            //metadata, since we cannot rely on uninstalled()
            for (String s : SUPERSEDED_ATTRS) {
                if (Boolean.TRUE.equals(fo.getAttribute(s))) {
                    cleanUp (fo);
                }
            }
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Installer.class,
                "MSG_UNPACKING_RUNTIME")); //NOI18N
        File sdk = InstalledFileLocator.getDefault().locate(PLATFORM_DIRECTORY_NAME,
                "org.netbeans.modules.javacard.ri.bundle", false); //NOI18N
        if (sdk != null && sdk.exists() && sdk.isDirectory()) {
            FileObject sdkFolder = FileUtil.toFileObject(FileUtil.normalizeFile(sdk));
            if (sdkFolder != null) {
                for (JavacardPlatformLocator l : Lookup.getDefault().lookupAll(JavacardPlatformLocator.class)) {
                    if (l.accept(sdkFolder)) {
                        String runtimeName = NbBundle.getMessage(Installer.class,
                                "BUNDLED_RUNTIME_NAME"); //NOI18N
                        try {
                            FileObject platform = l.install(sdkFolder, runtimeName);
                            assert platform != null : "Platform not created"; //NOI18N
                            platform.setAttribute(ATTR, Boolean.TRUE);
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                        return;
                    }
                }
            }
        }
    }

    private void cleanUp (FileObject platformFileObject) {
        //If there was an older version of the bundle, try to delete its
        //card files, so we do not end up with stale properties files
        //that cannot be read in directories we will try to use
        try {
            FileObject serversFolder = Utils.sfsFolderForDeviceConfigsForPlatformNamed(
                    platformFileObject.getName(), false);
            if (serversFolder != null) {
                serversFolder.delete();
            }
            FileObject eepromFolder = Utils.sfsFolderForDeviceEepromsForPlatformNamed(
                    platformFileObject.getName(), false);
            if (eepromFolder != null) {
                eepromFolder.delete();
            }
            platformFileObject.delete();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void uninstalled() {
        FileObject platformsFolder = Utils.sfsFolderForRegisteredJavaPlatforms();
        for (FileObject fo : platformsFolder.getChildren()) {
            if (Boolean.TRUE.equals(fo.getAttribute(ATTR))) {
                cleanUp (fo);
            }
        }
    }
}
