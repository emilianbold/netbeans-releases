/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.query;

import java.net.URI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntity;
import org.netbeans.modules.cnd.api.codemodel.visit.CMReference;
import org.netbeans.modules.cnd.api.codemodel.visit.CMReferenceQuery;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Vladimir Kvashin
 */
public class CMReferenceResolver {

    private static final CMReferenceResolver INSTANCE = new CMReferenceResolver();

    public static CMReferenceResolver getDefault() {
        return INSTANCE;
    }

    public CMReference findReference(FileObject fo, int offset) {
        StyledDocument doc = CMUtilities.getDocument(fo);
//        if (doc == null) {
//            return findReference(doc, offset);
//        }
//        return null;
        throw new UnsupportedOperationException(); // TODO: implement
    }

    public CMReference findReference(FileObject fo, int line, int column) {
        //StyledDocument doc = CMUtilities.getDocument(fo);
//        if (doc == null) {
//            return findReference(doc, offset);
//        }
//        return null;
        return CMReferenceQuery.findReference(fo.toURI(), line, column);
//        throw new UnsupportedOperationException(); // TODO: implement
    }

    public CMReference findReference(Document doc, int line, int column) {
        URI uri = CMUtilities.getURI(doc);
        return (uri == null) ? null : CMReferenceQuery.findReference(uri, line, column);
//        CMFile cmFile = CMUtilities.getCMFile(doc, true, true);
//        if (cmFile != null) {
//            CMTranslationUnit tu = CMUtilities.getTranslationUnit(doc, true, true); //TODO: should be cmFile.getTranslationUnit
//            if (tu != null) {
//                CMSourceLocation loc;
//                    CMSourceLocation offsetLoc, lineColLoc = null;
//                    lineColLoc = tu.getLocation(cmFile, line, column);
//                    offsetLoc = tu.getLocation(cmFile, line, column);
//                    loc = offsetLoc.isValid() ? offsetLoc : lineColLoc;
//                CMCursor cursor = tu.getCursor(loc);
//                new UnsupportedOperationException("Don't know how to get reference by offset").printStackTrace(System.err);//NOI18N
//                return null;
//            }
//        }
//        return null;
    }
    
    public CMReference findReference(final Document doc, int offset) {
        URI uri = CMUtilities.getURI(doc);
        if (uri == null) {
            return null;
        }
        final int[] off = new int[]{offset};
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, off[0], true, true);
                ts.move(off[0]);
                if (ts.moveNext()) {
                    Token<TokenId> token = ts.token();
                    off[0] = ts.offset();
                }
            }
        });
        offset = off[0];
        
        CMReference res = CMReferenceQuery.findReference(uri, offset);
        if (res == null) {
            // TODO: remove it!
            //for(int i = offset-1, j=0; i > 0 && j < 5; i--, j++) {
            //    res = CMReferenceQuery.findReference(uri, i);
            //    if (res != null) {
            //        return res;
            //    }
            //}
        }
        return res;
//        CMFile cmFile = CMUtilities.getCMFile(doc, true, true);
//        if (cmFile != null) {
//            CMTranslationUnit tu = CMUtilities.getTranslationUnit(doc, true, true); //TODO: should be cmFile.getTranslationUnit
//            if (tu != null) {
//                CMSourceLocation loc;
//                LineAndCol lineCol = getLineAndCol(doc, offset);
//                {
//                    CMSourceLocation offsetLoc, lineColLoc = null;
//                    if (lineCol != null) {
//                        lineColLoc = tu.getLocation(cmFile, lineCol.line, lineCol.col);
//                    }
//                    offsetLoc = tu.getLocation(cmFile, offset);
//                    loc = offsetLoc.isValid() ? offsetLoc : lineColLoc;
//                }
//                CMCursor cursor = tu.getCursor(loc);
//                new UnsupportedOperationException("Don't know how to get reference by offset").printStackTrace(System.err);//NOI18N
//                return null;
//            }
//        }
//        return null;
    }
    
    private LineAndCol getLineAndCol(Document doc, int offset) {
        if (doc instanceof LineDocument) {
            LineDocument baseDoc = (LineDocument) doc;
            try {
                int lineOffset = LineDocumentUtils.getLineIndex(baseDoc, offset);
                int rowStart = LineDocumentUtils.getLineStartFromIndex(baseDoc, lineOffset);
                int rowOffset = offset - rowStart;
                return new LineAndCol(lineOffset + 1, rowOffset + 1);
            } catch (BadLocationException ex) {
                ex.printStackTrace(System.err);
            }
        }
        return null;
    }

    public CMEntity findReference(Node node) {
        return null;//TODO: implement
    }

    private static class LineAndCol {
        public final int line;
        public final int col;
        public LineAndCol(int line, int col) {
            this.line = line;
            this.col = col;
        }
        @Override
        public String toString() {
            return "" + line + ',' + col; //NOIO18N
        }
    }
}
