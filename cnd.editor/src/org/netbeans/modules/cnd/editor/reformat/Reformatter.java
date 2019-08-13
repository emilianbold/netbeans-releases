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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.reformat;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.clang.format.FormatGlobals;
import org.clang.format.FormatStyle;
import org.clang.tooling.core.Range;
import org.clang.tooling.core.Replacement;
import org.clank.java.std;
import org.llvm.adt.StringRef;
import org.llvm.adt.aliases.ArrayRef;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class Reformatter implements ReformatTask {
    private Context context;
    private final Document doc;
    private CodeStyle codeStyle;
    private int carret = -1;
    private JTextComponent currentComponent;
    private boolean expandTabToSpaces = true;
    private int tabSize = 8;
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.editor"); // NOI18N


    public Reformatter(Context context) {
        this.context = context;
        this.doc = context.document();
    }

    public Reformatter(Document doc, CodeStyle codeStyle) {
        this.doc = doc;
        this.codeStyle = codeStyle;
    }

    @Override
    public void reformat() throws BadLocationException {
        if (codeStyle == null){
            codeStyle = CodeStyle.getDefault(doc);
        }
        for(JTextComponent component : EditorRegistry.componentList()) {
            if (doc.equals(component.getDocument())) {
                carret = component.getCaretPosition();
                currentComponent = component;
                break;
            }
        }
	expandTabToSpaces = codeStyle.expandTabToSpaces();
        tabSize = codeStyle.getTabSize();
        if (tabSize <= 1) {
            tabSize = 8;
        }
        if (Boolean.TRUE.equals(doc.getProperty("code-template-insert-handler"))) { // NOI18N
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                if (fo != null) {
                    String lf = (String)fo.getAttribute(FileObject.DEFAULT_LINE_SEPARATOR_ATTR);
                    if (lf != null) {
                        doc.putProperty(FileObject.DEFAULT_LINE_SEPARATOR_ATTR, lf);
                        doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, lf);
                    }
                    doc.insertString(0, "", null); // NOI18N
                }
            }
        }

        if (context != null) {
            for (Context.Region region : context.indentRegions()) {
                reformatImpl(region);
            }
        } else {
            int endOffset = doc.getLength();
            TokenHierarchy<?> hierarchy = TokenHierarchy.get(doc);
            if (hierarchy == null) {
                LOG.log(Level.SEVERE, "Token hierarchy is not found in the document {1}", new Object[]{doc});
                return;
            }
            reformatImpl(hierarchy, 0, endOffset);
        }
    }

    private void reformatImpl(Context.Region region) throws BadLocationException {
        int startOffset = region.getStartOffset();
        int endOffset = region.getEndOffset();
        if (endOffset > 0 && endOffset < doc.getLength()) {
            String text = doc.getText(endOffset - 1, 1);
            if (text.charAt(0) == '\n') {
                endOffset--;
            }
        }
        Language<CppTokenId> language = CndLexerUtilities.getLanguage(context.mimePath());
        if (language != null) {
            FormatStyle clangFormatStyle = codeStyle.getClangFormatStyle();
            if (clangFormatStyle == null) {
                reformatLanguage(language, startOffset, endOffset);
            } else {
                reformat(clangFormatStyle, startOffset, endOffset);
            }
        } else {
            //LOG.log(Level.SEVERE, "Language of mime type {0} is not found in the document {1}", new Object[]{context.mimePath(), doc});
        }
    }

    private void reformat(FormatStyle clangFormatStyle, int startOffset, int endOffset) throws BadLocationException {
        final String text = doc.getText(0, doc.getLength());
        final byte[] byteText = new byte[text.length()+1];
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c <= 127) {
                byteText[i] = (byte)(c & 0xFF);
            } else {
                // Assumption: formatter never copied non-white text in replacment.
                // Replace all wide chars to keep parity between char and byte offsets.
                byteText[i] = (byte)(0xD3);
            }
        }
        byteText[text.length()] = 0;
        //StringRef stringRef = new StringRef(text);
        StringRef stringRef = new StringRef(byteText);
        ArrayRef<Range> Ranges = new ArrayRef(new Range[]{new Range(startOffset, endOffset - startOffset)});
        String title = (String) doc.getProperty("title"); //NOI18N
        if (title == null) {
          title = "<stdin>"; //NOI18N
        }
        StringRef file = new StringRef(/*KEEP_STR*/title); //NOI18N
        std.setType<Replacement> replaces = FormatGlobals.reformat(clangFormatStyle, stringRef, Ranges, file);
        LinkedList<Replacement> diffs = new LinkedList<Replacement>();
        for(Replacement r : replaces){
            diffs.addFirst(r);
        }
        for(Replacement r : diffs) {
            unsafeApplyDiff(r.getOffset(), r.getOffset()+r.getLength(), r.getReplacementText().toJavaString());
        }
    }

    private void reformatLanguage(Language<CppTokenId> language, int startOffset, int endOffset) throws BadLocationException {
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(doc.getText(0, doc.getLength()), language);
        if (hierarchy == null) {
            LOG.log(Level.SEVERE, "Token hierarchy {0} is not found in the document {1}", new Object[]{language, doc});
            return;
        }
        reformatImpl(hierarchy, startOffset, endOffset);
    }

                
    @SuppressWarnings("unchecked")
    private void reformatImpl(TokenHierarchy<?> hierarchy, int startOffset, int endOffset) throws BadLocationException {
        TokenSequence<?> ts = hierarchy.tokenSequence();
        ts.move(startOffset);
        if (ts.moveNext() && ts.token().id() != CppTokenId.NEW_LINE){
            while (ts.movePrevious()){
                startOffset = ts.offset();
                if (ts.token().id() != CppTokenId.NEW_LINE) {
                    break;
                }
            }
        }
        while (ts != null && (startOffset == 0 || ts.moveNext())) {
            ts.move(startOffset);
            if (CndLexerUtilities.isCppLanguage(ts.language(), true)) {
                reformatImpl((TokenSequence<CppTokenId>) ts, startOffset, endOffset);
                return;
            }
            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }
            ts = ts.embedded();
        }
    }

    private static final int FAST_DIFF_SIZE = 10*1000;
    private static final int GAP_SIZE = 500;
    private void reformatImpl(TokenSequence<CppTokenId> ts, int startOffset, int endOffset) throws BadLocationException {
        int prevStart = -1;
        int prevEnd = -1;
        String prevText = null;
        LinkedList<Diff> reformatDiffs = new ReformatterImpl(ts, startOffset, endOffset, codeStyle).reformat();
        LinkedList<Diff> pack = new LinkedList<Diff>();
        for(Diff diff : reformatDiffs) {
            if (diff.getStartOffset() == diff.getEndOffset() &&
                diff.getText(expandTabToSpaces, tabSize).length() == 0){
                continue;
            }
            pack.add(diff);
        }
        reformatDiffs = pack;
        if (reformatDiffs.size() < FAST_DIFF_SIZE) {
            for (Diff diff : reformatDiffs) {
                int curStart = diff.getStartOffset();
                int curEnd = diff.getEndOffset();
                if (startOffset > curEnd || endOffset < curStart) {
                    continue;
                }
                String curText = diff.getText(expandTabToSpaces, tabSize);
                if (carret != -1) {
                    if (carret >= curStart) {
                        carret += curText.length() - (curEnd - curStart);
                    }
                }
                if (endOffset < curEnd) {
                    if (curText != null && curText.length() > 0) {
                        curText = curEnd - endOffset >= curText.length() ? null :
                               curText.substring(0, curText.length() - curEnd + endOffset);
                    }
                    curEnd = endOffset;
                }
                if (prevStart == curEnd) {
                    prevStart = curStart;
                    prevText = curText+prevText;
                    continue;
                } else {
                    if (!applyDiff(prevStart, prevEnd, prevText)){
                        return;
                    }
                    prevStart = curStart;
                    prevEnd = curEnd;
                    prevText = curText;
                }
            }
            if (prevStart > -1) {
                applyDiff(prevStart, prevEnd, prevText);
            }
        } else {
            StringBuilder buf = new StringBuilder(doc.getText(0, doc.getLength()));
            LinkedList<String> res = new LinkedList<String>();
            for (Diff diff : reformatDiffs) {
                int start = diff.getStartOffset();
                int end = diff.getEndOffset();
                if (buf.length() > end + GAP_SIZE) {
                    res.addFirst(buf.substring(end + GAP_SIZE));
                    buf.setLength(end + GAP_SIZE);
                }
                String text = diff.getText(expandTabToSpaces, tabSize);
                if (startOffset > end || endOffset < start) {
                    System.err.println("What?" + startOffset + ":" + start + "-" + end);// NOI18N
                    continue;
                }
                if (carret != -1) {
                    if (carret >= start) {
                        carret += text.length() - (end - start);
                    }
                }
                if (endOffset < end) {
                    if (text != null && text.length() > 0) {
                        text = end - endOffset >= text.length() ? null : text.substring(0, text.length() - end + endOffset);
                    }
                    end = endOffset;
                }
                String what = buf.substring(start, end);
                if (text != null && text.equals(what)) {
                    // optimization
                    continue;
                }
                if (end - start > 0) {
                    if (!checkRemoved(what)){
                        // Reformat failed
                        LOG.log(Level.SEVERE, NbBundle.getMessage(Reformatter.class, "REFORMATTING_FAILED", // NOI18N
                                doc.getText(start, end - start), text));
                        return;
                    }
                    buf.delete(start, end);
                }
                if (text != null && text.length() > 0) {
                    buf.insert(start, text);
                }
            }
            res.addFirst(buf.toString());
            StringBuilder prod = new StringBuilder(doc.getLength());
            for(String s : res) {
                prod.append(s);
            }
            doc.remove(0, doc.getLength());
            doc.insertString(0, prod.toString(), null);
        }
        if (carret != -1) {
            currentComponent.getCaret().setDot(carret);
        }
    }

    private boolean unsafeApplyDiff(int start, int end, String text) throws BadLocationException{
        if (end - start > 0) {
            doc.remove(start, end - start);
        }
        if (text != null && text.length() > 0) {
            doc.insertString(start, text, null);
        }
        return true;
    }

    private boolean applyDiff(int start, int end, String text) throws BadLocationException{
        if (end - start > 0) {
            String what = doc.getText(start, end - start);
            if (text != null && text.equals(what)) {
                // optimization
                return true;
            }
            if (!checkRemoved(what)){
                // Reformat failed
                LOG.log(Level.SEVERE, NbBundle.getMessage(Reformatter.class, "REFORMATTING_FAILED", // NOI18N
                        doc.getText(start, end - start), text));
                return false;
            }
            doc.remove(start, end - start);
        }
        if (text != null && text.length() > 0) {
            doc.insertString(start, text, null);
        }
        return true;
    }
            
    
    private boolean checkRemoved(String whatRemoved){
        for(int i = 0; i < whatRemoved.length(); i++){
            char c = whatRemoved.charAt(i);
            switch(c){
                case ' ':
                case '\n':
                case '\t':
                case '\r':
                case '\f':
                case 0x0b:
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }
    
    @Override
    public ExtraLock reformatLock() {
        return new Lock();
    }

    public static class Factory implements ReformatTask.Factory {
        @Override
        public ReformatTask createTask(Context context) {
            return new Reformatter(context);
        }        
    }

    private static class Lock implements ExtraLock {
        @Override
        public void lock() {}
        @Override
        public void unlock() {}        
    }
    
    static class Diff {
        private final int start;
        private final int end;
        private int newLines;
        private int spaces;
        private boolean isIndent;

        Diff(int start, int end, int newLines, int spaces, boolean isIndent) {
            this.start = start;
            this.end = end;
            this.spaces = spaces;
            this.newLines = newLines;
	    this.isIndent = isIndent;
        }

        public int getStartOffset() {
            return start;
        }
        
        public int getEndOffset() {
            return end;
        }

        public String getText( boolean expandTabToSpaces, int tabSize) {
            return repeatChar(newLines, '\n', false, expandTabToSpaces, tabSize)+repeatChar(spaces, ' ', isIndent, expandTabToSpaces, tabSize); // NOI18N
        }

        public void setText(int newLines, int spaces, boolean isIndent) {
            this.newLines = newLines;
            this.spaces = spaces;
	    this.isIndent = isIndent;
        }
        
        public void replaceSpaces(int spaces, boolean isIndent){
            this.spaces = spaces;
	    this.isIndent = isIndent;
        }

        public boolean hasNewLine(){
            return newLines > 0;
        }

        public int spaceLength() {
            return spaces;
        }

        @Override
        public String toString() {
            return "Diff<" + start + "," + end + ">: newLines="+newLines+" spaces="+spaces; //NOI18N
        }

        private static String repeatChar(int length, char c, boolean indent, boolean expandTabToSpaces, int tabSize) {
            if (length == 0) {
                return ""; //NOI18N
            } else if (length == 1) {
                if (c == ' ') {
                    return " "; //NOI18N
                } else {
                    return "\n"; //NOI18N
                }
            }
            StringBuilder buf = new StringBuilder(length);
            if (c == ' '  && indent && !expandTabToSpaces && tabSize > 1) {
                while (length >= tabSize) {
                    buf.append('\t'); //NOI18N
                    length -= tabSize;
                }
            }
            for (int i = 0; i < length; i++) {
                buf.append(c);
            }
            return buf.toString();
        }

        public static boolean equals(String text, int newLines, int spaces, boolean isIndent, boolean expandTabToSpaces, int tabSize){
           String space = repeatChar(newLines, '\n', false, expandTabToSpaces, tabSize)+repeatChar(spaces, ' ', isIndent, expandTabToSpaces, tabSize); // NOI18N
           return text.equals(space);
        }
    }
}
