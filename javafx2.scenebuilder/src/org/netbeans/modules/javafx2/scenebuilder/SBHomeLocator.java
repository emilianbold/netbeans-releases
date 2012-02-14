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
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Locates and validates a SceneBuilder installation
 * @author Jaroslav Bachorik
 */
public class SBHomeLocator {
    private static final String VER_CURRENT = "1.0"; // NOI18N
    private static final String PROPERTIES_FILE = "scenebuilder.properties"; // NOI18N
    
    private static final HomeLocator WINDOWS_HOME_LOCATOR = new HomeLocator() {
        final private static String WKIP = "C:\\Program Files\\Oracle\\JavaFX Scene Builder " + VER_CURRENT; // NOI18N
        final private static String WKIP_MIX = "C:\\Program Files (x86)\\Oracle\\JavaFX Scene Builder " + VER_CURRENT; // NOI18N
        
        @Override
        public Home locateHome() {
            Home h = getHomeForPath(WKIP);
            if (h == null) {
                h = getHomeForPath(WKIP_MIX);
            }
            return h;
        }
    };
    private static final HomeLocator MAC_HOME_LOCATOR = new HomeLocator() {
        final private static String WKIP = "/Applications/JavaFX Scene Builder 1.0.app/Contents/Resources/SceneBuilder"; // NOI18N
        @Override
        public Home locateHome() {
            return getHomeForPath(WKIP);
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
    
    private static Home getHomeForPath(String path) {
        File installDir = new File(path);
        if (installDir != null && installDir.exists() && installDir.isDirectory()) {
            FileObject installDirFO = FileUtil.toFileObject(installDir);

            FileObject propertiesFO = installDirFO.getFileObject("bin/" + PROPERTIES_FILE); // NOI18N
            if (propertiesFO != null && propertiesFO.isValid() && propertiesFO.isData()) {
                try {
                    Properties props = new Properties();
                    props.load(new FileReader(FileUtil.toFile(propertiesFO)));
                    return new Home(path, props.getProperty("version", "1.0")); // NOI18N
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
