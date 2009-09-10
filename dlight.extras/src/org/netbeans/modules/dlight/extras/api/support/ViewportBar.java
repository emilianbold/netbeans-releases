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

import java.awt.Shape;
import org.netbeans.modules.dlight.extras.api.support.dragging.AbstractDraggable;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.datafilter.DataFilterManager;
import org.netbeans.modules.dlight.extras.api.AxisMark;
import org.netbeans.modules.dlight.extras.api.AxisMarksProvider;
import org.netbeans.modules.dlight.extras.api.support.dragging.DraggingSupport;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.extras.api.ViewportModelState;
import org.netbeans.modules.dlight.extras.api.support.dragging.Draggable;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilterFactory;
import org.netbeans.modules.dlight.util.DLightMath;

/**
 * @author Alexey Vladykin
 */
/*package*/ class ViewportBar extends JComponent implements ChangeListener, DataFilterListener {

    private final ViewportModel viewportModel;
    private final DataFilterManager filterManager;
    private final List<Draggable> marks;
    private final AxisMarksProvider timeMarksProvider;
    private final int margin;

    public ViewportBar(final ViewportModel viewportModel, final DataFilterManager filterManager, final int margin) {
        setMinimumSize(new Dimension(200, 30));
        setPreferredSize(new Dimension(200, 30));
        setOpaque(true);
        this.margin = margin;

        List<Draggable> tmpMarks = new ArrayList<Draggable>();

        this.viewportModel = viewportModel;

        Draggable viewportStartMark = new AbstractDraggable(this) {

            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                return (int) DLightMath.map(vms.getViewport().getStart(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin - 2);
            }

            @Override
            protected void setPosition(int pos, boolean isAdjusting) {
                ViewportModelState vms = getViewportModelState();
                viewportModel.setViewport(new Range<Long>(DLightMath.map(pos, margin, getWidth() - margin - 2, vms.getLimits().getStart(), vms.getLimits().getEnd()), null));
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            }

            @Override
            protected Color getColor() {
                return Color.BLACK;
            }

            @Override
            protected Shape getShape() {
                return new Rectangle(getPosition(), 0, 2, getHeight());
            }
        };
        Draggable viewportEndMark = new AbstractDraggable(this) {
            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                return (int) DLightMath.map(vms.getViewport().getEnd(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin - 2);
            }

            @Override
            protected void setPosition(int pos, boolean isAdjusting) {
                ViewportModelState vms = getViewportModelState();
                viewportModel.setViewport(new Range<Long>(null, DLightMath.map(pos, margin, getWidth() - margin - 2, vms.getLimits().getStart(), vms.getLimits().getEnd())));
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            }

            @Override
            protected Color getColor() {
                return Color.BLACK;
            }

            @Override
            protected Shape getShape() {
                return new Rectangle(getPosition(), 0, 2, getHeight());
            }
        };
        viewportStartMark.setRightBound(viewportEndMark);
        viewportEndMark.setLeftBound(viewportStartMark);
        tmpMarks.add(viewportStartMark);
        tmpMarks.add(viewportEndMark);
        viewportModel.addChangeListener(this);

        this.filterManager = filterManager;

        Draggable selectionStartMark = new AbstractDraggable(this) {

            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                Range<Long> selection = getTimeSelection();
                if (selection == null) {
                    selection = vms.getLimits();
                }
                return (int) DLightMath.map(selection.getStart(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin);
            }

            @Override
            protected void setPosition(int pos, boolean isAdjusting) {
                ViewportModelState vms = getViewportModelState();
                setTimeSelection(new Range<Long>(DLightMath.map(pos, margin, getWidth() - margin, vms.getLimits().getStart(), vms.getLimits().getEnd()), null), isAdjusting);
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            }

            @Override
            protected Color getColor() {
                return Color.RED;
            }

            @Override
            protected Shape getShape() {
                int pos = getPosition();
                int h = getHeight();
                return new Polygon(new int[] {pos - 10, pos, pos}, new int[] {h, h - 10, h}, 3);
            }
        };
        Draggable selectionEndMark = new AbstractDraggable(this) {

            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                Range<Long> selection = getTimeSelection();
                if (selection == null) {
                    selection = vms.getLimits();
                }
                return (int) DLightMath.map(selection.getEnd(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin);
            }

            @Override
            protected void setPosition(int pos, boolean isAdjusting) {
                ViewportModelState vms = getViewportModelState();
                setTimeSelection(new Range<Long>(null, DLightMath.map(pos, margin, getWidth() - margin, vms.getLimits().getStart(), vms.getLimits().getEnd())), isAdjusting);
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            }

            @Override
            protected Color getColor() {
                return Color.RED;
            }

            @Override
            protected Shape getShape() {
                int pos = getPosition();
                int h = getHeight();
                return new Polygon(new int[] {pos, pos, pos + 10}, new int[] {h, h - 10, h}, 3);
            }
        };
        selectionStartMark.setRightBound(selectionEndMark);
        selectionEndMark.setLeftBound(selectionStartMark);
        tmpMarks.add(selectionStartMark);
        tmpMarks.add(selectionEndMark);
        if (this.filterManager != null) {
            this.filterManager.addDataFilterListener(this);
        }

        this.marks = Collections.unmodifiableList(tmpMarks);
        this.timeMarksProvider = TimeMarksProvider.newInstance();

        DraggingSupport dragAdapter = new DraggingSupport(this, marks);
    }

    // Use this method instead of querying viewportModel directly!
    private ViewportModelState getViewportModelState() {
        return new ViewportModelStateWrapper(viewportModel.getState());
    }

    private Range<Long> getTimeSelection() {
        Collection<TimeIntervalDataFilter> timeFilters = filterManager == null ? null : filterManager.getDataFilter(TimeIntervalDataFilter.class);
        if (timeFilters != null && !timeFilters.isEmpty()) {
            Range<Long> selection = timeFilters.iterator().next().getInterval();
            return new Range<Long>(
                    TimeUnit.NANOSECONDS.toMillis(selection.getStart()),
                    TimeUnit.NANOSECONDS.toMillis(selection.getEnd()));
        } else {
            return null;
        }
    }

    private void setTimeSelection(Range<Long> selection, boolean isAdjusting) {
        if (filterManager != null) {
            if (selection.getStart() == null || selection.getEnd() == null) {
                Range<Long> currentSelection = getTimeSelection();
                ViewportModelState vms = getViewportModelState();
                selection = substituteDefaults(selection, currentSelection == null ? vms.getLimits() : currentSelection);
            }
            filterManager.addDataFilter(TimeIntervalDataFilterFactory.create(new Range<Long>(
                    TimeUnit.MILLISECONDS.toNanos(selection.getStart()),
                    TimeUnit.MILLISECONDS.toNanos(selection.getEnd()))), isAdjusting);
        }
    }

    private Range<Long> substituteDefaults(Range<Long> range, Range<Long> defaults) {
        return new Range<Long>(
                range.getStart() == null ? defaults.getStart() : range.getStart(),
                range.getEnd() == null ? defaults.getEnd() : range.getEnd());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        FontMetrics fm = g.getFontMetrics();
        Range<Long> limits = getViewportModelState().getLimits();
        List<AxisMark> timeMarks = timeMarksProvider.getAxisMarks(
                (int) TimeUnit.MILLISECONDS.toSeconds(limits.getStart()),
                (int) TimeUnit.MILLISECONDS.toSeconds(limits.getEnd()),
                getWidth() - 2 * margin, fm);

        for (AxisMark mark : timeMarks) {
            g.setColor(Color.BLACK);
            g.drawLine(margin + mark.getPosition(), 0, margin + mark.getPosition(), 5);
            if (mark.getText() != null) {
                int length = fm.stringWidth(mark.getText());
                g.drawString(mark.getText(), margin + mark.getPosition() - length / 2, 3 * fm.getAscent() / 2);
            }
        }

        for (Draggable mark : marks) {
            mark.paint(g);
        }
    }

    public void stateChanged(ChangeEvent e) {
        repaint();
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
        repaint();
    }

    /**
     * Hides the fact that we need to extend limits to viewport.
     */
    private static class ViewportModelStateWrapper implements ViewportModelState {

        private final Range<Long> limits;
        private final Range<Long> viewport;

        public ViewportModelStateWrapper(ViewportModelState originalState) {
            viewport = originalState.getViewport();
            limits = originalState.getLimits().extend(viewport);
        }

        public Range<Long> getLimits() {
            return limits;
        }

        public Range<Long> getViewport() {
            return viewport;
        }
    }
}
