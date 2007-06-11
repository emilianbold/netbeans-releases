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
public class SelectActionFactory extends AbstractComposerActionFactory {
    
    public SelectActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }

    public static MouseEvent getSelectionEvent(InputEvent evt) {
        if ( evt.getID() == MouseEvent.MOUSE_CLICKED) {
            MouseEvent me = (MouseEvent)evt;
            if ( me.getClickCount() > 0) {
                return me;
            }
        }
        return null;
    }
    
    public ComposerAction startAction(SVGObject selected) {
        return new SelectAction(this, selected);
    }
    
    public synchronized ComposerAction startAction(InputEvent e) {        
        MouseEvent me = getSelectionEvent(e);

        if ( me != null) {
            SVGObject [] objects = m_sceneMgr.getPerseusController().getObjectsAt(me.getX(), me.getY());
            if (objects != null && objects.length > 0)  {
                return new SelectAction(this, objects[0], me);
            }    
        }
        return null;
    }
}
