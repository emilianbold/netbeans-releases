/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.sandbox.download.connection;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Date;
import org.netbeans.installer.sandbox.download.DownloadOptions;
import org.netbeans.installer.utils.exceptions.InitializationException;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class Connection {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int DEFAULT_CONNECTION_TIMEOUT = 3000;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    protected static int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    
    public static Connection getConnection(URI uri, long offset, long length, 
            DownloadOptions options) throws InitializationException {
        try {
            if (uri.getScheme().equals("http")) {
                return new HTTPConnection(uri.toURL(), offset, length, options);
            }
            if (uri.getScheme().equals("file")) {
                return new FileConnection(new File(uri));
            }
            if (uri.getScheme().equals("resource")) {
                return new ResourceConnection(uri.getSchemeSpecificPart(), 
                        (ClassLoader) options.get(DownloadOptions.CLASSLOADER));
            }
        } catch (MalformedURLException e) {
            throw new InitializationException("Cannot convert the " +
                    "supplied URI to an URL, which is required for connections " +
                    "of this type.", e);
        }
        
        throw new InitializationException("Cannot initialize " +
                "connection, unknown URI scheme - " + uri.getScheme());
    }
    
    public static int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public static void setConnectionTimeout(int aConnectionTimeout) {
        connectionTimeout = aConnectionTimeout;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public abstract void open() throws IOException;
    
    public abstract void close() throws IOException;
    
    public abstract int read(byte[] buffer) throws IOException;
    
    public abstract int available() throws IOException;
    
    public abstract boolean supportsRanges();
    
    public abstract long getContentLength();
    
    public abstract Date getModificationDate();
}
