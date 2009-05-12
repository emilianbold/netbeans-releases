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

package org.netbeans.modules.cnd.api.execution;

import java.net.URL;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Alexander Simon
 */
public class LinkTestCase extends NbTestCase {

    public LinkTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testLink() throws Exception {
        URL file = LinkTestCase.class.getResource("/org/netbeans/modules/cnd/api/execution/gcc.exe.lnk.data"); // NOI18N
        String resolved = LinkSupport.getOriginalFile(file.getFile());
        assertEquals("C:\\util\\cygwin\\etc\\alternatives\\gcc", resolved); // NOI18N
        file = LinkTestCase.class.getResource("/org/netbeans/modules/cnd/api/execution/gcc.lnk.data"); // NOI18N
        resolved = LinkSupport.getOriginalFile(file.getFile());
        assertEquals("C:\\util\\cygwin\\bin\\gcc-3.exe", resolved); // NOI18N
    }

    public void testCygwinLink() throws Exception {
        URL file = LinkTestCase.class.getResource("/org/netbeans/modules/cnd/api/execution/g++.data"); // NOI18N
        String resolved = LinkSupport.getOriginalFile(file.getFile());
        assertEquals("/etc/alternatives/g++", resolved);
    }

    public void testCygwinLink2() throws Exception {
        URL file = LinkTestCase.class.getResource("/org/netbeans/modules/cnd/api/execution/c++.exe.data"); // NOI18N
        String resolved = LinkSupport.getOriginalFile(file.getFile());
        String expected = file.getFile();
        int i = expected.lastIndexOf("\\"); // NOI18N
        if (i < 0) {
            i = expected.lastIndexOf("/"); // NOI18N
        }
        if (i > 0) {
            expected = expected.substring(0, i + 1) + "g++.exe";// NOI18N
        }
        assertEquals(expected, resolved);
    }

    public void testCygwinLink3() throws Exception {
        URL file = LinkTestCase.class.getResource("/org/netbeans/modules/cnd/api/execution/f77.exe.data");// NOI18N
        String resolved = LinkSupport.getOriginalFile(file.getFile());
        String expected = file.getFile();
        int i = expected.lastIndexOf("\\"); // NOI18N
        if (i < 0) {
            i = expected.lastIndexOf("/"); // NOI18N
        }
        if (i > 0) {
            expected = expected.substring(0, i + 1) + "g77.exe";// NOI18N
        }
        assertEquals(expected, resolved);
    }
}
