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
 import javax.swing.*;
 import java.awt.*;
 import org.netbeans.core.output2.ui.*;
 import org.openide.windows.*;

/** Demo class for interactively testing changes */
 public class TestFrame extends JFrame implements Runnable {
    public static void main (String[] ignored) {
        new TestFrame().setVisible(true);
    }

    public TestFrame() {
        init();
    }

    private void writeContent() {
        System.err.println ("Writing content");
        io.getOut().println("This is an output window");
        for (int i=0; i < 100; i++) {
            out.println (i +  ": Wow, we will write a long line of text here.  Very long in fact - who knows just how long it" +
                " might end up being?  Well, we'll have to see.");

            io.getErr().println (i + ": This is a not so long line");
//            out.println (i + ": This, on the other hand, is a relatively short line");
        }
        out.println("And now we are done");
        out.flush();
        io.getErr().close();
        out.close();
        written = true;
    }

    private static boolean written = false;
    public void setVisible (boolean val) {
       boolean go = val != isVisible();
       super.setVisible(val);
       if (!SwingUtilities.isEventDispatchThread() && go) {
           try {
               Thread.currentThread().sleep (500);
                SwingUtilities.invokeLater(this);
           } catch (Exception e) {}
       }
    }

    private OutputWindow win;
    private NbIO io;
    private OutWriter out = null;
    private void init() {
        win = Controller.createOutputWindow();
        Controller.DEFAULT = win;
        getContentPane().setLayout (new BorderLayout());
        getContentPane().add (win, BorderLayout.CENTER);
        setBounds (20, 20, 300, 300);
        io = (NbIO) new NbIOProvider().getIO ("Test", false);
    }

    public void run () {
        if (SwingUtilities.isEventDispatchThread()) {
            out = (OutWriter) io.getOut();
           new Thread(this).start();
           out.println ("This is the first text");
           ((OutputPane) win.getSelectedTab().getOutputPane()).setWrapped(true);
        } else {
        try {
            Thread.currentThread().sleep(3000);
            } catch (Exception e) {}
            writeContent();
        }
    }


 }
