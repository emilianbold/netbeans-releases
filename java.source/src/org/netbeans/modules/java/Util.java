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

package org.netbeans.modules.java;

import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.ExtensionList;
import org.openide.text.IndentEngine;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import org.openide.cookies.SaveCookie;
import org.openide.ErrorManager;

import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import java.io.*;
import org.openide.util.SharedClassObject;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

import org.openide.util.NbBundle;

/** 
 * XXX: Stripped version of Util, it should be extended when needed,
 * the original version has references to JMI and org.openide.src
 * Miscellaneous utilities for Java data loader.
 *
 * @author Petr Hamernik, Ales Novak
 */
public final class Util extends Object {
    
    /** The prefix of all magic strings */
    final static String MAGIC_PREFIX = "//GEN-"; // NOI18N
    
    // ===================== i18n utilities ===========================

    /**
     * Instance of error manager used for annotating exceptions.
     */
    private static ErrorManager errorManager;
    
    static final String ATTR_FILE_ENCODING = "Content-Encoding"; // NOI18N

    /** Computes the localized string for the key.
    * @param key The key of the string.
    * @return the localized string.
    */
    static String getString(String key) {
        return NbBundle.getMessage(Util.class, key);
    }
    
    static ErrorManager getErrorManager() {
        if (errorManager != null)
            return errorManager;
        ErrorManager main = ErrorManager.getDefault();
        if (main == null) {
            System.err.println("WARNING: can't lookup error manager"); // NOI18N
            return null;
        }
        return errorManager = main;
    }
    
    static void annotateThrowable(Throwable t, Throwable nested) {
        getErrorManager().annotate(t, nested);
    }
    
    static void annotateThrowable(Throwable t, String localizedMessage, 
        boolean user) {
        if (user) {
            getErrorManager().annotate(t, ErrorManager.USER, null,
                localizedMessage, null, null);
        } else {
            getErrorManager().annotate(t, ErrorManager.EXCEPTION, null,
                localizedMessage, null, null);
        }
    }

    // ===================== loader utilities ===========================

    static FileObject findBrother(FileObject f, String extension) {
        return FileUtil.findBrother(f, extension);
    }

    private static FileObject findSibling(FileObject base, String name, Enumeration extlist) {
        FileObject ret;
        while (extlist.hasMoreElements()) {
            String ext = (String)extlist.nextElement();
            if (ext == null)
                continue;
            ret = base.getFileObject(name, ext);
            if (ret != null)
                return ret;
        }
        return null;
    }

    /** Notifies about an exception
    *
    * @param msg is ignored
    */
    private static void notifyException(Throwable t, String msg) {
        getErrorManager().notify(t);
    }


    // ===================== Indentation util ==============================

    /** Finds the appropriate indentation writer for the java sources.
    * @param doc The document where it will be inserted in
    * @param offset The position in the document
    * @param writer The encapsulated writer
    */
    static Writer findIndentWriter(Document doc, int offset, Writer writer) {
        IndentEngine engine = IndentEngine.find(doc); // NOI18N
        return engine.createWriter(doc, offset, writer);
    }

        
    /**
     * Retrieves file's encoding. If the file does not specify any encoding,
     * null is returned. Note: an empty string is also a legal value, which also
     * means platform-default encoding.
     * @deprecated Disclaimer: this method is <B>not an API method</B>. It may not
     * be available in future releases.
     * @param someFile file object to retrieve encoding for
     * @return encoding string, or null if no explicit encoding is given.
     */
    public static String getFileEncoding0(FileObject someFile) {
        return (String)someFile.getAttribute(ATTR_FILE_ENCODING);
    }
    
    /**
     * Retrieves file's encoding. Returns the default encoding for java files,
     * if the file's specific encoding is not set.
     * @deprecated Disclaimer: this method is <B>not an API method</B>. It may not
     * be available in future releases.
     * @param someFile file object to retrieve encoding for
     * @return encoding string, or null if no explicit encoding is given in file's
     * or IDE's settings.
     */
    public static String getFileEncoding(FileObject someFile) {
        String enc = getFileEncoding0(someFile);
        if (enc == null) {
//XXX:Todo fix this when moved back to java/src where JavaSettings exist            enc = JavaSettings.getDefault().getDefaultEncoding();
            enc = null;
        }
        if ("".equals(enc))
            return null;
        else
            return enc;
    }
    
    /**
     * Retrieves file's encoding. Returns the default encoding for java files,
     * if the file's specific encoding is not set.
     * This implementation stores the encoding information into file's extended
     * attributes (so it won't work on JARs).
     * @deprecated Disclaimer: this method is <B>not an API method</B>. It may not
     * be available in future releases.
     * @param someFile file object to set encoding for
     * @param enc encoding string. Null means revert to the default encoding.
     */
    public static void setFileEncoding(FileObject someFile, String enc) throws IOException {
        someFile.setAttribute(ATTR_FILE_ENCODING, enc);
    }               

