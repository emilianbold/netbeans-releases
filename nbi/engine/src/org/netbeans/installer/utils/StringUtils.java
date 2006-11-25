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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.UnrecognizedObjectException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExtendedURI;
import org.netbeans.installer.utils.helper.Platform;
import org.w3c.dom.Element;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class StringUtils {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String BACK_SLASH = "\\"; //NOI18N
    public static final String FORWARD_SLASH = "/"; //NOI18N
    public static final String DOUBLE_BACK_SLASH = "\\\\"; //NOI18N
    
    public static final String CR = "\r";
    public static final String LF = "\n";
    public static final String DOT = ".";
    public static final String EMPTY_STRING = "";
    public static final String CRLF = CR + LF;
    public static final String CRLFCRLF = CRLF + CRLF;
    public static final String SPACE = " ";
    
    private static final String LEFT_WHITESPACE  = "^\\s+";
    private static final String RIGHT_WHITESPACE = "\\s+$";
    
    private static final char   MNEMONIC_CHAR    = '&';
    private static final String MNEMONIC         = "&";
    private static final char   NO_MNEMONIC      = '\u0000';
    
    private static final char[] BASE64_TABLE = new char[] {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
        'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', '+', '/'
    };
    
    private static final byte[] BASE64_REVERSE_TABLE = new byte[] {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, 62, -1, -1, -1, 63, 52, 53,
        54, 55, 56, 57, 58, 59, 60, 61, -1, -1,
        -1, -1, -1, -1, -1,  0,  1,  2,  3,  4,
        5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, -1, -1, -1, -1, -1, -1, 26, 27, 28,
        29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
        49, 50, 51
    };
    
    private static final char BASE64_PAD = '=';
    
    private static final int BIN_11111111 = 0xff;
    private static final int BIN_00110000 = 0x30;
    private static final int BIN_00111100 = 0x3c;
    private static final int BIN_00111111 = 0x3f;
    
    ////////////////////////////////////////////////////////////////////////////
    // Static
    public static String format(String message, Object... arguments) {
        return MessageFormat.format(message, arguments);
    }
    
    public static String leftTrim(String string) {
        return string.replaceFirst(LEFT_WHITESPACE, EMPTY_STRING);
    }
    
    public static String rightTrim(String string) {
        return string.replaceFirst(RIGHT_WHITESPACE, EMPTY_STRING);
    }
    
    public static char fetchMnemonic(String string) {
        int index = string.indexOf(MNEMONIC_CHAR);
        if ((index != -1) && (index < string.length() - 1)) {
            return string.charAt(index + 1);
        }
        
        return NO_MNEMONIC;
    }
    
    public static String stripMnemonic(String string) {
        return string.replaceFirst(MNEMONIC, EMPTY_STRING);
    }
    
    public static String capitalizeFirst(String string) {
        return EMPTY_STRING + Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
    
    public static String getGetterName(String propertyName) {
        return "get" + capitalizeFirst(propertyName);
    }
    
    public static String getBooleanGetterName(String propertyName) {
        return "is" + capitalizeFirst(propertyName);
    }
    
    public static String getSetterName(String propertyName) {
        return "set" + capitalizeFirst(propertyName);
    }
    
    public static String asString(Throwable throwable) {
        StringWriter writer = new StringWriter();
        
        throwable.printStackTrace(new PrintWriter(writer));
        
        return writer.toString();
    }
    
    public static String getFilenameFromUrl(String urlString) {
        String url = urlString.trim();
        int index = Math.max(
                url.lastIndexOf(FORWARD_SLASH),
                url.lastIndexOf(BACK_SLASH));
        int length = url.length();
        return (index > 0 && (index < length - 1)) ?
            url.substring(index + 1,  length) : null;
    }
    
    public static String asString(List<? extends Object> objects) {
        return asString(objects, ", ");
    }
    
    public static String asString(List<? extends Object> objects, String separator) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < objects.size(); i++) {
            result.append(objects.get(i).toString());
            
            if (i != objects.size() - 1) {
                result.append(separator);
            }
        }
        
        return result.toString();
    }
    
    public static String asString(Object [] strings) {
        return asString(strings, ", ");
    }
    
    public static String asString(Object [] strings, String separator) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < strings.length; i++) {
            result.append((strings[i]==null) ? EMPTY_STRING+null :
                strings[i].toString());
            
            if (i != strings.length - 1) {
                result.append(separator);
            }
        }
        
        return result.toString();
    }
    
    public static String formatSize(long longBytes) {
        StringBuffer result = new StringBuffer();
        
        double bytes = (double) longBytes;
        
        // try as GB
        double gigabytes = bytes / 1024. / 1024. / 1024.;
        if (gigabytes > 1.) {
            return String.format("%.1f GB", gigabytes);
        }
        
        // try as MB
        double megabytes = bytes / 1024. / 1024.;
        if (megabytes > 1.) {
            return String.format("%.1f MB", megabytes);
        }
        
        // try as KB
        double kilobytes = bytes / 1024.;
        if (kilobytes > .5) {
            return String.format("%.1f KB", kilobytes);
        }
        
        // return as bytes
        return EMPTY_STRING + longBytes + " B";
    }
    
    public static String asHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            
            String byteHex = Integer.toHexString(b);
            if (byteHex.length() == 1) {
                byteHex = "0" + byteHex;
            }
            if (byteHex.length() > 2) {
                byteHex = byteHex.substring(byteHex.length() - 2);
            }
            
            builder.append(byteHex);
        }
        
        return builder.toString();
    }
    
    public static String base64Encode(String string) throws UnsupportedEncodingException {
        return base64Encode(string, "UTF-8");
    }
    
    public static String base64Encode(String string, String charset) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        
        byte[] bytes = string.getBytes(charset);
        
        int i;
        for (i = 0; i < bytes.length - 2; i += 3) {
            int byte1 = bytes[i] & BIN_11111111;
            int byte2 = bytes[i + 1] & BIN_11111111;
            int byte3 = bytes[i + 2] & BIN_11111111;
            
            builder.append(BASE64_TABLE[byte1 >> 2]);
            builder.append(BASE64_TABLE[((byte1 << 4) & BIN_00110000) | (byte2 >> 4)]);
            builder.append(BASE64_TABLE[((byte2 << 2) & BIN_00111100) | (byte3 >> 6)]);
            builder.append(BASE64_TABLE[byte3 & BIN_00111111]);
        }
        
        if (i == bytes.length - 2) {
            int byte1 = bytes[i] & BIN_11111111;
            int byte2 = bytes[i + 1] & BIN_11111111;
            
            builder.append(BASE64_TABLE[byte1 >> 2]);
            builder.append(BASE64_TABLE[((byte1 << 4) & BIN_00110000) | (byte2 >> 4)]);
            builder.append(BASE64_TABLE[(byte2 << 2) & BIN_00111100]);
            builder.append(BASE64_PAD);
        }
        
        if (i == bytes.length - 1) {
            int byte1 = bytes[i] & BIN_11111111;
            
            builder.append(BASE64_TABLE[byte1 >> 2]);
            builder.append(BASE64_TABLE[(byte1 << 4) & BIN_00110000]);
            builder.append(BASE64_PAD);
            builder.append(BASE64_PAD);
        }
        
        return builder.toString();
    }
    
    public static String base64Decode(String string) throws UnsupportedEncodingException {
        return base64Decode(string, "UTF-8");
    }
    
    public static String base64Decode(String string, String charset) throws UnsupportedEncodingException {
        int completeBlocksNumber = string.length() / 4;
        int missingBytesNumber = 0;
        
        if (string.endsWith("=")) {
            completeBlocksNumber--;
            missingBytesNumber++;
        }
        if (string.endsWith("==")) {
            missingBytesNumber++;
        }
        
        int decodedLength = (completeBlocksNumber * 3) + (3 - missingBytesNumber);
        byte[] decodedBytes = new byte[decodedLength];
        
        int encodedCounter = 0;
        int decodedCounter = 0;
        for (int i = 0; i < completeBlocksNumber; i++) {
            int byte1 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte2 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte3 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte4 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            
            decodedBytes[decodedCounter++] = (byte) ((byte1 << 2) | (byte2 >> 4));
            decodedBytes[decodedCounter++] = (byte) ((byte2 << 4) | (byte3 >> 2));
            decodedBytes[decodedCounter++] = (byte) ((byte3 << 6) | byte4);
        }
        
        if (missingBytesNumber == 1) {
            int byte1 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte2 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte3 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            
            decodedBytes[decodedCounter++] = (byte) ((byte1 << 2) | (byte2 >> 4));
            decodedBytes[decodedCounter++] = (byte) ((byte2 << 4) | (byte3 >> 2));
        }
        
        if (missingBytesNumber == 2) {
            int byte1 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte2 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            
            decodedBytes[decodedCounter++] = (byte) ((byte1 << 2) | (byte2 >> 4));
        }
        
        return new String(decodedBytes, charset);
    }
    
    public static String pad(String string, int number) {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < number; i++) {
            builder.append(string);
        }
        
        return builder.toString();
    }
    
    public static String escapeForRE(String string) {
        return string.replace(BACK_SLASH, BACK_SLASH + BACK_SLASH);
    }
    
    public static String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        byte[] buffer = new byte[1024];
        while (stream.available() > 0) {
            int read = stream.read(buffer);
            
            String readString = new String(buffer, 0, read);
            for(String string : readString.split("(?:\n\r|\r\n|\n|\r)")) {
                builder.append(string).append("\n");
            }
        }
        
        return builder.toString();
    }
    
    public static String parseAscii(String string) {
        Properties properties = new Properties();
        
        // we don't really care about enconding here, as the input string is
        // expected to be ASCII-only, which means it's the same for any encoding
        try {
            properties.load(new ByteArrayInputStream(("key=" + string).getBytes()));
        } catch (IOException e) {
            ErrorManager.notify(ErrorLevel.WARNING, "Cannot parse string", e);
            return string;
        }
        
        return (String) properties.get("key");
    }
    
    public static String convertToAscii(String string) {
        Properties properties = new Properties();
        
        properties.put("uberkey", string);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            properties.store(outputStream, EMPTY_STRING);
        } catch (IOException e) {
            ErrorManager.notify(ErrorLevel.WARNING, "Cannot convert string", e);
            return string;
        }
        
        Matcher matcher = Pattern.compile("uberkey=(.*)$", Pattern.MULTILINE).matcher(outputStream.toString());
        
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return string;
        }
    }
    
    // parsing //////////////////////////////////////////////////////////////////////
    public static Locale parseLocale(String string) {
        String[] parts = string.split("_");
        switch (parts.length) {
            case 1:
                return new Locale(parts[0]);
            case 2:
                return new Locale(parts[0], parts[1]);
            default:
                return new Locale(parts[0], parts[1], parts[2]);
        }
    }
    
    public static URL parseUrl(String string) throws ParseException {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            throw new ParseException("Cannot parse URL", e);
        }
    }
    
    public static Platform parsePlatform(String string) throws ParseException {
        for (Platform platform: Platform.values()) {
            if (platform.getName().equals(string)) {
                return platform;
            }
        }
        
        throw new ParseException("Platform \"" + string + "\" is not recognized.");
    }
    
    public static List<Platform> parsePlatforms(String string) throws ParseException {
        if (string.equals("all")) {
            return Arrays.asList(Platform.values());
        } else {
            List<Platform> platforms = new ArrayList<Platform>();
            
            for (String name: string.split(SPACE)) {
                Platform platform = parsePlatform(name);
                if (!platforms.contains(platform)) {
                    platforms.add(platform);
                }
            }
            return platforms;
        }
    }
    
    public static ExtendedURI parseExtendedUri(Element element) throws ParseException {
        try {
            URI    uri           = new URI(XMLUtils.getChildNodeTextContent(element, "default-uri"));
            long   estimatedSize = Long.parseLong(XMLUtils.getAttribute(element, "estimated-size"));
            String md5           = XMLUtils.getAttribute(element, "md5");
            String crc32         = XMLUtils.getAttribute(element, "crc32");
            
            if (uri.getScheme().equals("file")) {
                return new ExtendedURI(uri, uri, estimatedSize, md5, crc32);
            } else {
                return new ExtendedURI(uri, estimatedSize, md5, crc32);
            }
        } catch (URISyntaxException e) {
            throw new ParseException("Cannot parse extended URI", e);
        }
    }
    
    public static Status parseStatus(final String string) throws ParseException {
        for (Status status: Status.values()) {
            if (status.getName().equals(string)) {
                return status;
            }
        }
        
        throw new ParseException("Cannot parse status: " + string);
    }
}
