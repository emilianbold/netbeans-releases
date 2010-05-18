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