    public static char[] readContents(Reader r) throws IOException {
        int read = 0;
        int total = 0;
        int offset;
        char[] buffer;
        List buflist = new LinkedList();

        do {
            buffer = new char[2048];
            offset = 0;
            while (offset < buffer.length) {
                read = r.read(buffer, offset, buffer.length - offset);
                if (read == -1) break;
                offset += read;
            }
            if (offset > 0) buflist.add(buffer);
            total += offset;
        } while (read >= 0);
        r.close();

        buffer = new char[total];
        Iterator it = buflist.iterator();
        int offset2 = 0;
        while (it.hasNext()) {
            char[] buf = (char[])it.next();
            int size = (it.hasNext()) ? buf.length : total - offset2;
            System.arraycopy(buf, 0, buffer, offset2, size);
            offset2 += size;
        }
        return buffer;
    }
    
    private static String getDocumentText(FileObject fo, boolean save) throws IOException {
        DataObject obj = DataObject.find(fo);
        EditorCookie editor = null;

        if (obj instanceof JavaDataObject)
            editor = (EditorCookie) ((JavaDataObject) obj).getCookie (EditorCookie.class);

	final Document doc;
        if ((editor != null) && (doc = editor.getDocument()) != null) {
            // loading from the memory (Document)
            final String[] str = new String[1];
            // safely take the text from the document
            Runnable run = new Runnable() {
                               public void run() {
                                   try {
                                       str[0] = doc.getText(0, doc.getLength());
                                   }
                                   catch (BadLocationException e) {
                                       // impossible
                                   }
                               }
                           };
            if (save) {
                SaveCookie cookie = (SaveCookie) obj.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }      
            doc.render(run);            
            return str[0];
        } else {
            return null;
        }
    }            
    
    /** The input stream which holds all data which are read in the StringBuffer.
     * @deprecated The class doesn't process character data in the stream and
     * is not very usable in I18N environments.
     */
    static class ParserInputStream extends InputStream {
        
        /** The underlaying stream.  */
        private InputStream stream;
        
        /** Whole text  */
        private String text;
        
        /** The string buffer which collect the data.  */
        private StringBuffer buffer;
        
        /** This flag determines if there is used the text field or buffer field.
         * The constructor set it
         */
        private boolean mode;
        
        /** The counter of read chars  */
        private int counter;
        
        /** Offset of the begins of the lines (e.g. offset of [line,col] is lines[line] + col - 1
         */
        private int[] lines = new int[200];
        
        /** Current line counter - it is used for filling the lines array in the read method
         */
        int lineCounter = 2;
        
        /** Length of the current line
         */
        int currentLineLength = 0;
        
        /** Creates the stream from the text.
         */
        ParserInputStream(String text) {
            this(text, null);
        }
        
        ParserInputStream(String text, String encoding) {
            this.text = text;
            counter = 0;
            mode = false;
            ByteArrayOutputStream outstm = new ByteArrayOutputStream(text.length());
            Writer wr = null;
            
            if (encoding != null) {
                try {
                    wr = new OutputStreamWriter(outstm, encoding);
                } catch (UnsupportedEncodingException ex) {
                }
            }
            if (wr == null) {
                wr = new OutputStreamWriter(outstm);
            }
            
            try {
                wr.write(text);
                wr.close();
            } catch (IOException ex) {
            }
            this.stream = new ByteArrayInputStream(outstm.toByteArray());
        }
        
        /** Creates the stream from the another stream.  */
        ParserInputStream(InputStream stream) {
            this.stream = stream;
            buffer = new StringBuffer();
            mode = true;
        }
        
        /** Gets the part of the text which was already read.
         * @param begin the begin index
         * @param end the end index
         */
        public String getString(int begin, int end) {
            return mode ? buffer.substring(begin, end) : text.substring(begin, end);
        }
        
        /** Gets the part of the text which was already read.
         * End is last position which was already read.
         * @param begin the begin index
         */
        public String getString(int begin) {
            if (mode) {
                return buffer.substring(begin);
            }
            else {
                int end = Math.min(counter - 1, text.length());
                return text.substring(begin, end);
            }
        }
        
        /** Read one character from the stream.  */
        public int read() throws IOException {
            int x = stream.read();
            if (mode && (x != -1)) {
                buffer.append((char)x);
                counter++;
            }
            
            // counting line's length
            if (x == (int)'\n') {
                if (lineCounter == lines.length - 1) {
                    int[] newLines = new int[lineCounter + lineCounter];
                    System.arraycopy(lines, 0, newLines, 0, lines.length);
                    lines = newLines;
                }
                lines[lineCounter] = lines[lineCounter - 1] + currentLineLength + 1;
                lineCounter++;
                currentLineLength = 0;
            }
            else {
                currentLineLength++;
            }
            
            return x;
        }
        
        /** Closes the stream  */
        public void close() throws IOException {
            stream.close();
        }
        
        /** Compute offset in the stream from line and column.
         * @return the offset
         */
        int getOffset(int line, int column) {
            return lines[line] + column - 1;
        }
        
    }
}
