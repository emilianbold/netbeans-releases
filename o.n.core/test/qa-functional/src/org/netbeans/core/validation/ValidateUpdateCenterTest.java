/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.core.validation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.NbTopManager;
import org.netbeans.core.startup.ConsistencyVerifier;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

/**
 * Checks that all modules in the distribution are suitably visible to Plugin Manager.
 */
public class ValidateUpdateCenterTest extends NbTestCase {

    public ValidateUpdateCenterTest(String n) {
        super(n);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ValidateUpdateCenterTest.class).
                clusters(".*").enableModules(".*").honorAutoloadEager(true).gui(false).enableClasspathModules(false)));
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ValidateUpdateCenterTest.class).
                clusters("(platform|harness|ide|websvccommon|gsf|java|profiler|nb)[0-9.]*").enableModules(".*").
                honorAutoloadEager(true).gui(false).enableClasspathModules(false)));
        /* Too many failures to all be fixed:
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ValidateUpdateCenterTest.class).
                clusters("(platform|ide)[0-9.]*").enableModules(".*").honorAutoloadEager(true).gui(false).enableClasspathModules(false)));
         */
        return suite;
    }

    public void testInvisibleModules() throws Exception {
        Set<Manifest> manifests = loadManifests();
        Set<String> requiredBySomeone = new HashSet<String>();
        for (Manifest m : manifests) {
            String deps = m.getMainAttributes().getValue("OpenIDE-Module-Module-Dependencies");
            if (deps != null) {
                String identifier = "[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*";
                Matcher match = Pattern.compile(identifier + "(\\." + identifier + ")*").matcher(deps);
                while (match.find()) {
                    requiredBySomeone.add(match.group());
                }
            }
        }
        StringBuilder auVisibilityProblems = new StringBuilder();
        String[] markers = {"autoload", "eager", "AutoUpdate-Show-In-Client", "AutoUpdate-Essential-Module"};
        MODULE: for (Manifest m : manifests) {
            String cnb = findCNB(m);
            if (requiredBySomeone.contains(cnb)) {
                continue;
            }
            Attributes attr = m.getMainAttributes();
            for (String marker : markers) {
                if ("true".equals(attr.getValue(marker))) {
                    continue MODULE;
                }
            }
            auVisibilityProblems.append("\n" + cnb);
        }
        if (auVisibilityProblems.length() > 0) {
            fail("Some regular modules (that no one depends on) neither AutoUpdate-Show-In-Client nor AutoUpdate-Essential-Module" + auVisibilityProblems);
        }
    }

    public void testDisabledAutoloads() throws Exception {
        Set<Manifest> manifests = loadManifests();
        Set<String> permittedDisabledAutoloads = new HashSet<String>();
        // org.netbeans.lib.terminalemulator is really unused in cnd cluster, yet apparently has to be in the build anyway; contact Thomas Preisler
        permittedDisabledAutoloads.add("org.netbeans.lib.terminalemulator");
        // some pseudomodules used only by tests
        permittedDisabledAutoloads.add("org.netbeans.modules.jellytools");
        permittedDisabledAutoloads.add("org.netbeans.modules.jellytools.cnd");
        permittedDisabledAutoloads.add("org.netbeans.modules.jellytools.enterprise");
        permittedDisabledAutoloads.add("org.netbeans.modules.jellytools.ide");
        permittedDisabledAutoloads.add("org.netbeans.modules.jellytools.java");
        permittedDisabledAutoloads.add("org.netbeans.modules.jellytools.platform");
        permittedDisabledAutoloads.add("org.netbeans.modules.jellytools.ruby");
        permittedDisabledAutoloads.add("org.netbeans.modules.jemmy");
        permittedDisabledAutoloads.add("org.netbeans.modules.nbjunit");
        permittedDisabledAutoloads.add("org.netbeans.insane");
        permittedDisabledAutoloads.add("org.netbeans.modules.visualweb.gravy");
        // just has to be present, not enabled
        permittedDisabledAutoloads.add("org.netbeans.modules.apisupport.harness");
        // really unused, yet kept in build for tutorials
        permittedDisabledAutoloads.add("org.netbeans.modules.lexer.editorbridge");
        // for compatibility
        permittedDisabledAutoloads.add("org.openide.util.enumerations");
        // for use by developers
        permittedDisabledAutoloads.add("org.netbeans.spi.actions.support");
        // kept in base IDE but not used in all cluster configs
        permittedDisabledAutoloads.add("org.netbeans.libs.commons_net");
        permittedDisabledAutoloads.add("org.netbeans.libs.httpunit");
        permittedDisabledAutoloads.add("org.netbeans.libs.jakarta_oro");
        permittedDisabledAutoloads.add("org.netbeans.modules.gsf.testrunner");
        permittedDisabledAutoloads.add("org.netbeans.modules.gsf.codecoverage");
        permittedDisabledAutoloads.add("org.netbeans.modules.extexecution");
        permittedDisabledAutoloads.add("org.netbeans.modules.glassfish.common");
        permittedDisabledAutoloads.add("org.netbeans.modules.server");
        // still under development
        permittedDisabledAutoloads.add("org.netbeans.modules.spi.actions");
        // needed in IDE cluster because of issue #162414
        permittedDisabledAutoloads.add("org.netbeans.modules.web.client.tools.api");
        // schlieman is our diamond
        permittedDisabledAutoloads.add("org.netbeans.modules.languages");
        // os-specific binaries for embedded browser
        permittedDisabledAutoloads.add("org.netbeans.core.xulrunner.linux");
        permittedDisabledAutoloads.add("org.netbeans.core.xulrunner.macosx");
        permittedDisabledAutoloads.add("org.netbeans.core.xulrunner.solaris");
        permittedDisabledAutoloads.add("org.netbeans.core.xulrunner.win");
        SortedMap<String,SortedSet<String>> problems = ConsistencyVerifier.findInconsistencies(manifests, permittedDisabledAutoloads);
        if (!problems.isEmpty()) {
            StringBuilder message = new StringBuilder("Problems found with autoloads");
            for (Map.Entry<String, SortedSet<String>> entry : problems.entrySet()) {
                message.append("\nProblems found for module " + entry.getKey() + ": " + entry.getValue());
            }
            fail(message.toString());
        }
    }

    private static Set<Manifest> loadManifests() throws Exception {
        ModuleManager mgr = NbTopManager.get().getModuleSystem().getManager();
        Set<Manifest> manifests = new HashSet<Manifest>();
        for (Module m : mgr.getModules()) {
            Manifest manifest = m.getManifest();
            if (m.isAutoload()) {
                manifest.getMainAttributes().putValue("autoload", "true");
            } else if (m.isEager()) {
                manifest.getMainAttributes().putValue("eager", "true");
            }
            manifests.add(manifest);
        }
        return manifests;
    }

    private static String findCNB(Manifest m) {
        String name = m.getMainAttributes().getValue("OpenIDE-Module");
        if (name == null) {
            throw new IllegalArgumentException();
        }
        return name.replaceFirst("/\\d+$", "");
    }

}
