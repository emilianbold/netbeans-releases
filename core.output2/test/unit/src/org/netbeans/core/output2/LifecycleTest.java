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

package org.netbeans.core.output2;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;

/**
 *
 * @author tim
 */
public class LifecycleTest extends TestCase {

    public LifecycleTest(String testName) {
        super(testName);
    }

    private OutputWindow win;
    private NbIO io;
    private OutWriter out = null;
    JFrame jf = null;

    OutputTab tab = null;
    OutputPane pane = null;
    protected void setUp() throws java.lang.Exception {
//        Controller.logStdOut = true;
//        Controller.log = true;
        
        jf = new JFrame();
        win = new OutputWindow();
        OutputWindow.DEFAULT = win;
        jf.getContentPane().setLayout (new BorderLayout());
        jf.getContentPane().add (win, BorderLayout.CENTER);
        jf.setBounds (20, 20, 700, 300);
        io = (NbIO) new NbIOProvider().getIO ("Test", false);
        SwingUtilities.invokeAndWait (new Shower());
        io.select();
        sleep();
        sleep();
        tab = (OutputTab) win.getSelectedTab();
        if (tab == null) {
            fail ("Failed in setup - selected tab was null");
        }
        pane = (OutputPane) tab.getOutputPane();
        sleep();
    }
    
    protected void tearDown() {
        tab = null;
        pane = null;
        out = null;
        if (jf != null) {
            jf.dispose();
        }
        jf = null;
        if (io != null) {
            NbIOProvider.dispose(io);
        }
        io = null;
        win = null;
        OutputWindow.DEFAULT = null;
        sleep();
    }
    
    private final void sleep() {
        dosleep();
        dosleep();
        dosleep();
        int ct = 0;
        while (IOEvent.pendingCount > 0) {
            dosleep();
            ct++;
            if (ct > 1000) {
                fail ("After 1000 cycles on the event queue, there is still some IOEvent which was not processed");
            }
        }
    }
    
    private final void dosleep() {
        try {
            Thread.currentThread().sleep(200);
            SwingUtilities.invokeAndWait (new Runnable() {
                public void run() {
                    System.currentTimeMillis();
                }
            });
            Thread.currentThread().sleep(200);
        } catch (Exception e) {
            fail (e.getMessage());
        }
    }    
    
    public class Shower implements Runnable {
        public void run() {
            jf.setVisible(true);
        }
    }
    
    public void testGetErr() throws Exception {
        System.out.println("testGetOut");
        ErrWriter err = io.writer().err();
        assertNull ("Error output should not be created yet", err);
        err = (ErrWriter) io.writer().getErr();
        assertNotNull ("Error output should never be null from getErr()", err);
        assertTrue ("Error output should initially be closed", err.isClosed());
        err.println ("Hello");
        assertFalse ("Error output should not be closed after writing to it", err.isClosed());
        err.close();
        assertTrue ("Error output be closed after calling close()", err.isClosed());
    }
    
    public void testClose() throws Exception {
        System.out.println("testClose");
        NbWriter writer = (NbWriter) io.getOut();
        ErrWriter err = (ErrWriter) writer.getErr();
        OutWriter out = (OutWriter) writer.out();

        writer.reset();
        sleep();
        
        err.println ("hello");
        sleep();
        writer.println ("world");
        sleep();
        
//        assertTrue("Text in container not correct:\"" + pane.getTextView().getText() +"\"", 
//            pane.getTextView().getText().equals ("hello\nworld\n\n\n"));
        
        assertFalse ("Err should not be closed", err.isClosed());
        assertFalse ("Writer should not be closed", writer.isClosed());
//        assertFalse ("Out should not be closed", out.isClosed());
        
        err.close();
        sleep();
        assertFalse ("Out is open, err is closed, writer should return false from isClosed()", writer.isClosed());
        
        writer.close();
        sleep();
        assertTrue ("Out should be closed after calling close() on it", out.isClosed());
        assertTrue ("Out and err are closed, but writer says it is not", writer.isClosed());
        
        assertTrue ("Output's storage is not closed", writer.out().getStorage().isClosed());
        
        writer.reset();
        sleep();

        assertTrue ("After reset, err should be closed", err.isClosed());
        assertTrue ("After reset, writer should be closed", writer.isClosed());
        assertTrue ("After reset, out should be closed", out.isClosed());
        
        err.println ("goodbye");
        writer.println ("world");
        sleep();
        
        assertFalse ("Err should not be closed", err.isClosed());
        assertFalse ("Writer should not be closed", writer.isClosed());
//        assertFalse ("Out should not be closed", out.isClosed());

        //Now close err & out in the opposite order
        writer.close();
        sleep();
        assertTrue ("Out should  be closed after calling close() on it", writer.isClosed());
        
        err.close();
        sleep();
        assertTrue ("Out is closed, err is closed, writer should return true from isClosed()", writer.isClosed());
        assertTrue ("Out and err are closed, but writer says it is not", writer.isClosed());
        
        assertTrue ("Output's storage is not closed", writer.out().getStorage().isClosed());
        
        err.println("I should be reopened now");
        sleep();
        
        assertFalse ("Err should be open", err.isClosed());
    }
    
