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
package org.netbeans.modules.gsfret.source.parsing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.CharBuffer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
//import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.gsf.api.DataLoadersBridge;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.text.NbDocument;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Tomas Zezula
 */
public class SourceFileObject/* implements DocumentProvider*/ {    
    
    final FileObject file;    
//    private final Kind kind;
    private URI uri;        //Cache for URI
    private String text;
    private TokenHierarchy tokens;
    
    public static SourceFileObject create (final FileObject file) {        
        try {
            return new SourceFileObject (file, false);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return null;
        }        
    }
    
    /** Creates a new instance of SourceFileObject */
    public SourceFileObject (final FileObject file, final boolean renderNow) throws IOException {
        assert file != null;
        this.file = file;
//        String ext = this.file.getExt();
//        if (FileObjects.JAVA.equalsIgnoreCase(ext)) { //NOI18N
//            this.kind = Kind.SOURCE;
//        }
//        else if (FileObjects.CLASS.equalsIgnoreCase(ext)) {   //NOI18N
//            this.kind = Kind.CLASS;
//        }
//        else if (FileObjects.HTML.equalsIgnoreCase(ext)) {    //NOI18N
//            this.kind = Kind.HTML;
//        }
//        else {
//            this.kind = Kind.OTHER;
//        }
        if (renderNow) {
            text = getCharContentImpl().toString();
        }
    }

    

//    public boolean isNameCompatible (String simplename, JavaFileObject.Kind kind) {
//        assert simplename != null;
//        return this.kind == kind && this.getNameWithoutExtension().equals(simplename);
//    }

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
    
    public TokenHierarchy getTokenHierarchy() throws IOException {
        if (tokens == null)
            getCharContentImpl();
        
        return tokens;
    }
   

    public java.io.Writer openWriter() throws IOException {
        return new OutputStreamWriter (this.openOutputStream(),encodingName);
    }

    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new InputStreamReader (this.openInputStream(),encodingName);
    }

    static final String encodingName = new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding();            

    public java.io.OutputStream openOutputStream() throws IOException {
        final StyledDocument doc = DataLoadersBridge.getDefault().getDocument(file);
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
            final Document doc = DataLoadersBridge.getDefault().getDocument(file);
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


//    public JavaFileObject.Kind getKind() {
//        return this.kind;
//    }
//
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
            return this.file.lastModified().getTime();
        }
        else {
            return System.currentTimeMillis();
        }
    }
    
//    public NestingKind getNestingKind() {
//        return null;
//    }
//
//    public Modifier getAccessLevel() {
//        return null;
//    }
//    
//    public @Override String toString () {
//        return this.file.getPath();
//    }
    
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
        return DataLoadersBridge.getDefault().getDocument(file);     
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
        return DataLoadersBridge.getDefault().isModified(file);
    }
    
    public EditorCookie isOpened () {
        //if (this.kind == JavaFileObject.Kind.CLASS) {
        //    return null;
        //}
        return (EditorCookie) DataLoadersBridge.getDefault().getSafeCookie(file, EditorCookie.class);
    }
    
    private CharBuffer getCharContentImpl () throws IOException {
        final Document doc = DataLoadersBridge.getDefault().getDocument(file);
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
            doc.render(new Runnable() {
                public void run () {
                  try {
                        int len = doc.getLength();
                        result[0] = new char[len+1];
                        doc.getText(0,len).getChars(0,len,result[0],0);
                        length[0] = len;
                    } catch (BadLocationException e) {
                        ErrorManager.getDefault().notify(e);
                    }  
                }
            });            
        }
        result[0][length[0]]='\n'; //NOI18N
        CharBuffer charBuffer = CharBuffer.wrap (result[0],0,length[0]);
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(this.file.getMIMEType());
        if (language != null && language.getGsfLanguage() != null) {
            org.netbeans.api.lexer.Language lexerLanguage = (org.netbeans.api.lexer.Language)language.getGsfLanguage().getLexerLanguage();
            InputAttributes attributes = new InputAttributes();
            attributes.setValue(lexerLanguage, FileObject.class, file, false);
            tokens = TokenHierarchy.create(charBuffer, true, lexerLanguage, null, attributes); //TODO: .createSnapshot();
        }
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
                                doc.insertString(0,new String(data,0,pos,encodingName),null);
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
