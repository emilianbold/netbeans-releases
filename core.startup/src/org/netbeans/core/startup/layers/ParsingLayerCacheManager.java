/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.layers;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.openide.ErrorManager;
import org.openide.filesystems.FileSystem;
import org.openide.util.NotImplementedException;
import org.openide.xml.XMLUtil;

/** A cache manager which parses the layers according to the Filesystems 1.x DTDs.
 * This class just handles the parsing during cache rewrite time; subclasses are
 * responsible for loading from and saving to the storage format.
 * @author Jesse Glick
 */
public abstract class ParsingLayerCacheManager extends LayerCacheManager implements ContentHandler, ErrorHandler, EntityResolver {
    
    private final static String[] ATTR_TYPES = {
        "bytevalue",
        "shortvalue",
        "intvalue",
        "longvalue",
        "floatvalue",
        "doublevalue",
        "boolvalue",
        "charvalue",
        "stringvalue",
        "urlvalue",
        "methodvalue",
        "newvalue",
        "serialvalue",
    };
    
    private final static String DTD_1_0 = "-//NetBeans//DTD Filesystem 1.0//EN";
    private final static String DTD_1_1 = "-//NetBeans//DTD Filesystem 1.1//EN";
    
    private Locator locator;
    private MemFolder root;
    private Stack curr; // Stack<MemFileOrFolder | MemAttr>
    private URL base;
    private StringBuffer buf = new StringBuffer();
    private int fileCount, folderCount, attrCount;
    // Folders, files, and attrs already encountered in this layer.
    // By path; attrs as folder/file path plus "//" plus attr name.
    private Set oneLayerFiles; // Set<String>
    // Related:
    private boolean checkingForDuplicates;
    private String currPath;

    /** Constructor for subclasses.
     */
    protected ParsingLayerCacheManager(File cacheDir) throws IOException {
        super(cacheDir);
    }
    
    /** Implements storage by parsing the layers and calling
     * {@link #store(FileSystem,ParsingLayerCacheManager.MemFolder}.
     */
    public final void store(FileSystem fs, List urls) throws IOException {
        store(fs, createRoot(urls));
    }
    
    /** Implements storage by parsing the layers and calling
     * {@link #store(ParsingLayerCacheManager.MemFolder}.
     */
    public final FileSystem store(List urls) throws IOException {
        return store(createRoot(urls));
    }
    
    /**
     * Do the actual parsing.
     */
    private MemFolder createRoot(List urls) throws IOException {
        root = new MemFolder(null);
        curr = new Stack();
        curr.push(root);
        try {
            // XMLReader r = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            XMLReader r = XMLUtil.createXMLReader();
            // Speed enhancements.
            // XXX these are not really necessary; OK to run validation here!
            r.setFeature("http://xml.org/sax/features/validation", false);
            r.setFeature("http://xml.org/sax/features/namespaces", false);
            // XXX this is not standard, should not rely on it:
            r.setFeature("http://xml.org/sax/features/string-interning", true);
            r.setContentHandler(this);
            r.setErrorHandler(this);
            r.setEntityResolver(this);
            Exception carrier = null;
            // #23609: reverse these...
            urls = new ArrayList(urls);
            Collections.reverse(urls);
            checkingForDuplicates = LayerCacheManager.err.isLoggable(ErrorManager.WARNING);
            Iterator it = urls.iterator();
            while (it.hasNext()) {
                base = (URL)it.next();
                if (checkingForDuplicates) {
                    oneLayerFiles = new HashSet(100);
                    currPath = null;
                }
                LayerCacheManager.err.log("Parsing: " + base);
                try {
                    r.parse(base.toExternalForm());
                } catch (Exception e) {
                    curr.clear();
                    curr.push(root);
                    if (carrier == null) {
                        carrier = e;
                    } else {
                        LayerCacheManager.err.annotate(carrier, e);
                    }
                }
            }
            if (carrier != null) throw carrier;
            LayerCacheManager.err.log("Finished layer parsing; " + fileCount + " files, " + folderCount + " folders, " + attrCount + " attributes");
            return root;
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            IOException ioe = new IOException(e.toString());
            LayerCacheManager.err.annotate(ioe, e);
            throw ioe;
        } finally {
            fileCount = folderCount = attrCount = 0;
            base = null;
            locator = null;
            curr = null;
            root = null;
            oneLayerFiles = null;
            currPath = null;
        }
    }

