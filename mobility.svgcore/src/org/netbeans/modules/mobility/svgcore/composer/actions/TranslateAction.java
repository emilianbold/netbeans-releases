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
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.ComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;

/**
 *
 * @author Pavel Benes
 */
public final class TranslateAction extends AbstractComposerAction {
    private final SVGObject m_translated;
    private       int       m_x;
    private       int       m_y;
    private       boolean   m_changed = false;

    public TranslateAction(ComposerActionFactory factory,SVGObject translated, MouseEvent me) {
        super(factory);
        m_translated = translated;
        assert m_translated != null : "The translated object cannot be null";
        m_x = me.getX();
        m_y = me.getY();
        //m_selected.repaint(GraphicUtils.SELECTOR_OVERLAP);
    }

    public TranslateAction(ComposerActionFactory factory, SVGObject translated, KeyEvent ke) {
        super(factory);
        m_translated = translated;
        assert m_translated != null : "The translated object cannot be null";
        int [] diffs = TranslateActionFactory.getCoordDiff(ke);
        m_x = diffs[0];
        m_y = diffs[1];
        translate(m_x, m_y, true);
    }
    
    public boolean consumeEvent(AWTEvent evt, boolean isOutsideEvent) {
        if ( !isOutsideEvent)  {
            if (evt.getID() == MouseEvent.MOUSE_DRAGGED) {
                MouseEvent me = (MouseEvent)evt;
                int dx = me.getX() - m_x;
                int dy = me.getY() - m_y;
                //System.out.println("Dragging " + dx + "," + dy);
                
                float zoomRatio = m_translated.getScreenManager().getZoomRatio();
                translate(dx / zoomRatio, dy / zoomRatio, false);
                return false;
            } else {
                int [] diffs = TranslateActionFactory.getCoordDiff(evt);
                
                if (diffs != null) {
                    translate(m_x, m_y, true);
                    return true;
                }
            }
            
            actionCompleted();        
        }
        
        return false;
    }

    private void translate(float dx, float dy, boolean isRelative) {
        Rectangle bBox = m_translated.getScreenBBox();
        m_translated.translate(dx, dy, isRelative);
        bBox.add(m_translated.getScreenBBox());
        m_factory.getSceneManager().getScreenManager().repaint(bBox, SVGObjectOutline.SELECTOR_OVERLAP);
        m_changed = true;
    }
    
    public void actionCompleted() {
        if ( m_changed) {
            m_translated.applyTextChanges();
            m_translated.commitChanges();        
        }
        super.actionCompleted();
    }
}
