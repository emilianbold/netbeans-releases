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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Can persist and read log records from streams.
 *
 * @author Jaroslav Tulach
 */
public final class LogRecords {
    private LogRecords() {
    }

    private static final Formatter FORMATTER = new XMLFormatter();
    
    public static void write(OutputStream os, LogRecord rec) throws IOException {
        String formated = FORMATTER.format(rec);
        byte[] arr = formated.getBytes("utf-8");
        os.write(arr);
    }

    
    
    public static LogRecord read(InputStream is) throws IOException {
        String s = readXMLBlock(is);
        if (s == null) {
            return null;
        }
        
        // in case the block is not ours
        if (s.indexOf("record>") == -1) { // NOI18N
            Logger.getLogger(LogRecords.class.getName()).info("Skipping: " + s); // NOI18N
            s = readXMLBlock(is);
        }
        
        s = s.replaceAll("&amp;", "&").replaceAll("&gt;", ">")
            .replaceAll("&lt;", "<");

        String millis = content(s, "millis");
        String seq = content(s, "sequence");
        String lev = content(s, "level");
        String thread = content(s, "thread");
        String msg = content(s, "message");
        
        LogRecord r = new LogRecord(Level.parse(lev), msg);
        r.setThreadID(Integer.parseInt(thread));
        r.setSequenceNumber(Long.parseLong(seq));
        r.setMillis(Long.parseLong(millis));
        
        return r;
    }
    
    private static String content(String where, String what) throws IOException {
        int indx = where.indexOf("<" + what + ">");
        if (indx == -1) {
            throw new IOException("Not found: <" + what + "> inside of:\n"+ where); // NOI18N
        }
        int begin = indx + what.length() + 2;
        
        int end = where.indexOf("</" + what + ">", indx);
        if (indx == -1) {
            throw new IOException("Not found: </" + what + "> inside of:\n"+ where); // NOI18N
        }
        
        return where.substring(begin, end);
    }
    
    private static String readXMLBlock(InputStream is) throws IOException {
        byte[] arr = new byte[4096 * 4];
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
                if (ch == '?') {
                    seenQuest = true;
                } else if (ch == '/') {
                    seenSlash = true;
                } else if (ch == '>') {
                    inTag = false;
                    if (new String(arr, 0, index).indexOf("uigestures") >= 0) {
                        // header found, restart
                        return readXMLBlock(is);
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
                }
            }
        }
        return new String(arr, 0, index, "utf-8");
    }
}
