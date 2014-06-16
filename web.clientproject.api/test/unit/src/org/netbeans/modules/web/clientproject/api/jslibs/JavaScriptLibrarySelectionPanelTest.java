/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.api.jslibs;

import org.junit.Test;
import org.netbeans.junit.NbTestCase;

public class JavaScriptLibrarySelectionPanelTest extends NbTestCase {

    private final JavaScriptLibrarySelectionPanel.LibraryComparator libraryComparator = new JavaScriptLibrarySelectionPanel.LibraryComparator();


    public JavaScriptLibrarySelectionPanelTest(String name) {
        super(name);
    }

    @Test
    public void testCompareSameVersions() {
        assertEquals(1, libraryComparator.compareSameVersions("1.0", "1.0patch1").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("1.0patch1", "1.0").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("1.0", "1.0rc1").intValue());
        assertEquals(1, libraryComparator.compareSameVersions("1.0rc1", "1.0").intValue());
        assertEquals(1, libraryComparator.compareSameVersions("1.0-pre.1", "1.0-pre.2").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("1.0-rc2", "1.0-pre1").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("1.0-rc1", "1.0-pre7").intValue());
        assertEquals(1, libraryComparator.compareSameVersions("1.0-pre7", "1.0-rc1").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("1.0-rc1", "1.0-beta7").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("1.0-pre1", "1.0-beta7").intValue());
        assertEquals(1, libraryComparator.compareSameVersions("1.0-beta7", "1.0-pre1").intValue());
        assertEquals(0, libraryComparator.compareSameVersions("1.0-rc2", "1.0-rc2").intValue());
        assertEquals(1, libraryComparator.compareSameVersions("1.0-rc2", "1.0-patch").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("1.0-patch", "1.0-rc2").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("1.0-patch4", "1.0-patch3").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("1.0-patch1", "1.0-beta.3").intValue());
        assertEquals(1, libraryComparator.compareSameVersions("3.0.0-rc1", "3.0.0-rc2").intValue());
        assertEquals(-1, libraryComparator.compareSameVersions("3.0.0-rc2", "3.0.0-rc1").intValue());
        assertNull(libraryComparator.compareSameVersions("3.0.0-unk1", "3.0.0-xyz2"));
    }

    @Test
    public void testSanitizeVersion() {
        assertEquals("1.0", libraryComparator.sanitize("1.0-beta1"));
        assertEquals("53", libraryComparator.sanitize("r53"));
    }

}
