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
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.Charset;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 * @author Danila Dugurov
 * @author Kirill Sorokin
 */
public class StreamUtils {
    private static final int BUFFER_SIZE = 4096;
    
    public static void transferData(InputStream in, OutputStream out) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        int length = 0;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }
    
    public static void transferData(RandomAccessFile in, OutputStream out) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        int length = 0;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }
    
    public static void transferData(RandomAccessFile in, OutputStream out, long maxLength) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        
        long totalLength = 0;
        int  length = 0;
        while (((length = in.read(buffer)) != -1) && (totalLength < maxLength)) {
            totalLength += length;
            out.write(buffer, 0, (int) (totalLength > maxLength ? length : totalLength - maxLength));
        }
        out.flush();
    }
    
    public static void transferFile(File file, OutputStream out) throws IOException {
        FileInputStream in = null;
        
        try {
            transferData(in = new FileInputStream(file), out);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    ErrorManager.notify(ErrorLevel.DEBUG, e);
                }
            }
        }
    }
    
    public static CharSequence readStream(InputStream input) throws IOException {
        return readStream(input, Charset.forName("utf-8"));
    }
    
    public static CharSequence readStream(InputStream input, Charset charset) throws IOException {
        final Reader reader = new BufferedReader(new InputStreamReader(input, charset));
        return readReader(reader);
    }
    
    public static CharSequence readReader(Reader reader) throws IOException {
        final char[] buffer = new char[BUFFER_SIZE];
        final StringBuilder stringBuilder = new StringBuilder();
        int readLength;
        while ((readLength = reader.read(buffer)) != -1) {
            stringBuilder.append(buffer, 0, readLength);
        }
        return stringBuilder;
    }
    
    public static CharSequence readFile(File file, Charset charset) throws IOException {
        final InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            return readReader(new InputStreamReader(in, charset));
        } finally {
            try {
                in.close();
            } catch(IOException ignord) {}
        }
    }
    
    public static CharSequence readFile(File file) throws IOException {
        return readFile(file, Charset.forName("UTF-8"));
    }
    
    public static void writeChars(OutputStream out, CharSequence chars, Charset charset) throws IOException {
        out.write(chars.toString().getBytes(charset.name()));
    }
    
    public static void writeChars(OutputStream out, CharSequence chars) throws IOException {
        writeChars(out, chars, Charset.forName("utf-8"));
    }
    
    public static void writeChars(File file, CharSequence chars, Charset charset) throws IOException {
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            writeChars(out, chars, charset);
        } finally {
            try {
                out.close();
            } catch(IOException ignord) {}
        }
    }
    
    public static void writeChars(File file, CharSequence chars) throws IOException {
        writeChars(file, chars, Charset.forName("UTF-8"));
    }
}
