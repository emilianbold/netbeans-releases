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

package org.netbeans.modules.hudson.mercurial;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.TestFileUtils;

public class HudsonMercurialSCMTest extends NbTestCase {

    public HudsonMercurialSCMTest(String n) {
        super(n);
    }

    public void testGetDefaultPull() throws Exception {
        HudsonMercurialSCM.LOG.setLevel(Level.OFF);
        clearWorkDir();
        assertNull("no repo", HudsonMercurialSCM.getDefaultPull(getWorkDir().toURI()));
        assertPullURI("http://host/repo/", "[paths]", "default = http://host/repo/");
        assertPullURI("http://host/repo/", "[paths]", "default = http://host/repo");
        assertPullURI("http://host/repo/", "[paths]", "default-pull = http://host/repo/");
        assertPullURI("http://host/repo/", "[paths]", "default-pull = http://host/repo/", "default = http://host/other/");
        assertPullURI(getWorkDir().toURI().toString(), "[paths]", "default=" + getWorkDirPath().replace(File.separatorChar, '/'));
        assertPullURI(getWorkDir().toURI() + "foo/", "[paths]", "default = foo");
        assertPullURI(getWorkDir().toURI().toString(), "[paths]");
        assertPullURI("https://host/repo/", "[paths]", "default = https://bob:sEcReT@host/repo/");
        assertPullURI("https://host/repo/", "[paths]", "default = https://bob@host/repo/");
        assertPullURI("ssh://host/repo/", "[paths]", "default = ssh://bob@host/repo");
        assertPullURI(null, "[paths");
        assertPullURI(getWorkDir().toURI().toString());
    }

    private void assertPullURI(String pull, String... hgrc) throws Exception {
        clearWorkDir();
        TestFileUtils.writeFile(new File(getWorkDir(), ".hg/requires"), "revlogv1\nstore\n");
        if (hgrc.length > 0) {
            StringBuilder b = new StringBuilder();
            for (String line : hgrc) {
                b.append(line).append('\n');
            }
            TestFileUtils.writeFile(new File(getWorkDir(), ".hg/hgrc"), b.toString());
        }
        assertEquals(pull != null ? URI.create(pull) : null, HudsonMercurialSCM.getDefaultPull(getWorkDir().toURI()));
    }

}
