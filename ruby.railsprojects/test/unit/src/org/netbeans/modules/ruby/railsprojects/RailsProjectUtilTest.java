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

package org.netbeans.modules.ruby.railsprojects;

import org.netbeans.api.ruby.platform.RubyTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class RailsProjectUtilTest extends RubyTestBase {

    public RailsProjectUtilTest(String name) {
        super(name);
    }

    public void testGetVersionString() throws Exception {
        FileObject versionFo = getTestFile("testfiles/version.rb");
        String version = RailsProjectUtil.getVersionString(FileUtil.toFile(versionFo));
        assertEquals("2.0.2", version);
    }

    public void testGetSpecifiedRailsVersion() throws Exception {
        FileObject versionFo = getTestFile("testfiles/environment.rb");
        String version = RailsProjectUtil.getSpecifiedRailsVersion(versionFo);
        assertEquals("2.1.0", version);
    }

    public void testVersionFor() {
        RailsProjectUtil.RailsVersion version = RailsProjectUtil.versionFor("1");
        assertEquals("1.0.0", version.asString());

        version = RailsProjectUtil.versionFor("2.2");
        assertEquals("2.2.0", version.asString());

        version = RailsProjectUtil.versionFor("2.3.2");
        assertEquals("2.3.2", version.asString());

        version = RailsProjectUtil.versionFor("3.0.0.beta");
        assertEquals("3.0.0.beta", version.asString());

        version = RailsProjectUtil.versionFor("x.1");
        assertEquals("0.0.0", version.asString());
    }

    public void testCompareRailsVersions() {
        assertEquals(-1, RailsProjectUtil.versionFor("2.1").compareTo(RailsProjectUtil.versionFor("2.3.0")));
        assertEquals(0, RailsProjectUtil.versionFor("1.1").compareTo(RailsProjectUtil.versionFor("1.1")));
        assertEquals(1, RailsProjectUtil.versionFor("2.4.10").compareTo(RailsProjectUtil.versionFor("2.4.9")));
    }
}
