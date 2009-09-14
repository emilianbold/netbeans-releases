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
package org.netbeans.modules.dlight.extras.api.support;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.extras.api.AxisMark;
import org.netbeans.modules.dlight.extras.api.AxisMarksProvider;
import org.netbeans.modules.dlight.util.DLightMath;

/**
 * @author Alexey Vladykin
 */
public final class TimeMarksProvider implements AxisMarksProvider {

    public static TimeMarksProvider newInstance() {
        return new TimeMarksProvider();
    }

    private TimeMarksProvider() {
    }
    private static final int[] INTERVALS = {1, 5, 10, 30, 60, 300, 600};
    private static final String LABEL_TEXT = "99:99"; // NOI18N

    public List<AxisMark> getAxisMarks(int viewportStart, int viewportEnd, int axisSize, FontMetrics axisFontMetrics) {
        if (viewportStart == viewportEnd || axisSize < 10) {
            return Collections.emptyList();
        }
        int tickInterval = getTickInterval(viewportEnd - viewportStart, axisSize);
        int labelInterval = getLabelInterval(viewportEnd - viewportStart, axisSize, axisFontMetrics);
        List<AxisMark> marks = new ArrayList<AxisMark>();
        for (int value = viewportStart; value < viewportEnd; ++value) {
            if (value % tickInterval == 0) {
                String text = null;
                if (value % labelInterval == 0) {
                    text = formatTime(value);
                }
                marks.add(new AxisMark(DLightMath.map(value, viewportStart, viewportEnd, 0, axisSize), text));
            }
        }
        return marks;
    }

    private int getTickInterval(int viewportSize, int axisSize) {
        int pixelsPerSecond = axisSize / viewportSize;
        for (int i = 0; i < INTERVALS.length; ++i) {
            if (10 <= INTERVALS[i] * pixelsPerSecond) {
                return INTERVALS[i];
            }
        }
        return INTERVALS[INTERVALS.length - 1];
    }

    private int getLabelInterval(int viewportSize, int axisSize, FontMetrics axisFontMetrics) {
        int pixelsPerSecond = axisSize / viewportSize;
        for (int i = 0; i < INTERVALS.length; ++i) {
            if (4 * axisFontMetrics.stringWidth(LABEL_TEXT) / 3 <= INTERVALS[i] * pixelsPerSecond) {
                return INTERVALS[i];
            }
        }
        return INTERVALS[INTERVALS.length - 1];
    }

    private String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60); // NOI18N
    }
}
