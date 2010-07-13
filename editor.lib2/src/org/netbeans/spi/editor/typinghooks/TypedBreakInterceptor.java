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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.spi.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Parameters;

/**
 *
 * @author Vita Stejskal
 */
public interface TypedBreakInterceptor {

    boolean beforeInsert(Context context) throws BadLocationException;
    void insert(MutableContext context) throws BadLocationException;
    void afterInsert(Context context) throws BadLocationException;
    void cancelled(Context context);
    
    public static class Context {
        
        /**
         * Gets the editor component where the currently processed key typed event
         * occurred.
         *
         * @return The editor pane that contains the edited <code>Document</code>.
         */
        public JTextComponent getComponent() {
            return component;
        }

        /**
         * Gets the edited document. It's the document where the line break is going to
         * be inserted.
         *
         * @return The edited document.
         */
        public Document getDocument() {
            return document;
        }

        /**
         * Gets the caret offset. This is the offset in the document where
         * the caret is at the time when a user performed an action resulting in
         * the insertion of a line break (ie. where the currently processed <code>KeyEvent</code>
         * happened). This may or may not be the same offset, where the line break
         * will be inserted.
         *
         * @return The offset in the edited document.
         */
        public int getCaretOffset() {
            return caretOffset;
        }
        
        /**
         * Gets the line break insertion offset. This is the offset in the document where
         * the line break will be inserted.
         *
         * @return The offset in the edited document.
         */
        public int getBreakInsertOffset() {
            return breakInsertOffset;
        }
        
        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------
        
        private final JTextComponent component;
        private final Document document;
        private final int caretOffset;
        private final int breakInsertOffset;
        
        private Context(JTextComponent component, int caretOffset, int breakInsertOffset) {
            this.component = component;
            this.document = component.getDocument();
            this.caretOffset = caretOffset;
            this.breakInsertOffset = breakInsertOffset;
        }
        
    } // End of Context class
    
    public static final class MutableContext extends Context {
        
        /**
         * 
         * @param text (required)
         * @param breakInsertPosition
         * @param caretPosition
         * @param reindentBlocks
         */
        public void setText(String text, int breakInsertPosition, int caretPosition, int... reindentBlocks) {
            Parameters.notNull("text", text); //NOI18N
            
            if (text.indexOf('\n') == -1) {
                throw new IllegalArgumentException("The text must contain a new line (\\n) character."); //NOI18N
            }
            
            if (breakInsertPosition != -1) {
                if (breakInsertPosition < 0 || breakInsertPosition >= text.length()) {
                    throw new IllegalArgumentException("The breakInsertPosition=" + breakInsertPosition + " must point in the text=<0, " + text.length() + ")."); //NOI18N
                }
                if (text.charAt(breakInsertPosition) != '\n') {
                    throw new IllegalArgumentException("The character at breakInsertPosition=" + breakInsertPosition + " must be the new line (\\n) character."); //NOI18N
                }
            }
            
            if (caretPosition != -1) {
                if (caretPosition < 0 || caretPosition > text.length()) {
                    throw new IllegalArgumentException("The caretPosition=" + caretPosition + " must point in the text=<0, " + text.length() + ">."); //NOI18N
                }
            }

            if (reindentBlocks != null && reindentBlocks.length > 0) {
                if (reindentBlocks.length % 2 != 0) {
                    throw new IllegalArgumentException("The reindentBlocks must contain even number of positions within the text: " + reindentBlocks.length); //NOI18N
                }
                for(int i = 0; i < reindentBlocks.length / 2; i++) {
                    int s = reindentBlocks[2 * i];
                    if (s < 0 || s > text.length()) {
                        throw new IllegalArgumentException("The reindentBlocks[" + (2 * i) + "]=" + s + " must point in the text=<0, " + text.length() + ")."); //NOI18N
                    }
                    int e = reindentBlocks[2 * i + 1];
                    if (e < 0 || e > text.length()) {
                        throw new IllegalArgumentException("The reindentBlocks[" + (2 * i + 1) + "]=" + e + " must point in the text=<0, " + text.length() + ")."); //NOI18N
                    }
                    if (s > e) {
                        throw new IllegalArgumentException("The reindentBlocks[" + (2 * i) + "]=" + s + " must be smaller than reindentBlocks[" + (2 * i + 1) + "]=" + e); //NOI18N
                    }
                }
                
            }
            
            this.insertionText = text;
            this.breakInsertPosition = breakInsertPosition;
            this.caretPosition = caretPosition;
            this.reindentBlocks = reindentBlocks;
        }
        
        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------
        
        private String insertionText = null;
        private int breakInsertPosition = -1;
        private int caretPosition = -1;
        private int [] reindentBlocks = null;
        
        /* package */ MutableContext(JTextComponent component, int caretOffset, int insertBreakOffset) {
            super(component, caretOffset, insertBreakOffset);
        }
        
        /* package */ Object [] getData() {
            return insertionText != null ?
                new Object [] { insertionText, breakInsertPosition, caretPosition, reindentBlocks } :
                null;
        }
        
        /* package */ void resetData() {
            insertionText = null;
            breakInsertPosition = -1;
            caretPosition = -1;
            reindentBlocks = null;
        }
    } // End of MutableContext class

    /**
     * The factory interface for registering <code>TypedBreakInterceptor</code>s
     * in <code>MimeLookup</code>. An example registration in an XML layer shown
     * below registers <code>Factory</code> implementation under <code>text/x-something</code>
     * mime type in <code>MimeLookup</code>.
     *
     * <pre>
     * &lt;folder name="Editors"&gt;
     *  &lt;folder name="text"&gt;
     *   &lt;folder name="x-something"&gt;
     *    &lt;file name="org-some-module-TBIFactory.instance" /&gt;
     *   &lt;/folder&gt;
     *  &lt;/folder&gt;
     * &lt;/folder&gt;
     * </pre>
     */
    public interface Factory {

        /**
         * Creates a new interceptor for the given <code>MimePath</code>.
         * 
         * @param mimePath The <code>MimePath</code> for which the infrastructure
         *   needs the new interceptor. Typically this is the same <code>MimePath</code>
         *   where this <code>Factory</code> was registered, but in embedded scenarios
         *   this can be a different <code>MimePath</code>.
         *
         * @return The new interceptor.
         */
        TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath);
    } // End of Factory interface
}
