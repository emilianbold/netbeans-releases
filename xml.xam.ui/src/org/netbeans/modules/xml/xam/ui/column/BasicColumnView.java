/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.xml.xam.ui.column;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import org.netbeans.modules.xml.xam.ui.layout.JSplitterBar;
import org.netbeans.modules.xml.xam.ui.layout.SplitterLayout;

/**
 * A Swing widget with a horizontal splitter layout. The split bars can
 * be moved right and left.
 *
 * @author  Jeri Lockhart
 * @author  Nathan Fiedler
 */
public class BasicColumnView extends JPanel implements ColumnView {
    private static final String COLUMN_WEIGHT_1 = "1";   // NOI18N
    private static final int SCROLL_DELAY = 20;
    static final long serialVersionUID = 1L;
    /** columns in order from left to right */
    private List<Column> columnList;
    /** JSplitterBars in order from left to right */
    private List<JSplitterBar> splitterList;
    /** Contains all of the columns and splitters. */
    private JPanel mainParentPanel;

    /**
     * Creates new form BasicColumnView.
     */
    public BasicColumnView() {
        initComponents();
        columnList = new ArrayList<Column>();
        splitterList = new ArrayList<JSplitterBar>();

        mainParentPanel = new MainPanel();
        mainParentPanel.setBackground(Color.WHITE);
        mainParentPanel.setLayout(new SplitterLayout(false));
        scrollPane.setViewportView(mainParentPanel);
        scrollPane.setViewportBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                validate();
                revalidate();
            }

        });
    }

    /**
     * Appends the column to the list, without revalidating or scrolling.
     *
     * @param  column  the Column to add.
     */
    protected void appendColumnToList(Column column) {
        if (column == null) {
            return;
        }
        JComponent comp = column.getComponent();
        if (comp == null) {
            return;
        }
        columnList.add(column);
        mainParentPanel.add(COLUMN_WEIGHT_1, comp);
        JSplitterBar bar = new JSplitterBar();
        mainParentPanel.add(bar);
        splitterList.add(bar);
    }

    public void appendColumn(Column column) {
        appendColumnToList(column);
        // Call validate() which calls SplitterLayout layoutContainer()
        // layoutContainer() will set a new preferredSize on the
        // mainParentPanel that will be used by the scroll pane.
        validate();
        mainParentPanel.revalidate();
        scrollToColumn(column, false);
    }

    public void appendColumns(Column[] columns) {
        for (Column column : columns) {
            appendColumnToList(column);
        }
        validate();
        mainParentPanel.revalidate();
        scrollToColumn(columns[columns.length -  1], false);
    }

    public void clearColumns(){
        mainParentPanel.removeAll();
        columnList.clear();
        splitterList.clear();
        mainParentPanel.revalidate();
        mainParentPanel.repaint();
    }

    public void removeColumnsAfter(Column column) {
        if (column == null) {
            return;
        }

        if (!isLastColumn(column)) {
            // We have to scroll the view (synchronously) before removing
            // the column, because as soon as we remove it, the table resizes
            // and revalidates (shrinks).
            scrollToColumn(getNextColumn(column), true);
        }

        // Remove the columns and splitters from the lists and the panel.
        int loc = columnList.indexOf(column);
        for (int ii = columnList.size() - 1; ii > loc; ii--) {
            Column col = columnList.remove(ii);
            Component comp = col.getComponent();
            mainParentPanel.remove(comp);
        }
        for (int ii = splitterList.size() - 1; ii > loc; ii--) {
            JSplitterBar bar = splitterList.remove(ii);
            mainParentPanel.remove(bar);
        }
        mainParentPanel.revalidate();
        mainParentPanel.repaint();
    }

    public void scrollToColumn(final Column column, boolean synchronous) {
        if (column == null) {
            return;
        }
        if (synchronous) {
            if (!EventQueue.isDispatchThread()) {
                try {
                    // Invoke ourselves immediately on the event thread
                    EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            scrollToColumn(column);
                        }
                    });
                } catch (Exception e) {
                    return;
                }
            } else {
                // This is the event thread. Invoke the actual scrolling code.
                scrollToColumn(column);
            }
        } else {
            // Return now and let the invokeLater() do the scrolling
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    // Invoke ourselves later
                    scrollToColumn(column);
                }
            });
        }
    }

    /**
     * Scrolls the viewport to make the specified column the last column
     * visible. This method must be invoked on the AWT/Swing thread.
     *
     * @throws	IllegalStateException
     *		if this method is invoked on a non-AWT thread.
     */
    protected void scrollToColumn(Column column) {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("This method can only be " +
                    "invoked on the AWT event processing thread");
        }

        // If this column isn't in the table, return immediately.
        // Likewise, don't scroll if there is only one column.
        // Also don't scroll if next column is the last one, See IZ100119.
        int columnIndex = columnList.indexOf(column);
        if ( (columnIndex == -1) ||
             (columnList.size() <= 1) ||
             (columnList.size() -2  == columnIndex) ) {
            return;
        }

        // This is the bounds of the column: the x,y is the coordinate
        // of the left side of the column relative to the left side of
        // the viewport, and width, height is the size of the column.
        Rectangle viewBounds = column.getComponent().getBounds();
        viewBounds.width += 5;

        // This is the bounds of the viewport
        JViewport viewport = scrollPane.getViewport();
        Rectangle viewportBounds = viewport.getViewRect();

        // Calculate the distance we need to move the viewport
        final int DELTA = (int) ((viewportBounds.getX() +
                viewportBounds.getWidth()) - (viewBounds.getX() +
                viewBounds.getWidth()));
        if (DELTA == 0) {
            return;
        }

        // Calculate the number of columns we need to move so we can have
        // a basically constant per-column scroll rate.
        // Get the number of columns between the specified column and the
        // last showing column.  We check a point relative (-10,10) to the
        // upper-rightmost point of the viewport so that we don't
        // accidentally hit any non-column JTable pixels.
        int deltaColumns = Math.abs(lastShowingColumnIndex() - columnIndex);

        Point position = viewport.getViewPosition();

        final int STEPS = 5 * (deltaColumns == 0 ? 1 : deltaColumns);
        final int INCREMENT = DELTA / STEPS;

        for (int step = 0; step < STEPS; step++) {
            int newX = (int) position.getX() - INCREMENT;
            if (newX <= 0) {
                break;
            }
            // Add a fudge factor of 1 pixel to insulate against roundoff
            // errors that cause the view not to scroll fully to the right
            newX += 1;
//            //Fix for IZ
//            try {
//                // Pause briefly to perform the synchronous animation.
//                // Using a Timer simply will not work, as this method
//                // must return only after the animation is complete.
//                Thread.currentThread().sleep(SCROLL_DELAY);
//            } catch (InterruptedException ie) {
//                // Do nothing
//            }

            position = new Point(newX, (int) position.getY());
            viewport.setViewPosition(position);
        }
    }

    /**
     * Indicates if the given Column is the last one in the view.
     *
     * @return  true if last column, false otherwise.
     */
    protected boolean isLastColumn(Column column){
        if (column == null){
            return false;
        }
        return columnList.indexOf(column) == columnList.size()-1;

    }

    public int getColumnCount() {
        return columnList.size();
    }

    public Column getFirstColumn() {
        if (columnList.size() > 0) {
            return columnList.get(0);
        } else {
            return null;
        }
    }

    public Column getNextColumn(Column column){
        if (column == null) {
            return null;
        }
        if (isLastColumn(column)) {
            return null;
        }
        return columnList.get(columnList.indexOf(column) + 1);
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        int index = lastShowingColumnIndex();
        if (index > -1) {
            Column column = columnList.get(index);
            column.getComponent().requestFocus();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean retVal = super.requestFocusInWindow();
        int index = lastShowingColumnIndex();
        if (index > -1) {
            Column column = columnList.get(index);
            return column.getComponent().requestFocusInWindow();
        }
        return retVal;
    }

    /**
     * Determine the index of the right-most visible column.
     *
     * @return  index of last visible column (-1 if no columns).
     */
    private int lastShowingColumnIndex() {
        int index = -1;
        for (int ii = columnList.size() - 1; ii > -1; ii--){
            if (columnList.get(ii).getComponent().isShowing()) {
                index = ii;
                break;
            }
        }
        return index;
    }

    /**
     * Convert a point in view coordinates to the closest index of the
     * column at that location.
     *
     * @param  location   the coordinates of the column, relative to the
     *                    scrollpane viewport.
     * @param  direction  less than zero for left, greater than zero for right.
     * @return  the index of the column at the given location, or -1.
     */
    private int locationToIndex(Point location, int direction) {
        int index = -1;
        Component comp = mainParentPanel.getComponentAt(location);
        if (comp instanceof JSplitterBar) {
            // The columns and splitters have a 1:1 relationship, so the
            // index into the splitter list is equal to the index into
            // the column list for the splitter's paired column.
            index = splitterList.indexOf(comp);
            if (direction > 0) {
                // Moving to the right, favor the column to the right
                // of the splitter.
                index++;
            }
        } else {
            Component[] comps = mainParentPanel.getComponents();
            // Components consist of column/splitter pairs.
            for (int ii = 0; ii < comps.length; ii += 2) {
                if (comps[ii] == comp) {
                    index = ii / 2;
                    break;
                }
            }
        }
        return index;
    }

    public int getColumnIndex(Column column) {
        return columnList.indexOf(column);
    }

    public void addNotify() {
        super.addNotify();
        Container parent = getParent();
        assert !(parent instanceof JViewport) :
            "BasicColumnView has its own scrollpane. " +
                "Do not place BasicColumnView in a scrollpane.";
    }

    /**
     * Panel that manages the scrolling behavior of the column view.
     */
    private class MainPanel extends JPanel implements Scrollable {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect,
                int orientation, int direction) {
            // If all else fails, default to the view width.
            int inc = visibleRect.width;
            if (direction > 0) {
                // Scroll to the right.
                int last = locationToIndex(new Point(visibleRect.x +
                        visibleRect.width - 1, visibleRect.y), direction);
                if (last >= 0 && last < columnList.size()) {
                    Column col = columnList.get(last);
                    Rectangle lastRect = col.getComponent().getBounds();
                    if (lastRect != null) {
                        inc = lastRect.x - visibleRect.x;
                        if (inc < 0) {
                            inc += lastRect.width;
                        } else if (inc == 0 && last < columnList.size() - 1) {
                            inc = lastRect.width;
                        }
                    }
                }
            } else {
                // Scroll to the left.
                int first = locationToIndex(new Point(visibleRect.x -
                        visibleRect.width, visibleRect.y), direction);
                if (first >= 0 && first < columnList.size()) {
                    Column col = columnList.get(first);
                    Rectangle firstRect = col.getComponent().getBounds();
                    if (firstRect != null) {
                        if (firstRect.x < visibleRect.x - visibleRect.width) {
                            if (firstRect.x + firstRect.width >= visibleRect.x) {
                                inc = visibleRect.x - firstRect.x;
                            } else {
                                inc = visibleRect.x - firstRect.x - firstRect.width;
                            }
                        } else {
                            inc = visibleRect.x - firstRect.x;
                        }
                    }
                }
            }
            return inc;
        }

        public boolean getScrollableTracksViewportHeight() {
            return true;
        }

        public boolean getScrollableTracksViewportWidth() {
            if (getParent() instanceof JViewport) {
                return (getParent().getWidth() > getPreferredSize().width);
            }
            return false;
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect,
                int orientation, int direction) {
	    int index = locationToIndex(visibleRect.getLocation(), direction);
            if (index >= 0 && index < columnList.size()) {
                Column col = columnList.get(index);
                Rectangle bounds = col.getComponent().getBounds();
                if (bounds != null) {
                    if (bounds.x != visibleRect.x) {
                        if (direction < 0) {
                            return Math.abs(bounds.x - visibleRect.x);
                        }
                        return bounds.width + bounds.x - visibleRect.x;
                    }
                    // Need to compensate for the width of the splitter.
                    // They are all same width, so first one is sufficient.
                    JSplitterBar bar = splitterList.get(0);
                    return bounds.width + bar.getWidth();
                }
            }
            // If all else fails, return the default value.
            return 1;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        scrollPane = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        setBackground(java.awt.Color.white);
        scrollPane.setBackground(java.awt.Color.white);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        add(scrollPane, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
