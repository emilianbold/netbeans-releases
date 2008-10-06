/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.javascript.debugger.api;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.web.client.tools.api.JSLocation;

/**
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public class JSURILocation implements JSLocation {
    private URI uri;
    private int lineNumber;
    private int columnNumber;
    private Set<URI> extraURIs;

    public JSURILocation(URI uri, int lineNumber) {
        this(uri, lineNumber, -1);
    }

    public JSURILocation(URI uri, int lineNumber, int columnNumber) {
        this.uri = uri;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public JSURILocation(URL url, int lineNumber){
    	this( URI.create(url.getPath()), lineNumber);
    }

    public JSURILocation(String uri, int lineNumber, int columnNumber) {
        try {
            String fileScheme = "file://";

            //In case of URI returned by IE, spaces are not encoded
            if( uri.indexOf(fileScheme) != -1 && uri.indexOf("\\") != -1) {
                uri = uri.substring(fileScheme.length());
                uri = uri.replace("\\", "/");
                this.uri = new File(uri).toURI();
            }
            if(this.uri == null) {
                this.uri = new URI(uri);
            }
            assert this.uri.isAbsolute();
            
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        } catch (URISyntaxException ex) {
            // Decode UTF-16 characters from the URL
            if (uri.contains("%u")) { // NOI18N
                uri = decodeUnicode16Escapes(uri);
                try {
                    this.uri = new URI(uri);
                } catch (URISyntaxException uie) {
                    Log.getLogger().log(Level.SEVERE, "URI syntax exception", uie);
                }
            } else {
                Log.getLogger().log(Level.SEVERE, "URI syntax exception", ex);
            }
        }
    }

    public URI getURI() {
        return uri;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public JSLocation getJSLocation() {
        return this;
    }

    public String getDisplayName() {
        return getURI().toString() + ":" + getLineNumber();
    }

    public Set<URI> getEquivalentURIs() {
        return extraURIs;
    }

    public void addEquivalentURI(URI uri) {
        if (extraURIs == null) {
            extraURIs = new LinkedHashSet<URI>();
        }
        
        extraURIs.add(uri);
    }

    public void removeEquivalentURI(URI uri) {
        if (extraURIs != null) {
            extraURIs.remove(uri);
        }
    }
    private static String decodeUnicode16Escapes(String url) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < url.length()-6; i++) {
            String pattern = url.substring(i, i+6);
            if (pattern.startsWith("%u")) { // NOI18N
                String firstByte = pattern.substring(2, 4);
                String secondByte = pattern.substring(4, 6);
                try {
                    byte[] b = new byte[] { Byte.parseByte(firstByte, 16), Byte.parseByte(secondByte, 16) };
                    String s = new String(b, "UTF-16"); // NOI18N
                    sb.append(s);
                    i += 5;
                } catch (Exception ex) {
                    sb.append(url.charAt(i));
                }
            } else {
                sb.append(url.charAt(i));
            }
        }

        return sb.toString();
    }
}
