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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.extbrowser;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Basic support for default browser funcionality on Unix system,
 * currently using "xdg-open".
 * Note this class is not used for JDK 6 and up, for that purpose is used
 * build-in JDK mechanism (java.awt.Desktop#browse).
 *
 * @author Peter Zavadsky
 */
class NbDefaultUnixBrowserImpl extends ExtBrowserImpl {
    
    private static final String XDG_COMMAND = "xdg-open"; // NOI18N
    private static final String XBROWSER_COMMAND = "x-www-browser"; // NOI18N
    
    private static final RequestProcessor REQUEST_PROCESSOR = 
        new RequestProcessor( NbDefaultUnixBrowserImpl.class );

    private static final boolean XDG_AVAILABLE;
    private static final boolean XBROWSER_AVAILABLE;
    
    static {
        // XXX Lame check to find out whether the functionality is installed.
        // TODO Find some better way to ensure it is there.
        XDG_AVAILABLE = new File("/usr/bin/" + XDG_COMMAND).exists(); // NOI18N
        XBROWSER_AVAILABLE = new File("/usr/bin/" + XBROWSER_COMMAND).exists(); // NOI18N
    }
    
    static boolean isAvailable() {
        return XDG_AVAILABLE || XBROWSER_AVAILABLE;
    }
    
    
    NbDefaultUnixBrowserImpl(ExtWebBrowser extBrowser) {
        super ();
        this.extBrowserFactory = extBrowser;
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "NbDefaultUnixBrowserImpl created with factory: " + extBrowserFactory); // NOI18N
        }
    }

    
    protected void loadURLInBrowser(URL url) {
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "NbDeaultUnixBrowserImpl.setUrl: " + url); // NOI18N
        }
        url = URLUtil.createExternalURL(url, false);
        String urlArg = url.toExternalForm();
        // go with x-www-browser if available
        String command = XBROWSER_AVAILABLE ? XBROWSER_COMMAND : XDG_COMMAND;

        ProcessBuilder pb = new ProcessBuilder(new String[] { command, urlArg });
        try {
            Process p = pb.start();
            REQUEST_PROCESSOR.post(new ProcessWatcher(p));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static final class ProcessWatcher implements Runnable {

        private final Process p;

        ProcessWatcher (Process p) {
            this.p = p;
        }

        public void run() {
            try {
                int exitValue = p.waitFor();
                if (exitValue != 0) {
                    StringBuilder sb = new StringBuilder();
                    InputStream is = p.getErrorStream();
                    try {
                        int curByte = 0;
                        while ((curByte = is.read()) != -1) {
                            sb.append((char)curByte);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    ExtWebBrowser.getEM().log(Level.WARNING, sb.toString()); // NOI18N
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                // XXX #155906 Cleanup of the finished process.
                cleanupProcess(p);
            }
        }
    } // ProcessWatcher

    private static void cleanupProcess(Process p) {
        closeStream(p.getOutputStream());
        closeStream(p.getInputStream());
        closeStream(p.getErrorStream());
        p.destroy();
    }

    private static void closeStream(Closeable stream) {
        try {
            stream.close();
        } catch (IOException ioe) {
            log(ioe);
        }
    }

    private static void log(Exception e) {
        Logger.getLogger(NbDefaultUnixBrowserImpl.class.getName()).log(Level.INFO, null, e);
    }
}
