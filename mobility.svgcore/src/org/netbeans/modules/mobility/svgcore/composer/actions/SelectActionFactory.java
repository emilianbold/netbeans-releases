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

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGAction;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 *
 * @author Pavel Benes
 */
public class SelectActionFactory extends AbstractComposerActionFactory implements SceneManager.SelectionListener {
    private final AbstractSVGAction       m_navigateBackAction = 
        new AbstractSVGAction("back.png", "HINT_SelectionBack", "LBL_SelectionBack", false) {  //NOI18N
            public void actionPerformed(ActionEvent e) {
                if ( m_selectionHistoryIndex > 0){
                    String [] selection = m_selectionHistory.get(--m_selectionHistoryIndex);
                    m_sceneMgr.setSelection(selection[0]);
                    updateSelectionHistoryButtons();
                }
            }
    };            

    private final AbstractSVGAction       m_navigateForwardAction = 
        new AbstractSVGAction("forward.png", "HINT_SelectionForward", "LBL_SelectionForward", false) {  //NOI18N
            public void actionPerformed(ActionEvent e) {
                if ( m_selectionHistoryIndex < m_selectionHistory.size() - 1){
                    String [] selection = m_selectionHistory.get(++m_selectionHistoryIndex);
                    m_sceneMgr.setSelection(selection[0]);
                    updateSelectionHistoryButtons();
                }
            }
    };            

    private final AbstractSVGAction       m_navigateUpAction = 
        new AbstractSVGAction("up.png", "HINT_SelectionUp", "LBL_SelectionUp", false) {  //NOI18N
            public void actionPerformed(ActionEvent e) {
                SVGObject [] selected = m_sceneMgr.getSelected();
                if (selected != null && selected.length > 0) {
                    assert selected[0] != null;
                    SVGLocatableElement elem = selected[0].getSVGElement();
                    assert elem != null;
                    Node parent = elem.getParentNode();
                    while( parent != null &&
                         !(parent instanceof SVGLocatableElement)) {
                        parent = parent.getParentNode();
                    }
                    if (parent != null) {
                        String elemId = ((SVGLocatableElement) parent).getId();
                        m_sceneMgr.setSelection(elemId);
                        updateSelectionHistory(elemId);
                    }                           
                }
            }
    };            
    
    private final List<String[]> m_selectionHistory = new ArrayList<String[]>();
    private int                  m_selectionHistoryIndex = -1;
    private       SelectAction   m_activeAction;
            
    public SelectActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }

    public static MouseEvent getSelectionEvent(InputEvent evt) {
        if ( evt.getID() == MouseEvent.MOUSE_CLICKED) {
            MouseEvent me = (MouseEvent)evt;
            if ( me.getClickCount() > 0) {
                return me;
            }
        }
        return null;
    }
    
    public synchronized ComposerAction startAction(SVGObject selected) {
        return setActiveAction( new SelectAction(this, selected));
    }
    
    public synchronized ComposerAction startAction(InputEvent e, boolean isOutsideEvent) {        
        MouseEvent me;
        
        if (!isOutsideEvent &&
            !m_sceneMgr.isReadOnly() &&
            (me=getSelectionEvent(e)) != null) {
            SVGObject [] objects = m_sceneMgr.getPerseusController().getObjectsAt(me.getX(), me.getY());
            if (objects != null && objects.length > 0)  {
                updateSelectionHistory( objects[0].getElementId());
                return setActiveAction(new SelectAction(this, objects[0], me));
            }    
        }
        return null;
    }
    
    public SelectAction getActiveAction() {
        if (m_activeAction != null && !m_activeAction.isCompleted()) {
            return m_activeAction;
        } else {
            return null;
        }
    }
    
    public AbstractSVGAction [] getActions() {
        return new AbstractSVGAction [] { m_navigateBackAction, m_navigateForwardAction, m_navigateUpAction};
    }

    public void selectionChanged(SVGObject[] newSelection, SVGObject[] oldSelection, boolean isReadOnly) {
        boolean navigationUpEnabled = false;
        if ( !m_sceneMgr.isReadOnly()) {
            if ( newSelection != null && newSelection.length > 0) {
                Node parent = newSelection[0].getSVGElement().getParentNode();
                if ( parent != null) {
                    navigationUpEnabled = true;
                }
            }
        }
        m_navigateUpAction.setEnabled(navigationUpEnabled);
        updateSelectionHistoryButtons();
    }

    
    private SelectAction setActiveAction( SelectAction active) {
        if (m_activeAction != null) {
            m_activeAction.actionCompleted();
        }
        m_activeAction = active;
        return active;
    }
    
    private void updateSelectionHistory(String elemId) {
        String [] selectedIds = new String[] { elemId};
        for (int i = m_selectionHistory.size() - 1; i > m_selectionHistoryIndex; i--) {
            m_selectionHistory.remove(i);
        }
        m_selectionHistoryIndex = m_selectionHistory.size();
        m_selectionHistory.add(selectedIds);
        updateSelectionHistoryButtons();
    }
    
    private void updateSelectionHistoryButtons() {
        m_navigateBackAction.setEnabled( !m_sceneMgr.isReadOnly() && m_selectionHistoryIndex > 0);
        m_navigateForwardAction.setEnabled(!m_sceneMgr.isReadOnly() && m_selectionHistoryIndex < m_selectionHistory.size() - 1);
    }    
}
