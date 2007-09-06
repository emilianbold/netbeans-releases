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

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.ActionMouseCursor;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.ComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 *
 * @author Pavel Benes
 */
public final class ScaleActionFactory extends AbstractComposerActionFactory {
    private static final ActionMouseCursor SCALE_MOUSE_CURSOR = new ActionMouseCursor( 
                Toolkit.getDefaultToolkit().createCustomCursor(org.openide.util.Utilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/resize_cursor.png"), // NOI18N
                new Point(8,8), "rotateCursor"), 2);  //NOI18N
    
    private static class ScaleAction extends AbstractComposerAction {
        private final SVGObject m_scaled;
        private final int       m_x;
        private final int       m_y;

        public ScaleAction(ComposerActionFactory factory, SVGObject selected, MouseEvent me) {
            super(factory);
            m_scaled = selected;
            m_x = me.getX();
            m_y = me.getY();
        }

        public boolean consumeEvent(AWTEvent evt, boolean isOutsideEvent) {
            if ( !isOutsideEvent && evt.getID() == MouseEvent.MOUSE_DRAGGED) {
                MouseEvent me = (MouseEvent)evt;
                
                //calculate area to repaint
                Rectangle bBox = m_scaled.getScreenBBox();
                m_scaled.scale(calculateScale(me.getX(), me.getY()));
                bBox.add(m_scaled.getScreenBBox());
                
                m_factory.getSceneManager().getScreenManager().repaint(bBox, SVGObjectOutline.SELECTOR_OVERLAP);
            } else {
                actionCompleted();
                m_scaled.commitChanges();
            }
            return false;
        }
        
        public ActionMouseCursor getMouseCursor(boolean isOutsideEvent) {
            return isOutsideEvent ? null : SCALE_MOUSE_CURSOR;
        }        
        
        protected float calculateScale( int x, int y) {
            float[] pt = m_scaled.getOutline().getScalePivotPoint();
            float d1,d2;
                    
            d1 = pt[0] - m_x;
            d2 = pt[1] - m_y;
            float dist1 = d1*d1 + d2*d2;
            d1 = pt[0] - x;
            d2 = pt[1] - y;
            float dist2 = d1*d1 + d2*d2;
            
            return dist2 / dist1;
        }
    }
    
    public ScaleActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }
    
    public synchronized ComposerAction startAction(AWTEvent e, boolean isOutsideEvent) {        
        if ( !isOutsideEvent &&
             !m_sceneMgr.isReadOnly() &&
             e.getID() == MouseEvent.MOUSE_PRESSED) {
            MouseEvent me = (MouseEvent)e;
            SVGObject selObj = getObjectToScaleAt(me);
            if ( selObj != null) {
                return new ScaleAction(this, selObj, me);
            }
        } 
        return null;
    }

    public ActionMouseCursor getMouseCursor(MouseEvent evt, boolean isOutsideEvent) {
        if ( !isOutsideEvent && getObjectToScaleAt(evt) != null) {
            return SCALE_MOUSE_CURSOR;
        }
        return null;
    }
    
    private SVGObject getObjectToScaleAt( MouseEvent me) {
        SVGObject [] selectedObjects = m_sceneMgr.getSelected();
        if (selectedObjects != null && selectedObjects.length > 0) {
            SVGObject selObj = selectedObjects[0];
            if ( selObj.getOutline().isAtScaleHandlePoint((float) me.getX(), (float) me.getY())) {
                return selObj;
            }
        }
        return null;
    }    
}