    /** Delegated storage method supplied with a merged layer parse.
     * Not called if the manager does not support loading;
     * otherwise must be overridden.
     */
    protected void store(FileSystem fs, MemFolder root) throws IOException {
        throw new NotImplementedException();
    }
    
    /** Delegated storage method supplied with a merged layer parse.
     * Not called if the manager supports loading;
     * otherwise must be overridden.
     */
    protected FileSystem store(MemFolder root) throws IOException {
        throw new NotImplementedException();
    }
    
    /** If true, file content URLs should be opened and the contents extracted,
     * if they are of an appropriate type (locally stored). If false, the original
     * URLs should be left alone.
     */
    protected abstract boolean openURLs();
    
    public void startElement(String ns, String lname, String qname, Attributes attrs) throws SAXException {
        if (qname == "filesystem") {
            return;
        } else if (qname == "folder") {
            fileOrFolder(qname, attrs);
        } else if (qname == "file") {
            MemFile file = (MemFile)fileOrFolder(qname, attrs);
            file.contents = null;
            String u = attrs.getValue("url");
            if (u != null) {
                try {
                    file.ref = new URL(base, u);
                } catch (MalformedURLException mfue) {
                    SAXException saxe = new SAXException(mfue.toString());
                    LayerCacheManager.err.annotate(saxe, mfue);
                    throw saxe;
                }
            } else {
                file.ref = null;
            }
        } else if (qname == "attr") {
            attrCount++;
            MemAttr attr = new MemAttr();
            attr.name = attrs.getValue("name");
            for (int i = 0; i < ATTR_TYPES.length; i++) {
                String v = attrs.getValue(ATTR_TYPES[i]);
                if (v != null) {
                    attr.type = ATTR_TYPES[i];
                    attr.data = v;
                    break;
                }
            }
            if (attr.type == null) throw new SAXParseException("unknown <attr> value type for " + attr.name, locator);
            MemFileOrFolder parent = (MemFileOrFolder)curr.peek();
            if (parent.attrs == null) parent.attrs = new LinkedList();
            Iterator it = parent.attrs.iterator();
            while (it.hasNext()) {
                if (((MemAttr)it.next()).name.equals(attr.name)) {
                    attrCount--;
                    it.remove();
                }
            }
            parent.attrs.add(attr);
            if (checkingForDuplicates && !oneLayerFiles.add(currPath + "//" + attr.name)) { // NOI18N
                LayerCacheManager.err.log(ErrorManager.WARNING, "Warning: layer " + base + " contains duplicate attributes " + attr.name + " for " + currPath);
            }
        } else {
            throw new SAXException(qname);
        }
    }
    
    private MemFileOrFolder fileOrFolder(String qname, Attributes attrs) {
        String name = attrs.getValue("name");
        if (name == null) throw new NullPointerException("No name"); // NOI18N
        if (!(curr.peek() instanceof MemFolder)) throw new ClassCastException("Stack: " + curr); // NOI18N
        MemFolder parent = (MemFolder)curr.peek();
        if (parent.children == null) parent.children = new LinkedList();
        MemFileOrFolder f = null;
        Iterator it = parent.children.iterator();
        while (it.hasNext()) {
            MemFileOrFolder f2 = (MemFileOrFolder)it.next();
            if (f2.name.equals(name)) {
                f = f2;
                break;
            }
        }
        if (f == null) {
            if (qname == "folder") { // NOI18N
                f = new MemFolder(base);
                folderCount++;
            } else {
                f = new MemFile(base);
                fileCount++;
            }
            f.name = name;
            parent.children.add(f);
        }
        curr.push(f);
        if (checkingForDuplicates) {
            if (currPath == null) {
                currPath = name;
            } else {
                currPath += "/" + name;
            }
            if (!oneLayerFiles.add(currPath)) {
                LayerCacheManager.err.log(ErrorManager.WARNING, "Warning: layer " + base + " contains duplicate " + qname + "s named " + currPath);
            }
        }
        return f;
    }
    
