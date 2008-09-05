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

package org.netbeans.modules.openide.filesystems.declmime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * MIMEResolver implementation driven by an XML document instance
 * following PUBLIC "-//NetBeans//DTD MIME Resolver 1.0//EN".
 *
 * @author  Petr Kuzel
 */
public final class MIMEResolverImpl {
    
    // enable some tracing
    private static final Logger ERR = Logger.getLogger(MIMEResolverImpl.class.getName());
        
    private static final boolean CASE_INSENSITIVE =
        Utilities.isWindows() || Utilities.getOperatingSystem() == Utilities.OS_VMS;
    
    public static MIMEResolver forDescriptor(FileObject fo) {
        return new Impl(fo);
    }

    /** Check whether given resolver is declarative. */
    public static boolean isDeclarative(MIMEResolver resolver) {
        return resolver instanceof Impl;
    }

    /** Returns resolvable MIME Types for given declarative resolver. */
    public static String[] getMIMETypes(MIMEResolver resolver) {
        return ((Impl)resolver).implResolvableMIMETypes;
    }
    

    /** Returns list of extension and MIME type pairs for given MIMEResolver
     * FileObject. The list can contain duplicates and also [null, MIME] pairs.
     * @param fo MIMEResolver FileObject
     * @return list of extension and MIME type pairs. The list can contain 
     * duplicates and also [null, MIME] pairs.
     */
    public static List<String[]> getExtensionsAndMIMETypes(FileObject fo) {
        assert fo.getPath().startsWith("Services/MIMEResolver");  //NOI18N
        if (!fo.hasExt("xml")) { // NOI18N
            return Collections.emptyList();
        }
        ArrayList<String[]> result = new ArrayList<String[]>();
        Impl impl = new Impl(fo);
        impl.parseDesc();
        FileElement[] elements = impl.smell;
        if(elements != null) {
            for (int i = 0; i < elements.length; i++) {
                FileElement fileElement = elements[i];
                String mimeType = fileElement.getMimeType();
                String[] extensions = fileElement.getExtensions();
                if(extensions != null) {
                    for (int j = 0; j < extensions.length; j++) {
                        result.add(new String[] {extensions[j], mimeType});
                    }
                } else {
                    result.add(new String[] {null, mimeType});
                }
            }
        }
        return result;
    }
    
    // MIMEResolver ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static class Impl extends MIMEResolver {
        // This file object describes rules that drive ths instance
        private final FileObject data;

