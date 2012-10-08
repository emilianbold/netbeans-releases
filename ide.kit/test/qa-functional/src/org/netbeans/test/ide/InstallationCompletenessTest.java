/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.test.ide;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;

public class InstallationCompletenessTest extends NbTestCase {

    public static final String DISTRO_PROPERTY = "netbeans.distribution";

    public enum Type {

        JAVASE("base,javase,websvccommon"),
        JAVAEE("base,javase,websvccommon,javaee,webcommon"),
        PHP("base,php,websvccommon,webcommon"),
        CND("base,cnd"),
        FULL("base,javase,javaee,php,cnd,webcommon,websvccommon,full");
        private String parts;

        private Type(String parts) {
            this.parts = parts;
        }

        public String getParts() {
            return parts;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public InstallationCompletenessTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); //To change body of generated methods, choose Tools | Templates.
    }

    public static Test suite() throws IOException {
        // disable 'slowness detection'
        System.setProperty("org.netbeans.core.TimeableEventQueue.quantum", "100000");
        NbTestSuite s = new NbTestSuite();
        s.addTest(
                NbModuleSuite.createConfiguration(
                InstallationCompletenessTest.class).gui(true).clusters(".*").enableModules(".*").
                honorAutoloadEager(true).
                addTest("testInstalledKits")
                .suite());
        return s;
    }

    public void testInstalledKits() throws Exception {
        String distro = System.getProperty(DISTRO_PROPERTY);
        assertNotNull("Distribution not set, please set by setting property:" + DISTRO_PROPERTY, distro);
        Set<String> kitsGolden = getModulesForDistro(distro, new File(getDataDir(), "kits.properties"));
        Set<String> redundantKits = new HashSet<String>();
        UpdateManager um = UpdateManager.getDefault();
        List<UpdateUnit> l = um.getUpdateUnits(UpdateManager.TYPE.KIT_MODULE);
        for (UpdateUnit updateUnit : l) {
            String kitName = updateUnit.getCodeName();
            if (kitsGolden.contains(kitName)) {
                kitsGolden.remove(kitName);
                System.out.println("OK - IDE contains:" + updateUnit.getCodeName());
            } else {
                redundantKits.add(kitName);
                System.out.println("REDUNDANT - IDE contains:" + kitName);
            }
        }
        for (String missing : kitsGolden) {
            System.out.println("MISSING - IDE does not contain:" + missing);
        }
        assertTrue("Some modules are missing", kitsGolden.isEmpty());
        assertTrue("Some modules are redundant", redundantKits.isEmpty());
    }

    private static Set<String> getModulesForDistro(String distro, File f) {
        Set<String> result = new HashSet<String>();
        Type distroT = Type.valueOf(distro.toUpperCase());
        Properties p = readProps(f);
        assertNotNull("Distro golden files are correctly read", p);
        for (String kit : p.stringPropertyNames()) {
            if (distroT.getParts().contains(p.getProperty(kit))) {
                result.add(kit);
            }
        }
        return result;
    }

    private static Properties readProps(File f) {
        Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            props.load(fis);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                }
            }
        }
        return props;
    }
}