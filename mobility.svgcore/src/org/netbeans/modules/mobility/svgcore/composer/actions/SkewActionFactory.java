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
import org.openide.util.ImageUtilities;

/**
 *
 * @author Pavel Benes
 */
public final class SkewActionFactory extends AbstractComposerActionFactory {
    private static final ActionMouseCursor SKEW_MOUSE_CURSOR = new ActionMouseCursor( 
                Toolkit.getDefaultToolkit().createCustomCursor(ImageUtilities.loadImage ("org/netbeans/modules/mobility/svgcore/resources/skew_cursor.png"), // NOI18N
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
            m_skewed.commitChanges();
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
