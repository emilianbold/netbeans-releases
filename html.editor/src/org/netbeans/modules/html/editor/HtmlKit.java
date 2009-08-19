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
package org.netbeans.modules.html.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.im.InputContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPasswordField;
import javax.swing.TransferHandler;
import javax.swing.plaf.UIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Position;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.*;
import org.netbeans.editor.BaseKit.DeleteCharAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.core.SelectCodeElementAction;
import org.netbeans.modules.csl.editor.InstantRenameAction;
import org.netbeans.modules.csl.editor.ToggleBlockCommentAction;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.html.editor.gsf.HtmlCommentHandler;
import org.openide.util.Exceptions;

/**
 * Editor kit implementation for HTML content type
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public class HtmlKit extends NbEditorKit implements org.openide.util.HelpCtx.Provider {

    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(HtmlKit.class);
    }
    private static final Logger LOGGER = Logger.getLogger(HtmlKit.class.getName());
    static final long serialVersionUID = -1381945567613910297L;
    public static final String HTML_MIME_TYPE = "text/html"; // NOI18N
    public static final String shiftInsertBreakAction = "shift-insert-break"; // NOI18N

    public HtmlKit() {
        this(HTML_MIME_TYPE);
        NbReaderProvider.setupReaders();
    }

    public HtmlKit(String mimeType) {
        super();
    }

    @Override
    public String getContentType() {
        return HTML_MIME_TYPE;
    }

    @Override
    public Object clone() {
        return new HtmlKit();
    }

    @Override
    protected void initDocument(final BaseDocument doc) {
        TokenHierarchy hi = TokenHierarchy.get(doc);
        if (hi == null) {
            LOGGER.log(Level.WARNING, "TokenHierarchy is null for document " + doc);
            return;
        }

        //listen on the HTML parser and recolor after changes
//        LanguagePath htmlLP = LanguagePath.get(HTMLTokenId.language());
//        SyntaxParser.get(doc, htmlLP).addSyntaxParserListener(new EmbeddingUpdater(doc));
    }

    /** Called after the kit is installed into JEditorPane */
    @Override
    public void install(javax.swing.JEditorPane c) {
        super.install(c);
        c.setTransferHandler(new HtmlTransferHandler());
        NbReaderProvider.setupReaders();
    }
    
    protected DeleteCharAction createDeletePrevAction() {
        return new HtmlDeleteCharAction(deletePrevCharAction, false);
    }
    
    protected ExtDefaultKeyTypedAction createDefaultKeyTypedAction() {
        return new HtmlDefaultKeyTypedAction();
    }

    protected InsertBreakAction createInsertBreakAction() {
        return new HtmlInsertBreakAction();
    }
    
    @Override
    protected Action[] createActions() {
        Action[] HtmlActions = new Action[]{
            createInsertBreakAction(),
            createDefaultKeyTypedAction(),
            createDeletePrevAction(),
            new HtmlDeleteCharAction(deleteNextCharAction, true),
            new SelectCodeElementAction(SelectCodeElementAction.selectNextElementAction, true),
            new SelectCodeElementAction(SelectCodeElementAction.selectPreviousElementAction, false),
            new InstantRenameAction(),
            new ToggleBlockCommentAction(new HtmlCommentHandler()),
            new ExtKit.CommentAction(""), //NOI18N
            new ExtKit.UncommentAction("") //NOI18N
        };
        return TextAction.augmentList(super.createActions(), HtmlActions);
    }

    static KeystrokeHandler getBracketCompletion(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument) doc;
        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, offset);
        for (Language l : list) {
            if (l.getBracketCompletion() != null) {
                return l.getBracketCompletion();
            }
        }

        return null;
    }

    /**
     * Returns true if bracket completion is enabled in options.
     */
    private static boolean completionSettingEnabled() {
        //return ((Boolean)Settings.getValue(HTMLEditorKit.class, JavaSettingsNames.PAIR_CHARACTERS_COMPLETION)).booleanValue();
        return true;
    }

    public class HtmlInsertBreakAction extends InsertBreakAction {

        static final long serialVersionUID = -1506173310438326380L;

        @Override
        protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) {
            if (completionSettingEnabled()) {
                KeystrokeHandler bracketCompletion = getBracketCompletion(doc, caret.getDot());

                if (bracketCompletion != null) {
                    try {
                        int newOffset = bracketCompletion.beforeBreak(doc, caret.getDot(), target);

                        if (newOffset >= 0) {
                            return new Integer(newOffset);
                        }
                    } catch (BadLocationException ble) {
                        Exceptions.printStackTrace(ble);
                    }
                }
            }

            // return Boolean.TRUE;
            return null;
        }

        @Override
        protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret,
                Object cookie) {
            if (completionSettingEnabled()) {
                if (cookie != null) {
                    if (cookie instanceof Integer) {
                        // integer
                        int dotPos = ((Integer) cookie).intValue();
                        if (dotPos != -1) {
                            caret.setDot(dotPos);
                        } else {
                            int nowDotPos = caret.getDot();
                            caret.setDot(nowDotPos + 1);
                        }
                    }
                }
            }
        }
    }

    public static class HtmlDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {

        private JTextComponent currentTarget;

        @Override
        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                currentTarget = target;
                BaseDocument doc = (BaseDocument) target.getDocument();
                final Indent indent = Indent.get(doc);
                indent.lock();
                try {
                    doc.runAtomic(new Runnable() {
                        public void run() {
                            HtmlDefaultKeyTypedAction.super.actionPerformed(evt, target);
                        }
                    });
                } finally {
                    indent.unlock();
                }
                currentTarget = null;
            } else {
                //backw comp.
                super.actionPerformed(evt, target);
            }
        }

        @Override
        protected void insertString(BaseDocument doc, int dotPos,
                Caret caret, String str,
                boolean overwrite) throws BadLocationException {

            if (completionSettingEnabled()) {
                KeystrokeHandler bracketCompletion = getBracketCompletion(doc, dotPos);

                if (bracketCompletion != null) {
                    // TODO - check if we're in a comment etc. and if so, do nothing
                    boolean handled =
                            bracketCompletion.beforeCharInserted(doc, dotPos, currentTarget,
                            str.charAt(0));

                    if (!handled) {
                        super.insertString(doc, dotPos, caret, str, overwrite);
                        handled = bracketCompletion.afterCharInserted(doc, dotPos, currentTarget,
                                str.charAt(0));
                    }

                    return;
                }
            }

            super.insertString(doc, dotPos, caret, str, overwrite);
        }

        @Override
        protected void replaceSelection(JTextComponent target, int dotPos, Caret caret,
                String str, boolean overwrite) throws BadLocationException {
            char insertedChar = str.charAt(0);
            Document document = target.getDocument();

            if (document instanceof BaseDocument) {
                BaseDocument doc = (BaseDocument) document;

                if (completionSettingEnabled()) {
                    KeystrokeHandler bracketCompletion = getBracketCompletion(doc, dotPos);

                    if (bracketCompletion != null) {
                        try {
                            int caretPosition = caret.getDot();

                            boolean handled =
                                    bracketCompletion.beforeCharInserted(doc, caretPosition,
                                    target, insertedChar);

                            int p0 = Math.min(caret.getDot(), caret.getMark());
                            int p1 = Math.max(caret.getDot(), caret.getMark());

                            if (p0 != p1) {
                                doc.remove(p0, p1 - p0);
                            }

                            if (!handled) {
                                if ((str != null) && (str.length() > 0)) {
                                    doc.insertString(p0, str, null);
                                }

                                bracketCompletion.afterCharInserted(doc, caret.getDot() - 1,
                                        target, insertedChar);
                            }
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }

                        return;
                    }
                }
            }

            super.replaceSelection(target, dotPos, caret, str, overwrite);
        }

        private void handleTagClosingSymbol(final BaseDocument doc, final int dotPos, final char lastChar) throws BadLocationException {
            if (lastChar == '>') {
                TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
                for (final LanguagePath languagePath : (Set<LanguagePath>) tokenHierarchy.languagePaths()) {
                    if (languagePath.innerLanguage() == HTMLTokenId.language()) {
                        final Indent indent = Indent.get(doc);
                        indent.lock();
                        try {
                            doc.runAtomic(new Runnable() {

                                public void run() {
                                    try {
                                        int startOffset = Utilities.getRowStart(doc, dotPos);
                                        int endOffset = Utilities.getRowEnd(doc, dotPos);
                                        indent.reindent(startOffset, endOffset);
                                    } catch (BadLocationException ex) {
                                        //ignore
                                    }
                                }
                            });
                        } finally {
                            indent.unlock();
                        }
                    }
                }
            }
        }

    }

    public static class HtmlDeleteCharAction extends DeleteCharAction {

        private JTextComponent currentTarget;
        
        public HtmlDeleteCharAction(String name, boolean nextChar) {
            super(name, nextChar);
        }
        
        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            try {
                currentTarget = target;
                super.actionPerformed(evt, target);
            } finally {
                currentTarget = null;
            }
        }

        @Override
        protected void charBackspaced(BaseDocument doc, int dotPos, Caret caret, char ch) throws BadLocationException {
              if (completionSettingEnabled()) {
                KeystrokeHandler bracketCompletion = getBracketCompletion(doc, dotPos);

                if (bracketCompletion != null) {
                    boolean success = bracketCompletion.charBackspaced(doc, dotPos, currentTarget, ch);
                    return;
                }
            }
            
            
            super.charBackspaced(doc, dotPos, caret, ch);
        }

        public boolean getNextChar() {
            return nextChar;
        }
    }

    /* !!!!!!!!!!!!!!!!!!!!!
     *
     * Inner classes bellow were taken from BasicTextUI and rewritten in the place marked
     * with [REWRITE_PLACE]. This needs to be done to fix the issue #43309
     *
     * !!!!!!!!!!!!!!!!!!!!!
     */
    static class HtmlTransferHandler extends TransferHandler implements UIResource {

        private JTextComponent exportComp;
        private boolean shouldRemove;
        private int p0;
        private int p1;

        /**
         * Try to find a flavor that can be used to import a Transferable.
         * The set of usable flavors are tried in the following order:
         * <ol>
         *     <li>First, an attempt is made to find a flavor matching the content type
         *         of the EditorKit for the component.
         *     <li>Second, an attempt to find a text/plain flavor is made.
         *     <li>Third, an attempt to find a flavor representing a String reference
         *         in the same VM is made.
         *     <li>Lastly, DataFlavor.stringFlavor is searched for.
         * </ol>
         */
        protected DataFlavor getImportFlavor(DataFlavor[] flavors, JTextComponent c) {
            DataFlavor plainFlavor = null;
            DataFlavor refFlavor = null;
            DataFlavor stringFlavor = null;

            if (c instanceof JEditorPane) {
                for (int i = 0; i < flavors.length; i++) {
                    String mime = flavors[i].getMimeType();
                    if (mime.startsWith(((JEditorPane) c).getEditorKit().getContentType())) {
                        //return flavors[i]; [REWRITE_PLACE]
                    } else if (plainFlavor == null && mime.startsWith("text/plain")) { //NOI18N
                        plainFlavor = flavors[i];
                    } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref") //NOI18N
                            && flavors[i].getRepresentationClass() == java.lang.String.class) {
                        refFlavor = flavors[i];
                    } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                        stringFlavor = flavors[i];
                    }
                }
                if (plainFlavor != null) {
                    return plainFlavor;
                } else if (refFlavor != null) {
                    return refFlavor;
                } else if (stringFlavor != null) {
                    return stringFlavor;
                }
                return null;
            }


            for (int i = 0; i < flavors.length; i++) {
                String mime = flavors[i].getMimeType();
                if (mime.startsWith("text/plain")) { //NOI18N
                    return flavors[i];
                } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref") //NOI18N
                        && flavors[i].getRepresentationClass() == java.lang.String.class) {
                    refFlavor = flavors[i];
                } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                    stringFlavor = flavors[i];
                }
            }
            if (refFlavor != null) {
                return refFlavor;
            } else if (stringFlavor != null) {
                return stringFlavor;
            }
            return null;
        }

        /**
         * Import the given stream data into the text component.
         */
        protected void handleReaderImport(Reader in, JTextComponent c, boolean useRead)
                throws BadLocationException, IOException {
            if (useRead) {
                int startPosition = c.getSelectionStart();
                int endPosition = c.getSelectionEnd();
                int length = endPosition - startPosition;
                EditorKit kit = c.getUI().getEditorKit(c);
                Document doc = c.getDocument();
                if (length > 0) {
                    doc.remove(startPosition, length);
                }
                kit.read(in, doc, startPosition);
            } else {
                char[] buff = new char[1024];
                int nch;
                boolean lastWasCR = false;
                int last;
                StringBuffer sbuff = null;

                // Read in a block at a time, mapping \r\n to \n, as well as single
                // \r to \n.
                while ((nch = in.read(buff, 0, buff.length)) != -1) {
                    if (sbuff == null) {
                        sbuff = new StringBuffer(nch);
                    }
                    last = 0;
                    for (int counter = 0; counter < nch; counter++) {
                        switch (buff[counter]) {
                            case '\r':
                                if (lastWasCR) {
                                    if (counter == 0) {
                                        sbuff.append('\n');
                                    } else {
                                        buff[counter - 1] = '\n';
                                    }
                                } else {
                                    lastWasCR = true;
                                }
                                break;
                            case '\n':
                                if (lastWasCR) {
                                    if (counter > (last + 1)) {
                                        sbuff.append(buff, last, counter - last - 1);
                                    }
                                    // else nothing to do, can skip \r, next write will
                                    // write \n
                                    lastWasCR = false;
                                    last = counter;
                                }
                                break;
                            default:
                                if (lastWasCR) {
                                    if (counter == 0) {
                                        sbuff.append('\n');
                                    } else {
                                        buff[counter - 1] = '\n';
                                    }
                                    lastWasCR = false;
                                }
                                break;
                        }
                    }
                    if (last < nch) {
                        if (lastWasCR) {
                            if (last < (nch - 1)) {
                                sbuff.append(buff, last, nch - last - 1);
                            }
                        } else {
                            sbuff.append(buff, last, nch - last);
                        }
                    }
                }
                if (lastWasCR) {
                    sbuff.append('\n');
                }
                c.replaceSelection(sbuff != null ? sbuff.toString() : ""); //NOI18N
            }
        }

        // --- TransferHandler methods ------------------------------------
        /**
         * This is the type of transfer actions supported by the source.  Some models are
         * not mutable, so a transfer operation of COPY only should
         * be advertised in that case.
         *
         * @param c  The component holding the data to be transfered.  This
         *  argument is provided to enable sharing of TransferHandlers by
         *  multiple components.
         * @return  This is implemented to return NONE if the component is a JPasswordField
         *  since exporting data via user gestures is not allowed.  If the text component is
         *  editable, COPY_OR_MOVE is returned, otherwise just COPY is allowed.
         */
        @Override
        public int getSourceActions(JComponent c) {
            int actions = NONE;
            if (!(c instanceof JPasswordField)) {
                if (((JTextComponent) c).isEditable()) {
                    actions = COPY_OR_MOVE;
                } else {
                    actions = COPY;
                }
            }
            return actions;
        }

        /**
         * Create a Transferable to use as the source for a data transfer.
         *
         * @param comp  The component holding the data to be transfered.  This
         *  argument is provided to enable sharing of TransferHandlers by
         *  multiple components.
         * @return  The representation of the data to be transfered.
         *
         */
        @Override
        protected Transferable createTransferable(JComponent comp) {
            exportComp = (JTextComponent) comp;
            shouldRemove = true;
            p0 = exportComp.getSelectionStart();
            p1 = exportComp.getSelectionEnd();
            return (p0 != p1) ? (new HtmlTransferable(exportComp, p0, p1)) : null;
        }

        /**
         * This method is called after data has been exported.  This method should remove
         * the data that was transfered if the action was MOVE.
         *
         * @param source The component that was the source of the data.
         * @param data   The data that was transferred or possibly null
         *               if the action is <code>NONE</code>.
         * @param action The actual action that was performed.
         */
        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            // only remove the text if shouldRemove has not been set to
            // false by importData and only if the action is a move
            if (shouldRemove && action == MOVE) {
                HtmlTransferable t = (HtmlTransferable) data;
                t.removeText();
            }

            exportComp = null;
        }

        /**
         * This method causes a transfer to a component from a clipboard or a
         * DND drop operation.  The Transferable represents the data to be
         * imported into the component.
         *
         * @param comp  The component to receive the transfer.  This
         *  argument is provided to enable sharing of TransferHandlers by
         *  multiple components.
         * @param t     The data to import
         * @return  true if the data was inserted into the component, false otherwise.
         */
        @Override
        public boolean importData(JComponent comp, Transferable t) {
            JTextComponent c = (JTextComponent) comp;

            // if we are importing to the same component that we exported from
            // then don't actually do anything if the drop location is inside
            // the drag location and set shouldRemove to false so that exportDone
            // knows not to remove any data
            if (c == exportComp && c.getCaretPosition() >= p0 && c.getCaretPosition() <= p1) {
                shouldRemove = false;
                return true;
            }

            boolean imported = false;
            DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors(), c);
            if (importFlavor != null) {
                try {
                    boolean useRead = false;
                    if (comp instanceof JEditorPane) {
                        JEditorPane ep = (JEditorPane) comp;
                        if (!ep.getContentType().startsWith("text/plain") && //NOI18N
                                importFlavor.getMimeType().startsWith(ep.getContentType())) {
                            useRead = true;
                        }
                    }
                    InputContext ic = c.getInputContext();
                    if (ic != null) {
                        ic.endComposition();
                    }
                    Reader r = importFlavor.getReaderForText(t);
                    handleReaderImport(r, c, useRead);
                    imported = true;
                } catch (UnsupportedFlavorException ufe) {
                    //just ignore
                } catch (BadLocationException ble) {
                    //just ignore
                } catch (IOException ioe) {
                    //just ignore
                }
            }
            return imported;
        }

        /**
         * This method indicates if a component would accept an import of the given
         * set of data flavors prior to actually attempting to import it.
         *
         * @param comp  The component to receive the transfer.  This
         *  argument is provided to enable sharing of TransferHandlers by
         *  multiple components.
         * @param flavors  The data formats available
         * @return  true if the data can be inserted into the component, false otherwise.
         */
        @Override
        public boolean canImport(JComponent comp, DataFlavor[] flavors) {
            JTextComponent c = (JTextComponent) comp;
            if (!(c.isEditable() && c.isEnabled())) {
                return false;
            }
            return (getImportFlavor(flavors, c) != null);
        }

        /**
         * A possible implementation of the Transferable interface
         * for text components.  For a JEditorPane with a rich set
         * of EditorKit implementations, conversions could be made
         * giving a wider set of formats.  This is implemented to
         * offer up only the active content type and text/plain
         * (if that is not the active format) since that can be
         * extracted from other formats.
         */
        static class HtmlTransferable extends BasicTransferable {

            HtmlTransferable(JTextComponent c, int start, int end) {
                super(null, null);

                this.c = c;

                Document doc = c.getDocument();

                try {
                    p0 = doc.createPosition(start);
                    p1 = doc.createPosition(end);

                    plainData = c.getSelectedText();

                    if (c instanceof JEditorPane) {
                        JEditorPane ep = (JEditorPane) c;

                        mimeType = ep.getContentType();

                        if (mimeType.startsWith("text/plain")) { //NOI18N
                            return;
                        }

                        StringWriter sw = new StringWriter(p1.getOffset() - p0.getOffset());
                        ep.getEditorKit().write(sw, doc, p0.getOffset(), p1.getOffset() - p0.getOffset());

                        if (mimeType.startsWith("text/html")) { //NOI18N
                            htmlData = sw.toString();
                        } else {
                            richText = sw.toString();
                        }
                    }
                } catch (BadLocationException ble) {
                } catch (IOException ioe) {
                }
            }

            void removeText() {
                if ((p0 != null) && (p1 != null) && (p0.getOffset() != p1.getOffset())) {
                    try {
                        Document doc = c.getDocument();
                        doc.remove(p0.getOffset(), p1.getOffset() - p0.getOffset());
                    } catch (BadLocationException e) {
                    }
                }
            }

            // ---- EditorKit other than plain or HTML text -----------------------
            /**
             * If the EditorKit is not for text/plain or text/html, that format
             * is supported through the "richer flavors" part of BasicTransferable.
             */
            @Override
            protected DataFlavor[] getRicherFlavors() {
                if (richText == null) {
                    return null;
                }

                try {
                    DataFlavor[] flavors = new DataFlavor[3];
                    flavors[0] = new DataFlavor(mimeType + ";class=java.lang.String"); //NOI18N
                    flavors[1] = new DataFlavor(mimeType + ";class=java.io.Reader"); //NOI18N
                    flavors[2] = new DataFlavor(mimeType + ";class=java.io.InputStream;charset=unicode"); //NOI18N
                    return flavors;
                } catch (ClassNotFoundException cle) {
                    // fall through to unsupported (should not happen)
                }

                return null;
            }

            /**
             * The only richer format supported is the file list flavor
             */
            @Override
            protected Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (richText == null) {
                    return null;
                }

                if (String.class.equals(flavor.getRepresentationClass())) {
                    return richText;
                } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(richText);
                } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new ByteArrayInputStream(richText.getBytes());
                }
                throw new UnsupportedFlavorException(flavor);
            }
            Position p0;
            Position p1;
            String mimeType;
            String richText;
            JTextComponent c;
        }
    }

    private static class BasicTransferable implements Transferable, UIResource {

        protected String plainData;
        protected String htmlData;
        private static DataFlavor[] htmlFlavors;
        private static DataFlavor[] stringFlavors;
        private static DataFlavor[] plainFlavors;
        

        static {
            try {
                htmlFlavors = new DataFlavor[3];
                htmlFlavors[0] = new DataFlavor("text/html;class=java.lang.String"); //NOI18N
                htmlFlavors[1] = new DataFlavor("text/html;class=java.io.Reader"); //NOI18N
                htmlFlavors[2] = new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"); //NOI18N

                plainFlavors = new DataFlavor[3];
                plainFlavors[0] = new DataFlavor("text/plain;class=java.lang.String"); //NOI18N
                plainFlavors[1] = new DataFlavor("text/plain;class=java.io.Reader"); //NOI18N
                plainFlavors[2] = new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream"); //NOI18N

                stringFlavors = new DataFlavor[2];
                stringFlavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String"); //NOI18N
                stringFlavors[1] = DataFlavor.stringFlavor;

            } catch (ClassNotFoundException cle) {
                System.err.println("error initializing javax.swing.plaf.basic.BasicTranserable"); ////NOI18N
            }
        }

        public BasicTransferable(String plainData, String htmlData) {
            this.plainData = plainData;
            this.htmlData = htmlData;
        }

        /**
         * Returns an array of DataFlavor objects indicating the flavors the data
         * can be provided in.  The array should be ordered according to preference
         * for providing the data (from most richly descriptive to least descriptive).
         * @return an array of data flavors in which this data can be transferred
         */
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] richerFlavors = getRicherFlavors();
            int nRicher = (richerFlavors != null) ? richerFlavors.length : 0;
            int nHTML = (isHTMLSupported()) ? htmlFlavors.length : 0;
            int nPlain = (isPlainSupported()) ? plainFlavors.length : 0;
            int nString = (isPlainSupported()) ? stringFlavors.length : 0;
            int nFlavors = nRicher + nHTML + nPlain + nString;
            DataFlavor[] flavors = new DataFlavor[nFlavors];

            // fill in the array
            int nDone = 0;
            if (nRicher > 0) {
                System.arraycopy(richerFlavors, 0, flavors, nDone, nRicher);
                nDone += nRicher;
            }
            if (nHTML > 0) {
                System.arraycopy(htmlFlavors, 0, flavors, nDone, nHTML);
                nDone += nHTML;
            }
            if (nPlain > 0) {
                System.arraycopy(plainFlavors, 0, flavors, nDone, nPlain);
                nDone += nPlain;
            }
            if (nString > 0) {
                System.arraycopy(stringFlavors, 0, flavors, nDone, nString);
                nDone += nString;
            }
            return flavors;
        }

        /**
         * Returns whether or not the specified data flavor is supported for
         * this object.
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns an object which represents the data to be transferred.  The class
         * of the object returned is defined by the representation class of the flavor.
         *
         * @param flavor the requested flavor for the data
         * @see DataFlavor#getRepresentationClass
         * @exception IOException                if the data is no longer available
         *              in the requested flavor.
         * @exception UnsupportedFlavorException if the requested data flavor is
         *              not supported.
         */
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            DataFlavor[] richerFlavors = getRicherFlavors();
            if (isRicherFlavor(flavor)) {
                return getRicherData(flavor);
            } else if (isHTMLFlavor(flavor)) {
                String data = getHTMLData();
                data = (data == null) ? "" : data; //NOI18N
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return data;
                } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(data);
                } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new ByteArrayInputStream(data.getBytes());
                }
            // fall through to unsupported
            } else if (isPlainFlavor(flavor)) {
                String data = getPlainData();
                data = (data == null) ? "" : data;
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return data;
                } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(data);
                } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new ByteArrayInputStream(data.getBytes());
                }
            // fall through to unsupported

            } else if (isStringFlavor(flavor)) {
                String data = getPlainData();
                data = (data == null) ? "" : data; //NOI18N
                return data;
            }
            throw new UnsupportedFlavorException(flavor);
        }

        // --- richer subclass flavors ----------------------------------------------
        protected boolean isRicherFlavor(DataFlavor flavor) {
            DataFlavor[] richerFlavors = getRicherFlavors();
            int nFlavors = (richerFlavors != null) ? richerFlavors.length : 0;
            for (int i = 0; i < nFlavors; i++) {
                if (richerFlavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Some subclasses will have flavors that are more descriptive than HTML
         * or plain text.  If this method returns a non-null value, it will be
         * placed at the start of the array of supported flavors.
         */
        protected DataFlavor[] getRicherFlavors() {
            return null;
        }

        protected Object getRicherData(DataFlavor flavor) throws UnsupportedFlavorException {
            return null;
        }

        // --- html flavors ----------------------------------------------------------
        /**
         * Returns whether or not the specified data flavor is an HTML flavor that
         * is supported.
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isHTMLFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = htmlFlavors;
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Should the HTML flavors be offered?  If so, the method
         * getHTMLData should be implemented to provide something reasonable.
         */
        protected boolean isHTMLSupported() {
            return htmlData != null;
        }

        /**
         * Fetch the data in a text/html format
         */
        protected String getHTMLData() {
            return htmlData;
        }

        // --- plain text flavors ----------------------------------------------------
        /**
         * Returns whether or not the specified data flavor is an plain flavor that
         * is supported.
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isPlainFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = plainFlavors;
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Should the plain text flavors be offered?  If so, the method
         * getPlainData should be implemented to provide something reasonable.
         */
        protected boolean isPlainSupported() {
            return plainData != null;
        }

        /**
         * Fetch the data in a text/plain format.
         */
        protected String getPlainData() {
            return plainData;
        }

        // --- string flavorss --------------------------------------------------------
        /**
         * Returns whether or not the specified data flavor is a String flavor that
         * is supported.
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isStringFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = stringFlavors;
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }
    }
    // END of fix of issue #43309
}

