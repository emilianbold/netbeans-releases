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
 * The Original Software is Terminal Emulator.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc..
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Ivan Soleimanipour.
 */

/*
 * "ActiveTerm.java"
 * ActiveTerm.java 1.9 01/07/30
 */

package org.netbeans.lib.terminalemulator;

import java.awt.*;
import java.awt.event.*;

public class ActiveTerm extends StreamTerm {

    private ActiveTermListener at_listener;

    private RegionManager rm;

    private Coord last_begin = null;
    private Coord last_end = null;

    public ActiveTerm() {
	super();

	setCursorVisible(false);

	rm = regionManager();

	getScreen().addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent e) {
		if ( (e.getModifiers() & InputEvent.BUTTON1_MASK) !=
		    InputEvent.BUTTON1_MASK) {
		    // ignore if not left button
		    return;
		}
		Point p = mapToBufRowCol(e.getPoint());
		BCoord c = new BCoord(p.y, p.x);
		Coord ac = new Coord(c, firsta);
		ActiveRegion region = rm.findRegion(ac);
		if (region != null) {
		    if (region.isSelectable())
			setSelectionExtent(region.getExtent());
		    if (at_listener != null)
			at_listener.action(region, e);
		}
	    }
	} );

	getScreen().addMouseMotionListener(new MouseMotionAdapter() {
	    public void mouseMoved(MouseEvent e) {
		Point p = mapToBufRowCol(e.getPoint());
		BCoord c = new BCoord(p.y, p.x);
		Coord ac = new Coord(c, firsta);
		ActiveRegion region = rm.findRegion(ac);
		ActiveRegion hl_region = findRegionToHilite(region);

		if (hl_region == null)
		    hilite(null, null);
		else 
		    hilite(hl_region.begin, hl_region.end);
	    }
	} );
    } 

    private ActiveRegion findRegionToHilite(ActiveRegion region) {
	if (region == null)
	    return null;
	else if (region.isFeedbackEnabled())
	    return region;
	else if (region.isFeedbackViaParent())
	    return findRegionToHilite(region.parent());
	else
	    return null;
    }

    public void setActionListener(ActiveTermListener listener) {
	this.at_listener = listener;
    } 

    private void hilite_help(Coord begin, Coord end, boolean on) {
	if (begin == null && end == null)
	    return;	// nothing to do
	setCharacterAttribute(begin, end, 9, on);
    }

    public void hilite(Coord begin, Coord end) {
	if (end != null && end.row == 1 && end.col == 0)
	    end = getCursorCoord();
	hilite_help(last_begin, last_end, false);
	last_begin = (begin == null)? null: (Coord) begin.clone();
	last_end = (end == null)? null: (Coord) end.clone();
        hilite_help(begin, end, true);        
    }

    public void hilite(ActiveRegion region) {
	hilite(region.begin, region.end);
    } 

    public ActiveRegion beginRegion(boolean hyperlink) {
	ActiveRegion region = null;
	try {
	    region = rm.beginRegion(getCursorCoord());
	} catch (RegionException x) {
	    ;
	} 
	if (hyperlink) {
	    setAttribute(34);		// fg -> blue
	    setAttribute(4);		// underline
	}
	return region;
    }

    public void endRegion() {
	Coord cursor = getCursorCoord();
	Coord bcursor = backup(cursor);

	// This only happens if we begin and end a region w/o any output
	// in between
	if (bcursor == null)
	    bcursor = cursor;

	try {
	    rm.endRegion(bcursor);
	} catch (RegionException x) {
	    ;
	}
	setAttribute(0);		// reset
    } 

    public ActiveRegion findRegion(Coord coord) {
	return rm.findRegion(coord);
    }

    public void cancelRegion() {
	try {
	    rm.cancelRegion();
	} catch (RegionException x) {
	    ;
	} 
    }
    
    public void clear() {
        nullLasts();
        super.clear ();
    }

    public void clearHistoryNoRefresh() {
        nullLasts();
	super.clearHistoryNoRefresh ();
    }
    
    private void nullLasts() {
        last_begin = null;
        last_end = null;        
    }
}
