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

package org.netbeans.core.filesystems;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * This source represents a <b>XML rules</b> core plugin to <tt>MIMEReolverImpl</tt>.
 *
 * @author  Petr Kuzel
 * @version
 */
final class XMLMIMEComponent extends DefaultParser implements MIMEComponent {

    private short state = INIT;
    
    // template obtained form parsed description
    private final Smell template = new Smell();

    // cached and reused parser used for sniffing    
    private static final LocalSniffingParser local = new LocalSniffingParser();

    // FileObjectFilter ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public boolean acceptFileObject(FileObject fo) {

        // it may come from arbitrary thread
        // retrive per thread instance
        
        SniffingParser sniffer = local.getParser();
        Smell print = sniffer.sniff(fo);
//        System.err.println("Print of " + fo);
//        System.err.println("print " + print);
//        System.err.println("template " + template);
        return template.match(print);
    }

    public String toString() {
       return template.toString();
    }

    // XML description -> memory representation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // pseudo validation states
    private static final short INIT = 0;
    private static final short IN_ROOT = 1;
    private static final short IN_DOCTYPE = 2;
    private static final short IN_ELEMENT = 3;

    // grammar elements
    private static final String ROOT = "xml-rule"; // NOI18N
    private static final String PI = "pi"; // NOI18N
    private static final String ELEMENT = "element"; // NOI18N
    private static final String DOCTYPE  = "doctype"; // NOI18N
    private static final String PUBLIC_ID = "public-id"; // NOI18N
    private static final String ID = "id"; // NOI18N
    private static final String ATTR = "attr"; // NOI18N
    private static final String NAME = "name"; // NOI18N
    private static final String VALUE = "text"; // NOI18N
    private static final String NS = "ns"; // NOI18N
    private static final String TARGET = "target"; // NOI18N


    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        String s;

        switch (state) {

            case INIT:
                if (ROOT.equals(qName) ==  false) error();
                state = IN_ROOT;
                break;

            case IN_ROOT:
                if (PI.equals(qName)) {
                    s = atts.getValue(TARGET); if (s == null) error();
                    template.addPI(s);

                    //!!! TODO presudo atts

                } else if (DOCTYPE.equals(qName)) {
                    s = atts.getValue(PUBLIC_ID);
                    if (s == null) {
                        state = IN_DOCTYPE;
                        break;
                    } else {
                        template.addDoctype(s);
                    }

                } else if (ELEMENT.equals(qName)) {

                    s = atts.getValue(NAME);
                    if (s == null) {
                        s = atts.getValue(NS);
                        if (s != null) template.addElementNS(s);
                    } else {
                        template.addElementName(s);
                        s = atts.getValue(NS);
                        if (s != null) template.addElementNS(s);
                    }

                    state = IN_ELEMENT;

                } else {
                    error();
                }
                break;

            case IN_DOCTYPE:
                if (PUBLIC_ID.equals(qName) == false) error();
                s = atts.getValue(ID); if (s == null) error();
                template.addDoctype(s);
                break;

            case IN_ELEMENT:
                if (ATTR.equals(qName)) {
                    s = atts.getValue(NAME); if (s == null) error();
                    template.addElementAtt(s, atts.getValue(VALUE));

                } else if (NS.equals(qName)) {
                    s = atts.getValue(NAME); if (s == null) error();
                    template.addElementNS(s);

                } else {
                    error();
                }

        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {

        switch (state) {
            case IN_ELEMENT:
                if (ELEMENT.equals(qName)) state = IN_ROOT;
                break;      

            case IN_DOCTYPE:
                if (DOCTYPE.equals(qName)) state = IN_ROOT;
                break;
        }
    }
    
    // Sniffing parser ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    

    /**
     * Create just one shared parser instance per thread.
     * Consequently one instance cannot be run in paralel eliminating need for sync.
     */
    private static class LocalSniffingParser extends ThreadLocal<WeakReference<SniffingParser>> {
        LocalSniffingParser() {}
        
        private WeakReference<SniffingParser> wref = null;
        
        protected WeakReference<SniffingParser> initialValue() {            
            SniffingParser parser = new SniffingParser();
            wref = new WeakReference<SniffingParser>(parser);
            return wref;
        }
        
        public SniffingParser getParser() {
            WeakReference<SniffingParser> cache = get();
            SniffingParser cached = cache.get();
            if (cached == null) {
                cached = new SniffingParser();
                wref = new WeakReference<SniffingParser>(cached);                
                super.set(wref);
            }
            return cached;            
        }
        
        public void set(WeakReference<SniffingParser> data) {
            // we are read only!
        }
    }

        
    /**
     * Parser that test XML Document header.
     */
    private static class SniffingParser extends DefaultParser implements LexicalHandler {

        SniffingParser() {
            super(null);
        }

        // last succesfully sniffed fileobject
        private FileObject lastFileObject = null;
        
        private Smell print = null;
        
        // the only way how to stop parser is throwing an exception
        private static final SAXException STOP = new SAXException("STOP");  //NOI18N

        /**
         * Go ahead and retrieve a print or null
         */
        protected Smell sniff(FileObject fo) {

            if (fo == null) return null;
            
            if (fo.equals(lastFileObject)) return print;
            
            if (fo.isValid() == false) return null;

            if (fo.getSize() == 0) return null;
            
            print = new Smell();
            parse(fo);
            if (this.state == ERROR) {
                return null;
            }
            
            lastFileObject = fo;
            return print;
        }
        
        protected XMLReader createXMLReader() {
            XMLReader parser = null;
            
            try {
                parser = XMLUtil.createXMLReader(false, true);           
                try {
                    parser.setProperty("http://xml.org/sax/properties/lexical-handler", this);  //NOI18N
                } catch (SAXException sex) {
                    Logger.global.fine(NbBundle.getMessage(XMLMIMEComponent.class, "W-003"));  //NOI18N
                }
            } catch (SAXException ex) {
                Logger.global.log(Level.WARNING, null, ex);
            }
            return parser;
        }
        
        protected boolean isStopException(Exception e) {
            return STOP.getMessage().equals(e.getMessage());
        }        
        
        
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {            
            if (namespaceURI != null) {
                print.addElementNS(namespaceURI);
            }
            if ("".equals(localName)) localName = null;  //#16484  //NOI18N
            print.addElementName(localName != null ? localName : qName);
            for (int i = 0; i<atts.getLength(); i++) {
                print.addElementAtt(atts.getQName(i), atts.getValue(i));
            }
            throw STOP;
        }
        
        public void processingInstruction(String target, String data) throws SAXException {
            print.addPI(target);
        }
        
        // LexicalHandler

        public void startDTD(String root, String pID, String sID) throws SAXException {
            print.addDoctype(pID);
        }

        public void endDTD() {}

        public void startEntity(String name) {}

        public void endEntity(String name) {}

        public void startCDATA() {}

        public void endCDATA() {}

        public void comment(char[] ch, int start, int length) {}
        
        public void error(SAXParseException exception) throws SAXException {            
            // we are not validating should not occure
            Logger.global.warning(exception.getMessage());
            this.state = ERROR;
            throw STOP;
        }

        public void fatalError(SAXParseException exception) throws SAXException {

            // it may be caused by wrong user XML documents, notify only in debug mode
            // also see #16484 if the error message makes no sense
            Logger emgr = Logger.getLogger("org.netbeans.core.filesystems.XMLMIMEComponent"); // NOI18N
            if (emgr.isLoggable(Level.FINE)) {
                emgr.fine("[while parsing " + fo + "] " + exception.getSystemId() + ":" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ": " + exception.getMessage()); // NOI18N
            }

            this.state = ERROR;
            throw STOP;
        }
        
        
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
     * Template smell per resolver and print data per FileObject.
     */
    private static class Smell {
        Smell() {}
        
        private String[] doctypes = null;
        private String[] pis = null;
        
        private String   root = null;
        private String[] nss = null;
        
        private String[] attns = null;
        private String[] attvs = null;

        public String toString() {
            StringBuffer buf = new StringBuffer();
            int i = 0;
            buf.append("xml-check(");
            
            if (doctypes != null) {
                buf.append("doctypes:");
                for (i = 0; i<doctypes.length; i++)
                    buf.append(doctypes[i]).append(", ");
            }

            if (pis != null) {
                buf.append("PIs:");
                for (i = 0; i<pis.length; i++)
                    buf.append(pis[i]).append(", ");
            }

            if (root != null) {
               buf.append("root:").append(root);
            }

            if (nss != null) {
                buf.append("root-namespaces:");
                for (i = 0; i<nss.length; i++)
                    buf.append(nss[i]).append(", ");
            }

            if (attns != null) {
                buf.append("attributes:");
                for (i = 0; i<attns.length; i++)
                    buf.append(attns[i]).append("='").append(attvs[i]).append("'");
            }

            buf.append(')');
            return buf.toString();

        }

        private void addDoctype(String s) {
            if (doctypes == null) {
                doctypes = new String[] { s };
            } else {
                doctypes = Util.addString(doctypes, s);
            }            
        }
        
        private void addPI(String s) {
            if (pis == null) {
                pis = new String[] { s };
            } else {
                pis = Util.addString(pis, s);
            }                        
        }
        
        private void addElementNS(String s) {
            if (nss == null) {
                nss = new String[] { s };
            } else {
                nss = Util.addString(nss, s);
            }                        
        }
        
        private void addElementName(String name) {
            root = name;
        }
        
        private void addElementAtt(String name, String value) {
            if (attns == null) {
                attns = new String[] {name};
                attvs = new String[] {value};
            } else {
                attns = Util.addString(attns, name);
                attvs = Util.addString(attvs, value);
            }
            
        }

        /**
         * Matches passed data this template?
         * Any of constructs must match.
         */
        public boolean match(Smell t) {

            if (t == null) return false;
            
            // try if a doctype public-id matches
            
            if (doctypes != null && t.doctypes != null) {
                if (Util.contains(doctypes, t.doctypes[0])) return true;
            }
            
            // try root element match
            
            if (root != null && root.equals(t.root)) {
                if (nss == null) {                                                            
                    if (attMatch(t)) return true;
                } else {                                        
                    if (t.nss != null && Util.contains(nss, t.nss[0])) {
                        if (attMatch(t)) return true;
                    }                    
                }
            } else {
                if (root == null && nss != null && t.nss != null && Util.contains(nss, t.nss[0])) {
                    if (attMatch(t)) return true;
                }                                    
            }
            
            // try if a PI matches
            
            if (pis != null && t.pis!=null) {
                for (int i = 0; i<pis.length; i++) {
                    for (int j = 0; j<t.pis.length; j++) {
                        if (pis[i].equals(t.pis[j])) return true;
                    }
                }
            }            
            
            return false;
        }
        
        
        private boolean attMatch(Smell t) {

            if (attns == null) return true;
            if (t.attns == null) return false;
            
            // all attributes must match by name ...
            for (int i = 0 ; i<attns.length; i++) {
                int match = Util.indexOf(t.attns, attns[i]);
                if (match == -1) {
                    return false;
                }

                // ... and value if specified in template

                if (attvs[i] != null && (!attvs[i].equals(t.attvs[match]))) {
                    return  false;
                }
            }
            
            return true;
            
        }

    }
}
