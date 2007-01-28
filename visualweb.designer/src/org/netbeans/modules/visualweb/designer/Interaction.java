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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * Interaction.java
 *
 * Created on February 22, 2005, 11:34 PM
 */
package org.netbeans.modules.visualweb.designer;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


/**
 * An Interaction is an object which handles one particular kind of
 * operation/interaction in the designer, such as a drag (to move),
 * a resize, a marquee selection, or an internal resize (such as moving
 * a column in a table component).
 *
 * @author Tor Norbye
 */
public abstract class Interaction implements MouseListener, MouseMotionListener {
    /**
     * Cancel the Interaction. Typically called when the user presses the
     * Escape key.
     */
    public abstract void cancel(DesignerPane pane);

    public void mouseClicked(MouseEvent event) {
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    public void mouseMoved(MouseEvent event) {
    }

    /** Paint the interaction on the given graphics object */
    public abstract void paint(Graphics g);
}
