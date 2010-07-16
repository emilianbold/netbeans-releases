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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.extras.api.support;

import java.awt.FontMetrics;
import java.util.List;
import org.junit.Test;
import org.netbeans.modules.dlight.extras.api.AxisMark;
import static org.junit.Assert.*;

/**
 *
 * @author Alexey Vladykin
 */
public class TimeMarksProviderTest {

    public TimeMarksProviderTest() {
    }

    @Test
    public void testViewport1s() {
        TimeMarksProvider tmp = TimeMarksProvider.newInstance();
        List<AxisMark> marks = tmp.getAxisMarks(0L, 1000000000L, 100, new FakeFontMetrics());
        assertEquals(11, marks.size());
        for (int i = 0; i < 11; ++i) {
            assertEquals(10 * i, marks.get(i).getPosition());
            assertEquals(255, marks.get(i).getMarkOpacity());
        }
        assertEquals("0:00", marks.get(0).getText());
        assertEquals(255, marks.get(0).getTextOpacity());
        assertNull(marks.get(1).getText());
        assertNull(marks.get(2).getText());
        assertNull(marks.get(3).getText());
        assertNull(marks.get(4).getText());
        assertEquals("0:00.5", marks.get(5).getText());
        assertEquals(255, marks.get(5).getTextOpacity());
        assertNull(marks.get(6).getText());
        assertNull(marks.get(7).getText());
        assertNull(marks.get(8).getText());
        assertNull(marks.get(9).getText());
        assertEquals("0:01", marks.get(10).getText());
        assertEquals(255, marks.get(10).getTextOpacity());
    }

    @Test
    public void testViewport10s() {
        TimeMarksProvider tmp = TimeMarksProvider.newInstance();
        List<AxisMark> marks = tmp.getAxisMarks(0L, 10000000000L, 100, new FakeFontMetrics());
        assertEquals(11, marks.size());
        for (int i = 0; i < 11; ++i) {
            assertEquals(10 * i, marks.get(i).getPosition());
            assertEquals(255, marks.get(i).getMarkOpacity());
        }
        assertEquals("0:00", marks.get(0).getText());
        assertEquals(255, marks.get(0).getTextOpacity());
        assertNull(marks.get(1).getText());
        assertNull(marks.get(2).getText());
        assertNull(marks.get(3).getText());
        assertNull(marks.get(4).getText());
        assertEquals("0:05", marks.get(5).getText());
        assertEquals(255, marks.get(5).getTextOpacity());
        assertNull(marks.get(6).getText());
        assertNull(marks.get(7).getText());
        assertNull(marks.get(8).getText());
        assertNull(marks.get(9).getText());
        assertEquals("0:10", marks.get(10).getText());
        assertEquals(255, marks.get(10).getTextOpacity());
    }

    @Test
    public void testViewport100s() {
        TimeMarksProvider tmp = TimeMarksProvider.newInstance();
        List<AxisMark> marks = tmp.getAxisMarks(0L, 100000000000L, 100, new FakeFontMetrics());
        assertEquals(11, marks.size());
        for (int i = 0; i < 11; ++i) {
            assertEquals(10 * i, marks.get(i).getPosition());
            assertEquals(255, marks.get(i).getMarkOpacity());
        }
        assertEquals("0:00", marks.get(0).getText());
        assertEquals(255, marks.get(0).getTextOpacity());
        assertNull(marks.get(1).getText());
        assertNull(marks.get(2).getText());
        assertNull(marks.get(3).getText());
        assertNull(marks.get(4).getText());
        assertNull(marks.get(5).getText());
        assertEquals("1:00", marks.get(6).getText());
        assertEquals(255, marks.get(6).getTextOpacity());
        assertNull(marks.get(7).getText());
        assertNull(marks.get(8).getText());
        assertNull(marks.get(9).getText());
        assertNull(marks.get(10).getText());
    }

    @Test
    public void testViewport500s() {
        TimeMarksProvider tmp = TimeMarksProvider.newInstance();
        List<AxisMark> marks = tmp.getAxisMarks(0L, 500000000000L, 100, new FakeFontMetrics());
        assertEquals(9, marks.size());
        for (int i = 0; i < 9; ++i) {
            assertEquals(12 * i, marks.get(i).getPosition());
            assertEquals(255, marks.get(i).getMarkOpacity());
        }
        assertEquals("0:00", marks.get(0).getText());
        assertEquals(255, marks.get(0).getTextOpacity());
        assertNull(marks.get(1).getText());
        assertNull(marks.get(2).getText());
        assertNull(marks.get(3).getText());
        assertNull(marks.get(4).getText());
        assertEquals("5:00", marks.get(5).getText());
        assertEquals(255, marks.get(5).getTextOpacity());
        assertNull(marks.get(6).getText());
        assertNull(marks.get(7).getText());
        assertNull(marks.get(8).getText());
    }

    @Test
    public void testViewport1000s() {
        TimeMarksProvider tmp = TimeMarksProvider.newInstance();
        List<AxisMark> marks = tmp.getAxisMarks(0L, 1000000000000L, 100, new FakeFontMetrics());
        assertEquals(4, marks.size());
        for (int i = 0; i < 4; ++i) {
            assertEquals(30 * i, marks.get(i).getPosition());
            assertEquals(255, marks.get(i).getMarkOpacity());
        }
        assertEquals("0:00", marks.get(0).getText());
        assertEquals(255, marks.get(0).getTextOpacity());
        assertNull(marks.get(1).getText());
        assertEquals("10:00", marks.get(2).getText());
        assertEquals(255, marks.get(2).getTextOpacity());
        assertNull(marks.get(3).getText());
    }

    private static class FakeFontMetrics extends FontMetrics {
        public FakeFontMetrics() {
            super(null);
        }
        @Override
        public int stringWidth(String str) {
            return 6 * str.length();
        }
    }
}

