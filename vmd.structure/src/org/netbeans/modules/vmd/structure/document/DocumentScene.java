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
package org.netbeans.modules.vmd.structure.document;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.util.NbBundle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author David Kaspar
 */
public class DocumentScene extends ObjectScene {

    static final Color COLOR_HOVERED = Color.CYAN;
    static final Color COLOR_SELECTED = Color.YELLOW;

    private LabelWidget labelWidget;
    private WidgetAction hoverAction = ActionFactory.createHoverAction (new DocumentHoverProvider ());
    private WidgetAction selectAction = ActionFactory.createSelectAction (new DocumentSelectProvider ());

    private DesignDocument document;

    public DocumentScene () {
        setOpaque (false);
        getActions ().addAction (ActionFactory.createZoomAction ());
        getActions ().addAction (ActionFactory.createPanAction ());
        getActions ().addAction (hoverAction);

        labelWidget = new LabelWidget (this, NbBundle.getMessage (DocumentScene.class, "MSG_Loading")); // NOI18N
        labelWidget.setFont (getDefaultFont ().deriveFont (Font.BOLD, 24));
//        labelWidget.setForeground (Color.DARK_GRAY);
    }

    public void clear () {
        ArrayList<Object> objects = new ArrayList<Object> (getObjects ());
        for (Object object : objects) {
            findWidget (object).removeFromParent ();
            removeObject (object);
        }
        if (getChildren ().contains (labelWidget))
            removeChild (labelWidget);
    }

    public void setRootNode (DesignDocument document, ComponentWidget widget) {
        this.document = document;
        addChild (widget);
        addObject (document, widget);
    }

    public void setupLoadingDocument () {
        clear ();
        if (getChildren ().contains (labelWidget))
            return;
        addChild (labelWidget);
        validate ();
    }

    public WidgetAction getHoverAction () {
        return hoverAction;
    }

    public WidgetAction getSelectAction () {
        return selectAction;
    }

    private class DocumentHoverProvider implements TwoStateHoverProvider {

        private Paint background;

        public void unsetHovering (Widget widget) {
            widget.setBackground (background);
        }

        public void setHovering (Widget widget) {
            background = widget.getBackground ();
            widget.setBackground (COLOR_HOVERED);
        }
    }

    private class DocumentSelectProvider implements SelectProvider {

        public boolean isAimingAllowed (Widget widget, Point point, boolean b) {
            return false;
        }

        public boolean isSelectionAllowed (Widget widget, Point point, boolean b) {
            return true;
        }

        public void select (Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject (widget);
            final DesignComponent component = object != null ? ((ComponentWidget) findWidget (object)).getComponent () : null;
            final DesignDocument document = DocumentScene.this.document;
            if (document != null)
                document.getTransactionManager ().writeAccess (new Runnable() {
                    public void run () {
                        if (component != null)
                            document.setSelectedComponents (DocumentEditorView.DOCUMENT_ID, Collections.singleton (component));
                        else {
                            document.setSelectedComponents (DocumentEditorView.DOCUMENT_ID, Collections.<DesignComponent>emptySet ());
                        }
                    }
                });
        }

    }

}
