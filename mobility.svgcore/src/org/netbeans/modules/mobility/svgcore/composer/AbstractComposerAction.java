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
import org.netbeans.modules.mobility.svgcore.SVGDataObject;

/**
 *
 * @author Pavel Benes
 */
public abstract class AbstractComposerAction implements ComposerAction{
    protected final ComposerActionFactory m_factory;
    protected       boolean               m_isCompleted;

    protected AbstractComposerAction(ComposerActionFactory factory) {
        m_factory     = factory;
        m_isCompleted = false;
    }

    public synchronized void actionCompleted() {
        m_isCompleted = true;
    }

    public synchronized boolean isCompleted() {
        return m_isCompleted;
    }

    public void paint(Graphics g, int x, int y) {
    }
        
    protected ScreenManager getScreenManager() {
        return m_factory.getSceneManager().getScreenManager();
    }

    protected PerseusController getPerseusController() {
        return m_factory.getSceneManager().getPerseusController();
    }

    protected SVGDataObject getDataObject() {
        return m_factory.getSceneManager().getDataObject();
    }

    public ActionMouseCursor getMouseCursor(boolean isOutsideEvent) {
        return null;
    }
}
