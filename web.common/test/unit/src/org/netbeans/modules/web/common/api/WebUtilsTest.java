/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.common.api;

import java.awt.Color;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class WebUtilsTest extends CslTestBase {

    public WebUtilsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        WebUtils.UNIT_TESTING = true;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        WebUtils.UNIT_TESTING = false;
    }

    
    public void toHexColorCode() {
        assertEquals("#ff0000", WebUtils.toHexCode(Color.RED));
        assertEquals("#2201aa", WebUtils.toHexCode(Color.decode("#2201aa")));
    }

    public void testResolve() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("third.txt");
        assertNotNull(two);

        FileObject resolved = WebUtils.resolve(one, "third.txt");
        assertNotNull(resolved);
        assertEquals(two, resolved);

    }

    public void testResolveFolderReferences() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("folder/second.txt");
        assertNotNull(two);

        //test resolve path reference
        FileObject resolved = WebUtils.resolve(one, "folder/second.txt");
        assertNotNull(resolved);
        assertEquals(two, resolved);

        //test resolve path reference backward
        resolved = WebUtils.resolve(two, "../one.txt");
        assertNotNull(resolved);
        assertEquals(one, resolved);

    }

    public void testFileReference() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("folder/second.txt");
        assertNotNull(two);

        //test resolve path reference
        FileReference resolved = WebUtils.resolveToReference(one, "folder/second.txt");
        assertNotNull(resolved);

        assertEquals(one, resolved.source());
        assertEquals(two, resolved.target());
        assertEquals(FileReferenceType.RELATIVE, resolved.type());
        assertEquals("folder/second.txt", resolved.linkPath());
        assertEquals("folder/second.txt", resolved.optimizedLinkPath());

    }

    public void testOptimizedLink() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject fourth = getTestFile("folder/innerfolder/fourth.txt");
        assertNotNull(fourth);

        FileReference resolved = WebUtils.resolveToReference(one, "folder/innerfolder/fourth.txt");
        assertNotNull(resolved);
        assertEquals(fourth, resolved.target());
        assertEquals("folder/innerfolder/fourth.txt", resolved.optimizedLinkPath());

        //and back
        resolved = WebUtils.resolveToReference(fourth, "../../one.txt");
        assertNotNull(resolved);
        assertEquals(one, resolved.target());
        assertEquals("../../one.txt", resolved.optimizedLinkPath());
    }

}