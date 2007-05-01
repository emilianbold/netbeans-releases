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
package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TemplateParser extends DefaultHandler {
    static private Writer out;
    private TemplateFactory factory;

    public TemplateParser() {
    }

    public TemplateParser(InputStream stream, TemplateFactory fac) {
        this.factory = fac;
        init(stream);
    }

    public void characters(char buf[], int offset, int len) throws SAXException {
        String s = new String(buf, offset, len);
        showData(s);
    }

    // ===========================================================
    // Methods in SAX DocumentHandler
    // ===========================================================

    public void endDocument() throws SAXException {
        try {
            newLine();
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    // SAX 2.0
    public void endElement(String uri, String localName, String qName) throws SAXException {
        showData("</" + qName + ">");
        if (factory != null) {
            factory.endElement(uri, localName, qName);
        }
    }

    public void startDocument() throws SAXException {
        showData("<?xml version='1.0' encoding='UTF-8'?>");
        newLine();
    }

    // SAX 2.0
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        showData("<" + qName);
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                showData(" ");
                showData(attrs.getQName(i) + "=\"" + attrs.getValue(i) + "\"");
            }
        }
        showData(">");
        if (factory != null) {
            factory.startElement(uri, localName, qName, attrs);
        }
    }

    private void init(InputStream stream) {
        // Use the default (non-validating) parser
        SAXParserFactory saxpFactory = SAXParserFactory.newInstance();
        try {
            // Set up output stream
            if (Boolean.getBoolean("org.netbeans.modules.sql.framework.ui.editor.property.impl.TemplateParser.showOutput")) {
                out = new OutputStreamWriter(System.out, "UTF8");
            } else {
                out = new StringWriter(512);
            }

            // Parse the input
            SAXParser saxParser = saxpFactory.newSAXParser();
            saxParser.parse(stream, this);
        } catch (Exception t) {
            t.printStackTrace();
        }
    }

    // ===========================================================
    // Helpers Methods
    // ===========================================================

    // Start a new line
    private void newLine() throws SAXException {
        String lineEnd = System.getProperty("line.separator");
        try {
            out.write(lineEnd);
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    // Wrap I/O exceptions in SAX exceptions, to
    // suit handler signature requirements
    private void showData(String s) throws SAXException {
        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }
}

