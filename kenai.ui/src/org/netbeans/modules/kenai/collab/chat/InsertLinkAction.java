/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.collab.chat;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor.IssueHandle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * 
 */
public class InsertLinkAction extends AbstractAction {

    private String outText;
    private JTextPane out;

    static InsertLinkAction create (IssueHandle issueHandle, JTextPane outbox, boolean insertAccelerator) {
        return new InsertLinkAction(issueHandle, outbox, insertAccelerator);
    }
        
    static InsertLinkAction create(JTextComponent component, JTextPane out, boolean insertLineNumber, boolean insertAccelerator) {
        assert component != null;
        Document document = component.getDocument();
        FileObject fo = NbEditorUtilities.getFileObject(document);
        if(fo == null) {
            return null;
        }
        int line = NbDocument.findLineNumber((StyledDocument) document, component.getCaretPosition()) + 1;
        return new InsertLinkAction(fo, line, out, insertLineNumber, insertAccelerator);
    }
    
    private InsertLinkAction(FileObject fo, int line, JTextPane out, boolean insertLineNumber, boolean insertAccelerator) {
        super();
        
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (insertLineNumber) {
            putValue(NAME, fo.getNameExt() + ":" + line); // NOI18N
            if (insertAccelerator)
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        } else {
            putValue(NAME, fo.getNameExt());
        }
        if (cp != null) {
            outText = cp.getResourceName(fo);
        } else {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null) {
                outText = "{$" + ProjectUtils.getInformation(p).getName() + "}/" + FileUtil.getRelativePath(p.getProjectDirectory(), fo); //NOI18N
                } else {
                outText = fo.getPath();
            }
        }
        if (insertLineNumber)
            outText += ":" + line; //NOI18N
        this.out = out;
        outText =  "FILE:" + outText; // NOI18N
    }

    private InsertLinkAction (IssueHandle issueHandle, JTextPane outbox, boolean insertAccelerator) {
        putValue(NAME, issueHandle.getShortDisplayName());
        if (insertAccelerator)
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));

        this.out=outbox;
        outText = "ISSUE:" + issueHandle.getID(); // NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        try {
            out.getDocument().insertString(out.getCaretPosition(),outText, null);
            out.requestFocus();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}

