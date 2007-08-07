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

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 *
 * @author Pavel Benes
 */
public class HighlightActionFactory extends AbstractComposerActionFactory {
    
    public HighlightActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }

    public synchronized ComposerAction startAction(InputEvent e, boolean isOutsideEvent) {        
        if ( !isOutsideEvent &&
             !m_sceneMgr.isReadOnly() &&                
             e.getID() == MouseEvent.MOUSE_MOVED && 
             !m_sceneMgr.containsAction(HighlightAction.class)) {
            MouseEvent me = (MouseEvent)e;
            SVGObject [] objects = m_sceneMgr.getPerseusController().getObjectsAt(me.getX(), me.getY());
            if (objects != null && objects.length > 0 && objects[0] != null)  {
                return new HighlightAction(this, objects[0]);
            }
        }   
        return null;
    }
}
