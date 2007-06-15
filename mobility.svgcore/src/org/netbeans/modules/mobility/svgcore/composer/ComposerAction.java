/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer;

import java.awt.Graphics;
import java.awt.event.InputEvent;

/**
 *
 * @author Pavel Benes
 */
public interface ComposerAction {
    public boolean consumeEvent(InputEvent event);

    public void actionCompleted();
    //TODO offset should be handled somewhere else
    public void paint(Graphics g, int x, int y);

    public boolean isCompleted();

    //TODO Remove - better use some container for active actions
    // on the SelectActionFactory and ask there for current selection
    public SVGObject getSelected();
}
