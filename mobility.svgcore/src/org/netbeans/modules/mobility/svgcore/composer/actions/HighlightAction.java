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
        assert highlighted != null : "Null object for highlight"; //NOI18N
        m_highlighted = highlighted;
        getScreenManager().getAnimatorView().setToolTipText(getTooltipText());
        m_highlighted.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
    }

    @Override
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

    @Override
    public void paint(Graphics g, int x, int y, boolean isReadOnly) {
        if (!m_highlighted.isDeleted()) {
            if ( !m_isCompleted && getScreenManager().getHighlightObject()) {
                m_highlighted.getOutline().highlight(g, x, y);
            }
        } else {
            actionCompleted();
        }
    }
    
    @Override
    public void actionCompleted() {
        super.actionCompleted();
        getScreenManager().getAnimatorView().setToolTipText("");
        m_highlighted.repaint(SVGObjectOutline.SELECTOR_OVERLAP);
    }
    
    private String getTooltipText() {
        String text       = "";  //NOI18N
        
        if (getScreenManager().getShowTooltip()) {
            SVGLocatableElement elem = m_highlighted.getSVGElement();
            
            String prefix = getPerseusController().getSVGDocument().toPrefix( 
                elem.getNamespaceURI(), elem);
            if (prefix == null || prefix.length() == 0){
               prefix = elem.getLocalName();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("<html>&nbsp;");  //NOI18N
            sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_Type", prefix)); //NOI18N
            sb.append("<br>&nbsp;"); //NOI18N

            String id = elem.getId();
            if (id != null){
                sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "LBL_Id", id)); //NOI18N
                sb.append("<br>"); //NOI18N
                sb.append(getDataObject().getModel().describeElement(id /*, false, true, "<br>"*/)); //NOI18N
            }
            
            sb.append("</html>"); //NOI18N
            return sb.toString();
        }
        
        return text;    
    }
}
