/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.startup.layers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.net.UnknownServiceException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * StreamHandlerFactory for nbinst protocol
 */
@org.openide.util.lookup.ServiceProvider(service=java.net.URLStreamHandlerFactory.class)
public class NbinstURLStreamHandlerFactory implements URLStreamHandlerFactory {

    /**
     * Creates URLStreamHandler for nbinst protocol
     * @param protocol
     * @return NbinstURLStreamHandler if the protocol is nbinst otherwise null
     */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (NbinstURLMapper.PROTOCOL.equals(protocol)) {
            return new NbinstURLStreamHandler ();
        }
        return null;
    }

    /**
     * URLStreamHandler for nbinst protocol
     */
    private static class NbinstURLStreamHandler extends URLStreamHandler {

        /**
         * Creates URLConnection for URL with nbinst protocol.
         * @param u URL for which the URLConnection should be created
         * @return URLConnection
         * @throws IOException
         */
        protected URLConnection openConnection(URL u) throws IOException {
            return new NbinstURLConnection (u);
        }
    }

    /** URLConnection for URL with nbinst protocol.
     *
     */
    private static class NbinstURLConnection extends URLConnection {

        private FileObject fo;
        private InputStream iStream;

        /**
         * Creates new URLConnection
         * @param url the parameter for which the connection should be
         * created
         */
        public NbinstURLConnection (URL url) {
            super (url);
        }


        public void connect() throws IOException {
            if (fo == null) {
                FileObject[] decoded = NbinstURLMapper.decodeURL(this.url);
                if (decoded != null && decoded.length>0) {
                    fo = decoded[0];
                }
                else {
                    throw new FileNotFoundException("Cannot find: " + url); // NOI18N
                }
            }
            if (fo.isFolder()) {
                throw new UnknownServiceException();
            }
        }

        public int getContentLength() {
            try {
                this.connect();
                return (int) this.fo.getSize();     //May cause overflow long->int
            } catch (IOException e) {
                return -1;
            }
        }


        public InputStream getInputStream() throws IOException {
            this.connect();
            if (iStream == null) {
                iStream = fo.getInputStream();
            }
            return iStream;
        }


        public String getHeaderField (String name) {
            if ("content-type".equals(name)) {                  //NOI18N
                try {
                    this.connect();
                    return fo.getMIMEType();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
            return super.getHeaderField(name);
        }
    }
}
