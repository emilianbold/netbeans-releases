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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.Action;
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
