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
 * WrappedTextViewTest.java
 * JUnit based test
 *
 * Created on July 8, 2004, 8:35 PM
 */

package org.netbeans.core.output2;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.text.View;
import junit.framework.*;
import junit.framework.*;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.netbeans.core.output2.ui.AbstractOutputTab;
import org.openide.ErrorManager;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Tim Boudreau
 */
public class OutputWindowTest extends TestCase {
    
    public OutputWindowTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(OutputWindowTest.class);
        return suite;
    }
    
    private OutputWindow win;
    private NbIO io;
    private OutWriter out = null;
    JFrame jf = null;
    NbIOProvider provider = null;

    protected void setUp() throws Exception {
        jf = new JFrame();
        win = new OutputWindow();
        OutputWindow.DEFAULT = win;
        jf.getContentPane().setLayout (new BorderLayout());
        jf.getContentPane().add (win, BorderLayout.CENTER);
        jf.setBounds (20, 20, 700, 300);
        provider = new NbIOProvider();
        io = (NbIO) provider.getIO ("Test", false);
        SwingUtilities.invokeAndWait (new Shower());
    }
    
    private final void sleep() {
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

    public void testTabsShownAndHidden() {
        System.out.println ("testTabsShownAndHidden");
        int n = win.getTabs().length;
        assertTrue ("Number of tabs should be 1, not " + n, n == 1);
        NbIO io2 = (NbIO) provider.getIO("Test2", true);
        sleep();
        n = win.getTabs().length;
        assertTrue ("Number of tabs should be 2, not " + n, n == 2);

        io2.closeInputOutput();
        sleep();

        n = win.getTabs().length;
        assertTrue ("After closeInputOutput on second tab, number of tabs should be 1, not " + n, n == 1);
    }
    
    public void testTabNamesUpdatedCorrectly() throws Exception {
        System.out.println ("testTabNamesUpdatedCorrectly");
        io.select();
        for (int i=0; i < 100; i++) {
            io.getOut().println ("Here is some text we can delete");
        }
        sleep();
        sleep();
        sleep();
        sleep();
        io.getOut().flush();
        
        assertTrue ("Tab name should be html but is " + win.getDisplayName(), 
            win.getDisplayName().indexOf("<") >= 0);

        io.getOut().close();
        sleep();
        sleep();
        assertFalse ("Tab name should not be html", win.getDisplayName().indexOf("<") >= 0);
        
        final OutputTab tab = (OutputTab) win.getSelectedTab();
        
        //Ensure the actions are paying attention
        SwingUtilities.invokeLater (new Runnable() {
            public void run() {
                win.getController().postPopupMenu(win, tab, new Point(0,0), tab);
                win.getController().clearAction.actionPerformed(new ActionEvent (tab, ActionEvent.ACTION_PERFORMED, "clear"));
            }
        });
        sleep();
        sleep();
        assertFalse ("Tab name should not be html", win.getDisplayName().indexOf("<") >= 0);
        
        io.getOut().reset();
        sleep();
        io.getOut().println("And here is some more text");
        sleep();
        
        assertTrue ("Tab name should be html", win.getDisplayName().indexOf("<") >= 0);

        io.getOut().close();
        sleep();
        sleep();
        assertFalse ("Tab name should not be html", win.getDisplayName().indexOf("<") >= 0);
        
    }

    public void testAbleToRetrieveSameInputOutputInstance() {
        System.out.println ("testAbleToRetrieveSameInputOutputInstance");
        NbIO io2 = (NbIO) provider.getIO("Test2", true);
        sleep();

        NbIO io3 = (NbIO) provider.getIO("Test", false);
        assertSame ("Requesting a tab with a name already in use should return the original InputOutput", io3, io);

        NbIO io4 = (NbIO) provider.getIO("Test2", false);
        assertSame ("Requesting a second tab with a name already in use should return the same InputOutput", io4, io2);

        NbIO io5 = (NbIO) provider.getIO("Test", true);
        assertNotSame ("Requesting a new InputOutput with a name matching another tab should not return the other tab", io5, io);
    }

    public void testIOclosed() {
        System.out.println ("testIOclosed");
        assertFalse ("If a tab is showing, its InputOutput should not say it is closed", io.isClosed());
        NbIO io2 = (NbIO) provider.getIO("Test2", true);
        sleep();
        assertFalse ("If a tab is showing, its InputOutput should not say it is closed", io2.isClosed());

        assertFalse ("Adding another tab should not make the first tab think it has been closed, its InputOutput should not say it is closed", io.isClosed());

        io2.closeInputOutput();
        sleep();

        assertTrue ("After programmatically closing a tab, its InputOutput should return true from isClosed()", io2.isClosed());

        io2 = (NbIO) provider.getIO("Test3", true);
        sleep();

        NbWriter writer = (NbWriter) io.getOut();
        writer.println ("Hello world");
        sleep();
        OutWriter outwriter = writer.out();

        assertFalse ("After removing and adding another tab, the last tab added should not say it has been closed", io2.isClosed());
        io.select();
        sleep();

        final OutputTab tab = (OutputTab) win.getSelectedTab();
        assertSame ("After calling InputOutput.select(), a tab owing the IO it was requested on should be the selected tab", tab.getIO(), io);

        //Ensure the actions are paying attention
        SwingUtilities.invokeLater (new Runnable() {
            public void run() {
                win.getController().postPopupMenu(win, tab, new Point(0,0), tab);
                win.getController().clearAction.actionPerformed(new ActionEvent (tab, ActionEvent.ACTION_PERFORMED, "clear"));
            }
        });
        sleep();
        sleep();

        io.getOut().println ("Goodbye world");

        assertSame ("OutputWriter should not be replaced by calling clearing/reset() on it", writer, io.getOut());
        assertNotSame ("Underlying OutWriter should be replaced by calling reset() on a used NbWriter", outwriter, writer.out());
    }


    public void testListenersCleared() throws Exception {
//        Controller.log = true;
//        Controller.logStdOut = true;
        
        System.out.println ("testListenersCleared");
        io.select();
        io.getOut().println ("Helloooooo....");
        sleep();
        L[] ls = new L[20];
        for (int i=0; i < ls.length; i++) {
            L l = new L();
            io.getOut().println ("Hyperlink " + i, l);
            ls[i] = l;
        }
        sleep();

        for (int i=0; i < ls.length; i++) {
            ls[i].assertNotCleared("Newly written listeners should be cleared");
        }

        sleep();
        
        final OutputTab tab = (OutputTab) win.getSelectedTab();
        assertNotNull ("Selected tab should not be null", tab);
        assertSame ("After calling InputOutput.select(), a tab owing the IO it was requested on should be the selected tab", tab.getIO(), io);

        //Ensure the actions are paying attention
        SwingUtilities.invokeLater (new Runnable() {
            public void run() {
                win.getController().postPopupMenu(win, tab, new Point(0,0), tab);
                win.getController().clearAction.actionPerformed(new ActionEvent (tab, ActionEvent.ACTION_PERFORMED, "clear"));
            }
        });
        sleep();
        sleep();

        for (int i=0; i < ls.length; i++) {
            ls[i].assertCleared("After invoking the GUI's clear action, listeners should be cleared");
        }

        L[] ls2 = new L[20];
        for (int i=0; i < ls2.length; i++) {
            L l = new L();
            io.getOut().println ("Second round of hyperlinks " + i, l);
            ls2[i] = l;
        }
        sleep();

        for (int i=0; i < ls2.length; i++) {
            ls2[i].assertNotCleared("The second round of listeners were cleared prematurely");
        }

        //Make sure the old ones are untouched
        for (int i=0; i < ls.length; i++) {
            //These should not have been touched.  The last assertCleared() cleared the cleared flag :-o
            ls[i].assertNotCleared("Already cleared listeners should be unreferenced and should not be cleared a second time");
        }

        //Ensure the actions are paying attention
        SwingUtilities.invokeLater (new Runnable() {
            public void run() {
                win.getController().postPopupMenu(win, tab, new Point(0,0), tab);
                win.getController().clearAction.actionPerformed(new ActionEvent (tab, ActionEvent.ACTION_PERFORMED, "clear"));
            }
        });
        sleep();
        sleep();

        for (int i=0; i < ls2.length; i++) {
            ls2[i].assertCleared("After invoking the Clear Output action a second time, the newly written listeners were not cleared - " + i);
        }

        //Make sure the old ones are untouched again - clearing a new set of lines should not
        //touch the old listeners - they are already forgotten
        for (int i=0; i < ls.length; i++) {
            //These should not have been touched.  The last assertCleared() cleared the cleared flag :-o
            ls[i].assertNotCleared("Already cleared listeners should not be touched by clearing or writing new data");
        }

        L[] ls3 = new L[20];
        for (int i=0; i < ls3.length; i++) {
            L l = new L();
            io.getOut().println ("Third round of hyperlinks " + i, l);
            ls3[i] = l;
        }
        sleep();

        for (int i=0; i < ls3.length; i++) {
            ls3[i].assertNotCleared("Third round of writes with listeners had those listeners prematurely cleared");
        }

        //This time we test it using reset(), not programmatically invoking the GUI call to do the same
        io.reset();
        sleep();
        sleep();

        for (int i=0; i < ls3.length; i++) {
            ls3[i].assertCleared("InputOutput.reset() should cause all OutputListeners to be cleared");
        }
        
        io = (NbIO) provider.getIO("Another tab", true);

        L[] ls4 = new L[20];
        for (int i=0; i < ls4.length; i++) {
            L l = new L();
            io.getOut().println ("Third round of hyperlinks " + i, l);
            ls4[i] = l;
        }
        sleep();

        for (int i=0; i < ls4.length; i++) {
            ls4[i].assertNotCleared("Premature clear");
        }

        io.getOut().close(); //Close the stream
        sleep();
        //And this time with closeInputOutput()
        io.closeInputOutput();
        sleep();
        sleep();
        sleep();
        sleep();
        sleep();

        for (int i=0; i < ls4.length; i++) {
            ls4[i].assertCleared("CloseInputOutput should cause all OutputListeners to be cleared");
        }
    }


    public class L implements OutputListener {
        private OutputEvent clearedEvent = null;
        public void assertCleared(String msg) {
            assertNotNull (msg, clearedEvent);
            clearedEvent = null;
        }

        public void assertNotCleared(String msg) {
            assertNull (msg, clearedEvent);
        }

        public void outputLineSelected(OutputEvent ev) {
        }

        public void outputLineAction(OutputEvent ev) {
        }

        public void outputLineCleared(OutputEvent ev) {
            clearedEvent = ev;
        }
    }
    
}
