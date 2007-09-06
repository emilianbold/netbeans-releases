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

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import javax.swing.Action;

/**
 *
 * @author Pavel Benes
 */
public interface ComposerActionFactory {
    public ComposerAction       startAction(AWTEvent event, boolean isOutsideEvent);
    public boolean              isBlocked();
    public void                 setBlocked(boolean isBlocked);
    public SceneManager         getSceneManager();
    public ActionMouseCursor    getMouseCursor(MouseEvent evt, boolean isOutsideEvent);
    public Action []            getMenuActions();
    public void                 updateActionState();
}
