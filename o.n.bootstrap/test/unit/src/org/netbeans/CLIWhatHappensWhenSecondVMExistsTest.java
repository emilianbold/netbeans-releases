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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fakepkg.FakeHandler;
import org.netbeans.junit.NbTestCase;

/** Tests that handler can set netbeans.mainclass property in its constructor.
 *
 * @author Jaroslav Tulach
 */
public class CLIWhatHappensWhenSecondVMExistsTest extends NbTestCase
implements Map {
    private boolean called;
    private Exception e;
    private int howMuchOut;
    private boolean open;
    private static Logger LOG;
    
    public CLIWhatHappensWhenSecondVMExistsTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.FINEST;
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        LOG = Logger.getLogger("TEST." + getName());
        called = false;
        
        System.setProperty("netbeans.mainclass", CLIWhatHappensWhenSecondVMExistsTest.class.getName());
        
        FakeHandler.chained = this;
    }

    public static void main(String[] args) {
        // ok, ready to work
        LOG.info("We are in main, finishInitialization now");
        CLIHandler.finishInitialization(false);
        LOG.info("finishInitialization done");
    }
    
    private int cli(CLIHandler.Args a) {
        called = true;
        
        
        String[] args = a.getArguments();
        LOG.info("  cli: args: " + Arrays.asList(args));
        
        boolean yes = false;
        for (int i = 0; i < args.length; i++) {
            if ("--userdir".equals(args[i])) {
                args[i] = null;
                System.setProperty("netbeans.user", args[i + 1]);
                args[i + 1] = null;
            }
            if ("--generate".equals(args[i])) {
                yes = true;
                args[i] = null;
            }
        }
        
        LOG.info("  yes: " + yes);
        if (yes) {
            this.open = a.isOpen();
            assertTrue("We are open at begining", this.open);
            try {
                OutputStream os = a.getOutputStream();
                os.write("123\n".getBytes());
                LOG.info("send 123 to the output stream");
                for (howMuchOut = 0; howMuchOut < 1000; howMuchOut++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        this.e = ex;
                    }
                    LOG.info(" howMuchOut " + howMuchOut);
                    
                    if (!a.isOpen()) {
                        LOG.info("a is closed, break");
                        break;
                    }
                }
                
            } catch (IOException ex) {
                this.e = ex;
                LOG.log(Level.WARNING, "Exception while writing", ex);
            } finally {
                synchronized (this) {
                    this.open = a.isOpen();
                    notifyAll();
                }
                LOG.info("open assigned " + this.open + " all notified");
            }
        }
        
        LOG.info("Exit cli");
        return 0;
    }
    

    public void testGet1000AndExit() throws Exception {
        LOG.info("testGet1000AndExit starts");
        org.netbeans.MainImpl.main(new String[] { "--userdir", getWorkDirPath() });
        LOG.log(Level.INFO, "main finished with userdir {0}", getWorkDirPath());
        assertEquals("Called", true, called);

        called = false;
        Process p = exec(new String[] { "--userdir", getWorkDirPath(), "--generate" });
        
        byte[] arr = new byte[4];
        int offset = 0;
        int time = 10;
        InputStream is = p.getInputStream();
        while(offset < 4 && time-- > 0) {
            offset += is.read(arr, offset, arr.length - offset);
        }
        assertEquals("Ofset is 4", 4, offset);
        
        String s = new String(arr);
        assertEquals("123\n", s);
        
        assertEquals("Our main method called once more", true, called);

        try {
            int r = p.exitValue();
            fail("We should be still running: " + r);
        } catch (IllegalThreadStateException ex) {
            // ok
        }
        // destroy the 
        p.destroy();

        // wait for it to be killed
        int result = p.waitFor();
        
        synchronized (this) {
            int cnt = 10;
            while (this.open && cnt-- > 0) {
                this.wait(1000);
            }
        }
        if (this.open) {
            fail("We should not be open: " + howMuchOut + " open: " + this.open);
        }
        if (e instanceof InterruptedException) {
            // ok
            e = null;
        }

        
        if (e != null) {
            throw e;
        }
    }
    
    private static Process exec(String[] args) throws IOException {
        String s = System.getProperty("java.home");
        assertNotNull(s);
        String cp = System.getProperty("java.class.path");
        assertNotNull(cp);
        
        ArrayList<String> l = new ArrayList<String>();
        l.add(s + File.separator + "bin" + File.separator + "java");
        l.add("-cp");
        l.add(cp);
        l.add("org.netbeans.Main");
        l.addAll(Arrays.asList(args));
        
//        System.err.println("exec: " + l);
        
        args = l.toArray(args);
        
        return Runtime.getRuntime().exec(args);
    }
    
    //
    // To allow callback from FakeHandler
    //

    public int size() {
        fail("Not implemented");
        return 0;
    }

    public boolean isEmpty() {
        fail("Not implemented");
        return true;
    }

    public boolean containsKey(Object key) {
        fail("Not implemented");
        return true;
    }

    public boolean containsValue(Object value) {
        fail("Not implemented");
        return true;
    }

    public Object get(Object key) {
        CLIHandler.Args a = (CLIHandler.Args)key;
        return new Integer(cli(a));
    }

    public Object put(Object key, Object value) {
        fail("Not implemented");
        return null;
    }

    public Object remove(Object key) {
        fail("Not implemented");
        return null;
    }

    public void putAll(Map t) {
        fail("Not implemented");
    }

    public void clear() {
        fail("Not implemented");
    }

    public Set keySet() {
        fail("Not implemented");
        return null;
    }

    public Collection values() {
        fail("Not implemented");
        return null;
    }

    public Set entrySet() {
        fail("Not implemented");
        return null;
    }
}
