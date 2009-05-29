/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author ondra.vrabec
 */
public final class TestUtilities {
    private TestUtilities () {}

    /**
     * Formats file's location into SVNUrl format
     * @param file
     * @return file's location in a SVNUrl format
     */
    public static String formatFileURL (File file) {
        String url;
        url = ("file:///" + file.getAbsolutePath().replaceAll("\\\\", "/")).replace("file:////", "file:///");
        return url;
    }

    private static boolean isEncodedByte(char c, String s, int i) {
        return c == '%' && i + 2 < s.length() && isHexDigit(s.charAt(i + 1)) && isHexDigit(s.charAt(i + 2));
    }

    private static boolean isHexDigit(char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f';
    }

    /**
     * Decodes svn URI by decoding %XX escape sequences.
     *
     * @param url url to decode
     * @return decoded url
     */
    public static SVNUrl decode(SVNUrl url) {
        if (url == null) return null;
        String s = url.toString();
        StringBuffer sb = new StringBuffer(s.length());

        boolean inQuery = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '?') {
                inQuery = true;
            } else if (c == '+' && inQuery) {
                sb.append(' ');
            } else if (isEncodedByte(c, s, i)) {
                List<Byte> byteList = new ArrayList<Byte>();
                do  {
                    byteList.add((byte) Integer.parseInt(s.substring(i + 1, i + 3), 16));
                    i += 3;
                    if (i >= s.length()) break;
                    c = s.charAt(i);
                } while(isEncodedByte(c, s, i));

                if(byteList.size() > 0) {
                    byte[] bytes = new byte[byteList.size()];
                    for(int ib = 0; ib < byteList.size(); ib++) {
                        bytes[ib] = byteList.get(ib);
                    }
                    try {
                        sb.append(new String(bytes, "UTF8"));
                    } catch (Exception e) {
                        
                    }
                    i--;
                }
            } else {
                sb.append(c);
            }
        }
        try {
            return new SVNUrl(sb.toString());
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
