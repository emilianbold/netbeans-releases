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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;

/**
 *
 * @author Tomas Zezula
 */
public class SourceFileObject implements JavaFileObject, DocumentProvider {    
    
    final FileObject file;
    final FileObject root;
    private final Kind kind;
    private URI uri;        //Cache for URI
    private String text;
    private TokenHierarchy<Void> tokens;
    private final JavaFileFilterImplementation filter;
    
    public static SourceFileObject create (final FileObject file, final FileObject root) {        
        try {
            return new SourceFileObject (file, root, null, false);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return null;
        }        
    }
    
    /** Creates a new instance of SourceFileObject */
    public SourceFileObject (final FileObject file, final FileObject root, final JavaFileFilterImplementation filter, final boolean renderNow) throws IOException {
        assert file != null;
        this.file = file;
        this.root = root;
        this.filter = filter;
        String ext = this.file.getExt();        
        this.kind = FileObjects.getKind(ext);        
        if (renderNow && this.kind == Kind.SOURCE) {
            text = getCharContentImpl().toString();
        }
    }

    

    public boolean isNameCompatible (String simplename, JavaFileObject.Kind kind) {
        assert simplename != null;
        return this.kind == kind && this.getNameWithoutExtension().equals(simplename);
    }

    public CharBuffer getCharContent(boolean ignoreEncodingErrors) throws IOException {
        String _text;
        synchronized (this) {
            _text = this.text;
        }
        if (_text != null) {
            return CharBuffer.wrap(_text);
        }
        else {
            return getCharContentImpl();
        }
    }
    
    public TokenHierarchy<Void> getTokenHierarchy() throws IOException {
        if (tokens == null)
            getCharContentImpl();
        
        return tokens;
    }
   

    public java.io.Writer openWriter() throws IOException {
        return new OutputStreamWriter (this.openOutputStream(), FileEncodingQuery.getEncoding(file));
    }

    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {        
        String _text;
        synchronized (this) {
            _text = text;
        }
        if (_text != null) {
            return new StringReader (_text);
        }
        else {
            final Document doc = getDocument(isOpened());
            if (doc == null) {
                Reader r = new InputStreamReader (new BufferedInputStream (this.file.getInputStream()),FileEncodingQuery.getEncoding(file));
                if (filter != null) {
                    r = filter.filterReader(r);
                }
                return r;
            }
            else {
                final StringBuilder builder = new StringBuilder ();
                doc.render(new Runnable() {
                    public void run () {
                      try {
                            builder.append(doc.getText(0, doc.getLength()));
                        } catch (BadLocationException e) {
                            ErrorManager.getDefault().notify(e);
                        }  
                    }
                });
                return new StringReader (builder.toString());
            }
        }       
    }

    public java.io.OutputStream openOutputStream() throws IOException {
        final StyledDocument doc = getDocument(isOpened());
        if (doc == null) {
            return new LckStream (this.file);
        }
        else {
            return new DocumentStream (doc);
        }
    }

    public InputStream openInputStream() throws IOException {
        String _text;
        synchronized (this) {
            _text = text;
        }
        if (_text != null) {
            return new ByteArrayInputStream (_text.getBytes());
        }
        else {
            final Document doc = getDocument(isOpened());
            if (doc == null) {
                return this.file.getInputStream();
            }
            else {
                final StringBuilder builder = new StringBuilder ();
                doc.render(new Runnable() {
                    public void run () {
                      try {
                            builder.append(doc.getText(0, doc.getLength()));
                        } catch (BadLocationException e) {
                            ErrorManager.getDefault().notify(e);
                        }  
                    }
                });
                return new ByteArrayInputStream (builder.toString().getBytes());
            }
        }
    }

    public boolean delete() {
        if (isModified()!=null) {
            //If the file is modified in editor do not delete it
            return false;
        }
        else {
            try {
                FileLock lock = this.file.lock();
                try {
                    this.file.delete (lock);
                    return true;
                }
                finally {
                    lock.releaseLock();
                }
            } catch (IOException e) {
                return false;
            }
        }
    }


    public JavaFileObject.Kind getKind() {
        return this.kind;
    }

    public String getName() {
       return this.file.getNameExt();
    }

    public String getNameWithoutExtension() {
        return this.file.getName();
    }
    
    public synchronized URI toUri () {
        if (this.uri == null) {
            try {            
                this.uri = URI.create(this.file.getURL().toExternalForm());
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(e);                
            }
        }
        return this.uri;
    }

    /**
     * Returns the mtime of the file, in the case of opened
     * modified file, the mtime is not known, this method returns
     * the current time.
     */
    public long getLastModified() {
        if (isModified()==null) {
            try {
                //Prefer class files to packed sources, the packed sources may have wrong time stamps.
                if (this.file.getFileSystem() instanceof JarFileSystem) {
                    return 0L;
                }
            } catch (FileStateInvalidException e) {
                //Handled below
            }
            return this.file.lastModified().getTime();
        }
        else {
            return System.currentTimeMillis();
        }
    }
    
    public NestingKind getNestingKind() {
        return null;
    }

    public Modifier getAccessLevel() {
        return null;
    }
    
    public @Override String toString () {
        return this.file.getPath();
    }
    
    public @Override boolean equals (Object other) {
        if (other instanceof SourceFileObject) {
            SourceFileObject otherSource = (SourceFileObject) other;
            return this.file.equals (otherSource.file);
        }
        else {
            return false;
        }
    }
    
    public @Override int hashCode () {
        return this.file.hashCode();
    }
    
    public StyledDocument getDocument() {
        EditorCookie ec = isOpened();
        return ec != null ? getDocument(ec) : null;       
    }
    
