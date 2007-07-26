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

import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.ComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.view.source.SVGSourceMultiViewElement;

/**
 *
 * @author Pavel Benes
 */
public class SelectAction extends AbstractComposerAction {
    private final SVGObject m_selected;

    public SelectAction(SelectActionFactory factory, SVGObject selected) {
        super(factory);
        m_selected = selected;
        m_selected.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
    }
    
    public SelectAction(ComposerActionFactory factory, SVGObject selected, MouseEvent me) {
        super(factory);
        m_selected = selected;
        assert m_selected != null : "The selected object cannot be null";

        if (me.getClickCount() > 1) {
            SVGDataObject dObj = m_factory.getSceneManager().getDataObject();
            String id = m_selected.getElementId();
            DocumentElement delem = dObj.getModel().getElementById(id);
            if (delem != null) {
                SVGSourceMultiViewElement.selectElement( dObj, delem, true);
            }
        }
        m_selected.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
    }

    public boolean consumeEvent(InputEvent evt, boolean isOutsideEvent) {
        SceneManager sceneMgr = m_factory.getSceneManager();
        assert sceneMgr.containsAction(SelectAction.class);

        MouseEvent me = SelectActionFactory.getSelectionEvent(evt);

        if ( me != null) {
            SVGObject [] objects = null;
            
            if (!isOutsideEvent) {
                objects = sceneMgr.getPerseusController().getObjectsAt(me.getX(), me.getY());
            }
            if (objects == null || objects.length == 0 || 
                objects[0] != m_selected)  {
                actionCompleted();
            }
        }
        return false;
    }

    public void paint(Graphics g, int x, int y) {
        if ( !m_isCompleted && !m_selected.isDeleted()) {
            m_selected.getOutline().draw(g, x, y);
        }
    }

    public SVGObject getSelected() {
        if ( m_isCompleted) {
            return null;
        } else if ( m_selected.isDeleted()) {
            actionCompleted();
            return null;
        } else {
            return m_selected;
        }
    }

    public void actionCompleted() {
        m_selected.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
        super.actionCompleted();
    }
}
