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
public final class TranslateActionFactory extends AbstractComposerActionFactory {
    private static final ActionMouseCursor TRANSLATE_MOUSE_CURSOR = new ActionMouseCursor( Cursor.MOVE_CURSOR, 1);
    
    private static int [] DIFF_LEFT   = new int[] { -1, 0};
    private static int [] DIFF_RIGHT  = new int[] { 1, 0};
    private static int [] DIFF_DOWN   = new int[] { 0, 1};
    private static int [] DIFF_UP     = new int[] { 0, -1};
    
    public TranslateActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }
    
    public synchronized ComposerAction startAction(AWTEvent e, boolean isOutsideEvent) {        
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
    
    static int [] getCoordDiff(AWTEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            KeyEvent ke = (KeyEvent) e;
            if (ke.getModifiers() == 0) {
                switch (ke.getKeyCode()) {
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
        }
        return null;
    }
}
