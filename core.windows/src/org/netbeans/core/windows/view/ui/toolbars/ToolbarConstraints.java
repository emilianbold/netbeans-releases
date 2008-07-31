/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.windows.view.ui.toolbars;

import org.openide.awt.Toolbar;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.Vector;

/** An object that encapsulates position and (optionally) size for
 * Absolute positioning of components.
 *
 * Each toolbar constraints (TC) is component of toolbar row(s) and configuration.
 * Every TC has cached the nearest neighbournhood, so there is a list of rows for TC is part,
 * list of previous and next toolbars.
 * So when there is some motion all of those cached attributes are recomputed.
 *
 * @author Libor Kramolis
 */
public class ToolbarConstraints {
    static final long serialVersionUID =3065774641403311880L;

    /** Toolbar anchor status. */
    static final int LEFT_ANCHOR  = -1;
    static final int NO_ANCHOR    =  0;
    
    public static final String PREFERRED_SIZE = "preferredSize";

    /** Toolbar name */
    private String    name;
    /** Toolbar horizontal position */
    private int       position;
    /** Which anchor toolbar use. */
    private int       anchor;    // LEFT_ANCHOR | NO_ANCHOR
    /** Is toolbar visible. */
    private boolean   visible;
    /** Toolbar is part of those rows. */
    private Vector<ToolbarRow>    ownRows;
    /** List of previous toolbars. */
    private Vector<ToolbarConstraints>    prevBars;
    /** List of next toolbars. */
    private Vector<ToolbarConstraints>    nextBars;
    /** The nearest end of previous toolbars. */
    private int       prevEnd;   // nejblizsi konec predchozich toolbaru
    /** The nearest begin of next toolbars. */
    private int       nextBeg;   // nejblizsi zacatek nasledujicich toolbaru
    /** The nearest begin of previous toolbars. */
    private int       prevBeg;   // nejblizsi zacatek predchozich toolbaru
    /** The nearest end of next toolbars. */
    private int       nextEnd;   // nejblizsi konec nasledujicich toolbaru

    /** Preferred size. */
    private Dimension prefSize;
    /** Toolbar bounds. */
    private Rectangle bounds;
    /** Toolbar constraints is part of ToolbarConfiguration. */
    private ToolbarConfiguration toolbarConfig;
    /** Number of rows. */
    private int       rowCount;
    /** Width of last toolbar. */
    private int       prefLastWidth;
    /** Last row index. */
    private int       lastRowIndex;
    /** The toolbar index as defined by the order of declarations in layers.
        Value -1 means that absolute pixel positioning will be used instead. */
    private int initialIndexInRow;
    
    private boolean alwaysRight = false;
    
    private PropertyChangeSupport propSupport;

    ToolbarConstraints (ToolbarConfiguration conf, String nam, Integer pos, Boolean vis, boolean alwaysRight) {
        this( conf, nam, pos, vis, -1, alwaysRight );
    }
    
    /** Create new ToolbarConstraints
     * @param conf own ToolbarConfiguration
     * @param nam name of toolbar
     * @param pos wanted position of toolbar
     * @param vis visibility of toolbar
     */
    ToolbarConstraints (ToolbarConfiguration conf, String nam, Integer pos, Boolean vis, int initialIndexInRow, boolean alwaysRight) {
        toolbarConfig = conf;
        name = nam;
        if (pos == null) {
            position = 0;
            anchor = LEFT_ANCHOR;
            this.initialIndexInRow = initialIndexInRow;
        } else {
            position = pos.intValue();
            anchor = NO_ANCHOR;
            //the absolute positioning takes precedence over the order of 
            //declarations in layers
            this.initialIndexInRow = -1;
        }
        this.alwaysRight = alwaysRight;
        visible = vis.booleanValue();

        prefSize = new Dimension ();
        rowCount = 0;
        prefLastWidth = 0;
        bounds = new Rectangle ();

        initValues();
    }
    
    boolean isAlwaysRight() {
        return alwaysRight;
    }

    /** Init neighbourhood values. */
    void initValues () {
        ownRows = new Vector<ToolbarRow>();
        prevBars = new Vector<ToolbarConstraints>();
        nextBars = new Vector<ToolbarConstraints>();

        resetPrev();
        resetNext();
    }

    /** Checks position and visibility of multirow toolbar.
     * @param position maybe new position
     * @param visible maybe new visibility
     */
    void checkNextPosition (Integer position, Boolean visible) {
            if (position == null) {
                this.position = 0;
                this.anchor = LEFT_ANCHOR;
            } else {
                if (anchor == NO_ANCHOR)
                    this.position = (this.position + position.intValue()) / 2;
                else
                    this.position = position.intValue();
                this.anchor = NO_ANCHOR;
            }
        this.visible = this.visible || visible.booleanValue();
    }

