/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.cnd.modelimpl.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.AbstractFileBuffer;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * FileBuffer implementation
 * @author Vladimir Kvashin
 */
public class FileBufferDoc extends AbstractFileBuffer {
    
    private Document doc;
    private EventListenerList listeners = new EventListenerList();
    private DocumentListener docListener;
    
    private static class StringInputStream extends InputStream {
        
        private String buffer;
        private int pos;
        protected int count;
        
        public StringInputStream(String s) {
            buffer = s;
            count = s.length();
        }
        
        public synchronized int read() {
            return (pos < count) ? (buffer.charAt(pos++) & 0xFF) : -1;
        }

        public synchronized int read(byte b[], int off, int len) {
            if (b == null) {
                throw new NullPointerException();
            } else if ((off < 0) || (off > b.length) || (len < 0) ||
                       ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            if (pos >= count) {
                return -1;
            }
            if (pos + len > count) {
                len = count - pos;
            }
            if (len <= 0) {
                return 0;
            }
            String	s = buffer;
            int cnt = len;
            while (--cnt >= 0) {
                b[off++] = (byte)s.charAt(pos++);
            }

            return len;
        }

        public synchronized long skip(long n) {
            if (n < 0) {
                return 0;
            }
            if (n > count - pos) {
                n = count - pos;
            }
            pos += n;
            return n;
        }

        public synchronized int available() {
            return count - pos;
        }

        public synchronized void reset() {
            pos = 0;
        }
        
    }
    
    public FileBufferDoc(File file, Document doc) {
        super(file);
        this.doc = doc;
    }
    
    private void fireDocumentChanged() {
        EventListener[] list = listeners.getListeners(ChangeListener.class);
        if( list.length > 0 ) {
            ChangeEvent ev = new ChangeEvent(this);
            for( int i = 0; i < list.length; i++ ) {
                ((ChangeListener) list[i]).stateChanged(ev);
            }
        }
        // TODO: think over when do invalidate? before informing listeners or after
        if (TraceFlags.USE_AST_CACHE) {
            CacheManager.getInstance().invalidate(getFile().getAbsolutePath());
        } else {
            APTDriver.getInstance().invalidateAPT(this);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        if (listeners.getListenerCount() == 0)
        {
            docListener = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    fireDocumentChanged();
                }
                public void removeUpdate(DocumentEvent e) {
                    fireDocumentChanged();
                }
                public void changedUpdate(DocumentEvent e) {
                    // Add/remove annotation shouldn't result in reparse.
                    //fireDocumentChanged();
                }
            };
            doc.addDocumentListener(docListener);
        }
        listeners.add(ChangeListener.class, listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(ChangeListener.class, listener);
        if (listeners.getListenerCount() == 0)
        {
            doc.removeDocumentListener(docListener);
            docListener = null;
        }
    }
    
    //public boolean isSaved() {
    //}
    
    private IOException convert(BadLocationException e) {
        IOException ioe = new  IOException(e.getMessage());
        ioe.setStackTrace(e.getStackTrace());
        return ioe;
    }

    public InputStream getInputStream() throws IOException {
        
        try {
            String text = doc.getText(0, doc.getLength());
            return new StringInputStream(text);
        }
        catch( BadLocationException e ) {
            throw convert(e);
        }
    }
    
    public String getText() throws IOException {
        try {
            return doc.getText(0, doc.getLength());
        }
        catch( BadLocationException e ) {
            //e.printStackTrace(System.err);
            throw convert(e);
        }
    }
    
    public String getText(int start, int end) throws IOException {
        try {
            return doc.getText(start, end - start);
        }
        catch( BadLocationException e ) {
            //e.printStackTrace(System.err);
            throw convert(e);
        }
    }
    
    public int getLength() {
        return doc.getLength();
    }

    public boolean isFileBased() {
        return false;
    }
}
