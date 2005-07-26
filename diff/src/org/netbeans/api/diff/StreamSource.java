/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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

import org.netbeans.modules.diff.EncodedReaderFactory;

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
            encoding = EncodedReaderFactory.getDefault().getEncoding(file);
        }
        
        private File createReaderSource(Reader r) throws IOException {
            File tmp = null;
            tmp = File.createTempFile("sss", "tmp");
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
                return new BufferedReader(EncodedReaderFactory.getDefault().getReader(file, MIMEType, encoding));
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
