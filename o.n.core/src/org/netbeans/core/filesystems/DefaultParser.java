/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.filesystems;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.xml.*;
import org.openide.*;


/**
 * Implements default interruptible silent parser behaviour.
 * Errors can be tested by quering parser state.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
abstract class DefaultParser  extends DefaultHandler {

    protected FileObject fo;
    private Locator locator = null;

    protected short state = INIT;

    protected static final short PARSED = 1000;
    protected static final short ERROR = -1;
    protected static final short INIT = 0;

    protected DefaultParser() {        
    }
    
    protected DefaultParser(FileObject fo) {
        this.fo = fo;
    }

    /**
     * Preconfigure parser and return it.
     */
    protected XMLReader createXMLReader() throws IOException, SAXException {
        return XMLUtil.createXMLReader(false);
    }

    /**
     * Check if the given exception is one thrown from the handler
     * for stopping the parser.
     */
    protected boolean isStopException(Exception e) {
        return false;
    }

    /**
     * @return current parser state
     */
    protected short getState() {
        return state;
    }

    protected final Locator getLocator() {
        return locator;
    }
    
    /**
     * Parser content workarounding known parser implementation
     * problems.
     */
    protected void parse(FileObject fo) {
        state = INIT; // #15672
        InputStream is = null;
        this.fo = fo;
        try {
            XMLReader parser = createXMLReader();
            parser.setEntityResolver(this);
            parser.setErrorHandler(this);
            parser.setContentHandler(this);

//            try {
//                // do not read DTD
//                parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);  //NOI18N
//            } catch (SAXException ignore) {
//                // parsing may be slower :-(
//            }

            InputSource in = new InputSource();                
            is = fo.getInputStream();
            in.setByteStream(is);
            in.setSystemId(fo.getURL().toExternalForm());
            customizeInputSource(in);
            
            parser.parse(in);

        } catch (IOException io) {
            if (!isStopException(io)) {
                if (fo.isValid() && fo.canRead()) {
                    ErrorManager emgr = ErrorManager.getDefault();
                    emgr.log("While parsing: " + fo.toString());
                    emgr.notify(emgr.INFORMATIONAL, io);
                    state = ERROR;
                }
            }
        } catch (SAXException sex) {
            if (!isStopException(sex)) {
                ErrorManager emgr = ErrorManager.getDefault();
                emgr.annotate(sex, ErrorManager.UNKNOWN, "While parsing: " + fo, null, null, null); // NOI18N
                emgr.notify(ErrorManager.INFORMATIONAL, sex);
                state = ERROR;
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    // already closed
                }
            }
        }                        
    }


    protected void customizeInputSource(InputSource in) {
    }
    
    /**
     * Parser default file object
     */
    protected final void parse() {
        if (fo == null) throw new NullPointerException();
        parse(fo);
    }

    /** Report error occured during custom validation. */
    protected void error() throws SAXException {
        String reason = org.openide.util.NbBundle.getMessage(DefaultParser.class, "Invalid_XML_document");
        error(reason);
    }

    /** Report error occured during custom validation. */
    protected void error(String reason) throws SAXException {
        StringBuffer buf = new StringBuffer (reason).append(": ").append(fo.toString());//NOI18N
        if (locator != null) {
            buf.append(" line: ").append(locator.getLineNumber());//NOI18N
            buf.append(" column: ").append(locator.getColumnNumber());//NOI18N
        }
        String msg = buf.toString();  //NOI18N
        SAXException sex = new SAXException(msg);
        throw sex;
    }

    public void error(SAXParseException exception) throws SAXException {
        throw exception;
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }

    public void endDocument() throws SAXException {
        state = PARSED;
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public InputSource resolveEntity (String publicID, String systemID) {
        // Read nothing whatsoever.
        return new InputSource (new ByteArrayInputStream (new byte[] { }));
    }
    
}