    /** @return name of toolbar. */
    String getName () {
        return name;
    }

    /** @return anchor of toolbar. */
    int getAnchor () {
        return anchor;
    }

    /** Set anchor of toolbar.
     * @param anch new toolbar anchor.
     */
    void setAnchor (int anch) {
        anchor = anch;
    }

    /** @return toolbar visibility. */
    boolean isVisible () {
        return visible;
    }

    /** Set new toolbar visibility.
     * @param v new toolbar visibility
     */
    void setVisible (boolean v) {
        visible = v;
    }

    /** @return horizontal toolbar position. */
    int getPosition () {
        return position;
    }

    /** Set new toolbar position.
     * @param pos new toolbar position
     */
    void setPosition (int pos) {
        position = pos;
    }

    /** @return toolbar width. */
    int getWidth () {
        return prefSize.width;
    }

    /** @return number toolbar rows. */
    int getRowCount () {
        return rowCount;
    }
    
    int checkInitialIndexInRow() {
        int retValue = initialIndexInRow;
        initialIndexInRow = -1;
        return retValue;
    }
    
    /** @return toolbar bounds. */
    Rectangle getBounds () {
        return new Rectangle (bounds);
    }

    /** Destroy toolbar and it's neighbourhood (row context).
     * @return true if after destroy stay some empty row.
     */
    boolean destroy () {
        lastRowIndex = rowIndex();
        rowCount = ownRows.size();

        boolean emptyRow = false;
        for (ToolbarRow row: ownRows) {
            row.removeToolbar (this);
            emptyRow = emptyRow || row.isEmpty();
        }
        initValues();
        return emptyRow;
    }

    /** Add row to owned rows.
     * @param row new owned row.
     */
    void addOwnRow (ToolbarRow row) {
        ownRows.add (row);
    }

    /** Add toolbar to list of previous toolbars.
     * @param prev new previous toolbar
     */
    void addPrevBar (ToolbarConstraints prev) {
        if (prev == null)
            return;
        prevBars.add (prev);
        
       // #102450 - keep structure correct
        if (nextBars.contains(prev)) {
            nextBars.remove(prev);
        }
    }

    /** Add toolbar to list of next toolbars.
     * @param next new next toolbar
     */
    void addNextBar (ToolbarConstraints next) {
        if (next == null)
            return;
        nextBars.add (next);
        
       // #102450 - keep structure correct
        if (prevBars.contains(next)) {
            prevBars.remove(next);
        }
    }

    /** Remove toolbar from previous toolbars.
     * @param prev toolbar for remove.
     */
    void removePrevBar (ToolbarConstraints prev) {
        if (prev == null)
            return;
        prevBars.remove (prev);
    }

    /** Remove toolbar from next toolbars.
     * @param next toolbar for remove.
     */
    void removeNextBar (ToolbarConstraints next) {
        if (next == null)
            return;
        nextBars.remove (next);
    }

    /** Set preferred size of toolbar. There is important recompute toolbar neighbourhood.
     * @param size preferred size
     */
    void setPreferredSize (Dimension size) {
        Dimension oldSize = prefSize;
        prefSize = size;
        // #102450 - don't allow row rearrangement during icon size toggle 
        if (!toolbarConfig.isTogglingIconSize()) {
            rowCount = Toolbar.rowCount (prefSize.height);
        }

        if (ownRows.isEmpty())
            return;

        ToolbarRow row;

        // #102450 - don't allow row rearrangement during icon size toggle 
        if (visible && !toolbarConfig.isTogglingIconSize()) {
            boolean emptyRow = false;
            while (rowCount < ownRows.size()) {
                row = ownRows.lastElement();
                row.removeToolbar (this);
                ownRows.remove (row);
                emptyRow = emptyRow || row.isEmpty();
            }
            if (emptyRow)
                toolbarConfig.checkToolbarRows();
            while (rowCount > ownRows.size()) {
                row = ownRows.lastElement();
                ToolbarRow nR = row.getNextRow();
                if (nR == null)
                    nR = toolbarConfig.createLastRow();
                nR.addToolbar (this, position);
            }
        }
        updatePosition();
        propSupport.firePropertyChange(PREFERRED_SIZE, oldSize, prefSize); 
    }

    /** @return index of first toolbar row. */
    int rowIndex () {
        if (!visible)
            return toolbarConfig.getRowCount();
        if (ownRows.isEmpty())
            return lastRowIndex;
        return toolbarConfig.rowIndex (ownRows.firstElement());
    }

    /** @return true if toolbar is alone at row(s). */
    boolean isAlone () {
        for (ToolbarRow row: ownRows) {
            if (row.toolbarCount() != 1)
                return false;
        }
        return true;
    }

