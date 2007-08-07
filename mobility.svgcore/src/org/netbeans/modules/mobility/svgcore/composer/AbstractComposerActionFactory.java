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

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGAction;

/**
 *
 * @author Pavel Benes
 */
public abstract class AbstractComposerActionFactory implements ComposerActionFactory {
    protected final SceneManager m_sceneMgr;
    protected       boolean      m_isBlocked;

    protected AbstractComposerActionFactory(SceneManager sceneMgr) {
        m_sceneMgr = sceneMgr;
    }

    public boolean isBlocked() {
        return m_isBlocked;
    }

    public void setBlocked(boolean isBlocked) {
        m_isBlocked = isBlocked;
    }

    public SceneManager getSceneManager() {
        return m_sceneMgr;
    }

    public ComposerAction startAction(InputEvent event, boolean isOutsideEvent) {
        return null;
    }
    
    public ActionMouseCursor getMouseCursor(MouseEvent evt, boolean isOutsideEvent) {
        return null;
    }   

    public AbstractSVGAction [] getMenuActions() {
        return null;
    }    
}
