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

package org.netbeans.modules.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * Factory of readers that are encoded according to best known approach how to
 * get the encoding information.
 * <p>
 * This factory should ideally be replaced by some public APIs. This uses just
 * heuristics to find things out. This is intended to be only a temporary solution.
 * <p>
 * Use on your own risk.
 *
 * @author Martin Entlicher
 */
public class EncodedReaderFactory {
    
    /** The FileObject attribute that defines the encoding of the FileObject content. */
    private static final String CHAR_SET_ATTRIBUTE = "Content-Encoding"; // NOI18N

    private static EncodedReaderFactory factory;
    
    /** Creates a new instance of EncodedReaderFactory */
    private EncodedReaderFactory() {
    }
    
    /** Get the default implementation. */
    public static synchronized EncodedReaderFactory getDefault() {
        if (factory == null) {
            factory = new EncodedReaderFactory();
        }
        return factory;
    }
    
    /**
     * Get the reader from file of given MIME type, it tries to find the best encoding itself.
     */
    public Reader getReader(File file, String mimeType) throws FileNotFoundException {
        return getReader(file, mimeType, getEncoding(file));
    }
    
    public Reader getReader(File file, String mimeType, String encoding) throws FileNotFoundException {
        if (encoding != null) {
            try {
                return new InputStreamReader(new FileInputStream(file), encoding);
            } catch (UnsupportedEncodingException ueex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ueex);
            }
        }
        Reader r = getReaderFromKit(file, null, mimeType);
        if (r != null) {
            return r;
        } else {
            // Fallback, use current encoding
            return new InputStreamReader(new FileInputStream(file));
        }
    }
    
    public Reader getReader(FileObject fo, String encoding) throws FileNotFoundException {
        if (encoding != null) {
            try {
                return new InputStreamReader(fo.getInputStream(), encoding);
            } catch (UnsupportedEncodingException ueex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ueex);
            }
        }
        Reader r = getReaderFromKit(null, fo, fo.getMIMEType());
        if (r != null) {
            return r;
        } else {
            // Fallback, use current encoding
            return new InputStreamReader(fo.getInputStream());
        }
    }
    
    /** @return The reader or <code>null</code>. */
    private Reader getReaderFromKit(File file, FileObject fo, String mimeType) throws FileNotFoundException {
        EditorKit kit = JEditorPane.createEditorKitForContentType(mimeType);
        if (kit == null && "text/x-dtd".equalsIgnoreCase(mimeType)) {
             // Use XML kit for DTDs if not defined otherwise
            kit = JEditorPane.createEditorKitForContentType("text/xml");
        }
        //System.out.println("  KIT for "+mimeType+" = "+kit);
        if (kit != null) {
            Document doc = kit.createDefaultDocument();
            InputStream stream = null;
            try {
                if (file != null) {
                    stream = new FileInputStream(file);
                } else {
                    stream = fo.getInputStream();
                }
                kit.read(stream, doc, 0);
                String text = doc.getText(0, doc.getLength());
                //System.out.println("  TEXT = "+text);
                doc = null; // Release it, we have the text
                return new StringReader(text);
            } catch (IOException ioex) {
                FileNotFoundException fnfex;
                if (file != null) {
                    fnfex = new FileNotFoundException("Can not read file "+file.getAbsolutePath());
                } else {
                    fnfex = new FileNotFoundException("Can not read file "+fo);
                }
                fnfex.initCause(ioex);
                throw fnfex;
            } catch (BadLocationException blex) { // Something wrong???
                ErrorManager.getDefault().notify(blex);
            } finally {
                if (stream != null) {
                    try { stream.close(); } catch (IOException e) {}
                }
            }
        }
        return null;
    }
    
    public String getEncoding(FileObject fo) {
        String ext = fo.getExt();
        if ("html".equalsIgnoreCase(ext)) {
            return findHTMLEncoding(fo);
        }
        if ("properties".equalsIgnoreCase(ext)) {
            return findPropertiesEncoding();
        }
        Object encoding = null;
        if ("java".equalsIgnoreCase(ext)) {
            encoding = findJavaEncoding(fo);
        }
        if (encoding == null) {
            encoding = fo.getAttribute(CHAR_SET_ATTRIBUTE);
        }
        if (encoding != null) {
            return encoding.toString();
        } else {
            return null;
        }
    }
    
    public String getEncoding(File file) {
        String name = file.getName();
        int endingIndex = name.lastIndexOf('.');
        String ext = (endingIndex >= 0 && endingIndex < (name.length() - 1)) ? name.substring(endingIndex + 1) : "";
        if ("html".equalsIgnoreCase(ext)) {
            return findHTMLEncoding(file);
        }
        if ("properties".equalsIgnoreCase(ext)) {
            return findPropertiesEncoding();
        }
        Object encoding = null;
        try {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                if ("java".equalsIgnoreCase(ext)) {
                    encoding = findJavaEncoding(fo);
                }
                if (encoding == null) {
                    encoding = fo.getAttribute(CHAR_SET_ATTRIBUTE);
                }
            }
        } catch (IllegalArgumentException iaex) {} // Ignore
        if (encoding != null) {
            return encoding.toString();
        } else {
            return null;
        }
    }
    
    private static String findJavaEncoding(FileObject fo) {
        ClassLoader systemClassLoader =
                (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
        Method org_netbeans_modules_java_Util_getFileEncoding = null;
        try {
            Class c = systemClassLoader.
                    loadClass("org.netbeans.modules.java.Util"); // NOI18N
            org_netbeans_modules_java_Util_getFileEncoding =
                c.getMethod("getFileEncoding", new Class[] {FileObject.class});
        } catch (Exception e) {
            // Ignore
        }
        if (org_netbeans_modules_java_Util_getFileEncoding != null) {
            try {
                String encoding = (String) org_netbeans_modules_java_Util_getFileEncoding.
                    invoke(null, new Object[] {fo});
                return encoding;
            } catch (Exception e) {
                // Ignore
            }
        }
        return null;
    }
    
    /**
     * Finds the encoding of an HTML file.
     * <p>
     * This method was copied from org.netbeans.modules.html.HtmlEditorSupport.
     */
    private static String findHTMLEncoding(File file) {
        byte[] arr = new byte[4096];
        String txt;
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            int len = stream.read (arr, 0, arr.length);
            txt = new String (arr, 0, (len>=0)?len:0).toUpperCase();
        } catch (IOException ioex) {
            return null;
        } finally {
            if (stream != null) {
                try { stream.close(); } catch (IOException e) {}
            }
        }
        // encoding
        return findHTMLEncoding (txt);
    }

    /**
     * Finds the encoding of an HTML file.
     */
    private static String findHTMLEncoding(FileObject fo) {
        byte[] arr = new byte[4096];
        String txt;
        InputStream stream = null;
        try {
            stream = fo.getInputStream();
            int len = stream.read (arr, 0, arr.length);
            txt = new String (arr, 0, (len>=0)?len:0).toUpperCase();
        } catch (IOException ioex) {
            return null;
        } finally {
            if (stream != null) {
                try { stream.close(); } catch (IOException e) {}
            }
        }
        // encoding
        return findHTMLEncoding (txt);
    }

    
    /** Tries to guess the encoding from given header text. Tries to find
     *   <em>&lt;meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"&gt;</em>
     * <p>
     * This method was copied from org.netbeans.modules.html.HtmlEditorSupport.
     *
     * @param txt the string to search in (should be in upper case)
     * @return the encoding or null if no has been found
     */
    private static String findHTMLEncoding (String txt) {
        int headLen = txt.indexOf ("</HEAD>"); // NOI18N
        if (headLen == -1) headLen = txt.length ();
        
        int content = txt.indexOf ("CONTENT-TYPE"); // NOI18N
        if (content == -1 || content > headLen) {
            return null;
        }
        
        int charset = txt.indexOf ("CHARSET=", content); // NOI18N
        if (charset == -1) {
            return null;
        }
        
        int charend = txt.indexOf ('"', charset);
        int charend2 = txt.indexOf ('\'', charset);
        if (charend == -1 && charend2 == -1) {
            return null;
        }

        if (charend2 != -1) {
            if (charend == -1 || charend > charend2) {
                charend = charend2;
            }
        }
        
        return txt.substring (charset + "CHARSET=".length (), charend); // NOI18N
    }
    
    private static String findPropertiesEncoding() {
        return "ISO-8859-1"; // NOI18N
    }
    
}
