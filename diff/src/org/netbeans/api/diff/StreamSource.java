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

package org.netbeans.api.diff;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import org.openide.util.io.ReaderInputStream;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.queries.FileEncodingQuery;

/**
 * This class provides streams and information about them to be used by diff
 * and merge services.
 *
 * @author  Martin Entlicher
 */
public abstract class StreamSource extends Object {
    
    /**
     * Get the name of the source.
     */
    public abstract String getName();
    
    /**
     * Get the title of the source.
     */
    public abstract String getTitle();
    
    /**
     * Get the MIME type of the source.
     */
    public abstract String getMIMEType();
    
    /**
     * Hint for a diff visualizer about editability of this source. The source will only be made editable if it provides
     * some editable entity in its lookup (eg. FileObject) and this method returns true and the diff visualizer supports it.
     * 
     * @return true if this source can be editable in the diff visualizer, false otherwise
     * @since 1.17
     */ 
    public boolean isEditable() {
        return false;
    }

    /**
     * Source lookup that may define the content of this source. In case the lookup does not provide anything
     * usable, createReader() is used instead. Diff engines can process these inputs: 
     * <ul>
     * <li> instance of {@link org.openide.filesystems.FileObject} - in this case, the content of the source is defined 
     * by calling DataObject.find(fileObject).openDocument(). If the source is editable then it is
     * saved back via SaveCookie.save() when the Diff component closes.
     * <li> instance of {@link javax.swing.text.Document} - in this case, the content of the source is defined 
     * by this Document and the source will NOT be editable.
     * </ul>
     * 
     * For compatibility purposes, it is still adviced to fully implement createReader() as older Diff providers may
     * not use this method of obtaining the source.
     * 
     * @return an instance of Lookup
     * @since 1.17
     */ 
    public Lookup getLookup() {
        return Lookups.fixed();
    }
    
    /**
     * Create a reader, that reads the source.
     */
    public abstract Reader createReader() throws IOException ;
    
    /**
     * Create a writer, that writes to the source.
     * @param conflicts The list of conflicts remaining in the source.
     *                  Can be <code>null</code> if there are no conflicts.
     * @return The writer or <code>null</code>, when no writer can be created.
     */
    public abstract Writer createWriter(Difference[] conflicts) throws IOException ;
    
    /**
     * Close the stream source. This method, is called when this object
     * will never be asked for the streams any more and thus can
     * release it's resources in this method.
     */
    public void close() {
    }
    
    /**
     * Create the default implementation of <code>StreamSource</code>, that has
     * just reader and no writer.
     */
    public static StreamSource createSource(String name, String title, String MIMEType, Reader r) {
        return new Impl(name, title, MIMEType, r);
    }
    
    /**
     * Create the default implementation of <code>StreamSource</code>, that has
     * just reader and writer from/to a file.
     */
    public static StreamSource createSource(String name, String title, String MIMEType, File file) {
        return new Impl(name, title, MIMEType, file);
    }
    
    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {
        
        private String name;
        private String title;
        private String MIMEType;
        private Reader r;
        private File readerSource;
        private Writer w;
        private File file;
        private String encoding;
        
        Impl(String name, String title, String MIMEType, Reader r) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            this.r = r;
            this.readerSource = null;
            this.w = null;
            this.file = null;
            if (r instanceof InputStreamReader) {
                encoding = ((InputStreamReader) r).getEncoding();
            }
        }
        
        Impl(String name, String title, String MIMEType, File file) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            this.readerSource = null;
            this.w = null;
            this.file = file;
            encoding = FileEncodingQuery.getEncoding(FileUtil.toFileObject(file)).name();
        }
        
        private File createReaderSource(Reader r) throws IOException {
            File tmp = null;
            tmp = FileUtil.normalizeFile(File.createTempFile("sss", "tmp"));
            tmp.deleteOnExit();
            tmp.createNewFile();
            InputStream in = null;
            OutputStream out = null;
            try {
                if (encoding == null) {
                    in = new ReaderInputStream(r);
                } else {
                    in = new ReaderInputStream(r, encoding);
                }
                org.openide.filesystems.FileUtil.copy(in, out = new FileOutputStream(tmp));
            } finally {
                if (in != null) in.close();
                if (out != null) out.close();
            }
            return tmp;
        }
        
        public String getName() {
            return name;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getMIMEType() {
            return MIMEType;
        }
        
        public Reader createReader() throws IOException {
            if (file != null) {
                return new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            } else {
                synchronized (this) {
                    if (r != null) {
                        readerSource = createReaderSource(r);
                        r = null;
                    }
                }
                if (encoding == null) {
                    return new BufferedReader(new FileReader(readerSource));
                } else {
                    return new BufferedReader(new InputStreamReader(new FileInputStream(readerSource), encoding));
                }
            }
        }
        
        public Writer createWriter(Difference[] conflicts) throws IOException {
            if (conflicts != null && conflicts.length > 0) return null;
            if (file != null) {
                if (encoding == null) {
                    return new BufferedWriter(new FileWriter(file));
                } else {
                    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
                }
            } else return w;
        }
        
    }
}
