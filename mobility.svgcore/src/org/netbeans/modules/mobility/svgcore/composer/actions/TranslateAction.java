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
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.ComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;

/**
 *
 * @author Pavel Benes
 */
public class TranslateAction extends AbstractComposerAction {
    private final SVGObject m_translated;
    private       int       m_x;
    private       int       m_y;

    public TranslateAction(ComposerActionFactory factory,SVGObject translated, MouseEvent me) {
        super(factory);
        m_translated = translated;
        assert m_translated != null : "The translated object cannot be null";
        m_x = me.getX();
        m_y = me.getY();
        //m_selected.repaint(GraphicUtils.SELECTOR_OVERLAP);
    }

    public boolean consumeEvent(InputEvent evt) {
        if ( evt.getID() == MouseEvent.MOUSE_DRAGGED) {
            MouseEvent me = (MouseEvent)evt;
            int dx = me.getX() - m_x;
            int dy = me.getY() - m_y;
            //System.out.println("Dragging " + dx + "," + dy);
            Rectangle bBox = m_translated.getScreenBBox();
            float zoomRatio = m_translated.getScreenManager().getZoomRatio();
            m_translated.translate(dx / zoomRatio, dy / zoomRatio);
            bBox.add(m_translated.getScreenBBox());
            m_factory.getSceneManager().getScreenManager().repaint(bBox, SVGObjectOutline.SELECTOR_OVERLAP);
        } else {
            actionCompleted();
            m_translated.commitChanges();
        }
        return false;
    }

    public void paint(Graphics g, int x, int y) {
    }    
}
