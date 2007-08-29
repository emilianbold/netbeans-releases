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

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.ActionMouseCursor;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 *
 * @author Pavel Benes
 */
public class TranslateActionFactory extends AbstractComposerActionFactory {
    private static final ActionMouseCursor TRANSLATE_MOUSE_CURSOR = new ActionMouseCursor( Cursor.MOVE_CURSOR, 1);
    
    private static int [] DIFF_LEFT   = new int[] { -1, 0};
    private static int [] DIFF_RIGHT  = new int[] { 1, 0};
    private static int [] DIFF_DOWN   = new int[] { 0, 1};
    private static int [] DIFF_UP     = new int[] { 0, -1};
    
    public TranslateActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }
    
    public synchronized ComposerAction startAction(InputEvent e, boolean isOutsideEvent) {        
        if ( !isOutsideEvent &&
             !m_sceneMgr.isReadOnly()) {            
            if ( e.getID() == MouseEvent.MOUSE_PRESSED) {
                MouseEvent me = (MouseEvent)e;
                SVGObject selObj = getSelectedObjectAt(me);
                if ( selObj != null) {
                    return new TranslateAction(this, selObj, me);
                }
            } else {
                if ( getCoordDiff(e) != null) {
                    SVGObject selObj = getSelectedObject();
                    if ( selObj != null) {
                        return new TranslateAction(this, selObj, (KeyEvent) e);
                    }                
                }
            }
        } 
        return null;
    }
/*
    private void move(float dx, float dy) {
        SVGObject [] selectedObjects = m_sceneMgr.getSelected();
        if (selectedObjects != null) {
            SVGObject selected = selectedObjects[0];
            assert selected != null : "The selection array may not contain null";
            selected.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
            selected.translateIncr(dx, dy);
            selected.commitChanges();
            selected.applyTextChanges();
            selected.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
        }
    }
*/    
    public ActionMouseCursor getMouseCursor(MouseEvent evt, boolean isOutsideEvent) {
        if ( !isOutsideEvent && getSelectedObjectAt(evt) != null) {
            return TRANSLATE_MOUSE_CURSOR;
        }
        return null;
    }
        
    private SVGObject getSelectedObjectAt( MouseEvent me) {
        SVGObject [] selectedObjects = m_sceneMgr.getSelected();
        if (selectedObjects != null && selectedObjects.length > 0) {
            SVGObject selObj = selectedObjects[0];
            if (selObj.getScreenBBox().contains(me.getPoint())) {
                return selObj;
            }
        }
        return null;
    }

    private SVGObject getSelectedObject() {
        SVGObject [] selectedObjects = m_sceneMgr.getSelected();
        if (selectedObjects != null && selectedObjects.length > 0) {
            return selectedObjects[0];
        }
        return null;
    }
    
    static int [] getCoordDiff(InputEvent e) {
        if ( e.getID() == KeyEvent.KEY_PRESSED &&
                e.getModifiers() == 0) {
            switch( ((KeyEvent)e).getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    return DIFF_DOWN;
                case KeyEvent.VK_LEFT:
                    return DIFF_LEFT;
                case KeyEvent.VK_RIGHT:
                    return DIFF_RIGHT;
                case KeyEvent.VK_UP:
                    return DIFF_UP;
            }
        }  
        return null;
    }
}
