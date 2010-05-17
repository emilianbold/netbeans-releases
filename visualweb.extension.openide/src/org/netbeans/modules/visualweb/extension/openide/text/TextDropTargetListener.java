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


package org.netbeans.modules.visualweb.extension.openide.text;


import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.Timer;


// XXX Copied from previously located openide/src/../text/ dir, this is not a NB code.

/* A drop listener for text components. This seems necessary because
 * the NetBeans editor doesn't inherit from the Swing plaf.basic package,
 * so it's missing a bunch of drag & drop behavior.
 * In particular, this class is responsible for moving the caret
 * to the nearest drag location in the document, as well as autoscrolling
 * the view when necesary.
 * <p>
 * This code is basically a merged version of text-related code in
 * javax.swing.plaf.basic: BasicTextUI's TextDropTargetListener and
 * its parent class, BasickDropTargetListener.
 * <p>
 * I had to copy it since it has package protected access in
 * javax.swing.plaf.basic.
 *
 * <p>
 * @author Tor Norbye
 */
class TextDropTargetListener implements DropTargetListener,
                                               UIResource, ActionListener {

    // The first code is the general BasicDropTargetListener code; at the
    // end you'll find the TextDropTargetListener code


    /**
     * construct a DropTargetAutoScroller
     * <P>
     * @param c the <code>Component</code>
     * @param p the <code>Point</code>
     */
    protected TextDropTargetListener() {
    }


    /**
     * Update the geometry of the autoscroll region.  The geometry is
     * maintained as a pair of rectangles.  The region can cause
     * a scroll if the pointer sits inside it for the duration of the
     * timer.  The region that causes the timer countdown is the area
     * between the two rectangles.
     * <p>
     * This is implemented to use the visible area of the component
     * as the outer rectangle and the insets are based upon the
     * Scrollable information (if any).  If the Scrollable is
     * scrollable along an axis, the step increment is used as
     * the autoscroll inset.  If the component is not scrollable,
     * the insets will be zero (i.e. autoscroll will not happen).
     */
    void updateAutoscrollRegion(JComponent c) {
	// compute the outer
	Rectangle visible = c.getVisibleRect();
	outer.reshape(visible.x, visible.y, visible.width, visible.height);

	// compute the insets
	// TBD - the thing with the scrollable
	Insets i = new Insets(0, 0, 0, 0);
	if (c instanceof Scrollable) {
	    Scrollable s = (Scrollable) c;
	    i.left = s.getScrollableUnitIncrement(visible, SwingConstants.HORIZONTAL, 1);
	    i.top = s.getScrollableUnitIncrement(visible, SwingConstants.VERTICAL, 1);
	    i.right = s.getScrollableUnitIncrement(visible, SwingConstants.HORIZONTAL, -1);
	    i.bottom = s.getScrollableUnitIncrement(visible, SwingConstants.VERTICAL, -1);
	}

	// set the inner from the insets
	inner.reshape(visible.x + i.left,
		      visible.y + i.top,
		      visible.width - (i.left + i.right),
		      visible.height - (i.top  + i.bottom));
    }

    /**
     * Perform an autoscroll operation.  This is implemented to scroll by the
     * unit increment of the Scrollable using scrollRectToVisible.  If the
     * cursor is in a corner of the autoscroll region, more than one axis will
     * scroll.
     */
    void autoscroll(JComponent c, Point pos) {
	if (c instanceof Scrollable) {
	    Scrollable s = (Scrollable) c;
	    if (pos.y < inner.y) {
		// scroll top downward
		int dy = s.getScrollableUnitIncrement(outer, SwingConstants.VERTICAL, 1);
		Rectangle r = new Rectangle(inner.x, outer.y - dy, inner.width, dy);
		c.scrollRectToVisible(r);
	    } else if (pos.y > (inner.y + inner.height)) {
		// scroll bottom upward
		int dy = s.getScrollableUnitIncrement(outer, SwingConstants.VERTICAL, -1);
		Rectangle r = new Rectangle(inner.x, outer.y + outer.height, inner.width, dy);
		c.scrollRectToVisible(r);
	    }

	    if (pos.x < inner.x) {
		// scroll left side to the right
		int dx = s.getScrollableUnitIncrement(outer, SwingConstants.HORIZONTAL, 1);
		Rectangle r = new Rectangle(outer.x - dx, inner.y, dx, inner.height);
		c.scrollRectToVisible(r);
	    } else if (pos.x > (inner.x + inner.width)) {
		// scroll right side to the left
		int dx = s.getScrollableUnitIncrement(outer, SwingConstants.HORIZONTAL, -1);
		Rectangle r = new Rectangle(outer.x + outer.width, inner.y, dx, inner.height);
		c.scrollRectToVisible(r);
	    }
	}
    }

    /**
     * Initializes the internal properties if they haven't been already
     * inited. This is done lazily to avoid loading of desktop properties.
     */
    private void initPropertiesIfNecessary() {
        if (timer == null) {
            Toolkit t  = Toolkit.getDefaultToolkit();
            Integer    initial  = new Integer(100);
            Integer    interval = new Integer(100);

            try {
                initial = (Integer)t.getDesktopProperty(
                                                        "DnD.Autoscroll.initialDelay");
            } catch (Exception e) {
                // ignore
            }
            try {
                interval = (Integer)t.getDesktopProperty(
                                                         "DnD.Autoscroll.interval");
            } catch (Exception e) {
                // ignore
            }
            timer = new Timer(interval.intValue(), this);

            timer.setCoalesce(true);
            timer.setInitialDelay(initial.intValue());

            try {
                hysteresis = ((Integer)t.getDesktopProperty(
                                                            "DnD.Autoscroll.cursorHysteresis")).intValue();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    static JComponent getComponent(DropTargetEvent e) {
	DropTargetContext context = e.getDropTargetContext();
	return (JComponent) context.getComponent();
    }

    // --- ActionListener methods --------------------------------------

    /**
     * The timer fired, perform autoscroll if the pointer is within the
     * autoscroll region.
     * <P>
     * @param e the <code>ActionEvent</code>
     */
    public synchronized void actionPerformed(ActionEvent e) {
	updateAutoscrollRegion(component);
	if (outer.contains(lastPosition) && !inner.contains(lastPosition)) {
	    autoscroll(component, lastPosition);
	}
    }

    // --- DropTargetListener methods -----------------------------------

    public void dragEnter(DropTargetDragEvent e) {
	component = getComponent(e);
	TransferHandler th = component.getTransferHandler();
	canImport = th.canImport(component, e.getCurrentDataFlavors());
	if (canImport) {
	    saveComponentState(component);
	    lastPosition = e.getLocation();
	    updateAutoscrollRegion(component);
            initPropertiesIfNecessary();
	}
    }

    public void dragOver(DropTargetDragEvent e) {
	if (canImport) {
	    Point p = e.getLocation();
	    updateInsertionLocation(component, p);


	    // check autoscroll
	    synchronized(this) {
		if (Math.abs(p.x - lastPosition.x) > hysteresis ||
		    Math.abs(p.y - lastPosition.y) > hysteresis) {
		    // no autoscroll 
		    if (timer.isRunning()) timer.stop();
		} else {
		    if (!timer.isRunning()) timer.start();
		}
		lastPosition = p;
	    }
	}
    }

    public void dragExit(DropTargetEvent e) {
        if (canImport) {
            restoreComponentState(component);
        }
        cleanup();
    }

    public void drop(DropTargetDropEvent e) {
        if (canImport) {
            restoreComponentStateForDrop(component);
        }
        cleanup();
    }

    public void dropActionChanged(DropTargetDragEvent e) {
    }

    /**
     * Cleans up internal state after the drop has finished (either succeeded
     * or failed).
     */
    private void cleanup() {
        if (timer != null) {
            timer.stop();
        }
	component = null;
	lastPosition = null;
    }

    // --- fields --------------------------------------------------
	
    private Timer timer;
    private Point lastPosition;
    private Rectangle  outer = new Rectangle();
    private Rectangle  inner = new Rectangle();
    private int	   hysteresis = 10;
    private boolean canImport;

    /** 
     * The current component. The value is cached from the drop events and used
     * by the timer. When a drag exits or a drop occurs, this value is cleared.
     */
    private JComponent component;




    // TEXT DROP LISTENER SPECIFIC STUFF - from BasicTextUI's
    // TextDropListener

        
    /**
     * called to save the state of a component in case it needs to
     * be restored because a drop is not performed.
     */
    protected void saveComponentState(JComponent comp) {
        JTextComponent c = (JTextComponent) comp;
        Caret caret = c.getCaret();
        dot = caret.getDot();
        mark = caret.getMark();
        visible = caret.isVisible();
        caret.setVisible(true);
    }

    /**
     * called to restore the state of a component 
     * because a drop was not performed.
     */
    protected void restoreComponentState(JComponent comp) {
        JTextComponent c = (JTextComponent) comp;
        Caret caret = c.getCaret();
        caret.setDot(mark);
        caret.moveDot(dot);
        caret.setVisible(visible);
    }

    /**
     * called to restore the state of a component
     * because a drop was performed.
     */
    protected void restoreComponentStateForDrop(JComponent comp) {
        JTextComponent c = (JTextComponent) comp;
        Caret caret = c.getCaret();
        caret.setVisible(visible);
    }

    /**
     * called to set the insertion location to match the current
     * mouse pointer coordinates.
     */
    protected void updateInsertionLocation(JComponent comp, Point p) {
        JTextComponent c = (JTextComponent) comp;
        c.setCaretPosition(c.viewToModel(p));
    }

    int dot;
    int mark;
    boolean visible;
}
