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

package org.netbeans.api.xml.parsers;

import java.io.*;

import org.xml.sax.*;

/**
 * SAX parser wrapper allowing to parse XML entities (including DTDs) for 
 * wellformedness. It cannot be used for parsing of XML documents!
 * <p>
 * It wraps client's parser that it actually used for performing the parsing task.
 *
 * @author  Petr Kuzel
 * @deprecated XML tools API candidate
 */
public final class SAXEntityParser {
    
    //??? we are not fully bullet proof
    private static final long RANDOM = System.currentTimeMillis();
    
    private static final String FAKE_SID = "NetBeans:fake-entity:SID" + RANDOM; // NOI18N
    
    private static final String FAKE_PARAMETER_ENTITY_DOCUMENT = 
        "<!DOCTYPE fakeDocument" + RANDOM + " SYSTEM '" + FAKE_SID + "'>\n" +   // NOI18N
        "<fakeDocument" + RANDOM + "/>\n";                                      // NOI18N

    private static final String FAKE_ENTITY_DOCUMENT =
        "<!DOCTYPE fakeDocument" + RANDOM + " [\n" +                            // NOI18N
        "  <!ENTITY   fakeEntity" + RANDOM + " SYSTEM '" + FAKE_SID + "'>\n" +  // NOI18N
        "]>\n" +                                                                // NOI18N
        "<fakeDocument" + RANDOM + ">\n" +                                      // NOI18N
        "&fakeEntity" + RANDOM + ";\n" +                                        // NOI18N
        "</fakeDocument" + RANDOM + ">\n";                                      // NOI18N
    
    private final XMLReader peer;
    
    private final boolean generalEntity;

    /** 
     * Creates a new instance of general entity parser.
     * @param peer parser that will be used for parsing
     */
    public SAXEntityParser(XMLReader peer) {
        this( peer, true);
    }
    
    /** 
     * Creates a new instance of EntityParser.
     * @param peer parser that will be used for parsing
     * @param generalEntity it true treat entity as general entity otherwise as parameter one
     */
    public SAXEntityParser(XMLReader peer, boolean generalEntity) {
        if (peer == null) throw new NullPointerException();
        this.peer = peer;
        this.generalEntity = generalEntity;
    }

    /**
     * Start parsing
     * @param entity entity input source
     */
    public void parse( InputSource entity) throws IOException, SAXException {     
        
        if (entity == null) throw new NullPointerException();
        
        // provide fake entity resolver and input source

        EntityResolver resolver = peer.getEntityResolver();
        peer.setEntityResolver(new ER(resolver, entity));
                
        ErrorHandler errorHandler = peer.getErrorHandler();
        if (errorHandler != null) {
            peer.setErrorHandler( new EH( errorHandler));
        }
        
        InputSource fakeInput = new InputSource(FAKE_SID);
        String fakeDocument = generalEntity ? FAKE_ENTITY_DOCUMENT : FAKE_PARAMETER_ENTITY_DOCUMENT;
        fakeInput.setCharacterStream(new StringReader(fakeDocument));
        peer.parse(fakeInput);
        
    }
    
    /**
     * Redirest to entity input source, it is always the first request.
     * Pure FAKE_SID approach have problems with some parser implementations.
     */
    private class ER implements EntityResolver {

        private boolean entityResolved;
        private final EntityResolver peer;
        private final InputSource entity;
        
        public ER(EntityResolver peer, InputSource entity) {
            this.peer = peer;
            this.entity = entity;
        }
        
        public InputSource resolveEntity(String pid, String sid) throws SAXException, IOException {
            
            Util.THIS.debug("SAXEntityParser:resolving PID: " + pid + " SID: " + sid);
                
            if (isFirstRequest()) {

                // normalize passed entity InputSource using parent entity resolver
                Util.THIS.debug("SAXEntityParser:redirecting to " + entity + " SID: " + entity.getSystemId());
                
                if (peer != null && entity.getByteStream() == null && entity.getCharacterStream() == null) {                    
                    return peer.resolveEntity(entity.getPublicId(), entity.getSystemId());
                } else {
                    return entity;
                }
            } else {
                if (peer == null) {
                    return null;
                } else {
                    return peer.resolveEntity(pid, sid);
                }
            }
        }
        
        private synchronized boolean isFirstRequest() {
            if (entityResolved == false) {
                entityResolved = true;
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Filter out errors in our fake document
     */
    private class EH implements ErrorHandler {
        
        private final ErrorHandler peer;
        
        public EH( ErrorHandler peer) {
            this.peer = peer;
        }
        
        public void error(SAXParseException ex) throws SAXException {
            if (FAKE_SID != ex.getSystemId()) {
                peer.error(ex);
            } else {
                Util.THIS.debug("SAXEntityParser: filtering out:", ex);
            }
        }
        
        public void fatalError(SAXParseException ex) throws SAXException {
            if (FAKE_SID != ex.getSystemId()) {
                peer.fatalError(ex);
            } else {
                Util.THIS.debug("SAXEntityParser: filtering out:", ex);
            }
        }
        
        public void warning(SAXParseException ex) throws SAXException {
            if (FAKE_SID != ex.getSystemId()) {
                peer.warning(ex);
            } else {
                Util.THIS.debug("SAXEntityParser: filtering out:", ex);
            }
        }        
    }
    
}
