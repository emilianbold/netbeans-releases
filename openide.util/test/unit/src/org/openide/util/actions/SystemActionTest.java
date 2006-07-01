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

package org.openide.util.actions;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import javax.swing.Icon;
import javax.swing.JButton;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/** Test general aspects of system actions.
 * Currently, just the icon.
 * @author Jesse Glick
 */
public class SystemActionTest extends NbTestCase {

    public SystemActionTest(String name) {
        super(name);
    }
    
    /** Test that iconResource really works.
     * @see "#26887"
     */
    public void testIcons() throws Exception {
        Image i = Toolkit.getDefaultToolkit().getImage(SystemActionTest.class.getResource("data/someicon.gif"));
        int h = imageHash("Control icon", i, 16, 16);
        SystemAction a = SystemAction.get(SystemAction1.class);
        assertEquals("Absolute slash-initial iconResource works (though deprecated)", h, imageHash("icon1", icon2Image(a.getIcon()), 16, 16));
        a = SystemAction.get(SystemAction2.class);
        assertEquals("Absolute no-slash-initial iconResource works", h, imageHash("icon2", icon2Image(a.getIcon()), 16, 16));
        a = SystemAction.get(SystemAction3.class);
        assertEquals("Relative iconResource works", h, imageHash("icon3", icon2Image(a.getIcon()), 16, 16));
    }
    
    private static abstract class TestSystemAction extends SystemAction {
        public void actionPerformed(ActionEvent e) {}
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        public String getName() {
            return getClass().getName();
        }
    }
    public static final class SystemAction1 extends TestSystemAction {
        protected String iconResource() {
            return "/org/openide/util/actions/data/someicon.gif";
        }
    }
    public static final class SystemAction2 extends TestSystemAction {
        protected String iconResource() {
            return "org/openide/util/actions/data/someicon.gif";
        }
    }
    public static final class SystemAction3 extends TestSystemAction {
        protected String iconResource() {
            return "data/someicon.gif";
        }
    }
    
    private static Image icon2Image(Icon ico) {
        int w = ico.getIconWidth();
        int h = ico.getIconHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        ico.paintIcon(new JButton(), img.getGraphics(), 0, 0);
        return img;
    }
    
    // Copied from SystemFileSystemTest:
    private static int imageHash(String name, Image img, int w, int h) throws InterruptedException {
        int[] pixels = new int[w * h];
        PixelGrabber pix = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        pix.grabPixels();
        assertEquals(0, pix.getStatus() & ImageObserver.ABORT);
        if (false) {
            // Debugging.
            System.out.println("Pixels of " + name + ":");
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (x == 0) {
                        System.out.print('\t');
                    } else {
                        System.out.print(' ');
                    }
                    int p = pixels[y * w + x];
                    String hex = Integer.toHexString(p);
                    while (hex.length() < 8) {
                        hex = "0" + hex;
                    }
                    System.out.print(hex);
                    if (x == w - 1) {
                        System.out.print('\n');
                    }
                }
            }
        }
        int hash = 0;
        for (int i = 0; i < pixels.length; i++) {
            hash += 172881;
            int p = pixels[i];
            if ((p & 0xff000000) == 0) {
                // Transparent; normalize.
                p = 0;
            }
            hash ^= p;
        }
        return hash;
    }
    
}
