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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.composer.ScreenManager;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGToggleAction;

/**
 *
 * @author Pavel Benes
 */
public final class HighlightActionFactory extends AbstractComposerActionFactory {
    
    private abstract class ExtendedAction extends AbstractSVGToggleAction {
        private boolean m_state;
        
        protected ExtendedAction( String id) {
            super(id);   
        }
        
        public void actionPerformed(ActionEvent e) {
            setIsSelectedImpl( !isSelectedImpl());
            refresh();
        }

        public void animStarted() {
            m_state = setIsSelectedImpl(false);
            setEnabled(false);                
        }

        public void animStopped() {
            setIsSelectedImpl(m_state);
            setEnabled(true);
        }   
        
        public void refresh() {
            setIsSelected( isSelectedImpl());
        }
        
        protected abstract boolean isSelectedImpl();
        
        protected abstract boolean setIsSelectedImpl(boolean newState);
    };
    
    private final ExtendedAction  m_highlightAction = 
        new ExtendedAction("svg_toggle_highlight") {  //NOI18N
            protected boolean isSelectedImpl() {
                return getScreenManager().getHighlightObject();
            }
            
            protected boolean setIsSelectedImpl(boolean newState) {
                return getScreenManager().setHighlightObject(newState);
            }
    };    
        
    private final ExtendedAction  m_tooltipAction = 
        new ExtendedAction("svg_toggle_tooltip") {  //NOI18N
            protected boolean isSelectedImpl() {
                return getScreenManager().getShowTooltip();
            }
            
            protected boolean setIsSelectedImpl(boolean newState) {
                return getScreenManager().setShowTooltip(newState);
            }
    };    
        
    public HighlightActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }

    public synchronized ComposerAction startAction(AWTEvent e, boolean isOutsideEvent) {        
        if ( !isOutsideEvent && e.getID() == MouseEvent.MOUSE_MOVED) {
            if ( !m_sceneMgr.containsAction(HighlightAction.class)) {
                MouseEvent me = (MouseEvent)e;
                SVGObject [] objects = m_sceneMgr.getPerseusController().getObjectsAt(me.getX(), me.getY());
                if (objects != null && objects.length > 0 && objects[0] != null)  {
                    return new HighlightAction(this, objects[0]);
                }
            }             
        }
        if ( e.getID() == SceneManager.EVENT_ANIM_STARTED) {
            m_highlightAction.animStarted();
            m_tooltipAction.animStarted();
        } else if ( e.getID() == SceneManager.EVENT_ANIM_STOPPED) {
            m_highlightAction.animStopped();
            m_tooltipAction.animStopped();
        } else if ( e.getID() == SceneManager.EVENT_IMAGE_DISPLAYED) {
            m_highlightAction.refresh(); 
            m_tooltipAction.refresh();
        }
        
        return null;
    }

    public Action [] getMenuActions() {
        return new Action [] { m_highlightAction, m_tooltipAction};
    }    
    
    private ScreenManager getScreenManager() {
        return getSceneManager().getScreenManager();
    }
}
