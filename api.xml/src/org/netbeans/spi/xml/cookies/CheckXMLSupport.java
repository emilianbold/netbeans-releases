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
package org.netbeans.spi.xml.cookies;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.ProtectionDomain;
import java.security.CodeSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.swing.text.Document;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.util.*;
import org.openide.filesystems.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.api.xml.services.*;
import org.netbeans.api.xml.parsers.*;

/**
 * <code>CheckXMLCookie</code> and <code>ValidateXMLCookie</code> cookie 
 * implementation support simplifing cookie providers based on 
 * <code>DataObject</code>s representing XML documents and entities.
 * <p>
 * <b>Primary use case</b> in a DataObject subclass (which primary file is XML):
 * <pre>
 *   CookieSet cookies = getCookieSet();
 *   CheckXMLSupport cookieImpl = new CheckXMLSupport(this);
 *   cookies.add(cookieImpl);
 * </pre>
 * <p>
 * <b>Secondary use case:</b> Subclasses my customize the class by customization
 * protected methods. The customized subclass can be used according to
 * primary use case.
 *
 * @author Petr Kuzel
 * @deprecated XML tools SPI candidate
 */
public class CheckXMLSupport implements CheckXMLCookie, ValidateXMLCookie {

    // it will viasualize our results
    private ProcessorNotifier console;
    
    // associated data object
    private final DataObject dataObject;

    /**
     * General entity mode, only check cookie supported.
     */
    public static final int CHECK_ENTITY_MODE = 1;
    
    /**
     * Parameter entity mode (e.g. DTD), only check cookie supported.
     */
    public static final int CHECK_PARAMETER_ENTITY_MODE = 2;
    
    /**
     * XML document mode. Check/validity depends on called cookie method.
     */
    public static final int DOCUMENT_MODE = 3;
    
    // one of above modes
    private final int mode;
    
    // error locator or null
    private Locator locator;

    // fatal error counter
    private int fatalErrors;
    
    // error counter
    private int errors;
    
    /** 
     * Create new CheckXMLSupport for given data object in DOCUMENT_MODE.
     * @param dataObject supported data object
     */    
    public CheckXMLSupport(DataObject dataObject) {
        this(dataObject, DOCUMENT_MODE);
    }    
    
    /** 
     * Create new CheckXMLSupport for given data object
     * @param dataObject supported data object
     * @param mode one of <code>*_MODE</code> constants
     */
    public CheckXMLSupport(DataObject dataObject, int mode) {

        if (dataObject == null) throw new NullPointerException();
        if (mode < CHECK_ENTITY_MODE || mode > DOCUMENT_MODE) {
            throw new IllegalArgumentException();
        }
        
        this.dataObject = dataObject;
        this.mode = mode;
    }

    // inherit JavaDoc
    public boolean checkXML(ProcessorNotifier l) {
        console = l;

        parse(false);
        
        return fatalErrors > 0;
    }
    
    // inherit JavaDoc
    public boolean validateXML(ProcessorNotifier l) {
        console = l;

        if (mode != DOCUMENT_MODE) {
            if (console != null) console.message(Util.THIS.getString("MSG_not_a_doc"));
            return false;
        } else {        
            parse(true);
            return errors > 0 || fatalErrors > 0;
        }
    }
                

    
    /**
     * Perform parsing in current thread.
     */
    private void parse (boolean validate) {
        
        fatalErrors = 0;
        errors = 0;
                
        String checkedFile = dataObject.getPrimaryFile().getPackageNameExt('/','.');
        if (console != null) console.message(Util.THIS.getString("MSG_checking", checkedFile));

        Handler handler = new Handler();
        
        try {

            // set up parser
            
            XMLReader parser = createParser(validate);

            parser.setErrorHandler(handler);           
            parser.setContentHandler(handler);
            
            if ( Util.THIS.isLoggable()) {
                Util.THIS.debug(checkedFile + ":" + parserDescription(parser));
            }

            // parse
            
            final InputSource input = createInputSource();

            if (mode == CHECK_ENTITY_MODE) {
                new SAXEntityParser(parser, true).parse(input);
            } else if (mode == CHECK_PARAMETER_ENTITY_MODE) {
                new SAXEntityParser(parser, false).parse(input);
            } else {
                parser.parse (input);
            }

        } catch (SAXException ex) {

            // same as one catched by ErrorHandler
            // because we do not have content handler

        } catch (FileStateInvalidException ex) {

            // bad luck report as fatal error
            handler.fatalError(new SAXParseException(ex.getLocalizedMessage(), locator, ex));

        } catch (IOException ex) {

            // bad luck probably because cannot resolve entity
            // report as error at -1,-1 if we do not have Locator
            handler.fatalError(new SAXParseException (ex.getLocalizedMessage(), locator, ex));

        } catch (RuntimeException ex) {

            // probably an internal parser error
            String msg = Util.THIS.getString("EX_parser_ierr", ex.getMessage());
            handler.fatalError(new SAXParseException (msg, locator, ex));

        }
        
    }

    /**
     * Parametrizes default parser creatin process. Default implementation
     * takes user's catalog entity resolver.
     * @return EntityResolver entity resolver or <code>null</code>
     */
    protected EntityResolver createEntityResolver() {
        return (EntityResolver) Lookup.getDefault().lookup(SourceResolver.class);
    }
    
