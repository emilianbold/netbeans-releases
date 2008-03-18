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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.glassfish.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.common.actions.RefreshAction;
import org.netbeans.modules.glassfish.common.actions.StartServerAction;
import org.netbeans.modules.glassfish.common.actions.StopServerAction;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;


/**
 * This class is capable of tailing the specified file or input stream. It
 * checks for changes at the specified intervals and outputs the changes to
 * the given I/O panel in NetBeans
 *
 * @author Michal Mocnak
 * @author Peter Williams
 */
public class LogViewMgr {
    
    /**
     * Amount of time in milliseconds to wait between checks of the input
     * stream
     */
    private static final int DELAY = 1000;
    
    /**
     * Singleton model pattern
     */
    private static Map<String, LogViewMgr> instances = new HashMap<String, LogViewMgr>();
    
    /**
     * The I/O window where to output the changes
     */
    private InputOutput io;
    
    /**
     * Creates and starts a new instance of Hk2Logger
     * 
     * @param uri the uri of the server
     */
    private LogViewMgr(String uri) {
        io = getServerIO(uri);
        
        if (io == null) {
            return; // finish, it looks like this server instance has been unregistered
        }
        
        // clear the old output
        try {
            io.getOut().reset();
        } catch (IOException ioe) {
            // no op
        }
        
        io.select();
    }
    
    /**
     * Returns uri specific instance of Hk2Logger
     * 
     * @param uri the uri of the server
     * @return uri specific instamce of OCHk2Logger
     */
    public static LogViewMgr getInstance(String uri) {
        LogViewMgr logViewMgr = null;
        synchronized (instances) {
            logViewMgr = instances.get(uri);
            if(logViewMgr == null) {
                logViewMgr = new LogViewMgr(uri);
                instances.put(uri, logViewMgr);
            }
        }
        return logViewMgr;
    }
    
    /**
     * Reads a newly included InputSreams
     *
     * @param inputStreams InputStreams to read
     */
    public void readInputStreams(InputStream... inputStreams) {
        RequestProcessor rp = RequestProcessor.getDefault();
        for(InputStream inputStream : inputStreams){
            rp.post(new LoggerRunnable(inputStream));
        }
    }
    
    /**     
     * Reads a newly included Files
     * 
     * @param files Files to read
     */
    public void readFiles(File[] files) {
        RequestProcessor rp = RequestProcessor.getDefault();
        for(File file : files) {
            try {
                // LoggerRunnable will close the stream.
                rp.post(new LoggerRunnable(new FileInputStream(file)));
            } catch (FileNotFoundException ex) {
                Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage());
            }
        }
    }
    
    /**
     * Writes a message into output
     * 
     * @param s message to write
     */
    public synchronized void write(String s) {
        io.getOut().print(s);
    }
    
    /**
     * Selects output panel
     */
    public synchronized void selectIO() {
        io.select();
    }
    
    private class LoggerRunnable implements Runnable {
        
        private InputStream inputStream;
        
        public LoggerRunnable(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        /**
         * Implementation of the Runnable interface. Here all tailing is
         * performed
         */
        public void run() {
            try {
                // create a reader from the input stream
                Reader reader = new BufferedReader(new InputStreamReader(inputStream));
                
                // read from the input stream and put all the changes to the I/O window
                char [] chars = new char[1024];
                while (true) {
                    // while there is something in the stream to be read - read that
                    while (reader.ready()) {
                        write(new String(chars, 0, reader.read(chars)));
                        selectIO();
                    }
                    
                    // when the stream is empty - sleep for a while
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            } finally {
                // close the opened stream
                try {
                    inputStream.close();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                }
            }
        }
    }
    
    private static final WeakHashMap<ServerInstance, InputOutput> ioWeakMap = 
            new WeakHashMap<ServerInstance, InputOutput>();
    
    public static InputOutput getServerIO(String uri) {

        ServerInstance si = GlassfishInstanceProvider.getDefault().getInstance(uri);

        if (si == null) {
            return null;
        }
        
        synchronized (ioWeakMap) {
            // look in the cache
            InputOutput serverIO = ioWeakMap.get(si);
            if(serverIO != null) {
                return serverIO;
            }

            // look up the node that belongs to the given server instance
            Node node = si.getFullNode();

            // it looks like that the server instance has been removed 
            if (node == null) {
                return null;
            }
            
            // No server control interface...
            GlassfishModule commonSupport = node.getLookup().lookup(GlassfishModule.class);
            if(commonSupport == null) {
                return null;
            }

            Action[] actions = new Action[] {
                new StartServerAction.OutputAction(commonSupport),
//                new DebugAction.OutputAction(node),
//                new RestartAction.OutputAction(node),
                new StopServerAction.OutputAction(commonSupport),
                new RefreshAction.OutputAction(commonSupport)
            };
            InputOutput newIO = IOProvider.getDefault().getIO(si.getDisplayName(), actions);

            // put the newIO in the cache
            ioWeakMap.put(si, newIO);
            return newIO;
        }
    }    
}