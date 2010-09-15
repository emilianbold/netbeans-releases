/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.StringBuilder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author marekfukala
 */
public class Documentation {

    static final Pattern SECTIONS_PATTERN = Pattern.compile("<h[\\w\\d]*\\s*id=\\\"(.*?)\\\">");
    private static final String DOC_ZIP_FILE_NAME = "docs/html5doc.zip"; //NOI18N
    private static URL DOC_ZIP_URL;

    public static URL getZipURL() {
        if (DOC_ZIP_URL == null) {
            File file = InstalledFileLocator.getDefault().locate(DOC_ZIP_FILE_NAME, null, false);
            if (file != null) {
                try {
                    URL url = file.toURI().toURL();
                    DOC_ZIP_URL = FileUtil.getArchiveRoot(url);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getAnonymousLogger().warning(String.format("Cannot locate the %s documentation file.", DOC_ZIP_FILE_NAME)); //NOI18N
            }
        }
        return DOC_ZIP_URL;
    }

    static URL resolveLink(String relativeLink) {
        try {
            return new URI(getZipURL().toExternalForm() + relativeLink).toURL();
        } catch (URISyntaxException ex) {
            Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    static String getContentAsString(URL url, Charset charset) {
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        try {
            URLConnection con = url.openConnection();
            con.connect();
            Reader r = new InputStreamReader(new BufferedInputStream(con.getInputStream()), charset);
            char[] buf = new char[2048];
            int read;
            StringBuilder content = new StringBuilder();
            while ((read = r.read(buf)) != -1) {
                content.append(buf, 0, read);
            }
            r.close();
            return content.toString();
        } catch (IOException ex) {
            Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static String getSectionContent(URL url, Charset charset) {
        String surl = url.toExternalForm();
        int hashIndex = surl.indexOf('#');
        if (hashIndex == -1) {
            throw new IllegalArgumentException();
        }
        String sectionName = surl.substring(hashIndex + 1);

        String content = getContentAsString(url, charset);
        Matcher matcher = SECTIONS_PATTERN.matcher(content);

        int from = -1;
        int to = -1;
        while (matcher.find()) {
            if (from != -1) {
                to = matcher.start();
                break;
            }
            if (matcher.group(1).equals(sectionName)) {
                from = matcher.start();
            }
        }

        if (from != -1) {
            if (to == -1) {
                to = content.length();
            }
            return content.substring(from, to);
        } else {

            return null;
        }
    }
}
