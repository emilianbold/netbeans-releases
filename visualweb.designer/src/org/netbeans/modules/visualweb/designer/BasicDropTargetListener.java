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

package org.netbeans.modules.visualweb.designer;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.plaf.UIResource;


/**
 * COPIED from javax.swing.plaf.basic.BasicDropTargetListener
 * because that copy is package private.
 *
 * <p>
 * The Swing DropTarget implementation supports multicast notification
 * to listeners, so this implementation is used as an additional
 * listener that extends the primary drop target functionality
 * (i.e. linkage to the TransferHandler) to include autoscroll and
 * establish an insertion point for the drop.  This is used by the ComponentUI
 * of components supporting a selection mechanism, which have a
 * way of indicating a location within their model.
 * <p>
 * The autoscroll functionality is based upon the Swing scrolling mechanism
 * of the Scrollable interface.  The unit scroll increment is used to as
 * the scroll amount, and the scrolling is based upon JComponent.getVisibleRect
 * and JComponent.scrollRectToVisible.  The band of area around the visible
 * rectangle used to invoke autoscroll is based upon the unit scroll increment
 * as that is assumed to represent the last possible item in the visible region.
 * <p>
 * The subclasses are expected to implement the following methods to manage the
 * insertion location via the components selection mechanism.
 * <ul>
 * <li>saveComponentState
 * <li>restoreComponentState
 * <li>restoreComponentStateForDrop
 * <li>updateInsertionLocation
 * </ul>
 *
 * @author  Timothy Prinzing
 * @version 1.8 01/23/03
 */
class BasicDropTargetListener implements DropTargetListener, UIResource, ActionListener {

    /**
     * construct a DropTargetAutoScroller
     * <P>
     * @param c the <code>Component</code>
     * @param p the <code>Point</code>
     */
    protected BasicDropTargetListener() {
    }

    /**
     * called to save the state of a component in case it needs to
     * be restored because a drop is not performed.
     */
    protected void saveComponentState(JComponent c) {
    }

    /**
     * called to restore the state of a component in case a drop
     * is not performed.
     */
    protected void restoreComponentState(JComponent c) {
    }

    /**
     * called to restore the state of a component in case a drop
     * is performed.
     */
    protected void restoreComponentStateForDrop(JComponent c) {
    }

    /**
     * called to set the insertion location to match the current
     * mouse pointer coordinates.
     */
    protected void updateInsertionLocation(JComponent c, Point p) {
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
//	outer.reshape(visible.x, visible.y, visible.width, visible.height);
	outer.setBounds(visible.x, visible.y, visible.width, visible.height);

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
//	inner.reshape(visible.x + i.left,
//		      visible.y + i.top,
//		      visible.width - (i.left + i.right),
//		      visible.height - (i.top  + i.bottom));
	inner.setBounds(visible.x + i.left,
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

            Integer initial = (Integer)t.getDesktopProperty("DnD.Autoscroll.initialDelay");
            if(initial == null) {
                initial = new Integer(100);
            }
            Integer interval = (Integer)t.getDesktopProperty("DnD.Autoscroll.interval");
            if(interval == null) {
                interval = new Integer(100);
            }
                
            timer = new Timer(interval.intValue(), this);

            timer.setCoalesce(true);
            timer.setInitialDelay(initial.intValue());

//            Integer h = (Integer)t.getDesktopProperty("DnD.Autoscroll.cursorHysteresis");
//            hysteresis = h == null ? 10 : h.intValue();
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
        if (th instanceof DesignerTransferHandler) {
            // XXX #99457 Internal enhanced TransferHandler to provide more fine-grained decision (based on Transferable as well).
            canImport = ((DesignerTransferHandler)th).canImport(component, e.getCurrentDataFlavors(), e.getTransferable());
        } else {
            canImport = th.canImport(component, e.getCurrentDataFlavors());
        }
	if (canImport) {
	    saveComponentState(component);
	    lastPosition = e.getLocation();
	    updateAutoscrollRegion(component);
            initPropertiesIfNecessary();
	}
    }

    public void dragOver(DropTargetDragEvent e) {
        /*  Perhaps I need to add autoscrolling myself now....
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
         */
    }

    public void dragExit(DropTargetEvent e) {
        if (canImport && component != null) {
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
//    private int	   hysteresis = 10;
    private boolean canImport;

    /**
     * The current component. The value is cached from the drop events and used
     * by the timer. When a drag exits or a drop occurs, this value is cleared.
     */
    private JComponent component;

}
