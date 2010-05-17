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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.m2g.SVGImage;
import javax.swing.Action;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.ActionMouseCursor;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.PerseusController;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGAction;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 *
 * @author Pavel Benes
 */
public final class MoveFocusActionFactory extends AbstractComposerActionFactory {
    private static final ActionMouseCursor ACTIVATE_MOUSE_CURSOR = new ActionMouseCursor( 
                Cursor.HAND_CURSOR, 3);
    
    private static final Color FOCUS_OUTLINE_COLOR = Color.GREEN;
    
    private final class ShowFocusAction extends AbstractComposerAction {
        private final SVGObject m_focused;

        public ShowFocusAction(SVGObject focused) {
            super(MoveFocusActionFactory.this);
            m_focused = focused;
        }

        public void paint(Graphics g, int x, int y, boolean isReadOnly) {
            if ( isReadOnly && !m_isCompleted) {
                SVGObjectOutline outline = m_focused.getOutline();
                outline.draw(g, x, y, FOCUS_OUTLINE_COLOR, false);
                outline.setDirty();
            }
        }
                
        public void actionCompleted() {
            m_focused.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
            super.actionCompleted();
        }
    }
    
    private final AbstractSVGAction       m_focusNext = 
        new AbstractSVGAction("svg_focus_next", false) { //NOI18N
            public void actionPerformed(ActionEvent e) {
                if ( ++m_focusedIdIndex > m_focusTargets.size() - 1) {
                    m_focusedIdIndex = 0;
                }
                focusElement();
            }
    };            

    
    private final transient AbstractSVGAction       m_focusPrevious = 
        new AbstractSVGAction("svg_focus_previous", false) { //NOI18N
            public void actionPerformed(ActionEvent e) {
                if ( --m_focusedIdIndex < 0) {
                    m_focusedIdIndex = m_focusTargets.size() - 1;
                }
                assert m_focusedIdIndex >= 0;
                focusElement();
            }
    };            

    private final transient AbstractSVGAction       m_activateFocused = 
        new AbstractSVGAction("svg_activate_focused", false) { //NOI18N
            public void actionPerformed(ActionEvent e) {
                SVGImage svgImg = m_sceneMgr.getSVGImage();
                if (svgImg != null) {
                    svgImg.activate();
                }
            }
    };            
     
    private final List<String> m_focusTargets    =  new ArrayList<String>();
    private int                m_focusedIdIndex  = -1;
    private ShowFocusAction    m_showFocusAction = null;
    
    public MoveFocusActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }

    private void focusElement() {
        if (m_showFocusAction != null) {
            m_showFocusAction.actionCompleted();
            m_showFocusAction = null;
        }
        
        SVGObject  obj = null;
        String     id  = m_focusTargets.get(m_focusedIdIndex);
        if ( id != null) {
            obj = getPerseusController().getObjectById(id);
            assert obj != null; 
            m_showFocusAction = new ShowFocusAction(obj);
            m_sceneMgr.startAction( m_showFocusAction);
        }
        m_sceneMgr.getSVGImage().focusOn(obj != null ? obj.getSVGElement() : null);
        m_sceneMgr.getScreenManager().repaint();            
    }

    public synchronized ComposerAction startAction(AWTEvent e, boolean isOutsideEvent) {        
        if ( e.getID() == SceneManager.EVENT_ANIM_STARTED) {
            ((PerseusController) e.getSource()).getFocusableTargets(m_focusTargets);
            m_focusedIdIndex = 0;
            setActionseEnabled(true);
        } else if ( e.getID() == SceneManager.EVENT_ANIM_STOPPED) {
            m_focusTargets.clear();
            setActionseEnabled(false);
        } else if (e.getID() == MouseEvent.MOUSE_CLICKED) {
            MouseEvent me = (MouseEvent) e;
            if ( me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() == 1) {
                int index = getFocusTargetIdAt( me, isOutsideEvent);
                if ( index != -1) {
                    m_focusedIdIndex = index;
                    focusElement();
                    m_activateFocused.actionPerformed(null);
                }
            }
        }
        return null;
    }
    
    public ActionMouseCursor getMouseCursor(MouseEvent me, boolean isOutsideEvent) {
        return me.getID() == MouseEvent.MOUSE_MOVED && getFocusTargetIdAt(me, isOutsideEvent) != -1 ? ACTIVATE_MOUSE_CURSOR : null;
    }       
    
    private int getFocusTargetIdAt(MouseEvent me, boolean  isOutsideEvent) {
        if ( !isOutsideEvent &&
              m_sceneMgr.isReadOnly() &&
              m_focusTargets.size() > 0) {
            SVGLocatableElement elem = m_sceneMgr.getPerseusController().findElementAt(me.getX(), me.getY());
            if (elem != null)  {
                String id = elem.getId();
                if ( id != null)
                    return m_focusTargets.indexOf(id); {
                }
            }
        }
        return -1;
    }
    
    public Action [] getMenuActions() {
        return new Action [] { m_focusNext, m_focusPrevious, m_activateFocused};
    }    
    
    private void setActionseEnabled(boolean enabled) {
        m_activateFocused.setEnabled(enabled);
        m_focusNext.setEnabled(enabled);
        m_focusPrevious.setEnabled(enabled);
    }
}
