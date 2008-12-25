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
package org.netbeans.modules.cnd.navigation.macroview;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansionProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.navigation.hierarchy.ContextUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.UserQuestionException;

public final class ShowMacroExpansionAction extends CookieAction {

    protected void performAction(Node[] activatedNodes) {
        Document mainDoc = getDocument(activatedNodes);
        if (mainDoc == null) {
            return;
        }
        CsmFile csmFile = CsmUtilities.getCsmFile(mainDoc, true);
        if (csmFile == null) {
            return;
        }

        // Init expanded context field

        FileObject expandedContextFile = createMemoryFile(CsmUtilities.getFile(mainDoc).getName());
        if(expandedContextFile == null) {
            return;
        }
        Document expandedContextDoc = openFileDocument(expandedContextFile);
        if(expandedContextDoc == null) {
            return;
        }
        expandedContextDoc.putProperty(Document.TitleProperty, mainDoc.getProperty(Document.TitleProperty));
        expandedContextDoc.putProperty(CsmFile.class, csmFile);
        expandedContextDoc.putProperty(FileObject.class, expandedContextFile);

        mainDoc.putProperty(Document.class, expandedContextDoc);
        expandedContextDoc.putProperty(Document.class, mainDoc);

        CsmMacroExpansionProvider mep = CsmMacroExpansionProvider.getDefault();
        CsmScope scope = ContextUtils.findScope(activatedNodes[0]);
        if (CsmKindUtilities.isOffsetable(scope)) {
            String expandedText = mep.getExpandedText(csmFile, ((CsmOffsetable) scope).getStartOffset(), ((CsmOffsetable) scope).getEndOffset());
            try {
                expandedContextDoc.insertString(0, expandedText, null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            String expandedText = mep.getExpandedText(csmFile, 0, mainDoc.getLength());
            try {
                expandedContextDoc.insertString(0, expandedText, null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }




        reformat(expandedContextDoc);
        saveFile(expandedContextFile);
//        lockFile(expandedContextFile);

        // Init expanded macro field

        FileObject expandedMacroFile = createMemoryFile(CsmUtilities.getFile(mainDoc).getName());
        if(expandedMacroFile == null) {
            return;
        }
        Document expandedMacroDoc = openFileDocument(expandedMacroFile);
        if(expandedMacroDoc == null) {
            return;
        }

        CsmDeclaration decl = ContextUtils.findDeclaration(activatedNodes[0]);
        if (decl != null) {
            try {
                expandedMacroDoc.insertString(0, decl.getName().toString(), null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        reformat(expandedMacroDoc);
        saveFile(expandedMacroFile);
        lockFile(expandedMacroFile);



        MacroExpansionTopComponent view = MacroExpansionTopComponent.findInstance();
        if (!view.isOpened()) {
            view.open();
        }
        view.setDocuments(expandedContextDoc, expandedMacroDoc);
        view.requestActive();
    }

    private FileObject createMemoryFile (String name) {
        FileObject fo = null;
        try {
//            File f = File.createTempFile("temp", name);
//            fo = FileUtil.createData(f);

            FileObject root = FileUtil.createMemoryFileSystem().getRoot();
//            FileObject folder = FileUtil.createFolder(root, "folder"); // NOI18N
            fo = FileUtil.createData(root, name);
//            FileObject file = FileUtil.copyFile(FileUtil.createData(CsmUtilities.getFile(doc)), root, CsmUtilities.getFile(doc).getName());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return fo;
    }

    private Document openFileDocument (FileObject fo) {
        Document doc = null;
        try {
            DataObject dob = DataObject.find(fo);
            EditorCookie ec = dob.getCookie(EditorCookie.class);
            try {
                doc = ec.openDocument();
            } catch (UserQuestionException ex) {
                ex.confirmed();
                doc = ec.openDocument();
            }
            if(doc != null) {
                doc.putProperty(Document.StreamDescriptionProperty, dob);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return doc;
    }

    private void saveFile (FileObject fo) {
        try {
            DataObject dob = DataObject.find(fo);
            EditorCookie ec = dob.getCookie(EditorCookie.class);
            ec.saveDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void lockFile (FileObject fo) {
        try {
            fo.lock();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Document getDocument(Node[] activatedNodes) {
        EditorCookie c = activatedNodes[0].getCookie(EditorCookie.class);
        if (c != null) {
            return c.getDocument();
        }
        return null;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            if (ContextUtils.USE_REFERENCE_RESOLVER) {
                CsmReference ref = ContextUtils.findReference(activatedNodes[0]);
                if (ref != null && CsmKindUtilities.isInclude(ref.getOwner())) {
                    return true;
                }
            }
            return ContextUtils.findFile(activatedNodes[0]) != null;
        }
        return false;
    }

    private void reformat(Document doc) {
        Reformat reformat = Reformat.get(doc);
        reformat.lock();
        try {
            try {
                reformat.reformat(0, doc.getLength());
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            reformat.unlock();
        }
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_ShowMacroExpansionAction"); // NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[]{
                    EditorCookie.class
                };
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

