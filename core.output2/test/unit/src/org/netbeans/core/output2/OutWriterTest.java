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

package org.netbeans.core.output2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;

/** Tests the OutWriter class
 *
 * @author Tim Boudreau
 */
public class OutWriterTest extends NbTestCase {

    public OutWriterTest(String testName) {
        super(testName);
    }

    public void testPositionOfLine() throws UnsupportedEncodingException {
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
        
        int expectedPosition = first.length() + OutWriter.LINE_SEPARATOR.length();
        pos = ow.getLines().getLineStart(1);
        
        assertTrue ("Second line position should be length of first (" + first.length() + ") + line " +
            "separator length (" + OutWriter.LINE_SEPARATOR.length() + "), which should be " +
            expectedPosition + " but is " + pos, 
            pos == expectedPosition);
         
        
        pos = ow.getLines().getLineStart (2);
        int targetPos = first.length() + second.length() + 2 * OutWriter.LINE_SEPARATOR.length();
        
        assertTrue ("Third line position should be " + targetPos + " but is " + pos, pos == targetPos);
    }
    
    
    public void testPosition() throws UnsupportedEncodingException {
        System.out.println("testPosition");
        
        OutWriter ow = new OutWriter();

        
        String first = "This is the first string";
        String second ="This is the second string";
        String third = "This is the third string";
        
        assertTrue (ow.getLines().getLineCount() == 1);
        
        ow.println(first);
        
        assertTrue (ow.getLines().getLineCount() == 2);
        
        ow.println (second);
        
        assertTrue (ow.getLines().getLineCount() == 3);

        int targetLength = first.length() + second.length() + 2 * OutWriter.LINE_SEPARATOR.length();

        assertTrue ( 
            "After printing strings with length " + first.length() + " and " + 
            second.length() + " outfile position should be " + targetLength +
            " not " + ow.getLines().getCharCount(),
            ow.getLines().getCharCount() == targetLength);
        
        ow.println (third);
        
        targetLength = first.length() + second.length() + third.length() + 
            (3 *  OutWriter.LINE_SEPARATOR.length());
        
        assertTrue ("Length should be " + targetLength + " but position is "
            + ow.getLines().getCharCount(), targetLength == ow.getLines().getCharCount());
    }
    
    public void testLine() throws UnsupportedEncodingException {
        System.out.println("testLine");
        
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        assertTrue ("After writing 3 lines, linecount should be 4, not " + 
            ow.getLines().getLineCount(), ow.getLines().getLineCount() == 4);
        
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
        String firstExpected = first + OutWriter.LINE_SEPARATOR;
        String secondExpected = second + OutWriter.LINE_SEPARATOR;
        String thirdExpected = third + OutWriter.LINE_SEPARATOR;
        
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
        
        line = ow.getLines().getLineAt (first.length() + OutWriter.LINE_SEPARATOR.length() +
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
        String appended = "Appended string to last line";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        ow.flush();
        processEQ();
        Thread.yield();
        
        assertTrue ("Linecount should be 4 after printing 3 lines, not " +
            ow.getLines().getLineCount(), ow.getLines().getLineCount() == 4);
        
        ow.print(appended);
        ow.print(appended);
        ow.flush();

        assertTrue("Linecount should be still 4 after appending text to last line, not " +
                ow.getLines().getLineCount(), ow.getLines().getLineCount() == 4);

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
        processEQ();
        cl.assertChanged();
    }

    public void testMultilineText() {
        System.out.println("testMultilineText");
        OutWriter ow = new OutWriter ();
        String threeLines = "This is\nthree lines of\nText";
        ow.print(threeLines);
        assertTrue ("Line count should be 3, not " + ow.getLines().getLineCount(), ow.getLines().getLineCount() == 3);
        ow.println("This is another line");
        assertTrue ("Line count should be 4, not " + ow.getLines().getLineCount(), ow.getLines().getLineCount() == 4);
        ow.println(threeLines);
        assertTrue ("Line count should be 7, not " + ow.getLines().getLineCount(), ow.getLines().getLineCount() == 7);
    }

    @RandomlyFails // NB-Core-Build #1887
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

        cl.clear();
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
            
            String firstExpected = first + OutWriter.LINE_SEPARATOR;
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
            assertEquals(2, ow.getLines().getLineCount());
            String firstReceived = ow.getLines().getLine(0);
            assertEquals ("xyz" + OutWriter.LINE_SEPARATOR, firstReceived);
        
            ow = new OutWriter();
            ow.println("firstline");
            ow.write('x');
            ow.println("yz");
            ow.flush();
            assertEquals(3, ow.getLines().getLineCount());
            firstReceived = ow.getLines().getLine(1);
            assertEquals ("xyz" + OutWriter.LINE_SEPARATOR, firstReceived);
            
            ow = new OutWriter();
            ow.println("firstline");
            ow.write(new char[] {'x', 'y', 'z'});
            ow.write(new char[] {'x', 'y', 'z'});
            ow.println("-end");
            ow.flush();
            assertEquals(3, ow.getLines().getLineCount());
            firstReceived = ow.getLines().getLine(1);
            assertEquals ("xyzxyz-end" + OutWriter.LINE_SEPARATOR, firstReceived);
            
            ow = new OutWriter();
            ow.write(new char[] {'x', 'y', '\n', 'z', 'z', 'z', '\n', 'A'});
            ow.flush();
            assertEquals(3, ow.getLines().getLineCount());
            assertEquals("xy" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            assertEquals("zzz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(1));
            assertEquals("A", ow.getLines().getLine(2));
            
            ow = new OutWriter();
            ow.write(new char[] {'x', 'y', '\n', 'z', 'z', 'z', '\n'});
            ow.flush();
            assertEquals(3, ow.getLines().getLineCount());
            assertEquals("xy" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            assertEquals("zzz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(1));
            
            ow = new OutWriter();
            ow.write(new char[] {'\n', '\n', '\n', 'z', 'z', 'z', '\n'});
            ow.flush();
            assertEquals(5, ow.getLines().getLineCount());
            assertEquals(OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            assertEquals(OutWriter.LINE_SEPARATOR, ow.getLines().getLine(1));
            assertEquals(OutWriter.LINE_SEPARATOR, ow.getLines().getLine(2));
            assertEquals("zzz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(3));
            
            
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
            assertEquals ("xyz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            ow.write('a');
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("a", ow.getLines().getLine(1));
            
            
            ow = new OutWriter();
            ow.write(new char[] { 'x', 'y', 'z', '\n', 'A'});
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("xyz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            assertEquals ("A", ow.getLines().getLine(1));
            ow.write('B');
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("AB", ow.getLines().getLine(1));
            ow.println("CD");
            assertEquals(3, ow.getLines().getLineCount());
            assertEquals ("ABCD" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(1));
            
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
        
    }
    
    void processEQ() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    System.currentTimeMillis();
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

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
        
        void clear() {
            ce = null;
        }
    }
}
