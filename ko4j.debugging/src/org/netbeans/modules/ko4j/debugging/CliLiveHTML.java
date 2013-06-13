/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ko4j.debugging;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.browser.spi.MessageDispatcher;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.WebKitUIManager;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Env;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach
 */
public final class CliLiveHTML implements ArgsProcessor {
    private static final Logger LOG = Logger.getLogger(CliLiveHTML.class.getName());
    private static DbgSession session;
    
    @Arg(longName = "livehtml")
    public String file; 

    @NbBundle.Messages({
        "# {0} - the url",
        "MSG_InvalidURL=Invalid URL provided '{0}'",
        "# {0} - the file",
        "MSG_InvalidFile=File {0} does not exist",
        "# {0} - the file",
        "MSG_NoProject=Can't find project for {0} file"
    })
    @Override
    public void process(Env env) throws CommandException {
        FileObject fo = findFileObject(env);
        {
            DbgSession prev = session;
            if (prev != null) {
                session = null;
                try {
                    prev.close();
                } catch (Throwable ex) {
                    LOG.log(Level.INFO, "Can't close previous session",ex);
                }
            }
        }
        session = new DbgSession(fo, env);
    }

    private FileObject findFileObject(Env env) throws CommandException {
        File f = new File(file);
        if (!f.exists()) {
            File rel = new File(env.getCurrentDirectory(), file);
            if (rel.exists()) {
                f = rel;
            }
        }
        if (!f.exists()) {
            throw new CommandException(2, Bundle.MSG_InvalidFile(file));
        }
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null) {
            throw new CommandException(2, Bundle.MSG_InvalidFile(file));
        }
        return fo;
    }
    
    
    private static class DbgSession extends MessageDispatcher 
    implements Closeable {
        private final WebKitDebuggingTransport transport;
        private final Session debugSession;
        private final WebKitDebugging webKitDebugging;
        
        DbgSession(FileObject fo, final Env env) {
            transport = new WebKitDebuggingTransport() {
                @Override
                protected InputStream inputStream() {
                    return env.getInputStream();
                }

                @Override
                protected PrintStream outputStream() {
                    return env.getOutputStream();
                }
                
            };
            //        transport.setBaseUrl(url);
            webKitDebugging = Factory.createWebKitDebugging(transport);
            transport.attach();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
    //        if (navigateToUrl) {
    //            webKitDebugging.getPage().navigate(url);
    //        }
            LOG.log(Level.INFO, "Initializing livehtml for {0}", fo);
            Project p = FileOwnerQuery.getOwner(fo);
            webKitDebugging.getDebugger().enable();
            Lookup projectContext = Lookups.singleton(p);
            debugSession = WebKitUIManager.getDefault().createDebuggingSession(webKitDebugging, projectContext);
            Lookup consoleLogger = WebKitUIManager.getDefault().createBrowserConsoleLogger(webKitDebugging, projectContext);
            Lookup networkMonitor = WebKitUIManager.getDefault().createNetworkMonitor(webKitDebugging, projectContext);
            PageInspector.getDefault().inspectPage(Lookups.fixed(webKitDebugging, p, this));
            LOG.log(Level.INFO, "Initialization done for {0}", fo);
            transport.waitFinished();
        }

        @Override
        public void dispatchMessage( String featureId, String message ) {
            super.dispatchMessage( featureId, message );
        }

        @Override
        public void close() {
            transport.detach();
            webKitDebugging.getDebugger().disable();
            debugSession.kill();
        }

    }
}
