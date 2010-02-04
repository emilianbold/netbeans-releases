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

package org.netbeans.modules.java.source.parsing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
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
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.parsing.api.Source;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;

/**
 *
 * @author Tomas Zezula
 */
public class SourceFileObject implements DocumentProvider, InferableJavaFileObject {

    final Handle handle;
    private final Kind kind;
    private URI uri;        //Cache for URI
    private volatile String text;
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
        this (new Handle(file, root), filter);
        update(content);
    }

    /** Creates a new instance of SourceFileObject */
    public SourceFileObject (final FileObject file, final FileObject root, final JavaFileFilterImplementation filter, final boolean renderNow) throws IOException {
        this (new Handle(file, root), filter);
        if (renderNow) {
            update();
        }
    }

    public SourceFileObject (final Handle handle, final JavaFileFilterImplementation filter) {
        assert handle != null;
        this.handle = handle;
        this.filter = filter;
        String ext = this.handle.getExt();
        this.kind = filter == null ? FileObjects.getKind(ext) : Kind.SOURCE; //#141411
    }

    public void update () throws IOException {
        if (this.kind != Kind.CLASS) {
            //Side effect assigns the text
            getContent(true);
        }
    }

    public void update (final CharSequence content) throws IOException {
        if (content == null) {
            update();
        }
        else {
            this.text = toString(content);
        }
        this.tokens = null;
    }


    public boolean isNameCompatible (String simplename, JavaFileObject.Kind kind) {
        assert simplename != null;
        return this.kind == kind && this.getNameWithoutExtension().equals(simplename);
    }

    public CharBuffer getCharContent(boolean ignoreEncodingErrors) throws IOException {
        String _text = this.text;
        if (_text == null) {
            _text = getContent(false);
        }
        return CharBuffer.wrap(_text);
    }

    public TokenHierarchy<?> getTokenHierarchy() throws IOException {
        if (this.tokens == null) {
            final CharBuffer charBuffer = getCharContent(true);
            this.tokens = TokenHierarchy.create(charBuffer, false, JavaTokenId.language(), null, null); //TODO: .createSnapshot();
        }
        return this.tokens;
    }

    public java.io.Writer openWriter() throws IOException {
        final FileObject file = handle.resolveFileObject(true);
        if (file == null) {
            throw new IOException("Cannot create file: " + toString());   //NOI18N
        }
        return new OutputStreamWriter (this.openOutputStream(), FileEncodingQuery.getEncoding(file));
    }

    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        String _text = text;
        if (_text == null) {
            _text = getContent(false);
        }
        return new StringReader(_text);
    }

    public java.io.OutputStream openOutputStream() throws IOException {
        final FileObject file = handle.resolveFileObject(true);
        if (file == null) {
            throw new IOException("Cannot create file: " + toString());   //NOI18N
        }
        final StyledDocument doc = getDocument();
        if (doc == null) {
            return new LckStream (file);
        }
        else {
            return new DocumentStream (doc);
        }
    }

    public InputStream openInputStream() throws IOException {
        String _text = text;
        if (_text == null) {
            _text= getContent(false);
        }
        return new ByteArrayInputStream (_text.getBytes());
    }

    public boolean delete() {
        if (isModified()!=null) {
            //If the file is modified in editor do not delete it
            return false;
        }
        else {
            final FileObject file = handle.resolveFileObject(false);
            if (file == null) {
                return false;
            }
            try {
                FileLock lock = file.lock();
                try {
                    file.delete (lock);
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
       return this.handle.getName(true);
    }

    public String getNameWithoutExtension() {
        return this.handle.getName(false);
    }

    public synchronized URI toUri () {
        if (this.uri == null) {
            try {
                this.uri = URI.create(this.handle.getURL().toExternalForm());
            } catch (IOException e) {
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
        EditorCookie ec;
        if ((ec=isModified())==null) {
            return getFileLastModified();
        }
        else {
            final Document doc = ec.getDocument();
            return doc != null ?
                DocumentUtilities.getDocumentTimestamp(doc) :
                getFileLastModified();
        }
    } //where

    private long getFileLastModified() {
        final FileObject file = handle.resolveFileObject(false);
        try {
            //Prefer class files to packed sources, the packed sources may have wrong time stamps.
            if (file == null || file.getFileSystem() instanceof JarFileSystem) {
                return 0L;
            }
        } catch (FileStateInvalidException e) {
            //Handled below
        }
        return file.lastModified().getTime();
    }

    public NestingKind getNestingKind() {
        return null;
    }

    public Modifier getAccessLevel() {
        return null;
    }

    public String inferBinaryName () {
        if (handle.root == null) {
            return null;
        }
        final String relativePath = handle.getRelativePath();
        assert relativePath != null : "root=" + FileUtil.getFileDisplayName(handle.root) + ", file=" + toString();
        final int index = relativePath.lastIndexOf('.');
        assert index > 0;
        final String result = relativePath.substring(0,index).replace('/','.');
        return result;
    }

    public @Override String toString () {
        final URI uri = toUri();
        try {
            final File file = new File (uri);
            return file.getAbsolutePath();
        } catch (IllegalArgumentException iae) {
            return uri.toString();
        }
    }

    public @Override boolean equals (Object other) {
        if (other instanceof SourceFileObject) {
            SourceFileObject otherSource = (SourceFileObject) other;
            return this.handle.equals (otherSource.handle);
        }
        else {
            return false;
        }
    }

    public @Override int hashCode () {
        return this.handle.hashCode();
    }

    public StyledDocument getDocument() {
        final FileObject file = handle.resolveFileObject(false);
        if (file == null) {
            return null;
        }
        final Source src = Source.create(file);
        if (src == null) {
            return null;
        }
        final Document doc = src.getDocument(false);
        return (doc instanceof StyledDocument) ?  ((StyledDocument)doc) : null;
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
        final FileObject file = handle.resolveFileObject(false);
        if (file == null) {
            return null;
        }
        DataObject.Registry regs = DataObject.getRegistry();
        Set<DataObject> modified = regs.getModifiedSet();
        for (DataObject dobj : modified) {
            if (file.equals(dobj.getPrimaryFile())) {
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                return ec;
            }
        }
        return null;
    }

    private String getContent(boolean assign) throws IOException {
        final FileObject file = handle.resolveFileObject(false);
        if (file == null) {
            throw new FileNotFoundException("Cannot open file: " + toString());
        }
        final Source source = Source.create(file);
        if (source == null) {
            throw new IOException("No source for: " + FileUtil.getFileDisplayName(file));   //NOI18N
        }
        CharSequence content = toString(source.createSnapshot().getText());
        if (source.getDocument(false)!=null) {
            //Snapshot is from Document we have to filter it
            if (filter != null) {
                content = filter.filterCharSequence(content);
            }
        }
        String result = toString(content);
        if (assign) {
            this.text = result;
        }
        return result;
    }

    private String toString (final CharSequence c) {
        if (c instanceof String) {
            return (String) c;
        }
        else {
            return c.toString();
        }
    }

    public static class Handle {

        protected final FileObject root;
        protected FileObject file;

        protected Handle(final FileObject root) {
            this.root = root;
        }

        public Handle(final FileObject file, final FileObject root) {
            assert file != null;
            this.file = file;
            this.root = root;
        }

        protected FileObject resolveFileObject (boolean write) {
            return file;
        }

        protected URL getURL() throws IOException {
            return file == null ? null : file.getURL();
        }

        protected String getExt() {
            return file == null ? null : file.getExt();
        }

        protected String getName(boolean includeExtension) {
            return file == null ? null : includeExtension ? file.getNameExt() : file.getName();
        }

        protected String getRelativePath() {
            return file == null ? null : FileUtil.getRelativePath(root,file);
        }

        @Override
        public int hashCode() {
            return file == null ? 0 : file.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Handle other = (Handle) obj;
            return this.file == null ? other.file == null : this.file.equals(other.file);
        }
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
                text = null;                
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
                                doc.insertString(0,new String(data,0,pos,FileEncodingQuery.getEncoding(handle.resolveFileObject(false))),null);
                            } catch (BadLocationException e) {
                                if (log.isLoggable(Level.SEVERE))
                                    log.log(Level.SEVERE, e.getMessage(), e);
                            }
                        }
                    });
            } finally {
                text = null;
            }
        }
    }
}
