/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ko4j.debugging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

abstract class WebKitDebuggingTransport implements TransportImplementation, Runnable {
    private ResponseCallback callback;
    private volatile String urlToLoad; // The url to be loaded to the browser
    
    private static RequestProcessor RP = new RequestProcessor("CLI debugging callback");
    
    private static final Logger LOGGER = Logger.getLogger(WebKitDebuggingTransport.class.getName());
    private RequestProcessor.Task task;

    protected WebKitDebuggingTransport() {
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public boolean attach() {
        task = RP.post(this);
        return true;
    }

    @Override
    public boolean detach() {
        return true;
    }
    
    final void waitFinished() {
        if (task != null) {
            task.waitFinished();
        }
    }

    @Override
    public void sendCommand(final Command command) {
        final String cmd = command.toString();
        assert cmd.indexOf('\n') == -1 : "No new line: " + cmd;
        LOGGER.log(Level.INFO, "toDebuggee: {0}", cmd);
        final PrintStream os = outputStream();
        os.println(cmd);
    }

    @Override
    public void registerResponseCallback(ResponseCallback callback) {
        this.callback = callback;
    }

    @Override
    public String getConnectionName() {
        return "Local connection";
    }
    
    @Override
    public URL getConnectionURL() {
        try {
            return new URL(urlToLoad);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
    
    @Override
    public void run() {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream()));
            for (;;) {
                String line = r.readLine();
                LOGGER.log(Level.INFO, "fromDebgee: {0}", line);
                if (line == null) {
                    break;
                }
                msgToIde(line);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Error reading input", ex);
        }
    }
    
    private void msgToIde(String p) {
        try {
            JSONObject json = (JSONObject)JSONValue.parseWithException(p);
            if( null != callback )
                callback.handleResponse(new Response(json));
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected abstract InputStream inputStream();
    protected abstract PrintStream outputStream();
    
}
