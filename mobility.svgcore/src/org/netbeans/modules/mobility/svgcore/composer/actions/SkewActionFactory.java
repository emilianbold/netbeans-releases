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
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.ActionMouseCursor;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 *
 * @author Pavel Benes
 */
public final class SkewActionFactory extends AbstractComposerActionFactory {
    private static final ActionMouseCursor SKEW_MOUSE_CURSOR = new ActionMouseCursor( 
                Toolkit.getDefaultToolkit().createCustomCursor(org.openide.util.Utilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/skew_cursor.png"), // NOI18N
                new Point(8,8), "skewCursor"), 3);  //NOI18N
    
    private final class SkewAction extends AbstractComposerAction {
        private final SVGObject m_skewed;
        private final int       m_initialX;
        private final int       m_initialY;

        public SkewAction(SVGObject skewed, MouseEvent me) {
            super(SkewActionFactory.this);
            m_skewed = skewed;
            m_initialX = me.getX();
            m_initialY = me.getY();
        }

        public boolean consumeEvent(AWTEvent evt, boolean isOutsideEvent) {
            if ( !isOutsideEvent && evt.getID() == MouseEvent.MOUSE_DRAGGED) {
                MouseEvent me = (MouseEvent)evt;
                
                //calculate area to repaint
                Rectangle bBox = m_skewed.getScreenBBox();
                int   diff;
                float skewX = 0;
                float skewY = 0;
                
                if ( bBox.height > 0 && (diff=me.getX()-m_initialX) != 0) {
                    double a = Math.atan( (double) diff / bBox.height);
                    skewX = (float) java.lang.Math.toDegrees(a);
                }
                if ( bBox.width > 0 && (diff=m_initialY - me.getY()) != 0) {
                    double a = Math.atan( (double) diff / bBox.width);
                    skewY = (float) java.lang.Math.toDegrees(a);
                }
                
                m_skewed.skew(skewX, skewY);
                bBox.add(m_skewed.getScreenBBox());
                
                m_factory.getSceneManager().getScreenManager().repaint(bBox, SVGObjectOutline.SELECTOR_OVERLAP);
            } else {
                actionCompleted();
                m_skewed.commitChanges();
            }
            return false;
        }

        public ActionMouseCursor getMouseCursor(boolean isOutsideEvent) {
            return isOutsideEvent ? null : SKEW_MOUSE_CURSOR;
        }        
        
        public void actionCompleted() {
            m_skewed.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
            m_skewed.applyTextChanges();
            super.actionCompleted();
        }
    }
    
    public SkewActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }
    
    public synchronized ComposerAction startAction(AWTEvent e, boolean isOutsideEvent) {        
        if ( !isOutsideEvent &&
             !m_sceneMgr.isReadOnly() &&
             e.getID() == MouseEvent.MOUSE_PRESSED) {
            MouseEvent me = (MouseEvent)e;
            SVGObject selObj = getObjectToSkewAt(me);
            if ( selObj != null) {
                return new SkewAction(selObj, me);
            }
        } 
        return null;
    }

    public ActionMouseCursor getMouseCursor(MouseEvent evt, boolean isOutsideEvent) {
        if ( !isOutsideEvent && getObjectToSkewAt(evt) != null) {
            return SKEW_MOUSE_CURSOR;
        }
        return null;
    }
    
    private SVGObject getObjectToSkewAt( MouseEvent me) {
        SVGObject [] selectedObjects = m_sceneMgr.getSelected();
        if (selectedObjects != null && selectedObjects.length > 0) {
            SVGObject selObj = selectedObjects[0];
            if ( selObj.getOutline().isAtSkewHandlePoint((float) me.getX(), (float) me.getY())) {
                return selObj;
            }
        }
        return null;
    }    
}
