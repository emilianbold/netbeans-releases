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

package org.netbeans.modules.dlight.indicators.graph;

import org.junit.Test;
import org.netbeans.modules.dlight.indicators.Aggregation;
import static org.junit.Assert.*;

/**
 *
 * @author Alexey Vladykin
 */
public class TimeSeriesDataContainerTest {

    @Test(expected=IllegalArgumentException.class)
    public void testEmpty() {
        TimeSeriesDataContainer c = new TimeSeriesDataContainer(10, Aggregation.FIRST, 1, false);
        assertNull(c.get(0));
    }

    @Test
    public void testFirst() {
        TimeSeriesDataContainer c = new TimeSeriesDataContainer(10, Aggregation.FIRST, 1, false);
        c.put(0, new float[] {1});
        c.put(0, new float[] {2});
        assertEquals(1f, c.get(0)[0], 0);
    }

    @Test
    public void testLast() {
        TimeSeriesDataContainer c = new TimeSeriesDataContainer(10, Aggregation.LAST, 1, false);
        c.put(0, new float[] {1});
        c.put(0, new float[] {2});
        assertEquals(2f, c.get(0)[0], 0);
    }

    @Test
    public void testSum() {
        TimeSeriesDataContainer c = new TimeSeriesDataContainer(10, Aggregation.SUM, 1, false);
        c.put(0, new float[] {1});
        c.put(0, new float[] {2});
        assertEquals(3f, c.get(0)[0], 0);
    }

    @Test
    public void testAverage() {
        TimeSeriesDataContainer c = new TimeSeriesDataContainer(10, Aggregation.AVERAGE, 1, false);
        c.put(0, new float[] {1});
        c.put(0, new float[] {2});
        assertEquals(1.5f, c.get(0)[0], 0);
    }

    @Test
    public void testBucketMapping() {
        TimeSeriesDataContainer c = new TimeSeriesDataContainer(10, Aggregation.SUM, 1, false);
        c.put(0, new float[] {0});
        assertEquals(0, c.get(0)[0], 0);
        c.put(1, new float[] {1});
        c.put(9, new float[] {9});
        c.put(10, new float[] {10});
        assertEquals(20, c.get(1)[0], 0);
        c.put(11, new float[] {11});
        assertEquals(11, c.get(2)[0], 0);
    }
}
