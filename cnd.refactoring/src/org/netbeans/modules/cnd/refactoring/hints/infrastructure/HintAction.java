/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.hints.infrastructure;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.modules.cnd.refactoring.support.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.text.CloneableEditorSupport;

/**
 * based on org.netbeans.modules.java.hints.infrastructure.HintAction
 * @author Vladimir Voskresensky
 */
public abstract class HintAction extends TextAction implements PropertyChangeListener {

    protected HintAction() {
        super(null);
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N

        TopComponent.getRegistry().addPropertyChangeListener(WeakListeners.propertyChange(this, TopComponent.getRegistry()));
    }

    private void updateEnabled() {
        if (!CsmRefactoringUtils.REFACTORING_EXTRA) {
            return;
        }
        setEnabled(getCurrentDocument(new int[] {0,0,0}) != null);
    }

    @Override
    public boolean isEnabled() {
        if (!CsmRefactoringUtils.REFACTORING_EXTRA) {
            return false;
        }
        updateEnabled();
        return super.isEnabled();
    }

    public void actionPerformed(ActionEvent e) {
        if (!CsmRefactoringUtils.REFACTORING_EXTRA) {
            return;
        }
        String error = doPerform();

        if (error != null) {
            String errorText = NbBundle.getMessage(HintAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);

            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    private String doPerform() {
        int[] span = new int[3];
        Document doc = getCurrentDocument(span);

        if (doc == null) {
            if (span[0] != span[1]) {
                return "ERR_Not_Selected"; //NOI18N
            } else {
                return "ERR_No_Selection"; //NOI18N
            }
        }

        CsmContext editorContext = CsmContext.create(doc, span[0], span[1], span[2]);
        if (editorContext == null) {
            return "ERR_Not_Supported"; //NOI18N
        }
        perform(editorContext);

        return null;
    }

    protected abstract void perform(CsmContext context);

    private Document getCurrentDocument(int[] span) {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        JTextComponent pane = null;

        //XXX check if inside AWT?
        if (SwingUtilities.isEventDispatchThread() && (tc instanceof CloneableEditorSupport.Pane)) {
            pane = ((CloneableEditorSupport.Pane) tc).getEditorPane();
        }

        if (pane == null) {
            return null;
        }
        if (span != null) {
            span[0] = pane.getSelectionStart();
            span[1] = pane.getSelectionEnd();
            span[2] = pane.getCaretPosition();
            if (span[0] == span[1] && requiresSelection()) {
                return null;
            }
        }

        Document doc = pane.getDocument();
        Object stream = doc != null ? doc.getProperty(Document.StreamDescriptionProperty) : null;

        if (!(stream instanceof DataObject)) {
            return null;
        }

        DataObject dObj = (DataObject) stream;
        FileObject result = dObj.getPrimaryFile();

        if (MIMENames.isHeaderOrCppOrC(FileUtil.getMIMEType(result))) {
            return doc;
        } else {
            return null;
        }
    }

    protected boolean requiresSelection() {
        return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        updateEnabled();
    }
}
