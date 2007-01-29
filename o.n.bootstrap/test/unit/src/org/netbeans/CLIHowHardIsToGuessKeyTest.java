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
package org.netbeans;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import org.fakepkg.FakeHandler;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;

/** Tests that handler can set netbeans.mainclass property in its constructor.
 *
 * @author Jaroslav Tulach
 */
public class CLIHowHardIsToGuessKeyTest extends NbTestCase {
    private static Object LOCK = new Object();
    
    public CLIHowHardIsToGuessKeyTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.mainclass", "org.netbeans.CLIHowHardIsToGuessKeyTest");
        System.setProperty("netbeans.user", getWorkDirPath());
//        System.setProperty("org.netbeans.CLIHandler", "-1");
    }
    
    public static void main(String[] args) throws Exception {
        org.netbeans.MainImpl.finishInitialization();
        synchronized (LOCK) {
            LOCK.notifyAll();
            LOCK.wait();
        }
    }

    public void testGuessTheKey() throws Exception {
        class R implements Runnable {
            public int cnt;
            
            public void run() {
                cnt++;
            }
        }
        
        R run = new R();
        FakeHandler.toRun = run;
        
        class Main implements Runnable {
            Exception ex;
            public void run() {
                try {
                    org.netbeans.MainImpl.main(new String[] { });
                } catch (Exception ex) {
                    this.ex = ex;
                }
            }
        }
        Main main = new Main();
        synchronized (LOCK) {
            RequestProcessor.getDefault().post(main);
            LOCK.wait();
        }
        
        assertEquals("One call", 1, run.cnt);
        
        if (main.ex != null) {
            throw main.ex;
        }
        
        File lock = new File(getWorkDir(), "lock");
        assertTrue("Lock is created", lock.canRead());
        for (int i = 0; i < 10; i++) {
            if (lock.length() >= 14) {
                break;
            }
            Thread.sleep(500);
        }
        assertTrue("Lock must contain the key now: " + lock.length(), lock.length() >= 14);
        
        final byte[] arr = new byte[10]; // CLIHandler.KEY_LENGTH
        DataInputStream is = new DataInputStream(new FileInputStream(lock));
        final int port = is.readInt();
        int read = is.read(arr);
        assertEquals("All read", arr.length, read);

        FileOutputStream os = new FileOutputStream(lock);
        os.write(arr);
        os.close();
        
        class Connect implements Runnable {
            int times;
            Exception ex;
            
            public void run() {
                
                while(times++ < 100) {
                    // making the key incorrect
                    arr[5]++;
                    try {
                        Socket s = new Socket(localHostAddress(), port);
                        OutputStream os = s.getOutputStream();
                        os.write(arr);
                        os.flush();
                        int reply = s.getInputStream().read();
                        if (reply == 0) { // CLIHandler.REPLY_FAIL
                            continue;
                        }
                        fail("The reply should be fail: " + reply);
                    } catch (Exception ex) {
                        this.ex = ex;
                        return;
                    }
                }
            }
        }
        
        Connect c = new Connect();
        RequestProcessor.getDefault().post(c).waitFinished(5000);
        
        if (c.ex != null) {
            throw c.ex;
        }
        
        if (c.times > 10) {
            fail("Too many allowed connections, the responce has to be slow to prevent secure attacks: " + c.times);
        }
        
    }
    static InetAddress localHostAddress () throws Exception {
        java.net.NetworkInterface net = java.net.NetworkInterface.getByName ("lo");
        if (net == null || !net.getInetAddresses().hasMoreElements()) {
            return InetAddress.getLocalHost();
        }
        else {
            return net.getInetAddresses().nextElement();
        }
    }
}
