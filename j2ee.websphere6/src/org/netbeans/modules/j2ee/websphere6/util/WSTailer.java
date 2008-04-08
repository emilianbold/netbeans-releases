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
package org.netbeans.modules.j2ee.websphere6.util;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.*;
import org.openide.windows.*;

/**
 * This class is capable of tailing the specified file or input stream. It
 * checks for changes at the specified intervals and outputs the changes to
 * the given I/O panel in NetBeans
 *
 * @author Kirill Sorokin
 */
public class WSTailer extends Thread {
    
    /**
     * Amount of time in milliseconds to wait between checks of the input
     * stream
     */
    private static final int delay = 1000;
        
    private static Map isIOPanelOpen = Collections.synchronizedMap((Map)new HashMap(2,1));
    
    /**
     * The file for which to track changes
     */
    private File file;
    
    /**
     * The input stream for which to track changes
     */
    private InputStream inputStream;
    
    /**
     * The I/O window where to output the changes
     */
    private InputOutput io;
    
    /**
     * Creates a new instance of WSTailer
     *
     * @param file the file for which to track changes
     * @param ioPanelName the I/O window where to output the changes
     */
    public WSTailer(File file, String ioPanelName) {
        // save the parameters
        this.file = file;
        this.io = IOProvider.getDefault().getIO(ioPanelName, false);        
    }
    
    /**
     * Creates a new instance of WSTailer
     *
     * @param file the input stream for which to track changes
     * @param ioPanelName the I/O window where to output the changes
     */
    public WSTailer(InputStream inputStream, String ioPanelName) {
        // save the parameters
        this.inputStream = inputStream;
        this.io = IOProvider.getDefault().getIO(ioPanelName, false);        
    }
    
    /**
     * Implementation of the Runnable interface. Here all tailing is
     * performed
     */
    public void run() {
        if(isIOPanelOpen.containsKey(io)) {
            io.select();
            return;
        }
        
        isIOPanelOpen.put(io, new Object());
        try {            
            
            if(io.isClosed()) {                
                io.getOut().reset();
            }            
            this.io.select();
            
            // check the source for the tailing, if it is a file we create a
            // new FileInputStream            
            if (file != null) {
                while (inputStream == null) {
                    try {
                        inputStream = new FileInputStream(file);
                    } catch (IOException e) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ex) {
                            continue;
                        }
                    }
                }
            }            
            // create a reader from the input stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            // read from the input stream and put all the changes to the
            // I/O window
            String line;                   
            while (!io.isClosed()) {
                // while there is something in the stream to be read - read that
                while (reader.ready()) {
                    line = reader.readLine();                    
                    if(line!=null) {
                        io.getOut().println(line);
                        io.getOut().flush();                        
                    }
                }
                
                // when the stream is empty - sleep for a while
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }            
        } catch (FileNotFoundException e) {            
            Logger.getLogger("global").log(Level.WARNING, null, e);
            return;
        } catch (IOException e) {            
            Logger.getLogger("global").log(Level.WARNING, null, e);
        } finally {
            // close the opened stream
            try {
                isIOPanelOpen.remove(io);
                inputStream.close();         
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
        }        
    }
    
}
