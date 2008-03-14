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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.netbeans.ModuleInstaller;
import org.netbeans.Stamps;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest9 extends SetupHid {

    public NbInstallerTest9(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File workdir = getWorkDir();
        for (String jarname : new String[] {
            "little-manifest.jar",
            "medium-manifest.jar",
            "big-manifest.jar",
        }) {
            copy(new File(jars, jarname), new File(workdir, jarname));
        }
    }
    
    /** Test #26786/#28755: manifest caching can be buggy.
     */
    public void testManifestCaching() throws Exception {
        File workdir = getWorkDir();
        System.setProperty("netbeans.user", workdir.getAbsolutePath());
        ModuleInstaller inst = new org.netbeans.core.startup.NbInstaller(new FakeEvents());
        File littleJar = new File(workdir, "little-manifest.jar");
        //inst.loadManifest(littleJar).write(System.out);
        assertEquals(getManifest(littleJar), inst.loadManifest(littleJar));
        File mediumJar = new File(workdir, "medium-manifest.jar");
        assertEquals(getManifest(mediumJar), inst.loadManifest(mediumJar));
        File bigJar = new File(workdir, "big-manifest.jar");
        assertEquals(getManifest(bigJar), inst.loadManifest(bigJar));
        Stamps.getModulesJARs().shutdown();
        File allManifestsDat = new File(new File(new File(workdir, "var"), "cache"), "all-manifest.dat");
        assertTrue("File " + allManifestsDat + " exists", allManifestsDat.isFile());
        // Create a new NbInstaller, since otherwise it turns off caching...
        inst = new org.netbeans.core.startup.NbInstaller(new FakeEvents());
        assertEquals(getManifest(littleJar), inst.loadManifest(littleJar));
        assertEquals(getManifest(mediumJar), inst.loadManifest(mediumJar));
        assertEquals(getManifest(bigJar), inst.loadManifest(bigJar));
    }
    
    private static Manifest getManifest(File jar) throws IOException {
        JarFile jf = new JarFile(jar);
        try {
            return jf.getManifest();
        } finally {
            jf.close();
        }
    }
    
}
