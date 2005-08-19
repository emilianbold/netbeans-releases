/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ExecSupport.java
 *
 * Created on March 5, 2004, 12:57 PM
 */

package org.netbeans.modules.derby;
import java.io.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.windows.*;
/**
 *
 * @author  ludo
 */
public class ExecSupport {
    
    private String lookFor;
    private OutputCopier[] copyMakers;
    
    /** Creates a new instance of ExecSupport */
    public ExecSupport() {
    }
    
    public void setStringToLookFor(String lookFor) {
        this.lookFor = lookFor;
    }
    
    public boolean isStringFound() {
        if (copyMakers == null)
            return false;
        return (copyMakers[0].stringFound() ||
                copyMakers[1].stringFound() ||
                copyMakers[2].stringFound());
    }
    
    /**
     * Redirect the standard output and error streams of the child
     * process to an output window.
     */
    public void displayProcessOutputs(final Process child, String displayName)
    throws IOException, InterruptedException {
        // Get a tab on the output window.  If this client has been
        // executed before, the same tab will be returned.
        InputOutput io = org.openide.windows.IOProvider.getDefault().getIO(
            displayName, false);
        OutputWriter ow = io.getOut();
        try {
            io.getOut().reset();
        }
        catch (IOException e) {
            // not a critical error, continue
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        io.select();
        copyMakers = new OutputCopier[3];
        (copyMakers[0] = new OutputCopier(new InputStreamReader(child.getInputStream()), io.getOut(), true, lookFor)).start();
        (copyMakers[1] = new OutputCopier(new InputStreamReader(child.getErrorStream()), io.getErr(), true, lookFor)).start();
        (copyMakers[2] = new OutputCopier(io.getIn(), new OutputStreamWriter(child.getOutputStream()), true)).start();
        new Thread() {
            public void run() {
                try {
                    int ret = child.waitFor();
                    Thread.sleep(2000);  // time for copymakers
                } catch (InterruptedException e) {
                } finally {
                    try {
                        copyMakers[0].interrupt();
                        copyMakers[1].interrupt();
                        copyMakers[2].interrupt();
                    }
                    catch (Exception e) {
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
        private String stringToLookFor;
        private boolean stringFound = false;
        
        
        private static final int FOUND = SearchUtil.FOUND;
        
        public OutputCopier(Reader is, Writer os, boolean b, String lookFor) {
            this.os = os;
            this.is = is;
            autoflush = b;
            this.stringToLookFor = lookFor;
        }
        
        public OutputCopier(Reader is, Writer os, boolean b) {
            this(is, os, b, null);
        }
        
        public boolean stringFound() {
            return stringFound;
        }
        
        /* Makes copy. */
        public void run() {
            int read;
            int stringFoundChars = 0;
            char[] buff = new char [256];
            try {
                while ((read = read(is, buff, 0, 256)) > 0x0) {
                    if (stringToLookFor != null) {
                        stringFoundChars = SearchUtil.checkForString(stringToLookFor, stringFoundChars, buff, read);
                        if (stringFoundChars == FOUND) {
                            stringToLookFor = null;
                            stringFound = true;
                        }
                    }
                    if (os!=null){
                        os.write(buff,0,read);
                        if (autoflush) os.flush();
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

    /** Waits for startup of a server, waits until the message set through the setStringToLookFor() method. 
     *  @param progressMessage message to be displayed in the progress bar. If null, no progress bar is shown.
     *  @param timeout timeout
     *  @return true if the connection was successfully established, false if timed out
     */ 
    public boolean waitForMessage(String progressMessage, int timeout) {
        int retryTime = 10;
        ProgressHandle ph = null;
        if (progressMessage != null) {
            ph = ProgressHandleFactory.createHandle(progressMessage);
            ph.start();
        }
        try {
            Connect connect = new Connect(retryTime); 
            Thread t = new Thread(connect);
            t.start();
            try {
                t.join(timeout);
            } catch(InterruptedException ie) {
            }
            if (t.isAlive()) {
                connect.finishLoop();
                t.interrupt();//for thread deadlock
            }
            return connect.getStatus();
        }
        finally {
            if (ph != null)
                ph.finish();
        }
    }
    
    private class Connect implements Runnable  {

        int retryTime;
        boolean status = false;
        boolean loop = true;

        public Connect(int retryTime) {
            this.retryTime = retryTime; 
        } 

        public void finishLoop() {
            loop = false;
        }

        public void run() {
            while (loop) {
                if (isStringFound()) {
                    status = true;
                    break;
                }
                try {
                    Thread.currentThread().sleep(retryTime);
                } catch(InterruptedException ie) {
                }
            }
        }

        boolean getStatus() {
            return status;
        }
    }
    
    
    
}
