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

package org.netbeans.modules.dlight.indicators.graph;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.dlight.indicators.ValueFormatter;
import org.netbeans.modules.dlight.util.DLightMath;

/**
 * @author Alexey Vladykin
 */
public final class AxisMarksProviderFactory {

    private AxisMarksProviderFactory() {}

    public static AxisMarksProvider newTimeMarksProvider() {
        return new TimeMarksProvider();
    }

    public static AxisMarksProvider newValueMarksProvider(ValueFormatter formatter) {
        return new ValueMarksProvider(formatter);
    }


    private static class TimeMarksProvider implements AxisMarksProvider {

        private TimeMarksProvider() {}

        public List<AxisMark> getAxisMarks(int viewportStart, int viewportEnd, int axisSize, FontMetrics axisFontMetrics) {
            List<AxisMark> marks = new ArrayList<AxisMark>();
            for (int value = viewportStart; value < viewportEnd; ++value) {
                String text = null;
                if (value % 5 == 0) {
                    text = formatTime(value);
                }
                marks.add(new AxisMark(DLightMath.map(value, viewportStart, viewportEnd, 0, axisSize), text));
            }
            return marks;
        }

        private String formatTime(int seconds) {
            return String.format("%d:%02d", seconds / 60, seconds % 60); // NOI18N
        }
    }

    private static class ValueMarksProvider implements AxisMarksProvider {

        private final ValueFormatter formatter;

        private ValueMarksProvider(ValueFormatter formatter) {
            this.formatter = formatter;
        }

        public List<AxisMark> getAxisMarks(int viewportStart, int viewportEnd, int axisSize, FontMetrics axisFontMetrics) {
            List<AxisMark> marks = new ArrayList<AxisMark>();
            createMarks(viewportStart, viewportEnd, 0, axisSize, axisFontMetrics, marks);
            marks.add(new AxisMark(axisSize, formatValue(viewportEnd)));
            return marks;
        }

        private void createMarks(int minVal, int maxVal, int minPos, int maxPos, FontMetrics axisFontMetrics, List<AxisMark> marks) {
            if (maxPos - minPos <= axisFontMetrics.getAscent()) {
                return;
            }
            int midPos = (minPos + maxPos) / 2;
            int midVal = (minVal + maxVal) / 2;
            if (axisFontMetrics.getAscent() <= midPos - minPos) {
                createMarks(minVal, midVal, minPos, midPos, axisFontMetrics, marks);
            }
            marks.add(new AxisMark(midPos,
                    maxVal - minVal < 2? null : formatValue(midVal),
                    DLightMath.map(maxPos - minPos, 3 * axisFontMetrics.getAscent() / 2, 2 * axisFontMetrics.getAscent(), 0, 255),
                    DLightMath.map(maxPos - minPos, 2 * axisFontMetrics.getAscent(), 3 * axisFontMetrics.getAscent(), 0, 255)));
            if (axisFontMetrics.getAscent() <= maxPos - midPos) {
                createMarks(midVal, maxVal, midPos, maxPos, axisFontMetrics, marks);
            }
        }

        private String formatValue(int value) {
            if (formatter == null) {
                return String.valueOf(value);
            } else {
                return formatter.format(value);
            }
        }
    }

}
