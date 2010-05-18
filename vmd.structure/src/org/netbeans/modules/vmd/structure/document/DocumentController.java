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
