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
package org.netbeans.modules.vmd.structure.registry;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.model.*;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author David Kaspar
 */
public class RegistryEditorView implements DataEditorView, DescriptorRegistryListener {

    private static final long serialVersionUID = 3328221238376153199L;

    static final String REGISTRY_ID = "registry"; // NOI18N

    private DataObjectContext context;
    private transient DescriptorRegistry registry;

    private transient JToolBar toolbarRepresentation;
    private transient JScrollPane scrollPane;
    private transient RegistryScene scene;
    private transient JComponent view;

    public RegistryEditorView () {
    }

    public RegistryEditorView (DataObjectContext context) {
        this.context = context;
        init ();
    }

    private void init () {
        registry = DescriptorRegistry.getDescriptorRegistry (context.getProjectType (), context.getProjectID ());
        scene = new RegistryScene ();
        view = scene.createView ();
        scrollPane = new JScrollPane (view);
        toolbarRepresentation = new JToolBar ();
        toolbarRepresentation.setFloatable (false);
    }

    public DataObjectContext getContext () {
        return context;
    }

    public Kind getKind () {
        return Kind.MODEL;
    }

    public boolean canShowSideWindows () {
        return true;
    }

    public Collection<String> getTags () {
        return Collections.emptySet ();
    }

    public String preferredID () {
        return REGISTRY_ID;
    }

    public String getDisplayName () {
        return NbBundle.getMessage (RegistryEditorView.class, "TITLE_RegistryView"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (RegistryEditorView.class);
    }

    public JComponent getVisualRepresentation () {
        return scrollPane;
    }

    public JComponent getToolbarRepresentation () {
        return toolbarRepresentation;
    }

    public UndoRedo getUndoRedo () {
        return null;
    }

    public void componentOpened () {
        registry.addRegistryListener (this);
    }

    public void componentClosed () {
        registry.removeRegistryListener (this);
    }

    public void componentShowing () {
    }

    public void componentHidden () {
    }

    public void componentActivated () {
    }

    public void componentDeactivated () {
    }

    public int getOpenPriority () {
        return getOrder ();
    }

    public int getEditPriority () {
        return - getOrder ();
    }

    public int getOrder () {
        return 12;
    }

    public void descriptorRegistryUpdated () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                update ();
            }
        });
    }

    private void update () {
        registry.readAccess (new Runnable() {
            public void run () {
                RegistryWidget widget = new RegistryWidget (scene, false, null, NbBundle.getMessage (RegistryEditorView.class, "DISP_Descriptors")); // NOI18N
                Collection<ComponentDescriptor> descriptors = registry.getComponentDescriptors ();
                scene.clear ();
                scene.addRootNode ("descriptors", widget); // NOI18N
                updateForSuper (descriptors, null, widget);

                widget = new RegistryWidget (scene, false, null, NbBundle.getMessage (RegistryEditorView.class, "DISP_Producers")); // NOI18N
                scene.addRootNode ("producers", widget); // NOI18N
                for (ComponentProducer producer : registry.getComponentProducers ()) {
                    PaletteDescriptor paletteDescriptor = producer.getPaletteDescriptor ();
                    Widget sub;
                    if (paletteDescriptor != null) {
                        String smallIcon = paletteDescriptor.getSmallIcon ();
                        Image image = smallIcon != null ? ImageUtilities.loadImage(smallIcon) : null;
                        sub = new RegistryWidget (scene, true, image, paletteDescriptor.getDisplayName ());
                    } else
                        sub = new RegistryWidget (scene, false, null, producer.getMainComponentTypeID ().toString ());
                    widget.addSub ("producer:" + producer.getProducerID (), sub); // NOI18N
                }

                scene.validate ();
            }
        });
    }

    private void updateForSuper (Collection<ComponentDescriptor> descriptors, ComponentDescriptor superDescriptor, RegistryWidget widget) {
        for (ComponentDescriptor componentDescriptor : descriptors) {
            if (componentDescriptor.getSuperDescriptor () == superDescriptor) {
                PaletteDescriptor paletteDescriptor = componentDescriptor.getPaletteDescriptor ();
                RegistryWidget sub;
                if (paletteDescriptor != null) {
                    String smallIcon = paletteDescriptor.getSmallIcon ();
                    Image image = smallIcon != null ? ImageUtilities.loadImage(smallIcon) : null;
                    sub = new RegistryWidget (scene, true, image, paletteDescriptor.getDisplayName ());
                } else
                    sub = new RegistryWidget (scene, false, null, componentDescriptor.getTypeDescriptor ().getThisType ().toString ());
                widget.addSub ("descriptor:" + componentDescriptor.getTypeDescriptor ().getThisType (), sub);

                updateForSuper (descriptors, componentDescriptor, sub);
            }
        }
    }

    private void writeObject (java.io.ObjectOutputStream out) throws IOException {
        out.writeObject (context);
    }

    private void readObject (java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object object = in.readObject ();
        if (! (object instanceof DataObjectContext))
            throw new ClassNotFoundException ("DataObjectContext expected but not found"); // NOI18N
        context = (DataObjectContext) object;
        init ();
    }

}
