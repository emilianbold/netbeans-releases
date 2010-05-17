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

import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.UndoRedo;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author David Kaspar
 */
public class DocumentEditorView implements DataEditorView {

    private static final long serialVersionUID = 3317521238376153199L;

    static final String DOCUMENT_ID = "document"; // NOI18N

    private DataObjectContext context;
    private transient DocumentController controller;

    public DocumentEditorView (DataObjectContext context) {
        this.context = context;
        init ();
    }

    private void init () {
        controller = new DocumentController (context);
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
        return DOCUMENT_ID;
    }

    public String getDisplayName () {
        return NbBundle.getMessage (DocumentEditorView.class, "TITLE_DocumentView"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (DocumentEditorView.class);
    }

    public JComponent getVisualRepresentation () {
        return controller.getVisualRepresentation ();
    }

    public JComponent getToolbarRepresentation () {
        return controller.getToolbarRepresentation ();
    }

    public UndoRedo getUndoRedo () {
        return null;
    }

    public void componentOpened () {
        controller.attach ();
    }

    public void componentClosed () {
        controller.deattach ();
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
        return 11;
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