        private final FileChangeListener listener = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                synchronized (Impl.this) {
                    state = DescParser.INIT;
                    implResolvableMIMETypes = null;
                }
            }
        };

        // Resolvers in reverse order
        private FileElement[] smell = null;
                
        private short state = DescParser.INIT;

        private String[] implResolvableMIMETypes = null;

        @SuppressWarnings("deprecation")
        Impl(FileObject obj) {
            if (ERR.isLoggable(Level.FINE)) ERR.fine("MIMEResolverImpl.Impl.<init>(" + obj + ")");  // NOI18N
            data = obj;
            data.addFileChangeListener(FileUtil.weakFileChangeListener(listener, data));
        }

        public String findMIMEType(FileObject fo) {
            if (fo.hasExt("xml") && fo.getPath().startsWith("Services/MIMEResolver")) { // NOI18N
                // do not try to check ourselves!
                return null;
            }

            init();
            if (state == DescParser.ERROR) {
                return null;
            }

            // smell is filled in reverse order
            
            for (int i = smell.length-1; i>=0; i--) {
                String s = smell[i].resolve(fo);
                if (s != null) {
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("MIMEResolverImpl.findMIMEType(" + fo + ")=" + s);  // NOI18N
                    return s;
                }
            }
            
            return null;
        }

        private void init() {
            synchronized (this) {  // lazy init
                if (state == DescParser.INIT) {
                    state = parseDesc();
                }
            }
        }

        // description document is parsed in the same thread
        private short parseDesc() {
            smell = new FileElement[0];
            DescParser parser = new DescParser(data);
            parser.parse();
            smell = (parser.template != null) ? parser.template : smell;
            if (ERR.isLoggable(Level.FINE)) {
                if (parser.state == DescParser.ERROR) {
                    ERR.fine("MIMEResolverImpl.Impl parsing error!");
                } else {
                    StringBuffer buf = new StringBuffer();
                    buf.append("Parse: ");
                    for (int i = 0; i<smell.length; i++)
                        buf.append('\n').append(smell[i]);
                    ERR.fine(buf.toString());
                }
            }
            // fill resolvableMIMETypes array with available MIME types
            if(parser.state != DescParser.ERROR) {
                for (int i = 0; i < smell.length; i++) {
                    String mimeType = smell[i].getMimeType();
                    if(mimeType != null) {
                        implResolvableMIMETypes = Util.addString(implResolvableMIMETypes, mimeType);
                    }
                }
            }
            return parser.state;
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
                        String reason = "Unexpected element:  " + qName;
                        error(reason);
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
            template[0].rule = component;
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
                        component.setDocumentLocator(null);
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
        FileElement() {}
        
        private Type fileCheck = new Type();
        private String mime = null;
        private MIMEComponent rule = null;

        private String[] getExtensions() {
            return fileCheck.exts;
        }
        
        private String getMimeType() {
            return mime;
        }
        
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
                Logger.getLogger(MIMEResolverImpl.class.getName()).log(Level.INFO, null, io);
            }
            return null;
        }
        
        /**
         * For debug puroses only.
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("FileElement(");
            buf.append(fileCheck).append(' ');
            buf.append(rule).append(' ');
            buf.append("Result:").append(mime);
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
        Type() {}
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
                    buf.append(exts[i]).append(", ");
            }
            
            if (mimes != null) {
                buf.append("mimes:");
                for (i = 0; i<mimes.length; i++)
                    buf.append(mimes[i]).append(", ");
            }
            
            if (fatts != null) {
                buf.append("file-attributes:");
                for (i = 0; i<fatts.length; i++)
                    buf.append(fatts[i]).append("='").append(vals[i]).append("', ");
            }

            if (magic != null) {
                buf.append("magic:").append(XMLUtil.toHex(magic, 0, magic.length));
            }
            
            if (mask != null) {
                buf.append("mask:").append(XMLUtil.toHex(mask, 0, mask.length));
            }

            buf.append(')');
            
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

        @SuppressWarnings("deprecation")
        private static String getMIMEType(String extension) {
            return FileUtil.getMIMEType(extension);
        }
        private boolean accept(FileObject fo) throws IOException {
            // check for resource extension
            if (exts != null) {
                if (fo.getExt() == null) return false;
                if (!Util.contains(exts, fo.getExt(), CASE_INSENSITIVE)) return false;
            }
            
            // check for resource mime type

            if (mimes != null) {
                boolean match = false;
                String s = getMIMEType(fo.getExt());
                if (s == null) return false;

                // RFC2045; remove content type paramaters and ignore case
                int l = s.indexOf(';');
                if (l>=0) s = s.substring(0, l);
                s = s.toLowerCase();

                for (int i = mimes.length -1 ; i>=0; i--) {
                    if (s.equals(mimes[i])) {
                        match = true;
                        break;
                    }

                    // RFC3023; allows "+xml" suffix
                    if (mimes[i].length() > 0 && mimes[i].charAt(0) == '+' && s.endsWith(mimes[i])) {
                        match = true;
                        break;
                    }
                }
                if (!match) return false;
            }
            
            // check for magic
            
            if (magic != null) {
                byte[] header = new byte[magic.length];

//                System.err.println("FO" + fo);
                
//                String m = mask == null ? "" : " mask " + XMLUtil.toHex(mask, 0, mask.length);
//                System.err.println("Magic test " + XMLUtil.toHex(magic, 0, magic.length) + m);
                
                // fetch header

                InputStream in = null;
                boolean unexpectedEnd = false;
                try {
                    in = fo.getInputStream();
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
                } catch (IOException openex) {
                    unexpectedEnd = true;
                    boolean isBug114976 = false;
                    if (Utilities.isWindows() && fo.canRead()  && (openex instanceof FileNotFoundException)) {
                        if (fo.isValid() && fo.getName().toLowerCase().indexOf("ntuser") != -1) {//NOI18N
                            isBug114976 = true;
                        }
                    }
                    
                    if (fo.canRead() == true && !isBug114976) {
                        throw openex;
                    } else {
                        // #26521  silently do not recognize it
                    }
                } finally {
                    try {
                        if (in != null) in.close();
                    } catch (IOException ioe) {
                        // already closed
                    }
                }


//                System.err.println("Header " + XMLUtil.toHex(header, 0, header.length));
                
                // compare it
                
                if ( unexpectedEnd ) {
                    return false;
                } else {
                    for (int i=0  ; i<magic.length; i++) {
                        if (mask != null) header[i] &= mask[i];
                        if (magic[i] != header[i]) {
                            return false;
                        }
                    }
                }
            }
            
            // check for fileobject attributes

            if (fatts != null) {
                for (int i = fatts.length -1 ; i>=0; i--) {
                    Object attr = fo.getAttribute(fatts[i]);
                    if (attr != null) {
                        if (!attr.toString().equals(vals[i]) && vals[i] != null) return false;
                    }
                }
            }
            
            // all templates matched
            return true;
        }
        
    }
}
