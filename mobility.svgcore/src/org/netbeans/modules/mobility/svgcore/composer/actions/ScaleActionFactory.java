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
import java.awt.Cursor;
import java.awt.Rectangle;
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
public class ScaleActionFactory extends AbstractComposerActionFactory {
//    private static final ActionMouseCursor SCALE_MOUSE_CURSOR = new ActionMouseCursor( 
//                Toolkit.getDefaultToolkit().createCustomCursor(org.openide.util.Utilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/resize_cursor.png"), // NOI18N
//                new Point(8,8), "rotateCursor"), 2);  //NOI18N
    private static final ActionMouseCursor SCALE_NW_MOUSE_CURSOR = new ActionMouseCursor( 
                Cursor.NW_RESIZE_CURSOR, 3);  //NOI18N
    private static final ActionMouseCursor SCALE_SE_MOUSE_CURSOR = new ActionMouseCursor( 
                Cursor.SE_RESIZE_CURSOR, 3);  //NOI18N
    
    protected static class ScaleAction extends AbstractComposerAction {
        protected final SVGObject m_scaled;
        private final int       m_x;
        private final int       m_y;

        public ScaleAction(ComposerActionFactory factory, SVGObject selected, MouseEvent me) {
            super(factory);
            m_scaled = selected;
            m_x = me.getX();
            m_y = me.getY();
        }

        @Override
        public boolean consumeEvent(AWTEvent evt, boolean isOutsideEvent) {
            if ( !isOutsideEvent && evt.getID() == MouseEvent.MOUSE_DRAGGED) {
                MouseEvent me = (MouseEvent)evt;
                
                //calculate area to repaint
                Rectangle bBox = m_scaled.getScreenBBox();
                m_scaled.scale(calculateScaleX(me.getX()), calculateScaleY(me.getY()));
                bBox.add(m_scaled.getScreenBBox());
                
                m_factory.getSceneManager().getScreenManager().repaint(bBox, SVGObjectOutline.SELECTOR_OVERLAP);
            } else {
                actionCompleted();
                m_scaled.commitChanges();
            }
            return false;
        }
        
        @Override
        public ActionMouseCursor getMouseCursor(boolean isOutsideEvent) {
            return isOutsideEvent ? null : SCALE_SE_MOUSE_CURSOR;
        }

        @Override
        public synchronized void actionCompleted() {
            m_scaled.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
            m_scaled.applyTextChanges();
            m_scaled.commitChanges();
            super.actionCompleted();
        }
        
        protected float calculateScaleX( int x ) {
            float[] pt = m_scaled.getOutline().getScalePivotPoint();
            return calculateScale(pt[0], m_x, x);
        }

        protected float calculateScaleY( int y ) {
            float[] pt = m_scaled.getOutline().getScalePivotPoint();
            return calculateScale(pt[1], m_y, y);
        }

        private float calculateScale(float pivot, float from, float to){
            float d1,d2;
                    
            d1 = pivot - from;
            float dist1 = d1*d1;
            d1 = pivot - to;
            float dist2 = d1*d1;
            
            return dist2 / dist1;
        }
        
    }
    
    public ScaleActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }
    
    @Override
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

    @Override
    public ActionMouseCursor getMouseCursor(MouseEvent evt, boolean isOutsideEvent) {
        SVGObject selObj = getSelectedObject();
        if ( !isOutsideEvent && selObj != null) {
            if (isNWScalePoint(selObj, evt)){
                return SCALE_NW_MOUSE_CURSOR;
            } else if (isSEScalePoint(selObj, evt)){
                return SCALE_SE_MOUSE_CURSOR;
            }
        }
        return null;
    }
    
    private SVGObject getObjectToScaleAt( MouseEvent me) {
        SVGObject selObj = getSelectedObject();
        if (selObj != null){
            if ( isNWScalePoint(selObj, me) || isSEScalePoint(selObj, me)) {
                return selObj;
            }
        }
        return null;
    }    
    
    protected SVGObject getSelectedObject(){
        SVGObject [] selectedObjects = m_sceneMgr.getSelected();
        if (selectedObjects != null && selectedObjects.length > 0) {
            return selectedObjects[0];
        }
        return null;
    }

    private boolean isNWScalePoint(SVGObject selObj, MouseEvent me) {
        return selObj.getOutline().isAtScaleNWHandlePoint((float) me.getX(), (float) me.getY());
    }

    private boolean isSEScalePoint(SVGObject selObj, MouseEvent me) {
        return selObj.getOutline().isAtScaleSEHandlePoint((float) me.getX(), (float) me.getY());
    }

}
