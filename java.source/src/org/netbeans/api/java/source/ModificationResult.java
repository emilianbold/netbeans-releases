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

package org.netbeans.api.java.source;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.java.source.JavaSourceSupportAccessor;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.PositionRef;

/**
 * Class that collects changes built during a modification task run.
 *
 * @author Dusan Balek
 */
public final class ModificationResult {

    private JavaSource js;
    Map<FileObject, List<Difference>> diffs = new HashMap<FileObject, List<Difference>>();
    
    /** Creates a new instance of ModificationResult */
    ModificationResult(final JavaSource js) {
        this.js = js;
    }

    // API of the class --------------------------------------------------------
    
    public Set<? extends FileObject> getModifiedFileObjects() {
        return diffs.keySet();
    }
    
    public List<? extends Difference> getDifferences(FileObject fo) {
        return diffs.get(fo);
    }
    
    /**
     * Once all of the changes have been collected, this method can be used
     * to commit the changes to the source files
     */
    public void commit() throws IOException {
        try {
            RepositoryUpdater.getDefault().lockRU();
            for (Map.Entry<FileObject, List<Difference>> me : diffs.entrySet()) {
                commit(me.getKey(), me.getValue(), null);
            }
        } finally {
            RepositoryUpdater.getDefault().unlockRU();
            Set<FileObject> alreadyRefreshed = new HashSet<FileObject>();
            if (this.js != null) {
                this.js.revalidate();
                alreadyRefreshed.addAll(js.getFileObjects());
            }
            for (FileObject currentlyVisibleInEditor : JavaSourceSupportAccessor.ACCESSOR.getVisibleEditorsFiles()) {
                if (!alreadyRefreshed.contains(currentlyVisibleInEditor)) {
                    JavaSource source = JavaSource.forFileObject(currentlyVisibleInEditor);                
                    if (source != null) {
                        source.revalidate();
                    }
                }
            }
        }
    }
            
    private void commit(final FileObject fo, final List<Difference> differences, Writer out) throws IOException {
        DataObject dObj = DataObject.find(fo);
        EditorCookie ec = dObj != null ? dObj.getCookie(org.openide.cookies.EditorCookie.class) : null;
        // if editor cookie was found and user does not provided his own
        // writer where he wants to see changes, commit the changes to 
        // found document.
        if (ec != null && out == null) {
            Document doc = ec.getDocument();
            if (doc != null) {
                if (doc instanceof BaseDocument)
                    ((BaseDocument)doc).atomicLock();
                try {
                    for (Difference diff : differences) {
                        if (diff.isExcluded())
                            continue;
                        try {
                            switch (diff.getKind()) {
                                case INSERT:
                                    doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                    break;
                                case REMOVE:
                                    doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                    break;
                                case CHANGE:
                                    doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                    doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                    break;
                            }
                        } catch (BadLocationException ex) {
                            IOException ioe = new IOException();
                            ioe.initCause(ex);
                            throw ioe;
                        }
                    }
                } finally {
                    if (doc instanceof BaseDocument)
                        ((BaseDocument)doc).atomicUnlock();
                }
                return;
            }
        }
        InputStream ins = null;
        ByteArrayOutputStream baos = null;           
        Reader in = null;
        try {
            Charset encoding = FileEncodingQuery.getEncoding(fo);
            ins = fo.getInputStream();
            baos = new ByteArrayOutputStream();
            FileUtil.copy(ins, baos);

            ins.close();
            ins = null;
            byte[] arr = baos.toByteArray();
            int arrLength = convertToLF(arr);
            baos.close();
            baos = null;
            in = new InputStreamReader(new ByteArrayInputStream(arr, 0, arrLength), encoding);
            // initialize standard commit output stream, if user
            // does not provide his own writer
            if (out == null) {
                out = new OutputStreamWriter(fo.getOutputStream(), encoding);
            }
            int offset = 0;                
            for (Difference diff : differences) {
                if (diff.isExcluded())
                    continue;
                int pos = diff.getStartPosition().getOffset();
                int toread = pos - offset;
                char[] buff = new char[toread];
                int n;
                int rc = 0;
                while ((n = in.read(buff,0, toread - rc)) > 0 && rc < toread) {
                    out.write(buff, 0, n);
                    rc+=n;
                    offset += n;
                }
                switch (diff.getKind()) {
                    case INSERT:
                        out.write(diff.getNewText());
                        break;
                    case REMOVE:
                        int len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        in.skip(len);
                        offset += len;
                        break;
                    case CHANGE:
                        len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        in.skip(len);
                        offset += len;
                        out.write(diff.getNewText());
                        break;
                }
            }                    
            char[] buff = new char[1024];
            int n;
            while ((n = in.read(buff)) > 0)
                out.write(buff, 0, n);
        } finally {
            if (ins != null)
                ins.close();
            if (baos != null)
                baos.close();
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }            
    }
    
    private int convertToLF(byte[] buff) {
        int j = 0;
        for (int i = 0; i < buff.length; i++) {
            if (buff[i] != '\r') {
                buff[j++] = buff[i];
            }
        }
        return j;
    }
    
    /**
     * Returned string represents preview of resulting source. No difference
     * really is applied. Respects {@code isExcluded()} flag of difference.
     * 
     * @param   there can be more resulting source, user has to specify
     *          which wants to preview.
     * @return  if changes are applied source looks like return string
     */
    public String getResultingSource(FileObject fileObject) throws IOException {
        assert fileObject != null : "Provided fileObject is null";
        StringWriter writer = new StringWriter();
        commit(fileObject, diffs.get(fileObject), writer);
        
        return writer.toString();
    }
    
    public static final class Difference {
        Kind kind;
        PositionRef startPos;
        PositionRef endPos;
        String oldText;
        String newText;
        String description;
        private boolean excluded;

        Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText, String description) {
            this.kind = kind;
            this.startPos = startPos;
            this.endPos = endPos;
            this.oldText = oldText;
            this.newText = newText;
            this.description = description;
            this.excluded = false;
        }
        
        Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText) {
            this(kind, startPos, endPos, oldText, newText, null);
        }
        
        public Kind getKind() {
            return kind;
        }
        
        public PositionRef getStartPosition() {
            return startPos;
        }
        
        public PositionRef getEndPosition() {
            return endPos;
        }
        
        public String getOldText() {
            return oldText;
        }
        
        public String getNewText() {
            return newText;
        }
        
        public boolean isExcluded() {
            return excluded;
        }
        
        public void exclude(boolean b) {
            excluded = b;
        }

        @Override
        public String toString() {
            return kind + "<" + startPos.getOffset() + ", " + endPos.getOffset() + ">: " + oldText + " -> " + newText;
        }
        public String getDescription() {
            return description;
        }
        
        public static enum Kind {
            INSERT,
            REMOVE,
            CHANGE
        }
    }
}
