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
package org.netbeans.modules.cnd.navigation.macroview;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.support.ReadOnlySupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.navigation.hierarchy.ContextUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/**
 * Utility methods for Macro Expansion view.
 *
 * @author Nick Krasilnikov
 */
public final class MacroExpansionViewUtils {

    // Properties for macro expansion document
    /** Start offset of expansion */
    public final static String MACRO_EXPANSION_START_OFFSET = "macro-expansion-start-offset"; // NOI18N
    /** End offset of expansion */
    public final static String MACRO_EXPANSION_END_OFFSET = "macro-expansion-end-offset"; // NOI18N

    /**
     * Updates content of macro expansion panel on offset change.
     * 
     * @param newOffset - offset
     * @return true if something was changed during update
     */
    public static boolean updateView(int newOffset) {
        final MacroExpansionTopComponent view = MacroExpansionTopComponent.findInstance();
        boolean localContext = MacroExpansionTopComponent.isLocalContext();

        final Document expandedContextDoc = view.getExpandedContextDoc();
        if (expandedContextDoc == null) {
            return false;
        }
        final Document mainDoc = (Document) expandedContextDoc.getProperty(Document.class);
        if (mainDoc == null) {
            return false;
        }
        CsmFile csmFile = CsmUtilities.getCsmFile(mainDoc, true, false);
        if (csmFile == null) {
            return false;
        }

        // Get ofsets and check if update needed
        int startOffset = 0;
        int endOffset = mainDoc.getLength();
        if (localContext) {
            CsmScope scope = ContextUtils.findInnerFileScope(csmFile, newOffset);
            if (CsmKindUtilities.isOffsetable(scope)) {
                startOffset = ((CsmOffsetable) scope).getStartOffset();
                endOffset = ((CsmOffsetable) scope).getEndOffset();
            }
        }
        if (!isOffsetChanged(expandedContextDoc, startOffset, endOffset)) {
            return false;
        }
        
        // Init expanded context field
        final Document newExpandedContextDoc = createExpandedContextDocument(mainDoc, csmFile);
        if (newExpandedContextDoc == null) {
            return false;
        }
        final int expansionsNumber = CsmMacroExpansion.expand(mainDoc, startOffset, endOffset, newExpandedContextDoc);
        setOffset(newExpandedContextDoc, startOffset, endOffset);
        saveDocumentAndMarkAsReadOnly(newExpandedContextDoc);

        // Init expanded macro field
        final Document expandedMacroDoc = createExpandedMacroDocument(mainDoc, csmFile);
        if (expandedMacroDoc == null) {
            return false;
        }
        CsmDeclaration decl = ContextUtils.findInnerFileDeclaration(csmFile, newOffset);
        if (decl != null) {
            try {
                expandedMacroDoc.insertString(0, decl.getName().toString(), null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        saveDocumentAndMarkAsReadOnly(expandedMacroDoc);

        // Open view
        Runnable openView = new Runnable() {

            public void run() {
                if (!view.isOpened()) {
                    view.open();
                }
                view.setDocuments(newExpandedContextDoc, expandedMacroDoc);
                view.setStatusBarText(NbBundle.getMessage(MacroExpansionTopComponent.class, "CTL_MacroExpansionStatusBarLine", expansionsNumber)); // NOI18N
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            openView.run();
        } else {
            SwingUtilities.invokeLater(openView);
        }
        return true;
    }

    /**
     * Check match between old and new offsets of expansion.
     *
     * @param doc - document of macro expansion view
     * @param startOffset - new start offset
     * @param endOffset - new end offset
     * @return true in offsets have changed
     */
    private static boolean isOffsetChanged(Document doc, int startOffset, int endOffset) {
        int oldStartOffset = 0;
        int oldEndOffset = 0;
        Object obj = doc.getProperty(MACRO_EXPANSION_START_OFFSET);
        if (obj instanceof Integer) {
            oldStartOffset = (Integer) obj;
        }
        obj = doc.getProperty(MACRO_EXPANSION_END_OFFSET);
        if (obj instanceof Integer) {
            oldEndOffset = (Integer) obj;
        }
        return !((oldStartOffset == startOffset) && (oldEndOffset == endOffset));
    }

    /**
     * Sets expansion offsets.
     *
     * @param doc - document
     * @param startOffset - new start offset
     * @param endOffset - new end offset
     */
    public static void setOffset(Document doc, int startOffset, int endOffset) {
        doc.putProperty(MACRO_EXPANSION_START_OFFSET, Integer.valueOf(startOffset));
        doc.putProperty(MACRO_EXPANSION_END_OFFSET, Integer.valueOf(endOffset));
    }

    /**
     * Creates document for expanded context pane.
     *
     * @param mainDoc - original document
     * @param csmFile - file
     * @return document
     */
    public static Document createExpandedContextDocument(Document mainDoc, CsmFile csmFile) {
        FileObject fobj = createMemoryFile(CsmUtilities.getFile(mainDoc).getName());
        if (fobj == null) {
            return null;
        }
        Document doc = openFileDocument(fobj);
        if (doc == null) {
            return null;
        }

        doc.putProperty(Document.TitleProperty, mainDoc.getProperty(Document.TitleProperty));
        doc.putProperty(CsmFile.class, csmFile);
        doc.putProperty(FileObject.class, fobj);
        doc.putProperty("beforeSaveRunnable", null); // NOI18N
        doc.putProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT, true);

        mainDoc.putProperty(Document.class, doc);
        doc.putProperty(Document.class, mainDoc);

        setupMimeType(doc);

        return doc;
    }

    /**
     * Creates document for expanded macro pane.
     *
     * @param mainDoc - original document
     * @param csmFile - file
     * @return document
     */
    public static Document createExpandedMacroDocument(Document mainDoc, CsmFile csmFile) {
        FileObject fobj = createMemoryFile(CsmUtilities.getFile(mainDoc).getName());
        if (fobj == null) {
            return null;
        }
        final Document doc = openFileDocument(fobj);
        if (doc == null) {
            return null;
        }
        doc.putProperty(FileObject.class, fobj);
        setupMimeType(doc);
        return doc;
    }

    /**
     * Returns offset in original file.
     *
     * @param doc - document
     * @param documentOffset - offset in document
     * @return - offset in file
     */
    public static int getFileOffset(Document doc, int documentOffset) {
        return CsmMacroExpansion.getOffsetInOriginalText(doc, documentOffset);
    }

    /**
     * Returns offset in document.
     *
     * @param doc - document
     * @param fileOffset - offset in original file
     * @return - offset in document
     */
    public static int getDocumentOffset(Document doc, int fileOffset) {
        return CsmMacroExpansion.getOffsetInExpandedText(doc, fileOffset);
    }

    /**
     * Finds editor pane of document.
     *
     * @param doc - document
     * @return editor pane
     */
    public static JEditorPane getEditor(Document doc) {
        Object jEditorPane = doc.getProperty(JEditorPane.class);
        if (jEditorPane != null) {
            return (JEditorPane) jEditorPane;
        }
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        if (dobj != null) {
            EditorCookie ec = dobj.getCookie(EditorCookie.class);
            return ec == null ? null : CsmUtilities.findRecentEditorPaneInEQ(ec);
        }
        return null;
    }

    /**
     * Sets mime type.
     *
     * @param doc - document
     */
    public static void setupMimeType(Document doc) {
        Object mimeTypeObj = doc.getProperty(NbEditorDocument.MIME_TYPE_PROP);
        if (mimeTypeObj != null) {
            if ("text/plain".equals(mimeTypeObj)) { // NOI18N
                doc.putProperty(NbEditorDocument.MIME_TYPE_PROP, MIMENames.CPLUSPLUS_MIME_TYPE);
            }
        }
    }

    /**
     * Creates file in memory.
     *
     * @param name - file name
     * @return file
     */
    public static FileObject createMemoryFile(String name) {
        FileObject fo = null;
        try {
            FileObject root = FileUtil.createMemoryFileSystem().getRoot();
            fo = FileUtil.createData(root, name);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return fo;
    }

    /**
     * Opens document for file object.
     *
     * @param fo - file object
     * @return document
     */
    public static Document openFileDocument(FileObject fo) {
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
            if (doc != null) {
                doc.putProperty(Document.StreamDescriptionProperty, dob);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return doc;
    }

    /**
     * Saves document.
     *
     * @param doc - document
     */
    public static void saveDocumentAndMarkAsReadOnly(Document doc) {
        FileObject fo = CsmUtilities.getFileObject(doc);
        if (fo != null) {
            saveFileAndMarkAsReadOnly(fo);
        }
    }

    /**
     * Saves file.
     *
     * @param fo - file object
     */
    private static void saveFileAndMarkAsReadOnly(FileObject fo) {
        try {
            DataObject dob = DataObject.find(fo);
            EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            ec.saveDocument();
            ReadOnlySupport ro = dob.getLookup().lookup(ReadOnlySupport.class);
            if (ro != null) {
                ro.setReadOnly(true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Locks file.
     *
     * @param fo - file object
     */
    public static void lockFile(FileObject fo) {
        try {
            fo.lock();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
