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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandler;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.SequenceInputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** Can persist and read log records from streams.
 *
 * @author Jaroslav Tulach
 */
public final class LogRecords {
    private LogRecords() {
    }

    private static final Logger LOG = Logger.getLogger(LogRecords.class.getName());
    
    private static final Formatter FORMATTER = new XMLFormatter();
    
    public static void write(OutputStream os, LogRecord rec) throws IOException {
        String formated = FORMATTER.format(rec);
        byte[] arr = formated.getBytes("utf-8");
        os.write(arr);
    }

    public static void scan(InputStream is, Handler h) throws IOException {
        PushbackInputStream wrap = new PushbackInputStream(is, 32);
        byte[] arr = new byte[5];
        int len = wrap.read(arr);
        if (len == -1) {
            return;
        }
        wrap.unread(arr, 0, len);
        
        if (arr[0] == '<' &&
            arr[1] == '?' &&
            arr[2] == 'x' &&
            arr[3] == 'm' &&
            arr[4] == 'l'
        ) {
            is = wrap;
        } else {
            ByteArrayInputStream header = new ByteArrayInputStream(
    "<?xml version='1.0' encoding='UTF-8'?><uigestures version='1.0'>".getBytes()
            );
            ByteArrayInputStream footer = new ByteArrayInputStream(
                "</uigestures>".getBytes()
            );
            is = new SequenceInputStream(
                new SequenceInputStream(header, wrap),
                footer
            );
        }
        
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setValidating(false);
        SAXParser p;
        try {
            p = f.newSAXParser();
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw (IOException)new IOException(ex.getMessage()).initCause(ex);
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw (IOException)new IOException(ex.getMessage()).initCause(ex);
        }
        
        Parser parser = new Parser(h);
        try {
            p.parse(is, parser);
        } catch (SAXException ex) {
            LOG.log(Level.WARNING, null, ex);
            throw (IOException)new IOException(ex.getMessage()).initCause(ex);
        }
    }
    
    
    public static LogRecord read(InputStream is) throws IOException {
        int[] end = new int[1];
        byte[] data = readXMLBlock(is, end);
        if (data == null) {
            return null;
        }
        
        String s = new String(data, 0, end[0]);
        
        // in case the block is not ours
        if (s.indexOf("record>") == -1) { // NOI18N
            Logger.getLogger(LogRecords.class.getName()).info("Skipping: " + s); // NOI18N
            data = readXMLBlock(is, end);
            if (data == null) {
                return null;
            }
            s = new String(data, 0, end[0]);
        }
        
        s = s.replaceAll("&amp;", "&").replaceAll("&gt;", ">")
            .replaceAll("&lt;", "<");

        String millis = content(s, "millis", true);
        String seq = content(s, "sequence", true);
        String lev = content(s, "level", true);
        String thread = content(s, "thread", true);
        String msg = content(s, "message", true);
        String key = content(s, "key", false);
        
        LogRecord r = new LogRecord(Level.parse(lev), msg);
        r.setThreadID(Integer.parseInt(thread));
        r.setSequenceNumber(Long.parseLong(seq));
        r.setMillis(Long.parseLong(millis));
        r.setResourceBundleName(key);
        
        return r;
    }
    
    private static String content(String where, String what, boolean fail) throws IOException {
        int indx = where.indexOf("<" + what + ">");
        if (indx == -1) {
            if (fail) {
                throw new IOException("Not found: <" + what + "> inside of:\n"+ where); // NOI18N
            } else {
                return null;
            }
        }
        int begin = indx + what.length() + 2;
        
        int end = where.indexOf("</" + what + ">", indx);
        if (indx == -1) {
            throw new IOException("Not found: </" + what + "> inside of:\n"+ where); // NOI18N
        }
        
        return where.substring(begin, end);
    }
    
    private static byte[] readXMLBlock(InputStream is, int[] len) throws IOException {
        byte[] arr = new byte[4096 * 12];
        int index = 0;
        
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                return null;
            }

