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
 * Supports CheckXMLCookie and ValidateXMLCookie providers. 
 * JAXP created XML parser is used for this purpose. 
 *
 * @author Petr Kuzel
 * @deprecated XML tools SPI candidate
 */
public final class CheckXMLSupport implements CheckXMLCookie, ValidateXMLCookie {

    // it will viasualize our results
    private ProcessorListener console;
    
    // associated dataobject
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

    // used parser instance
    private XMLReader parser = null;
    
    // fatal error counter
    private int fatalErrors;
    
    // error counter
    private int errors;
    
    // set it to true if we want test internal parser
    private static final boolean USE_INTERNAL_PARSER = false;

    // it is way how to identify that event occured in wrapper
    private static final String WRAPPER_PUBLIC_ID = "-//PRIVATE//wrapper id//CX";  // NOI18N

    
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
    
    public boolean checkXML(ProcessorListener l) {
        console = l;

        parse(false);
        
        return fatalErrors > 0;
    }
    
    public boolean validateXML(ProcessorListener l) {
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

            EntityResolver res = (EntityResolver) Lookup.getDefault().lookup(SourceResolver.class);
            if (res != null) parser.setEntityResolver(new VerboseEntityResolver(res));

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
     * Create InputSource preferably from open Swing Document.
     */
    private InputSource createInputSource() throws IOException {
        
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
     * Create preconfigured new parser using JAXP factory and attach itself as ErrorHandler.
     * Try to set reasonable locale to prevent pure system messages.
     */
    private XMLReader createParser(boolean validate) {
       
        XMLReader ret = null;
        final String XERCES_FEATURE_PREFIX = "http://apache.org/xml/features/";         // NOI18N
        final String XERCES_PROPERTY_PREFIX = "http://apache.org/xml/properties/";      // NOI18N
        
        if (USE_INTERNAL_PARSER || Boolean.getBoolean("netbeans.debug.xml.parser")) {   // NOI18N
        
            final ClassLoader loader = this.getClass().getClassLoader();
            try {
                Class clazz = loader.loadClass("org.apache.xerces.parsers.SAXParser");  // NOI18N
                Object obj = clazz.newInstance();
                ret = (XMLReader) obj;

                final String SAX_FEATURE = "http://xml.org/sax/features/";  // NOI18N
                ret.setFeature(SAX_FEATURE + "validation", validate);       // NOI18N
                ret.setFeature(SAX_FEATURE + "external-general-entities", validate);   // NOI18N
                ret.setFeature(SAX_FEATURE + "external-parameter-entities", validate); // NOI18N

                ret.setFeature(XERCES_FEATURE_PREFIX + "validation/schema", validate); // NOI18N

                if (console != null) console.message(Util.THIS.getString("MSG_parser_internal"));
                
            } catch (Exception ex) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLCompiler", ex);  // NOI18N
            }
        
        } else {  // JAXP plugin parser (bastarded by core factories!)
        
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
                
                try {
                    String schemaURL = "nbres:/org/netbeans/modules/xml/tools/resources/XMLSchema2001.xml";
                    String val = "http://www.w3.org/2001/XMLSchema " + schemaURL;
                    ret.setProperty(XERCES_PROPERTY_PREFIX + "schema/external-schemaLocation", val);
                } catch (SAXException ex) {
                    if (console != null) console.message(Util.THIS.getString("MSG_parser_no_schema_loc"));
                }
            }

        }
                                
        return ret;
        
    }

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
    
    // ErrorHandler implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    private class Handler extends DefaultHandler {
    
        public void warning (SAXParseException ex) {
            try {
                if (console != null) console.warning(ex);
            } catch (SAXException sex) {
                // it is not allowed to throw it
            }
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
                try {
                    if (console != null) console.error(ex);
                } catch (SAXException sex) {
                    // it is not allowed to throw it
                }                
            }
        }

        public void fatalError (SAXParseException ex) {        
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Just diagnostic exception", ex); // NOI18N
            try {
                fatalErrors++;
                if (console != null) console.fatalError(ex);
            } catch (SAXException sex) {
                // it is not allowed to throw it
            }
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
                try {
                    String file = new URL(sid).getFile();
                    if (file != null) {
                        warning = Util.THIS.getString("MSG_resolver_1", pid, sid);
                    } else {  // probably NS id
                        warning = Util.THIS.getString("MSG_resolver_2", pid, sid);
                    }
                } catch (MalformedURLException ex) {
                    warning = Util.THIS.getString("MSG_resolver_2", pid, sid);
                }
                if (console != null) console.message(warning);
            }
            return result;
        }
        
    }
}
