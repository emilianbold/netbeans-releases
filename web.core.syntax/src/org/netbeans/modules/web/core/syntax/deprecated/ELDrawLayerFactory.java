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

package org.netbeans.modules.web.core.syntax.deprecated;

import org.netbeans.modules.web.core.syntax.deprecated.ELLayerTokenContext;
import org.netbeans.modules.web.core.syntax.*;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.DrawContext;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.MarkFactory;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.Analyzer;

/**
* Various EL-layers
*
* @author Petr Pisl
* @deprecated Will be replaced by Semantic Coloring
*/

public class ELDrawLayerFactory {

    public static final String EL_LAYER_NAME = "jsp-el-layer"; // NOI18N

    public static final int EL_LAYER_VISIBILITY = 1010;

    /** Layer that colors extra EL information like the methods or special
     * characters in the character and string literals.
     */
    public static class ELLayer extends DrawLayer.AbstractLayer {

        /** End of the area that is resolved right now. It saves
         * repetitive searches for '(' for multiple fragments
         * inside one identifier token.
         */
        private int resolvedEndOffset;

        private boolean resolvedValue;

        private NonWhitespaceFwdFinder nwFinder = new NonWhitespaceFwdFinder();

        public ELLayer() {
            super(EL_LAYER_NAME);
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
	    
	    /*TokenContextPath path = ctx.getTokenContextPath().replaceStart(
		    ELLayerTokenContext.contextPath);
		return ctx.getEditorUI().getColoring(
		    path.getFullTokenName(ELLayerTokenContext.METHOD));
	    */
	   
	   return ctx.getEditorUI().getColoring(ELLayerTokenContext.contextPath.getFullTokenName(ELLayerTokenContext.METHOD));
        }

        private boolean isMethod(DrawContext ctx) {
	    
            int idEndOffset = ctx.getTokenOffset() + ctx.getTokenLength();
            if (idEndOffset > resolvedEndOffset) { // beyond the resolved area
                resolvedEndOffset = idEndOffset; // will resolve now
                int endOffset = ctx.getEndOffset();		
                int bufferStartOffset = ctx.getBufferStartOffset();
                char[] buffer = ctx.getBuffer();
                int nwOffset = Analyzer.findFirstNonWhite(buffer,
                        idEndOffset - bufferStartOffset,
                        endOffset - idEndOffset);
                if (nwOffset >= 0) { // found non-white
                    resolvedValue = (buffer[nwOffset] == '(');
                } else { // must resolve after buffer end
                    try {
                        resolvedValue = (ctx.getEditorUI().getDocument().find(
                            nwFinder, endOffset, -1) >= 0)
                                && (nwFinder.getFoundChar() == '(');
                    } catch (BadLocationException e) {
                        resolvedValue = false;
                    }
                }
            }
            return resolvedValue;
        }
	/** Update draw context by setting colors, fonts and possibly other draw
	* properties.
	* The method can use information from the context to find where the painting
	* process is currently located. It is called only if the layer is active.
	*/
        public void updateContext(DrawContext ctx) {
            if (ctx.getTokenID() == ELTokenContext.IDENTIFIER && isMethod(ctx)) {
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

