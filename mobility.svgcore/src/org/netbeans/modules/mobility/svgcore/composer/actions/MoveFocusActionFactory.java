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
        if ( e.getID() == PerseusController.EVENT_ANIM_STARTED) {
            ((PerseusController) e.getSource()).getFocusableTargets(m_focusTargets);
            m_focusedIdIndex = 0;
            setActionseEnabled(true);
        } else if ( e.getID() == PerseusController.EVENT_ANIM_STOPPED) {
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
            SVGObject [] objects = m_sceneMgr.getPerseusController().getObjectsAt(me.getX(), me.getY());
            if (objects != null && objects.length > 0 && objects[0] != null)  {
                String id = objects[0].getElementId();
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
