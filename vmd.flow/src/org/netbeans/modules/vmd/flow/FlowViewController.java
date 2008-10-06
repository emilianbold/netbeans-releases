/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.flow;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.vmd.VMDMinimizeAbility;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author David Kaspar
 */
public class FlowViewController implements DesignDocumentAwareness {

    public static final String FLOW_ID = "flow"; // NOI18N

//    private DataObjectContext context;
    private JComponent loadingPanel;
    private JPanel visual;
    private JToolBar toolbar;

    private JButton overviewButton;

    private DesignDocument designDocument;
    private JComponent view;

    public FlowViewController (DataObjectContext context) {
//        this.context = context;
        loadingPanel = IOUtils.createLoadingPanel ();
        visual = new JPanel (new BorderLayout ()) {
            @Override
            public void requestFocus() {
                super.requestFocus();
                if (view != null)
                    view.requestFocus();
            }

            @Override
            public boolean requestFocusInWindow() {
                if (view != null) {
                    super.requestFocusInWindow();
                    return view.requestFocusInWindow();
                } else
                    return super.requestFocusInWindow();
            }
        };

        toolbar = new JToolBar ();
        toolbar.setFloatable (false);
        toolbar.setRollover (true);
        toolbar.setPreferredSize (new Dimension (14, 14));
        toolbar.setSize (new Dimension (14, 14));
        JToolBar.Separator separator = new JToolBar.Separator ();
        separator.setOrientation(JSeparator.VERTICAL);
        toolbar.add (separator);

        addToolbarButton ("layout", NbBundle.getMessage (FlowViewController.class, "TTIP_Layout"), new ActionListener() { // NOI18N
            public void actionPerformed (ActionEvent e) {
                layout ();
            }
        });

        overviewButton = addToolbarButton ("overview", NbBundle.getMessage (FlowViewController.class, "TTIP_Overview"), new ActionListener() { // NOI18N
            public void actionPerformed (ActionEvent e) {
                overview ();
            }
        });

        addToolbarButton ("collapse-all", NbBundle.getMessage (FlowViewController.class, "TTIP_CollapseAll"), new ActionListener () { // NOI18N
            public void actionPerformed (ActionEvent e) {
                collapseAll ();
            }
        });

        addToolbarButton ("expand-all", NbBundle.getMessage (FlowViewController.class, "TTIP_ExpandAll"), new ActionListener() { // NOI18N
            public void actionPerformed (ActionEvent e) {
                expandAll ();
            }
        });

        context.addDesignDocumentAwareness (this);
    }

    private JButton addToolbarButton (String imageResourceName, String toolTipText, ActionListener listener) {
        final JButton button = new JButton (new ImageIcon (ImageUtilities.loadImage ("org/netbeans/modules/vmd/flow/resources/" + imageResourceName + ".png"))); // NOI18N
        button.setOpaque (false);
        button.setToolTipText (toolTipText);
        button.setBorderPainted (false);
        button.setRolloverEnabled (true);
        button.setSize (14, 14);
        button.addActionListener (listener);
        toolbar.add (button);
        return button;
    }

    private void layout () {
        IOUtils.runInAWTNoBlocking (new Runnable() {
            public void run () {
                FlowAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (FlowAccessController.class) : null;
                if (accessController == null)
                    return;
                FlowScene scene = accessController.getScene ();
                scene.layoutScene ();
                scene.validate ();
            }
        });
    }

    private void overview () {
        DesignDocument doc = designDocument;
        FlowAccessController accessController = doc != null ? doc.getListenerManager ().getAccessController (FlowAccessController.class) : null;
        if (accessController == null)
            return;

        JPopupMenu popup = new JPopupMenu ();
        popup.setLayout (new BorderLayout ());
        JComponent sateliteView = accessController.createSatelliteView ();
        popup.add (sateliteView, BorderLayout.CENTER);
        popup.show (overviewButton, (overviewButton.getSize ().width - sateliteView.getPreferredSize ().width) / 2, overviewButton.getSize ().height);
    }

    private void collapseAll () {
        IOUtils.runInAWTNoBlocking (new Runnable() {
            public void run () {
                FlowAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (FlowAccessController.class) : null;
                if (accessController == null)
                    return;
                FlowScene scene = accessController.getScene ();
                for (Object object : scene.getObjects ()) {
                    Widget widget = scene.findWidget (object);
                    if (widget instanceof VMDMinimizeAbility)
                        ((VMDMinimizeAbility) widget).collapseWidget ();
                }
            }
        });
    }

    private void expandAll () {
        IOUtils.runInAWTNoBlocking (new Runnable() {
            public void run () {
                FlowAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (FlowAccessController.class) : null;
                if (accessController == null)
                    return;
                FlowScene scene = accessController.getScene ();
                for (Object object : scene.getObjects ()) {
                    Widget widget = scene.findWidget (object);
                    if (widget instanceof VMDMinimizeAbility)
                        ((VMDMinimizeAbility) widget).expandWidget ();
                }
            }
        });
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
                designDocument = newDesignDocument;
                FlowAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (FlowAccessController.class) : null;
                view = accessController != null ? accessController.getCreateView () : null;

                visual.removeAll ();
                if (view != null) {
		    // Fix for #142397 - Only visible part of Flow View is printed
		    view.putClientProperty("print.printable", Boolean.TRUE); // NOI18N

                    JScrollPane scroll = new JScrollPane (view);
                    scroll.getHorizontalScrollBar ().setUnitIncrement (64);
                    scroll.getHorizontalScrollBar ().setBlockIncrement (256);
                    scroll.getVerticalScrollBar ().setUnitIncrement (64);
                    scroll.getVerticalScrollBar ().setBlockIncrement (256);
                    visual.add (scroll, BorderLayout.CENTER);
                    if (visual.hasFocus ())
                        view.requestFocus ();
                } else
                    visual.add (loadingPanel, BorderLayout.CENTER);
                visual.validate ();
            }
        });
    }

}