            if (ch == '<') {
                arr[index++] = '<';
                break;
            }
        }
        
        int depth = 0;
        boolean inTag = true;
        boolean seenSlash = false;
        boolean seenQuest = false;
        int uigestures = 0;
        for (;;) {
            if (!inTag && depth == 0) {
                break;
            }
            
            int ch = is.read();
            if (ch == -1) {
                throw new EOFException();
            }
            if (index == arr.length) {
                throw new EOFException("Buffer size " + arr.length + " exceeded"); // NOI18N
            }
            
            arr[index++] = (byte)ch;
            
            if (inTag) {
                switch (uigestures) {
                    case 0: if (ch == 'u') uigestures = 1; else uigestures = 0; break;
                    case 1: if (ch == 'i') uigestures = 2; else uigestures = 0; break;
                    case 2: if (ch == 'g') uigestures = 3; else uigestures = 0; break;
                    case 3: if (ch == 'e') uigestures = 4; else uigestures = 0; break;
                    case 4: if (ch == 's') uigestures = 5; else uigestures = 0; break;
                    case 5: if (ch == 't') uigestures = 6; else uigestures = 0; break;
                    case 6: if (ch == 'u') uigestures = 7; else uigestures = 0; break;
                    case 7: if (ch == 'r') uigestures = 8; else uigestures = 0; break;
                    case 8: if (ch == 'e') uigestures = 9; else uigestures = 0; break;
                    case 9: if (ch == 's') uigestures = 10; else uigestures = 0; break;
                    case 10: // ok, stay at 10
                }
                
                if (ch == '?') {
                    seenQuest = true;
                } else if (ch == '/') {
                    seenSlash = true;
                } else if (ch == '>') {
                    inTag = false;
                    if (uigestures == 10) {
                        // header found, restart
                        return readXMLBlock(is, len);
                    }
                    if (seenSlash) {
                        depth--;
                    } else if (seenQuest) {
              //          depth--;
                    } else {
                        depth++;
                    }
                }
            } else {
                if (ch == '<') {
                    inTag = true;
                    seenSlash = false;
                    seenQuest = false;
                    uigestures = 0;
                }
            }
        }
        len[0] = index;
        return arr;
    }
    
    private static final class Parser extends DefaultHandler {
        private Handler callback;
        private static enum Elem {
            UIGESTURES, RECORD, DATE, MILLIS, SEQUENCE, LEVEL, THREAD, 
            MESSAGE, KEY;
                
            public String parse(Map<Elem,String> values) {
                String v = values.get(this);
                return v;
            }
        }
        private Map<Elem,String> values = new EnumMap<Elem,String>(Elem.class);
        private Elem current;
        private StringBuilder chars = new StringBuilder();
        
        public Parser(Handler c) {
            this.callback = c;
        }
        
        
        public void setDocumentLocator(Locator locator) {
        }

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
            callback.flush();
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "uri: {0} localName: {1} qName: {2} atts: {3}", new Object[] { uri, localName, qName, atts });
            }

            try {
                current = Elem.valueOf(qName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE, "Uknown tag " + qName, ex);
                current = null;
            }
            chars.setLength(0);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (current != null) {
                values.put(current, chars.toString());
            }
            current = null;
            chars.setLength(0);
            
            if ("record".equals(qName)) { // NOI18N
                String millis = Elem.MILLIS.parse(values);
                String seq = Elem.SEQUENCE.parse(values);
                String lev = Elem.LEVEL.parse(values);
                String thread = Elem.THREAD.parse(values);
                String msg = Elem.MESSAGE.parse(values);
                String key = Elem.KEY.parse(values);
                
                LogRecord r = new LogRecord(Level.parse(lev), msg);
                r.setThreadID(Integer.parseInt(thread));
                r.setSequenceNumber(Long.parseLong(seq));
                r.setMillis(Long.parseLong(millis));
                r.setResourceBundleName(key);
                callback.publish(r);
                
                values.clear();
            }
            
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            chars.append(ch, start, length);
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
        }

        public void skippedEntity(String name) throws SAXException {
        }

        public void fatalError(SAXParseException e) throws SAXException {
            LOG.log(Level.FINE, null, e);
        }
        
    }
}
