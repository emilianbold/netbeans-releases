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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

    // notification limit in bytes for reading file content. It should not exceed 4192 (4kB) because it is read in one disk touch.
    private static final int READ_LIMIT = 4000;
    private static Set<String> readLimitReported = new HashSet<String>();

    // constants for user defined declarative MIME resolver
    private static final String MIME_RESOLVERS_PATH = "Services/MIMEResolver";  //NOI18N
    private static final String USER_DEFINED_MIME_RESOLVER = "user-defined-mime-resolver";  //NOI18N
    /** Position of user-defined mime resolver. Need to very low to override all other resolvers. */
    private static final int USER_DEFINED_MIME_RESOLVER_POSITION = 10;

    public static MIMEResolver forDescriptor(FileObject fo) {
        return new Impl(fo);
    }

    /** Check whether given resolver is declarative. */
    public static boolean isDeclarative(MIMEResolver resolver) {
        return resolver instanceof Impl;
    }

    /** Returns resolvable MIME Types for given declarative resolver. */
    public static String[] getMIMETypes(MIMEResolver resolver) {
        ((Impl) resolver).init();  // #171312 - resolver must be parsed
        return ((Impl)resolver).implResolvableMIMETypes;
    }

    /** Check whether given resolver's FileObject is user defined.
     * @param mimeResolverFO resolver's FileObject
     * @return true if specified FileObject is user defined MIME resolver, false otherwise
     */
    public static boolean isUserDefined(FileObject mimeResolverFO) {
        return mimeResolverFO.getAttribute(USER_DEFINED_MIME_RESOLVER) != null;
    }

    /** Returns mapping of MIME type to set of extensions. It never returns null,
     * it can return empty set of extensions.
     * @param fo MIMEResolver FileObject
     * @return mapping of MIME type to set of extensions like
     * {@literal {image/jpeg=[jpg, jpeg], image/gif=[]}}.
     */
    public static Map<String, Set<String>> getMIMEToExtensions(FileObject fo) {
        if (!fo.hasExt("xml")) { // NOI18N
            return Collections.emptyMap();
        }
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        Impl impl = new Impl(fo);
        impl.parseDesc();
        FileElement[] elements = impl.smell;
        if (elements != null) {
            for (FileElement fileElement : elements) {
                String mimeType = fileElement.getMimeType();
                if (mimeType != null) {  // can be null if <exit/> element is used
                    String[] extensions = fileElement.getExtensions();
                    Set<String> extensionsSet = new HashSet<String>();
                    if (extensions != null) {
                        for (String extension : extensions) {
                            if (extension.length() > 0) {  // ignore empty extension
                                extensionsSet.add(extension);
                            }
                        }
                    }
                    Set<String> previous = result.get(mimeType);
                    if (previous != null) {
                        extensionsSet.addAll(previous);
                    }
                    result.put(mimeType, extensionsSet);
                }
            }
        }
        return result;
    }

    /** Returns FileObject representing declarative user defined MIME resolver
     * or null if not yet created.
     * @return FileObject representing declarative user defined MIME resolver
     * or null if not yet created.
     */
    public static FileObject getUserDefinedResolver() {
        FileObject resolversFolder = FileUtil.getConfigFile(MIME_RESOLVERS_PATH);
        if (resolversFolder != null) {
            FileObject[] resolvers = resolversFolder.getChildren();
            for (FileObject resolverFO : resolvers) {
                if (resolverFO.getAttribute(USER_DEFINED_MIME_RESOLVER) != null) {
                    return resolverFO;
                }
            }
        }
        return null;
    }

    /** Stores declarative resolver corresponding to specified mapping of MIME type
     * and set of extensions. This resolver has the highest priority. Usually
     * it resides in userdir/config/Servicer/MIMEResolver.
     * @param mimeToExtensions mapping of MIME type to set of extensions like
     * {@literal {image/jpeg=[jpg, jpeg], image/gif=[]}}.
     */
    public static void storeUserDefinedResolver(final Map<String, Set<String>> mimeToExtensions) {
        Parameters.notNull("mimeToExtensions", mimeToExtensions);  //NOI18N
        FileObject userDefinedResolverFO = getUserDefinedResolver();
        if (userDefinedResolverFO != null) {
            try {
                // delete previous resolver because we need to refresh MIMEResolvers
                userDefinedResolverFO.delete();
            } catch (IOException e) {
                ERR.log(Level.SEVERE, "Cannot delete resolver " + FileUtil.toFile(userDefinedResolverFO), e);  //NOI18N
                return;
            }
        }
        if (mimeToExtensions.isEmpty()) {
            // nothing to write
            return;
        }
        FileUtil.runAtomicAction(new Runnable() {

            public void run() {
                Document document = XMLUtil.createDocument("MIME-resolver", null, "-//NetBeans//DTD MIME Resolver 1.1//EN", "http://www.netbeans.org/dtds/mime-resolver-1_1.dtd");  //NOI18N
                for (String mimeType : mimeToExtensions.keySet()) {
                    Set<String> extensions = mimeToExtensions.get(mimeType);
                    if (!extensions.isEmpty()) {
                        Element fileElement = document.createElement("file");  //NOI18N
                        for (String extension : mimeToExtensions.get(mimeType)) {
                            Element extElement = document.createElement("ext");  //NOI18N
                            extElement.setAttribute("name", extension);  //NOI18N
                            fileElement.appendChild(extElement);
                        }
                        Element resolverElement = document.createElement("resolver");  //NOI18N
                        resolverElement.setAttribute("mime", mimeType);  //NOI18N
                        fileElement.appendChild(resolverElement);
                        document.getDocumentElement().appendChild(fileElement);
                    }
                }
                if (!document.getDocumentElement().hasChildNodes()) {
                    // nothing to write
                    return;
                }
                OutputStream os = null;
                FileObject userDefinedResolverFO = null;
                try {
                    FileObject resolversFolder = FileUtil.getConfigFile(MIME_RESOLVERS_PATH);
                    if (resolversFolder == null) {
                        resolversFolder = FileUtil.createFolder(FileUtil.getConfigRoot(), MIME_RESOLVERS_PATH);
                    }
                    userDefinedResolverFO = resolversFolder.createData(USER_DEFINED_MIME_RESOLVER, "xml");  //NOI18N
                    userDefinedResolverFO.setAttribute(USER_DEFINED_MIME_RESOLVER, Boolean.TRUE);
                    userDefinedResolverFO.setAttribute("position", USER_DEFINED_MIME_RESOLVER_POSITION);  //NOI18N
                    os = userDefinedResolverFO.getOutputStream();
                    XMLUtil.write(document, os, "UTF-8"); //NOI18N
                } catch (IOException e) {
                    ERR.log(Level.SEVERE, "Cannot write resolver " + (userDefinedResolverFO == null ? "" : FileUtil.toFile(userDefinedResolverFO)), e);  //NOI18N
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            ERR.log(Level.SEVERE, "Cannot close OutputStream of file " + (userDefinedResolverFO == null ? "" : FileUtil.toFile(userDefinedResolverFO)), e);  //NOI18N
                        }
                    }
                }
            }
        });
    }

    /** Returns map of all registered MIMEResolver instances in revers order,
     * i.e. first are ones with lower priority (position attribute higher)
     * and last are ones with highest prority (position attribute lower).
     * @return map of all registered MIMEResolver instances in revers order
     * (highest priority last)
     */
    public static Map<Integer, FileObject> getOrderedResolvers() {
        // scan resolvers and order them to be able to assign extension to mime type from resolver with the lowest position
        FileObject[] resolvers = FileUtil.getConfigFile(MIME_RESOLVERS_PATH).getChildren();
        TreeMap<Integer, FileObject> orderedResolvers = new TreeMap<Integer, FileObject>(Collections.reverseOrder());
        for (FileObject mimeResolverFO : resolvers) {
            Integer position = (Integer) mimeResolverFO.getAttribute("position");  //NOI18N
            if (position == null) {
                position = Integer.MAX_VALUE;
            }
            while (orderedResolvers.containsKey(position)) {
                position--;
            }
            orderedResolvers.put(position, mimeResolverFO);
        }
        return orderedResolvers;
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
            if (fo.hasExt("xml") && fo.getPath().startsWith(MIME_RESOLVERS_PATH)) { // NOI18N
                // do not try to check ourselves!
                return null;
            }

            init();
            if (state == DescParser.ERROR) {
                return null;
            }

            FileElement[] smell2 = smell;  //#163378, #157838 - copy to prevent concurrent modification and not synchronize to prevent deadlock
            // smell is filled in reverse order
            for (int i = smell2.length - 1; i >= 0; i--) {
                if (ERR.isLoggable(Level.FINE)) ERR.fine("findMIMEType - smell.resolve.");
                String s = smell2[i].resolve(fo);
                if (s != null) {
                    if (s.equals(FileElement.EXIT_MIME_TYPE)) {
                        // if file matches conditions and exit element is present, do not continue in loop and return null
                        return null;
                    }
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
        @Override
        public String toString() {
            return "MIMEResolverImpl.Impl[" + data.getPath() + "]";  // NOI18N
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
        // holds level of pattern element
        private int patternLevel = 0;
        // used to prohibit more pattern elements on the same level
        Set<Integer> patternLevelSet;


        DescParser(FileObject fo) {
            super(fo);
        }

        // pseudo validation states
        private static final short IN_ROOT = 1;
        private static final short IN_FILE = 2;
        private static final short IN_RESOLVER = 3;
        private static final short IN_COMPONENT = 4;
        private static final short IN_PATTERN = 5;

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
        private static final String PATTERN = "pattern"; // NOI18N
        private static final String VALUE = "value"; // NOI18N
        private static final String RANGE = "range"; // NOI18N
        private static final String IGNORE_CASE = "ignorecase"; // NOI18N
        private static final String SUBSTRING = "substring"; // NOI18N
        private static final String MAGIC = "magic"; // NOI18N
        private static final String HEX = "hex"; // NOI18N
        private static final String MASK = "mask"; // NOI18N
        private static final String TEXT = "text"; // NOI18N
        private static final String EXIT = "exit"; // NOI18N
        private static final String XML_RULE_COMPONENT = "xml-rule";  // NOI18N

        @Override
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
                        String val = atts.getValue(TEXT);
                        template[0].fileCheck.addAttr(s, val);                        

                    } else if (PATTERN.equals(qName)) {

                        s = atts.getValue(VALUE); if (s == null) error();
                        int range = Integer.valueOf(atts.getValue(RANGE));
                        assert range <= READ_LIMIT || !readLimitReported.add(fo.getPath()): "MIME resolver " + fo.getPath() + " should not exceed " + READ_LIMIT + " bytes limit for files content check.";  //NOI18N
                        boolean ignoreCase = Type.FilePattern.DEFAULT_IGNORE_CASE;
                        String ignoreCaseAttr = atts.getValue(IGNORE_CASE);
                        if (ignoreCaseAttr != null) {
                            ignoreCase = Boolean.valueOf(ignoreCaseAttr);
                        }
                        if (file_state == IN_PATTERN) {
                            if (patternLevelSet == null) {
                                patternLevelSet = new HashSet<Integer>();
                            }
                            if (!patternLevelSet.add(patternLevel)) {
                                error("Second pattern element on the same level not allowed");  //NOI18N
                            }
                            template[0].fileCheck.addInnerPattern(s, range, ignoreCase);
                        } else {
                            template[0].fileCheck.addPattern(s, range, ignoreCase);
                            file_state = IN_PATTERN;
                        }
                        patternLevel++;
                        break;

                    } else if (NAME.equals(qName)) {

                        s = atts.getValue(NAME); if (s == null) error();
                        String substringAttr = atts.getValue(SUBSTRING);
                        boolean substring = Type.FileName.DEFAULT_SUBSTRING;
                        if (substringAttr != null) {
                            substring = Boolean.valueOf(substringAttr);
                        }
                        boolean ignoreCase = Type.FileName.DEFAULT_IGNORE_CASE;
                        String ignoreCaseAttr = atts.getValue(IGNORE_CASE);
                        if (ignoreCaseAttr != null) {
                            ignoreCase = Boolean.valueOf(ignoreCaseAttr);
                        }
                        template[0].fileCheck.addName(s, substring, ignoreCase);
                        break;

                    } else if (RESOLVER.equals(qName)) {

                        if (template[0].fileCheck.exts == null 
                            && template[0].fileCheck.mimes == null 
                            && template[0].fileCheck.fatts == null
                            && template[0].fileCheck.patterns == null
                            && template[0].fileCheck.names == null
                            && template[0].fileCheck.magic == null) {
                                error();  // at least one must be specified
                        }

                        s = atts.getValue(MIME); if (s == null) error();
                        template[0].setMIME(s);

                        state = IN_RESOLVER;
                        
                        break;

                    } else if (EXIT.equals(qName)) {
                        template[0].fileCheck.setExit();
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
        
        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            switch (state) {
                case IN_FILE:
                    if (FILE.equals(qName)) {
                        state = IN_ROOT;
                        file_state = INIT;
                    }
                    if (PATTERN.equals(qName)) {
                        if (--patternLevel == 0) {
                            patternLevelSet = null;
                            file_state = INIT;
                        }
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

        @Override
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
        // unique string to mark exit condition
        private static final String EXIT_MIME_TYPE = "mime-type-to-exit";  //NOI18N

        private String[] getExtensions() {
            return fileCheck.exts;
        }
        
        private String getMimeType() {
            return mime;
        }
        
        private boolean isExit() {
            return fileCheck.exit;
        }

        private void setMIME(String mime) {
            if ("null".equals(mime)) return;  // NOI18N
            this.mime = mime;
        }
        
        private String resolve(FileObject file) {
                        
            try {
                if (fileCheck.accept(file)) {
                    if (rule != null && !rule.acceptFileObject(file)) {
                        return null;
                    }
                    if (isExit() || mime == null) {
                        // all matched but exit element was found or mime attribute of resolver element is null => escape this resolver
                        return EXIT_MIME_TYPE;
                    }
                    // all matched
                    return mime;
                }
            } catch (IOException io) {
                Logger.getLogger(MIMEResolverImpl.class.getName()).log(Level.INFO, null, io);
            }
            return null;
        }
        
        /**
         * For debug puroses only.
         */
        @Override
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
        private static final String EMPTY_EXTENSION = "";  //NOI18N
        private String[] mimes;
        private String[] fatts;
        private List<FilePattern> patterns;
        private FilePattern lastAddedPattern = null;
        private List<FileName> names;
        private String[] vals;   // contains null or value of attribute at the same index
        private byte[]   magic;
        private byte[]   mask;
        private boolean exit = false;

        /** Used to search in the file for given pattern in given range. If there is an inner
         * pattern element, it is used only if outer is fulfilled. Searching starts
         * always from the beginning of the file. For example:
         * <p>
         * Pattern &lt;?php in first 255 bytes
         * <pre>
         *      &lt;pattern value="&lt;?php" range="255"/&gt;
         * </pre>
         * </p>
         * <p>
         * Pattern &lt;HTML&gt;> or &lt;html&gt; in first 255 bytes and pattern &lt;?php in first 4000 bytes.
         * <pre>
         *      &lt;pattern value="&lt;HTML&gt;" range="255" ignorecase="true"&gt;
         *          &lt;pattern value="&lt;?php" range="4000"/&gt;
         *      &lt;/pattern&gt;
         * </pre>
         * </p>
         */
        private class FilePattern {
            // case sensitive by default
            private static final boolean DEFAULT_IGNORE_CASE = false;
            private final String value;
            private final int range;
            private final boolean ignoreCase;
            private FilePattern inner;
            private final byte[] bytes;
            private final int valueLength;

            public FilePattern(String value, int range, boolean ignoreCase) {
                this.value = value;
                this.valueLength = value.length();
                if (ignoreCase) {
                    this.bytes = value.toLowerCase().getBytes();
                } else {
                    this.bytes = value.getBytes();
                }
                this.range = range;
                this.ignoreCase = ignoreCase;
            }

            public void setInner(FilePattern inner) {
                this.inner = inner;
            }

            private boolean match(byte b, AtomicInteger pointer) {
                if (b == bytes[pointer.get()]) {
                    return pointer.incrementAndGet() >= valueLength;
                } else {
                    pointer.set(0);
                    return false;
                }
            }

            /** Read from given file and compare byte-by-byte if pattern
             * appers in given range.
             */
            public boolean match(FileObject fo) throws IOException {
                InputStream is = null;
                boolean matched = false;
                try {
                    is = fo.getInputStream();  // it is CachedInputStream, so you can call getInputStream and read more times without performance penalty
                    byte[] byteRange = new byte[range];
                    int read = is.read(byteRange);
                    AtomicInteger pointer = new AtomicInteger(0);
                    for (int i = 0; i < read; i++) {
                        byte b = byteRange[i];
                        if (ignoreCase) {
                            b = (byte) Character.toLowerCase(b);
                        }
                        if (match(b, pointer)) {
                            matched = true;
                            break;
                        }
                    }
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException ioe) {
                        // already closed
                    }
                }
                if (matched) {
                    if (inner == null) {
                        return true;
                    } else {
                        return inner.match(fo);
                    }
                }
                return false;
            }

            @Override
            public String toString() {
                return "[" + value + ", " + range + ", " + ignoreCase + (inner != null ? ", " + inner : "") + "]";
            }
        }

        /** Used to compare filename with given name.
         * For example:
         * <p>
         * Filename matches makefile, Makefile, MaKeFiLe, mymakefile, gnumakefile, makefile1, ....
         * <pre>
         *      &lt;name name="makefile" substring="true"/&gt;
         * </pre>
         * </p>
         * <p>
         * Filename exactly matches rakefile or Rakefile.
         * <pre>
         *      &lt;name name="rakefile" ignorecase="false"/&gt;
         *      &lt;name name="Rakefile" ignorecase="false"/&gt;
         * </pre>
         * </p>
         */
        private class FileName {

            // case insensitive by default
            private static final boolean DEFAULT_IGNORE_CASE = true;
            private static final boolean DEFAULT_SUBSTRING = false;
            private final String name;
            private final boolean substring;
            private final boolean ignoreCase;

            public FileName(String name, boolean substring, boolean ignoreCase) {
                if (ignoreCase) {
                    this.name = name.toLowerCase();
                } else {
                    this.name = name;
                }
                this.substring = substring;
                this.ignoreCase = ignoreCase;
            }

            public boolean match(FileObject fo) {
                String nameAndExt = fo.getNameExt();
                if (ignoreCase) {
                    nameAndExt = nameAndExt.toLowerCase();
                }
                if (substring) {
                    return nameAndExt.contains(name);
                } else {
                    return nameAndExt.equals(name);
                }
            }

            @Override
            public String toString() {
                return "[" + name + ", " + substring + ", " + ignoreCase + "]";
            }
        }

        /**
         * For debug purposes only.
         */
        @Override
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

            if (patterns != null) {
                buf.append("patterns:");
                for (FilePattern pattern : patterns) {
                    buf.append(pattern.toString()).append(", ");
                }
            }

            if (names != null) {
                buf.append("names:");
                for (FileName name : names) {
                    buf.append(name.toString()).append(", ");
                }
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

        private void addPattern(String value, int range, boolean ignoreCase) {
            if (patterns == null) {
                patterns = new ArrayList<FilePattern>();
            }
            lastAddedPattern = new FilePattern(value, range, ignoreCase);
            patterns.add(lastAddedPattern);
        }

        private void addInnerPattern(String value, int range, boolean ignoreCase) {
            FilePattern inner = new FilePattern(value, range, ignoreCase);
            lastAddedPattern.setInner(inner);
            lastAddedPattern = inner;
        }

        private void addName(String name, boolean substring, boolean ignoreCase) {
            if (names == null) {
                names = new ArrayList<FileName>();
            }
            names.add(new FileName(name, substring, ignoreCase));
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

        private void setExit() {
            exit = true;
        }

        @SuppressWarnings("deprecation")
        private static String getMIMEType(String extension) {
            return FileUtil.getMIMEType(extension);
        }

        /** #26521, 114976 - ignore not readable and windows' locked files. */
        private static void handleIOException(FileObject fo, IOException ioe) throws IOException {
            if (fo.canRead()) {
                if (!Utilities.isWindows() || !(ioe instanceof FileNotFoundException) || !fo.isValid() || !fo.getName().toLowerCase().contains("ntuser")) {//NOI18N
                    throw ioe;
                }
            }
        }

        private boolean accept(FileObject fo) throws IOException {
            // check for resource extension
            if (exts != null) {
                String ext = fo.getExt();
                if (ext == null) {
                    ext = EMPTY_EXTENSION;
                }
                if (!Util.contains(exts, ext, CASE_INSENSITIVE)) {
                    return false;
                }
            }
            
            // check for resource mime type

            if (mimes != null) {
                boolean match = false;
                String s = getMIMEType(fo.getExt());  //from the very first implementation there is still question "how to obtain resource MIME type as classified by lower layers?"
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

                // fetch header

                InputStream in = null;
                try {
                    in = fo.getInputStream();
                    int read = in.read(header);
                    if (read < 0) {
                        return false;
                    }
                } catch (IOException openex) {
                    handleIOException(fo, openex);
                    return false;
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ioe) {
                        // already closed
                    }
                }

                // compare it

                for (int i = 0; i < magic.length; i++) {
                    if (mask != null) {
                        header[i] &= mask[i];
                    }
                    if (magic[i] != header[i]) {
                        return false;
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

            // check for patterns in file
            if (patterns != null) {
                try {
                    boolean matched = false;
                    for (FilePattern pattern : patterns) {
                        if(pattern.match(fo)) {
                            // at least one pattern matched => escape loop, otherwise continue
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        return false;
                    }
                } catch (IOException ioe) {
                    handleIOException(fo, ioe);
                    return false;
                }
            }

            // check file name
            if (names != null) {
                boolean matched = false;
                for (FileName name : names) {
                    if(name.match(fo)) {
                        // at least one matched => escape loop, otherwise continue
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }

            // all templates matched
            return true;
        }
    }
}
