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
package org.netbeans.modules.dlight.indicators.support;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import org.netbeans.modules.dlight.indicators.graph.Range;

/**
 * @author Alexey Vladykin
 */
public class TimeSeriesIndicatorManager extends JScrollBar implements AdjustmentListener {

    private static final int EXTENT = 10;

    private final List<TimeSeriesIndicator> indicators;
    private int viewportStart;
    private int viewportEnd;
    private int dataStart;
    private int dataEnd;
    private boolean autoscroll;
    private boolean isAdjusting;

    public TimeSeriesIndicatorManager() {
        super(JScrollBar.HORIZONTAL);
        indicators = new ArrayList<TimeSeriesIndicator>();
        addAdjustmentListener(this);
    }

    public synchronized void addIndicator(TimeSeriesIndicator indicator) {
        dataStart = dataEnd = 0;
        viewportStart = 0;
        viewportEnd = EXTENT;
        indicator.setViewport(new Range<Integer>(viewportStart, viewportEnd));
        indicator.setSelection(new Range<Integer>(null, null));
        indicators.add(indicator);
        adjust();
    }

    public JComponent getComponent() {
        return this;
    }

    public synchronized void tick(TimeSeriesIndicator indicator, int time) {
        dataEnd = Math.max(dataEnd, time);
        if (autoscroll) {
            // default behavior: scroll to show last 10 seconds
            if (viewportEnd < time) {
                viewportStart += time - viewportEnd;
                viewportEnd = time;
            }
            updateIndicators();
        }
        adjust();
    }

    private void updateIndicators() {
        for (TimeSeriesIndicator i : indicators) {
            i.setViewport(new Range<Integer>(viewportStart, viewportEnd));
        }
    }

    private void adjust() {
        isAdjusting = true;
        setMinimum(Math.min(dataStart, viewportStart));
        setMaximum(Math.max(dataEnd, viewportEnd));
        setValue(viewportStart);
        setVisibleAmount(viewportEnd - viewportStart);
        autoscroll = (dataEnd <= viewportEnd);
        isAdjusting = false;
    }

    public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
        if (!isAdjusting) {
            viewportStart = e.getValue();
            viewportEnd = viewportStart + EXTENT;
            adjust();
            updateIndicators();
        }
    }
}
