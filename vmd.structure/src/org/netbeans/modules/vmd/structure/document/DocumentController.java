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

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.model.*;

import javax.swing.*;
import java.util.Collection;

/**
 * @author David Kaspar
 */
public class DocumentController implements DesignDocumentAwareness, DesignListener {

    private DataObjectContext context;
    private JToolBar toolbarRepresentation;
    private JScrollPane scrollPane;

    private DesignDocument document;
    private DocumentScene scene;
    private JComponent view;

    public DocumentController (DataObjectContext context) {
        this.context = context;
        scene = new DocumentScene ();
        view = scene.createView ();
        scrollPane = new JScrollPane (view);
    }

    public JComponent getVisualRepresentation () {
        return scrollPane;
    }

    public void setDesignDocument (final DesignDocument newDocument) {
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                updateDocumentReference (newDocument);
            }
        });
    }

    public void attach () {
        context.addDesignDocumentAwareness (this);
    }

    public void deattach () {
        context.removeDesignDocumentAwareness (this);
    }

    public JComponent getToolbarRepresentation () {
        if (toolbarRepresentation == null) {
            toolbarRepresentation = new JToolBar ();
            toolbarRepresentation.setFloatable (false);
        }
        return toolbarRepresentation;
    }

    private void updateDocumentReference (DesignDocument wantedDocument) {
        if (document == wantedDocument)
            return;
        if (document != null)
            document.getListenerManager ().removeDesignListener (this);
        document = wantedDocument;
        if (document != null) {
            document.getListenerManager ().addDesignListener (this, new DesignEventFilter ().setGlobal (true));
            designChanged (null);
        } else
            scene.setupLoadingDocument ();
    }

    public void designChanged (DesignEvent event) {
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                final DesignDocument document = DocumentController.this.document;
                if (document != null)
                    document.getTransactionManager ().readAccess (new Runnable() {
                        public void run () {
                            updateScene (document);
                        }
                    });
            }
        });
    }

    private void updateScene (DesignDocument document) {
        if (document == null)
            return;

        scene.clear ();
        Collection<DesignComponent> selectedComponents = document.getSelectedComponents ();
        DesignComponent rootComponent = document.getRootComponent ();
        ComponentWidget widget = new ComponentWidget (scene, rootComponent, selectedComponents.contains (rootComponent));
        scene.setRootNode (document, widget);
        updateForChildren (selectedComponents, widget, rootComponent);
        scene.validate ();
    }

    private void updateForChildren (Collection<DesignComponent> selectedComponents, ComponentWidget widget, DesignComponent component) {
        for (DesignComponent child : component.getComponents ()) {
            ComponentWidget childWidget = new ComponentWidget (scene, child, selectedComponents.contains (child));
            widget.addChildComponentWidget (childWidget);
            updateForChildren (selectedComponents, childWidget, child);
        }
    }

}