    public void endElement(String ns, String lname, String qname) throws SAXException {
        if (qname == "file" && buf.length() > 0) {
            String text = buf.toString().trim();
            if (text.length() > 0) {
                MemFile file = (MemFile)curr.peek();
                if (file.ref != null) throw new SAXParseException("CDATA plus url= in <file>", locator);
                LayerCacheManager.err.log(ErrorManager.WARNING, "Warning: use of inline CDATA text contents in <file name=\"" + file.name + "\"> deprecated for performance and charset safety at " + locator.getSystemId() + ":" + locator.getLineNumber() + ". Please use the 'url' attribute instead, or the file attribute 'originalFile' on *.shadow files (with IDE/1 > 2.23).");
                // Note: platform default encoding used. If you care about the encoding,
                // you had better be using url= instead.
                file.contents = text.getBytes();
            }
            buf.setLength(0);
        }
        if (qname == "file" && openURLs()) {
            MemFile file = (MemFile)curr.peek();
            // Only open simple URLs. Assume that JARs are the same JARs with the layers.
            if (file.ref != null && file.ref.toExternalForm().startsWith("jar:file:")) { // NOI18N
                try {
                    URLConnection conn = file.ref.openConnection();
                    conn.connect();
                    byte[] buf = new byte[conn.getContentLength()];
                    InputStream is = conn.getInputStream();
                    try {
                        int pos = 0;
                        while (pos < buf.length) {
                            int read = is.read(buf, pos, buf.length - pos);
                            if (read < 1) throw new IOException("Premature EOF on " + file.ref.toExternalForm()); // NOI18N
                            pos += read;
                        }
                        if (is.read() != -1) throw new IOException("Delayed EOF on " + file.ref.toExternalForm()); // NOI18N
                    } finally {
                        is.close();
                    }
                    file.contents = buf;
                    file.ref = null;
                } catch (IOException ioe) {
                    throw new SAXException(ioe);
                }
            }
        }
        if (qname == "file" || qname == "folder") { // NOI18N
            curr.pop();
            if (checkingForDuplicates) {
                int i = currPath.lastIndexOf('/'); // NOI18N
                if (i == -1) {
                    currPath = null;
                } else {
                    currPath = currPath.substring(0, i);
                }
            }
        }
    }
    
    public void characters(char[] ch, int start, int len) throws SAXException {
        Object currF = curr.peek();
        if (!(currF instanceof MemFile)) {
            return;
        }
        // Usually this will just be whitespace which we will ignore anyway.
        // Do it anyway, so that people who accidentally write:
        // <file name="x" url="y"><![CDATA[z]]></file>
        // will at least get an error.
        buf.append(ch, start, len);
    }
    
    public void warning(SAXParseException e) throws SAXException {
        LayerCacheManager.err.notify(ErrorManager.INFORMATIONAL, e);
    }
    
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }
    
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }
    
    public InputSource resolveEntity(String pubid, String sysid) throws SAXException, IOException {
        if (pubid != null && (pubid.equals(DTD_1_0) || pubid.equals(DTD_1_1))) {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        } else {
            return null;
        }
    }
    
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
    
    public void endDocument() throws SAXException {
        if (curr.size() != 1) throw new SAXException("Wrong stack: " + curr); // NOI18N
    }
    
    public void startDocument() throws SAXException {}
    
    public void startPrefixMapping(String str, String str1) throws SAXException {}
    
    public void skippedEntity(String str) throws SAXException {}
    
    public void processingInstruction(String str, String str1) throws SAXException {}
    
    public void ignorableWhitespace(char[] values, int param, int param2) throws SAXException {}
    
    public void endPrefixMapping(String str) throws SAXException {}

    /** Struct for <file> or <folder>.
     */
    protected static abstract class MemFileOrFolder {
        public String name;
        public List attrs = null; // {null | List<MemAttr>}
        public final URL base;
        
        public MemFileOrFolder (URL base) {
            this.base = base;
        }
    }
    
    /** Struct for <folder>.
     */
    protected static final class MemFolder extends MemFileOrFolder {
        public List children = null; // {null | List<MemFileOrFolder>}
        
        public MemFolder (URL base) {
            super (base);
        }
        
        public String toString() {
            return "MemFolder[" + name + "]"; // NOI18N
        }
    }
    
    /** Struct for <file>.
     */
    protected static final class MemFile extends MemFileOrFolder {
        public byte[] contents = null; // {null | byte[]}
        public URL ref = null; // {null | URL}
        
        public MemFile (URL base) {
            super (base);
        }
        
        public String toString() {
            return "MemFile[" + name + "]"; // NOI18N
        }
    }
    
    /** Struct for <attr>.
     */
    protected static final class MemAttr {
        public String name;
        public String type;
        public String data;
        public String toString() {
            return "MemAttr[" + name + "," + type + "," + data + "]"; // NOI18N
        }
    }
    
}
