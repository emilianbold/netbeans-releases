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

import java.io.File;
import java.util.Arrays;
import java.util.StringTokenizer;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tries to validate all the example XML metadata files
 * according to available XML schemas.
 * @author Jesse Glick
 */
public class ValidateAllBySchema {

    /**
     * args[0] = comma-separated list of files to validate
     * args[1] = comma-separated list of available XML Schema files
     */
    public static void main(String[] args) throws Exception {
        File[] xmls = split(args[0]);
        File[] schemas = split(args[1]);
        String[] schemaUris = new String[schemas.length];
        for (int i = 0; i < schemas.length; i++) {
            schemaUris[i] = schemas[i].toURI().toString();
        }
        System.err.println("Validating against " + Arrays.asList(schemas));
        SAXParserFactory f;
        // #46847: needs to work on both JDK 1.4 and 1.5.
        try {
            f = (SAXParserFactory)Class.forName("com.sun.org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance();
        } catch (ClassNotFoundException e) {
            f = (SAXParserFactory)Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance();
        }
        f.setNamespaceAware(true);
        f.setValidating(true);
        SAXParser p = f.newSAXParser();
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                      "http://www.w3.org/2001/XMLSchema");
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",
                      schemaUris);
        int exit = 0;
        for (int i = 0; i < xmls.length; i++) {
            System.err.println("Parsing " + xmls[i] + "...");
            try {
                p.parse(xmls[i].toURI().toString(), new Handler());
            } catch (SAXParseException e) {
                System.err.println(e.getSystemId() + ":" + e.getLineNumber() + ": " + e.getLocalizedMessage());
                exit = 1;
            }
        }
        System.err.println("All files validated.");
        System.exit(exit);
    }
    private static final class Handler extends DefaultHandler {
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
    private static File[] split(String s) {
        StringTokenizer tok = new StringTokenizer(s, ",");
        File[] files = new File[tok.countTokens()];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(tok.nextToken());
        }
        return files;
    }
}
