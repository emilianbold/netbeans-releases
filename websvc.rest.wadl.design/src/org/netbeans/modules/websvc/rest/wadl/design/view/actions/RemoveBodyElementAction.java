
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
/*
 * RemoveBodyElementAction.java
 *
 * Created on April 6, 2007, 10:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.websvc.rest.wadl.design.view.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Set;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Ayub Khan
 */
public class RemoveBodyElementAction<T extends WadlComponent> extends AbstractAction {

    public static final String REMOVE_BODYELEMENTS = "REMOVE_BODYELEMENTS";
    private Set<T> elements;
    private WadlModel model;

    /** Creates a new instance of RemoveBodyElementAction */
    public RemoveBodyElementAction(Set<T> elements, WadlModel model) {
        super(NbBundle.getMessage(RemoveBodyElementAction.class, "LBL_RemoveBodyElement", 
                getType((elements.iterator().next() instanceof Representation))));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(
                RemoveBodyElementAction.class, "Hint_RemoveBodyElement",
                getType((elements.iterator().next() instanceof Representation))));
        putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(
                RemoveBodyElementAction.class, "LBL_RemoveBodyElement_mnem_pos",
                getType((elements.iterator().next() instanceof Representation)))));
        this.elements = elements;
        this.model = model;
//        setEnabled(false);
    }
    
    private static String getType(boolean isRepresentation) {
        return isRepresentation?"Representation":"Fault";
    }

    public void setWorkingSet(Set<T> elements) {
        this.elements = elements;
        setEnabled(elements != null && !elements.isEmpty());
    }

    public void actionPerformed(ActionEvent arg0) {
        if (elements.size() < 1) {
            return;
        }
        boolean singleSelection = elements.size() == 1;
        String beName = "";//+elements.size();
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(RemoveBodyElementAction.class,
                (singleSelection ? "MSG_BODYELEMENT_DELETE" : "MSG_BODYELEMENTS_DELETE"), 
                getType((elements.iterator().next() instanceof Representation)), //NOI18N
                beName));
        Object retVal = DialogDisplayer.getDefault().notify(desc);
        if (retVal == NotifyDescriptor.YES_OPTION) {
            final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RemoveBodyElementAction.class,
                    (singleSelection ? "MSG_RemoveBodyElement" : "MSG_RemoveBodyElements"), 
                    getType((elements.iterator().next() instanceof Representation)), //NOI18N
                    beName)); //NOI18N
//            Task task = new Task(new Runnable() {
//
//                public void run() {
                    handle.start();
                    try {
                        removeBodyElement(elements);
                    } catch (IOException e) {
                        handle.finish();
                        ErrorManager.getDefault().notify(e);
                    } finally {
                        handle.finish();
                    }
//                }
//            });
//            RequestProcessor.getDefault().post(task);
        }
    }

    private void removeBodyElement(Set<T> elements) throws IOException {
        try {
            model.startTransaction();
            for (WadlComponent elem : elements) {
                if (elem.getParent() instanceof Request && elem instanceof Representation) {
                    ((Request) elem.getParent()).removeRepresentation((Representation) elem);
                } else if (elem.getParent() instanceof Response) {
                    if (elem instanceof Representation) {
                        ((Response) elem.getParent()).removeRepresentation((Representation) elem);
                    } else if (elem instanceof Fault) {
                        ((Response) elem.getParent()).removeFault((Fault) elem);
                    }
                }
            }
        } finally {
            model.endTransaction();
        }

        PropertyChangeListener[] listeners = getPropertyChangeListeners();
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(new PropertyChangeEvent(model, REMOVE_BODYELEMENTS, elements, null));
        }
    }

    private WadlModel getModel() {
        return this.model;
    }
}
