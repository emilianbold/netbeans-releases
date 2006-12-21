/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

        labelWidget = new LabelWidget (this, "Loading Document ...");
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
