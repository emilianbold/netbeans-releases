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

// This file is currently unused. Rip it out if I don't plan to
// get text drag & drop handling working within the designer
// in flow mode.

package org.netbeans.modules.visualweb.designer;

import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.TransferHandler;






/**
 * COPIED from javax.swing.plaf.basic.BasicDragGestureRecognizer
 * because that copy is package private.
 *
 *
 * <p>
 * Default drag gesture recognition for drag operations performed by classses
 * that have a <code>dragEnabled</code> property.  The gesture for a drag in
 * this package is a mouse press over a selection followed by some movement
 * by enough pixels to keep it from being treated as a click.
 *
 * @author  Timothy Prinzing
 * @version 1.5 01/23/03
 */
public class BasicDragGestureRecognizer implements MouseListener, MouseMotionListener {

    private MouseEvent dndArmedEvent = null;

    private static int motionThreshold;

    private static boolean checkedMotionThreshold = false;

    static int getMotionThreshold() {
        if (checkedMotionThreshold) {
            return motionThreshold;
        } else {
            checkedMotionThreshold = true;
            try {
                motionThreshold = ((Integer)Toolkit.getDefaultToolkit().getDesktopProperty("DnD.gestureMotionThreshold")).intValue();
            } catch (Exception e) {
                motionThreshold = 5;
            }
        }
        return motionThreshold;
    }

    protected int mapDragOperationFromModifiers(MouseEvent e) {
        int mods = e.getModifiersEx();

        if ((mods & InputEvent.BUTTON1_DOWN_MASK) != InputEvent.BUTTON1_DOWN_MASK) {
            return TransferHandler.NONE;
        }

        JComponent c = getComponent(e);
        TransferHandler th = c.getTransferHandler();
//        return SunDragSourceContextPeer.convertModifiersToDropAction(mods, th.getSourceActions(c));
        return convertModifiersToDropAction(mods, th.getSourceActions(c));
    }

    // XXX Moved from DesignerUtils.
    /** XXX Copied from SunDragSourceContextPeer, to avoid dependency on sun jdk. */
    private static int convertModifiersToDropAction(final int modifiers,
                                                   final int supportedActions) {
        int dropAction = DnDConstants.ACTION_NONE;

        /*
         * Fix for 4285634.
         * Calculate the drop action to match Motif DnD behavior.
         * If the user selects an operation (by pressing a modifier key),
         * return the selected operation or ACTION_NONE if the selected
         * operation is not supported by the drag source.
         * If the user doesn't select an operation search the set of operations
         * supported by the drag source for ACTION_MOVE, then for
         * ACTION_COPY, then for ACTION_LINK and return the first operation
         * found.
         */
        switch (modifiers & (InputEvent.SHIFT_DOWN_MASK |
                             InputEvent.CTRL_DOWN_MASK)) {
        case InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK:
            dropAction = DnDConstants.ACTION_LINK; break;
        case InputEvent.CTRL_DOWN_MASK:
            dropAction = DnDConstants.ACTION_COPY; break;
        case InputEvent.SHIFT_DOWN_MASK:
            dropAction = DnDConstants.ACTION_MOVE; break;
        default:
            if ((supportedActions & DnDConstants.ACTION_MOVE) != 0) {
                dropAction = DnDConstants.ACTION_MOVE;
            } else if ((supportedActions & DnDConstants.ACTION_COPY) != 0) {
                dropAction = DnDConstants.ACTION_COPY;
            } else if ((supportedActions & DnDConstants.ACTION_LINK) != 0) {
                dropAction = DnDConstants.ACTION_LINK; 
            }
        }

        return dropAction & supportedActions;
    }    
    
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        dndArmedEvent = null;
	if (isDragPossible(e) && mapDragOperationFromModifiers(e) != TransferHandler.NONE) {
            dndArmedEvent = e;
	    e.consume();
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        dndArmedEvent = null;
    }
    
    public void mouseEntered(MouseEvent e) {
        //dndArmedEvent = null;
    }
    
    public void mouseExited(MouseEvent e) {
        //if (dndArmedEvent != null && mapDragOperationFromModifiers(e) == TransferHandler.NONE) {
        //    dndArmedEvent = null;
        //}
    }

    public void mouseDragged(MouseEvent e) {
	if (dndArmedEvent != null) {
            e.consume();

            int action = mapDragOperationFromModifiers(e);
            if (action == TransferHandler.NONE) {
                return;
            }
            
	    int dx = Math.abs(e.getX() - dndArmedEvent.getX());
	    int dy = Math.abs(e.getY() - dndArmedEvent.getY());
            if ((dx > getMotionThreshold()) || (dy > getMotionThreshold())) {
		// start transfer... shouldn't be a click at this point
                JComponent c = getComponent(e);
		TransferHandler th = c.getTransferHandler();
		th.exportAsDrag(c, dndArmedEvent, action);
		dndArmedEvent = null;
	    }
	}
    }
    
    public void mouseMoved(MouseEvent e) {
    }
    
    /**
     * Determines if the following are true:
     * <ul>
     * <li>the press event is located over a selection
     * <li>the dragEnabled property is true
     * <li>A TranferHandler is installed
     * </ul>
     * <p>
     * This is implemented to check for a TransferHandler.
     * Subclasses should perform the remaining conditions.
     */
    protected boolean isDragPossible(MouseEvent e) {
        JComponent c = getComponent(e);
        return (c == null) ? true : (c.getTransferHandler() != null);
    }

    protected JComponent getComponent(MouseEvent e) {
	Object src = e.getSource();
	if (src instanceof JComponent) {
	    JComponent c = (JComponent) src;
	    return c;
	}
	return null;
    }

}