    public void testReset() throws Exception {
        System.out.println("testReset");
        ErrWriter err = (ErrWriter) io.writer().getErr();
        OutWriter out = (OutWriter) io.writer().out();
        NbWriter writer = io.writer();
        
        OutputDocument doc = (OutputDocument) pane.getDocument();
        assertNotNull ("Document should not be null", doc);
        
        err.println ("hello");
        writer.println ("world");
        sleep();
        writer.reset();
        sleep();
        
        assertTrue ("Same writer object should be used after a reset", io.writer() == writer);
        assertTrue ("Same err object should be used after a reset", io.writer().err() == err);
        assertTrue ("Different output should be used afer a reset", out != io.writer().out());
        
        assertNull ("Old document's Lines object not disposed - that means neither was its writer", doc.getLines());
        
        Exception e = null;
        try {
            out.getStorage();
        } catch (Exception exc) {
            e = exc;
        }
        assertNotNull ("OutWriter should have thrown an exception on trying to " +
            "fetch its storage after it was disposed.  It appears it wasn't disposed.", e);
    }

    public void testCloseInputOutput() throws Exception {
        
        System.out.println("testCloseInputOutput");
        ErrWriter err = (ErrWriter) io.writer().getErr();
        OutWriter out = (OutWriter) io.writer().out();
        NbWriter writer = io.writer();
        
        err.println ("joy to the world");
        writer.println ("all the boys and girls");
        err.close();
        sleep();
        writer.close();
        sleep();
        
        io.closeInputOutput();
        sleep();        
        
        assertNull ("Should be no selected tab after closeInputOutput", win.getSelectedTab());
    } 
    
    public void testFilesCleanedUp() throws Exception {
        System.out.println("testFilesCleanedUp");
        NbWriter writer = io.writer();
        ErrWriter err = (ErrWriter) writer.getErr();
        OutWriter out = (OutWriter) writer.out();
        
        err.println ("hello");
        writer.println ("world");
        sleep();
        
        assertTrue ("Output should not have changed - was " + out + " now " + io.writer().out(), io.writer().out() == out);
        FileMapStorage storage = (FileMapStorage) writer.out().getStorage();
        String fname = storage.toString();
        assertTrue ("FileMapStorage should be returning a file name", fname.indexOf("[") == -1);
        assertTrue ("FileMapStorage should be pointing to an existing file", new File(fname).exists());
        
        err.close();
        sleep();
        writer.close();
        sleep();
        io.closeInputOutput();
        sleep();

        assertTrue (out.isDisposed());
        sleep();
//        assertFalse ("FileMapStorage's file should have been deleted", new File(fname).exists());
    }
    
    public void testMultipleResetsAreHarmless() throws Exception {
        System.out.println("testMultipleResetsAreHarmless");
        NbWriter writer = io.writer();
        ErrWriter err = (ErrWriter) writer.getErr();
        OutWriter out = (OutWriter) writer.out();
        
        assertTrue ("Before any writes, out should be empty", out.isEmpty());
        
        writer.reset();
        sleep();
        assertTrue ("Reset on an unused writer should not replace its output", writer.out() == out);
        
        writer.reset();
        writer.reset();
        writer.reset();
        sleep();
        assertTrue ("Reset on an unused writer should not replace its output", writer.out() == out);
        
        writer.println ("Now there is data");
        writer.reset();
        sleep();
        
        assertFalse ("Reset on a used writer should replace its underlying output", writer.out() == out);
        
    }
    
}
