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
 *
 */

package org.netbeans.modules.vmd.screen;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.io.javame.MidpProjectPropertiesSupport;
import org.netbeans.modules.vmd.api.model.DesignDocument;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class ScreenViewController implements DesignDocumentAwareness {

    public static final String SCREEN_ID = "screen"; // NOI18N

    private DesignDocument designDocument;

    private JPanel visual;
    private JToolBar toolbar;

    private JComponent loadingPanel;

    public ScreenViewController (DataObjectContext context) {
        loadingPanel = IOUtils.createLoadingPanel ();

        visual = new JPanel ();
        visual.setLayout (new BorderLayout ());
        toolbar = new JToolBar ();
        toolbar.setFloatable (false);

        context.addDesignDocumentAwareness (this);
    }

    public JComponent getVisualRepresentation () {
        return visual;
    }

    public JComponent getToolbarRepresentation () {
        return toolbar;
    }

    public void setDesignDocument (final DesignDocument newDesignDocument) {
        IOUtils.runInAWTNoBlocking (new Runnable () {
            public void run () {
                if (designDocument != null) {
                    ScreenAccessController accessController = designDocument.getListenerManager ().getAccessController (ScreenAccessController.class);
                    if (accessController != null)
                        accessController.hideNotify ();
                }
                toolbar.removeAll ();
                visual.removeAll ();

                designDocument = newDesignDocument;

                final ScreenAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (ScreenAccessController.class) : null;
                if (accessController != null) {
                    accessController.showNotify ();
                    final DataObjectContext context = ProjectUtils.getDataObjectContextForDocument (designDocument);
                    if (context != null) {
                        designDocument.getTransactionManager ().readAccess (new Runnable () {
                            public void run () {
                                accessController.setScreenSize (MidpProjectPropertiesSupport.getDeviceScreenSizeFromProject(context));
                            }
                        });
                    }
                    visual.add (new JScrollPane (accessController.getMainPanel ()), BorderLayout.CENTER);

                    JToolBar.Separator separator = new JToolBar.Separator ();
                    separator.setOrientation(JSeparator.VERTICAL);
                    toolbar.add (separator);

                    toolbar.add (accessController.getToolBar ());
                } else {
                    visual.add (loadingPanel, BorderLayout.CENTER);
                }
                toolbar.validate ();
                visual.validate ();
            }
        });
    }

    public void setScreenSize (final Dimension deviceScreenSize) {
        IOUtils.runInAWTNoBlocking (new Runnable () {
            public void run () {
                if (designDocument != null) {
                    designDocument.getTransactionManager ().readAccess (new Runnable () {
                        public void run () {
                            ScreenAccessController accessController = designDocument.getListenerManager ().getAccessController (ScreenAccessController.class);
                            if (accessController != null)
                                accessController.setScreenSize (deviceScreenSize);
                        }
                    });
                }
            }
        });
    }
}
