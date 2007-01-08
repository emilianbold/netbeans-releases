/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.toolbars;

import org.openide.awt.Toolbar;

/** Listener for toolbar motion.
 *
 * When somebody works (drag and drop) with any toolbar and this work comes up to this listener.
 * Listener is adherented to ToolbarConfiguration so every toolbar's motion is reflected in
 * this ToolbarConfiguration and it's appropriate ToolbarConstraints.
 * So this is only place, where the ToolbarConfiguration is changed by toolbar's motion.
 *
 * @author Libor Kramolis
 */
public class ToolbarDnDListener extends Object implements Toolbar.DnDListener {
    /** now dragged toolbar */
    private ToolbarConstraints draggedToolbar;
    private ToolbarConfiguration configuration;

    /** Create new Toolbar listener.
     * @param conf specified toolbat configuration.
     */
    public ToolbarDnDListener (ToolbarConfiguration conf) {
        configuration = conf;
    }

    /** Move toolbar and followers horizontaly.
     * @param tc first moved toolbar
     * @param dx horizontaly distance
     */
    protected void moveToolbar2EndHorizontally (ToolbarConstraints tc, int dx) {
        if (dx == 0) // no move
            return;

        if (dx < 0)
            tc.moveLeft2End (-dx);
        if (dx > 0)
            tc.moveRight2End (dx);
    }

    /** Move toolbar horizontaly.
     * @param tc moved toolbar
     * @param dx horizontal distance
     */
    protected void moveToolbarHorizontally (ToolbarConstraints tc, int dx) {
        if (dx == 0) // no move
            return;

        if (dx < 0)
            tc.moveLeft (-dx);
        if (dx > 0)
            tc.moveRight (dx);
    }

    /** Move toolbar verticaly.
     * @param tc moved toolbar
     * @param dy vertical distance
     */
    protected void moveToolbarVertically (ToolbarConstraints tc, int dy) {
        if (dy == 0) // no move
            return;

        if (dy < 0)
            moveUp (tc, -dy);
        if (dy > 0)
            moveDown (tc, dy);
    }

    /** Try move toolbar up.
     * @param tc moved toolbar
     * @param dy vertical distance
     */
    protected void moveUp (ToolbarConstraints tc, int dy) {
        if (dy < ((Toolbar.getBasicHeight() / 2) + 2))
            return;

        int rI = tc.rowIndex();
        if (draggedToolbar.isAlone()) { // is alone on row(s) -> no new rows
            if (rI == 0) // in first row
                return;
        }

        int pos = rI - 1;
        tc.destroy();

        int plus = 0;
        int rowCount = configuration.getRowCount();
        for (int i = pos; i < pos + tc.getRowCount(); i++) {
            configuration.getRow (i + plus).addToolbar (tc, tc.getPosition());
            if (rowCount != configuration.getRowCount()) {
                rowCount = configuration.getRowCount();
                plus++;
            }
        }
        configuration.checkToolbarRows();
        configuration.updateBounds( configuration.getRow(0) );
    }

    /** Try move toolbar down.
     * @param tc moved toolbar
     * @param dy vertical distance
     */
    public void moveDown (ToolbarConstraints tc, int dy) {
        int rI = tc.rowIndex();

        int step = ((Toolbar.getBasicHeight() / 2) + 2);

        if (draggedToolbar.isAlone()) { // is alone on row(s) -> no new rows
            if (rI == (configuration.getRowCount() - tc.getRowCount())) // in last rows
                return;
            step = ((Toolbar.getBasicHeight() / 4) + 2);
        }

        if (dy < step)
            return;

        int pos = rI + 1;
        tc.destroy();
        
        for (int i = pos; i < pos + tc.getRowCount(); i++)
            configuration.getRow (i).addToolbar (tc, tc.getPosition());

        configuration.checkToolbarRows();
    }
    
    ///////////////////////////
    // from Toolbar.DnDListener

    /** Invoced when toolbar is dragged. */
    public void dragToolbar (Toolbar.DnDEvent e) {
        if (draggedToolbar == null) {
            draggedToolbar = configuration.getToolbarConstraints (e.getName());
        }

        switch (e.getType()) {
        case Toolbar.DnDEvent.DND_LINE:
            // not implemented yet - it's bug [1]
            // not implemented in this version
            return; // only Toolbar.DnDEvent.DND_LINE
        case Toolbar.DnDEvent.DND_END:
            moveToolbar2EndHorizontally (draggedToolbar, e.getDX());
            break;
        case Toolbar.DnDEvent.DND_ONE:
            moveToolbarVertically (draggedToolbar, e.getDY());
            break;
        }
        if (e.getType() == Toolbar.DnDEvent.DND_ONE)
            moveToolbarHorizontally (draggedToolbar, e.getDX());

        draggedToolbar.updatePosition();

        configuration.revalidateWindow();
    }

    /** Invoced when toolbar is dropped. */
    public void dropToolbar (Toolbar.DnDEvent e) {
        dragToolbar (e);

        configuration.reflectChanges();
        draggedToolbar = null;
    }
} // end of class ToolbarDnDListener

