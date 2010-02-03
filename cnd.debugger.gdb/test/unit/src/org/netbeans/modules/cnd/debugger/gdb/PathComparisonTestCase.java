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

package org.netbeans.modules.cnd.debugger.gdb;

import junit.framework.TestCase;
import org.junit.Test;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;

/**
 *
 * @author Egor Ushakov
 */
public class PathComparisonTestCase extends TestCase {

    /*@Test
    public void testPathComparisonCase() {
        assert GdbUtils.compareUnixPaths("C:\\a", "c:\\A");
    }

    @Test
    public void testWinPathComparisonTrim() {
        assert GdbUtils.compareUnixPaths("   /cygdrive/c/a   ", " /cygdrive/c/a      ");
    }

    @Test
    public void testWinPathComparisonNormal() {
        assert GdbUtils.compareUnixPaths("/cygdrive/c/./a", "c:\\a");
    }

    @Test
    public void testWinPathComparisonNormal2() {
        assert GdbUtils.compareUnixPaths("/cygdrive/c/../c/a", "c:\\temp\\..\\a");
    }

    @Test
    public void testWinPathComparisonSpace() {
        assert GdbUtils.compareUnixPaths("C:\\dir space\\a", "/cygdrive/c/dir space/a");
    }

    @Test
    public void testWinPathComparisonSeparators() {
        assert GdbUtils.compareUnixPaths("C:/temp/test/SubProjects/hello3lib/hello3.cc", "c:\\temp\\test\\SubProjects\\hello3lib/hello3.cc");
    }*/

    @Test
    public void testUnixPathComparisonNormal() {
        assert GdbUtils.compareUnixPaths("/tmp/./a", "/tmp/../tmp/a");
    }
}
