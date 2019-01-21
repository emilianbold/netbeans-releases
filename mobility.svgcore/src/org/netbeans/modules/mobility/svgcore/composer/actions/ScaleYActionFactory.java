/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.mobility.svgcore.composer.ActionMouseCursor;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.ComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 *
 * 
 */
public final class ScaleYActionFactory extends ScaleActionFactory {
//    private static final ActionMouseCursor SCALE_MOUSE_CURSOR = new ActionMouseCursor( 
//                Toolkit.getDefaultToolkit().createCustomCursor(org.openide.util.Utilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/resizey_cursor.png"), // NOI18N
//                new Point(8,8), "rotateCursor"), 2);  //NOI18N
    private static final ActionMouseCursor SCALE_N_MOUSE_CURSOR = new ActionMouseCursor( 
                Cursor.N_RESIZE_CURSOR, 2);  //NOI18N
    private static final ActionMouseCursor SCALE_S_MOUSE_CURSOR = new ActionMouseCursor( 
                Cursor.S_RESIZE_CURSOR, 2);  //NOI18N
    
    private static class ScaleYAction extends ScaleActionFactory.ScaleAction {

        public ScaleYAction(ComposerActionFactory factory, SVGObject selected, MouseEvent me) {
            super(factory, selected, me);
        }

        @Override
        public boolean consumeEvent(AWTEvent evt, boolean isOutsideEvent) {
            if ( !isOutsideEvent && evt.getID() == MouseEvent.MOUSE_DRAGGED) {
                MouseEvent me = (MouseEvent)evt;
                
                //calculate area to repaint
                Rectangle bBox = m_scaled.getScreenBBox();
                //m_scaled.scale(calculateScale(me.getX(), me.getY()));
                m_scaled.scale(1, calculateScaleY(me.getY()), m_scalePtIdx);
                bBox.add(m_scaled.getScreenBBox());
                
                m_factory.getSceneManager().getScreenManager().repaint(bBox, SVGObjectOutline.SELECTOR_OVERLAP);
            } else if (evt.getID() == MouseEvent.MOUSE_RELEASED) {
                actionCompleted();
                m_scaled.commitChanges();
            }
            return false;
        }
        
        @Override
        public ActionMouseCursor getMouseCursor(boolean isOutsideEvent) {
            return isOutsideEvent ? null : SCALE_N_MOUSE_CURSOR;
        }        

    }
    
    public ScaleYActionFactory(SceneManager sceneMgr) {
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
                return new ScaleYAction(this, selObj, me);
            }
        } 
        return null;
    }

    @Override
    public ActionMouseCursor getMouseCursor(MouseEvent evt, boolean isOutsideEvent) {
        SVGObject selObj = getSelectedObject();
        if ( !isOutsideEvent && selObj != null) {
            if (isNScalePoint(selObj, evt)){
                return SCALE_N_MOUSE_CURSOR;
            } else if (isSScalePoint(selObj, evt)){
                return SCALE_S_MOUSE_CURSOR;
            }
        }
        return null;
    }
    
    
    private SVGObject getObjectToScaleAt( MouseEvent me) {
        SVGObject selObj = getSelectedObject();
        if (selObj != null){
            if ( isNScalePoint(selObj, me) || isSScalePoint(selObj, me)) {
                return selObj;
            }
        }
        return null;
    }
    
    private boolean isNScalePoint(SVGObject selObj, MouseEvent me) {
        return selObj.getOutline().isAtScaleNHandlePoint((float) me.getX(), (float) me.getY());
    }
    
    private boolean isSScalePoint(SVGObject selObj, MouseEvent me) {
        return selObj.getOutline().isAtScaleSHandlePoint((float) me.getX(), (float) me.getY());
    }
    
}
