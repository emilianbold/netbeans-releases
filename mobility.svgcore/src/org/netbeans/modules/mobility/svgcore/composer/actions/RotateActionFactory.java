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
import java.awt.Color;
import java.awt.Graphics;
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
import org.netbeans.modules.mobility.svgcore.composer.GraphicUtils;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 *
 * @author Pavel Benes
 */
public final class RotateActionFactory extends AbstractComposerActionFactory {
    private static final int   ROTATE_PIVOT_SIZE          = 4;
    private static final Color COLOR_ROTATE_PIVOT_BODY    = Color.BLACK;
    private static final Color COLOR_ROTATE_PIVOT_OUTLINE = Color.WHITE;
    private static final ActionMouseCursor ROTATE_MOUSE_CURSOR = new ActionMouseCursor( 
                Toolkit.getDefaultToolkit().createCustomCursor(org.openide.util.Utilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/rotate_cursor.png"), // NOI18N
                new Point(8,8), "rotateCursor"), 3);  //NOI18N
    
    private class RotateAction extends AbstractComposerAction {
        private final SVGObject m_rotated;
        private final float     m_initialAngle;

        public RotateAction(SVGObject rotated, MouseEvent me) {
            super(RotateActionFactory.this);
            m_rotated = rotated;
            repaintRotatePivot();
            m_initialAngle = calculateRotate(me.getX(), me.getY());
        }

        public boolean consumeEvent(AWTEvent evt, boolean isOutsideEvent) {
            if ( !isOutsideEvent && evt.getID() == MouseEvent.MOUSE_DRAGGED) {
                MouseEvent me = (MouseEvent)evt;
                
                //calculate area to repaint
                Rectangle bBox = m_rotated.getScreenBBox();
                float angleDiff = calculateRotate(me.getX(),me.getY()) - m_initialAngle;
                if (angleDiff < 0) {
                    angleDiff += 360.0f;
                }
                m_rotated.rotate(angleDiff);
                bBox.add(m_rotated.getScreenBBox());
                
                m_factory.getSceneManager().getScreenManager().repaint(bBox, SVGObjectOutline.SELECTOR_OVERLAP);
            } else {
                actionCompleted();
                m_rotated.commitChanges();
            }
            return false;
        }

        public void paint(Graphics g, int x, int y, boolean isReadOnly) {
            if ( !isReadOnly) {
                float [] pt = m_rotated.getOutline().getRotatePivotPoint();
                GraphicUtils.drawRoundSelectorCorner(g, COLOR_ROTATE_PIVOT_OUTLINE,
                        COLOR_ROTATE_PIVOT_BODY, (int) (pt[0] + x),(int)(pt[1] + y),
                        ROTATE_PIVOT_SIZE);
            }
        }

        public ActionMouseCursor getMouseCursor(boolean isOutsideEvent) {
            return isOutsideEvent ? null : ROTATE_MOUSE_CURSOR;
        }        
        
        public void actionCompleted() {
            m_rotated.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
            repaintRotatePivot();
            m_rotated.applyTextChanges();
            super.actionCompleted();
        }
        
        private void repaintRotatePivot() {
            float [] pt = m_rotated.getOutline().getRotatePivotPoint();
            m_rotated.repaint((int) pt[0] - ROTATE_PIVOT_SIZE - 1,
                              (int) pt[1] - ROTATE_PIVOT_SIZE - 1, 
                               ROTATE_PIVOT_SIZE * 2 + 3,
                               ROTATE_PIVOT_SIZE * 2 + 3);
        }
        
        protected float calculateRotate( int x, int y) {
            float [] pivot = m_rotated.getOutline().getRotatePivotPoint();
            return GraphicUtils.calcAngle( pivot[0], pivot[1], x, y);
        }        
    }
    
    public RotateActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }
    
    public synchronized ComposerAction startAction(AWTEvent e, boolean isOutsideEvent) {        
        if ( !isOutsideEvent &&
             !m_sceneMgr.isReadOnly() &&
             e.getID() == MouseEvent.MOUSE_PRESSED) {
            MouseEvent me = (MouseEvent)e;
            SVGObject selObj = getObjectToRotateAt(me);
            if ( selObj != null) {
                return new RotateAction(selObj, me);
            }
        } 
        return null;
    }

    public ActionMouseCursor getMouseCursor(MouseEvent evt, boolean isOutsideEvent) {
        if ( !isOutsideEvent && getObjectToRotateAt(evt) != null) {
            return ROTATE_MOUSE_CURSOR;
        }
        return null;
    }
    
    private SVGObject getObjectToRotateAt( MouseEvent me) {
        SVGObject [] selectedObjects = m_sceneMgr.getSelected();
        if (selectedObjects != null && selectedObjects.length > 0) {
            SVGObject selObj = selectedObjects[0];
            if ( selObj.getOutline().isAtRotateHandlePoint((float) me.getX(), (float) me.getY())) {
                return selObj;
            }
        }
        return null;
    }    
}
