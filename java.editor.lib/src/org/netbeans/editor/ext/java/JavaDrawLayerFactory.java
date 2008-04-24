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

package org.netbeans.editor.ext.java;

import javax.swing.text.BadLocationException;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSyntaxSupport;

/**
 * Various java-layers
 *
 * @author Miloslav Metelka
 * @version 1.00
 * @deprecated Please use Highlighting SPI instead, for details see
 *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
 */

public class JavaDrawLayerFactory {

    public static final String JAVA_LAYER_NAME = "java-layer"; // NOI18N

    public static final int JAVA_LAYER_VISIBILITY = 1010;

    /** Layer that colors extra java information like the methods or special
     * characters in the character and string literals.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static class JavaLayer extends DrawLayer.AbstractLayer {

        /** End of the area that is resolved right now. It saves
         * repetitive searches for '(' for multiple fragments
         * inside one identifier token.
         */
        private int resolvedEndOffset;

        private boolean resolvedValue;

        private NonWhitespaceFwdFinder nwFinder = new NonWhitespaceFwdFinder();

        public JavaLayer() {
            super(JAVA_LAYER_NAME);
        }

        public void init(DrawContext ctx) {
            resolvedEndOffset = 0; // nothing resolved
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            int nextOffset = ctx.getTokenOffset() + ctx.getTokenLength();

            setNextActivityChangeOffset(nextOffset);
            return true;
        }

        protected Coloring getMethodColoring(DrawContext ctx) {
            TokenContextPath path = ctx.getTokenContextPath().replaceStart(
                JavaLayerTokenContext.contextPath);
            return ctx.getEditorUI().getColoring(
                path.getFullTokenName(JavaLayerTokenContext.METHOD));
        }

        private boolean isMethod(DrawContext ctx) {
            int idEndOffset = ctx.getTokenOffset() + ctx.getTokenLength();
            if (idEndOffset > resolvedEndOffset) { // beyond the resolved area
                resolvedEndOffset = idEndOffset; // will resolve now
                int endOffset = ctx.getEndOffset();
                int bufferStartOffset = ctx.getBufferStartOffset();
                char[] buffer = ctx.getBuffer();
                ExtSyntaxSupport sup = (ExtSyntaxSupport) ctx.getEditorUI().getDocument().getSyntaxSupport().get(ExtSyntaxSupport.class);
                int nwOffset = Analyzer.findFirstNonWhite(buffer,
                        idEndOffset - bufferStartOffset,
                        endOffset - idEndOffset);
                if (nwOffset >= 0) { // found non-white
                    resolvedValue = (buffer[nwOffset] == '(');
                    if (!resolvedValue && buffer[nwOffset] == '<') {
                        try {
                            int[] block = sup.findMatchingBlock(ctx.getBufferStartOffset() + nwOffset, true);
                            if (block != null) {
                                int off = Utilities.getFirstNonWhiteFwd(ctx.getEditorUI().getDocument(), block[1]);
                                if (off > -1) {
                                    if (bufferStartOffset + buffer.length > off) {
                                        resolvedValue = (buffer[off - bufferStartOffset] == '(');
                                    } else {
                                        resolvedValue = (ctx.getEditorUI().getDocument().getChars(off, 1)[0] == '(');
                                    }
                                }
                            }
                        } catch (BadLocationException e) {
                            resolvedValue = false;
                        }
                    }
                } else { // must resolve after buffer end
                    try {
                        int off = ctx.getEditorUI().getDocument().find(nwFinder, endOffset, -1);
                        resolvedValue = off >= 0 && (nwFinder.getFoundChar() == '(');
                        if (!resolvedValue && nwFinder.getFoundChar() == '<') {
                            int[] block = sup.findMatchingBlock(off, true);
                            if (block != null) {
                                off = Utilities.getFirstNonWhiteFwd(ctx.getEditorUI().getDocument(), block[1]);
                                if (off > -1)
                                    resolvedValue = (ctx.getEditorUI().getDocument().getChars(off, 1)[0] == '(');
                            }
                        }
                    } catch (BadLocationException e) {
                        resolvedValue = false;
                    }
                }
            }

            return resolvedValue;
        }

        public void updateContext(DrawContext ctx) {
            if (ctx.getTokenID() == JavaTokenContext.IDENTIFIER && isMethod(ctx)) {
                Coloring mc = getMethodColoring(ctx);
                if (mc != null) {
                    mc.apply(ctx);
                }
            }
        }

    }

    /** Find first non-white character forward */
    static class NonWhitespaceFwdFinder extends FinderFactory.GenericFwdFinder {

        private char foundChar;

        public char getFoundChar() {
            return foundChar;
        }

        protected int scan(char ch, boolean lastChar) {
            if (!Character.isWhitespace(ch)) {
                found = true;
                foundChar = ch;
                return 0;
            }
            return 1;
        }
    }

    /** Find first non-white character backward */
    public static class NonWhitespaceBwdFinder extends FinderFactory.GenericBwdFinder {

        private char foundChar;

        public char getFoundChar() {
            return foundChar;
        }

        protected int scan(char ch, boolean lastChar) {
            if (!Character.isWhitespace(ch)) {
                found = true;
                foundChar = ch;
                return 0;
            }
            return -1;
        }
    }

}

