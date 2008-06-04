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

package org.netbeans.api.java.source;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**Binding between virtual Java source and the real source.
 * Please note that this class is needed only for clients that need to work
 * in non-Java files (eg. JSP files) or in dialogs, like code completion.
 * Most clients do not need to use this class.
 * 
 * @author Dusan Balek
 * @since 0.21
 */
public final class PositionConverter {
    
    private int offset;
    private int length;
    private JTextComponent component;
    
    private final Snapshot snapshot;
    
    PositionConverter (final Snapshot snapshot) {
        assert snapshot != null;
        this.snapshot = snapshot;        
    }
    
    PositionConverter (final FileObject fo, int offset, int length, final JTextComponent component) {
//        this.fo = fo;
        this.offset = offset;
        this.length = length;
        this.component = component;
        this.snapshot = null;
    }
    // API of the class --------------------------------------------------------

    /**Compute position in the document for given position in the virtual
     * Java source.
     * 
     * @param javaSourcePosition position in the virtual Java Source
     * @return position in the document
     * @since 0.21
     */
    public int getOriginalPosition(int javaSourcePosition) {
        return snapshot.getOriginalOffset(javaSourcePosition);        
    }
    
    /**Compute position in the virtual Java source for given position
     * in the document.
     *
     * @param originalPosition position in the document
     * @return position in the virtual Java source
     * @since 0.21
     */
    public int getJavaSourcePosition(int originalPosition) {
        return snapshot.getEmbeddedOffset(originalPosition);
    }        
    // Nested classes ----------------------------------------------------------

    private class Filter implements JavaSourceProvider.PositionTranslatingJavaFileFilterImplementation, DocumentListener {
        
        CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
        
        public Filter() {
            component.getDocument().addDocumentListener(this);
        }

        public Reader filterReader(final Reader r) {
            return new Reader() {
                
                private int next = 0;
                private String text = convert(component.getText());

                public int read(char[] cbuf, int off, int len) throws IOException {
                    synchronized (lock) {
                        if (off < 0 || off > cbuf.length || len < 0 || (off + len) > cbuf.length) {
                            throw new IndexOutOfBoundsException();
                        } else if (len == 0) {
                            return 0;
                        }
                        if (text.length() == 0 && length == 0)
                            return r.read(cbuf, off, len);
                        if (next < offset) {
                            int n = r.read(cbuf, off, Math.min(offset - next, len));
                            next += n;
                            return n;
                        }
                        if (next == offset)
                            r.skip(length);
                        if (next < offset + text.length()) {
                            int n = Math.min(offset + text.length() - next, len);
                            text.getChars(next - offset, next - offset + n, cbuf, off);
                            next += n;
                            return n;
                        }
                        return r.read(cbuf, off, len);
                    }
                }

                public void close() throws IOException {
                    r.close();
                }
            };
        }

        public CharSequence filterCharSequence(CharSequence charSequence) {
            return charSequence.subSequence(0, offset) + 
                    convert(component.getText()) + 
                    charSequence.subSequence(offset + length, charSequence.length());
        }

        public Writer filterWriter(Writer w) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChangeListener(ChangeListener listener) {
            listeners.addIfAbsent(listener);
        }
        
        public void removeChangeListener(ChangeListener listener) {
            listeners.remove(listener);
        }
    
        public int getOriginalPosition(int javaSourcePosition) {
            if (javaSourcePosition < offset)
                return -1;
            int diff = javaSourcePosition - offset;
            return diff <= convert(component.getText()).length() ? diff : -1;
        }

        public int getJavaSourcePosition(int originalPosition) {
            return offset + originalPosition;
        }
    
        public void insertUpdate(DocumentEvent event) {
            this.changedUpdate(event);
        }

        public void removeUpdate(DocumentEvent event) {
            this.changedUpdate(event);
        }

        public void changedUpdate(DocumentEvent event) {
            for (ChangeListener changeListener : listeners)
                changeListener.stateChanged(null);
        }
        
        private String convert(String string) {
            StringBuilder sb = new StringBuilder(string.length());
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                if (c == '\r') { //NOI18N
                    if (i + 1 >= string.length() || string.charAt(i + 1) != '\n') { //NOI18N
                        sb.append('\n'); //NOI18N
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