    public void runAtomic(final Runnable r) {
        assert r != null;
        final StyledDocument doc = getDocument();
        if (doc == null) {
            throw new IllegalStateException ();
        }
        else {
            NbDocument.runAtomic(doc,r);
        }
    }
    
    @SuppressWarnings ("unchecked")     // NOI18N
    private EditorCookie isModified () {
        DataObject.Registry regs = DataObject.getRegistry();
        Set<DataObject> modified = regs.getModifiedSet();
        for (DataObject dobj : modified) {
            if (this.file.equals(dobj.getPrimaryFile())) {
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                return ec;
            }
        }
        return null;
    }
    
    public EditorCookie isOpened () {
        try {
            if (this.kind == JavaFileObject.Kind.CLASS) {
                return null;
            }
            DataObject dobj = DataObject.find(this.file);
            return dobj.getCookie(EditorCookie.class);
        } catch (DataObjectNotFoundException dnf) {
            return null;
        }
    }
    
    private CharBuffer getCharContentImpl () throws IOException {
        final Document doc = getDocument(isOpened());
	final char[][] result = new char[1][];
        final int[] length = new int[1];
        if (doc == null) {
	    Reader in = this.openReader (true);
            int red = 0, rv;
            try {
                int len = (int)this.file.getSize();
                result[0] = new char [len+1];
                while ((rv=in.read(result[0],red,len-red))>0 && (red=red+rv)<len);
            } finally {
                in.close();
            }
            int j=0;
            for (int i=0; i<red;i++) {
                if (result[0][i] =='\r') {                                          //NOI18N
                    if (i+1>=red || result[0][i+1]!='\n') {                         //NOI18N
                        result[0][j++] = '\n';                                      //NOI18N
                    }
                }
                else {
                    result[0][j++] = result[0][i];
                }
            }
            length[0] = j;
        }
        else {            
            final CharSequence[] _text = new CharSequence[1];
            doc.render(new Runnable() {
                public void run () {
                    try {
                        _text[0] = doc.getText(0, doc.getLength());
                    } catch (BadLocationException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            });
            if (_text[0] != null) {
                if (filter != null) {
                    _text[0] = filter.filterCharSequence(_text[0]);
                }
                int len = _text[0].length();
                result[0] = new char[len+1];
                _text[0].toString().getChars(0,len,result[0],0);
                length[0] = len;
            }
        }
	result[0][length[0]]='\n'; //NOI18N
	CharBuffer charBuffer = CharBuffer.wrap (result[0],0,length[0]);
        tokens = TokenHierarchy.create(charBuffer, true, JavaTokenId.language(), null, null); //TODO: .createSnapshot();
        return charBuffer;
    }
            
    private static StyledDocument getDocument (EditorCookie ec) {
        return ec == null ? null : ec.getDocument();
    }    
    
    
    private class LckStream extends OutputStream {
        
        private final OutputStream delegate;
        private final FileLock lock;
        
        public LckStream (final FileObject fo) throws IOException {
            assert fo != null;
            this.lock = fo.lock();
            try {
                this.delegate = fo.getOutputStream (this.lock);
            } finally {
                if (this.delegate == null) {
                    this.lock.releaseLock();
                }
            }
        }

        public @Override void write(byte[] b, int off, int len) throws IOException {
            this.delegate.write(b, off, len);
        }

        public @Override void write(byte[] b) throws IOException {
            this.delegate.write(b);
        }

        public void write(int b) throws IOException {
            this.delegate.write (b);
        }

        public @Override void close() throws IOException {
            try {
                this.delegate.close();
            } finally {
                this.lock.releaseLock();
                synchronized (SourceFileObject.this) {
                    text = null;
                }
            }            
        }                
    }
    
    private class DocumentStream extends OutputStream {
        
        private static final int BUF_SIZ=2048;
        
        private final StyledDocument doc;
        private byte[] data;
        private int pos;
            
        public DocumentStream (final StyledDocument doc) {
            assert doc != null;
            this.doc = doc;
            this.data = new byte[BUF_SIZ];
            this.pos = 0;
        }
        
        public synchronized @Override void write(byte[] b, int off, int len) throws IOException {
            ensureSize (len);
            System.arraycopy(b,off,this.data,this.pos,len);
            this.pos+=len;
        }

        public synchronized @Override void write(byte[] b) throws IOException {
            ensureSize (b.length);
            System.arraycopy(b,0,this.data,this.pos,b.length);
            this.pos+=b.length;
        }

        public synchronized void write(int b) throws IOException {
            ensureSize (1);
            this.data[this.pos++]=(byte)(b&0xff);
        }
        
        private void ensureSize (int delta) {
            int requiredLength = this.pos + delta;
            if (this.data.length<requiredLength) {
                int newSize = this.data.length + BUF_SIZ;
                while (newSize<requiredLength) {
                    newSize+=BUF_SIZ;
                }
                byte[] newData = new byte[newSize];
                System.arraycopy(this.data,0,newData,0,this.pos);
                this.data = newData;
            }
        }
        
        public synchronized @Override void close() throws IOException {
            try {
                NbDocument.runAtomic(this.doc,
                    new Runnable () {
                        public void run () {
                            try {
                                doc.remove(0,doc.getLength());
                                //todo: use new String(data,0,pos,FileEncodingQuery.getEncoding(file)) on JDK 6.0
                                doc.insertString(0,new String(data,0,pos,FileEncodingQuery.getEncoding(file).name()),null);
                            } catch (BadLocationException e) {
                                ErrorManager.getDefault().notify(e);
                            }
                            catch (UnsupportedEncodingException ee) {
                                ErrorManager.getDefault().notify (ee);
                        }
                        }
                    });
            } finally {
                synchronized (SourceFileObject.this) {
                    text = null;
                }
            }
        }        
    }
}
