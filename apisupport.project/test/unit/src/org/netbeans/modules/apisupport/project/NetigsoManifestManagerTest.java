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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * Test functionality of ManifestManager when it sees OSGi bundles.
 *
 * @author Jaroslav Tulach
 */
public class NetigsoManifestManagerTest extends TestBase {

    public NetigsoManifestManagerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }
    
    public void testFriends() throws Exception {
        File manifest = new File(getWorkDir(), "testManifest.mf");
        String mfContent = "Manifest-Version: 1.0\n" +
                "Ant-Version: Apache Ant 1.6.5\n" +
                "Created-By: 1.4.2_10-b03 (Sun Microsystems Inc.)\n" +
                "Bundle-SymbolicName: org.netbeans.modules.sendopts\n" +
                "OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/sendopts/Bundle.properties\n" +
                "Bundle-Version: 1.9\n" +
                "Export-Package: a, b, c\n" +
                "Bundle-RequireExecutionEnvironment: J2SE-1.3\n" +
                "OpenIDE-Module-Layer: org/netbeans/modules/sendopts/layer.xml\n";
        dump(manifest, mfContent);
        ManifestManager mm = ManifestManager.getInstance(manifest, true);
        assertEquals("cnb", "org.netbeans.modules.sendopts", mm.getCodeNameBase());
        assertEquals("version", "1.9", mm.getSpecificationVersion());
        assertEquals("layer", "org/netbeans/modules/sendopts/layer.xml", mm.getLayer());
        assertEquals("Three packages", 3, mm.getPublicPackages().length);
        assertEquals("a", mm.getPublicPackages()[0].getPackage());
        assertEquals("b", mm.getPublicPackages()[1].getPackage());
        assertEquals("c", mm.getPublicPackages()[2].getPackage());
        assertFalse("not recursivea", mm.getPublicPackages()[0].isRecursive());
        assertFalse("not recursiveb", mm.getPublicPackages()[1].isRecursive());
        assertFalse("not recursivec", mm.getPublicPackages()[2].isRecursive());
    }
    
}
