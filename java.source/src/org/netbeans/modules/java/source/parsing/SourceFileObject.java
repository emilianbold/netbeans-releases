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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;

/**
 *
 * @author Tomas Zezula
 */
public class SourceFileObject implements DocumentProvider, FileObjects.InferableJavaFileObject {    
    
    final FileObject file;
    final FileObject root;
    private final Kind kind;
    private URI uri;        //Cache for URI
    private String text;
    private TokenHierarchy<?> tokens;
    private final JavaFileFilterImplementation filter;
    private static Logger log = Logger.getLogger(SourceFileObject.class.getName());
    
    public static SourceFileObject create (final FileObject file, final FileObject root) {        
        try {
            return new SourceFileObject (file, root, null, false);
        } catch (IOException ioe) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, ioe.getMessage(), ioe);
            return null;
        }        
    }
    
    public SourceFileObject (final FileObject file, final FileObject root, final JavaFileFilterImplementation filter, final CharSequence content) throws IOException {
        this (file, root, filter);
        update(content);
    }
    
    //where
    private String toString (final CharSequence c) {
        if (c instanceof String) {
            return (String) c;
        }
        else {
            return c.toString();
        }
    }
    
    /** Creates a new instance of SourceFileObject */
    public SourceFileObject (final FileObject file, final FileObject root, final JavaFileFilterImplementation filter, final boolean renderNow) throws IOException {
        this (file, root, filter);
        if (renderNow && this.kind != Kind.CLASS) {
            getCharContentImpl(true);
        }
    }
    
    private SourceFileObject (final FileObject file, final FileObject root, final JavaFileFilterImplementation filter) {
        assert file != null;
        this.file = file;
        this.root = root;
        this.filter = filter;
        String ext = this.file.getExt();        
        this.kind = filter == null ? FileObjects.getKind(ext) : Kind.SOURCE; //#141411
    }

    
    public void update () throws IOException {
        if (this.kind != Kind.CLASS) {
            getCharContentImpl(true);
        }
    }
    
    public void update (final CharSequence content) throws IOException {
        if (content == null) {
            update();
        }
        else {            
            final CharBuffer charBuffer = CharBuffer.wrap (content);
            this.text = toString(content);
            tokens = TokenHierarchy.create(charBuffer, false, JavaTokenId.language(), null, null);
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
            return getCharContentImpl(false);
        }
    }
    
    public TokenHierarchy<?> getTokenHierarchy() throws IOException {
        if (tokens == null)
            getCharContentImpl(false);
        
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
                          if (log.isLoggable(Level.SEVERE))
                              log.log(Level.SEVERE, e.getMessage(), e);
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
                          if (log.isLoggable(Level.SEVERE))
                              log.log(Level.SEVERE, e.getMessage(), e);
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
                if (log.isLoggable(Level.SEVERE))
                    log.log(Level.SEVERE, e.getMessage(), e);
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
    
    public String inferBinaryName () {
        if (root == null) {
            return null;
        }
        final String relativePath = FileUtil.getRelativePath(root,file);
        assert relativePath != null : "root=" + FileUtil.getFileDisplayName(root) + ", file=" + FileUtil.getFileDisplayName(file);
        final int index = relativePath.lastIndexOf('.');
        assert index > 0;
        final String result = relativePath.substring(0,index).replace('/','.');
        return result;
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
    
    private CharBuffer getCharContentImpl (boolean assign) throws IOException {
        final Document doc = getDocument(isOpened());
	char[] result = null;
        int length = 0;
        if (doc == null) {
	    Reader in = this.openReader (true);
            int red = 0, rv;
            try {
                int len = (int)this.file.getSize();
                result = new char [len+1];
                while ((rv=in.read(result,red,len-red))>0 && (red=red+rv)<len);
            } finally {
                in.close();
            }
            int j=0;
            for (int i=0; i<red;i++) {
                if (result[i] =='\r') {                                          //NOI18N
                    if (i+1>=red || result[i+1]!='\n') {                         //NOI18N
                        result[j++] = '\n';                                      //NOI18N
                    }
                }
                else {
                    result[j++] = result[i];
                }
            }
            length = j;
        }
        else {            
            final CharSequence[] _text = new CharSequence[1];
            doc.render(new Runnable() {
                public void run () {
                    try {
                        _text[0] = doc.getText(0, doc.getLength());
                    } catch (BadLocationException e) {
                        if (log.isLoggable(Level.SEVERE))
                            log.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            });
            if (_text[0] != null) {
                if (filter != null) {
                    _text[0] = filter.filterCharSequence(_text[0]);
                }
                int len = _text[0].length();
                result = new char[len+1];
                _text[0].toString().getChars(0,len,result,0);
                length = len;
            }
        }
	result[length]='\n'; //NOI18N
        
        String str = new String(result,0,length);
	CharBuffer charBuffer = CharBuffer.wrap (str);
        tokens = TokenHierarchy.create(charBuffer, false, JavaTokenId.language(), null, null); //TODO: .createSnapshot();
        if (assign) text = str;
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
                                if (log.isLoggable(Level.SEVERE))
                                    log.log(Level.SEVERE, e.getMessage(), e);
                            }
                            catch (UnsupportedEncodingException ee) {
                                if (log.isLoggable(Level.SEVERE))
                                    log.log(Level.SEVERE, ee.getMessage(), ee);
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
