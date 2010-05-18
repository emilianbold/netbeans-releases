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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerAction;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObjectOutline;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 *
 * @author Pavel Benes
 */
public final class SelectAction extends AbstractComposerAction {
    private static final Color SELECTION_BODY_COLOR = new Color( 64, 64, 255, 64);
    private static final Color SELECTION_OUTLINE_COLOR = new Color( 64, 64, 255, 128);

    private final SVGObject m_selected;

    public SelectAction(SelectActionFactory factory, SVGObject selected) {
        super(factory);
        m_selected = selected;
        assert m_selected != null : "The selected object cannot be null"; //NOI18N
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
        if ( !m_isCompleted && !m_selected.isDeleted()) {
            if ( isReadOnly) {
                if ( getScreenManager().getHighlightObject()) {
                    SVGObjectOutline outline = m_selected.getOutline();
                    outline.highlight(g, x, y, SELECTION_BODY_COLOR);
                    outline.draw(g, x, y, SELECTION_OUTLINE_COLOR, false);
                }
            } else {
                m_selected.getOutline().draw(g, x, y, SVGObjectOutline.SELECTOR_BODY, true);
            }
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
