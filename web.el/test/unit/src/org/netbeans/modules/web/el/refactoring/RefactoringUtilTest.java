/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.el.refactoring;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Erno Mononen
 */
public class RefactoringUtilTest {

    public RefactoringUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testEncodeAndHighlight() {
        String text = "#{bar.foo}";
        OffsetRange expressionOffset = new OffsetRange(0, 10);
        OffsetRange nodeOffset = new OffsetRange(6, 9);
        String result = RefactoringUtil.encodeAndHighlight(text, expressionOffset, nodeOffset);
        assertEquals("#{bar.<b>foo</b>}", result);
    }

    @Test
    public void testEncodeAndHighlight2() {
        String text = "<h v=\"#{bar.foo}#{bar.foo}\"/>";
        OffsetRange expressionOffset = new OffsetRange(6, 16);
        OffsetRange nodeOffset = new OffsetRange(6, 9);
        String result = RefactoringUtil.encodeAndHighlight(text, expressionOffset, nodeOffset);

        expressionOffset = new OffsetRange(16, 26);
        result = RefactoringUtil.encodeAndHighlight(text, expressionOffset, nodeOffset);
        assertEquals("&lt;h v=\"#{bar.foo}#{bar.<b>foo</b>}\"/&gt;", result);
    }

    @Test
    public void testGetPropertyName() {
        assertEquals("foo", RefactoringUtil.getPropertyName("getFoo"));
        assertEquals("foo", RefactoringUtil.getPropertyName("isFoo"));
        assertEquals("foo", RefactoringUtil.getPropertyName("foo"));
        assertEquals("FOO", RefactoringUtil.getPropertyName("getFOO"));
        assertEquals("FOo", RefactoringUtil.getPropertyName("getFOo"));

        assertEquals("gettyFoo", RefactoringUtil.getPropertyName("gettyFoo"));
        assertEquals("issyFoo", RefactoringUtil.getPropertyName("issyFoo"));
    }

}