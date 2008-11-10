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

import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class Reformatter implements ReformatTask {
    private Context context;
    private Document doc;
    private CodeStyle codeStyle;
    private static boolean expandTabToSpaces = true;
    private static int tabSize = 8;


    public Reformatter(Context context) {
        this.context = context;
        this.doc = context.document();
    }

    public Reformatter(Document doc, CodeStyle codeStyle) {
        this.doc = doc;
        this.codeStyle = codeStyle;
    }

    public void reformat() throws BadLocationException {
        if (codeStyle == null){
            codeStyle = CodeStyle.getDefault(doc);
        }
	expandTabToSpaces = codeStyle.expandTabToSpaces();
        tabSize = codeStyle.getTabSize();
        if (context != null) {
            for (Context.Region region : context.indentRegions()) {
                reformatImpl(region);
            }
        } else {
            int endOffset = doc.getLength();
            TokenHierarchy hierarchy = TokenHierarchy.get(doc);
            if (hierarchy == null) {
                return;
            }
            reformatImpl(hierarchy, 0, endOffset);
        }
    }

    private void reformatImpl(Context.Region region) throws BadLocationException {
        int startOffset = region.getStartOffset();
        int endOffset = region.getEndOffset();
        if ("text/x-c++".equals(context.mimePath())) { //NOI18N
            reformatLanguage(CppTokenId.languageCpp(), startOffset, endOffset);
        } else if ("text/x-c".equals(context.mimePath())) { //NOI18N
            reformatLanguage(CppTokenId.languageC(), startOffset, endOffset);
        }
    }

    private void reformatLanguage(Language<CppTokenId> language, int startOffset, int endOffset) throws BadLocationException {
        TokenHierarchy hierarchy = TokenHierarchy.create(doc.getText(0, doc.getLength()), language);
        if (hierarchy == null) {
            return;
        }
        reformatImpl(hierarchy, startOffset, endOffset);
    }

                
    @SuppressWarnings("unchecked")
    private void reformatImpl(TokenHierarchy hierarchy, int startOffset, int endOffset) throws BadLocationException {
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
            if (ts.language() == CppTokenId.languageC() ||
                ts.language() == CppTokenId.languageCpp() ||
                ts.language() == CppTokenId.languagePreproc()) {
                reformatImpl((TokenSequence<CppTokenId>) ts, startOffset, endOffset);
                return;
            }
            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }
            ts = ts.embedded();
        }
    }
    
    private void reformatImpl(TokenSequence<CppTokenId> ts, int startOffset, int endOffset) throws BadLocationException {
        int prevStart = -1;
        int prevEnd = -1;
        String prevText = null;
        for (Diff diff : new ReformatterImpl(ts, startOffset, endOffset, codeStyle).reformat()) {
            int curStart = diff.getStartOffset();
            int curEnd = diff.getEndOffset();
            if (startOffset > curEnd || endOffset < curStart) {
                continue;
            }
            String curText = diff.getText();
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
                Logger log = Logger.getLogger("org.netbeans.modules.cnd.editor"); // NOI18N
                String error = NbBundle.getMessage(Reformatter.class, "REFORMATTING_FAILED", // NOI18N
                        doc.getText(start, end - start), text);
                log.severe(error);
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
    
    public ExtraLock reformatLock() {
        return new Lock();
    }

    public static class Factory implements ReformatTask.Factory {
        public ReformatTask createTask(Context context) {
            return new Reformatter(context);
        }        
    }

    private class Lock implements ExtraLock {
        public void lock() {}
        public void unlock() {}        
    }
    
    static class Diff {
        private int start;
        private int end;
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

        public String getText() {
            return repeatChar(newLines, '\n', false)+repeatChar(spaces, ' ', isIndent); // NOI18N
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

        public static String repeatChar(int length, char c, boolean indent) {
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
            if (c == ' '  && indent && !Reformatter.expandTabToSpaces && Reformatter.tabSize > 1) {
                while (length >= Reformatter.tabSize) {
                    buf.append('\t'); //NOI18N
                    length -= Reformatter.tabSize;
                }
            }
            for (int i = 0; i < length; i++) {
                buf.append(c);
            }
            return buf.toString();
        }

        public static boolean equals(String text, int newLines, int spaces, boolean isIndent){
           String space = repeatChar(newLines, '\n', false)+repeatChar(spaces, ' ', isIndent); // NOI18N
           return text.equals(space);
        }
    }
}
