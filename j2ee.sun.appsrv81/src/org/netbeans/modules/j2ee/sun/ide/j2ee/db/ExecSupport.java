/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ExecSupport.java
 *
 * Created on March 5, 2004, 12:57 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.db;
import java.io.*;
import org.openide.ErrorManager;
import org.openide.windows.*;
/**
 *
 * @author  ludo
 */
public class ExecSupport {

    /** Creates a new instance of ExecSupport */
    public ExecSupport() {
    }

    /**
     * Redirect the standard output and error streams of the child
     * process to an output window.
     */
    public void displayProcessOutputs(final Process child, String displayName, String initialMessage)
    throws IOException, InterruptedException {
        // Get a tab on the output window.  If this client has been
        // executed before, the same tab will be returned.
        InputOutput io = org.openide.windows.IOProvider.getDefault().getIO( displayName, false);
        OutputWriter ow = io.getOut();
        try {
            io.getOut().reset();
        }
        catch (IOException e) {
            // not a critical error, continue
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        io.select();
        ow.println(initialMessage);
        final Thread[] copyMakers = new Thread[3];
        (copyMakers[0] = new OutputCopier(new InputStreamReader(child.getInputStream()), ow, true)).start();
        (copyMakers[1] = new OutputCopier(new InputStreamReader(child.getErrorStream()), io.getErr(), true)).start();
        (copyMakers[2] = new OutputCopier(io.getIn(), new OutputStreamWriter(child.getOutputStream()), true)).start();
        new Thread() {
            public void run() {
                try {
                    child.waitFor();
                    Thread.sleep(2000);  // time for copymakers
                } catch (InterruptedException e) {
                } finally {
                    try {
                        copyMakers[0].interrupt();
                        copyMakers[1].interrupt();
                        copyMakers[2].interrupt();
                    }
                    catch (Exception e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }.start();
    }
       
    
    
    /** This thread simply reads from given Reader and writes read chars to given Writer. */
    static public  class OutputCopier extends Thread {
        final Writer os;
        final Reader is;
        /** while set to false at streams that writes to the OutputWindow it must be
         * true for a stream that reads from the window.
         */
        final boolean autoflush;
        private boolean done = false;
        
        public OutputCopier(Reader is, Writer os, boolean b) {
            this.os = os;
            this.is = is;
            autoflush = b;
        }
        
        /* Makes copy. */
        public void run() {
            int read;
            char[] buff = new char [256];
            try {
                 while ((read = read(is, buff, 0, 256)) > 0x0) {
                    if (os!=null){
                        os.write(buff,0,read);
                        if (autoflush) {
                            os.flush();
                        }
                    }
                }
            } catch (IOException ex) {
            } catch (InterruptedException e) {
            }
        }
        
        public void interrupt() {
            super.interrupt();
            done = true;
        }
        
        private int read(Reader is, char[] buff, int start, int count) throws InterruptedException, IOException {
            
            while (!is.ready() && !done) sleep(100);
            
            return is.read(buff, start, count);
        }
    }
    
    
}
