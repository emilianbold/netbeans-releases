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
public class WrappedTextViewTest extends TestCase {
    
    public WrappedTextViewTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(WrappedTextViewTest.class);
        return suite;
    }
    
    private OutputWindow win;
    private NbIO io;
    private OutWriter out = null;
    JFrame jf = null;

    protected void setUp() throws java.lang.Exception {
        jf = new JFrame();
        win = new OutputWindow();
        OutputWindow.DEFAULT = win;
        jf.getContentPane().setLayout (new BorderLayout());
        jf.getContentPane().add (win, BorderLayout.CENTER);
        jf.setBounds (20, 20, 700, 300);
        io = (NbIO) new NbIOProvider().getIO ("Test", false);
        SwingUtilities.invokeAndWait (new Shower());
        sleep();
        io.getOut().println ("Test line 1");
        sleep();
        sleep();
        sleep();
        
        for (int i=0; i < 100; i++) {
            if (i == 42) {
                io.getOut().println ("This is a hyperlink to click which may or may not trigger the problem", new L());
            }
            if (i % 2 == 0) {
                io.getOut().println ("Hello there.  What a short line");
                io.getOut().println("Splead 2 - 148: Wow, we will write a long line of text here.  Very long in fact - who knows just how long it might end up being?  Well, we'll have to see.  Why it's extraordinarily long!  It might even wrap several times!  How do you like them apples, eh?  Maybe we should just go on and on and on, and never stop.  That would be cool, huh?\n");
            } else {
                //io.getErr().println ("aaa: This is a not so long line");
            }
        }
        io.getOut().close();
        sleep();
        
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                win.getSelectedTab().getOutputPane().setWrapped(true);
            }
        });
        
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
            jf.show();
        }
    }
    
    public void testModelToView() throws Exception {
        System.out.println("testModelToView");
        
        if (true) {
            //THIS TEST TAKES ABOUT 10 MINUTES TO RUN!  LEAVE IT COMMENTED OUT
            //FOR PRODUCTION AND USE IT JUST FOR DEBUGGING
            return;
        }
        
        AbstractOutputTab tab = win.getSelectedTab();
        AbstractOutputPane pane = tab.getOutputPane();
        JTextComponent text = pane.getTextView();
        
        View view = text.getUI().getRootView(text);
        
        Rectangle r = new Rectangle(1,1,1,1);
        Rectangle alloc = new Rectangle();
        
        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().
                                          getDefaultScreenDevice().getDefaultConfiguration().getColorModel(java.awt.Transparency.TRANSLUCENT);
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(model,
                model.createCompatibleWritableRaster(text.getWidth() + 10, text.getHeight() + 10), model.isAlphaPremultiplied(), null);
        
        text.paint (img.getGraphics());
        boolean errorsFound = false;
        ArrayList errors = new ArrayList();
        
        System.out.println("...scanning " + (text.getWidth() * text.getHeight() + " pixels to make sure viewToModel() matches modeltoView().  Expect it to take about 10 minutes."));
        
        for (int y=0; y < text.getHeight(); y++) {
            r.y = y;
            for (int x=0; x < text.getWidth(); x++) {
                r.x = x;
                alloc.setBounds (0, 0, text.getWidth(), text.getHeight());
                
                int vtm = view.viewToModel (x, y, alloc, new Position.Bias[1]);
                
                Rectangle mtv = (Rectangle) view.modelToView (vtm, Position.Bias.Forward, vtm, Position.Bias.Forward, new Rectangle (0, 0, text.getWidth(), text.getHeight()));
                
                int xvtm = view.viewToModel (mtv.x, mtv.y, alloc, new Position.Bias[1]);

                if (vtm != xvtm) {
                    errorsFound = true;
                    try {
                        errors.add ("ViewToModel(" + x + "," + y + ") returns character position " + vtm + "; modelToView on " + vtm + " returns " + mtv + "; that Rectangle's corner, passed back to viewToModel maps to a different character position: " + xvtm + "\n");
                        img.setRGB(x, y, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                        img.setRGB(x-1, y-1, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                        img.setRGB(x+1, y-1, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                        img.setRGB(x+1, y+1, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                        img.setRGB(x-1, y+1, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        System.err.println("OUT OF BOUNDS: " + x + "," + y + " image width " + img.getWidth() + " img height " + img.getHeight());
                    }
                        
                    System.err.println(x + "," + y + "=" + vtm + " -> [" + mtv.x + "," + mtv.y + "," + mtv.width + "," + mtv.height + "]->" + xvtm);
                }
                
                r.y = y; //just in case
                r.width = 1;
                r.height = 1;
            }
        }
        
        if (errorsFound) {
            String dir = System.getProperty ("java.io.tmpdir");
            if (!dir.endsWith(File.separator)) {
                dir += File.separator;
            }
            String fname = dir + "outputWindowDiffs.png";
            ImageIO.write (img, "png", new File (fname));
            fail ("In a wrapped view, some points as mapped by viewToModel do " +
                "not map back to the same coordinates in viewToModel.  \nA bitmap " +
                "of the problem coordinates is saved in " + fname + "  Problem" +
                "spots are marked in red and blue.\n" + errors);
        }
        

        
/*        
        try {
            Thread.currentThread().sleep (40000);
        } catch (Exception e) {}
 */
        
//        WrappedTextView wtv = win.getSelectedTab().getOutputPane().getTextView().getUI().getView();
    }



    public class L implements OutputListener {

        public void outputLineSelected(OutputEvent ev) {
        }

        public void outputLineAction(OutputEvent ev) {
        }

        public void outputLineCleared(OutputEvent ev) {
        }

    }    
    
}
