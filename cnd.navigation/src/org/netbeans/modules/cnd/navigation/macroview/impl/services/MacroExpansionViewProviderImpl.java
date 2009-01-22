/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.navigation.macroview.impl.services;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.navigation.hierarchy.ContextUtils;
import org.netbeans.modules.cnd.navigation.macroview.MacroExpansionTopComponent;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionViewProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.UserQuestionException;

/**
 * Service that provides UI for macro expansion
 *
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionViewProvider.class)
public class MacroExpansionViewProviderImpl implements CsmMacroExpansionViewProvider {

    /**
     * Expands document on specified position and shows Macro Expansion View panel.
     *
     * @param doc - document
     * @param offset - offset in document
     */
    public void showMacroExpansionView(Document doc, int offset) {
        Document mainDoc = doc;
        if (mainDoc == null) {
            return;
        }
        CsmFile csmFile = CsmUtilities.getCsmFile(mainDoc, true);
        if (csmFile == null) {
            return;
        }

        MacroExpansionTopComponent view = MacroExpansionTopComponent.findInstance();
        boolean localContext = view.isLocalContext();

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
        expandedContextDoc.putProperty("beforeSaveRunnable", null); // NOI18N
        expandedContextDoc.putProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT, true);

        mainDoc.putProperty(Document.class, expandedContextDoc);
        expandedContextDoc.putProperty(Document.class, mainDoc);
        setupMimeType(expandedContextDoc);

        CsmScope scope = ContextUtils.findInnerFileScope(csmFile, offset);
        if (localContext && CsmKindUtilities.isOffsetable(scope)) {
            CsmMacroExpansion.expand(mainDoc, ((CsmOffsetable) scope).getStartOffset(), ((CsmOffsetable) scope).getEndOffset(), expandedContextDoc);
        } else {
            CsmMacroExpansion.expand(mainDoc, 0, mainDoc.getLength(), expandedContextDoc);
        }

        saveFile(expandedContextFile);
        lockFile(expandedContextFile);

        // Init expanded macro field

        FileObject expandedMacroFile = createMemoryFile(CsmUtilities.getFile(mainDoc).getName());
        if(expandedMacroFile == null) {
            return;
        }
        Document expandedMacroDoc = openFileDocument(expandedMacroFile);
        if(expandedMacroDoc == null) {
            return;
        }
        setupMimeType(expandedMacroDoc);

        CsmDeclaration decl = ContextUtils.findInnerFileDeclaration(csmFile, offset);
        if (decl != null) {
            try {
                expandedMacroDoc.insertString(0, decl.getName().toString(), null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        saveFile(expandedMacroFile);
        lockFile(expandedMacroFile);

        // Open view

        if (!view.isOpened()) {
            view.open();
        }
        view.setDocuments(expandedContextDoc, expandedMacroDoc);
        view.requestActive();

        view.setDisplayName("Macro Expansion of " + CsmUtilities.getFile(mainDoc).getName()); // NOI18N
    }

    private void setupMimeType(Document doc){
        Object mimeTypeObj = doc.getProperty(NbEditorDocument.MIME_TYPE_PROP);
        if (mimeTypeObj != null) {
            if ("text/plain".equals(mimeTypeObj)){ // NOI18N
                doc.putProperty(NbEditorDocument.MIME_TYPE_PROP, MIMENames.CPLUSPLUS_MIME_TYPE);
            }
        }
    }

    private FileObject createMemoryFile (String name) {
        FileObject fo = null;
        try {
            FileObject root = FileUtil.createMemoryFileSystem().getRoot();
            fo = FileUtil.createData(root, name);
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
}