    /**
     * Create InputSource to be checked. Default implementation prefers opened
     * Swing <code>Document</code> over primary file URL.
     * @throws IOException if I/O error occurs.
     * @return InputSource never <code>null</code>
     */
    protected InputSource createInputSource() throws IOException {
        
        InputSource ret = null;
        URL url = dataObject.getPrimaryFile().getURL();

        // test if a document is opened

        EditorCookie editor = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        
        if (editor != null) {
            Document doc = editor.getDocument();
            if (doc != null) {
                ret = new DocumentInputSource(doc);
            }
        } 

        // anyway use system id

        if (ret == null) {
            ret = new InputSource(url.toExternalForm());            
        } else {
            ret.setSystemId(url.toExternalForm());
        }
        
        return ret;
    }

    /** 
     * Create and preconfigure new parser. Default implementation uses JAXP.
     * @param validate true if validation module is required
     * @return SAX reader that is used for command performing or <code>null</code>
     * @see #createEntityResolver
     */
    protected XMLReader createParser(boolean validate) {
       
        XMLReader ret = null;
        final String XERCES_FEATURE_PREFIX = "http://apache.org/xml/features/";         // NOI18N
        final String XERCES_PROPERTY_PREFIX = "http://apache.org/xml/properties/";      // NOI18N
        
       // JAXP plugin parser (bastarded by core factories!)
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(validate);

        try {
            SAXParser parser = factory.newSAXParser();
            ret = parser.getXMLReader();                
        } catch (Exception ex) {
            if (console != null) console.message(Util.THIS.getString("MSG_parser_err_1"));
            return null;
        }

        //??? It is Xerces specifics, but no general API for XML Schema based validation exists
        if (validate) {
            try {
                ret.setFeature(XERCES_FEATURE_PREFIX + "validation/schema", validate); // NOI18N
            } catch (SAXException ex) {
                if (console != null) console.message(Util.THIS.getString("MSG_parser_no_schema"));
            }                
        }

        if (ret != null) {
            EntityResolver res = createEntityResolver();
            if (res != null) ret.setEntityResolver(new VerboseEntityResolver(res));
        }
        
        return ret;
        
    }

    /**
     * It may be helpfull for tracing down some oddities.
     */
    private String parserDescription(XMLReader parser) {

        // report which parser implementation is used
        
        Class klass = parser.getClass();
        try {
            ProtectionDomain domain = klass.getProtectionDomain();
            CodeSource source = domain.getCodeSource();
            
            if (source == null && (klass.getClassLoader() == null || klass.getClassLoader().equals(Object.class.getClassLoader()))) {
                return Util.THIS.getString("MSG_platform_parser");
            } else if (source == null) {
                return Util.THIS.getString("MSG_unknown_parser", klass.getName());
            } else {
                URL location = source.getLocation();
                return Util.THIS.getString("MSG_parser_plug", location.toExternalForm());
            }
            
        } catch (SecurityException ex) {
            return Util.THIS.getString("MSG_unknown_parser", klass.getName());
        }
        
    }
    
    // Content & ErrorHandler implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    private class Handler extends DefaultHandler {
    
        public void warning (SAXParseException ex) {
            if (console != null) console.warning(new ProcessorNotifier.Message(ex));
        }

        /**
         * Report maximally getMaxErrorCount() errors then stop the parser.
         */
        public void error (SAXParseException ex) throws SAXException {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Just diagnostic exception", ex); // NOI18N
            if (errors++ == getMaxErrorCount()) {
                String msg = Util.THIS.getString("MSG_too_many_errs");
                if (console != null) console.message(msg);
                throw ex; // stop the parser                
            } else {
                if (console != null) console.error(new ProcessorNotifier.Message(ex));
            }
        }

        public void fatalError (SAXParseException ex) {        
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Just diagnostic exception", ex); // NOI18N
            fatalErrors++;
            if (console != null) console.fatalError(new ProcessorNotifier.Message(ex));
        }
        
        public void setDocumentLocator(Locator locator) {
            CheckXMLSupport.this.locator = locator;
        }

        private int getMaxErrorCount() {
            return 20;  //??? load from option
        }    
        
    }


    /**
     * EntityResolver that reports unresolved entities.
     */
    private class VerboseEntityResolver implements EntityResolver {
        
        private final EntityResolver peer;
        
        public VerboseEntityResolver(EntityResolver res) {
            if (res == null) throw new NullPointerException();
            peer = res;
        }
        
        public InputSource resolveEntity(String pid, String sid) throws SAXException, IOException {
            InputSource result = peer.resolveEntity(pid, sid);
            
            // null result may be suspicious, may be no Schema location found etc.
            
            if (result == null) {
                                
                String warning;
                String pidLabel = pid != null ? pid : Util.THIS.getString("MSG_no_pid");
                try {
                    String file = new URL(sid).getFile();
                    if (file != null) {
                        warning = Util.THIS.getString("MSG_resolver_1", pidLabel, sid);
                    } else {  // probably NS id
                        warning = Util.THIS.getString("MSG_resolver_2", pidLabel, sid);
                    }
                } catch (MalformedURLException ex) {
                    warning = Util.THIS.getString("MSG_resolver_2", pidLabel, sid);
                }
                if (console != null) console.message(warning);
            }
            return result;
        }
        
    }
}
