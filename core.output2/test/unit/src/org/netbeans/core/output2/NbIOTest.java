/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.output2;

import junit.framework.*;
import org.openide.ErrorManager;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 *
 * @author mkleint
 */
public class NbIOTest extends TestCase {
    
    public NbIOTest(String testName) {
        super(testName);
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(NbIOTest.class);
        
        return suite;
    }

    public void test54117() throws Exception {
        NbIO io = new NbIO("test");
        assertFalse(io.isClosed());
        Reader str = io.getIn();
        assertNotNull(str);
        assertEquals(NbIO.IOReader.class, str.getClass());
        writeText(str);
        int read = str.read(new char[100]);
        // not eof..
        assertTrue(read != -1);
        writeEof(str);
        read = str.read(new char[100]);
        assertTrue(read == -1);
        //reseting
        io.getOut().close();
        io.getErr().close();
        io.dispose();
        io.getOut().reset();
        io.getErr().reset();
        
        str = io.getIn();
        writeText(str);
        read = str.read(new char[100]);
        // not eof..
        assertTrue(read != -1);
        writeEof(str);
        read = str.read(new char[100]);
        assertTrue(read == -1);
        
    }
    
    private void writeText(final Reader reader) {
              NbIO.IOReader rdr = (NbIO.IOReader)reader;
              rdr.pushText("hello");

    }
    private void writeEof(final Reader reader) {
              NbIO.IOReader rdr = (NbIO.IOReader)reader;
              rdr.eof();
    }
    
}
