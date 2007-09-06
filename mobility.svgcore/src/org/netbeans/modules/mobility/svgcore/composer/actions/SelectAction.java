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
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.ComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 *
 * @author Pavel Benes
 */
public final class SelectAction extends AbstractComposerAction {
    private final SVGObject m_selected;

    public SelectAction(SelectActionFactory factory, SVGObject selected) {
        super(factory);
        m_selected = selected;
        assert m_selected != null : "The selected object cannot be null";
        m_selected.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
    }
    
    public boolean consumeEvent(AWTEvent evt, boolean isOutsideEvent) {
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

    public void paint(Graphics g, int x, int y, boolean isReadOnly) {
        if ( !isReadOnly && !m_isCompleted && !m_selected.isDeleted()) {
            m_selected.getOutline().draw(g, x, y, SVGObjectOutline.SELECTOR_BODY, true);
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
