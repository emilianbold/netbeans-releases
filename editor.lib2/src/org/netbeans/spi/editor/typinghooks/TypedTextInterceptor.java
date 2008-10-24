/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.spi.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.lib2.typinghooks.TypingHooksSpiAccessor;

/**
 *
 * @author vita
 * @since 1.9
 */
public interface TypedTextInterceptor {

    /**
     * - runs outside document lock
     * - can return true to cancel further processing
     * - cannot alter text that will be inserted in the document as a result of
     *   further processing
     *
     * @param context
     * @return <code>false</code> by default. You can return <code>true</code> in
     *   order to cancel further processing of the typed text.
     */
    boolean beforeInsertion(Context context) throws BadLocationException;

    /**
     * - the first that mutates the typedText wins, others' typedText is not called
     * - caretPosition is within the insertionText (!) and not within the document
     * @param context
     * @throws javax.swing.text.BadLocationException
     */
    void textTyped(MutableContext context) throws BadLocationException;

    void afterInsertion(Context context) throws BadLocationException;

    void cancelled(Context context);

    public static class Context {

        public JTextComponent getComponent() {
            return component;
        }

        public Document getDocument() {
            return document;
        }

        public int getOffset() {
            return offset;
        }

        /**
         * - guaranteed to have lenght==1 in beforeInsertion and textTyped, but
         *   can have any lenght in afterInsertion or cancelled
         * @return
         */
        public String getText() {
            return originallyTypedText;
        }

        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------

        private final JTextComponent component;
        private final Document document;
        private final int offset;
        private final String originallyTypedText;

        /* package */ Context(JTextComponent component, int offset, String typedText) {
            this.component = component;
            this.document = component.getDocument();
            this.offset = offset;
            this.originallyTypedText = typedText;
        }
        
    } // End of Context class

    public static final class MutableContext extends Context {

        public @Override String getText() {
            return insertionText != null ? insertionText : super.getText();
        }

        public void setText(String text, int caretPosition) {
            assert text != null : "Invalid text, it must not be null."; //NOI18N
            assert caretPosition >= 0 && caretPosition < text.length() : "Invalid caretPostion=" + caretPosition + ", text.length=" + text.length(); //NOI18N

            this.insertionText = text;
            this.caretPosition = caretPosition;
        }

        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------

        private String insertionText = null;
        private int caretPosition = -1;
        
        /* package */ MutableContext(JTextComponent c, int offset, String typedText) {
            super(c, offset, typedText);
        }

        private static final class Accessor extends TypingHooksSpiAccessor {

            @Override
            public MutableContext createContext(JTextComponent c, int offset, String typedText) {
                return new MutableContext(c, offset, typedText);
            }

            @Override
            public Object[] getContextData(MutableContext context) {
                return context.insertionText != null ?
                    new Object [] { context.insertionText, context.caretPosition } :
                    null;
            }

            @Override
            public void resetContextData(MutableContext context) {
                context.insertionText = null;
                context.caretPosition = -1;
            }

        }

        static {
            TypingHooksSpiAccessor.register(new Accessor());
        }

    } // End of MutableContext class

    public interface Factory {
        TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath);
    } // End of Factory interface

}
