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
package org.netbeans.modules.javafx2.scenebuilder;

import java.io.File;
import org.netbeans.modules.javafx2.scenebuilder.utils.WinRegistry;
import org.openide.util.Utilities;

/**
 *
 * @author jbachorik
 */
public class SBHomeLocator {
    private static final HomeLocator WINDOWS_HOME_LOCATOR = new HomeLocator() {
        final private static String wellKnownPath = "C:\\Program Files\\Oracle\\Scene Builder";
        
        @Override
        public Home locateHome() {
            // 1. get current version
            String version = WinRegistry.getString("\"HKLM\\SOFTWARE\\JavaSoft\\JavaFX Scene Builder\" /v SBVersion");
            if (version == null) {
                version = "1.0";
            }
            // 1. registry
            String home = WinRegistry.getString("\"HKLM\\SOFTWARE\\Oracle\\JavaFX Scene Builder\\" + version + "\" /v Path");
            if (home == null) {
                // 2. well known location
                File h = new File(wellKnownPath);
                if (h.exists() && h.isDirectory()) {
                    home = wellKnownPath;
                }
            }
            
            return new Home(home, version);
        }
    };
    private static final HomeLocator MAC_HOME_LOCATOR = new HomeLocator() {

        @Override
        public Home locateHome() {
            throw new UnsupportedOperationException();
        }
    };
    private static final HomeLocator UX_HOME_LOCATOR = new HomeLocator() {

        @Override
        public Home locateHome() {
            throw new UnsupportedOperationException();
        }
    };
    
    public static HomeLocator getLocator() {
        if (Utilities.isWindows()) {
            return WINDOWS_HOME_LOCATOR;
        } else if (Utilities.isMac()) {
            return MAC_HOME_LOCATOR;
        } else {
            return UX_HOME_LOCATOR;
        }
    }
}