    /** Update preferred size of toolbar.
     * @param size new preferred size
     */
    void updatePreferredSize (Dimension size) {
        if (!prefSize.equals (size)) {
            setPreferredSize (size);
        }
    }

    /** Update toolbar bounds. */
    void updateBounds () {
        if (ownRows.size() > 0) {
            Iterator<ToolbarRow> iter = ownRows.iterator();
            ToolbarRow firstRow = iter.next();
            int toolbarHeight = firstRow.getPreferredHeight();
            while (iter.hasNext()) {
                toolbarHeight += iter.next().getPreferredHeight() + ToolbarLayout.VGAP;
            }
            bounds = new Rectangle (position, toolbarConfig.getRowVertLocation(firstRow),
                                    nextBeg - position - ToolbarLayout.HGAP, toolbarHeight);
        }
        else {
            bounds = new Rectangle(position, 0, 0, 0);
        }
    }

    /** Update toolbar position and it's neighbourhood. */
    void updatePosition () {
        updatePrev();
        if (anchor == NO_ANCHOR) {
            if (position < (prevEnd + ToolbarLayout.HGAP)) {
                position = prevEnd + ToolbarLayout.HGAP;
                anchor = LEFT_ANCHOR;
            }
        } else {
            position = prevEnd + ToolbarLayout.HGAP;
        }
        updatePrevBars();
        updateNextBars();
        updateBounds();
        updatePrefWidth();
    }

