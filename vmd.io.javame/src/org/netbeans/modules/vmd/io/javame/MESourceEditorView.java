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
package org.netbeans.modules.vmd.io.javame;

import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.openide.awt.UndoRedo;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;

import javax.swing.*;
import javax.swing.text.Document;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author David Kaspar
 */
public final class MESourceEditorView implements DataEditorView {

    private static final long serialVersionUID = -1;

    static final String VIEW_ID = "source"; // NOI18N

    private DataObjectContext context;
    private transient MECloneableEditor editor;
    private transient JComponent toolbar;

    MESourceEditorView (DataObjectContext context) {
        this.context = context;
        init ();
    }

    private void init () {
        MEDesignEditorSupport sup = (MEDesignEditorSupport) context.getCloneableEditorSupport ();
        editor = new MECloneableEditor (sup);
        sup.initializeCloneableEditor (editor);
    }

    public DataObjectContext getContext () {
        return context;
    }

    public Kind getKind () {
        return Kind.CODE;
    }

    public boolean canShowSideWindows () {
        return true;
    }

    public Collection<String> getTags () {
        return Collections.emptySet ();
    }

    public String preferredID () {
        return VIEW_ID;
    }

    public String getDisplayName () {
        return ProjectUtils.getSourceEditorViewDisplayName ();
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (MESourceEditorView.class);
    }

    public JComponent getVisualRepresentation () {
        return editor;
    }

    public JComponent getToolbarRepresentation () {
        if (toolbar == null) {
            JEditorPane pane = editor.getEditorPane ();
            if (pane != null) {
                Document doc = pane.getDocument ();
                if (doc instanceof NbDocument.CustomToolbar)
                    toolbar = ((NbDocument.CustomToolbar) doc).createToolbar (pane);
            }
            if (toolbar == null)
                toolbar = new JPanel ();
        }
        return toolbar;
    }

    public UndoRedo getUndoRedo () {
        return editor.getUndoRedo ();
    }

    public void componentOpened () {
        editor.componentOpened();
    }

    public void componentClosed () {
        editor.componentClosed();
    }

    public void componentShowing () {
        editor.componentShowing();
    }

    public void componentHidden () {
    }

    public void componentActivated () {
        editor.componentActivatedSuper();
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
        return - 1000;
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

    private static final class MECloneableEditor extends CloneableEditor {
        public MECloneableEditor(MEDesignEditorSupport s) {
            super(s);
        }

        final void componentActivatedSuper() {
            componentActivated();
        }

        @Override
        protected  void componentShowing() {
            super.componentShowing();
        }

        @Override
        protected void componentOpened() {
            super.componentOpened();
        }

        @Override
        protected void componentClosed() {
            super.componentClosed();
        }

    }
}
