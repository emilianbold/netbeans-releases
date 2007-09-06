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
import org.netbeans.modules.mobility.svgcore.view.svg.SVGViewTopComponent;
import org.openide.util.NbBundle;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 *
 * @author Pavel Benes
 */
public final class HighlightAction extends AbstractComposerAction {
    private final SVGObject m_highlighted;

    public HighlightAction(ComposerActionFactory factory, SVGObject highlighted) {
        super(factory);
        assert highlighted != null : "Null object for highlight";
        m_highlighted = highlighted;
        getScreenManager().getAnimatorView().setToolTipText(getTooltipText());
        m_highlighted.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
    }

    public boolean consumeEvent(AWTEvent evt, boolean isOutsideEvent) {
        SceneManager sceneMgr = m_factory.getSceneManager();
        assert sceneMgr.containsAction(HighlightAction.class);
 
        if ( !isOutsideEvent) {
            if ( evt.getID() == MouseEvent.MOUSE_MOVED) {
                MouseEvent me = (MouseEvent) evt;
                SVGObject [] objects = sceneMgr.getPerseusController().getObjectsAt(me.getX(), me.getY());
                if (objects == null || objects.length == 0 || 
                    objects[0] != m_highlighted)  {
                    actionCompleted();
                }
            } else if ( evt.getID() == MouseEvent.MOUSE_EXITED) {
                actionCompleted();
            }
            
        }
        return false;
    }

    public void paint(Graphics g, int x, int y, boolean isReadOnly) {
        if (!m_highlighted.isDeleted()) {
            if (getScreenManager().getHighlightObject()) {
                m_highlighted.getOutline().highlight(g, x, y);
            }
        } else {
            actionCompleted();
        }
    }
    
    public void actionCompleted() {
        super.actionCompleted();
        getScreenManager().getAnimatorView().setToolTipText("");
        m_highlighted.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
    }
    
    private String getTooltipText() {
        String text       = "";  //NOI18N
        String selectedId = m_highlighted.getElementId();
        
        if (getScreenManager().getShowTooltip()) {
            SVGLocatableElement elem = m_highlighted.getSVGElement();
            
            String prefix = getPerseusController().getSVGDocument().toPrefix( 
                elem.getNamespaceURI(), elem);
            if (prefix == null || prefix.length() == 0){
               prefix = elem.getLocalName();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");  //NOI18N
            sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_Type", prefix)); //NOI18N
            sb.append("<br>"); //NOI18N

            String id = elem.getId();
            if (id != null){
                sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_Id", id)); //NOI18N
                sb.append("<br>"); //NOI18N
            }
            
            sb.append(getDataObject().getModel().describeElement(
                      selectedId, false, true, "<br>")); //NOI18N
            sb.append("</html>"); //NOI18N
            return sb.toString();
        }
        
        return text;    
    }
}
