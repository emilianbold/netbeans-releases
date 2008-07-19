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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.common.actions.DebugAction;
import org.netbeans.modules.glassfish.common.actions.RefreshAction;
import org.netbeans.modules.glassfish.common.actions.RestartAction;
import org.netbeans.modules.glassfish.common.actions.StartServerAction;
import org.netbeans.modules.glassfish.common.actions.StopServerAction;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;


/**
 * This class is capable of tailing the specified file or input stream. It
 * checks for changes at the specified intervals and outputs the changes to
 * the given I/O panel in NetBeans
 *
 * FIXME Refactor: LogViewMgr should be a special case of SimpleIO
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
    private static final Map<String, LogViewMgr> instances = new HashMap<String, LogViewMgr>();
    
    /**
     * The I/O window where to output the changes
     */
    private InputOutput io;

    /**
     * Active readers for this log view.  This list contains references either
     * to nothing (which means log is not active), a single file reader to
     * monitor server.log if the server is running outside the IDE, or two
     * stream readers for servers started within the IDE.
     *
     * !PW not sure this complexity is worth it.  Reading server.log correctly
     * is a major pain compared to reading server I/O streams directly.  But we
     * don't have that luxury for servers created outside the IDE, so this is a
     * feeble attempt to have our cake and eat it too :)  I'll probably regret
     * it later.
     */
    private final List<WeakReference<LoggerRunnable>> readers =
            Collections.synchronizedList(new ArrayList<WeakReference<LoggerRunnable>>());

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
        } catch (IOException ex) {
            // no op
        }
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
    
    public void ensureActiveReader(File serverLog) {
        synchronized (readers) {
            boolean activeReader = false;
            for(WeakReference<LoggerRunnable> ref: readers) {
                LoggerRunnable logger = ref.get();
                if(logger != null) {
                    activeReader = true;
                    break;
                }
            }

            if(!activeReader && serverLog != null) {
                readFiles(new File [] { serverLog });
            }
        }
    }

    /**
     * Reads a newly included InputSreams
     *
     * @param inputStreams InputStreams to read
     */
    public void readInputStreams(InputStream... inputStreams) {
        synchronized (readers) {
            stopReaders();
            
            RequestProcessor rp = RequestProcessor.getDefault();
            for(InputStream inputStream : inputStreams){
                // LoggerRunnable will close the stream if necessary.
                LoggerRunnable logger = new LoggerRunnable(inputStream, false);
                readers.add(new WeakReference<LoggerRunnable>(logger));
                rp.post(logger);
            }
        }
    }
    
    /**     
     * Reads a newly included Files
     * 
     * @param files Files to read
     */
    public void readFiles(File[] files) {
        synchronized (readers) {
            stopReaders();
            
            RequestProcessor rp = RequestProcessor.getDefault();
            for(File file : files) {
                try {
                    // LoggerRunnable will close the stream.
                    LoggerRunnable logger = new LoggerRunnable(new FileInputStream(file), true);
                    readers.add(new WeakReference<LoggerRunnable>(logger));
                    rp.post(logger);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage());
                }
            }
        }
    }
    
    private void stopReaders() {
        synchronized (readers) {
            for(WeakReference<LoggerRunnable> ref: readers) {
                LoggerRunnable logger = ref.get();
                if(logger != null) {
                    logger.stop();
                }
            }
            readers.clear();
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
        
        private final InputStream inputStream;
        private final boolean ignoreEof;
        private volatile boolean shutdown;
        
        public LoggerRunnable(InputStream inputStream, boolean ignoreEof) {
            this.inputStream = inputStream;
            this.ignoreEof = ignoreEof;
            this.shutdown = false;
        }

        public void stop() {
            shutdown = true;
        }
        
        /**
         * Implementation of the Runnable interface. Here all tailing is
         * performed
         */
        public void run() {
            final String originalName = Thread.currentThread().getName();
            Reader reader = null;
            
            try {
                Thread.currentThread().setName(this.getClass().getName() + " - " + inputStream);
                
                // create a reader from the input stream
                reader = new BufferedReader(new InputStreamReader(inputStream));
                
                // read from the input stream and put all the changes to the I/O window
                char [] chars = new char[1024];
                int len = 0;
                while(!shutdown && len != -1) {
                    if(ignoreEof) {
                        // For file streams, only read if there is something there.
                        while(!shutdown && reader.ready()) {
                            write(new String(chars, 0, reader.read(chars)));
                            selectIO();
                        }
                    } else {
                        // For process streams, check for EOF every <DELAY> interval.
                        while(!shutdown && (len = reader.read(chars)) != -1) {
                            write(new String(chars, 0, len));
                            selectIO();

                            if(!reader.ready()) {
                                break;
                            }
                        }
                    }
                    
                    // sleep for a while when the stream is empty
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger("glassfish").log(Level.SEVERE, "I/O exception reading server log", ex);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger("glassfish").log(Level.SEVERE, "I/O exception closing server log", ex);
                }
                
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish").log(Level.WARNING, "I/O exception closing stream buffer", ex);
                    }
                }
                
                Thread.currentThread().setName(originalName);
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
                new DebugAction.OutputAction(commonSupport),
                new RestartAction.OutputAction(commonSupport),
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