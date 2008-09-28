/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.ImageUtilities;

/**
 *
 * @author Pavel Benes
 */
public final class RotateActionFactory extends AbstractComposerActionFactory {
    private static final int   ROTATE_PIVOT_SIZE          = 4;
    private static final Color COLOR_ROTATE_PIVOT_BODY    = Color.BLACK;
    private static final Color COLOR_ROTATE_PIVOT_OUTLINE = Color.WHITE;
    private static final ActionMouseCursor ROTATE_MOUSE_CURSOR = new ActionMouseCursor( 
                Toolkit.getDefaultToolkit().createCustomCursor(ImageUtilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/rotate_cursor.png"), // NOI18N
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
            m_rotated.commitChanges();
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
