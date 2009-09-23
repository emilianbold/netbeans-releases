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
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openide.util.ImageUtilities;

/**
 * @author Alexey Vladykin
 */
/*package*/ class ViewportBar extends JComponent implements ChangeListener, DataFilterListener {

    private static final Image VIEWPORT_HANDLE = ImageUtilities.loadImage("org/netbeans/modules/dlight/extras/resources/viewport_handle.png"); // NOI18N
    private static final Color VIEWPORT_HANDLE_COLOR = new Color(0x72, 0x8A, 0x84);
    private static final int VIEWPORT_HANDLE_WIDTH = VIEWPORT_HANDLE.getWidth(null);
    private static final int VIEWPORT_HANDLE_HEIGHT = VIEWPORT_HANDLE.getHeight(null);

    private static final Image FILTER_HANDLE = ImageUtilities.loadImage("org/netbeans/modules/dlight/extras/resources/filter_handle.png"); // NOI18N
    private static final Color FILTER_HANDLE_COLOR = new Color(0xE7, 0x6F, 0x00);
    private static final int FILTER_HANDLE_WIDTH = FILTER_HANDLE.getWidth(null);
    private static final int FILTER_HANDLE_HEIGHT = FILTER_HANDLE.getHeight(null);

    private final ViewportModel viewportModel;
    private final DataFilterManager filterManager;
    private final Draggable viewportStartMark;
    private final Draggable viewportEndMark;
    private final Draggable selectionStartMark;
    private final Draggable selectionEndMark;
    private final AxisMarksProvider timeMarksProvider;
    private final int margin;

    public ViewportBar(final ViewportModel viewportModel, final DataFilterManager filterManager, final int margin) {
        setMinimumSize(new Dimension(200, 30));
        setPreferredSize(new Dimension(200, 30));
        setOpaque(true);
        this.margin = margin;

        this.viewportModel = viewportModel;

        this.viewportStartMark = new AbstractDraggable(this) {

            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                return (int) DLightMath.map(vms.getViewport().getStart(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin);
            }

            @Override
            protected void setPosition(int pos, boolean isAdjusting) {
                ViewportModelState vms = getViewportModelState();
                Range<Long> viewport = vms.getViewport();
                Long startTime = DLightMath.map(pos, margin, getWidth() - margin, vms.getLimits().getStart(), vms.getLimits().getEnd());
                if (viewport != null && startTime >= viewport.getEnd()){
                    return;
                }
                viewportModel.setViewport(new Range<Long>(startTime, null));
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            }

            @Override
            public void paint(Graphics g) {
                int pos = getPosition();
                g.setColor(VIEWPORT_HANDLE_COLOR);
                g.drawLine(pos, 0, pos, ViewportBar.this.getHeight());
                g.drawImage(VIEWPORT_HANDLE, pos - VIEWPORT_HANDLE_WIDTH / 2, 0, null);
            }

            @Override
            protected Shape getShape() {
                return new Rectangle(
                        getPosition() - VIEWPORT_HANDLE_WIDTH / 2, 0,
                        VIEWPORT_HANDLE_WIDTH, VIEWPORT_HANDLE_HEIGHT);
            }
        };
        this.viewportEndMark = new AbstractDraggable(this) {
            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                return (int) DLightMath.map(vms.getViewport().getEnd(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin);
            }

            @Override
            protected void setPosition(int pos, boolean isAdjusting) {
                ViewportModelState vms = getViewportModelState();
                Range<Long> viewport = vms.getViewport();
                Long endTime = DLightMath.map(pos, margin, getWidth() - margin, vms.getLimits().getStart(), vms.getLimits().getEnd());
                if (viewport != null && viewport.getStart() >= endTime){
                    return;
                }
                viewportModel.setViewport(new Range<Long>(null, endTime));
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            }

            @Override
            public void paint(Graphics g) {
                int pos = getPosition();
                g.setColor(VIEWPORT_HANDLE_COLOR);
                g.drawLine(pos, 0, pos, ViewportBar.this.getHeight());
                g.drawImage(VIEWPORT_HANDLE, pos - VIEWPORT_HANDLE_WIDTH / 2, 0, null);
            }

            @Override
            protected Shape getShape() {
                return new Rectangle(
                        getPosition() - VIEWPORT_HANDLE_WIDTH / 2, 0,
                        VIEWPORT_HANDLE_WIDTH, VIEWPORT_HANDLE_HEIGHT);
            }
        };
        viewportStartMark.setRightBound(viewportEndMark);
        viewportEndMark.setLeftBound(viewportStartMark);
        viewportModel.addChangeListener(this);

        this.filterManager = filterManager;

        this.selectionStartMark = new AbstractDraggable(this) {

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
                Range<Long> selection = ViewportBar.this.getTimeSelection();
                Long startTime = DLightMath.map(pos, margin, getWidth() - margin, vms.getLimits().getStart(), vms.getLimits().getEnd());
                if (selection != null && selection.getEnd() <= startTime){
                    //return
                    return;
                }
                setTimeSelection(new Range<Long>(startTime, null), isAdjusting);
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            }

            @Override
            public void paint(Graphics g) {
                int pos = getPosition();
                g.drawImage(FILTER_HANDLE, pos - FILTER_HANDLE_WIDTH / 2,
                        ViewportBar.this.getHeight() - FILTER_HANDLE_HEIGHT, null);
            }

            @Override
            protected Shape getShape() {
                int pos = getPosition();
                return new Rectangle(
                        pos - FILTER_HANDLE_WIDTH / 2, ViewportBar.this.getHeight() - FILTER_HANDLE_HEIGHT,
                        FILTER_HANDLE_WIDTH, FILTER_HANDLE_HEIGHT);
            }
        };
        this.selectionEndMark = new AbstractDraggable(this) {

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
                Long endTime = DLightMath.map(pos, margin, getWidth() - margin, vms.getLimits().getStart(), vms.getLimits().getEnd());
                Range<Long> selection = ViewportBar.this.getTimeSelection();
                if (selection  != null && selection.getStart() >= endTime){
                    return;
                }
                setTimeSelection(new Range<Long>(null, endTime), isAdjusting);
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            }

            @Override
            public void paint(Graphics g) {
                int pos = getPosition();
                g.drawImage(FILTER_HANDLE, pos - FILTER_HANDLE_WIDTH / 2,
                        ViewportBar.this.getHeight() - FILTER_HANDLE_HEIGHT, null);
            }

            @Override
            protected Shape getShape() {
                int pos = getPosition();
                return new Rectangle(
                        pos - FILTER_HANDLE_WIDTH / 2, ViewportBar.this.getHeight() - FILTER_HANDLE_HEIGHT,
                        FILTER_HANDLE_WIDTH, FILTER_HANDLE_HEIGHT);
            }
        };
        selectionStartMark.setRightBound(selectionEndMark);
        selectionEndMark.setLeftBound(selectionStartMark);
        if (this.filterManager != null) {
            this.filterManager.addDataFilterListener(this);
        }

        this.timeMarksProvider = TimeMarksProvider.newInstance();

        DraggingSupport dragAdapter = new DraggingSupport(this,
                Arrays.asList(viewportStartMark, viewportEndMark, selectionStartMark, selectionEndMark));
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

        viewportStartMark.paint(g);
        viewportEndMark.paint(g);

        int x1 = selectionStartMark.getPosition();
        int x2 = selectionEndMark.getPosition();
        g.setColor(FILTER_HANDLE_COLOR);
        g.fillRect(x1, getHeight() - 5, x2 - x1, 5);
        selectionStartMark.paint(g);
        selectionEndMark.paint(g);
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
