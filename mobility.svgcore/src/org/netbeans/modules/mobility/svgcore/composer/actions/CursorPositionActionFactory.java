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

package org.netbeans.modules.mobility.svgcore.composer.actions;

import com.sun.perseus.j2d.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.view.svg.SVGStatusBar;
import org.w3c.dom.svg.SVGPoint;

/**
 *
 * @author Pavel Benes
 */
public class CursorPositionActionFactory extends AbstractComposerActionFactory {
    
    public CursorPositionActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }
    public synchronized ComposerAction startAction(InputEvent e, boolean isOutsideEvent) {        
        if ( !isOutsideEvent) {
            switch( e.getID()) {
                case MouseEvent.MOUSE_EXITED:
                    setStatusText("");
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                case MouseEvent.MOUSE_MOVED:
                    MouseEvent me = (MouseEvent) e;
                    SVGPoint p = m_sceneMgr.getPerseusController().convertCoords(me.getX(), me.getY());
                    setStatusText( round(p.getX()) + "," + round(p.getY()) );
                    break;
            }
        } 
        return null;
    }

    private void setStatusText(String text) {
        m_sceneMgr.getScreenManager().getStatusBar().setText( SVGStatusBar.CELL_POSITION, text);
    }
    
    private static float round(float f) {
        return Math.round(f * 10) / 10.0f;
    }    
}
