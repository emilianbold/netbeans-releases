/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider;
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
    
    private FileObject fo;
    private JavaFileFilterImplementation filter;
    private int offset;
    private int length;
    private JTextComponent component;
    
    PositionConverter (final FileObject fo, final JavaFileFilterImplementation filter) {
        this.fo = fo;
        this.filter = filter;
    }
    
    PositionConverter (final FileObject fo, int offset, int length, final JTextComponent component) {
        this.fo = fo;
        this.offset = offset;
        this.length = length;
        this.component = component;
        this.filter = new Filter();
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
        if (filter instanceof JavaSourceProvider.PositionTranslatingJavaFileFilterImplementation) {
            return ((JavaSourceProvider.PositionTranslatingJavaFileFilterImplementation)filter).getOriginalPosition(javaSourcePosition);
        }
        return javaSourcePosition;
    }
    
    /**Compute position in the virtual Java source for given position
     * in the document.
     *
     * @param originalPosition position in the document
     * @return position in the virtual Java source
     * @since 0.21
     */
    public int getJavaSourcePosition(int originalPosition) {
        if (filter instanceof JavaSourceProvider.PositionTranslatingJavaFileFilterImplementation) {
            return ((JavaSourceProvider.PositionTranslatingJavaFileFilterImplementation)filter).getJavaSourcePosition(originalPosition);
        }
        return originalPosition;
    }

    // Package private methods -------------------------------------------------

    JavaFileFilterImplementation getFilter() {
        return filter;
    }
    
    FileObject getFileObject() {
        return fo;
    }
    
    // Nested classes ----------------------------------------------------------

    private class Filter implements JavaSourceProvider.PositionTranslatingJavaFileFilterImplementation, DocumentListener {
        
        CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
        
        public Filter() {
            component.getDocument().addDocumentListener(this);
        }

        public Reader filterReader(Reader r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public CharSequence filterCharSequence(CharSequence charSequence) {
            return charSequence.subSequence(0, offset) + 
                    component.getText() + 
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
            return diff <= component.getText().length() ? diff : -1;
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
    }
}