    /** Update positions of previous toolbars. */
    void updatePrevPosition () {
        Iterator it = prevBars.iterator();
        ToolbarConstraints tc;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            tc.updatePosition();
        }
    }

    /** Update next position of previous toolbars. */
    void updatePrevBars () {
        for (ToolbarConstraints tc: prevBars) {
            tc.updateNext();
        }
    }

    /** Update previous position of next toolbars. */
    void updateNextBars () {
        Iterator<ToolbarConstraints> it = nextBars.iterator();
        ToolbarConstraints tc;
        if (!it.hasNext()) {
            resetNext();
            updatePrefWidth();
        }
        while (it.hasNext()) {
            tc = it.next();
            //hotfix for issue 31822, ToolbarConstraint endless loop.  Core
            //problem is that somehow nextBars ends up containing this 
            //constraint
            if (tc != this) tc.updatePosition();
        }
    }

    /** Update width of prevoius toolbars. */
    void updatePrefWidth () {
        if (nextBars.size() == 0) {
            prefLastWidth = getPosition() + getWidth() + ToolbarLayout.HGAP;
            toolbarConfig.updatePrefWidth();
        }
    }

    /** @return preferred toolbar width. */
    int getPrefWidth () {
        return prefLastWidth;
    }
    
    int getPrefHeight () {
        return prefSize.height;
    }

    /** Update values about next toolbars. */
    void updateNext () {
        resetNext();
        Iterator it = nextBars.iterator();
        ToolbarConstraints tc;
        int nextPos;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            if (!tc.isAlwaysRight()) {
                nextBeg = Math.min (nextBeg, nextPos = tc.getPosition());
                nextEnd = Math.min (nextEnd, nextPos + tc.getWidth());
            }
        }
        updateBounds();
    }

    /** Update values about previous toolbars. */
    void updatePrev () {
        resetPrev();
        Iterator it = prevBars.iterator();
        ToolbarConstraints tc;
        int prevPos;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            prevBeg = Math.max (prevBeg, prevPos = tc.getPosition());
            prevEnd = Math.max (prevEnd, prevPos + tc.getWidth());
        }
    }

    /** Reset values about previous toolbars. */
    void resetPrev () {
        prevBeg = 0;
        prevEnd = 0;
    }

    /** Reset values about next toolbars. */
    void resetNext () {
        nextBeg = Integer.MAX_VALUE;
        nextEnd = Integer.MAX_VALUE;
    }

    /** Move toolbar left if it's possible.
     * @param dx horizontal distance
     */
    void moveLeft (int dx) {
        int wantX = position - dx;

        position = wantX;
        anchor = NO_ANCHOR;
        if (wantX > prevEnd) { // no problem to move left
            setAnchorTo (NO_ANCHOR, nextBars);
        } else {
            if (canSwitchLeft (getPosition(), getWidth(), prevBeg, prevEnd - prevBeg)) { // can switch left ?
                switchToolbarLeft ();
            }
        }
    }

    /** Move toolbar right if it's possible.
     * @param dx horizontal distance
     */
    void moveRight (int dx) {
        int wantX = position + dx;
        int wantXpWidth = wantX + getWidth(); // wantX plus width

        if (wantXpWidth < nextBeg) { // no problem to move right
            anchor = NO_ANCHOR;
            position = wantX;
        } else {
            if (canSwitchRight (wantX, getWidth(), nextBeg, nextEnd - nextBeg)) { // can switch right ?
                position = wantX;
                anchor = NO_ANCHOR;
                switchToolbarRight ();
            } else {
                position = nextBeg - getWidth() - ToolbarLayout.HGAP;
                anchor = NO_ANCHOR;
            }
        }

        updatePrevPosition();
    }

    /** Move toolbar left with all followers. */
    void moveLeft2End (int dx) {
        int wantX = position - dx;

        anchor = NO_ANCHOR;
        if (wantX < (prevEnd + ToolbarLayout.HGAP)) {
            wantX = prevEnd + ToolbarLayout.HGAP;
        }
        move2End (wantX - position);
    }

    /** Move toolbar right with all followers. */
    void moveRight2End (int dx) {
        move2End (dx);
    }

    /** Move toolbar horizontal with all followers. */
    void move2End (int dx) {
        position += dx;
        Iterator it = nextBars.iterator();
        ToolbarConstraints tc;
        int nextPos;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            tc.move2End (dx);
        }
    }

    /** Set anchor to list of toolbars.
     * @param anch type of anchor
     * @param bars list of toolbars
     */
    void setAnchorTo (int anch, Vector bars) {
        Iterator it = bars.iterator();
        ToolbarConstraints tc;
        while (it.hasNext()) {
            tc = (ToolbarConstraints)it.next();
            tc.setAnchor (anch);
        }
    }

    /** Switch toolbar left if it's possible. */
    void switchToolbarLeft () {
        Iterator it = ownRows.iterator();
        ToolbarRow row;
        while (it.hasNext()) {
            row = (ToolbarRow)it.next();
            row.trySwitchLeft (this);
        }
    }

    /** Switch toolbar right if it's possible. */
    void switchToolbarRight () {
        Iterator it = ownRows.iterator();
        ToolbarRow row;
        while (it.hasNext()) {
            row = (ToolbarRow)it.next();
            row.trySwitchRight (this);
        }
    }
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        if (propSupport == null) {
            propSupport = new PropertyChangeSupport(this);
        }
        propSupport.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        if (propSupport != null) {
            propSupport.removePropertyChangeListener(l);
        }
    }

    /** Can switch toolbar left?
     * @param p1 toolbar1 position
     * @param w1 toolbar1 width
     * @param p2 toolbar2 position
     * @param w2 toolbar2 width
     * @return true if possible switch toolbar left.
     */
    static boolean canSwitchLeft (int p1, int w1, int p2, int w2) {
        return (p1 < (p2));
    }

    /** Can switch toolbar right?
     * @param p1 toolbar1 position
     * @param w1 toolbar1 width
     * @param p2 toolbar2 position
     * @param w2 toolbar2 width
     * @return true if possible switch toolbar right.
     */
    static boolean canSwitchRight (int p1, int w1, int p2, int w2) {
        return (p1 > (p2));
    }
    

    /** Class to store toolbar in xml format. */
    static class WritableToolbar {
        /** name of toolbar */
        String name;
        /** position of toolbar */
        int position;
        /** anchor of toolbar */
        int anchor;
        /** toolbar visibility */
        boolean visible;

        /** Create new WritableToolbar.
         * @param tc ToolbarConstraints
         */
        public WritableToolbar (ToolbarConstraints tc) {
            name = tc.getName();
            position = tc.getPosition();
            anchor = tc.getAnchor();
            visible = tc.isVisible();
        }

        /** @return ToolbarConstraints int xml format. */
        @Override
        public String toString () {
            StringBuffer sb = new StringBuffer();
            String quotedName = name;
            try {
                quotedName = org.openide.xml.XMLUtil.toAttributeValue(name);
            }
            catch (java.io.IOException ignore) {}
            
            sb.append ("    <").append (ToolbarConfiguration.TAG_TOOLBAR); // NOI18N
            sb.append (" ").append (ToolbarConfiguration.ATT_TOOLBAR_NAME).append ("=\""). // NOI18N
            append (quotedName).append ("\""); // NOI18N
            if ((anchor == ToolbarConstraints.NO_ANCHOR) || !visible)
                sb.append (" ").append (ToolbarConfiguration.ATT_TOOLBAR_POSITION).append ("=\""). // NOI18N
                append (position).append ("\""); // NOI18N
            if (!visible)
                sb.append (" ").append (ToolbarConfiguration.ATT_TOOLBAR_VISIBLE).append ("=\""). // NOI18N
                append (visible).append ("\""); // NOI18N
            sb.append (" />\n"); // NOI18N

            return sb.toString();
        }
    } // end of inner class WritableToolbar
} // end of class ToolbarConstraints

