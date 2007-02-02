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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.gen;

import java.lang.reflect.Field;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.lexer.gen.util.LexerGenUtilities;

/**
 * Updates the language data by reading and interpreting
 * a given xml file with the language description updates.
 * <BR>The xml file is read in two rounds. In the first
 * round all the hidden token types elements
 * are interpreted. In the second round the rest
 * of the elements are interpreted.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DescriptionReader extends DefaultHandler {
    
    // Elements names
    private static final String LANGUAGE_ELEM = "Language";
    private static final String TOKEN_ID_ELEM = "TokenId";
    private static final String HIDDEN_TOKEN_TYPE_ELEM = "HiddenTokenType";
    private static final String CATEGORY_ELEM = "Category";
    private static final String SAMPLE_TEXT_ELEM = "SampleText";
    private static final String COMMENT_ELEM = "Comment";
    
    private static final String NAME_ATTR = "name";
    private static final String TOKEN_TYPE_ATTR = "tokenType";
    private static final String SAMPLE_TEXT_CHECK_ATTR = "sampleTextCheck";
    private static final String CASE_INSENSITIVE_ATTR = "caseInsensitive";
    private static final String RESET_SAMPLES_ATTR = "resetSamples";

    private String systemId;
    
    /** Active language data into which the input xml is parsed. */
    protected LanguageData languageData;
    
    /**
     * Whether processing HiddenTokenType elements (1st round)
     * or the rest of the elements (2nd round).
     */
    private boolean processingHiddenTokenTypes;
    
    /** Currently processed tokenId. */
    protected MutableTokenId id;
    
    /* Currently in Comment element. */
    private boolean inCommentElement;

    /* Currently in SampleText element. */
    private boolean inSampleTextElement;
    
    /**
     * Create the description reader over the given systemId.
     *  @param systemId identification of the source xml language description file.
     */
    public DescriptionReader(String systemId) {
        this.systemId = systemId;
    }

    /**
     * Parse the xml determined by systemId and update the languageData.
     *  @param languageData update the language data by updating/adding mutable
     *   tokenIds. If there is an TokenId element with a name not yet present
     *   in the languageData it will be added. Otherwise the attributes of existing
     *   mutable tokenId will be updated.
     * @throws javax.xml.parsers.SAXException (also encapsulates ParserConfigurationException)
     *   and IOException
     */
    public synchronized void applyTo(LanguageData languageData) throws SAXException, IOException {
        
        this.languageData = languageData;

        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            
            processingHiddenTokenTypes = true;
            
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(this);
            reader.parse(new InputSource(systemId)); // process hidden token types only
        
            processingHiddenTokenTypes = false;

            reader.parse(new InputSource(systemId)); // process tokenId elements

        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
        
        this.languageData = null;
    }
    
    public void startElement(String uri, String localName, String qname, 
    Attributes attributes) throws SAXException {
        if (LANGUAGE_ELEM.equals(qname)) {
            // no info

        } else if (TOKEN_ID_ELEM.equals(qname)) {
            if (!processingHiddenTokenTypes) {
                // Create a new tokenId or start updating the existing one
                String name = empty2nullFromSource(attributes.getValue(NAME_ATTR));
                id = languageData.findId(name);
                if (id == null) {
                    id = languageData.newId(name);
                }

                // Possibly update the tokenId by the given token type
                String tokenTypeName = empty2nullFromSource(attributes.getValue(TOKEN_TYPE_ATTR));
                if (tokenTypeName != null) {
                    id.updateByTokenType(tokenTypeName);
                }

                // Possibly reset the existing samples (usually got from token type)
                if (toBoolean(attributes.getValue(RESET_SAMPLES_ATTR))) {
                    id.resetSamples();
                }
                
                // Update case insensitivity
                id.setCaseInsensitive(toBoolean(attributes.getValue(CASE_INSENSITIVE_ATTR)));

                // Possibly update type of sample text checking
                String stc = empty2nullFromSource(attributes.getValue(SAMPLE_TEXT_CHECK_ATTR));
                if (stc != null) {
                    id.setSampleTextCheck(stc);
                }
            }
        
        } else if (HIDDEN_TOKEN_TYPE_ELEM.equals(qname)) {
            if (processingHiddenTokenTypes) {
                String tokenTypeName = empty2nullFromSource(attributes.getValue(NAME_ATTR));
                MutableTokenId id = languageData.findIdByTokenTypeName(tokenTypeName);
                if (id != null) {
                    languageData.remove(id);
                }
            }

        } else if (CATEGORY_ELEM.equals(qname)) {
            if (!processingHiddenTokenTypes) {
                id.getCategoryNames().add(attributes.getValue(NAME_ATTR));
            }

        } else if (COMMENT_ELEM.equals(qname)) {
            if (!processingHiddenTokenTypes) {
                inCommentElement = true;
            }
          
        } else if (SAMPLE_TEXT_ELEM.equals(qname)) {
            if (!processingHiddenTokenTypes) {
                inSampleTextElement = true;
            }

        } else {
            throw new IllegalStateException("Unknown element qname=" + qname);
        }
        
    }

    /** End element. */
    public void endElement(String uri, String localName, String qname) {
        if (TOKEN_ID_ELEM.equals(qname)) {
            if (!processingHiddenTokenTypes) {
                id = null;
            }
            
        } else if (COMMENT_ELEM.equals(qname)) {
            if (!processingHiddenTokenTypes) {
                inCommentElement = false;
            }

        } else if (SAMPLE_TEXT_ELEM.equals(qname)) {
            if (!processingHiddenTokenTypes) {
                inSampleTextElement = false;
            }
        }
    }
    
    /** Characters in element */
    public void characters (char ch[], int start, int length) throws SAXException {
        if (id != null) {
            if (inCommentElement) {
                String comment = empty2nullFromSource(new String(ch, start, length));
                if (comment != null) {
                    id.setComment(comment);
                }

            } else if (inSampleTextElement) {
                if (length > 0) { // only non-empty SampleTexts are supported
                    id.addSampleText(empty2nullFromSource(new String(ch, start, length)));
                }
            }
        }
    }

    private static String empty2null(String s) {
        if ("".equals(s)) {
            s = null;
        }

        return s;
    }
    
    private static String empty2nullFromSource(String s) {
        s = empty2null(s);
        if (s != null) {
            s = LexerGenUtilities.fromSource(s);
        }
        return s;
    }

    private static boolean toBoolean(String s) {
        return "true".equals(s);
    }

}

