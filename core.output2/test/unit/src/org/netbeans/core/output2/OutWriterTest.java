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
/*
 * OutWriterTest.java
 * JUnit based test
 *
 * Created on March 21, 2004, 9:50 PM
 */

package org.netbeans.core.output2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.*;
import org.openide.util.Mutex;
import org.openide.windows.OutputWriter;

/** Tests the OutWriter class
 *
 * @author Tim Boudreau
 */
public class OutWriterTest extends TestCase {
    private static final byte[] lineSepBytes = OutWriter.lineSepBytes;
    
    public OutWriterTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(OutWriterTest.class);
        return suite;
    }
    
   
    public void testPositionOfLine() {
        System.out.println("testPositionOfLine");

        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        int pos = ow.getLines().getLineStart(0);
        
        assertTrue ("First line position should be 0 but is " + pos, pos == 0);
        
        int expectedPosition = first.length() + 1;
        pos = ow.getLines().getLineStart(1);
        
        assertTrue ("Second line position should be length of first (" + first.length() + ") + line " +
            "separator length (" + lineSepBytes.length + "), which should be " + 
            expectedPosition + " but is " + pos, 
            pos == expectedPosition);
         
        
        pos = ow.getLines().getLineStart (2);
        int targetPos = first.length() + second.length() + (lineSepBytes.length * 2) - 2;
        
        assertTrue ("Third line position should be " + targetPos + " but is " +
            pos, pos == targetPos);
    }
    
    
    public void testPosition() {
        System.out.println("testPosition");
        
        OutWriter ow = new OutWriter();

        
        String first = "This is the first string";
        String second ="This is the second string";
        String third = "This is the third string";
        
        assertTrue (ow.getLines().getLineCount() == 0);
        
        ow.println(first);
        
        assertTrue (ow.getLines().getLineCount() == 1);
        
        ow.println (second);
        
        assertTrue (ow.getLines().getLineCount() == 2);
        
        int targetLength = first.length() + second.length() + 2;

        assertTrue ( 
            "After printing strings with length " + first.length() + " and " + 
            second.length() + " outfile position should be " + targetLength +
            " not " + ow.getLines().getCharCount(),
            ow.getLines().getCharCount() == targetLength);
        
        ow.println (third);
        
        targetLength = first.length() + second.length() + third.length() + 
            3;
        
        assertTrue ("Length should be " + targetLength + " but position is "
            + ow.getLines().getCharCount(), targetLength == ow.getLines().getCharCount());
    }
    
    public void testLine() {
        System.out.println("testLine");
        
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        assertTrue ("After writing 3 lines, linecount should be 3, not " + 
            ow.getLines().getLineCount(), ow.getLines().getLineCount() == 3);
        
        String firstBack = null;
        String secondBack = null;
        String thirdBack = null;
        try {
            firstBack = ow.getLines().getLine(0);
            secondBack = ow.getLines().getLine(1);
            thirdBack = ow.getLines().getLine(2);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail (ioe.getMessage());
        }
        
        String firstExpected = first + "\n";
        String secondExpected = second + "\n";
        String thirdExpected = third + "\n";
        
        assertEquals("First string should be \"" + firstExpected + "\" but was \"" + firstBack + "\"",
            firstBack, firstExpected);
        
        assertEquals("Second string should be \"" + secondExpected + "\" but was \"" + secondBack + "\"",
            secondBack, secondExpected);

        assertEquals("Third string should be \"" + thirdExpected + "\" but was \"" + thirdBack + "\"",
            thirdBack, thirdExpected);
        
    }
     
    public void testLineForPosition() {
        System.out.println("testLineForPosition");
        
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        int line = ow.getLines().getLineAt (first.length() / 2);
        
        assertTrue ("Position halfway through first line should map to line 0," +
            " not " + line,
            line == 0);
        
        line = ow.getLines().getLineAt (first.length() + lineSepBytes.length +
            (second.length() / 2));
        
        assertTrue ("Position halfway through line 1 should map to line 1, not " +
            line,
            line == 1);
        
        //XXX do some more tests here for very large buffers, to ensure no
        //off-by-ones
        
    }
    
    public void testLineCount() {
        System.out.println("testLineCount");
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        ow.flush();
        try {
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                System.currentTimeMillis();
             }
        });
        } catch (Exception e) {}
        Thread.currentThread().yield();
        
        assertTrue ("Linecount should be 3 after printing 3 lines, not " +
            ow.getLines().getLineCount(), ow.getLines().getLineCount()==3);
    }
    
    public void testAddChangeListener() {
        System.out.println("testAddChangeListener");
        OutWriter ow = new OutWriter ();
        
        CL cl = new CL();
        try {
            ow.getLines().addChangeListener(cl);
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Caught exception " + e);
        }
        
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        ow.flush();        
        
        cl.assertChanged();
        
    }
    
    public void testMultilineText() {
        System.out.println("testMultilineText");
        OutWriter ow = new OutWriter ();
        String threeLines = "This is\nthree lines of\nText";
        ow.println(threeLines);
        assertTrue ("Line count should be 3, not " + ow.getLines().getLineCount(), ow.getLines().getLineCount() == 3);
        ow.println("This is another line");
        assertTrue ("Line count should be 4, not " + ow.getLines().getLineCount(), ow.getLines().getLineCount() == 4);
        ow.println(threeLines);
        assertTrue ("Line count should be 7, not " + ow.getLines().getLineCount(), ow.getLines().getLineCount() == 7);
    }
    
    public void testRemoveChangeListener() {
        System.out.println("testRemoveChangeListener");
        
        
        
        OutWriter ow = new OutWriter ();
        
        CL cl = new CL();
        try {
            ow.getLines().addChangeListener(cl);
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Caught exception " + e);
        }


        ow.getLines().removeChangeListener(cl);

        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        ow.flush();        
        
        cl.assertNoChange();
    }
    
    public void testCheckDirty() {
        System.out.println("testCheckDirty");
        
        
        OutWriter ow = new OutWriter ();

        boolean dirty = ow.getLines().checkDirty(true);
        
        String first = "This is the a test";
        
        ow.println(first);
        
        
        //plan to delete checkDirty
    }
    
    public void testSubstring() {
        System.out.println("testSubstring");
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        
        //First test intra-line substrings
        
        String expected = first.substring(5, 15);
        String gotten = ow.getLines().getText (5, 15);
        System.err.println("\nGot " + gotten + "\n");
        
        assertEquals ("Should have gotten string \"" + expected + "\" but got \"" + gotten + "\"", expected, gotten);
        
        
    }    
    
    public void testPrintln() {
        System.out.println("testPrintln");

        try {
            OutWriter ow = new OutWriter ();

            String first = "This is a test string";

            ow.println(first);
            ow.flush();
            
            String firstExpected = first + "\n";
            String firstReceived = ow.getLines().getLine(0);
            
            assertEquals ("First line should be \"" + firstExpected + "\" but was \"" + firstReceived + "\"", firstExpected, firstReceived);
        
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
        
    }
    
    public void testReset() {
        System.out.println("testReset");
        
    }
    
    public void testFlush() {
        System.out.println("testFlush");
        
    }
    
    public void testClose() {
        System.out.println("testClose");
        
    }

    public void testCheckError() {
        System.out.println("testCheckError");
        
    }

    public void testSetError() {
        System.out.println("testSetError");
        
    }

    public void testWrite() {
        System.out.println("testWrite");
        try {
            OutWriter ow = new OutWriter ();

            ow.write('x');
            ow.write('y');
            ow.write('z');
            ow.println();
            ow.flush();
            assertEquals(1, ow.getLines().getLineCount());
            String firstReceived = ow.getLines().getLine(0);
            assertEquals ("xyz\n", firstReceived);
        
            ow = new OutWriter();
            ow.println("firstline");
            ow.write('x');
            ow.println("yz");
            ow.flush();
            assertEquals(2, ow.getLines().getLineCount());
            firstReceived = ow.getLines().getLine(1);
            assertEquals ("xyz\n", firstReceived);
            
            ow = new OutWriter();
            ow.println("firstline");
            ow.write(new char[] {'x', 'y', 'z'});
            ow.write(new char[] {'x', 'y', 'z'});
            ow.println("-end");
            ow.flush();
            assertEquals(2, ow.getLines().getLineCount());
            firstReceived = ow.getLines().getLine(1);
            assertEquals ("xyzxyz-end\n", firstReceived);
            
            ow = new OutWriter();
            ow.write(new char[] {'x', 'y', '\n', 'z', 'z', 'z', '\n', 'A'});
            ow.println();
            ow.flush();
            assertEquals(3, ow.getLines().getLineCount());
            assertEquals("xy\n", ow.getLines().getLine(0));
            assertEquals("zzz\n", ow.getLines().getLine(1));
            System.out.println("last=" + ow.getLines().getLine(2));
            assertEquals("A\n", ow.getLines().getLine(2));
            
            ow = new OutWriter();
            ow.write(new char[] {'x', 'y', '\n', 'z', 'z', 'z', '\n'});
            ow.flush();
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals("xy\n", ow.getLines().getLine(0));
            assertEquals("zzz\n", ow.getLines().getLine(1));
            
            
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
    }
    
    public void testWritePartial() {
        System.out.println("testWritePartial");
        try {
            OutWriter ow = new OutWriter ();

            ow.write('x');
            assertEquals(1, ow.getLines().getLineCount());
            assertEquals ("x", ow.getLines().getLine(0));
            ow.write('y');
            assertEquals ("xy", ow.getLines().getLine(0));
            ow.write('z');
            assertEquals ("xyz", ow.getLines().getLine(0));
            ow.println();
            assertEquals ("xyz\n", ow.getLines().getLine(0));
            ow.write('a');
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("a", ow.getLines().getLine(1));
            
            
            ow = new OutWriter();
            ow.write(new char[] { 'x', 'y', 'z', '\n', 'A'});
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("xyz\n", ow.getLines().getLine(0));
            assertEquals ("A", ow.getLines().getLine(1));
            ow.write('B');
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("AB", ow.getLines().getLine(1));
            ow.println("CD");
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("ABCD\n", ow.getLines().getLine(1));
            
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
        
    }
    


   
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
    private class CL implements ChangeListener {
        
        public void assertChanged () {
            ChangeEvent oldCE = ce;
            ce = null;
            assertTrue ("No change happened", oldCE != null);
        }
        
        public void assertNoChange() {
            ChangeEvent oldCE = ce;
            ce = null;
            assertFalse ("Change happened", oldCE != null);
        }
        
        private ChangeEvent ce = null;
        public void stateChanged(ChangeEvent changeEvent) {
            ce = changeEvent;
        }
        
    }
     
    
}
