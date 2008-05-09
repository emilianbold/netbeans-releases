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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import junit.framework.TestCase;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.netbeans.core.output2.ui.AbstractOutputTab;
import org.openide.util.Utilities;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Tim Boudreau
 */
public class WrappedTextViewTest extends TestCase {
    
    public WrappedTextViewTest(String testName) {
        super(testName);
    }
    
    private OutputWindow win;
    private NbIO io;
    private OutWriter out = null;
    JFrame jf = null;
    static int testNum;

    protected void setUp() throws Exception {
        jf = new JFrame();
        win = new OutputWindow();
        OutputWindow.DEFAULT = win;
        jf.getContentPane().setLayout (new BorderLayout());
        jf.getContentPane().add (win, BorderLayout.CENTER);
        jf.setBounds (20, 20, 700, 300);
        io = (NbIO) new NbIOProvider().getIO ("Test" + testNum++, false);
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
            jf.setVisible(true);
        }
    }
    
    /**
     * tests if caret position is computed correctly (see issue #122492)
     */
    public void testViewToModel() {
        Graphics g = win.getSelectedTab().getOutputPane().getGraphics();
        FontMetrics fm = g.getFontMetrics(win.getSelectedTab().getOutputPane().getTextView().getFont());
        int charWidth = fm.charWidth('m');
        int charHeight = fm.getHeight();
        int fontDescent = fm.getDescent();
        float x = charWidth * 50;
        float y = charHeight * 1 + fontDescent;
        int charPos = win.getSelectedTab().getOutputPane().getTextView().getUI().getRootView(null).viewToModel(x, y, new Rectangle(), new Position.Bias[]{});
        int expCharPos = (Utilities.getOperatingSystem() & Utilities.OS_WINDOWS_MASK) != 0 ? 45 : 43;
        assertTrue("viewToModel returned wrong value (it would result in bad caret position)!", charPos == expCharPos);
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
