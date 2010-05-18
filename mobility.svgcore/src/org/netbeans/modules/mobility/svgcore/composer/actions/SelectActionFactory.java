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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.ComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.view.source.SVGSourceMultiViewElement;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGAction;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 *
 * @author Pavel Benes
 */
public final class SelectActionFactory extends AbstractComposerActionFactory implements SceneManager.SelectionListener {
    private final AbstractSVGAction       m_navigateBackAction = 
        new AbstractSVGAction("svg_prev_sel", false) {  //NOI18N
            public void actionPerformed(ActionEvent e) {
                if ( m_selectionHistoryIndex > 0){
                    String [] selection = m_selectionHistory.get(--m_selectionHistoryIndex);
                    m_sceneMgr.setSelection(selection[0], false);
                    updateSelectionHistoryButtons();
                }
            }
    };            

    private final AbstractSVGAction       m_navigateForwardAction = 
        new AbstractSVGAction("svg_next_sel", false, 1) {  //NOI18N
            public void actionPerformed(ActionEvent e) {
                if ( m_selectionHistoryIndex < m_selectionHistory.size() - 1){
                    String [] selection = m_selectionHistory.get(++m_selectionHistoryIndex);
                    m_sceneMgr.setSelection(selection[0], false);
                    updateSelectionHistoryButtons();
                }
            }
    };            

    private final AbstractSVGAction       m_navigateUpAction = 
        new AbstractSVGAction("svg_parent_sel", false, 2) {  //NOI18N
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
                        m_sceneMgr.setSelection(elemId, false);
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

    public static MouseEvent getSelectionEvent(AWTEvent evt) {
        if ( evt.getID() == MouseEvent.MOUSE_CLICKED) {
            MouseEvent me = (MouseEvent)evt;
            if ( me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() > 0) {
                return me;
            }
        }
        return null;
    }
    
    public synchronized ComposerAction startAction(SVGObject selected) {
        return setActiveAction( new SelectAction(this, selected));
    }
    
    public synchronized ComposerAction startAction(AWTEvent e, boolean isOutsideEvent) {        
        MouseEvent me;
        
        if (!isOutsideEvent && (me=getSelectionEvent(e)) != null) {
            SVGObject [] objects = m_sceneMgr.getPerseusController().getObjectsAt(me.getX(), me.getY());
            if (objects != null && objects.length > 0)  {
                String id = objects[0].getElementId();
                
                if (me.getClickCount() > 1) {
                    SVGDataObject dObj = m_sceneMgr.getDataObject();
                    DocumentElement delem = dObj.getModel().getElementById(id);
                    if (delem != null) {
                        SVGSourceMultiViewElement.selectElement( dObj, delem.getStartOffset(), true);
                    }
                } 
                
                updateSelectionHistory( id);
                return setActiveAction(new SelectAction(this, objects[0]));
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
    
    public Action [] getMenuActions() {
        return new Action [] { m_navigateBackAction, m_navigateForwardAction, m_navigateUpAction};
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
