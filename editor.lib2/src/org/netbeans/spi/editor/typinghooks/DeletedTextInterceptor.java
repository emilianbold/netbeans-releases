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

/**
 *
 * @author Vita Stejskal
 */
public interface DeletedTextInterceptor {

    boolean beforeRemove(Context context) throws BadLocationException;
    void remove(Context context) throws BadLocationException;
    void afterRemove(Context context) throws BadLocationException;
    void cancelled(Context context);
    
    /**
     * The context class providing information about the edited document, its
     * editor pane and the offset where the delete action was performed.
     */
    public static final class Context {

        /**
         * Gets the editor component where the currently processed key typed event
         * occured.
         *
         * @return The editor pane that contains the edited <code>Document</code>.
         */
        public JTextComponent getComponent() {
            return component;
        }
        
        /**
         * Gets the edited document. It's the document, where the text will be
         * removed.
         *
         * @return The edited document.
         */
        public Document getDocument() {
            return document;
        }
        
        /**
         * Gets the removal offset. This is the offset in the document where
         * a user performed the delete action (ie. where the currently processed <code>KeyEvent</code>
         * happened). This is also the offset with text, which will be removed.
         *
         * @return The offset in the edited document.
         */
        public int getOffset() {
            return offset;
        }

// XXX: since this is always one (character) it make no sense to have it
//        public int getLength() {
//            return lenght;
//        }
        
        public boolean isBackwardDelete() {
            return backwardDelete;
        }
        
        public String getText() {
            // XXX: this should always return a copy of the text from the document
            // in beforeRemove and remove actions the same text is still in the document,
            // but in afterRemove the document text will have already been deleted
            // and the interceptor can use the copy from here.
            return removedText;
        }
        
        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------

        private final JTextComponent component;
        private final Document document;
        private final int offset;
        private final boolean backwardDelete;
        private final String removedText;

        /* package */ Context(JTextComponent component, int offset, String removedText, boolean backwardDelete) {
            this.component = component;
            this.document = component.getDocument();
            this.offset = offset;
            this.backwardDelete = backwardDelete;
            this.removedText = removedText;
        }
        
    } // End of Context class

    /**
     * The factory interface for registering <code>DeletedTextInterceptor</code>s
     * in <code>MimeLookup</code>. An example registration in an XML layer shown
     * below registers <code>Factory</code> implementation under <code>text/x-something</code>
     * mime type in <code>MimeLookup</code>.
     *
     * <pre>
     * &lt;folder name="Editors"&gt;
     *  &lt;folder name="text"&gt;
     *   &lt;folder name="x-something"&gt;
     *    &lt;file name="org-some-module-DTIFactory.instance" /&gt;
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
        DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath);
    } // End of Factory interface
}
