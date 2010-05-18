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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.rest.wadl.design.view.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.websvc.rest.wadl.design.MediaType;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.AddBodyElementPanel.BodyElementType;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Ayub Khan
 */
public class AddBodyElementAction<T extends WadlComponent> extends AbstractAction implements Node.Cookie {

    public static final String ADD_BODYELEMENT = "ADD_BODYELEMENT";
    private T parent;
    private WadlModel model;
    private ParamStyle type;

    /**
     * Creates a new instance of AddBodyElemAction
     * @param implementationClass fileobject of service implementation class
     */
    public AddBodyElementAction(ParamStyle type, T parent, WadlModel model) {
        super(NbBundle.getMessage(AddBodyElementAction.class, "LBL_AddBodyElement", 
                getType(parent instanceof Response)));
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/websvc/rest/wadl/design/view/resources/method.png", false));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddBodyElementAction.class, 
                "Hint_AddBodyElement", getType(parent instanceof Response)));
        putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(AddBodyElementAction.class, 
                "LBL_AddBodyElement_mnem_pos", getType(parent instanceof Response))));
        this.type = type;
        this.parent = parent;
        this.model = model;
    }

    private static String getType(boolean isResponse) {
        return isResponse?"Representation/Fault":"Representation";
    }

    public void actionPerformed(ActionEvent arg0) {
        if (parent instanceof Request) {
            addBodyElement(null, BodyElementType.REPRESENTATION);
        } else {
            final AddBodyElementPanel panel = new AddBodyElementPanel(parent);
            boolean closeDialog = false;
            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(AddBodyElementAction.class, "TTL_AddBodyElement", 
                    getType(parent instanceof Response)));
            while (!closeDialog) {
                DialogDisplayer.getDefault().notify(desc);
                if (desc.getValue() == DialogDescriptor.OK_OPTION) {
                    closeDialog = true;
                    final ProgressHandle handle = ProgressHandleFactory.createHandle(
                            NbBundle.getMessage(AddBodyElementAction.class, "MSG_AddingBodyElement",
                            getType(parent instanceof Response),
                            panel.getBodyElementType())); //NOI18N
                    addBodyElement(handle, panel.getBodyElementType());
                } else {
                    closeDialog = true;
                }
            }
        }
    }

    public void addBodyElement(BodyElementType type) throws IOException {
        WadlComponent elem = null;
        try {
            model.startTransaction();
            if (parent instanceof Request) {
                if (type.equals(BodyElementType.REPRESENTATION)) {
                    elem = model.getFactory().createRepresentation();
                    ((Representation) elem).setMediaType(MediaType.XML.value());
                    ((Request) parent).addRepresentation((Representation) elem);
                }
            } else if (parent instanceof Response) {
                if (type.equals(BodyElementType.REPRESENTATION)) {
                    elem = model.getFactory().createRepresentation();
                    ((Representation) elem).setMediaType(MediaType.XML.value());
                    ((Response) parent).addRepresentation((Representation) elem);
                } else if (type.equals(BodyElementType.FAULT)) {
                    elem = model.getFactory().createFault();
                    ((Fault) elem).setMediaType(MediaType.XML.value());
                    ((Response) parent).addFault((Fault) elem);
                }
            }
        } finally {
            model.endTransaction();
        }
        PropertyChangeListener[] listeners = getPropertyChangeListeners();
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(new PropertyChangeEvent(parent, ADD_BODYELEMENT, null, elem));
        }
    }

    private void addBodyElement(final ProgressHandle handle, final BodyElementType type) {
//        Task task = new Task(new Runnable() {
//
//            public void run() {
                try {
                    if (handle != null)
                        handle.start();
                    addBodyElement(type);
                } catch (Exception e) {
                    if (handle != null)
                        handle.finish();
                    ErrorManager.getDefault().notify(e);
                } finally {
                    if (handle != null)
                        handle.finish();
                }
//            }
//        });
//        RequestProcessor.getDefault().post(task);
    }

    private WadlModel getModel() {
        return this.model;
    }
}
