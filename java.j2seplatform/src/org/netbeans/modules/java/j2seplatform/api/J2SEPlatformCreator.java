/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.j2seplatform.api;

import java.io.IOException;
import java.util.Map;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.wizard.NewJ2SEPlatform;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Creates a new platform definition.
 * @since 1.11
 */
public class J2SEPlatformCreator {

    private J2SEPlatformCreator() {}

    /**
     * Create a new J2SE platform definition.
     * @param installFolder the installation folder of the JDK
     * @return the newly created platform
     * @throws IOException if the platform was invalid or its definition could not be stored
     */
    public static JavaPlatform createJ2SEPlatform(FileObject installFolder) throws IOException {
        NewJ2SEPlatform plat = NewJ2SEPlatform.create(installFolder);
        plat.run();
        if (!plat.isValid()) {
            throw new IOException("Invalid J2SE platform in " + installFolder); // NOI18N
        }
        String displayName = createPlatformDisplayName(plat);
        String antName = createPlatformAntName(displayName);
        plat.setDisplayName(displayName);
        plat.setAntName(antName);
        FileObject platformsFolder = Repository.getDefault().getDefaultFileSystem().findResource(
                "Services/Platforms/org-netbeans-api-java-Platform"); // NOI18N
        assert platformsFolder != null;
        DataObject dobj = PlatformConvertor.create(plat, DataFolder.findFolder(platformsFolder), antName);
        return dobj.getLookup().lookup(JavaPlatform.class);
    }

    private static String createPlatformDisplayName(JavaPlatform plat) {
        Map<String, String> m = plat.getSystemProperties();
        String vmVersion = m.get("java.specification.version"); // NOI18N
        StringBuffer displayName = new StringBuffer("JDK "); // NOI18N
        if (vmVersion != null) {
            displayName.append(vmVersion);
        }
        return displayName.toString();
    }

    private static String createPlatformAntName(String displayName) {
        assert displayName != null && displayName.length() > 0;
        String antName = PropertyUtils.getUsablePropertyName(displayName);
        if (platformExists(antName)) {
            String baseName = antName;
            int index = 1;
            antName = baseName + Integer.toString(index);
            while (platformExists(antName)) {
                index ++;
                antName = baseName + Integer.toString(index);
            }
        }
        return antName;
    }

    /**
     * Checks if the platform of given antName is already installed
     */
    private static boolean platformExists(String antName) {
        assert antName != null && antName.length() > 0;
        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            String otherName = p.getProperties().get("platform.ant.name");  // NOI18N
            if (antName.equals(otherName)) {
                return true;
            }
        }
        return false;
    }

}
