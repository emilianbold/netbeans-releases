/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.filesystems;

import java.io.*;
import java.util.*;
import java.net.URL;

import org.xml.sax.*;

import org.openide.cookies.InstanceCookie;
import org.openide.loaders.*;
import org.openide.execution.NbfsURLConnection;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.xml.*;
import org.openide.*;

/**
 * MIMEResolver implementation driven by an XML document instance
 * following PUBLIC "-//NetBeans//DTD MIME Resolver 1.0//EN".
 *
 * <p>
 * 1. It provides Environment for XMLDataObjects with above public ID.
 * <p>
 * 2. Provided environment returns (InstanceCookie) Impl instance.
 * <p>
 * 3. [Instance]Lookup return that Impl instance.
 * <p>
 * 4. MIMEResolver's findMIMEType() parses description file and applies checks on passed files.
 * <p>
 * <b>Note:</b> It is public to be accessible by XML layer.
 *
 * @author  Petr Kuzel
 */
public final class MIMEResolverImpl extends XMLEnvironmentProvider implements Environment.Provider {

    private static final long serialVersionUID = 18975L;
    
    // enable some tracing
    private static final boolean DEBUG = false;
        
    // DefaultEnvironmentProvider~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    protected InstanceContent createInstanceContent(DataObject obj) {
        FileObject fo = obj.getPrimaryFile();
        InstanceContent ic = new InstanceContent();
        ic.add((InstanceCookie) new Impl(fo));
        return ic;
    }
                    
    
    // MIMEResolver ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //
    // It implements InstanceCookie because it is added to environment of XML document.
    // The cookie return itself i.e. MIMEResolver to be searchable by Lookup.
    //
    static class Impl extends MIMEResolver implements InstanceCookie {

        // This file object describes rules that drive ths instance
        private final FileObject data;

        // Resolvers in reverse order
        private FileElement[] smell = null;
                
        private short state = DescParser.INIT;
        
        Impl(FileObject obj) {
            if (DEBUG) System.err.println("MIMEResolverImpl.Impl.<init>(" + obj + ")");  // NOI18N
            data = obj;
        }
        
        /**
         * Resolves FileObject and returns recognized MIME type
         * @param fo is FileObject which should be resolved
         * @return  recognized MIME type or null if not recognized
         */
        public String findMIMEType(FileObject fo) {

            synchronized (this) {  // lazy init

                if (state == DescParser.INIT) {
                    state = parseDesc();
                }
                
                if (state == DescParser.ERROR) {                    
                    return null;
                }                
            }

            // smell is filled in reverse order
            
            for (int i = smell.length-1; i>=0; i--) {
                String s = smell[i].resolve(fo);
                if (s != null) {
                    if (DEBUG) System.err.println("MIMEResolverImpl.findMIMEType(" + fo + ")=" + s);  // NOI18N
                    return s;
                }
            }
            
            return null;
        }

        // description document is parsed in the same thread
        private short parseDesc() {
            this.smell = new FileElement[0];
            DescParser parser = new DescParser(data);
            parser.parse();
            smell = parser.template;                
            if (DEBUG) {
                if (parser.state == DescParser.ERROR) {
                    System.err.println("MIMEResolverImpl.Impl parsing error!");
                } else {
                    StringBuffer buf = new StringBuffer();
                    for (int i = 0; i<smell.length; i++)
                        buf.append("\n" + smell[i]);
                    System.err.println("Parsed: " + buf.toString());
                }
            }
            return parser.state;
        }
        
        // InstanceCookie ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        public Object instanceCreate() {
            return this;
        }    

        public Class instanceClass() {
            return this.getClass();
        }    

        public String instanceName() {
            return this.getClass().getName();
        }

        /** For debug purposes. */
        public String toString() {
            return "MIMEResolverImpl.Impl[" + data + ", " + smell + "]";  // NOI18N
        }

        
    }

    
    // XML -> memory representation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Resonsible for parsing backend FileObject and filling resolvers
     * in memory structure according to it.
     */
    private static class DescParser extends DefaultParser {

        private FileElement[] template = null;
        
        // file state substates
        private short file_state = INIT;
        
        // references active resolver component
        private MIMEComponent component = null;        
        private String componentDelimiter = null;
        
        DescParser(FileObject fo) {
            super(fo);
        }

        // pseudo validation states
        private static final short IN_ROOT = 1;
        private static final short IN_FILE = 2;
        private static final short IN_RESOLVER = 3;
        private static final short IN_COMPONENT = 4;

        // second state dimension
        private static final short IN_EXIT = INIT + 1;
        
        // grammar elements
        private static final String ROOT = "MIME-resolver";  // NOI18N
        private static final String FILE = "file"; // NOI18N
        private static final String MIME = "mime"; // NOI18N
        private static final String EXT  = "ext"; // NOI18N
        private static final String RESOLVER = "resolver"; // NOI18N
        private static final String FATTR = "fattr"; // NOI18N
        private static final String NAME = "name"; // NOI18N
        private static final String MAGIC = "magic"; // NOI18N
        private static final String HEX = "hex"; // NOI18N
        private static final String MASK = "mask"; // NOI18N
        private static final String VALUE = "text"; // NOI18N
        private static final String EXIT = "exit"; // NOI18N
        private static final String XML_RULE_COMPONENT = "xml-rule";  // NOI18N

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

            String s;
            int i;

            switch (state) {

                case INIT:

                    if (ROOT.equals(qName) ==  false) error();
                    state = IN_ROOT;
                    break;

                case IN_ROOT:
                    if (FILE.equals(qName) == false) error();

                    // prepare file element structure
                    // actual one is at index 0

                    if (template == null) {
                        template = new FileElement[] {new FileElement()};
                    } else {
                        FileElement[] n = new FileElement[template.length +1];
                        System.arraycopy(template, 0, n, 1, template.length);
                        n[0] = new FileElement();
                        template = n;
                    }

                    state = IN_FILE;                        
                    break;

                case IN_FILE:

                    if (file_state == IN_EXIT) error();
                    
                    if (EXT.equals(qName)) {

                        s = atts.getValue(NAME); if (s == null) error();
                        template[0].fileCheck.addExt(s);

                    } else if (MAGIC.equals(qName)) {

                        s = atts.getValue(HEX); if (s == null) error();
                        String mask = atts.getValue(MASK);                            
                        
                        char[] chars = s.toCharArray();
                        byte[] mask_bytes = null;  // mask is optional
                                                
                        try {
                        
                            if (mask != null) {
                                char[] mask_chars = mask.toCharArray();
                                mask_bytes = XMLUtil.fromHex(mask_chars, 0, mask_chars.length);
                            }
                        
                            byte[] magic = XMLUtil.fromHex(chars, 0, chars.length);
                            if (template[0].fileCheck.setMagic(magic, mask_bytes) == false) {
                                error();
                            }
                        } catch (IOException ioex) {
                            error();
                        }


                    } else if (MIME.equals(qName)) {

                        s = atts.getValue(NAME); if (s == null) error();
                        template[0].fileCheck.addMIME(s);

                    } else if (FATTR.equals(qName)) {

                        s = atts.getValue(NAME); if (s == null) error();
                        String val = atts.getValue(VALUE);
                        template[0].fileCheck.addAttr(s, val);                        

                    } else if (RESOLVER.equals(qName)) {

                        if (template[0].fileCheck.exts == null 
                            && template[0].fileCheck.mimes == null 
                            && template[0].fileCheck.fatts == null 
                            && template[0].fileCheck.magic == null) {
                                error();  // at least one must be specified
                        }

                        s = atts.getValue(MIME); if (s == null) error();
                        template[0].setMIME(s);

                        state = IN_RESOLVER;
                        
                        break;

                    } else if (EXIT.equals(qName)) {
                        
                        file_state = IN_EXIT;
                        break;
                        
                        
                    } else {
                        error();
                    }
                    break;

                case IN_RESOLVER:
                    
                    // it is switch to hardcoded components
                    // you can smooth;y add new ones by entering them
                
                    // PLEASE update DTD public ID register it to XML Environment.Provider
                    // Let the DTD is backward compatible
                    
                    if (XML_RULE_COMPONENT.equals(qName)) {
                        enterComponent(XML_RULE_COMPONENT, new XMLMIMEComponent());
                        component.startElement(namespaceURI, localName, qName, atts);
                    }   
                    
                    break;

                case IN_COMPONENT:
                    
                    component.startElement(namespaceURI, localName, qName, atts);                    
                    break;
                    
                default:

            }
        }

        private void enterComponent(String name, MIMEComponent component) {
            this.component = component;
            componentDelimiter = name;

            component.setDocumentLocator(getLocator());           
            template[0].rule = (MIMEComponent) component;
            state = IN_COMPONENT;            
        }
        
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            switch (state) {
                case IN_FILE:
                    if (FILE.equals(qName)) {
                        state = IN_ROOT;
                        file_state = INIT;
                    }
                    break;       
                    
                case IN_RESOLVER:
                    if (RESOLVER.equals(qName)) {
                        state = IN_FILE;
                    }
                    break;
                    
                case IN_COMPONENT:
                    component.endElement(namespaceURI, localName, qName);
                    if (componentDelimiter.equals(qName)) {
                        state = IN_RESOLVER;
                    }
                    break;
            }
        }

        public void characters(char[] data, int offset, int len) throws SAXException {
            if (state == IN_COMPONENT) component.characters(data, offset, len);
        }
    }       
    
    /**
     * Represents a resolving process made using a <tt>file</tt> element.
     * <p>
     * Responsible for pairing and performing fast check followed by optional
     * rules and if all matches returning MIME type.
     */
    private static class FileElement {
        
        private Type fileCheck = new Type();
        private String mime = null;
        private MIMEComponent rule = null;

        private void setMIME(String mime) {
            if ("null".equals(mime)) return;  // NOI18N
            this.mime = mime;
        }
        
        private String resolve(FileObject file) {
                        
            try {
                if (fileCheck.accept(file)) {
                    if (mime == null) return null;                    
                    if (rule == null) return mime;                    
                    if (rule.acceptFileObject(file)) return mime;
                }
            } catch (IOException io) {
                ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);
                emgr.notify(emgr.INFORMATIONAL, io);                
            }
            return null;
        }
        
        /**
         * For debug puroses only.
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("FileElement(");
            buf.append(fileCheck + " ");
            buf.append(rule + " ");
            buf.append("Result:" + mime);
            return buf.toString();
        }
    }

        
    /**
     * Hold data from XML document and performs first stage check according to them.
     * <p>
     * The first stage check is resonsible for filtering files according  to their 
     * attributes provided by lower layers.
     * <p>
     * We could generate hardwired class bytecode on a fly.
     */
    private static class Type {
        private String[] exts;
        private String[] mimes;
        private String[] fatts;
        private String[] vals;   // contains null or value of attribute at the same index
        private byte[]   magic;
        private byte[]   mask;

        
        /**
         * For debug purposes only.
         */
        public String toString() {
            int i = 0;
            StringBuffer buf = new StringBuffer();

            buf.append("fast-check(");
            
            if (exts != null) {
                buf.append("exts:");            
                for (i = 0; i<exts.length; i++)
                    buf.append(exts[i] + ", ");
            }
            
            if (mimes != null) {
                buf.append("mimes:");
                for (i = 0; i<mimes.length; i++)
                    buf.append(mimes[i] + ", ");
            }
            
            if (fatts != null) {
                buf.append("file-attributes:");
                for (i = 0; i<fatts.length; i++)
                    buf.append(fatts[i] + "='" + vals[i] + "', ");
            }

            if (magic != null) {
                buf.append("magic:" + XMLUtil.toHex(magic, 0, magic.length));
            }
            
            if (mask != null) {
                buf.append("mask:" + XMLUtil.toHex(mask, 0, mask.length));
            }

            buf.append(")");
            
            return buf.toString();
        }
        
        private void addExt(String ext) {
            exts = Util.addString(exts, ext);
        }

        private void addMIME(String mime) {
            mimes = Util.addString(mimes, mime.toLowerCase());
        }
        
        private void addAttr(String name, String value) {
            fatts = Util.addString(fatts, name);
            vals = Util.addString(vals, value);
        }

        private boolean setMagic(byte[] magic, byte[] mask) {
            if (magic == null) return true;
            if (mask != null && magic.length != mask.length) return false;            
            this.magic = magic;
            if (mask != null) {
                this.mask = mask;
                for (int i = 0; i<mask.length; i++) {
                    this.magic[i] &= mask[i];
                }
            }
            return true;
        }
        
        private boolean accept(FileObject fo) throws IOException {
            
            // check for resource extension

            if (exts != null && fo.getExt() != null) {
                if (Util.contains(exts, fo.getExt())) return true;
            }
            
            // check for resource mime type

            if (mimes != null) {
                for (int i = mimes.length -1 ; i>=0; i--) {
                    String s = FileUtil.getMIMEType(fo.getExt());  //!!! how to obtain resource MIME type as classified by lower layers?
                    if (s == null) continue;

                    // RFC2045; remove content type paramaters and ignore case

                    int l = s.indexOf(';');
                    if (i>=0) s = s.substring(0, l-1);
                    s = s.toLowerCase();
                    if (s.equals(mimes[i])) return true;

                    // RFC3023; allows "+xml" suffix

                    if (mimes[i].startsWith("+") && s.endsWith(mimes[i])) return true; // NOI18N
                }
            }
            
            // check for magic
            
            if (magic != null) {
                byte[] header = new byte[magic.length];

//                System.err.println("FO" + fo);
                
//                String m = mask == null ? "" : " mask " + XMLUtil.toHex(mask, 0, mask.length);
//                System.err.println("Magic test " + XMLUtil.toHex(magic, 0, magic.length) + m);
                
                // fetch header
                
                InputStream in = fo.getInputStream();
                boolean unexpectedEnd = false;
                for (int i = 0; i<magic.length; ) {
                    try {
                        int read = in.read(header, i, magic.length-i);
                        if (read < 0) unexpectedEnd = true;
                        i += read;
                    } catch (IOException ex) {
                        unexpectedEnd = true;
                        break;
                    }
                    if (unexpectedEnd) break;
                }

                try {
                    in.close();
                } catch (IOException ioe) {
                    // closed
                }
                
                
//                System.err.println("Header " + XMLUtil.toHex(header, 0, header.length));
                
                // compare it
                
                if ( unexpectedEnd == false ) {
                    boolean diff = false;
                    for (int i=0  ; i<magic.length; i++) {
                        if (mask != null) header[i] &= mask[i];
                        if (magic[i] != header[i]) {
                            diff = true;
                            break;
                        }
                    }

                    if (diff == false) return true;
                }
            }
            
            // check for fileobject attributes

            if (fatts != null) {
                for (int i = fatts.length -1 ; i>=0; i--) {
                    Object attr = fo.getAttribute(fatts[i]);
                    if (attr != null) {
                        if (vals[i] == null) return true;                    
                        if (vals[i].equals(attr.toString())) return true;
                    }
                }
            }
            
            // no one template matched
            
            return false;
        }
        
    }
}
