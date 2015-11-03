/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Pair;

/**
 *
 * @author Petr Hejl
 */
public final class HttpUtils {

    private static final Pattern HTTP_RESPONSE_PATTERN = Pattern.compile("^HTTP/1\\.1 (\\d\\d\\d) (.*)$");

    private HttpUtils() {
        super();
    }

    public static String readResponseLine(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            if (b == '\r') {
                int next = is.read();
                if (next == '\n') {
                    return bos.toString("ISO-8859-1"); // NOI18N
                } else if (next == -1) {
                    return null;
                } else {
                    bos.write(b);
                    bos.write(next);
                }
            } else {
                bos.write(b);
            }
        }
        return null;
    }

    public static Pair<Integer, String> readResponse(InputStream is) throws IOException {
        String response = HttpUtils.readResponseLine(is);
        if (response == null) {
            throw new IOException("No response from server");
        }
        Matcher m = HTTP_RESPONSE_PATTERN.matcher(response);
        if (!m.matches()) {
            throw new IOException("Wrong response from server");
        }

        int responseCode = Integer.parseInt(m.group(1));
        return Pair.of(responseCode, m.group(2));
    }

    public static Map<String, String> parseHeaders(InputStream is) throws IOException {
        Map<String, String> result = new HashMap<>();
        String line;
        for (;;) {
            line = HttpUtils.readResponseLine(is).trim();
            if (line != null && !"".equals(line)) {
                int index = line.indexOf(':'); // NOI18N
                if (index <= 0) {
                    throw new IOException("Invalid header: " + line);
                }
                if (index == line.length() - 1) {
                    // XXX empty header ?
                    continue;
                }
                result.put(line.substring(0, index).toUpperCase(Locale.ENGLISH).trim(), line.substring(index + 1).trim());
            } else {
                break;
            }
        }
        return result;
    }

    public static String readContent(InputStream is, int length, String encoding) throws IOException {
        byte[] content = new byte[length];
        int count = 0;
        do {
             int current = is.read(content, count, length - count);
             if (current < 0 && count < length) {
                 throw new IOException("Stream closed before reading content");
             }
             count += current;
        } while (count < length);
        return new String(content, encoding);
    }

    public static boolean isChunked(Map<String, String> headers) {
        String value = headers.get("TRANSFER-ENCODING"); // NOI18N
        return value != null && value.contains("chunked"); // NOI18N
    }

    public static Integer getLength(Map<String, String> headers)  throws IOException {
        String value = headers.get("CONTENT-LENGTH"); // NOI18N
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IOException("Wrong content length: " + value);
        }
    }
}
