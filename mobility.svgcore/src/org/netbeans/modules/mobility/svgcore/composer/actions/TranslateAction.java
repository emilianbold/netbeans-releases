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
 */package org.netbeans.modules.mobility.svgcore.composer.actions;

import java.awt.AWTEvent;
import java.awt.Rectangle;
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
        if (!isOutsideEvent) {
            if (evt instanceof MouseEvent) {
                if (evt.getID() == MouseEvent.MOUSE_DRAGGED) {
                    MouseEvent me = (MouseEvent) evt;
                    int dx = me.getX() - m_x;
                    int dy = me.getY() - m_y;
                    //System.out.println("Dragging " + dx + "," + dy);

                    float zoomRatio = m_translated.getScreenManager().getZoomRatio();
                    translate(dx / zoomRatio, dy / zoomRatio, false);
                } else if (evt.getID() == MouseEvent.MOUSE_RELEASED) {
                    actionCompleted();
                    m_translated.commitChanges();
                }
            } else { // key event
                int[] diffs = TranslateActionFactory.getCoordDiff(evt);

                if (diffs != null) {
                    translate(m_x, m_y, true);
                    return true;
                }
                actionCompleted();
                m_translated.commitChanges();
            }

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
            if (getScreenManager().getShowAllArea()){
                getScreenManager().refresh();
            }
        }
        super.actionCompleted();
    }
}
