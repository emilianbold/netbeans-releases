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

package org.netbeans.modules.parsing.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.CharBuffer;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceFlags;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;


/**
 * Source represents some part of text. Source can be created from file or document
 * opened in editor. And source can represent some block of code written 
 * in different language embedded inside some top level language too. It can contain
 * some generated parts of code that is not contained in the original 
 * file. Source is read only and immutable. It means that source created 
 * from document opened in editor contains some copy of original text. 
 * You do not need to call source methods under 
 * any locks, but on other hand source may not be up to date.
 *
 * Following example shows how to create Source for block of code 
 * embedded in other source:
 * {@link org.netbeans.modules.parsing.spi.EmbeddingProvider}:
 * 
 * <pre> 
 *           int start = findStartJavaOffset (source);
 *           int length = getJavaBlockLength (source, start);
 *           return source.create (Arrays.asList (new Source[] {
 *               source.create ("some prefix code", "text/x-java"),
 *               source.create (start, length, "text/x-java"),
 *               source.create ("some postfix code", "text/x-java")
 *           })));
 * </pre>
 * 
 * @author Jan Jancura
 */
public final class Source {
    
    private static Map<FileObject,Reference<Source>> instances = new WeakHashMap<FileObject,Reference<Source>>();
    
    private String          mimeType;
    private Document        document;
    private FileObject      fileObject;
    
   
    Source (
        String              mimeType, 
        Document            document,
        FileObject          fileObject
    ) {
        this.mimeType =     mimeType;
        this.document =     document;
        this.fileObject =   fileObject;
    }
    
    /**
     * Creates source for given file.
     * 
     * @param fileObject    A file object.
     * @return source for given file or null when the given file doesn't exist.
     */
    public static Source create (
        FileObject          fileObject
    ) {
        Parameters.notNull("fileObject", fileObject);
        if (!fileObject.isValid()) {
            return null;
        }
        synchronized (Source.class) {
            Reference<Source> ref = instances.get(fileObject);
            Source result = null;
            if (ref != null) {
                result = ref.get();
            }
            if (result == null) {
                result = new Source (
                    fileObject.getMIMEType (),
                    null, 
                    fileObject
                );
                ref = new WeakReference<Source>(result);
                instances.put(fileObject, ref);
            }
            return result;
        }
        
    }
    
    /**
     * Creates source for given document.
     * 
     * @param document      A document.
     * @return              source for given document.
     */
    public static Source create (
        Document            document
    ) {
        FileObject fileObject = NbEditorUtilities.getFileObject (document);
        return Source.create (fileObject);
    }

    /**
     * Returns source mime type.
     * 
     * @return              this source mime type.
     */
    public String getMimeType (
    ) {
        return mimeType;
    }
    
    /**
     * Returns document this source has originally been created from or null.
     * 
     * @return              a document.
     */
    public Document getDocument () {
        if (document != null)
            return document;
        try {
            DataObject dataObject = DataObject.find (fileObject);
            EditorCookie editorCookie = dataObject.getLookup().lookup (EditorCookie.class);
            return editorCookie.getDocument ();
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }
    
    /**
     * Returns file this source has originally been created from or null.
     * 
     * @return              a file.
     */
    public FileObject getFileObject () {
        return fileObject;
    }

    public Snapshot createSnapshot () {
        Document document = getDocument ();
        if (document != null) {
            try {
                if (document instanceof AbstractDocument)
                    ((AbstractDocument) document).readLock ();
                String text = null;
                try {
                    text = document.getText (0, document.getLength ());
                } finally {
                    if (document instanceof AbstractDocument)
                        ((AbstractDocument) document).readUnlock ();
                }
                return new Snapshot (
                    text, this, mimeType, new int[][] {new int[] {0, 0}}
                );
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace (ex);
                return new Snapshot (
                    "", this, mimeType, new int[][] {new int[] {0, 0}}
                );
            }
        }
        FileObject fileObject = getFileObject ();
        try {
            BufferedReader bufferedReader = new BufferedReader (new InputStreamReader (fileObject.getInputStream (),FileEncodingQuery.getEncoding(fileObject)));
            CharBuffer charBuffer = CharBuffer.allocate (4096);
            StringBuilder sb = new StringBuilder ();
            int i = bufferedReader.read (charBuffer);
            while (i > 0) {
                sb.append (charBuffer);
                i = bufferedReader.read (charBuffer);
            }
            return new Snapshot (
                sb.toString (), this, mimeType, new int[][] {new int[] {0, 0}}
            );
        } catch (IOException ex) {
            Exceptions.printStackTrace (ex);
            return new Snapshot (
                "", this, mimeType, new int[][] {new int[] {0, 0}}
            );
        }
    }
    
    
    // accessor ................................................................
    
    static {
        SourceAccessor.setINSTANCE(new MySourceAccessor());
    }
    
    private final Set<SourceFlags> 
                            flags = EnumSet.of(SourceFlags.INVALID);
    private volatile Parser cachedParser;
    private SchedulerEvent  schedulerEvent;
    
    
    private static class MySourceAccessor extends SourceAccessor {

        @Override
        public Set<SourceFlags> getFlags(Source source) {
            assert source != null;
            return source.flags;
        }
        
        @Override
        public Parser getParser(Source source) {
            assert source != null;
            return source.cachedParser;
        }

        @Override
        public void setParser(Source source, Parser parser) throws IllegalStateException {
            assert source != null;
            assert parser != null;
            synchronized (source) {
                if (source.cachedParser != null) {
                    throw new IllegalStateException();
                }
                source.cachedParser = parser;
            }
        }

        @Override
        public void setEvent (Source source, SchedulerEvent event) {
            assert source != null;
            assert event != null;
            synchronized (source) {
                if (event != null) {
                    throw new IllegalStateException();
                }
                source.schedulerEvent = event;
            }
        }

        @Override
        public SchedulerEvent getEvent(Source source) {
            return source.schedulerEvent;
        }
    }
}



