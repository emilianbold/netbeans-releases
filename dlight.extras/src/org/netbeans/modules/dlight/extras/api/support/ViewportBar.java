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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.datafilter.DataFilterManager;
import org.netbeans.modules.dlight.extras.api.AxisMark;
import org.netbeans.modules.dlight.extras.api.AxisMarksProvider;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.extras.api.ViewportModelState;
import org.netbeans.modules.dlight.management.timeline.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.management.timeline.TimeIntervalDataFilterFactory;
import org.netbeans.modules.dlight.util.DLightMath;

/**
 * @author Alexey Vladykin
 */
/*package*/ class ViewportBar extends JComponent implements ChangeListener, DataFilterListener {

    private final ViewportModel viewportModel;
    private final DataFilterManager filterManager;
    private final List<Mark> marks;
    private final AxisMarksProvider timeMarksProvider;
    private final int margin;

    public ViewportBar(final ViewportModel viewportModel, final DataFilterManager filterManager, final int margin) {
        setMinimumSize(new Dimension(200, 30));
        setPreferredSize(new Dimension(200, 30));
        setOpaque(true);
        this.margin = margin;

        List<Mark> tmpMarks = new ArrayList<Mark>();

        this.viewportModel = viewportModel;

        Mark viewportStartMark = new AbstractMark() {

            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                return (int) DLightMath.map(vms.getViewport().getStart(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin - 2);
            }

            @Override
            protected void setPosition(int pos) {
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
        };
        Mark viewportEndMark = new AbstractMark() {

            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                return (int) DLightMath.map(vms.getViewport().getEnd(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin - 2);
            }

            @Override
            protected void setPosition(int pos) {
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
        };
        viewportStartMark.setRightBound(viewportEndMark);
        viewportEndMark.setLeftBound(viewportStartMark);
        tmpMarks.add(viewportStartMark);
        tmpMarks.add(viewportEndMark);
        viewportModel.addChangeListener(this);

        this.filterManager = filterManager;

        Mark selectionStartMark = new AbstractMark() {

            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                Range<Long> selection = getTimeSelection();
                if (selection == null) {
                    selection = vms.getLimits();
                }
                return (int) DLightMath.map(selection.getStart(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin - 2);
            }

            @Override
            protected void setPosition(int pos) {
                ViewportModelState vms = getViewportModelState();
                setTimeSelection(new Range<Long>(DLightMath.map(pos, margin, getWidth() - margin - 2, vms.getLimits().getStart(), vms.getLimits().getEnd()), null), dragging);
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            }

            @Override
            public void finishDragging() {
                super.finishDragging();
                ViewportModelState vms = getViewportModelState();
                int pos = getPosition();
                setTimeSelection(new Range<Long>(DLightMath.map(pos, 0, getWidth() - 2, vms.getLimits().getStart(), vms.getLimits().getEnd()), null), false);
            }


            @Override
            protected Color getColor() {
                return Color.RED;
            }
        };
        Mark selectionEndMark = new AbstractMark() {

            @Override
            public int getPosition() {
                ViewportModelState vms = getViewportModelState();
                Range<Long> selection = getTimeSelection();
                if (selection == null) {
                    selection = vms.getLimits();
                }
                return (int) DLightMath.map(selection.getEnd(), vms.getLimits().getStart(), vms.getLimits().getEnd(), margin, getWidth() - margin - 2);
            }

            @Override
            protected void setPosition(int pos) {
                ViewportModelState vms = getViewportModelState();
                setTimeSelection(new Range<Long>(null, DLightMath.map(pos, margin, getWidth() - margin - 2, vms.getLimits().getStart(), vms.getLimits().getEnd())), dragging);
            }

            @Override
            public Cursor getCursor() {
                return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            }

            @Override
            public void finishDragging() {
                super.finishDragging();
                ViewportModelState vms = getViewportModelState();
                int pos = getPosition();
                setTimeSelection(new Range<Long>(null, DLightMath.map(pos, 0, getWidth() - 2, vms.getLimits().getStart(), vms.getLimits().getEnd())), false);
                
            }

            @Override
            protected Color getColor() {
                return Color.RED;
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

        DragAdapter dragAdapter = new DragAdapter();
        addMouseListener(dragAdapter);
        addMouseMotionListener(dragAdapter);
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

        for (Mark mark : marks) {
            mark.paint(g);
        }
    }

    private Mark findMark(Point p) {
        for (Mark mark : marks) {
            if (mark.containsPoint(p)) {
                return mark;
            }
        }
        return null;
    }

    private void changeCursor(Point p) {
        Mark markAtPosition = findMark(p);
        if (markAtPosition == null) {
            setCursor(Cursor.getDefaultCursor());
        } else {
            setCursor(markAtPosition.getCursor());
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

    private class DragAdapter extends MouseAdapter implements MouseMotionListener {

        private Mark markBeingDragged;

        @Override
        public void mouseMoved(MouseEvent e) {
            changeCursor(e.getPoint());
            e.consume();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (markBeingDragged != null) {
                markBeingDragged.dragTo(e.getPoint());
                e.consume();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && markBeingDragged == null) {
                markBeingDragged = findMark(e.getPoint());
                if (markBeingDragged != null) {
                    markBeingDragged.startDragging();
                }
                e.consume();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && markBeingDragged != null) {
                markBeingDragged.dragTo(e.getPoint());
                markBeingDragged.finishDragging();
                markBeingDragged = null;
                e.consume();
            }
        }
    }

    private interface Mark {

        void setLeftBound(Mark mark);

        void setRightBound(Mark mark);

        int getPosition();

        boolean containsPoint(Point p);

        void startDragging();

        void dragTo(Point p);

        void finishDragging();

        void paint(Graphics g);

        Cursor getCursor();
    }

    private abstract class AbstractMark implements Mark {

        protected boolean dragging;
        private Mark leftBound;
        private Mark rightBound;

        public AbstractMark() {
            this.dragging = false;
        }

        public void setLeftBound(Mark leftBound) {
            this.leftBound = leftBound;
        }

        public void setRightBound(Mark rightBound) {
            this.rightBound = rightBound;
        }

        public abstract int getPosition();

        protected abstract void setPosition(int pos);

        protected abstract Color getColor();

        public boolean containsPoint(Point p) {
            int pos = getPosition();
            return pos <= p.x && p.x <= pos + 2;
        }

        public void startDragging() {
            if (!dragging) {
                dragging = true;
            }
        }

        public void dragTo(Point p) {
            if (dragging) {
                int leftBoundPos = leftBound == null? margin : leftBound.getPosition() + 2;
                int rightBoundPos = rightBound == null? ViewportBar.this.getWidth() - margin - 2 : rightBound.getPosition() - 2;
                int newPos;
                if (p.x < leftBoundPos) {
                    newPos = leftBoundPos;
                } else if (rightBoundPos < p.x) {
                    newPos = rightBoundPos;
                } else {
                    newPos = p.x;
                }
                setPosition(newPos);
            }
        }

        public void finishDragging() {
            if (dragging) {
                dragging = false;
            }
        }

        public void paint(Graphics g) {
            g.setColor(getColor());
            int pos = getPosition();
            g.fillRect(pos, 0, 2, ViewportBar.this.getHeight());
        }
    }
}
