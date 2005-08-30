/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.toolbars;

import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class represents one row of toolbars.
 *
 * Toolbar row is part of toolbar configuration and contains list of toolbars,
 * it is possible to add, remove and switch constraints.
 * There is cached row's neighbournhood, so when there is some row motion
 * those cached values are recomputed.
 *
 * @author Libor Kramolis
 */
public class ToolbarRow {
    /** ToolbarConfiguration */
    ToolbarConfiguration toolbarConfig;
    /** Previous row of toolbars. */
    ToolbarRow prevRow;
    /** Next row of toolbars. */
    ToolbarRow nextRow;

    /** List of toolbars (ToolbarConstraints) in row. */
    private Vector toolbars;
    /** listener for changes of constraints of contained toolbars */
    private PropertyChangeListener constraintsL;
    /** cached preferred height of this row */
    private int prefHeight;
    
    /** Create new ToolbarRow.
     * @param own ToolbarConfiguration
     */
    ToolbarRow (ToolbarConfiguration config) {
        toolbarConfig = config;
        toolbars = new Vector();
        prevRow = nextRow = null;
        // invoke revalidation of toolbar rows below if height changes
        constraintsL = new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {
                if (ToolbarConstraints.PREFERRED_SIZE.equals(evt.getPropertyName())) {
                    Dimension oldTCSize = (Dimension)evt.getOldValue();
                    Dimension newTCSize = (Dimension)evt.getNewValue();
                    if (oldTCSize.height != newTCSize.height) {
                        updateRowsBelow();
                    }
                }
            }
        };
    }

    /** Add toolbar to end of row.
     * @param tc ToolbarConstraints
     */
    void addToolbar (ToolbarConstraints tc) {
        addToolbar2 (tc, toolbars.size());
    }

    /** Add toolbar to specific position
     * @param newTC ToolbarConstraints
     * @param pos specified position of new toolbar
     */
    void addToolbar (ToolbarConstraints newTC, int pos) {
        int index = newTC.checkInitialIndexInRow();
        if( index >= 0 ) {
            //the toolbar is being added for the first time so get its index
            //from the order of declarations in layers xml
            index = Math.min( index, toolbars.size() );
        } else {
            index = 0;
            Iterator it = toolbars.iterator();
            ToolbarConstraints tc;
            while (it.hasNext()) {
                tc = (ToolbarConstraints)it.next();
                if (pos <= tc.getPosition())
                    break;
                index++;
            }
        }
        addToolbar2 (newTC, index);
    }

    /** Add toolbar to specific index int row
     * @param tc ToolbarConstraints
     * @param index specified index of new toolbar
     */
    private void addToolbar2 (ToolbarConstraints tc, int index) {
        if (toolbars.contains (tc))
            return;
        ToolbarConstraints prev = null;
        ToolbarConstraints next = null;
        if (index != 0) {
            prev = (ToolbarConstraints)toolbars.elementAt (index - 1);
            prev.addNextBar (tc);
            tc.addPrevBar (prev);
        }
        if (index < toolbars.size()) {
            next = (ToolbarConstraints)toolbars.elementAt (index);
            tc.addNextBar (next);
            next.addPrevBar (tc);
        }
        if ((prev != null) && (next != null)) {
            prev.removeNextBar (next);
            next.removePrevBar (prev);
        }
        
        int oldHeight = getPreferredHeight();

        tc.addOwnRow (this);
        toolbars.insertElementAt (tc, index);

        tc.updatePosition();

        tc.addPropertyChangeListener(constraintsL);
    }

    /** Remove toolbar from row.
     * @param tc toolbar for remove
     */
    void removeToolbar (ToolbarConstraints tc) {
        int index = toolbars.indexOf (tc);

        ToolbarConstraints prev = null;
        ToolbarConstraints next = null;
        try {
            prev = (ToolbarConstraints)toolbars.elementAt (index - 1);
            prev.removeNextBar (tc);
        } catch (ArrayIndexOutOfBoundsException e) { }
        try {
            next = (ToolbarConstraints)toolbars.elementAt (index + 1);
            next.removePrevBar (tc);
        } catch (ArrayIndexOutOfBoundsException e) { }
        if ((prev != null) && (next != null)) {
            prev.addNextBar (next);
            next.addPrevBar (prev);
        }

        toolbars.removeElement (tc);

        if (prev != null) {
            prev.updatePosition();
        } else {
            if (next != null) {
                next.updatePosition();
            }
        }
        tc.removePropertyChangeListener(constraintsL);
    }

    /** @return Iterator of toolbars int row. */
    Iterator iterator () {
        return toolbars.iterator();
    }

    /** Set a previous row.
     * @param prev new previous row.
     */
    void setPrevRow (ToolbarRow prev) {
        prevRow = prev;
    }

    /** @return previous row. */
    ToolbarRow getPrevRow () {
        return prevRow;
    }

    /** Set a next row.
     * @param next new next row.
     */
    void setNextRow (ToolbarRow next) {
        nextRow = next;
    }

    /** @return next row. */
    ToolbarRow getNextRow () {
        return nextRow;
    }

    /** @return preferred width of row. */
    int getPrefWidth () {
        if (toolbars.isEmpty())
            return -1;
        return ((ToolbarConstraints)toolbars.lastElement()).getPrefWidth();
    }

    /** @return true if row is empty */
    boolean isEmpty () {
        return toolbars.isEmpty();
    }

    /** @return number of toolbars int row. */
    int toolbarCount () {
        return toolbars.size();
    }

    /** Update bounds of all row toolbars. */
    void updateBounds () {
        Iterator it = toolbars.iterator();
        ToolbarConstraints tc;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            tc.updateBounds();
        }
    }

    /** Update position of rows below this one. Called when height of this row
     * has changed.
     */
    private void updateRowsBelow () {
        for (int i = toolbarConfig.rowIndex(this) + 1; i < toolbarConfig.getRowCount(); i++) {
            toolbarConfig.getRow(i).updateBounds();
        }
    }

    /** Switch two toolbars.
     * @param left ToolbarConstraints
     * @param right ToolbarConstraints
     */
    void switchBars (ToolbarConstraints left, ToolbarConstraints right) {
        int leftIndex = toolbars.indexOf (left);
        int rightIndex = toolbars.indexOf (right);
        ToolbarConstraints leftPrev = null;
        ToolbarConstraints rightNext = null;

        try {
            leftPrev = (ToolbarConstraints)toolbars.elementAt (leftIndex - 1);
        } catch (ArrayIndexOutOfBoundsException e) { }
        try {
            rightNext = (ToolbarConstraints)toolbars.elementAt (rightIndex + 1);
        } catch (ArrayIndexOutOfBoundsException e) { }

        if (leftPrev != null)
            leftPrev.removeNextBar (left);
        left.removePrevBar (leftPrev);
        left.removeNextBar (right);

        right.removePrevBar (left);
        right.removeNextBar (rightNext);
        if (rightNext != null)
            rightNext.removePrevBar (right);

        if (leftPrev != null)
            leftPrev.addNextBar (right);
        left.addPrevBar (right);
        left.addNextBar (rightNext);

        right.addPrevBar (leftPrev);
        right.addNextBar (left);
        if (rightNext != null)
            rightNext.addPrevBar (left);

        toolbars.setElementAt (left, rightIndex);
        toolbars.setElementAt (right, leftIndex);
    }

    /** Let's try switch toolbar left.
     * @param ToolbarConstraints
     */
    void trySwitchLeft (ToolbarConstraints tc) {
        int index = toolbars.indexOf (tc);
        if (index == 0)
            return;

        try {
            ToolbarConstraints prev = (ToolbarConstraints)toolbars.elementAt (index - 1);
            if (ToolbarConstraints.canSwitchLeft (tc.getPosition(), tc.getWidth(), prev.getPosition(), prev.getWidth())) {
                switchBars (prev, tc);
            }
        } catch (ArrayIndexOutOfBoundsException e) { /* No left toolbar - it means tc is toolbar like Palette (:-)) */ }
    }

    /** Let's try switch toolbar right.
     * @param ToolbarConstraints
     */
    void trySwitchRight (ToolbarConstraints tc) {
        int index = toolbars.indexOf (tc);

        try {
            ToolbarConstraints next = (ToolbarConstraints)toolbars.elementAt (index + 1);
            if (ToolbarConstraints.canSwitchRight (tc.getPosition(), tc.getWidth(), next.getPosition(), next.getWidth())) {
                switchBars (tc, next);
                next.setPosition (tc.getPosition() - next.getWidth() - ToolbarLayout.HGAP);
            }
        } catch (ArrayIndexOutOfBoundsException e) { /* No right toolbar - it means tc is toolbar like Palette (:-)) */ }
    }
    
    /** @return preferred height of this row. Computed as max from preferred
     * heights of individual toolbars, but not bigger then BASIC_HEIGHT
     */
    int getPreferredHeight () {
        ToolbarConstraints curConstr = null;
        ToolbarPool pool = ToolbarPool.getDefault();
        prefHeight = 0;
        int curHeight = 0;
        for (Iterator iter = toolbars.iterator(); iter.hasNext(); ) {
            curConstr = (ToolbarConstraints)iter.next();
            // compute only from one-row toolbars
            if (curConstr.getRowCount() == 1) {
                Toolbar curToolbar = pool.findToolbar(curConstr.getName());
                // data may be out of sync, see ToolbarConfiguration.updateConfiguration
                // for explanation
                if (curToolbar != null) {
                    curHeight = curToolbar.getPreferredSize().height;
                    if (prefHeight < curHeight) {
                        prefHeight = curHeight;
                    }
                }
            }
        }
        prefHeight = prefHeight <= 0 ? Toolbar.getBasicHeight() : Math.min(Toolbar.getBasicHeight(), prefHeight);
        return prefHeight;
    }

    /** Class to store row in xml format. */
    static class WritableToolbarRow {
        /** List of toolbars. */
        Vector toolbars;  // Vector of ToolbarConstraints.WritableToolbar

        /** Create new WritableToolbarRow.
         */
        public WritableToolbarRow () {
            toolbars = new Vector();
        }

        /** Create new WritableToolbarRow.
         * @param row ToolbarRow
         */
        public WritableToolbarRow (ToolbarRow row) {
            this();
            initToolbars (row);
        }

        /** Init list of writable toolbars. */
        void initToolbars (ToolbarRow r) {
            Iterator it = r.toolbars.iterator();
            while (it.hasNext()) {
                toolbars.addElement (new ToolbarConstraints.WritableToolbar ((ToolbarConstraints)it.next()));
            }
        }

        /** Add toolbar to list of writable toolbars.
         * @param newTC new tested ToolbarConstraints
         */
        void addToolbar (ToolbarConstraints newTC) {
            int index = 0;
            Iterator it = toolbars.iterator();
            ToolbarConstraints.WritableToolbar tc;
            while (it.hasNext()) {
                tc = (ToolbarConstraints.WritableToolbar)it.next();
                if (newTC.getPosition() < tc.position)
                    break;
                index++;
            }

            toolbars.insertElementAt (new ToolbarConstraints.WritableToolbar (newTC), index);
        }

        /** @return true if row is empty */
        boolean isEmpty () {
            return toolbars.isEmpty();
        }

        /** @return ToolbarRow in xml format. */
        public String toString () {
            StringBuffer sb = new StringBuffer();

            sb.append ("  <").append (ToolbarConfiguration.TAG_ROW).append (">\n"); // NOI18N
            Iterator it = toolbars.iterator();
            while (it.hasNext()) {
                sb.append (it.next().toString());
            }
            sb.append ("  </").append (ToolbarConfiguration.TAG_ROW).append (">\n"); // NOI18N

            return sb.toString();
        }
    } // end of class WritableToolbarRow
} // end of class ToolbarRow

